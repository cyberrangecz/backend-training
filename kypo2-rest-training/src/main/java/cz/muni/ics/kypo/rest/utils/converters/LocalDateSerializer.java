package cz.muni.ics.kypo.rest.utils.converters;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author Pavel Šeda
 *
 */
public class LocalDateSerializer extends StdSerializer<LocalDate> {

  private static final long serialVersionUID = 3078523754669503927L;

  public LocalDateSerializer() {
    super(LocalDate.class);
  }

  @Override
  public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider sp) throws IOException, JsonProcessingException {
    gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
  }
}
