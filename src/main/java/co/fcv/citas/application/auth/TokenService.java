package co.fcv.citas.application.auth;

import co.fcv.citas.domain.auth.Role;
import java.time.Instant;
import java.util.Set;

public interface TokenService {
    AuthCommands.TokenPair issue(String userId, Set<Role> roles);
    ParsedRefresh parseRefresh(String rawToken);
    ParsedAccess parseAccess(String rawToken);
    record ParsedRefresh(String sessionId, String userId, Instant expiresAt) { }
    record ParsedAccess(String userId, Set<Role> roles) { }
}
