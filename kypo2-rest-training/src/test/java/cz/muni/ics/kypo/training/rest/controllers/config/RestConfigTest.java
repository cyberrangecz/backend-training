package cz.muni.ics.kypo.training.rest.controllers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.commons.rest.config.WebConfigRestSecurityCommons;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = {  "cz.muni.ics.kypo.commons.persistence.repository", "cz.muni.ics.kypo.training.facade", "cz.muni.ics.kypo.training.api", "cz.muni.ics.kypo.training.mapping", "cz.muni.ics.kypo.training.service",
		"cz.muni.csirt.kypo.elasticsearch","cz.muni.ics.kypo.training.persistence.model", "cz.muni.ics.kypo.training.persistence.repository"})
@EntityScan(basePackages = "cz.muni.ics.kypo.training.persistence.model",  basePackageClasses = Jsr310JpaConverters.class)
@EnableJpaRepositories(basePackages = "cz.muni.ics.kypo.training.persistence.repository")
public class RestConfigTest {
	private static final Logger LOG = LoggerFactory.getLogger(RestConfigTest.class);

	@Bean
	public ModelMapper modelMapper() {
		LOG.debug("modelMapper()");
		return new ModelMapper();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Qualifier("objMapperRESTApi")
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
