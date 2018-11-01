package cz.muni.ics.kypo.training.utils.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalTime;

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
	public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		return LocalTime.parse(jp.readValueAs(String.class));
	}
}
