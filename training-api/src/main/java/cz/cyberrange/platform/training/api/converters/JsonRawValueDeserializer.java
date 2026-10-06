package cz.cyberrange.platform.training.api.converters;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Deserializes a JSON value of any shape into its literal JSON text */
public class JsonRawValueDeserializer extends ValueDeserializer<String> {

  @Override
  public String deserialize(JsonParser jp, DeserializationContext context) {
    return jp.readValueAsTree().toString();
  }
}
