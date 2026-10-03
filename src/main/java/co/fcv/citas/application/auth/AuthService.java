package co.fcv.citas.application.auth;

import co.fcv.citas.application.auth.AuthCommands.LoginCommand;
import co.fcv.citas.application.auth.AuthCommands.PasswordResetConfirmCommand;
import co.fcv.citas.application.auth.AuthCommands.PasswordResetRequestCommand;
import co.fcv.citas.application.auth.AuthCommands.RegisterCommand;
import co.fcv.citas.application.auth.AuthCommands.RegisteredUser;
import co.fcv.citas.application.auth.AuthCommands.TokenPair;
import co.fcv.citas.application.auth.AuthPorts.PasswordResetRepository;
import co.fcv.citas.application.auth.AuthPorts.SessionRepository;
import co.fcv.citas.application.auth.AuthPorts.UserRepository;
import co.fcv.citas.domain.auth.PasswordResetToken;
import co.fcv.citas.domain.auth.RefreshSession;
import co.fcv.citas.domain.auth.Role;
import co.fcv.citas.domain.auth.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class AuthService {
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository users;
    private final SessionRepository sessions;
    private final PasswordResetRepository passwordResets;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final Clock clock;
    private final Duration passwordResetTtl;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, SessionRepository sessions, PasswordResetRepository passwordResets,
                       PasswordEncoder passwords, TokenService tokens, Clock clock, Duration passwordResetTtl) {
        this.users = users; this.sessions = sessions; this.passwordResets = passwordResets; this.passwords = passwords;
        this.tokens = tokens; this.clock = clock; this.passwordResetTtl = passwordResetTtl;
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
        String tokenHash = sha256(rawRefreshToken);
        RefreshSession old = sessions.findSessionByTokenHash(tokenHash)
                .filter(s -> s.userId().equals(parsed.userId()) && s.isActiveAt(now) && constantTimeEquals(s.tokenHash(), tokenHash))
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_REFRESH));
        UserAccount user = users.findById(parsed.userId()).filter(UserAccount::active)
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_REFRESH));
        TokenPair next = tokens.issue(user.id(), user.roles());
        sessions.revoke(old.tokenHash());
        sessions.save(new RefreshSession(user.id(), sha256(next.refreshToken()), next.refreshExpiresAt(), null));
        return next;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        TokenService.ParsedRefresh parsed;
        try { parsed = tokens.parseRefresh(rawRefreshToken); }
        catch (RuntimeException ex) { throw new AuthException(AuthException.Reason.INVALID_REFRESH); }
        String tokenHash = sha256(rawRefreshToken);
        RefreshSession session = sessions.findSessionByTokenHash(tokenHash)
                .filter(s -> s.userId().equals(parsed.userId()) && s.isActiveAt(clock.instant()) && constantTimeEquals(s.tokenHash(), tokenHash))
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_REFRESH));
        sessions.revoke(session.tokenHash());
    }

    /**
     * HU-004 (DEC-008). Responde igual exista o no el email: nunca revela qué cuentas están registradas.
     * El token en texto plano solo se escribe en el log del servidor (canal de desarrollo autorizado por RF-03),
     * nunca en la respuesta HTTP.
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequestCommand command) {
        UserAccount user = users.findByEmail(normalizeEmail(command.email())).filter(UserAccount::active).orElse(null);
        if (user == null) return;
        String rawToken = randomToken();
        Instant expiresAt = clock.instant().plus(passwordResetTtl);
        passwordResets.save(new PasswordResetToken(user.id(), sha256(rawToken), expiresAt, null));
        LOG.info("[DEV] Password reset token for user {}: {} (expires {})", user.id(), rawToken, expiresAt);
    }

    /** HU-004 (DEC-008). Token vencido, consumido o desconocido responde igual, sin distinguir el motivo. */
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmCommand command) {
        String tokenHash = sha256(command.token());
        PasswordResetToken token = passwordResets.findByTokenHash(tokenHash)
                .filter(t -> t.isActiveAt(clock.instant()) && constantTimeEquals(t.tokenHash(), tokenHash))
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_RESET_TOKEN));
        UserAccount user = users.findById(token.userId()).filter(UserAccount::active)
                .orElseThrow(() -> new AuthException(AuthException.Reason.INVALID_RESET_TOKEN));
        users.updatePassword(user.id(), passwords.encode(command.newPassword()));
        passwordResets.consume(tokenHash, clock.instant());
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private TokenPair issueAndPersist(UserAccount user) {
        TokenPair pair = tokens.issue(user.id(), user.roles());
        sessions.save(new RefreshSession(user.id(), sha256(pair.refreshToken()), pair.refreshExpiresAt(), null));
        return pair;
    }
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
