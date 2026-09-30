package cz.cyberrange.platform.training.opensearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Configuration class for object mapper of OpenSearch logging */
@Configuration
@ComponentScan(
    basePackages = {"cz.cyberrange.platform.training.opensearch.events.training.logging"})
public class ObjectMapperConfigOpenSearch {

  /**
   * Supplies the mapper used for audit event JSON: field names are written in snake case, and a
   * date or time is written in its textual form rather than as a number.
   *
   * @return the mapper audit event writing and reading go through
   */
  @Bean("openSearchObjectMapper")
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }
}
