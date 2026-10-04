package co.fcv.citas.application.automation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class AutomationPorts {
    private AutomationPorts() { }

    public interface AutomationRepository {
        List<UpcomingAppointment> findUpcoming(LocalDateTime fromExclusive, LocalDateTime toInclusive);
        Optional<LocalDateTime> findApprovedStart(long appointmentId);
        /** Vacío cuando ya existe un SENT para esa cita y franja (índice único). */
        Optional<Reminder> insertReminder(long appointmentId, LocalDateTime windowStartAt, ReminderStatus status, String detail);
    }

    public enum ReminderStatus { SENT, FAILED }
    public record UpcomingAppointment(long appointmentId, LocalDateTime startAt, LocalDateTime endAt, String patientName,
                                      String patientEmail, String professionalName, String specialtyName, String locationName) { }
    public record Reminder(long id, long appointmentId, ReminderStatus status, LocalDateTime recordedAt) { }
}
