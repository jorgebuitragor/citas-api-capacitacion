package co.fcv.citas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** V11: las cuentas documentadas en DATOS_SEMILLA.md deben seguir funcionando con la contraseña de laboratorio. */
@SpringBootTest
@AutoConfigureMockMvc
class DemoSeedIntegrationTest {
    private static final String PASSWORD = "Demo1234*";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    private static final Map<String, Set<String>> ACCOUNTS = Map.ofEntries(
            Map.entry("admin@demo.invalid", Set.of("ROLE_ADMIN")),
            Map.entry("admin2@demo.invalid", Set.of("ROLE_ADMIN")),
            Map.entry("multirol@demo.invalid", Set.of("ROLE_ADMIN", "ROLE_PROFESSIONAL", "ROLE_USER")),
            Map.entry("dra.gomez@demo.invalid", Set.of("ROLE_PROFESSIONAL")),
            Map.entry("dr.salazar@demo.invalid", Set.of("ROLE_PROFESSIONAL")),
            Map.entry("dra.ortiz@demo.invalid", Set.of("ROLE_PROFESSIONAL")),
            Map.entry("dr.inactivo@demo.invalid", Set.of("ROLE_PROFESSIONAL")),
            Map.entry("paciente.nuevo@demo.invalid", Set.of("ROLE_USER")),
            Map.entry("paciente.general@demo.invalid", Set.of("ROLE_USER")),
            Map.entry("paciente.especialista@demo.invalid", Set.of("ROLE_USER")),
            Map.entry("paciente.historial@demo.invalid", Set.of("ROLE_USER")),
            Map.entry("paciente.reprogramacion@demo.invalid", Set.of("ROLE_USER")),
            Map.entry("paciente.recordatorio@demo.invalid", Set.of("ROLE_USER")));

    @Test void everyDocumentedAccountLogsInWithItsRoles() throws Exception {
        for (Map.Entry<String, Set<String>> account : ACCOUNTS.entrySet()) {
            String token = login(account.getKey());
            JsonNode me = mapper.readTree(mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
            Set<String> roles = new TreeSet<>();
            me.get("roles").forEach(role -> roles.add(role.asText()));
            assertThat(roles).as(account.getKey()).isEqualTo(new TreeSet<>(account.getValue()));
        }
    }

    @Test void inactivePatientCannotLogIn() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", "paciente.inactivo@demo.invalid", "password", PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test void patientsCoverEveryAppointmentSituation() {
        assertThat(appointmentsOf("paciente.nuevo@demo.invalid")).isEmpty();
        assertThat(appointmentsOf("paciente.general@demo.invalid")).contains("APPROVED", "COMPLETED");
        assertThat(appointmentsOf("paciente.especialista@demo.invalid")).contains("REQUESTED", "APPROVED");
        assertThat(appointmentsOf("paciente.historial@demo.invalid")).contains("COMPLETED", "NO_SHOW", "CANCELLED", "REJECTED");
        assertThat(jdbc.queryForList("""
                SELECT rs.code FROM reschedule_requests r
                JOIN reschedule_request_statuses rs ON rs.id = r.status_id
                JOIN appointments a ON a.id = r.appointment_id
                JOIN users u ON u.id = a.patient_user_id
                WHERE u.email = 'paciente.reprogramacion@demo.invalid'""", String.class)).contains("PENDING", "REJECTED");
    }

    private List<String> appointmentsOf(String email) {
        return jdbc.queryForList("""
                SELECT st.code FROM appointments a
                JOIN appointment_statuses st ON st.id = a.status_id
                JOIN users u ON u.id = a.patient_user_id
                WHERE u.email = ?""", String.class, email);
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("accessToken").asText();
    }
}
