package co.fcv.citas.application.booking;

import co.fcv.citas.application.booking.BookingCommands.AvailabilityQuery;
import co.fcv.citas.application.booking.BookingCommands.CancelAppointment;
import co.fcv.citas.application.booking.BookingCommands.CreateAppointment;
import co.fcv.citas.application.booking.BookingCommands.Decision;
import co.fcv.citas.application.booking.BookingCommands.RequestReschedule;
import co.fcv.citas.application.booking.BookingCommands.RescheduleDecision;
import co.fcv.citas.application.booking.BookingPorts.*;
import co.fcv.citas.domain.appointment.AppointmentStatus;
import co.fcv.citas.domain.appointment.RescheduleStatus;
import co.fcv.citas.domain.appointment.StatusSource;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

public class BookingService {
    private final BookingRepository bookings;
    private final Clock clock;

    public BookingService(BookingRepository bookings, Clock clock) { this.bookings = bookings; this.clock = clock; }

    public List<Location> locations() { return bookings.findActiveLocations(); }
    public List<Specialty> specialties() { return bookings.findActiveSpecialties(); }
    public List<Professional> professionals(long locationId, long specialtyId) { return bookings.findProfessionals(locationId, specialtyId); }

    public List<Availability> availability(AvailabilityQuery query) {
        ReservationContext context = bookings.findReservationContext(query.locationId(), query.specialtyId(), query.professionalId())
                .orElseThrow(() -> invalid("La especialidad, sede y profesional no forman una combinación habilitada."));
        if (query.date().isBefore(LocalDateTime.now(clock).toLocalDate())) return List.of();
        return contiguousAvailable(bookings.findSlots(query.professionalId(), query.locationId(), query.date(), false), context.specialty().durationMinutes());
    }

    @Transactional
    public CreatedAppointment create(CreateAppointment command) {
        if (!bookings.isActiveUser(command.patientUserId())) throw invalid("El usuario solicitante no está activo.");
        ReservationContext context = bookings.findReservationContext(command.locationId(), command.specialtyId(), command.professionalId())
                .orElseThrow(() -> invalid("La especialidad, sede y profesional no forman una combinación habilitada."));
        LocalDateTime now = LocalDateTime.now(clock);
        if (!command.startAt().isAfter(now) || !isHalfHour(command.startAt())) throw invalid("La franja debe ser futura y comenzar en un intervalo de 30 minutos.");
        List<Slot> matching = matchingSlots(bookings.findSlots(command.professionalId(), command.locationId(), command.startAt().toLocalDate(), true),
                command.startAt(), context.specialty().durationMinutes());
        if (matching.size() != context.specialty().durationMinutes() / 30 || matching.stream().anyMatch(slot -> !slot.free())) {
            throw new BookingException(BookingException.Reason.SLOT_UNAVAILABLE, "La franja ya no está disponible.");
        }
        AppointmentStatus status = context.specialty().requiresAdminApproval() ? AppointmentStatus.REQUESTED : AppointmentStatus.APPROVED;
        LocalDateTime endAt = matching.getLast().endAt();
        long id = bookings.createAppointment(new NewAppointment(command.patientUserId(), command.professionalId(), command.locationId(), command.specialtyId(), status, command.startAt(), endAt));
        bookings.assignSlots(id, matching.stream().map(Slot::id).toList());
        bookings.addHistory(id, status, status == AppointmentStatus.REQUESTED ? command.patientUserId() : null,
                status == AppointmentStatus.REQUESTED ? StatusSource.USER : StatusSource.SYSTEM,
                status == AppointmentStatus.REQUESTED ? "Solicitud especializada creada" : "Aprobación automática de Medicina General");
        return new CreatedAppointment(id, status, command.startAt(), endAt, context.specialty().durationMinutes());
    }

    public List<PendingAppointment> pending() { return bookings.findPending(); }

    public List<MyAppointmentView> myAppointments(String userId, AppointmentStatus status, java.time.LocalDate date) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<MyAppointment> appointments = bookings.findMyAppointments(userId, status, date);
        Map<Long, RescheduleView> reschedules = latestReschedules(appointments.stream().map(MyAppointment::id).toList());
        return appointments.stream().map(value -> view(value, value.status(), now, reschedules.get(value.id()))).toList();
    }

    private Map<Long, RescheduleView> latestReschedules(List<Long> appointmentIds) {
        if (appointmentIds.isEmpty()) return Map.of();
        return bookings.findLatestRescheduleRequests(appointmentIds).stream()
                .collect(Collectors.toMap(RescheduleRequest::appointmentId, BookingService::rescheduleView, (first, second) -> first, HashMap::new));
    }

    @Transactional
    public MyAppointmentView cancel(CancelAppointment command) {
        MyAppointment appointment = bookings.lockAppointmentForUser(command.appointmentId(), command.userId())
                .orElseThrow(() -> new BookingException(BookingException.Reason.NOT_FOUND, "La cita no existe."));
        LocalDateTime now = LocalDateTime.now(clock);
        if (appointment.terminal() || !appointment.startAt().isAfter(now)) {
            throw new BookingException(BookingException.Reason.INVALID_STATE, "La cita no se puede cancelar por su fecha o estado actual.");
        }
        bookings.cancel(appointment.id());
        bookings.releaseSlots(appointment.id());
        bookings.addHistory(appointment.id(), AppointmentStatus.CANCELLED, command.userId(), StatusSource.USER, null);
        return view(appointment, AppointmentStatus.CANCELLED, now, latestReschedules(List.of(appointment.id())).get(appointment.id()));
    }

    /**
     * HU-020. Conserva la cita original y retiene la nueva franja mediante
     * {@code professional_slots.reschedule_request_id} dentro de una sola transacción (RF-15, RN-01, RN-05, RN-10).
     */
    @Transactional
    public RescheduleRequestDetail requestReschedule(RequestReschedule command) {
        MyAppointment appointment = bookings.lockAppointmentForUser(command.appointmentId(), command.userId())
                .orElseThrow(() -> new BookingException(BookingException.Reason.NOT_FOUND, "La cita no existe."));
        if (appointment.professionalId() != command.professionalId() || appointment.specialtyId() != command.specialtyId()) {
            throw invalid("La reprogramación conserva el mismo profesional y la misma especialidad.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (appointment.status() != AppointmentStatus.APPROVED || !appointment.startAt().isAfter(now)) {
            throw new BookingException(BookingException.Reason.INVALID_STATE, "Solo una cita aprobada y futura admite reprogramación.");
        }
        if (bookings.hasPendingRescheduleRequest(appointment.id())) {
            throw new BookingException(BookingException.Reason.INVALID_STATE, "La cita ya tiene una solicitud de reprogramación pendiente.");
        }
        ReservationContext context = bookings.findReservationContext(command.locationId(), command.specialtyId(), command.professionalId())
                .orElseThrow(() -> invalid("La especialidad, sede y profesional no forman una combinación habilitada."));
        if (!command.startAt().isAfter(now) || !isHalfHour(command.startAt())) {
            throw invalid("La nueva franja debe ser futura y comenzar en un intervalo de 30 minutos.");
        }
        List<Slot> matching = matchingSlots(bookings.findSlots(command.professionalId(), command.locationId(), command.startAt().toLocalDate(), true),
                command.startAt(), context.specialty().durationMinutes());
        if (matching.size() != context.specialty().durationMinutes() / 30 || matching.stream().anyMatch(slot -> !slot.free())) {
            throw new BookingException(BookingException.Reason.SLOT_UNAVAILABLE, "La nueva franja ya no está disponible.");
        }
        LocalDateTime endAt = matching.getLast().endAt();
        if (command.startAt().isBefore(appointment.endAt()) && appointment.startAt().isBefore(endAt)) {
            throw new BookingException(BookingException.Reason.SLOT_UNAVAILABLE, "La nueva franja se solapa con la franja actual de la cita.");
        }
        long id = bookings.createRescheduleRequest(new NewRescheduleRequest(appointment.id(), command.userId(), command.locationId(),
                appointment.startAt(), appointment.endAt(), command.startAt(), endAt));
        bookings.holdSlots(id, matching.stream().map(Slot::id).toList());
        bookings.addHistory(appointment.id(), appointment.status(), command.userId(), StatusSource.USER,
                "Reprogramación solicitada para " + command.startAt());
        return bookings.findRescheduleRequestDetail(id)
                .orElseThrow(() -> new BookingException(BookingException.Reason.NOT_FOUND, "La solicitud de reprogramación no existe."));
    }

    public List<RescheduleRequestDetail> rescheduleRequests(RescheduleStatus status) {
        return bookings.findRescheduleRequests(status);
    }

    /**
     * HU-021. Aprobar sustituye la franja de forma atómica; rechazar libera solo la retención nueva
     * y conserva la cita original (RN-09, RN-10, RN-11). El motivo sigue DEC-005.
     */
    @Transactional
    public RescheduleRequestDetail decideReschedule(RescheduleDecision command) {
        RescheduleRequestDetail request = bookings.lockRescheduleRequest(command.rescheduleRequestId())
                .orElseThrow(() -> new BookingException(BookingException.Reason.NOT_FOUND, "La solicitud de reprogramación no existe."));
        if (request.status() != RescheduleStatus.PENDING) {
            throw new BookingException(BookingException.Reason.INVALID_STATE, "La solicitud de reprogramación ya fue decidida.");
        }
        boolean reject = command.type() == BookingCommands.DecisionType.REJECT;
        String reason = command.reason() == null || command.reason().trim().isEmpty() ? null : command.reason().trim();
        if (reject && reason == null) throw invalid("El motivo es obligatorio para rechazar una reprogramación.");
        LocalDateTime now = LocalDateTime.now(clock);
        if (reject) {
            bookings.releaseHeldSlots(request.id());
            bookings.decideRescheduleRequest(request.id(), RescheduleStatus.REJECTED, command.adminUserId(), now, reason);
        } else {
            bookings.releaseSlotsInRange(request.appointmentId(), request.previousStartAt(), request.previousEndAt());
            bookings.confirmHeldSlots(request.id(), request.appointmentId());
            bookings.moveAppointmentWindow(request.appointmentId(), request.requestedStartAt(), request.requestedEndAt());
            bookings.decideRescheduleRequest(request.id(), RescheduleStatus.APPROVED, command.adminUserId(), now, reason);
        }
        bookings.addHistory(request.appointmentId(), AppointmentStatus.APPROVED, command.adminUserId(), StatusSource.ADMIN,
                reason != null ? reason : "Reprogramación aprobada por ADMIN hacia " + request.requestedStartAt());
        return bookings.findRescheduleRequestDetail(request.id())
                .orElseThrow(() -> new BookingException(BookingException.Reason.NOT_FOUND, "La solicitud de reprogramación no existe."));
    }

    public List<AgendaAppointmentView> agenda(BookingCommands.AgendaQuery query) {
        long professionalId = requireProfessional(query.professionalUserId());
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime rangeStart = query.range() == BookingCommands.AgendaRange.WEEK
                ? query.date().minusDays(query.date().getDayOfWeek().getValue() - 1).atStartOfDay()
                : query.date().atStartOfDay();
        LocalDateTime rangeEnd = query.range() == BookingCommands.AgendaRange.WEEK ? rangeStart.plusDays(7) : rangeStart.plusDays(1);
        return bookings.findAgenda(professionalId, rangeStart, rangeEnd, query.locationId()).stream().map(value -> agendaView(value, now)).toList();
    }

    @Transactional
    public AgendaAppointmentView close(BookingCommands.CloseAppointment command) {
        if (command.outcome() != AppointmentStatus.COMPLETED && command.outcome() != AppointmentStatus.NO_SHOW) {
            throw invalid("El resultado debe ser COMPLETED o NO_SHOW.");
        }
        long professionalId = requireProfessional(command.professionalUserId());
        AgendaAppointment appointment = bookings.lockAppointmentForProfessional(command.appointmentId(), professionalId)
                .orElseThrow(() -> new BookingException(BookingException.Reason.NOT_FOUND, "La cita no existe."));
        LocalDateTime now = LocalDateTime.now(clock);
        if (appointment.status() != AppointmentStatus.APPROVED || appointment.endAt().isAfter(now)) {
            throw new BookingException(BookingException.Reason.INVALID_STATE, "La cita no es elegible para cierre por su fecha o estado actual.");
        }
        bookings.close(appointment.id(), command.outcome());
        bookings.addHistory(appointment.id(), command.outcome(), command.professionalUserId(), StatusSource.USER, null);
        return agendaView(new AgendaAppointment(appointment.id(), appointment.patientName(), appointment.locationId(), appointment.locationName(),
                appointment.specialtyId(), appointment.specialtyName(), appointment.startAt(), appointment.endAt(), appointment.durationMinutes(),
                command.outcome()), now);
    }

    private long requireProfessional(String userId) {
        return bookings.findProfessionalId(userId)
                .orElseThrow(() -> new BookingException(BookingException.Reason.NOT_FOUND, "No existe un profesional asociado a este usuario."));
    }

    private static AgendaAppointmentView agendaView(AgendaAppointment value, LocalDateTime now) {
        boolean closureAllowed = value.status() == AppointmentStatus.APPROVED && !value.endAt().isAfter(now);
        return new AgendaAppointmentView(value.id(), value.patientName(), value.locationId(), value.locationName(),
                value.specialtyId(), value.specialtyName(), value.startAt(), value.endAt(), value.durationMinutes(), value.status(), closureAllowed);
    }

    private static MyAppointmentView view(MyAppointment value, AppointmentStatus status, LocalDateTime now, RescheduleView reschedule) {
        boolean future = value.startAt().isAfter(now);
        boolean active = !value.terminal() && status != AppointmentStatus.CANCELLED;
        boolean reschedulePending = reschedule != null && reschedule.status() == RescheduleStatus.PENDING;
        return new MyAppointmentView(value.id(), value.locationId(), value.locationName(), value.professionalId(), value.professionalName(),
                value.specialtyId(), value.specialtyName(), value.startAt(), value.endAt(), value.durationMinutes(), status,
                status == AppointmentStatus.REJECTED ? value.rejectionReason() : null,
                active && future,
                status == AppointmentStatus.APPROVED && future && !reschedulePending,
                reschedule);
    }

    private static RescheduleView rescheduleView(RescheduleRequest value) {
        return new RescheduleView(value.id(), value.status(), value.requestedStartAt(), value.requestedEndAt(),
                value.decisionReason(), value.decidedAt());
    }

    @Transactional
    public CreatedAppointment decide(Decision command) {
        PendingAppointment pending = bookings.lockRequestedAppointment(command.appointmentId())
                .orElseThrow(() -> new BookingException(BookingException.Reason.NOT_FOUND, "La solicitud pendiente no existe."));
        if (command.type() == BookingCommands.DecisionType.REJECT && (command.reason() == null || command.reason().trim().isEmpty())) {
            throw invalid("El motivo es obligatorio para rechazar una solicitud.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        AppointmentStatus status = command.type() == BookingCommands.DecisionType.APPROVE ? AppointmentStatus.APPROVED : AppointmentStatus.REJECTED;
        if (status == AppointmentStatus.APPROVED) bookings.approve(pending.id(), command.adminUserId(), now);
        else { bookings.reject(pending.id()); bookings.releaseSlots(pending.id()); }
        bookings.addHistory(pending.id(), status, command.adminUserId(), StatusSource.ADMIN,
                status == AppointmentStatus.REJECTED ? command.reason().trim() : "Solicitud aprobada por ADMIN");
        return new CreatedAppointment(pending.id(), status, pending.startAt(), pending.endAt(), pending.durationMinutes());
    }

    private static List<Availability> contiguousAvailable(List<Slot> slots, int durationMinutes) {
        List<Availability> result = new ArrayList<>();
        for (Slot slot : slots) if (slot.free()) {
            List<Slot> matching = matchingSlots(slots, slot.startAt(), durationMinutes);
            if (matching.size() == durationMinutes / 30 && matching.stream().allMatch(Slot::free))
                result.add(new Availability(slot.startAt(), matching.getLast().endAt()));
        }
        return result;
    }

    private static List<Slot> matchingSlots(List<Slot> slots, LocalDateTime startAt, int durationMinutes) {
        int expected = durationMinutes / 30;
        List<Slot> result = new ArrayList<>();
        LocalDateTime cursor = startAt;
        for (Slot slot : slots) {
            if (slot.startAt().equals(cursor)) { result.add(slot); cursor = slot.endAt(); if (result.size() == expected) return result; }
            else if (!result.isEmpty()) return List.of();
        }
        return List.of();
    }

    private static boolean isHalfHour(LocalDateTime value) { return value.getMinute() % 30 == 0 && value.getSecond() == 0 && value.getNano() == 0; }
    private static BookingException invalid(String message) { return new BookingException(BookingException.Reason.INVALID_REQUEST, message); }

    public record Availability(LocalDateTime startAt, LocalDateTime endAt) { }
    public record CreatedAppointment(long id, AppointmentStatus status, LocalDateTime startAt, LocalDateTime endAt, int durationMinutes) { }
    public record MyAppointmentView(long id, long locationId, String locationName, long professionalId, String professionalName,
                                    long specialtyId, String specialtyName, LocalDateTime startAt, LocalDateTime endAt,
                                    int durationMinutes, AppointmentStatus status, String rejectionReason, boolean cancellationAllowed,
                                    boolean rescheduleAllowed, RescheduleView rescheduleRequest) { }
    public record RescheduleView(long id, RescheduleStatus status, LocalDateTime requestedStartAt, LocalDateTime requestedEndAt,
                                 String decisionReason, LocalDateTime decidedAt) { }
    public record AgendaAppointmentView(long id, String patientName, long locationId, String locationName,
                                        long specialtyId, String specialtyName, LocalDateTime startAt, LocalDateTime endAt,
                                        int durationMinutes, AppointmentStatus status, boolean closureAllowed) { }
}
