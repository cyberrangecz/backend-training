package cz.cyberrange.platform.training.opensearch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/** Configuration class for object mapper of OpenSearch logging */
@Configuration
@ComponentScan(
    basePackages = {"cz.cyberrange.platform.training.opensearch.events.training.logging"})
public class ObjectMapperConfigOpenSearch {

  /**
   * Supplies the mapper used for audit event JSON: it keeps the Jackson 2 defaults, fields keep
   * their declared order and are written in snake case, and a date or time is written in its
   * textual form rather than as a number.
   *
   * @return the mapper audit event writing and reading go through
   */
  @Bean("openSearchObjectMapper")
  public ObjectMapper objectMapper() {
    return JsonMapper.builderWithJackson2Defaults()
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();
  }
}
