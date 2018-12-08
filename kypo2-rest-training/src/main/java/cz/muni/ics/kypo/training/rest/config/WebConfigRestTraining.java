package cz.muni.ics.kypo.training.rest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cz.muni.ics.kypo.training.config.FacadeConfiguration;

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
@Import({FacadeConfiguration.class, SwaggerConfig.class})
public class WebConfigRestTraining extends SpringBootServletInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(WebConfigRestTraining.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebConfigRestTraining.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(WebConfigRestTraining.class, args);
    }

}
