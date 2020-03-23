package cz.muni.ics.kypo.training.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AssessmentResponseSchemaConfiguration {


    @Bean("assessmentResponseSchema")
    public JsonNode assessmentResponseSchema() {
        try {
            return JsonLoader.fromResource("/responses-schema.json");
        } catch (IOException ex) {
            throw new BeanInitializationException("Could not create bean AssessmentResponseSchema.");
        }
    }

    @Bean
    public JsonSchemaFactory jsonSchemaFactory() {
        return JsonSchemaFactory.byDefault();
    }
}

