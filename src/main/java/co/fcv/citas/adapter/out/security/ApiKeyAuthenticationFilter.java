package co.fcv.citas.adapter.out.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Credencial de servicio para n8n (DEC-010). Solo actúa sobre /api/v1/automation/** y solo concede
 * ROLE_AUTOMATION. Con la clave vacía queda deshabilitada; la clave nunca se registra.
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final String PREFIX = "/api/v1/automation/";
    private final byte[] expected;

    public ApiKeyAuthenticationFilter(@Value("${app.automation.api-key:}") String apiKey) {
        this.expected = apiKey == null ? new byte[0] : apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String provided = request.getHeader("X-Api-Key");
        if (request.getRequestURI().startsWith(PREFIX) && provided != null && expected.length > 0
                && MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("automation", null, List.of(new SimpleGrantedAuthority("ROLE_AUTOMATION"))));
        }
        chain.doFilter(request, response);
    }
}
