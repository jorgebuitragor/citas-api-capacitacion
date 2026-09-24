package co.fcv.citas.application.booking;

import co.fcv.citas.application.booking.BookingCommands.AvailabilityQuery;
import co.fcv.citas.application.booking.BookingCommands.CreateAppointment;
import co.fcv.citas.application.booking.BookingCommands.Decision;
import co.fcv.citas.application.booking.BookingPorts.*;
import co.fcv.citas.domain.appointment.AppointmentStatus;
import co.fcv.citas.domain.appointment.StatusSource;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        if (matching.size() != context.specialty().durationMinutes() / 30 || matching.stream().anyMatch(slot -> slot.appointmentId() != null)) {
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
        for (Slot slot : slots) if (slot.appointmentId() == null) {
            List<Slot> matching = matchingSlots(slots, slot.startAt(), durationMinutes);
            if (matching.size() == durationMinutes / 30 && matching.stream().allMatch(candidate -> candidate.appointmentId() == null))
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
}
