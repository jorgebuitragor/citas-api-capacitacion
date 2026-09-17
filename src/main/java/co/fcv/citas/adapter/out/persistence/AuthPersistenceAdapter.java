package co.fcv.citas.adapter.out.persistence;

import co.fcv.citas.application.auth.AuthPorts.SessionRepository;
import co.fcv.citas.application.auth.AuthPorts.UserRepository;
import co.fcv.citas.domain.auth.RefreshSession;
import co.fcv.citas.domain.auth.Role;
import co.fcv.citas.domain.auth.UserAccount;
import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AuthPersistenceAdapter implements UserRepository, SessionRepository {
    private final UserJpaRepository users;
    private final RoleJpaRepository roles;
    private final RefreshSessionJpaRepository sessions;
    private final Clock clock;
    public AuthPersistenceAdapter(UserJpaRepository users, RoleJpaRepository roles, RefreshSessionJpaRepository sessions, Clock clock) {
        this.users = users; this.roles = roles; this.sessions = sessions; this.clock = clock;
    }
    @Override public boolean existsByEmail(String email) { return users.existsByEmail(email); }
    @Override public boolean existsByDocument(String type, String number) { return users.existsByDocumentTypeAndDocumentNumber(type, number); }
    @Override public UserAccount save(UserAccount source) {
        UserEntity entity = new UserEntity();
        entity.id = source.id(); entity.firstName = source.firstName(); entity.lastName = source.lastName();
        entity.documentType = source.documentType(); entity.documentNumber = source.documentNumber(); entity.email = source.email();
        entity.phone = source.phone(); entity.passwordHash = source.passwordHash(); entity.active = source.active(); entity.createdAt = clock.instant();
        entity.roles.addAll(source.roles().stream().map(role -> roles.findByName(role.name()).orElseThrow()).collect(Collectors.toSet()));
        return map(users.save(entity));
    }
    @Override public Optional<UserAccount> findByEmail(String email) { return users.findByEmail(email).map(this::map); }
    @Override public Optional<UserAccount> findById(String id) { return users.findById(id).map(this::map); }
    @Override public void save(RefreshSession source) {
        RefreshSessionEntity entity = new RefreshSessionEntity(); entity.id = source.id(); entity.userId = source.userId();
        entity.tokenHash = source.tokenHash(); entity.issuedAt = source.issuedAt(); entity.expiresAt = source.expiresAt();
        entity.revokedAt = source.revokedAt(); entity.replacedBy = source.replacedBy(); sessions.save(entity);
    }
    @Override public Optional<RefreshSession> findSessionById(String id) { return sessions.findById(id).map(this::map); }
    @Override public void revoke(String id, String replacementId) {
        RefreshSessionEntity entity = sessions.findById(id).orElseThrow(); entity.revokedAt = clock.instant(); entity.replacedBy = replacementId; sessions.save(entity);
    }
    private UserAccount map(UserEntity e) {
        Set<Role> mappedRoles = e.roles.stream().map(r -> Role.valueOf(r.name)).collect(Collectors.toUnmodifiableSet());
        return new UserAccount(e.id, e.firstName, e.lastName, e.documentType, e.documentNumber, e.email, e.phone, e.passwordHash, e.active, mappedRoles);
    }
    private RefreshSession map(RefreshSessionEntity e) { return new RefreshSession(e.id, e.userId, e.tokenHash, e.issuedAt, e.expiresAt, e.revokedAt, e.replacedBy); }
}
