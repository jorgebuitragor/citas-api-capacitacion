package co.fcv.citas.adapter.in.web;

import co.fcv.citas.application.booking.BookingCommands;
import co.fcv.citas.application.booking.BookingException;
import co.fcv.citas.application.booking.BookingPorts;
import co.fcv.citas.application.booking.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/admin/appointments")
    public List<PendingAppointmentResponse> pending(@RequestParam(defaultValue = "REQUESTED") String status) {
        if (!"REQUESTED".equals(status)) throw new BookingException(BookingException.Reason.INVALID_REQUEST, "Solo se admiten solicitudes REQUESTED en S3.");
        return bookings.pending().stream().map(value -> new PendingAppointmentResponse(id(value.id()), value.patientName(), value.professionalName(), value.locationName(),
                value.specialtyName(), value.durationMinutes(), value.startAt(), value.endAt(), value.status().name())).toList();
    }

    @PostMapping("/admin/appointments/{id}/decision")
    public AppointmentResponse decide(Principal principal, @PathVariable @Positive long id, @Valid @RequestBody DecisionRequest request) {
        return response(bookings.decide(new BookingCommands.Decision(principal.getName(), id, request.decision(), request.reason())));
    }

    private static AppointmentResponse response(BookingService.CreatedAppointment value) {
        return new AppointmentResponse(id(value.id()), value.status().name(), value.startAt(), value.endAt(), value.durationMinutes());
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
    public record PendingAppointmentResponse(String id, String patientName, String professionalName, String locationName, String specialtyName,
                                             int durationMinutes, LocalDateTime startAt, LocalDateTime endAt, String status) { }
}
