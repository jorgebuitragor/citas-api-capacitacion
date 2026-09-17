package co.fcv.citas.application.auth;

import co.fcv.citas.application.auth.AuthCommands.LoginCommand;
import co.fcv.citas.application.auth.AuthCommands.RegisterCommand;
import co.fcv.citas.application.auth.AuthCommands.RegisteredUser;
import co.fcv.citas.application.auth.AuthCommands.TokenPair;
import co.fcv.citas.application.auth.AuthPorts.SessionRepository;
import co.fcv.citas.application.auth.AuthPorts.UserRepository;
import co.fcv.citas.domain.auth.RefreshSession;
import co.fcv.citas.domain.auth.Role;
import co.fcv.citas.domain.auth.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class AuthService {
    private final UserRepository users;
    private final SessionRepository sessions;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final Clock clock;

    public AuthService(UserRepository users, SessionRepository sessions, PasswordEncoder passwords, TokenService tokens, Clock clock) {
        this.users = users; this.sessions = sessions; this.passwords = passwords; this.tokens = tokens; this.clock = clock;
    }

    @Transactional
    public RegisteredUser register(RegisterCommand command) {
        String email = normalizeEmail(command.email());
        String documentType = normalized(command.documentType()).toUpperCase(Locale.ROOT);
        String documentNumber = normalized(command.documentNumber());
        if (users.existsByEmail(email)) throw new AuthException(AuthException.Reason.EMAIL_EXISTS);
        if (users.existsByDocument(documentType, documentNumber)) throw new AuthException(AuthException.Reason.DOCUMENT_EXISTS);
        UserAccount saved = users.save(new UserAccount(UUID.randomUUID().toString(), normalized(command.firstName()),
                normalized(command.lastName()), documentType, documentNumber, email, normalized(command.phone()),
                passwords.encode(command.password()), true, Set.of(Role.USER)));
        return new RegisteredUser(saved.id(), saved.email(), saved.roles().stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    @Transactional
    public TokenPair login(LoginCommand command) {
        UserAccount user = users.findByEmail(normalizeEmail(command.email()))
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_CREDENTIALS));
        if (!user.active()) throw new AuthException(AuthException.Reason.INVALID_CREDENTIALS);
        if (!passwords.matches(command.password(), user.passwordHash())) throw new AuthException(AuthException.Reason.INVALID_CREDENTIALS);
        return issueAndPersist(user);
    }

    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        TokenService.ParsedRefresh parsed;
        try { parsed = tokens.parseRefresh(rawRefreshToken); }
        catch (RuntimeException ex) { throw new AuthException(AuthException.Reason.INVALID_REFRESH); }
        Instant now = clock.instant();
        RefreshSession old = sessions.findSessionById(parsed.sessionId())
                .filter(s -> s.userId().equals(parsed.userId()) && s.isActiveAt(now) && constantTimeEquals(s.tokenHash(), sha256(rawRefreshToken)))
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_REFRESH));
        UserAccount user = users.findById(parsed.userId()).filter(UserAccount::active)
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_REFRESH));
        TokenPair next = tokens.issue(user.id(), user.roles());
        String newSessionId = sessionId(next.refreshToken());
        sessions.revoke(old.id(), newSessionId);
        sessions.save(new RefreshSession(newSessionId, user.id(), sha256(next.refreshToken()), now,
                next.refreshExpiresAt(), null, null));
        return next;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        TokenService.ParsedRefresh parsed;
        try { parsed = tokens.parseRefresh(rawRefreshToken); }
        catch (RuntimeException ex) { throw new AuthException(AuthException.Reason.INVALID_REFRESH); }
        RefreshSession session = sessions.findSessionById(parsed.sessionId())
                .filter(s -> s.userId().equals(parsed.userId()) && s.isActiveAt(clock.instant()) && constantTimeEquals(s.tokenHash(), sha256(rawRefreshToken)))
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_REFRESH));
        sessions.revoke(session.id(), null);
    }

    private TokenPair issueAndPersist(UserAccount user) {
        TokenPair pair = tokens.issue(user.id(), user.roles());
        TokenService.ParsedRefresh parsed = tokens.parseRefresh(pair.refreshToken());
        sessions.save(new RefreshSession(parsed.sessionId(), user.id(), sha256(pair.refreshToken()), clock.instant(),
                pair.refreshExpiresAt(), null, null));
        return pair;
    }
    private String sessionId(String refresh) { return tokens.parseRefresh(refresh).sessionId(); }
    private static String normalizeEmail(String value) { return normalized(value).toLowerCase(Locale.ROOT); }
    private static String normalized(String value) { return value.trim(); }
    private static String sha256(String value) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private static boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
