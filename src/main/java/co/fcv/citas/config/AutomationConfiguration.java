package co.fcv.citas.config;

import co.fcv.citas.application.automation.AutomationPorts.AutomationRepository;
import co.fcv.citas.application.automation.AutomationService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutomationConfiguration {
    @Bean AutomationService automationService(AutomationRepository repository, Clock clock) { return new AutomationService(repository, clock); }
}
