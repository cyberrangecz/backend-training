package cz.muni.ics.kypo.training.rest.controllers.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.converters.LocalDateTimeDeserializer;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCDeserializer;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMappingImpl;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.time.LocalDateTime;

public class ObjectConverter {

    private static BeanMapping beanMapping = new BeanMappingImpl(new ModelMapper());

    public static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule().addSerializer(new LocalDateTimeUTCSerializer()));
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        return mapper.writeValueAsString(object);
    }

    public static String convertJsonBytesToObject(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        return mapper.readValue(object, String.class);
    }

    public static <T> T convertJsonBytesToObject(String object, TypeReference<T> tTypeReference) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule( new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        return mapper.readValue(object, tTypeReference);
    }

    public static <T> T convertJsonBytesToObject(String object, Class<T> objectClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule( new JavaTimeModule().addDeserializer(LocalDateTime.class, new LocalDateTimeUTCDeserializer()));
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        return mapper.readValue(object, objectClass);
    }

    public static String getInitialExceptionMessage(Exception exception) {
        while (exception.getCause() != null) {
            exception = (Exception) exception.getCause();
        }
        return exception.getMessage();
    }


}
