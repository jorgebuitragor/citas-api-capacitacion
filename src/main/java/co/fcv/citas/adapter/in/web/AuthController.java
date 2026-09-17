package co.fcv.citas.adapter.in.web;

import co.fcv.citas.application.auth.AuthCommands;
import co.fcv.citas.application.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final String REFRESH_COOKIE = "refresh_token";
    private static final String CSRF_COOKIE = "XSRF-TOKEN";
    private final AuthService auth;
    private final Duration refreshTtl;
    private final boolean secureCookie;
    private final SecureRandom random = new SecureRandom();
    public AuthController(AuthService auth, @Value("${app.security.refresh-ttl}") Duration refreshTtl,
                          @Value("${app.security.refresh-cookie-secure:true}") boolean secureCookie) {
        this.auth = auth; this.refreshTtl = refreshTtl; this.secureCookie = secureCookie;
    }
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        var registered = auth.register(new AuthCommands.RegisterCommand(request.firstName, request.lastName, request.documentType,
                request.documentNumber, request.email, request.phone, request.password));
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegistrationResponse(registered.id(), registered.email(), registered.roles()));
    }
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return sessionResponse(auth.login(new AuthCommands.LoginCommand(request.email, request.password)));
    }
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request, @RequestHeader(value = "X-CSRF-Token", required = false) String csrf) {
        rejectJsonBody(request);
        assertCsrf(request, csrf);
        return sessionResponse(auth.refresh(requiredCookie(request, REFRESH_COOKIE)));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, @RequestHeader(value = "X-CSRF-Token", required = false) String csrf) {
        rejectJsonBody(request);
        assertCsrf(request, csrf);
        auth.logout(requiredCookie(request, REFRESH_COOKIE));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, expiredCookie(REFRESH_COOKIE, true).toString())
                .header(HttpHeaders.SET_COOKIE, expiredCookie(CSRF_COOKIE, false).toString()).build();
    }
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of("subject", authentication.getName(), "roles", authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }
    private ResponseEntity<TokenResponse> sessionResponse(AuthCommands.TokenPair pair) {
        String csrf = csrfToken();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie(pair.refreshToken()).toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie(csrf).toString())
                .body(new TokenResponse(pair.accessToken(), "Bearer", pair.accessExpiresAt(), csrf));
    }
    private ResponseCookie refreshCookie(String token) { return ResponseCookie.from(REFRESH_COOKIE, token).httpOnly(true).secure(secureCookie).sameSite("Lax")
            .path("/api/v1/auth").maxAge(refreshTtl).build(); }
    private ResponseCookie csrfCookie(String token) { return ResponseCookie.from(CSRF_COOKIE, token).httpOnly(false).secure(secureCookie).sameSite("Lax")
            .path("/api/v1/auth").maxAge(refreshTtl).build(); }
    private ResponseCookie expiredCookie(String name, boolean httpOnly) { return ResponseCookie.from(name, "").httpOnly(httpOnly).secure(secureCookie).sameSite("Lax")
            .path("/api/v1/auth").maxAge(Duration.ZERO).build(); }
    private String csrfToken() { byte[] bytes = new byte[32]; random.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private static String requiredCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) for (Cookie cookie : request.getCookies()) if (name.equals(cookie.getName()) && !cookie.getValue().isBlank()) return cookie.getValue();
        throw new InvalidCsrfException();
    }
    private static void assertCsrf(HttpServletRequest request, String header) {
        String cookie = requiredCookie(request, CSRF_COOKIE);
        if (header == null || !java.security.MessageDigest.isEqual(cookie.getBytes(java.nio.charset.StandardCharsets.UTF_8), header.getBytes(java.nio.charset.StandardCharsets.UTF_8))) throw new InvalidCsrfException();
    }
    private static void rejectJsonBody(HttpServletRequest request) {
        if (request.getContentType() != null && request.getContentType().toLowerCase(java.util.Locale.ROOT).contains("application/json")) throw new InvalidCsrfException();
    }
    public record RegisterRequest(@NotBlank @Size(max = 100) String firstName, @NotBlank @Size(max = 100) String lastName,
                                  @NotBlank @Size(max = 30) String documentType, @NotBlank @Size(max = 50) String documentNumber,
                                  @NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 30) String phone,
                                  @NotBlank @Size(max = 128) String password) { }
    public record LoginRequest(@NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 128) String password) { }
    public record RegistrationResponse(String id, String email, java.util.Set<String> roles) { }
    public record TokenResponse(String accessToken, String tokenType, Instant accessExpiresAt, String csrfToken) { }
}
