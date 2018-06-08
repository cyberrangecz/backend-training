package cz.muni.ics.kypo.rest.config;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import cz.muni.ics.kypo.config.FacadeConfiguration;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@SpringBootApplication
@Import({FacadeConfiguration.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.rest"})
public class WebConfigRestTraining extends SpringBootServletInitializer {

  @Bean
  public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
    return factory -> factory.setContextPath("/kypo2-rest-training/api/v1");
  }

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
    obj.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH));
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
