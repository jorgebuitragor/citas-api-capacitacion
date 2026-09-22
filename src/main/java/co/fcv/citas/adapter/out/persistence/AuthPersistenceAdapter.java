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
        entity.firstName = source.firstName(); entity.lastName = source.lastName();
        entity.documentType = source.documentType(); entity.documentNumber = source.documentNumber(); entity.email = source.email();
        entity.phone = source.phone(); entity.passwordHash = source.passwordHash(); entity.active = source.active();
        entity.roles.addAll(source.roles().stream().map(role -> roles.findByCode(role.name()).orElseThrow()).collect(Collectors.toSet()));
        return map(users.save(entity));
    }
    @Override public Optional<UserAccount> findByEmail(String email) { return users.findByEmail(email).map(this::map); }
    @Override public Optional<UserAccount> findById(String id) {
        try { return users.findById(Long.parseLong(id)).map(this::map); }
        catch (NumberFormatException ex) { return Optional.empty(); }
    }
    @Override public void save(RefreshSession source) {
        RefreshSessionEntity entity = new RefreshSessionEntity(); entity.userId = Long.parseLong(source.userId());
        entity.tokenHash = source.tokenHash(); entity.expiresAt = source.expiresAt(); entity.revokedAt = source.revokedAt(); sessions.save(entity);
    }
    @Override public Optional<RefreshSession> findSessionByTokenHash(String tokenHash) { return sessions.findByTokenHash(tokenHash).map(this::map); }
    @Override public void revoke(String tokenHash) {
        RefreshSessionEntity entity = sessions.findByTokenHash(tokenHash).orElseThrow(); entity.revokedAt = clock.instant(); sessions.save(entity);
    }
    private UserAccount map(UserEntity e) {
        Set<Role> mappedRoles = e.roles.stream().map(r -> Role.valueOf(r.code)).collect(Collectors.toUnmodifiableSet());
        return new UserAccount(e.id.toString(), e.firstName, e.lastName, e.documentType, e.documentNumber, e.email, e.phone, e.passwordHash, e.active, mappedRoles);
    }
    private RefreshSession map(RefreshSessionEntity e) { return new RefreshSession(e.userId.toString(), e.tokenHash, e.expiresAt, e.revokedAt); }
}
