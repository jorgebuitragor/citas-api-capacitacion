package co.fcv.citas.domain.auth;

import java.time.Instant;

public record PasswordResetToken(String userId, String tokenHash, Instant expiresAt, Instant consumedAt) {
    public boolean isActiveAt(Instant now) {
        return consumedAt == null && expiresAt.isAfter(now);
    }
}
