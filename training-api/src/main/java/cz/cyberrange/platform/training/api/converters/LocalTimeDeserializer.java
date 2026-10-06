package cz.cyberrange.platform.training.api.converters;

import java.time.LocalTime;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializes a JSON string into a {@link LocalTime} using {@link LocalTime#parse(CharSequence)}
 */
public class LocalTimeDeserializer extends StdDeserializer<LocalTime> {

  public LocalTimeDeserializer() {
    super(LocalTime.class);
  }

  @Override
  public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt) {
    return LocalTime.parse(jp.readValueAs(String.class));
  }
}
