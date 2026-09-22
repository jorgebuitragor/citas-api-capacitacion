package co.fcv.citas.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity @Table(name = "refresh_tokens")
class RefreshSessionEntity {
    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY) Long id;
    @Column(name = "user_id") Long userId;
    @Column(name = "token_hash") String tokenHash;
    @Column(name = "expires_at") Instant expiresAt;
    @Column(name = "revoked_at") Instant revokedAt;
}
