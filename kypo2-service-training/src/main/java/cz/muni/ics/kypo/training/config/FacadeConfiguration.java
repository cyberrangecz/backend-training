package cz.muni.ics.kypo.training.config;

import cz.muni.ics.kypo.training.validation.EmailValidator;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The type Facade configuration.
 */
@Configuration
@EnableTransactionManagement
@Import({ServiceConfig.class, ValidationMessagesConfig.class})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.facade", "cz.muni.ics.kypo.training.mapping"})
public class FacadeConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(FacadeConfiguration.class);

    /**
     * Model mapper model mapper.
     *
     * @return the model mapper
     */
    @Bean
    public ModelMapper modelMapper() {
        LOG.debug("modelMapper()");
        return new ModelMapper();
    }

    /**
     * Username validator email validator.
     *
     * @return the email validator
     */
    @Bean
    public EmailValidator usernameValidator() {
        LOG.debug("usernameValidator()");
        return new EmailValidator();
    }

}
