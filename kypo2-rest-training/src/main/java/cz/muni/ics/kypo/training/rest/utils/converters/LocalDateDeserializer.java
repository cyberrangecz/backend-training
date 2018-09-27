package cz.muni.ics.kypo.training.rest.utils.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @author Pavel Šeda
 *
 */
public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

	private static final long serialVersionUID = 8559445466757321763L;

	protected LocalDateDeserializer() {
		super(LocalDate.class);
	}

	@Override
	public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return LocalDate.parse(jp.readValueAs(String.class));
	}

}
