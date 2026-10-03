package co.fcv.citas.config;

import co.fcv.citas.application.auth.AuthPorts.PasswordResetRepository;
import co.fcv.citas.application.auth.AuthPorts.SessionRepository;
import co.fcv.citas.application.auth.AuthPorts.UserRepository;
import co.fcv.citas.application.auth.AuthService;
import co.fcv.citas.application.auth.TokenService;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthConfiguration {
    @Bean Clock clock() { return Clock.system(ZoneId.of("America/Bogota")); }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthService authService(UserRepository users, SessionRepository sessions, PasswordResetRepository passwordResets,
                                  PasswordEncoder passwords, TokenService tokens, Clock clock,
                                  @Value("${app.security.password-reset-ttl}") Duration passwordResetTtl) {
        return new AuthService(users, sessions, passwordResets, passwords, tokens, clock, passwordResetTtl);
    }
}
