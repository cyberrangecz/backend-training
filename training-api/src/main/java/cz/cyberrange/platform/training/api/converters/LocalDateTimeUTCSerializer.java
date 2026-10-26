package cz.cyberrange.platform.training.api.converters;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Writes a {@link LocalDateTime} as a JSON string in ISO-8601 instant form with exactly three
 * fractional-second digits and a trailing {@code Z} (for example {@code 2019-11-20T10:28:02.727Z}),
 * treating the value's date and time fields as already being at offset UTC rather than converting
 * them from another zone. Digits beyond the millisecond are truncated and missing ones are written
 * as zeros. A null value never reaches here and is written as a JSON null.
 */
public class LocalDateTimeUTCSerializer extends StdSerializer<LocalDateTime> {

  private static final DateTimeFormatter UTC_MILLISECOND_INSTANT =
      new DateTimeFormatterBuilder().appendInstant(3).toFormatter();

  public LocalDateTimeUTCSerializer() {
    super(LocalDateTime.class);
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext provider) {
    gen.writeString(UTC_MILLISECOND_INSTANT.format(value.toInstant(ZoneOffset.UTC)));
  }
}
