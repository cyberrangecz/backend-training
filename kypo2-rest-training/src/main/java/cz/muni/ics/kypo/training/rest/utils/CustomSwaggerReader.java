package cz.muni.ics.kypo.training.rest.utils;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.apache.maven.plugin.logging.Log;

/**
 * That class is used to generate snake_case fields in REST API .adoc and .pdf documentation.
 */
public class CustomSwaggerReader extends SpringMvcApiReader {

    /**
     * Instantiates a new Custom swagger reader.
     *
     * @param swagger the swagger
     * @param log     the log
     */
    public CustomSwaggerReader(Swagger swagger, Log log) {
        super(swagger, log);
        Json.mapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }
}
