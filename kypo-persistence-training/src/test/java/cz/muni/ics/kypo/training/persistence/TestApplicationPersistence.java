package cz.muni.ics.kypo.training.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// SpringBootApplication inherit from SpringBootConfiguration which is searched by the entities and repository tests
@SpringBootApplication
@ComponentScan(basePackages = "cz.muni.ics.kypo.training.persistence.util")
@EntityScan(basePackages = "cz.muni.ics.kypo.training.persistence.model")
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.training.persistence.repository")
public class TestApplicationPersistence {
}
