package cz.muni.ics.kypo.training.config;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FacadeTestConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(FacadeConfiguration.class);

    @Bean
    public ModelMapper modelMapper() {
        LOG.debug("modelMapper()");
        return new ModelMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}