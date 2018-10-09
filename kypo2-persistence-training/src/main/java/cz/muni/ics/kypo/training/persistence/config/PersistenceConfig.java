package cz.muni.ics.kypo.training.persistence.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.persistence.model", "cz.muni.ics.kypo.training.persistence.repository"})
@EntityScan(basePackages = "cz.muni.ics.kypo.training.persistence.model", basePackageClasses = Jsr310JpaConverters.class)
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.training.persistence.repository")
@PropertySource("file:${path.to.config.file}")
public class PersistenceConfig {

	private static final Logger LOG = LoggerFactory.getLogger(PersistenceConfig.class);

}
