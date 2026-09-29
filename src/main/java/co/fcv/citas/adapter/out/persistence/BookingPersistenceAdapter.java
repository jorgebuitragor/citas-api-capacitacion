package co.fcv.citas.adapter.out.persistence;

import co.fcv.citas.application.booking.BookingPorts;
import co.fcv.citas.application.booking.BookingPorts.*;
import co.fcv.citas.domain.appointment.AppointmentStatus;
import co.fcv.citas.domain.appointment.RescheduleStatus;
import co.fcv.citas.domain.appointment.StatusSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                SELECT ps.id, ps.start_at, ps.end_at, ps.appointment_id, ps.reschedule_request_id
                FROM professional_slots ps JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                WHERE ab.professional_id = ? AND ab.location_id = ? AND ab.available_date = ? AND ab.active = TRUE
                ORDER BY ps.start_at ASC, ps.id ASC
                """ + (lock ? " FOR UPDATE" : "");
        return jdbc.query(sql, (rs, row) -> new Slot(rs.getLong(1), rs.getObject(2, LocalDateTime.class), rs.getObject(3, LocalDateTime.class),
                rs.getObject(4) == null ? null : rs.getLong(4), rs.getObject(5) == null ? null : rs.getLong(5)), professionalId, locationId, date);
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
        for (Long slotId : slotIds) jdbc.update(
                "UPDATE professional_slots SET appointment_id = ? WHERE id = ? AND appointment_id IS NULL AND reschedule_request_id IS NULL",
                appointmentId, slotId);
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

    @Override public List<MyAppointment> findMyAppointments(String userId, AppointmentStatus status, LocalDate date) {
        StringBuilder sql = new StringBuilder(myAppointmentsSelect()).append(" WHERE a.patient_user_id = ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(Long.parseLong(userId));
        if (status != null) { sql.append(" AND status.code = ?"); parameters.add(status.name()); }
        if (date != null) { sql.append(" AND DATE(a.scheduled_start_at) = ?"); parameters.add(date); }
        sql.append(" ORDER BY a.scheduled_start_at ASC, a.id ASC");
        return jdbc.query(sql.toString(), (rs, row) -> myAppointment(rs), parameters.toArray());
    }

    @Override public Optional<MyAppointment> lockAppointmentForUser(long appointmentId, String userId) {
        List<MyAppointment> results = jdbc.query(myAppointmentsSelect() + " WHERE a.id = ? AND a.patient_user_id = ? FOR UPDATE",
                (rs, row) -> myAppointment(rs), appointmentId, Long.parseLong(userId));
        return results.stream().findFirst();
    }

    @Override public void cancel(long appointmentId) {
        jdbc.update("UPDATE appointments SET status_id = (SELECT id FROM appointment_statuses WHERE code = 'CANCELLED') WHERE id = ?", appointmentId);
    }

    @Override public Optional<Long> findProfessionalId(String userId) {
        try {
            List<Long> results = jdbc.query("SELECT id FROM professionals WHERE user_id = ? AND active = TRUE",
                    (rs, row) -> rs.getLong(1), Long.parseLong(userId));
            return results.stream().findFirst();
        } catch (NumberFormatException ex) { return Optional.empty(); }
    }

    @Override public List<AgendaAppointment> findAgenda(long professionalId, LocalDateTime rangeStart, LocalDateTime rangeEndExclusive, Long locationId) {
        StringBuilder sql = new StringBuilder(agendaSelect())
                .append(" WHERE a.professional_id = ? AND status.code = 'APPROVED' AND a.scheduled_start_at >= ? AND a.scheduled_start_at < ?");
        List<Object> parameters = new ArrayList<>(List.of(professionalId, rangeStart, rangeEndExclusive));
        if (locationId != null) { sql.append(" AND a.location_id = ?"); parameters.add(locationId); }
        sql.append(" ORDER BY a.scheduled_start_at ASC, a.id ASC");
        return jdbc.query(sql.toString(), (rs, row) -> agendaAppointment(rs), parameters.toArray());
    }

    @Override public Optional<AgendaAppointment> lockAppointmentForProfessional(long appointmentId, long professionalId) {
        List<AgendaAppointment> results = jdbc.query(agendaSelect() + " WHERE a.id = ? AND a.professional_id = ? FOR UPDATE",
                (rs, row) -> agendaAppointment(rs), appointmentId, professionalId);
        return results.stream().findFirst();
    }

    @Override public void close(long appointmentId, AppointmentStatus outcome) {
        jdbc.update("UPDATE appointments SET status_id = (SELECT id FROM appointment_statuses WHERE code = ?) WHERE id = ?", outcome.name(), appointmentId);
    }

    @Override public List<RescheduleRequest> findLatestRescheduleRequests(List<Long> appointmentIds) {
        if (appointmentIds.isEmpty()) return List.of();
        String placeholders = String.join(", ", appointmentIds.stream().map(id -> "?").toList());
        return jdbc.query("""
                SELECT r.id, r.appointment_id, status.code, r.requested_start_at, r.requested_end_at, r.decision_reason, r.decided_at
                FROM reschedule_requests r JOIN reschedule_request_statuses status ON status.id = r.status_id
                WHERE r.appointment_id IN (%s)
                  AND r.id = (SELECT MAX(latest.id) FROM reschedule_requests latest WHERE latest.appointment_id = r.appointment_id)
                """.formatted(placeholders),
                (rs, row) -> new RescheduleRequest(rs.getLong(1), rs.getLong(2), RescheduleStatus.valueOf(rs.getString(3)),
                        rs.getObject(4, LocalDateTime.class), rs.getObject(5, LocalDateTime.class), rs.getString(6),
                        rs.getObject(7, LocalDateTime.class)),
                appointmentIds.toArray());
    }

    @Override public boolean hasPendingRescheduleRequest(long appointmentId) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM reschedule_requests r JOIN reschedule_request_statuses status ON status.id = r.status_id
                WHERE r.appointment_id = ? AND status.code = 'PENDING'
                """, Long.class, appointmentId);
        return count != null && count > 0;
    }

    @Override public long createRescheduleRequest(NewRescheduleRequest request) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO reschedule_requests (appointment_id, requested_by_user_id, requested_location_id, status_id,
                                                     previous_start_at, previous_end_at, requested_start_at, requested_end_at)
                    VALUES (?, ?, ?, (SELECT id FROM reschedule_request_statuses WHERE code = 'PENDING'), ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, request.appointmentId());
            statement.setLong(2, Long.parseLong(request.requestedByUserId()));
            statement.setLong(3, request.requestedLocationId());
            statement.setObject(4, request.previousStartAt()); statement.setObject(5, request.previousEndAt());
            statement.setObject(6, request.requestedStartAt()); statement.setObject(7, request.requestedEndAt());
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }

    @Override public void holdSlots(long rescheduleRequestId, List<Long> slotIds) {
        for (Long slotId : slotIds) jdbc.update(
                "UPDATE professional_slots SET reschedule_request_id = ? WHERE id = ? AND appointment_id IS NULL AND reschedule_request_id IS NULL",
                rescheduleRequestId, slotId);
    }

    @Override public void releaseHeldSlots(long rescheduleRequestId) {
        jdbc.update("UPDATE professional_slots SET reschedule_request_id = NULL WHERE reschedule_request_id = ?", rescheduleRequestId);
    }

    @Override public void confirmHeldSlots(long rescheduleRequestId, long appointmentId) {
        jdbc.update("UPDATE professional_slots SET appointment_id = ?, reschedule_request_id = NULL WHERE reschedule_request_id = ?",
                appointmentId, rescheduleRequestId);
    }

    @Override public void moveAppointmentWindow(long appointmentId, LocalDateTime startAt, LocalDateTime endAt) {
        jdbc.update("UPDATE appointments SET scheduled_start_at = ?, scheduled_end_at = ? WHERE id = ?", startAt, endAt, appointmentId);
    }

    @Override public void releaseSlotsInRange(long appointmentId, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        jdbc.update("UPDATE professional_slots SET appointment_id = NULL WHERE appointment_id = ? AND start_at >= ? AND start_at < ?",
                appointmentId, fromInclusive, toExclusive);
    }

    @Override public List<RescheduleRequestDetail> findRescheduleRequests(RescheduleStatus status) {
        return jdbc.query(rescheduleDetailSelect() + " WHERE status.code = ? ORDER BY r.requested_start_at ASC, r.id ASC",
                (rs, row) -> rescheduleDetail(rs), status.name());
    }

    @Override public Optional<RescheduleRequestDetail> lockRescheduleRequest(long rescheduleRequestId) {
        // El bloqueo alcanza también la fila de la cita por el JOIN, de modo que una cancelación
        // concurrente de la misma cita queda serializada con la decisión ADMIN.
        return jdbc.query(rescheduleDetailSelect() + " WHERE r.id = ? FOR UPDATE",
                (rs, row) -> rescheduleDetail(rs), rescheduleRequestId).stream().findFirst();
    }

    @Override public void decideRescheduleRequest(long rescheduleRequestId, RescheduleStatus status, String adminUserId,
                                                 LocalDateTime decidedAt, String reason) {
        jdbc.update("""
                UPDATE reschedule_requests
                SET status_id = (SELECT id FROM reschedule_request_statuses WHERE code = ?),
                    decided_by_user_id = ?, decided_at = ?, decision_reason = ?
                WHERE id = ?
                """, status.name(), Long.parseLong(adminUserId), decidedAt, reason, rescheduleRequestId);
    }

    @Override public Optional<RescheduleRequestDetail> findRescheduleRequestDetail(long rescheduleRequestId) {
        return jdbc.query(rescheduleDetailSelect() + " WHERE r.id = ?", (rs, row) -> rescheduleDetail(rs), rescheduleRequestId)
                .stream().findFirst();
    }

    private static String rescheduleDetailSelect() {
        return """
                SELECT r.id, r.appointment_id, status.code, CONCAT(patient.first_name, ' ', patient.last_name),
                       CONCAT(professional_user.first_name, ' ', professional_user.last_name), s.name, l.name, l.id,
                       s.appointment_duration_minutes, r.previous_start_at, r.previous_end_at,
                       r.requested_start_at, r.requested_end_at, r.decision_reason, r.decided_at
                FROM reschedule_requests r
                JOIN reschedule_request_statuses status ON status.id = r.status_id
                JOIN appointments a ON a.id = r.appointment_id
                JOIN users patient ON patient.id = a.patient_user_id
                JOIN professionals p ON p.id = a.professional_id
                JOIN users professional_user ON professional_user.id = p.user_id
                JOIN specialties s ON s.id = a.specialty_id
                JOIN locations l ON l.id = r.requested_location_id
                """;
    }

    private static RescheduleRequestDetail rescheduleDetail(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new RescheduleRequestDetail(rs.getLong(1), rs.getLong(2), RescheduleStatus.valueOf(rs.getString(3)), rs.getString(4),
                rs.getString(5), rs.getString(6), rs.getString(7), rs.getLong(8), rs.getInt(9),
                rs.getObject(10, LocalDateTime.class), rs.getObject(11, LocalDateTime.class),
                rs.getObject(12, LocalDateTime.class), rs.getObject(13, LocalDateTime.class),
                rs.getString(14), rs.getObject(15, LocalDateTime.class));
    }

    private static String agendaSelect() {
        return """
                SELECT a.id, CONCAT(patient.first_name, ' ', patient.last_name), l.id, l.name, s.id, s.name,
                       a.scheduled_start_at, a.scheduled_end_at, s.appointment_duration_minutes, status.code
                FROM appointments a JOIN appointment_statuses status ON status.id = a.status_id
                JOIN users patient ON patient.id = a.patient_user_id
                JOIN locations l ON l.id = a.location_id
                JOIN specialties s ON s.id = a.specialty_id
                """;
    }

    private static AgendaAppointment agendaAppointment(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new AgendaAppointment(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getString(4), rs.getLong(5), rs.getString(6),
                rs.getObject(7, LocalDateTime.class), rs.getObject(8, LocalDateTime.class), rs.getInt(9), AppointmentStatus.valueOf(rs.getString(10)));
    }

    private static String myAppointmentsSelect() {
        return """
                SELECT a.id, l.id, l.name, p.id, CONCAT(professional_user.first_name, ' ', professional_user.last_name),
                       s.id, s.name, a.scheduled_start_at, a.scheduled_end_at, s.appointment_duration_minutes,
                       status.code, status.is_terminal,
                       (SELECT h.reason FROM appointment_status_history h
                        JOIN appointment_statuses rejected_status ON rejected_status.id = h.status_id
                        WHERE h.appointment_id = a.id AND rejected_status.code = 'REJECTED'
                        ORDER BY h.changed_at DESC, h.id DESC LIMIT 1)
                FROM appointments a JOIN appointment_statuses status ON status.id = a.status_id
                JOIN locations l ON l.id = a.location_id
                JOIN professionals p ON p.id = a.professional_id
                JOIN users professional_user ON professional_user.id = p.user_id
                JOIN specialties s ON s.id = a.specialty_id
                """;
    }

    private static MyAppointment myAppointment(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new MyAppointment(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getLong(4), rs.getString(5),
                rs.getLong(6), rs.getString(7), rs.getObject(8, LocalDateTime.class), rs.getObject(9, LocalDateTime.class),
                rs.getInt(10), AppointmentStatus.valueOf(rs.getString(11)), rs.getBoolean(12), rs.getString(13));
    }
}
