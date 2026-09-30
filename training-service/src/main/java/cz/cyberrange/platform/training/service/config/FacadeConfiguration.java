package cz.cyberrange.platform.training.service.config;

import cz.cyberrange.platform.training.api.validation.EmailValidator;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Assembles the facade layer: it registers the facades and the mappers, turns on declarative
 * transactions, and pulls in the service and validation configurations the facades depend on
 */
@Configuration
@EnableTransactionManagement
@Import({ServiceConfig.class, ValidationMessagesConfig.class})
@ComponentScan(
    basePackages = {
      "cz.cyberrange.platform.training.service.facade",
      "cz.cyberrange.platform.training.service.mapping"
    })
public class FacadeConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(FacadeConfiguration.class);

  /**
   * Supplies a mapper carrying its library's default conventions, with nothing configured on it
   * here.
   *
   * @return the model mapper
   */
  @Bean
  public ModelMapper modelMapper() {
    LOG.debug("modelMapper()");
    return new ModelMapper();
  }

  /**
   * Supplies a standalone email validator bean. Nothing injects it, and the constraint it backs is
   * placed on no field.
   *
   * @return the email validator
   */
  @Bean
  public EmailValidator usernameValidator() {
    LOG.debug("usernameValidator()");
    return new EmailValidator();
  }
}
