package co.fcv.citas.adapter.out.persistence;

import co.fcv.citas.application.booking.BookingPorts;
import co.fcv.citas.application.booking.BookingPorts.*;
import co.fcv.citas.domain.appointment.AppointmentStatus;
import co.fcv.citas.domain.appointment.StatusSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

@Component
public class BookingPersistenceAdapter implements BookingPorts.BookingRepository {
    private final JdbcTemplate jdbc;

    public BookingPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<Location> findActiveLocations() {
        return jdbc.query("SELECT id, code, name FROM locations WHERE active = TRUE ORDER BY name", (rs, row) -> new Location(rs.getLong(1), rs.getString(2), rs.getString(3)));
    }

    @Override public List<Specialty> findActiveSpecialties() {
        return jdbc.query("SELECT id, code, name, appointment_duration_minutes, is_general, requires_admin_approval FROM specialties WHERE active = TRUE ORDER BY name",
                (rs, row) -> new Specialty(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getBoolean(5), rs.getBoolean(6)));
    }

    @Override public List<Professional> findProfessionals(long locationId, long specialtyId) {
        return jdbc.query("""
                SELECT p.id, CONCAT(u.first_name, ' ', u.last_name), p.professional_code
                FROM professionals p JOIN users u ON u.id = p.user_id
                JOIN professional_specialties ps ON ps.professional_id = p.id
                JOIN professional_locations pl ON pl.professional_id = p.id
                WHERE p.active = TRUE AND ps.active = TRUE AND pl.active = TRUE
                  AND ps.specialty_id = ? AND pl.location_id = ? ORDER BY u.first_name, u.last_name
                """, (rs, row) -> new Professional(rs.getLong(1), rs.getString(2), rs.getString(3)), specialtyId, locationId);
    }

    @Override public Optional<ReservationContext> findReservationContext(long locationId, long specialtyId, long professionalId) {
        List<ReservationContext> results = jdbc.query("""
                SELECT s.id, s.code, s.name, s.appointment_duration_minutes, s.is_general, s.requires_admin_approval
                FROM specialties s JOIN professional_specialties ps ON ps.specialty_id = s.id
                JOIN professional_locations pl ON pl.professional_id = ps.professional_id
                JOIN professionals p ON p.id = ps.professional_id
                JOIN locations l ON l.id = pl.location_id
                WHERE s.id = ? AND ps.professional_id = ? AND pl.location_id = ?
                  AND s.active = TRUE AND ps.active = TRUE AND pl.active = TRUE AND p.active = TRUE AND l.active = TRUE
                """, (rs, row) -> new ReservationContext(new Specialty(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getBoolean(5), rs.getBoolean(6))),
                specialtyId, professionalId, locationId);
        return results.stream().findFirst();
    }

    @Override public boolean isActiveUser(String userId) {
        try { return Boolean.TRUE.equals(jdbc.queryForObject("SELECT active FROM users WHERE id = ?", Boolean.class, Long.parseLong(userId))); }
        catch (NumberFormatException ex) { return false; }
    }

    @Override public List<Slot> findSlots(long professionalId, long locationId, LocalDate date, boolean lock) {
        String sql = """
                SELECT ps.id, ps.start_at, ps.end_at, ps.appointment_id
                FROM professional_slots ps JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                WHERE ab.professional_id = ? AND ab.location_id = ? AND ab.available_date = ? AND ab.active = TRUE
                ORDER BY ps.start_at ASC, ps.id ASC
                """ + (lock ? " FOR UPDATE" : "");
        return jdbc.query(sql, (rs, row) -> new Slot(rs.getLong(1), rs.getObject(2, LocalDateTime.class), rs.getObject(3, LocalDateTime.class),
                rs.getObject(4) == null ? null : rs.getLong(4)), professionalId, locationId, date);
    }

    @Override public long createAppointment(NewAppointment appointment) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO appointments (patient_user_id, professional_id, location_id, specialty_id, status_id,
                                              scheduled_start_at, scheduled_end_at, created_by_user_id)
                    VALUES (?, ?, ?, ?, (SELECT id FROM appointment_statuses WHERE code = ?), ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, Long.parseLong(appointment.patientUserId()));
            statement.setLong(2, appointment.professionalId()); statement.setLong(3, appointment.locationId());
            statement.setLong(4, appointment.specialtyId()); statement.setString(5, appointment.status().name());
            statement.setObject(6, appointment.startAt()); statement.setObject(7, appointment.endAt());
            statement.setLong(8, Long.parseLong(appointment.patientUserId()));
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }

    @Override public void assignSlots(long appointmentId, List<Long> slotIds) {
        for (Long slotId : slotIds) jdbc.update("UPDATE professional_slots SET appointment_id = ? WHERE id = ? AND appointment_id IS NULL", appointmentId, slotId);
    }

    @Override public void addHistory(long appointmentId, AppointmentStatus status, String actorUserId, StatusSource source, String reason) {
        jdbc.update("""
                INSERT INTO appointment_status_history (appointment_id, status_id, changed_by_user_id, change_source, reason)
                VALUES (?, (SELECT id FROM appointment_statuses WHERE code = ?), ?, ?, ?)
                """, appointmentId, status.name(), actorUserId == null ? null : Long.parseLong(actorUserId), source.name(), reason);
    }

    @Override public Optional<PendingAppointment> lockRequestedAppointment(long appointmentId) {
        List<PendingAppointment> results = jdbc.query("""
                SELECT a.id, CONCAT(patient.first_name, ' ', patient.last_name), CONCAT(professional_user.first_name, ' ', professional_user.last_name),
                       l.name, s.name, s.appointment_duration_minutes, a.scheduled_start_at, a.scheduled_end_at, status.code
                FROM appointments a JOIN appointment_statuses status ON status.id = a.status_id
                JOIN users patient ON patient.id = a.patient_user_id
                JOIN professionals p ON p.id = a.professional_id JOIN users professional_user ON professional_user.id = p.user_id
                JOIN locations l ON l.id = a.location_id JOIN specialties s ON s.id = a.specialty_id
                WHERE a.id = ? AND status.code = 'REQUESTED' FOR UPDATE
                """, (rs, row) -> new PendingAppointment(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                rs.getInt(6), rs.getObject(7, LocalDateTime.class), rs.getObject(8, LocalDateTime.class), AppointmentStatus.valueOf(rs.getString(9))), appointmentId);
        return results.stream().findFirst();
    }

    @Override public void approve(long appointmentId, String adminUserId, LocalDateTime approvedAt) {
        jdbc.update("UPDATE appointments SET status_id = (SELECT id FROM appointment_statuses WHERE code = 'APPROVED'), approved_by_user_id = ?, approved_at = ? WHERE id = ?",
                Long.parseLong(adminUserId), approvedAt, appointmentId);
    }

    @Override public void reject(long appointmentId) {
        jdbc.update("UPDATE appointments SET status_id = (SELECT id FROM appointment_statuses WHERE code = 'REJECTED') WHERE id = ?", appointmentId);
    }

    @Override public void releaseSlots(long appointmentId) { jdbc.update("UPDATE professional_slots SET appointment_id = NULL WHERE appointment_id = ?", appointmentId); }

    @Override public List<PendingAppointment> findPending() {
        return jdbc.query("""
                SELECT a.id, CONCAT(patient.first_name, ' ', patient.last_name), CONCAT(professional_user.first_name, ' ', professional_user.last_name),
                       l.name, s.name, s.appointment_duration_minutes, a.scheduled_start_at, a.scheduled_end_at, status.code
                FROM appointments a JOIN appointment_statuses status ON status.id = a.status_id
                JOIN users patient ON patient.id = a.patient_user_id
                JOIN professionals p ON p.id = a.professional_id JOIN users professional_user ON professional_user.id = p.user_id
                JOIN locations l ON l.id = a.location_id JOIN specialties s ON s.id = a.specialty_id
                WHERE status.code = 'REQUESTED' ORDER BY a.scheduled_start_at, a.id
                """, (rs, row) -> new PendingAppointment(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                rs.getInt(6), rs.getObject(7, LocalDateTime.class), rs.getObject(8, LocalDateTime.class), AppointmentStatus.valueOf(rs.getString(9))));
    }
}
