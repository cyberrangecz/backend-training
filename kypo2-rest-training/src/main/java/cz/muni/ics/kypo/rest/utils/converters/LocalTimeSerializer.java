package cz.muni.ics.kypo.rest.utils.converters;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author Pavel Šeda
 *
 */
public class LocalTimeSerializer extends StdSerializer<LocalTime> {

  private static final long serialVersionUID = -4665110529123750815L;

  public LocalTimeSerializer() {
    super(LocalTime.class);
  }

  @Override
  public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_TIME));
  }
}
