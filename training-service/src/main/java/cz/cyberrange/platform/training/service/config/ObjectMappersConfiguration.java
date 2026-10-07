package cz.cyberrange.platform.training.service.config;

import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import java.time.LocalDateTime;
import org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.json.JsonFactoryBuilder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Shapes the auto-configured JSON mapper the service uses wherever one is injected unqualified, and
 * supplies the YAML mapper that reads and writes the same structure as YAML
 */
@Configuration
public class ObjectMappersConfiguration {

  /**
   * Gives the auto-configured JSON factory the Jackson 2 stream defaults, so a character outside
   * the Basic Multilingual Plane is written as an escaped surrogate pair.
   *
   * @return the customizer applied to the auto-configured JSON factory
   */
  @Bean
  public JsonFactoryBuilderCustomizer jackson2DefaultsJsonFactoryCustomizer() {
    return JsonFactoryBuilder::configureForJackson2;
  }

  /**
   * Gives the auto-configured JSON mapper the application's mapping settings and indents its
   * output.
   *
   * @return the customizer applied to the auto-configured JSON mapper builder
   */
  @Bean
  public JsonMapperBuilderCustomizer snakeCaseJsonMapperCustomizer() {
    return builder -> applyMappingSettings(builder).enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * Supplies the YAML mapper, carrying the same mapping settings as the auto-configured JSON
   * mapper.
   *
   * @return the mapper YAML request and response bodies and YAML exports go through
   */
  @Bean
  public YAMLMapper yamlMapper() {
    return applyMappingSettings(YAMLMapper.builder()).build();
  }

  /**
   * Applies the Jackson 2 defaults, then ignores properties the target type does not declare when
   * reading, keeps properties in their declared order, names them in snake case, writes a date or
   * time in its textual form rather than as a number, and writes every {@link LocalDateTime} as a
   * UTC instant through {@link LocalDateTimeUTCSerializer}.
   *
   * @param builder the mapper builder to configure
   * @param <B> the concrete builder type
   * @return {@code builder}, configured
   */
  private static <B extends MapperBuilder<?, B>> B applyMappingSettings(B builder) {
    return builder
        .configureForJackson2()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .addModule(
            new SimpleModule("utc-local-date-time")
                .addSerializer(LocalDateTime.class, new LocalDateTimeUTCSerializer()));
  }
}
