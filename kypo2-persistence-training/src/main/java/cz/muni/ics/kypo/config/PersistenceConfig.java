package cz.muni.ics.kypo.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"cz.muni.ics.kypo"})
@EntityScan(basePackages = "cz.muni.ics.kypo.model")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.repository")
public class PersistenceConfig {

}
