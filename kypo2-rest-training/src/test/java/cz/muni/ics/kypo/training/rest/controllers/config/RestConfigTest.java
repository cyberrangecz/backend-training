package cz.muni.ics.kypo.training.rest.controllers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.ics.kypo.training.validation.EmailValidator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@Configuration
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.facade", "cz.muni.ics.kypo.training.mapping", "cz.muni.ics.kypo.training.service",
		"cz.muni.csirt.kypo.elasticsearch.service",	"cz.muni.csirt.kypo.elasticsearch.data"})
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.persistence.model", "cz.muni.ics.kypo.commons.persistence.model"},  basePackageClasses = Jsr310JpaConverters.class)
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.training.persistence.repository", "cz.muni.ics.kypo.commons"})
public class RestConfigTest {
	private static final Logger LOG = LoggerFactory.getLogger(RestConfigTest.class);


	@Bean
	public ModelMapper modelMapper() {
		LOG.debug("modelMapper()");
		return new ModelMapper();
	}

	@Bean
	public RestClientBuilder coreBuilder() throws Exception {
		RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
		builder.setMaxRetryTimeoutMillis(10000);
		return builder;
	}

	@Bean
	public RestHighLevelClient restHighLevelClient() throws Exception{
		RestHighLevelClient client = new RestHighLevelClient(coreBuilder());
		return client;
	}

	@Bean
	@Qualifier("userManagementExchangeFunction")
	public ExchangeFunction userManagementExchangeFunction(){
		return Mockito.mock(ExchangeFunction.class);
	}


	@Bean
	@Qualifier("sandboxManagementExchangeFunction")
	public ExchangeFunction sandboxManagementExchangeFunction(){
		return Mockito.mock(ExchangeFunction.class);
	}

	@Bean
	@Primary
	@Qualifier("userManagementServiceWebClient")
	public WebClient userManagementServiceWebClient(){
		return WebClient.builder()
				.exchangeFunction(userManagementExchangeFunction())
				.build();
	}

	@Bean
	@Qualifier("sandboxServiceWebClient")
	public WebClient sandboxServiceWebClient(){
		return WebClient.builder()
				.exchangeFunction(sandboxManagementExchangeFunction())
				.build();
	}

	@Bean
	@Primary
	@Qualifier("objMapperRESTApi")
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper;
	}

	@Bean
	@Primary
	public TrainingEventsService trainingEventsServiceMock(){
		return Mockito.mock(TrainingEventsService.class);
	}

	@Bean
	public EmailValidator usernameValidator() {
		LOG.debug("usernameValidator()");
		return new EmailValidator();
	}

	@Bean
	public HttpServletRequest httpServletRequest(){
		return new HttpServletRequestWrapper(new Request(new Connector()));
	}

}
