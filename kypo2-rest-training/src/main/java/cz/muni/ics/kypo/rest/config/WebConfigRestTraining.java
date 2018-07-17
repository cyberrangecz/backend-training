package cz.muni.ics.kypo.rest.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import cz.muni.ics.kypo.config.FacadeConfiguration;

/**
 * <p>
 * To run with external property file add following to:
 * 
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
 *  --spring.config.location=classpath:file:///etc/kypo2/training/application.properties
 * </code>
 * </pre>
 * 
 * @author Pavel Seda (441048)
 *
 */
@SpringBootApplication
@EnableSpringDataWebSupport
@Import({FacadeConfiguration.class, SwaggerConfig.class})
public class WebConfigRestTraining extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(WebConfigRestTraining.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(WebConfigRestTraining.class, args);
  }

  // REST settings

  /**
   * Provides localized messages.
   */
  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Bean
  @Primary
  public MappingJackson2HttpMessageConverter jacksonHTTPMessageConverter() {
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
    jsonConverter.setObjectMapper(objectMapperForRestAPI());
    return jsonConverter;
  }

  @Bean(name = "objMapperRESTApi")
  @Primary
  public ObjectMapper objectMapperForRestAPI() {
    ObjectMapper obj = new ObjectMapper();
    obj.setPropertyNamingStrategy(snakeCase());
    return obj;
  }

  /**
   * Naming strategy for returned JSONs.
   * 
   * @return Naming Strategy for JSON properties
   */
  @Bean(name = "properyNamingSnakeCase")
  public PropertyNamingStrategy snakeCase() {
    return PropertyNamingStrategy.SNAKE_CASE;
  }

}
