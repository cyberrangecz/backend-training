package cz.cyberrange.platform.training.api.converters;

import java.time.LocalDateTime;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializes a JSON string into a {@link LocalDateTime} using {@link
 * LocalDateTime#parse(CharSequence)}
 */
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

  private static final long serialVersionUID = 732450547359556911L;

  public LocalDateTimeDeserializer() {
    super(LocalDateTime.class);
  }

  @Override
  public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) {
    return LocalDateTime.parse(jp.readValueAs(String.class));
  }
}
