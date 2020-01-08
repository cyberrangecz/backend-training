package cz.muni.ics.kypo.training.persistence.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.persistence.model", "cz.muni.ics.kypo.training.persistence.repository"})
@EntityScan(basePackages = "cz.muni.ics.kypo.training.persistence.model")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.training.persistence.repository")
public class PersistenceConfigTest {

}
