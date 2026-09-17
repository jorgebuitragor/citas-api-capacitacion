package co.fcv.citas.config;

import co.fcv.citas.application.auth.AuthPorts.SessionRepository;
import co.fcv.citas.application.auth.AuthPorts.UserRepository;
import co.fcv.citas.application.auth.AuthService;
import co.fcv.citas.application.auth.TokenService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthConfiguration {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthService authService(UserRepository users, SessionRepository sessions, PasswordEncoder passwords, TokenService tokens, Clock clock) {
        return new AuthService(users, sessions, passwords, tokens, clock);
    }
}
