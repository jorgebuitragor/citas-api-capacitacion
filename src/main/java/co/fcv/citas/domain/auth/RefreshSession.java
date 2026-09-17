package co.fcv.citas.domain.auth;

import java.time.Instant;

public record RefreshSession(
        String id, String userId, String tokenHash, Instant issuedAt, Instant expiresAt,
        Instant revokedAt, String replacedBy) {
    public boolean isActiveAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
