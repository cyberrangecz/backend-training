package cz.muni.ics.kypo.training.persistence.config;

import cz.muni.ics.kypo.commons.security.config.ResourceServerSecurityConfig;
import cz.muni.ics.kypo.commons.startup.config.MicroserviceRegistrationConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.persistence.model", "cz.muni.ics.kypo.training.persistence.repository"})
@EntityScan(basePackages = "cz.muni.ics.kypo.training.persistence.model")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.training.persistence.repository")
@Import(value = {ResourceServerSecurityConfig.class, MicroserviceRegistrationConfiguration.class})
public class PersistenceConfig {
}
