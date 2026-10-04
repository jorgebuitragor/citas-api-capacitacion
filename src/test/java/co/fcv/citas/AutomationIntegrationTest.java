package co.fcv.citas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HU-028 (DEC-010): fuente REST de WF-001. Las franjas se fijan por SQL para controlar la ventana. */
@SpringBootTest
@AutoConfigureMockMvc
class AutomationIntegrationTest {
    private static final String KEY = "test-automation-key-for-integration-tests";
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    private final List<Long> appointments = new ArrayList<>();
    private final List<Long> users = new ArrayList<>();

    @AfterEach void cleanup() {
        for (Long id : appointments) jdbc.update("DELETE FROM appointments WHERE id = ?", id);
        for (Long id : users) jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    @Test void requiresServiceKeyAndGrantsNothingElse() throws Exception {
        mvc.perform(get("/api/v1/automation/upcoming-appointments")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/automation/upcoming-appointments").header("X-Api-Key", "otra-clave")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/automation/upcoming-appointments").header("X-Api-Key", "")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/automation/upcoming-appointments").header("Authorization", "Bearer " + jwt("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/automation/upcoming-appointments").header("X-Api-Key", KEY)).andExpect(status().isOk());
        // la clave de servicio no sirve fuera de /api/v1/automation/**
        mvc.perform(get("/api/v1/admin/appointments").header("X-Api-Key", KEY)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/appointments").header("X-Api-Key", KEY)).andExpect(status().isUnauthorized());
    }

    @Test void validatesWindowHours() throws Exception {
        for (String hours : List.of("0", "73", "-5")) {
            mvc.perform(get("/api/v1/automation/upcoming-appointments").param("hours", hours).header("X-Api-Key", KEY)).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/v1/automation/upcoming-appointments").param("hours", "72").header("X-Api-Key", KEY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.windowHours").value(72));
    }

    @Test void selectsOnlyApprovedAppointmentsInsideTheWindow() throws Exception {
        long user = user();
        LocalDateTime now = LocalDateTime.now(BOGOTA);
        long inside = appointment(user, "APPROVED", now.plusHours(3));
        appointment(user, "REQUESTED", now.plusHours(3));
        appointment(user, "CANCELLED", now.plusHours(3));
        appointment(user, "REJECTED", now.plusHours(3));
        appointment(user, "COMPLETED", now.plusHours(3));
        long outside = appointment(user, "APPROVED", now.plusHours(30));
        long past = appointment(user, "APPROVED", now.minusHours(2));

        List<String> ids = upcomingIds(24);
        org.assertj.core.api.Assertions.assertThat(ids).contains(Long.toString(inside)).doesNotContain(Long.toString(outside), Long.toString(past));
        org.assertj.core.api.Assertions.assertThat(ids.stream().filter(id -> appointments.contains(Long.parseLong(id))).count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(upcomingIds(36)).contains(Long.toString(inside), Long.toString(outside));
        mvc.perform(get("/api/v1/automation/upcoming-appointments").header("X-Api-Key", KEY))
                .andExpect(jsonPath("$.items[?(@.appointmentId == '" + inside + "')].patient.email").value(email(user)))
                .andExpect(jsonPath("$.items[?(@.appointmentId == '" + inside + "')].specialtyName").isNotEmpty());
    }

    @Test void skipsInactivePatients() throws Exception {
        long user = user();
        long id = appointment(user, "APPROVED", LocalDateTime.now(BOGOTA).plusHours(2));
        jdbc.update("UPDATE users SET active = FALSE WHERE id = ?", user);
        org.assertj.core.api.Assertions.assertThat(upcomingIds(24)).doesNotContain(Long.toString(id));
    }

    @Test void deduplicatesSentRemindersPerAppointmentAndSlotButAllowsRetryAfterFailure() throws Exception {
        long user = user();
        LocalDateTime start = LocalDateTime.now(BOGOTA).plusHours(4);
        long id = appointment(user, "APPROVED", start);

        record(id, "FAILED", "SMTP no disponible").andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("FAILED"));
        record(id, "FAILED", "SMTP no disponible").andExpect(status().isCreated());
        org.assertj.core.api.Assertions.assertThat(upcomingIds(24)).contains(Long.toString(id));

        record(id, "SENT", "Mailpit 250 OK").andExpect(status().isCreated());
        org.assertj.core.api.Assertions.assertThat(upcomingIds(24)).doesNotContain(Long.toString(id));
        record(id, "SENT", "duplicado").andExpect(status().isConflict());

        jdbc.update("UPDATE appointments SET scheduled_start_at = ?, scheduled_end_at = ? WHERE id = ?", start.plusHours(1), start.plusHours(1).plusMinutes(30), id);
        org.assertj.core.api.Assertions.assertThat(upcomingIds(24)).contains(Long.toString(id));
        record(id, "SENT", "nueva franja").andExpect(status().isCreated());
    }

    @Test void rejectsRemindersForNonApprovedUnknownOrMalformedRequests() throws Exception {
        long user = user();
        long requested = appointment(user, "REQUESTED", LocalDateTime.now(BOGOTA).plusHours(2));
        record(requested, "SENT", null).andExpect(status().isNotFound());
        record(999999999L, "SENT", null).andExpect(status().isNotFound());
        long approved = appointment(user, "APPROVED", LocalDateTime.now(BOGOTA).plusHours(2));
        mvc.perform(post("/api/v1/automation/appointments/{id}/reminders", approved).header("X-Api-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DONE\"}")).andExpect(status().isBadRequest());
        record(approved, "SENT", "x".repeat(501)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/automation/appointments/{id}/reminders", approved).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SENT\"}")).andExpect(status().isUnauthorized());
    }

    private ResultActions record(long id, String status, String detail) throws Exception {
        Map<String, Object> body = detail == null ? Map.of("status", status) : Map.of("status", status, "detail", detail);
        return mvc.perform(post("/api/v1/automation/appointments/{id}/reminders", id).header("X-Api-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)));
    }

    private List<String> upcomingIds(int hours) throws Exception {
        String body = mvc.perform(get("/api/v1/automation/upcoming-appointments").param("hours", Integer.toString(hours)).header("X-Api-Key", KEY))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> ids = new ArrayList<>();
        mapper.readTree(body).get("items").forEach(item -> ids.add(item.get("appointmentId").asText()));
        return ids;
    }

    private long user() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO users (first_name, last_name, document_type, document_number, email, phone, password_hash, active) VALUES (?, ?, 'CC', ?, ?, '3000000000', ?, TRUE)",
                "Paciente", "Recordatorio", suffix.substring(0, 20), "rem." + suffix + "@example.test", "$2a$10$notarealhashnotarealhashnotarealhashnotarealhashnotare");
        long id = jdbc.queryForObject("SELECT id FROM users WHERE document_number = ?", Long.class, suffix.substring(0, 20));
        users.add(id);
        return id;
    }

    private String email(long userId) { return jdbc.queryForObject("SELECT email FROM users WHERE id = ?", String.class, userId); }

    private long appointment(long patient, String status, LocalDateTime start) {
        long professional = jdbc.queryForObject("SELECT id FROM professionals ORDER BY id LIMIT 1", Long.class);
        jdbc.update("""
                INSERT INTO appointments (patient_user_id, professional_id, location_id, specialty_id, status_id, scheduled_start_at, scheduled_end_at, created_by_user_id)
                VALUES (?, ?, 1, 1, (SELECT id FROM appointment_statuses WHERE code = ?), ?, ?, ?)
                """, patient, professional, status, start, start.plusMinutes(30), patient);
        long id = jdbc.queryForObject("SELECT MAX(id) FROM appointments WHERE patient_user_id = ?", Long.class, patient);
        appointments.add(id);
        return id;
    }

    private String jwt(String role) {
        byte[] secret = "test-access-secret-must-be-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8);
        Date now = new Date();
        return Jwts.builder().subject("1").claim("typ", "access").claim("roles", List.of(role))
                .issuedAt(now).expiration(new Date(now.getTime() + 300_000)).signWith(Keys.hmacShaKeyFor(secret)).compact();
    }
}
