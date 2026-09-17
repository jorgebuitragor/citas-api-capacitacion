package co.fcv.citas.application.auth;

import java.time.Instant;
import java.util.Set;

public final class AuthCommands {
    private AuthCommands() { }
    public record RegisterCommand(String firstName, String lastName, String documentType,
                                  String documentNumber, String email, String phone, String password) { }
    public record LoginCommand(String email, String password) { }
    public record TokenPair(String accessToken, String refreshToken, Instant accessExpiresAt, Instant refreshExpiresAt) { }
    public record RegisteredUser(String id, String email, Set<String> roles) { }
}
