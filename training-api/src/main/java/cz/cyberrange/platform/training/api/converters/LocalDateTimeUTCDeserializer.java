package cz.cyberrange.platform.training.api.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Reads a JSON string in ISO-8601 instant form with a trailing {@code Z} (for example {@code
 * 2018-11-30T10:26:02.727Z}) into a {@link LocalDateTime} holding that same instant's date and time
 * as observed in UTC. A JSON string that {@link java.time.Instant#parse} cannot parse in that form
 * throws a {@link java.time.format.DateTimeParseException}. A JSON null never reaches here and
 * yields a null field.
 */
public class LocalDateTimeUTCDeserializer extends StdDeserializer<LocalDateTime> {

  public LocalDateTimeUTCDeserializer() {
    super(LocalDateTime.class);
  }

  @Override
  public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    Instant instant = Instant.parse(jp.readValueAs(String.class));
    return LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
  }
}
