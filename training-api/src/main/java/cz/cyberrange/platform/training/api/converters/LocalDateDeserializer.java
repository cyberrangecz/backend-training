package cz.cyberrange.platform.training.api.converters;

import java.time.LocalDate;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializes a JSON string into a {@link LocalDate} using {@link LocalDate#parse(CharSequence)}
 */
public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

  private static final long serialVersionUID = 8559445466757321763L;

  protected LocalDateDeserializer() {
    super(LocalDate.class);
  }

  @Override
  public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) {
    return LocalDate.parse(jp.readValueAs(String.class));
  }
}
