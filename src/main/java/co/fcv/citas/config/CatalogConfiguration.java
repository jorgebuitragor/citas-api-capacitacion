package co.fcv.citas.config;

import co.fcv.citas.application.catalog.CatalogPorts.CatalogRepository;
import co.fcv.citas.application.catalog.CatalogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfiguration {
    @Bean CatalogService catalogService(CatalogRepository repository) { return new CatalogService(repository); }
}
