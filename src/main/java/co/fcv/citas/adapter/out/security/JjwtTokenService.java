package co.fcv.citas.adapter.out.security;

import co.fcv.citas.application.auth.AuthCommands.TokenPair;
import co.fcv.citas.application.auth.TokenService;
import co.fcv.citas.domain.auth.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JjwtTokenService implements TokenService {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final Clock clock;
    public JjwtTokenService(@Value("${app.security.access-secret}") String accessSecret,
                            @Value("${app.security.refresh-secret}") String refreshSecret,
                            @Value("${app.security.access-ttl}") Duration accessTtl,
                            @Value("${app.security.refresh-ttl}") Duration refreshTtl, Clock clock) {
        this.accessKey = key(accessSecret); this.refreshKey = key(refreshSecret); this.accessTtl = accessTtl; this.refreshTtl = refreshTtl; this.clock = clock;
    }
    @Override public TokenPair issue(String userId, Set<Role> roles) {
        Instant now = clock.instant(); Instant accessExpiry = now.plus(accessTtl); Instant refreshExpiry = now.plus(refreshTtl);
        String access = Jwts.builder().subject(userId).id(UUID.randomUUID().toString()).claim("typ", "access")
                .claim("roles", roles.stream().map(Enum::name).toList()).issuedAt(Date.from(now)).expiration(Date.from(accessExpiry)).signWith(accessKey).compact();
        String refresh = Jwts.builder().subject(userId).id(UUID.randomUUID().toString()).claim("typ", "refresh")
                .issuedAt(Date.from(now)).expiration(Date.from(refreshExpiry)).signWith(refreshKey).compact();
        return new TokenPair(access, refresh, accessExpiry, refreshExpiry);
    }
    @Override public ParsedRefresh parseRefresh(String rawToken) {
        Claims claims = parse(rawToken, refreshKey); if (!"refresh".equals(claims.get("typ", String.class))) throw new IllegalArgumentException("Not a refresh token");
        return new ParsedRefresh(claims.getId(), claims.getSubject(), claims.getExpiration().toInstant());
    }
    @Override public ParsedAccess parseAccess(String rawToken) {
        Claims claims = parse(rawToken, accessKey); if (!"access".equals(claims.get("typ", String.class))) throw new IllegalArgumentException("Not an access token");
        @SuppressWarnings("unchecked") var rawRoles = (java.util.List<String>) claims.get("roles", java.util.List.class);
        return new ParsedAccess(claims.getSubject(), rawRoles.stream().map(Role::valueOf).collect(Collectors.toUnmodifiableSet()));
    }
    private static Claims parse(String token, SecretKey key) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
    private static SecretKey key(String value) { return Keys.hmacShaKeyFor(value.getBytes(StandardCharsets.UTF_8)); }
}
