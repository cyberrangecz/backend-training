package cz.cyberrange.platform.training.api.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeString(value.toInstant(ZoneOffset.UTC).toString());
  }
}
