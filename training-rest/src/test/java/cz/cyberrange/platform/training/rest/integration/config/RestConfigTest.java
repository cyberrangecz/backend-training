package cz.cyberrange.platform.training.rest.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.cyberrange.platform.training.api.validation.EmailValidator;
import cz.cyberrange.platform.training.service.services.api.ElasticsearchApiService;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@Configuration
@ComponentScan(basePackages = {
		"cz.cyberrange.platform.training.service.facade",
		"cz.cyberrange.platform.training.service.mapping",
		"cz.cyberrange.platform.training.service.services",
		"cz.cyberrange.platform.training.elasticsearch.service"
})
@EntityScan(basePackages = {"cz.cyberrange.platform.training.persistence.model", "cz.cyberrange.platform.commons.persistence.model"},  basePackageClasses = Jsr310JpaConverters.class)
@EnableJpaRepositories(basePackages = {"cz.cyberrange.platform.training.persistence.repository", "cz.cyberrange.platform.commons"})
public class RestConfigTest {
	private static final Logger LOG = LoggerFactory.getLogger(RestConfigTest.class);


	@Bean
	public ModelMapper modelMapper() {
		LOG.debug("modelMapper()");
		return new ModelMapper();
	}

	@Bean
	public RestClientBuilder coreBuilder() throws Exception {
		return RestClient.builder(new HttpHost("localhost", 9200, "http"));
	}

	@Bean
	public RestHighLevelClient restHighLevelClient() throws Exception{
		return new RestHighLevelClient(coreBuilder());
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
	@Qualifier("elasticsearchExchangeFunction")
	public ExchangeFunction elasticsearchExchangeFunction(){
		return Mockito.mock(ExchangeFunction.class);
	}

	@Bean
	@Qualifier("feedbackExchangeFunction")
	public ExchangeFunction feedbackExchangeFunction(){
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
	@Qualifier("elasticsearchServiceWebClient")
	public WebClient elasticsearchServiceWebClient(){
		return WebClient.builder()
				.exchangeFunction(elasticsearchExchangeFunction())
				.build();
	}

	@Bean
	@Qualifier("feedbackServiceWebClient")
	public WebClient feedbackServiceWebClient(){
		return WebClient.builder()
				.exchangeFunction(elasticsearchExchangeFunction())
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
		mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
		return mapper;
	}

	@Bean
	@Primary
	public ElasticsearchApiService elasticsearchApiServiceMock(){
		return Mockito.mock(ElasticsearchApiService.class);
	}

	@Bean
	public EmailValidator usernameValidator() {
		LOG.debug("usernameValidator()");
		return new EmailValidator();
	}

	@Bean
	@Primary
	public LocalValidatorFactoryBean getValidator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public HttpServletRequest httpServletRequest(){
		return new HttpServletRequestWrapper(new Request(new Connector()));
	}

}
