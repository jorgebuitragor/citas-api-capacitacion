package co.fcv.citas;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// El origen CORS se fija aquí porque una variable de entorno APP_CORS_ALLOWED_ORIGINS del contenedor
// tiene más precedencia que src/test/resources/application.yml y haría dependiente la prueba del host.
@SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:3000")
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    private String testSuffix;

    @BeforeEach void isolateTestData() { testSuffix = UUID.randomUUID().toString().replace("-", ""); }

    @Test void registersUserWithBcryptHashAndUniqueIdentity() throws Exception {
        register(email("ana"), "CC", document("1010")).andExpect(status().isCreated()).andExpect(jsonPath("$.roles[0]").value("USER"));
        String hash = jdbc.queryForObject("SELECT password_hash FROM users WHERE email = ?", String.class, email("ana"));
        assertThat(hash).startsWith("$2").doesNotContain("Secret123!");
        register(email("ana"), "CC", document("2020")).andExpect(status().isConflict());
        register(email("other"), "CC", document("1010")).andExpect(status().isConflict());
        register(email("case"), "cc", document("1010")).andExpect(status().isConflict());
    }

    @Test void loginIssuesAccessAndSecureRefreshAndCsrfCookies() throws Exception {
        register(email("ana"), "CC", document("1010"));
        MvcResult result = login(email("ana"), "Secret123!").andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.hasItems(org.hamcrest.Matchers.containsString("refresh_token="), org.hamcrest.Matchers.containsString("XSRF-TOKEN=")))).andReturn();
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies.stream().filter(value -> value.startsWith("refresh_token=")).findFirst().orElseThrow())
                .contains("HttpOnly", "Secure", "SameSite=Lax", "Path=/api/v1/auth");
    }

    @Test void rotatesRefreshAndRejectsReuseOrInvalidCsrf() throws Exception {
        register(email("ana"), "CC", document("1010"));
        Session initial = session(login(email("ana"), "Secret123!").andReturn());
        mvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", initial.refresh()), new Cookie("XSRF-TOKEN", initial.csrf())))
                .andExpect(status().isUnauthorized());
        Session rotated = session(mvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", initial.refresh()), new Cookie("XSRF-TOKEN", initial.csrf()))
                .header("X-CSRF-Token", initial.csrf())).andExpect(status().isOk()).andReturn());
        assertThat(rotated.refresh()).isNotEqualTo(initial.refresh());
        mvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", initial.refresh()), new Cookie("XSRF-TOKEN", initial.csrf()))
                .header("X-CSRF-Token", initial.csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", rotated.refresh()), new Cookie("XSRF-TOKEN", rotated.csrf()))
                .header("X-CSRF-Token", "wrong")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", rotated.refresh() + "x"), new Cookie("XSRF-TOKEN", rotated.csrf()))
                .header("X-CSRF-Token", rotated.csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", expiredRefresh()), new Cookie("XSRF-TOKEN", rotated.csrf()))
                .header("X-CSRF-Token", rotated.csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("XSRF-TOKEN", rotated.csrf())).header("X-CSRF-Token", rotated.csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"ignored\"}")
                .cookie(new Cookie("refresh_token", rotated.refresh()), new Cookie("XSRF-TOKEN", rotated.csrf())).header("X-CSRF-Token", rotated.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test void logoutRevokesRefreshAndProtectedRoutesRequireAccess() throws Exception {
        register(email("ana"), "CC", document("1010"));
        Session initial = session(login(email("ana"), "Secret123!").andReturn());
        mvc.perform(post("/api/v1/auth/logout").cookie(new Cookie("refresh_token", initial.refresh()), new Cookie("XSRF-TOKEN", initial.csrf()))
                .header("X-CSRF-Token", initial.csrf())).andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));
        mvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", initial.refresh()), new Cookie("XSRF-TOKEN", initial.csrf()))
                .header("X-CSRF-Token", initial.csrf())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer invalid")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredAccess())).andExpect(status().isUnauthorized());
        MvcResult login = login(email("ana"), "Secret123!").andReturn();
        String access = mapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
        mvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + access)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/probe").header(HttpHeaders.AUTHORIZATION, "Bearer " + access)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email", email("ana"), "password", "bad"))))
                .andExpect(status().isUnauthorized());
    }

    @Test void corsAllowsOnlyConfiguredOriginWithCredentials() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/v1/auth/refresh")
                .header("Origin", "http://localhost:3000").header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/v1/auth/refresh")
                .header("Origin", "https://untrusted.example").header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test void requestingResetForActiveEmailIssuesSingleUseTokenOnlyInLogNeverInResponse() throws Exception {
        register(email("ana"), "CC", document("1010"));
        AtomicReference<MvcResult> captured = new AtomicReference<>();
        String token = capturingResetToken(() -> captured.set(requestPasswordReset(email("ana")).andExpect(status().isAccepted()).andReturn()));
        assertThat(captured.get().getResponse().getContentAsString()).isEmpty();
        assertThat(token).isNotBlank();

        confirmPasswordReset(token, "NewSecret123!").andExpect(status().isOk());
        login(email("ana"), "Secret123!").andExpect(status().isUnauthorized());
        login(email("ana"), "NewSecret123!").andExpect(status().isOk());

        confirmPasswordReset(token, "AnotherOne123!").andExpect(status().isUnauthorized());
    }

    @Test void requestingResetForUnknownOrInactiveEmailStillRespondsAcceptedWithoutIssuingToken() throws Exception {
        String noToken = capturingResetToken(() -> requestPasswordReset("unknown." + testSuffix + "@example.test").andExpect(status().isAccepted()));
        assertThat(noToken).isNull();

        register(email("inactive"), "CC", document("3030"));
        jdbc.update("UPDATE users SET active = FALSE WHERE email = ?", email("inactive"));
        String stillNoToken = capturingResetToken(() -> requestPasswordReset(email("inactive")).andExpect(status().isAccepted()));
        assertThat(stillNoToken).isNull();
    }

    @Test void confirmRejectsExpiredOrUnknownToken() throws Exception {
        register(email("ana"), "CC", document("1010"));
        String token = capturingResetToken(() -> requestPasswordReset(email("ana")).andExpect(status().isAccepted()));
        jdbc.update("UPDATE password_reset_tokens SET expires_at = ? WHERE token_hash = ?", Instant.now().minusSeconds(60), sha256(token));

        confirmPasswordReset(token, "NewSecret123!").andExpect(status().isUnauthorized());
        confirmPasswordReset("never-issued-token", "NewSecret123!").andExpect(status().isUnauthorized());
        login(email("ana"), "Secret123!").andExpect(status().isOk());
    }

    @Test void passwordResetRequestAndConfirmValidateInput() throws Exception {
        mvc.perform(post("/api/v1/auth/password-reset/request").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/password-reset/confirm").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"\",\"newPassword\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions requestPasswordReset(String email) throws Exception {
        return mvc.perform(post("/api/v1/auth/password-reset/request").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("email", email))));
    }
    private org.springframework.test.web.servlet.ResultActions confirmPasswordReset(String token, String newPassword) throws Exception {
        return mvc.perform(post("/api/v1/auth/password-reset/confirm").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("token", token, "newPassword", newPassword))));
    }
    /**
     * Captura el token en texto plano que `AuthService` escribe en su log — es el único lugar donde existe
     * (DEC-008: canal de desarrollo = log del servidor, nunca la respuesta HTTP). El appender se engancha
     * antes de ejecutar `action` para no perder el evento.
     */
    private String capturingResetToken(ThrowingRunnable action) throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger("co.fcv.citas.application.auth.AuthService");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            action.run();
            return appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                    .filter(message -> message.startsWith("[DEV] Password reset token for user"))
                    .reduce((first, second) -> second)
                    .map(message -> message.split(": ", 2)[1].split(" ", 2)[0])
                    .orElse(null);
        } finally {
            logger.detachAppender(appender);
        }
    }
    private interface ThrowingRunnable { void run() throws Exception; }
    private static String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }

    private org.springframework.test.web.servlet.ResultActions register(String email, String documentType, String documentNumber) throws Exception {
        return mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of(
                "firstName", "Ana", "lastName", "Prueba", "documentType", documentType, "documentNumber", documentNumber,
                "email", email, "phone", "3000000000", "password", "Secret123!"))));
    }
    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email", email, "password", password))));
    }
    private String email(String prefix) { return prefix + "." + testSuffix + "@example.test"; }
    private String document(String prefix) { return prefix + testSuffix.substring(0, 20); }
    private Session session(MvcResult result) throws Exception {
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        String refresh = cookie(result, "refresh_token");
        String csrf = cookie(result, "XSRF-TOKEN");
        assertThat(body.get("csrfToken").asText()).isEqualTo(csrf);
        return new Session(refresh, csrf);
    }
    private String cookie(MvcResult result, String name) {
        return result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream().filter(value -> value.startsWith(name + "=")).findFirst().orElseThrow()
                .substring((name + "=").length()).split(";", 2)[0];
    }
    private record Session(String refresh, String csrf) { }
    private String expiredRefresh() {
        byte[] secret = "test-refresh-secret-must-be-at-least-32-bytes-long".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Jwts.builder().subject("unknown-user").id("expired-session").claim("typ", "refresh")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000)).expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(secret)).compact();
    }
    private String expiredAccess() {
        byte[] secret = "test-access-secret-must-be-at-least-32-bytes-long".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Jwts.builder().subject("unknown-user").id("expired-access").claim("typ", "access").claim("roles", List.of("USER"))
                .issuedAt(new Date(System.currentTimeMillis() - 120_000)).expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(secret)).compact();
    }
}
