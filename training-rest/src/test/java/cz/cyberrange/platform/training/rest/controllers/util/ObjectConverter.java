package cz.cyberrange.platform.training.rest.controllers.util;

import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCDeserializer;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.service.mapping.modelmapper.BeanMapping;
import cz.cyberrange.platform.training.service.mapping.modelmapper.BeanMappingImpl;
import java.io.IOException;
import java.time.LocalDateTime;
import org.modelmapper.ModelMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

public class ObjectConverter {

  private static BeanMapping beanMapping = new BeanMappingImpl(new ModelMapper());

  public static String convertObjectToJsonBytes(Object object) throws IOException {
    return restResponseMapper().writeValueAsString(object);
  }

  /**
   * Builds a mapper writing JSON the way the application writes REST responses, in snake case with
   * every date-time as a UTC instant, without indentation.
   *
   * @return the mapper
   */
  public static JsonMapper restResponseMapper() {
    return snakeCaseMapperBuilder()
        .addModule(new SimpleModule().addSerializer(new LocalDateTimeUTCSerializer()))
        .build();
  }

  public static String convertJsonBytesToObject(String object) throws IOException {
    return snakeCaseMapperBuilder().build().readValue(object, String.class);
  }

  public static <T> T convertJsonBytesToObject(String object, TypeReference<T> tTypeReference)
      throws IOException {
    ObjectMapper mapper =
        snakeCaseMapperBuilder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    return mapper.readValue(object, tTypeReference);
  }

  public static <T> T convertJsonBytesToObject(String object, Class<T> objectClass)
      throws IOException {
    ObjectMapper mapper =
        snakeCaseMapperBuilder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addModule(
                new SimpleModule()
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeUTCDeserializer()))
            .build();
    return mapper.readValue(object, objectClass);
  }

  private static JsonMapper.Builder snakeCaseMapperBuilder() {
    return JsonMapper.builderWithJackson2Defaults()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
  }

  public static String getInitialExceptionMessage(Exception exception) {
    while (exception.getCause() != null) {
      exception = (Exception) exception.getCause();
    }
    return exception.getMessage();
  }
}
