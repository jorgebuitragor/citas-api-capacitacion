package co.fcv.citas.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity @Table(name = "refresh_sessions")
class RefreshSessionEntity {
    @Id String id;
    @Column(name = "user_id") String userId;
    @Column(name = "token_hash") String tokenHash;
    @Column(name = "issued_at") Instant issuedAt;
    @Column(name = "expires_at") Instant expiresAt;
    @Column(name = "revoked_at") Instant revokedAt;
    @Column(name = "replaced_by") String replacedBy;
}
