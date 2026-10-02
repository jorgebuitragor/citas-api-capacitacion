package co.fcv.citas.adapter.in.web;

import co.fcv.citas.application.booking.BookingCommands;
import co.fcv.citas.application.booking.BookingException;
import co.fcv.citas.application.booking.BookingService;
import co.fcv.citas.application.booking.BookingPorts;
import co.fcv.citas.domain.appointment.AppointmentStatus;
import co.fcv.citas.domain.appointment.RescheduleStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BookingController {
    private final BookingService bookings;

    public BookingController(BookingService bookings) { this.bookings = bookings; }

    @GetMapping("/catalogs/locations")
    public List<LocationResponse> locations() { return bookings.locations().stream().map(value -> new LocationResponse(id(value.id()), value.code(), value.name())).toList(); }

    @GetMapping("/catalogs/specialties")
    public List<SpecialtyResponse> specialties() {
        return bookings.specialties().stream().map(value -> new SpecialtyResponse(id(value.id()), value.code(), value.name(), value.durationMinutes(), value.general(), value.requiresAdminApproval())).toList();
    }

    @GetMapping("/catalogs/professionals")
    public List<ProfessionalResponse> professionals(@RequestParam @Positive long locationId, @RequestParam @Positive long specialtyId) {
        return bookings.professionals(locationId, specialtyId).stream().map(value -> new ProfessionalResponse(id(value.id()), value.name(), value.code())).toList();
    }

    @GetMapping("/availability")
    public AvailabilityResponse availability(@RequestParam @Positive long locationId, @RequestParam @Positive long specialtyId,
                                             @RequestParam @Positive long professionalId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BookingService.Availability> items = bookings.availability(new BookingCommands.AvailabilityQuery(locationId, specialtyId, professionalId, date));
        return new AvailabilityResponse(items.stream().map(value -> new SlotResponse(value.startAt(), value.endAt())).toList());
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> create(Principal principal, @Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(response(bookings.create(new BookingCommands.CreateAppointment(principal.getName(),
                request.locationId(), request.specialtyId(), request.professionalId(), request.startAt()))));
    }

    @GetMapping("/appointments")
    public MyAppointmentsResponse myAppointments(Principal principal, @RequestParam(required = false) String status,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AppointmentStatus requestedStatus = null;
        if (status != null) {
            try { requestedStatus = AppointmentStatus.valueOf(status); }
            catch (IllegalArgumentException ex) { throw new BookingException(BookingException.Reason.INVALID_REQUEST, "Estado de cita inválido."); }
        }
        return new MyAppointmentsResponse(bookings.myAppointments(principal.getName(), requestedStatus, date).stream()
                .map(BookingController::myAppointmentResponse).toList());
    }

    @PostMapping("/appointments/{id}/cancellation")
    public MyAppointmentResponse cancel(Principal principal, @PathVariable @Positive long id) {
        return myAppointmentResponse(bookings.cancel(new BookingCommands.CancelAppointment(principal.getName(), id)));
    }

    @PostMapping("/appointments/{id}/reschedule-requests")
    public ResponseEntity<RescheduleRequestResponse> requestReschedule(Principal principal, @PathVariable @Positive long id,
                                                                      @Valid @RequestBody RescheduleRequestBody request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rescheduleResponse(bookings.requestReschedule(
                new BookingCommands.RequestReschedule(principal.getName(), id, request.locationId(), request.specialtyId(),
                        request.professionalId(), request.startAt()))));
    }

    @GetMapping("/admin/reschedule-requests")
    public RescheduleRequestsResponse rescheduleRequests(@RequestParam(defaultValue = "PENDING") String status,
                                                         @RequestParam(required = false) @Positive Long locationId,
                                                         @RequestParam(required = false) @Positive Long professionalId,
                                                         @RequestParam(required = false) @Positive Long specialtyId,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        RescheduleStatus requestedStatus;
        try { requestedStatus = RescheduleStatus.valueOf(status); }
        catch (IllegalArgumentException ex) { throw new BookingException(BookingException.Reason.INVALID_REQUEST, "Estado de reprogramación inválido."); }
        return new RescheduleRequestsResponse(bookings.rescheduleRequests(requestedStatus, locationId, professionalId, specialtyId, date).stream()
                .map(BookingController::rescheduleResponse).toList());
    }

    @PostMapping("/admin/reschedule-requests/{id}/decision")
    public RescheduleRequestResponse decideReschedule(Principal principal, @PathVariable @Positive long id,
                                                      @Valid @RequestBody DecisionRequest request) {
        return rescheduleResponse(bookings.decideReschedule(
                new BookingCommands.RescheduleDecision(principal.getName(), id, request.decision(), request.reason())));
    }

    @GetMapping("/professional/agenda")
    public AgendaResponse agenda(Principal principal, @RequestParam String range,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @RequestParam(required = false) Long locationId) {
        BookingCommands.AgendaRange parsedRange;
        try { parsedRange = BookingCommands.AgendaRange.valueOf(range.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw new BookingException(BookingException.Reason.INVALID_REQUEST, "El parámetro range debe ser day o week."); }
        return new AgendaResponse(bookings.agenda(new BookingCommands.AgendaQuery(principal.getName(), parsedRange, date, locationId))
                .stream().map(BookingController::agendaResponse).toList());
    }

    @PostMapping("/appointments/{id}/closure")
    public AgendaAppointmentResponse close(Principal principal, @PathVariable @Positive long id, @Valid @RequestBody ClosureRequest request) {
        return agendaResponse(bookings.close(new BookingCommands.CloseAppointment(principal.getName(), id, request.outcome())));
    }

    @GetMapping("/admin/appointments")
    public List<PendingAppointmentResponse> pending(@RequestParam(defaultValue = "REQUESTED") String status,
                                                     @RequestParam(required = false) @Positive Long locationId,
                                                     @RequestParam(required = false) @Positive Long professionalId,
                                                     @RequestParam(required = false) @Positive Long specialtyId,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (!"REQUESTED".equals(status)) throw new BookingException(BookingException.Reason.INVALID_REQUEST, "Solo se admiten solicitudes REQUESTED en S3.");
        return bookings.pending(locationId, professionalId, specialtyId, date).stream().map(value -> new PendingAppointmentResponse(id(value.id()), value.patientName(), value.professionalName(), value.locationName(),
                value.specialtyName(), value.durationMinutes(), value.startAt(), value.endAt(), value.status().name())).toList();
    }

    @GetMapping("/appointments/{id}/status-history")
    public StatusHistoryResponse statusHistory(Authentication authentication, @PathVariable @Positive long id) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return new StatusHistoryResponse(bookings.statusHistory(new BookingCommands.StatusHistoryQuery(authentication.getName(), admin, id)).stream()
                .map(BookingController::statusHistoryResponse).toList());
    }

    @PostMapping("/admin/appointments/{id}/decision")
    public AppointmentResponse decide(Principal principal, @PathVariable @Positive long id, @Valid @RequestBody DecisionRequest request) {
        return response(bookings.decide(new BookingCommands.Decision(principal.getName(), id, request.decision(), request.reason())));
    }

    private static AppointmentResponse response(BookingService.CreatedAppointment value) {
        return new AppointmentResponse(id(value.id()), value.status().name(), value.startAt(), value.endAt(), value.durationMinutes());
    }
    private static MyAppointmentResponse myAppointmentResponse(BookingService.MyAppointmentView value) {
        return new MyAppointmentResponse(id(value.id()), new RelatedResponse(id(value.locationId()), value.locationName()),
                new RelatedResponse(id(value.professionalId()), value.professionalName()), new RelatedResponse(id(value.specialtyId()), value.specialtyName()),
                value.startAt(), value.endAt(), value.durationMinutes(), value.status().name(), value.rejectionReason(), value.cancellationAllowed(),
                value.rescheduleAllowed(), value.rescheduleRequest() == null ? null : new RescheduleSummaryResponse(
                        id(value.rescheduleRequest().id()), value.rescheduleRequest().status().name(),
                        value.rescheduleRequest().requestedStartAt(), value.rescheduleRequest().requestedEndAt(),
                        value.rescheduleRequest().decisionReason(), value.rescheduleRequest().decidedAt()));
    }
    private static RescheduleRequestResponse rescheduleResponse(BookingPorts.RescheduleRequestDetail value) {
        return new RescheduleRequestResponse(id(value.id()), id(value.appointmentId()), value.status().name(), value.patientName(),
                value.professionalName(), value.specialtyName(), new RelatedResponse(id(value.requestedLocationId()), value.locationName()),
                value.durationMinutes(), value.previousStartAt(), value.previousEndAt(), value.requestedStartAt(), value.requestedEndAt(),
                value.decisionReason(), value.decidedAt());
    }
    private static AgendaAppointmentResponse agendaResponse(BookingService.AgendaAppointmentView value) {
        return new AgendaAppointmentResponse(id(value.id()), value.patientName(), new RelatedResponse(id(value.locationId()), value.locationName()),
                new RelatedResponse(id(value.specialtyId()), value.specialtyName()), value.startAt(), value.endAt(), value.durationMinutes(),
                value.status().name(), value.closureAllowed());
    }
    private static StatusHistoryItemResponse statusHistoryResponse(BookingPorts.StatusHistoryEvent value) {
        ActorResponse actor = value.actorUserId() == null ? null : new ActorResponse(value.actorUserId(), value.actorName());
        return new StatusHistoryItemResponse(id(value.id()), id(value.appointmentId()), value.status().name(), actor,
                value.source().name(), value.changedAt(), value.reason());
    }
    private static String id(long value) { return Long.toString(value); }

    public record CreateAppointmentRequest(@Positive long locationId, @Positive long specialtyId, @Positive long professionalId,
                                           @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt) { }
    public record DecisionRequest(@NotNull BookingCommands.DecisionType decision, String reason) { }
    public record LocationResponse(String id, String code, String name) { }
    public record SpecialtyResponse(String id, String code, String name, int durationMinutes, boolean general, boolean requiresAdminApproval) { }
    public record ProfessionalResponse(String id, String name, String code) { }
    public record SlotResponse(LocalDateTime startAt, LocalDateTime endAt) { }
    public record AvailabilityResponse(List<SlotResponse> items) { }
    public record AppointmentResponse(String id, String status, LocalDateTime startAt, LocalDateTime endAt, int durationMinutes) { }
    public record RelatedResponse(String id, String name) { }
    public record MyAppointmentsResponse(List<MyAppointmentResponse> items) { }
    public record MyAppointmentResponse(String id, RelatedResponse location, RelatedResponse professional, RelatedResponse specialty,
                                        LocalDateTime startAt, LocalDateTime endAt, int durationMinutes, String status,
                                        String rejectionReason, boolean cancellationAllowed, boolean rescheduleAllowed,
                                        RescheduleSummaryResponse rescheduleRequest) { }
    public record RescheduleSummaryResponse(String id, String status, LocalDateTime requestedStartAt, LocalDateTime requestedEndAt,
                                            String decisionReason, LocalDateTime decidedAt) { }
    public record RescheduleRequestBody(@Positive long locationId, @Positive long specialtyId, @Positive long professionalId,
                                        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt) { }
    public record RescheduleRequestsResponse(List<RescheduleRequestResponse> items) { }
    public record RescheduleRequestResponse(String id, String appointmentId, String status, String patientName, String professionalName,
                                            String specialtyName, RelatedResponse location, int durationMinutes,
                                            LocalDateTime previousStartAt, LocalDateTime previousEndAt,
                                            LocalDateTime requestedStartAt, LocalDateTime requestedEndAt,
                                            String decisionReason, LocalDateTime decidedAt) { }
    public record PendingAppointmentResponse(String id, String patientName, String professionalName, String locationName, String specialtyName,
                                             int durationMinutes, LocalDateTime startAt, LocalDateTime endAt, String status) { }
    public record ClosureRequest(@NotNull AppointmentStatus outcome) { }
    public record AgendaResponse(List<AgendaAppointmentResponse> items) { }
    public record AgendaAppointmentResponse(String id, String patientName, RelatedResponse location, RelatedResponse specialty,
                                            LocalDateTime startAt, LocalDateTime endAt, int durationMinutes, String status, boolean closureAllowed) { }
    public record StatusHistoryResponse(List<StatusHistoryItemResponse> items) { }
    public record StatusHistoryItemResponse(String id, String appointmentId, String status, ActorResponse actor,
                                            String source, LocalDateTime changedAt, String reason) { }
    public record ActorResponse(String id, String name) { }
}
