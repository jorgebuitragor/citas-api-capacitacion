package co.fcv.citas.application.automation;

import co.fcv.citas.application.automation.AutomationPorts.AutomationRepository;
import co.fcv.citas.application.automation.AutomationPorts.Reminder;
import co.fcv.citas.application.automation.AutomationPorts.ReminderStatus;
import co.fcv.citas.application.automation.AutomationPorts.UpcomingAppointment;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/** HU-028 (DEC-010). Solo lectura de citas y registro de resultados; no altera el núcleo de reservas. */
public class AutomationService {
    public static final int MIN_HOURS = 1;
    public static final int MAX_HOURS = 72;
    private final AutomationRepository repository;
    private final Clock clock;

    public AutomationService(AutomationRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }

    public List<UpcomingAppointment> upcoming(int hours) {
        if (hours < MIN_HOURS || hours > MAX_HOURS) {
            throw new AutomationException(AutomationException.Reason.INVALID_REQUEST, "hours debe estar entre " + MIN_HOURS + " y " + MAX_HOURS + ".");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return repository.findUpcoming(now, now.plusHours(hours));
    }

    @Transactional
    public Reminder recordReminder(long appointmentId, ReminderStatus status, String detail) {
        LocalDateTime window = repository.findApprovedStart(appointmentId)
                .orElseThrow(() -> new AutomationException(AutomationException.Reason.NOT_FOUND, "La cita no existe o ya no está aprobada."));
        String trimmed = detail == null || detail.isBlank() ? null : detail.trim();
        return repository.insertReminder(appointmentId, window, status, trimmed)
                .orElseThrow(() -> new AutomationException(AutomationException.Reason.DUPLICATE, "Ya existe un recordatorio enviado para esta cita y franja."));
    }
}
