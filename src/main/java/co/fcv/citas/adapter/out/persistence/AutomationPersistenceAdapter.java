package co.fcv.citas.adapter.out.persistence;

import co.fcv.citas.application.automation.AutomationPorts;
import co.fcv.citas.application.automation.AutomationPorts.Reminder;
import co.fcv.citas.application.automation.AutomationPorts.ReminderStatus;
import co.fcv.citas.application.automation.AutomationPorts.UpcomingAppointment;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

@Component
public class AutomationPersistenceAdapter implements AutomationPorts.AutomationRepository {
    private final JdbcTemplate jdbc;

    public AutomationPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<UpcomingAppointment> findUpcoming(LocalDateTime fromExclusive, LocalDateTime toInclusive) {
        return jdbc.query("""
                SELECT a.id, a.scheduled_start_at, a.scheduled_end_at, CONCAT(patient.first_name, ' ', patient.last_name), patient.email,
                       CONCAT(professional_user.first_name, ' ', professional_user.last_name), s.name, l.name
                FROM appointments a JOIN appointment_statuses status ON status.id = a.status_id
                JOIN users patient ON patient.id = a.patient_user_id
                JOIN professionals p ON p.id = a.professional_id JOIN users professional_user ON professional_user.id = p.user_id
                JOIN specialties s ON s.id = a.specialty_id JOIN locations l ON l.id = a.location_id
                WHERE status.code = 'APPROVED' AND patient.active = TRUE
                  AND a.scheduled_start_at > ? AND a.scheduled_start_at <= ?
                  AND NOT EXISTS (SELECT 1 FROM appointment_reminders r WHERE r.appointment_id = a.id
                                  AND r.window_start_at = a.scheduled_start_at AND r.status = 'SENT')
                ORDER BY a.scheduled_start_at, a.id
                """, (rs, row) -> new UpcomingAppointment(rs.getLong(1), rs.getObject(2, LocalDateTime.class), rs.getObject(3, LocalDateTime.class),
                rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8)), fromExclusive, toInclusive);
    }

    @Override public Optional<LocalDateTime> findApprovedStart(long appointmentId) {
        return jdbc.query("""
                SELECT a.scheduled_start_at FROM appointments a JOIN appointment_statuses status ON status.id = a.status_id
                WHERE a.id = ? AND status.code = 'APPROVED'
                """, (rs, row) -> rs.getObject(1, LocalDateTime.class), appointmentId).stream().findFirst();
    }

    @Override public Optional<Reminder> insertReminder(long appointmentId, LocalDateTime windowStartAt, ReminderStatus status, String detail) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO appointment_reminders (appointment_id, window_start_at, status, dedup_key, detail)
                        VALUES (?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setLong(1, appointmentId); statement.setObject(2, windowStartAt); statement.setString(3, status.name());
                if (status == ReminderStatus.SENT) statement.setInt(4, 1); else statement.setNull(4, java.sql.Types.TINYINT);
                statement.setString(5, detail);
                return statement;
            }, keys);
        } catch (DuplicateKeyException ex) {
            return Optional.empty();
        }
        long id = keys.getKey().longValue();
        return jdbc.query("SELECT id, appointment_id, status, recorded_at FROM appointment_reminders WHERE id = ?",
                (rs, row) -> new Reminder(rs.getLong(1), rs.getLong(2), ReminderStatus.valueOf(rs.getString(3)), rs.getObject(4, LocalDateTime.class)), id)
                .stream().findFirst();
    }
}
