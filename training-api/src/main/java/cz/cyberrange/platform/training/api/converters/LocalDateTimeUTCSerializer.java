package cz.cyberrange.platform.training.api.converters;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Writes a {@link LocalDateTime} as a JSON string in ISO-8601 instant form with a trailing {@code
 * Z}, treating the value's date and time fields as already being at offset UTC rather than
 * converting them from another zone. A null value never reaches here and is written as a JSON null.
 */
public class LocalDateTimeUTCSerializer extends StdSerializer<LocalDateTime> {

  public LocalDateTimeUTCSerializer() {
    super(LocalDateTime.class);
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext provider) {
    gen.writeString(value.toInstant(ZoneOffset.UTC).toString());
  }
}
