package cz.muni.ics.kypo.training.rest.utils.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author Pavel Šeda
 *
 */
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

	private static final long serialVersionUID = 732450547359556911L;

	public LocalDateTimeDeserializer() {
		super(LocalDateTime.class);
	}

	@Override
	public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return LocalDateTime.parse(jp.readValueAs(String.class));
	}

}
