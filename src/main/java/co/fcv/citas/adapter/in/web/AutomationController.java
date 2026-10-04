package co.fcv.citas.adapter.in.web;

import co.fcv.citas.application.automation.AutomationPorts;
import co.fcv.citas.application.automation.AutomationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
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
@RequestMapping("/api/v1/automation")
public class AutomationController {
    private final AutomationService automation;

    public AutomationController(AutomationService automation) { this.automation = automation; }

    @GetMapping("/upcoming-appointments")
    public UpcomingResponse upcoming(@RequestParam(defaultValue = "24") int hours) {
        return new UpcomingResponse(hours, automation.upcoming(hours).stream().map(AutomationController::item).toList());
    }

    @PostMapping("/appointments/{appointmentId}/reminders")
    public ResponseEntity<ReminderResponse> record(@PathVariable @Positive long appointmentId, @Valid @RequestBody ReminderRequest request) {
        AutomationPorts.Reminder saved = automation.recordReminder(appointmentId, request.status(), request.detail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ReminderResponse(Long.toString(saved.id()), Long.toString(saved.appointmentId()),
                saved.status().name(), saved.recordedAt()));
    }

    private static UpcomingItem item(AutomationPorts.UpcomingAppointment value) {
        return new UpcomingItem(Long.toString(value.appointmentId()), value.startAt(), value.endAt(),
                new PatientResponse(value.patientName(), value.patientEmail()), value.professionalName(), value.specialtyName(), value.locationName());
    }

    public record UpcomingResponse(int windowHours, List<UpcomingItem> items) { }
    public record UpcomingItem(String appointmentId, LocalDateTime startAt, LocalDateTime endAt, PatientResponse patient,
                               String professionalName, String specialtyName, String locationName) { }
    public record PatientResponse(String name, String email) { }
    public record ReminderRequest(@NotNull AutomationPorts.ReminderStatus status, @Size(max = 500) String detail) { }
    public record ReminderResponse(String id, String appointmentId, String status, LocalDateTime recordedAt) { }
}
