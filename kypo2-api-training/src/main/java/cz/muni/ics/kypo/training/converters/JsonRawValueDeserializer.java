package cz.muni.ics.kypo.training.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * @author Pavel Šeda
 */
public class JsonRawValueDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        return jp.readValueAsTree().toString();
    }
}
