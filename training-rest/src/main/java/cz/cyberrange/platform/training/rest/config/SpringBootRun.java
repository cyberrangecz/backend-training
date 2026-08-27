package cz.cyberrange.platform.training.rest.config;

import cz.cyberrange.platform.training.service.config.FacadeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Spring Boot entry point for the training-rest module. Component scanning is limited to this
 * module's own package, so {@link FacadeConfiguration} from the training-service module is brought
 * into the context explicitly; Spring Data web support resolves controller method parameters of
 * type {@code Pageable} from request query parameters.
 */
@SpringBootApplication(scanBasePackages = "cz.cyberrange.platform.training.rest")
@EnableSpringDataWebSupport
@Import({WebConfigRestTraining.class, FacadeConfiguration.class})
public class SpringBootRun extends SpringBootServletInitializer {

  /**
   * Registers this class as the application's configuration source when deployed as a WAR under an
   * external servlet container.
   */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(SpringBootRun.class);
  }

  /** Starts the application with an embedded servlet container. */
  public static void main(String[] args) {
    SpringApplication.run(SpringBootRun.class, args);
  }
}
