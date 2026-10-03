package co.fcv.citas;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regresión: con un servidor real, los errores generados por el framework (400/404/405 vía
 * sendError) se redespachan a /error y, sin permitir el dispatch ERROR, caían en
 * anyRequest().authenticated() y salían como 401. MockMvc no hace ese redespacho, por eso solo
 * un servidor real (puerto aleatorio) lo detecta.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorDispatchIntegrationTest {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();

    @Test void frameworkErrorsKeepTheirStatusInsteadOfBecoming401() throws Exception {
        String token = token("USER");
        assertThat(status("GET", "/api/v1/availability?locationId=1", token)).isEqualTo(400);
        assertThat(status("GET", "/api/v1/catalogs/professionals?locationId=abc&specialtyId=1", token)).isEqualTo(400);
        assertThat(status("GET", "/api/v1/ruta-inexistente", token)).isEqualTo(404);
        assertThat(status("DELETE", "/api/v1/appointments", token)).isEqualTo(405);
    }

    @Test void missingCredentialsStillAnswer401() throws Exception {
        assertThat(status("GET", "/api/v1/appointments", null)).isEqualTo(401);
    }

    private int status(String method, String path, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody());
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private static String token(String role) {
        byte[] secret = "test-access-secret-must-be-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8);
        Date now = new Date();
        return Jwts.builder().subject("1").claim("typ", "access").claim("roles", List.of(role))
                .issuedAt(now).expiration(new Date(now.getTime() + 300_000)).signWith(Keys.hmacShaKeyFor(secret)).compact();
    }
}
