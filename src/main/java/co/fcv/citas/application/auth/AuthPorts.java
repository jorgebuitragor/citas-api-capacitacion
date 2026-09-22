package co.fcv.citas.application.auth;

import co.fcv.citas.domain.auth.RefreshSession;
import co.fcv.citas.domain.auth.UserAccount;
import java.util.Optional;

public final class AuthPorts {
    private AuthPorts() { }

    public interface UserRepository {
        boolean existsByEmail(String email);
        boolean existsByDocument(String type, String number);
        UserAccount save(UserAccount user);
        Optional<UserAccount> findByEmail(String email);
        Optional<UserAccount> findById(String id);
    }

    public interface SessionRepository {
        void save(RefreshSession session);
        Optional<RefreshSession> findSessionByTokenHash(String tokenHash);
        void revoke(String tokenHash);
    }
}
