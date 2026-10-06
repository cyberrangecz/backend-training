package cz.cyberrange.platform.training.api.converters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/** Serializes a {@link LocalDateTime} to its ISO-8601 date-time string representation */
public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

  private static final long serialVersionUID = 4981746330658018061L;

  public LocalDateTimeSerializer() {
    super(LocalDateTime.class);
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext sp) {
    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }
}
