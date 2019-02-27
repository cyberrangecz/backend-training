package cz.muni.ics.kypo.training.rest.config;

import cz.muni.ics.kypo.commons.rest.config.SwaggerConfig;
import cz.muni.ics.kypo.commons.rest.config.WebConfigRestSecurityCommons;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import cz.muni.ics.kypo.training.config.FacadeConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

/**
 * <p>
 * To run with external property file add following to:
 * <p>
 * Eclipse example:
 *
 * <pre>
 * <code>
 *  Run Configuration -> tab: Arguments -> Program arguments
 * </code>
 * </pre>
 * </p>
 *
 * <pre>
 * <code>
 *  --path.to.config.file="C:/CSIRT/property-files/training.properties"
 * </code>
 * </pre>
 * <p>
 * Intellij idea example:
 *
 * <pre>
 *  <code>
 *   Run Configuration -> tab: Arguments -> Program arguments
 *  </code>
 * </pre>
 * </p>
 *
 * <pre>
 *  <code>
 *   --path.to.config.file="/etc/kypo2/training/application.properties"
 *  </code>
 * </pre>
 *
 * @author Pavel Seda (441048)
 */
@SpringBootApplication(scanBasePackages = "cz.muni.ics.kypo.training.rest")
@EnableSpringDataWebSupport
@Import({WebConfigRestTraining.class, FacadeConfiguration.class, SwaggerConfig.class, WebConfigRestSecurityCommons.class})
public class SpringBootRun extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpringBootRun.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootRun.class, args);
    }

}
