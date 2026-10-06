package cz.cyberrange.platform.training.api.converters;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/** Serializes a {@link LocalTime} to its ISO-8601 time string representation */
public class LocalTimeSerializer extends StdSerializer<LocalTime> {

  private static final long serialVersionUID = -4665110529123750815L;

  public LocalTimeSerializer() {
    super(LocalTime.class);
  }

  @Override
  public void serialize(LocalTime value, JsonGenerator gen, SerializationContext sp) {
    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_TIME));
  }
}
