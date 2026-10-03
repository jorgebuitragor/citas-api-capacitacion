package co.fcv.citas.application.auth;

import co.fcv.citas.domain.auth.PasswordResetToken;
import co.fcv.citas.domain.auth.RefreshSession;
import co.fcv.citas.domain.auth.UserAccount;
import java.time.Instant;
import java.util.Optional;

public final class AuthPorts {
    private AuthPorts() { }

    public interface UserRepository {
        boolean existsByEmail(String email);
        boolean existsByDocument(String type, String number);
        UserAccount save(UserAccount user);
        Optional<UserAccount> findByEmail(String email);
        Optional<UserAccount> findById(String id);
        void updatePassword(String userId, String newPasswordHash);
    }

    public interface SessionRepository {
        void save(RefreshSession session);
        Optional<RefreshSession> findSessionByTokenHash(String tokenHash);
        void revoke(String tokenHash);
    }

    /** HU-004 (DEC-008): tokens de un solo uso, persistidos solo como hash. */
    public interface PasswordResetRepository {
        void save(PasswordResetToken token);
        Optional<PasswordResetToken> findByTokenHash(String tokenHash);
        void consume(String tokenHash, Instant consumedAt);
    }
}
