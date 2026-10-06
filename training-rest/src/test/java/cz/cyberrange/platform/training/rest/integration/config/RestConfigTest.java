package cz.cyberrange.platform.training.rest.integration.config;

import cz.cyberrange.platform.training.api.validation.EmailValidator;
import cz.cyberrange.platform.training.service.config.ObjectMappersConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.hc.core5.http.HttpHost;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.opensearch.client.json.jackson3.JacksonJsonpMapper;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@Import(ObjectMappersConfiguration.class)
@ComponentScan(
    basePackages = {
      "cz.cyberrange.platform.training.service.export",
      "cz.cyberrange.platform.training.service.facade",
      "cz.cyberrange.platform.training.service.mapping",
      "cz.cyberrange.platform.training.service.services",
    })
@EntityScan(
    basePackages = {
      "cz.cyberrange.platform.training.persistence.model",
      "cz.cyberrange.platform.commons.persistence.model"
    },
    basePackageClasses = Jsr310JpaConverters.class)
@EnableJpaRepositories(
    basePackages = {
      "cz.cyberrange.platform.training.persistence.repository",
      "cz.cyberrange.platform.commons"
    })
public class RestConfigTest {
  private static final Logger LOG = LoggerFactory.getLogger(RestConfigTest.class);

  @Bean
  public ModelMapper modelMapper() {
    LOG.debug("modelMapper()");
    return new ModelMapper();
  }

  @Bean("openSearchTransport")
  public OpenSearchTransport openSearchTransport() {
    return ApacheHttpClient5TransportBuilder.builder(new HttpHost("http", "localhost", 9200))
        .setMapper(new JacksonJsonpMapper(objectMapper()))
        .build();
  }

  @Bean
  @Qualifier("userManagementExchangeFunction")
  public ExchangeFunction userManagementExchangeFunction() {
    return Mockito.mock(ExchangeFunction.class);
  }

  @Bean
  @Qualifier("sandboxManagementExchangeFunction")
  public ExchangeFunction sandboxManagementExchangeFunction() {
    return Mockito.mock(ExchangeFunction.class);
  }

  @Bean
  @Qualifier("opensearchExchangeFunction")
  public ExchangeFunction opensearchExchangeFunction() {
    return Mockito.mock(ExchangeFunction.class);
  }

  @Bean
  @Qualifier("feedbackExchangeFunction")
  public ExchangeFunction feedbackExchangeFunction() {
    return Mockito.mock(ExchangeFunction.class);
  }

  @Bean
  @Primary
  @Qualifier("userManagementServiceWebClient")
  public WebClient userManagementServiceWebClient() {
    return WebClient.builder().exchangeFunction(userManagementExchangeFunction()).build();
  }

  @Bean
  @Qualifier("sandboxServiceWebClient")
  public WebClient sandboxServiceWebClient() {
    return WebClient.builder().exchangeFunction(sandboxManagementExchangeFunction()).build();
  }

  @Bean
  @Qualifier("opensearchServiceWebClient")
  public WebClient opensearchServiceWebClient() {
    return WebClient.builder().exchangeFunction(opensearchExchangeFunction()).build();
  }

  @Bean
  @Qualifier("feedbackServiceWebClient")
  public WebClient feedbackServiceWebClient() {
    return WebClient.builder().exchangeFunction(opensearchExchangeFunction()).build();
  }

  @Bean
  @Primary
  @Qualifier("objMapperRESTApi")
  public JsonMapper objectMapper() {
    return JsonMapper.builderWithJackson2Defaults()
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();
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
  public HttpServletRequest httpServletRequest() {
    return new HttpServletRequestWrapper(
        new Request(new Connector(), new org.apache.coyote.Request()));
  }
}
