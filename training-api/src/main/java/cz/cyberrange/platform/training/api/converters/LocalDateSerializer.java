package cz.cyberrange.platform.training.api.converters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/** Serializes a {@link LocalDate} to its ISO-8601 date string representation */
public class LocalDateSerializer extends StdSerializer<LocalDate> {

  private static final long serialVersionUID = 3078523754669503927L;

  public LocalDateSerializer() {
    super(LocalDate.class);
  }

  @Override
  public void serialize(LocalDate value, JsonGenerator gen, SerializationContext sp) {
    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
  }
}
