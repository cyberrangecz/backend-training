package cz.muni.ics.kypo.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cz.muni.ics.kypo.validation.EmailValidator;

/**
 * @author Pavel Šeda
 *
 */
@Configuration
@EnableTransactionManagement
@Import(ServiceConfig.class)
@ComponentScan(basePackages = {"cz.muni.ics.kypo.facade", "cz.muni.ics.kypo.mapping"})
public class FacadeConfiguration {

  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  @Bean
  public EmailValidator usernameValidator() {
    return new EmailValidator();
  }

}
