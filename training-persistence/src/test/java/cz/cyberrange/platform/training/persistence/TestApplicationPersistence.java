package cz.cyberrange.platform.training.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.postgresql.PostgreSQLContainer;

// SpringBootApplication inherit from SpringBootConfiguration which is searched by the entities and
// repository tests
@SpringBootApplication
@ComponentScan(basePackages = "cz.cyberrange.platform.training.persistence.util")
@EntityScan(basePackages = "cz.cyberrange.platform.training.persistence.model")
@EnableJpaRepositories(basePackages = "cz.cyberrange.platform.training.persistence.repository")
public class TestApplicationPersistence {

  /**
   * Starts the PostgreSQL container whose connection details replace the configured datasource; it
   * lives as long as the cached application context.
   *
   * @return the container the test datasource connects to
   */
  @Bean
  @ServiceConnection
  PostgreSQLContainer postgreSQLContainer() {
    return new FastPostgreSQLContainer();
  }
}
