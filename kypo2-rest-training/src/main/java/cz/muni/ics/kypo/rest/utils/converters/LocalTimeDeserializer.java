package cz.muni.ics.kypo.rest.utils.converters;

import java.io.IOException;
import java.time.LocalTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Pavel Šeda
 *
 */
public class LocalTimeDeserializer extends StdDeserializer<LocalTime> {

  private static final long serialVersionUID = -7109214569997590716L;

  public LocalTimeDeserializer() {
    super(LocalTime.class);
  }

  @Override
  public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    return LocalTime.parse(jp.readValueAs(String.class));
  }
}
