package cz.muni.ics.kypo.training.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Configuration
@EntityScan(basePackages = "cz.muni.ics.kypo.training.persistence.model")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.training.persistence.repository")
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service", "cz.muni.csirt.kypo.elasticsearch"})
public class ServiceTrainingConfigTest {

}
