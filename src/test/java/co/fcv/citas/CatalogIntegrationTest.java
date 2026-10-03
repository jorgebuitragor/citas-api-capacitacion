package co.fcv.citas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// El origen CORS se fija aquí porque una variable de entorno APP_CORS_ALLOWED_ORIGINS del contenedor
// tiene más precedencia que src/test/resources/application.yml (mismo motivo que AuthIntegrationTest).
/** HU-006/HU-007/HU-008 (DEC-009). */
@SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:3000")
@AutoConfigureMockMvc
class CatalogIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @Test void createsEpsListsItIncludingInactiveAndRejectsDuplicateCode() throws Exception {
        String admin = token("ADMIN");
        String code = "EPS_" + suffix();
        MvcResult created = postJson("/api/v1/admin/catalogs/eps", admin, Map.of("code", code, "name", "EPS de prueba"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value(code)).andExpect(jsonPath("$.active").value(true)).andReturn();
        String id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(get("/api/v1/admin/catalogs/eps").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == '" + id + "')]").isNotEmpty());

        postJson("/api/v1/admin/catalogs/eps", admin, Map.of("code", code, "name", "Otra")).andExpect(status().isConflict());

        patchJson("/api/v1/admin/catalogs/eps/" + id, admin, Map.of("active", false)).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/api/v1/admin/catalogs/eps").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == '" + id + "')].active").value(false));

        jdbc.update("DELETE FROM eps WHERE id = ?", Long.parseLong(id));
    }

    @Test void epsManagementRequiresAdminRole() throws Exception {
        String user = token("USER");
        mvc.perform(get("/api/v1/admin/catalogs/eps").header("Authorization", "Bearer " + user)).andExpect(status().isForbidden());
        postJson("/api/v1/admin/catalogs/eps", user, Map.of("code", "X", "name", "X")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/catalogs/eps")).andExpect(status().isUnauthorized());
    }

    @Test void createsPlanUnderExistingEpsAndValidatesEpsAndRegimeReferences() throws Exception {
        String admin = token("ADMIN");
        long epsId = jdbc.queryForObject("SELECT id FROM eps LIMIT 1", Long.class);
        long regimeId = jdbc.queryForObject("SELECT id FROM insurance_regimes LIMIT 1", Long.class);
        String code = "PLAN_" + suffix();

        MvcResult created = postJson("/api/v1/admin/catalogs/eps/" + epsId + "/plans", admin, Map.of("regimeId", regimeId, "code", code, "name", "Plan de prueba"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.epsId").value(Long.toString(epsId)))
                .andExpect(jsonPath("$.regime.id").value(Long.toString(regimeId))).andReturn();
        String planId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(get("/api/v1/admin/catalogs/eps/" + epsId + "/plans").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == '" + planId + "')]").isNotEmpty());

        postJson("/api/v1/admin/catalogs/eps/" + epsId + "/plans", admin, Map.of("regimeId", regimeId, "code", code, "name", "Duplicado")).andExpect(status().isConflict());
        postJson("/api/v1/admin/catalogs/eps/999999/plans", admin, Map.of("regimeId", regimeId, "code", "OTRO", "name", "X")).andExpect(status().isNotFound());
        postJson("/api/v1/admin/catalogs/eps/" + epsId + "/plans", admin, Map.of("regimeId", 999999, "code", "OTRO2", "name", "X")).andExpect(status().isBadRequest());

        patchJson("/api/v1/admin/catalogs/eps/" + epsId + "/plans/" + planId, admin, Map.of("active", false))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        patchJson("/api/v1/admin/catalogs/eps/999999/plans/" + planId, admin, Map.of("active", true)).andExpect(status().isNotFound());

        jdbc.update("DELETE FROM eps_plans WHERE id = ?", Long.parseLong(planId));
    }

    @Test void plansManagementRequiresAdminRole() throws Exception {
        String user = token("USER");
        long epsId = jdbc.queryForObject("SELECT id FROM eps LIMIT 1", Long.class);
        mvc.perform(get("/api/v1/admin/catalogs/eps/" + epsId + "/plans").header("Authorization", "Bearer " + user)).andExpect(status().isForbidden());
    }

    @Test void createsSpecialtyValidatesDurationAndKeepsCodeAndDurationImmutable() throws Exception {
        String admin = token("ADMIN");
        String code = "ESP_" + suffix();

        postJson("/api/v1/admin/catalogs/specialties", admin, Map.of("code", code, "name", "Especialidad de prueba",
                "durationMinutes", 45, "general", false, "requiresAdminApproval", true)).andExpect(status().isBadRequest());

        MvcResult created = postJson("/api/v1/admin/catalogs/specialties", admin, Map.of("code", code, "name", "Especialidad de prueba",
                        "durationMinutes", 60, "general", false, "requiresAdminApproval", true))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.durationMinutes").value(60)).andReturn();
        String id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        postJson("/api/v1/admin/catalogs/specialties", admin, Map.of("code", code, "name", "Otra",
                "durationMinutes", 30, "general", false, "requiresAdminApproval", true)).andExpect(status().isConflict());

        patchJson("/api/v1/admin/catalogs/specialties/" + id, admin, Map.of("name", "Nombre actualizado", "active", false))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Nombre actualizado"))
                .andExpect(jsonPath("$.active").value(false)).andExpect(jsonPath("$.durationMinutes").value(60))
                .andExpect(jsonPath("$.code").value(code));

        mvc.perform(get("/api/v1/admin/catalogs/specialties").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == '" + id + "')].active").value(false));

        jdbc.update("DELETE FROM specialties WHERE id = ?", Long.parseLong(id));
    }

    @Test void specialtiesManagementRequiresAdminRoleAndListIncludesInactive() throws Exception {
        String user = token("USER");
        mvc.perform(get("/api/v1/admin/catalogs/specialties").header("Authorization", "Bearer " + user)).andExpect(status().isForbidden());
        String admin = token("ADMIN");
        mvc.perform(get("/api/v1/admin/catalogs/specialties").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.code == 'MEDICINA_GENERAL')]").isNotEmpty());
    }

    /**
     * Regresión: el PATCH de estos endpoints quedó bloqueado por CORS porque la lista global
     * de métodos permitidos en SecurityConfiguration no incluía "PATCH" (solo GET/POST/OPTIONS).
     * MockMvc sin encabezado Origin no dispara el filtro CORS, por eso las demás pruebas de este
     * archivo no lo detectaban: solo un preflight real (OPTIONS + Access-Control-Request-Method) lo hace.
     */
    @Test void corsAllowsPatchForConfiguredOrigin() throws Exception {
        mvc.perform(options("/api/v1/admin/catalogs/eps/1").header("Origin", "http://localhost:3000").header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("PATCH")));
    }

    @Test void listsInsuranceRegimesForAnyAuthenticatedUser() throws Exception {
        mvc.perform(get("/api/v1/catalogs/insurance-regimes").header("Authorization", "Bearer " + token("USER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.code == 'CONTRIBUTIVO')]").isNotEmpty());
        mvc.perform(get("/api/v1/catalogs/insurance-regimes")).andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions postJson(String path, String token, Map<String, ?> body) throws Exception {
        return mvc.perform(post(path).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)));
    }
    private org.springframework.test.web.servlet.ResultActions patchJson(String path, String token, Map<String, ?> body) throws Exception {
        return mvc.perform(patch(path).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)));
    }
    private static String suffix() { return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(java.util.Locale.ROOT); }
    private String token(String role) {
        byte[] secret = "test-access-secret-must-be-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8);
        Date now = new Date();
        return Jwts.builder().subject("1").claim("typ", "access").claim("roles", List.of(role))
                .issuedAt(now).expiration(new Date(now.getTime() + 300_000)).signWith(Keys.hmacShaKeyFor(secret)).compact();
    }
}
