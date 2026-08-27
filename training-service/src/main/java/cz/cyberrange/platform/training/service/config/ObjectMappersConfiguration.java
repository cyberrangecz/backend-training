package cz.cyberrange.platform.training.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Supplies the JSON mapper the service uses by default wherever one is injected unqualified. */
@Configuration
public class ObjectMappersConfiguration {

  /**
   * Builds the service's default JSON mapper: property names on the wire are snake case, a date or
   * time is written in its textual form rather than as a number, and output is indented. Being the
   * primary mapper, it is the one injected wherever no qualifier names another.
   *
   * @return the mapper JSON reading and writing goes through by default
   */
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    return objectMapper;
  }
}
