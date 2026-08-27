package cz.cyberrange.platform.training.persistence.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Brings this module's persistence layer into an application context: it turns on declarative
 * transactions, registers the entities and the Spring Data repositories, and makes both packages
 * available for component scanning.
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(
    basePackages = {
      "cz.cyberrange.platform.training.persistence.model",
      "cz.cyberrange.platform.training.persistence.repository"
    })
@EntityScan(basePackages = "cz.cyberrange.platform.training.persistence.model")
@EnableJpaRepositories(basePackages = "cz.cyberrange.platform.training.persistence.repository")
public class PersistenceConfig {}
