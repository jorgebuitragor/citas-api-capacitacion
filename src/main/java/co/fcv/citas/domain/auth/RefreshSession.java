package co.fcv.citas.domain.auth;

import java.time.Instant;

public record RefreshSession(
        String userId, String tokenHash, Instant expiresAt, Instant revokedAt) {
    public boolean isActiveAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
