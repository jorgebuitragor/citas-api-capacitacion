package co.fcv.citas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.ArrayList;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    private final List<Long> createdAppointments = new ArrayList<>();

    @AfterEach void releaseFixtures() {
        for (Long id : createdAppointments) jdbc.update("DELETE FROM appointments WHERE id = ?", id);
    }

    @Test void rejectsSecondReservationWithoutCreatingAnotherAppointment() throws Exception {
        String userId = registerUser();
        Slot slot = slotFor("MEDICINA_GENERAL");
        long before = jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class);

        long appointmentId = appointmentId(reserve(userId, slot).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(appointmentId);
        reserve(userId, slot).andExpect(status().isConflict());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class)).isEqualTo(before + 1);
        assertThat(statusOf(appointmentId)).isEqualTo("APPROVED");
        assertThat(slotCount(appointmentId)).isEqualTo(1);
        assertThat(historyCount(appointmentId, "APPROVED", "SYSTEM")).isEqualTo(1);
        assertThat(historyActor(appointmentId, "APPROVED")).isNull();
    }

    @Test void userCanSearchOnlyCompleteAvailabilityUsingAllFilters() throws Exception {
        String userId = registerUser();
        Slot slot = slotFor("MEDICINA_GENERAL");
        mvc.perform(get("/api/v1/catalogs/locations").header("Authorization", "Bearer " + accessToken(userId, "USER"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/availability")
                .header("Authorization", "Bearer " + accessToken(userId, "USER"))
                .param("locationId", Long.toString(slot.locationId())).param("specialtyId", Long.toString(slot.specialtyId()))
                .param("professionalId", Long.toString(slot.professionalId())).param("date", slot.startAt().substring(0, 10)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].startAt").isNotEmpty());
    }

    @Test void specializedRequestReservesTwoConsecutiveSlotsAndRejectsPartialDuration() throws Exception {
        String userId = registerUser();
        Slot full = slotFor("ORTOPEDIA_TRAUMATOLOGIA");
        long requested = appointmentId(reserve(userId, full).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(requested);
        assertThat(statusOf(requested)).isEqualTo("REQUESTED");
        assertThat(slotCount(requested)).isEqualTo(2);
        assertThat(historyCount(requested, "REQUESTED", "USER")).isEqualTo(1);
        assertThat(historyActor(requested, "REQUESTED")).isEqualTo(Long.parseLong(userId));

        Slot finalHalfHour = jdbc.queryForObject("""
                SELECT ab.location_id, pspecialty.specialty_id, ab.professional_id,
                       DATE_FORMAT(ps.start_at, '%Y-%m-%dT%H:%i:%s')
                FROM professional_slots ps
                JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                JOIN professional_specialties pspecialty ON pspecialty.professional_id = ab.professional_id
                JOIN specialties specialty ON specialty.id = pspecialty.specialty_id
                WHERE specialty.code = 'ORTOPEDIA_TRAUMATOLOGIA' AND specialty.active = TRUE
                  AND pspecialty.active = TRUE AND ab.active = TRUE
                  AND ps.appointment_id IS NULL AND ps.start_at > NOW()
                ORDER BY ps.start_at DESC
                LIMIT 1
                """, (rs, row) -> new Slot(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4)));
        long before = jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class);
        reserve(userId, finalHalfHour).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM appointments", Long.class)).isEqualTo(before);
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM professional_slots ps JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                WHERE ps.appointment_id IS NULL AND ps.start_at = ? AND ab.professional_id = ? AND ab.location_id = ?
                """, Long.class, java.time.LocalDateTime.parse(finalHalfHour.startAt()), finalHalfHour.professionalId(), finalHalfHour.locationId())).isEqualTo(1);
    }

    @Test void adminApprovesOrRejectsRequestedAppointmentsAndKeepsAudit() throws Exception {
        String requester = registerUser();
        String admin = registerUser();
        long approved = appointmentId(reserve(requester, slotFor("CARDIOLOGIA_ADULTO")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(approved);
        decide(admin, approved, "APPROVE", null).andExpect(status().isOk());
        assertThat(statusOf(approved)).isEqualTo("APPROVED");
        assertThat(slotCount(approved)).isEqualTo(1);
        assertThat(historyCount(approved, "APPROVED", "ADMIN")).isEqualTo(1);
        assertThat(historyActor(approved, "APPROVED")).isEqualTo(Long.parseLong(admin));

        long rejected = appointmentId(reserve(requester, slotFor("CARDIOLOGIA_ADULTO")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(rejected);
        decide(admin, rejected, "REJECT", " ").andExpect(status().isBadRequest());
        assertThat(statusOf(rejected)).isEqualTo("REQUESTED");
        assertThat(slotCount(rejected)).isEqualTo(1);
        decide(admin, rejected, "REJECT", "Información clínica pendiente").andExpect(status().isOk());
        assertThat(statusOf(rejected)).isEqualTo("REJECTED");
        assertThat(slotCount(rejected)).isZero();
        assertThat(historyCount(rejected, "REJECTED", "ADMIN")).isEqualTo(1);
    }

    @Test void userAndProfessionalCannotDecideSpecializedRequests() throws Exception {
        String requester = registerUser();
        long requested = appointmentId(reserve(requester, slotFor("CARDIOLOGIA_ADULTO")).andExpect(status().isCreated()).andReturn());
        createdAppointments.add(requested);
        decide(requester, requested, "APPROVE", null, "USER").andExpect(status().isForbidden());
        decide(registerUser(), requested, "APPROVE", null, "PROFESSIONAL").andExpect(status().isForbidden());
        assertThat(statusOf(requested)).isEqualTo("REQUESTED");
    }

    private String registerUser() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String body = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of(
                "firstName", "Reserva", "lastName", "Prueba", "documentType", "CC", "documentNumber", suffix.substring(0, 20),
                "email", "booking." + suffix + "@example.test", "phone", "3000000000", "password", "BookingTestOnly!"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asText();
    }

    private org.springframework.test.web.servlet.ResultActions reserve(String userId, Slot slot) throws Exception {
        return mvc.perform(post("/api/v1/appointments")
                .header("Authorization", "Bearer " + accessToken(userId, "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "locationId", Long.toString(slot.locationId()),
                        "specialtyId", Long.toString(slot.specialtyId()),
                        "professionalId", Long.toString(slot.professionalId()),
                        "startAt", slot.startAt()))));
    }

    private org.springframework.test.web.servlet.ResultActions decide(String adminUserId, long appointmentId, String decision, String reason) throws Exception {
        return decide(adminUserId, appointmentId, decision, reason, "ADMIN");
    }

    private org.springframework.test.web.servlet.ResultActions decide(String userId, long appointmentId, String decision, String reason, String role) throws Exception {
        return mvc.perform(post("/api/v1/admin/appointments/{id}/decision", appointmentId)
                .header("Authorization", "Bearer " + accessToken(userId, role)).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reason == null ? Map.of("decision", decision) : Map.of("decision", decision, "reason", reason))));
    }

    private Slot slotFor(String specialtyCode) {
        return jdbc.queryForObject("""
                SELECT ab.location_id, pspecialty.specialty_id, ab.professional_id,
                       DATE_FORMAT(ps.start_at, '%Y-%m-%dT%H:%i:%s')
                FROM professional_slots ps
                JOIN availability_blocks ab ON ab.id = ps.availability_block_id
                JOIN professional_specialties pspecialty ON pspecialty.professional_id = ab.professional_id
                JOIN specialties specialty ON specialty.id = pspecialty.specialty_id
                WHERE specialty.code = ? AND specialty.active = TRUE AND pspecialty.active = TRUE AND ab.active = TRUE
                  AND ps.appointment_id IS NULL AND ps.start_at > NOW()
                ORDER BY ps.start_at LIMIT 1
                """, (rs, row) -> new Slot(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4)), specialtyCode);
    }

    private long appointmentId(org.springframework.test.web.servlet.MvcResult result) throws Exception { return Long.parseLong(mapper.readTree(result.getResponse().getContentAsString()).get("id").asText()); }
    private String statusOf(long appointmentId) { return jdbc.queryForObject("SELECT status.code FROM appointments a JOIN appointment_statuses status ON status.id = a.status_id WHERE a.id = ?", String.class, appointmentId); }
    private long slotCount(long appointmentId) { return jdbc.queryForObject("SELECT COUNT(*) FROM professional_slots WHERE appointment_id = ?", Long.class, appointmentId); }
    private long historyCount(long appointmentId, String status, String source) { return jdbc.queryForObject("""
            SELECT COUNT(*) FROM appointment_status_history h JOIN appointment_statuses s ON s.id = h.status_id
            WHERE h.appointment_id = ? AND s.code = ? AND h.change_source = ?
            """, Long.class, appointmentId, status, source); }
    private Long historyActor(long appointmentId, String status) { return jdbc.queryForObject("""
            SELECT h.changed_by_user_id FROM appointment_status_history h JOIN appointment_statuses s ON s.id = h.status_id
            WHERE h.appointment_id = ? AND s.code = ? ORDER BY h.id DESC LIMIT 1
            """, Long.class, appointmentId, status); }

    private String accessToken(String subject, String role) {
        byte[] secret = "test-access-secret-must-be-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8);
        Date now = new Date();
        return Jwts.builder().subject(subject).claim("typ", "access").claim("roles", List.of(role))
                .issuedAt(now).expiration(new Date(now.getTime() + 300_000)).signWith(Keys.hmacShaKeyFor(secret)).compact();
    }

    private record Slot(long locationId, long specialtyId, long professionalId, String startAt) { }
}
