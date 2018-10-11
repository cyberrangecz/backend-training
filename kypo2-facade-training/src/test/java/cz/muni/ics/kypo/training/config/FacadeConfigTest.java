package cz.muni.ics.kypo.training.config;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Configuration
@EntityScan(basePackages = "cz.muni.ics.kypo.training.persistence.model", basePackageClasses = Jsr310JpaConverters.class)
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.training.persistence.repository")
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.facade", "cz.muni.ics.kypo.training.mapping", "cz.muni.ics.kypo.training.service",
		"cz.muni.csirt.kypo.elasticsearch"})
public class FacadeConfigTest {

	private static final Logger LOG = LoggerFactory.getLogger(FacadeConfigTest.class);

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
