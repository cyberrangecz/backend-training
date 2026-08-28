package cz.cyberrange.platform.training.rest.utils.reader;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.apache.maven.plugin.logging.Log;

/**
 * Reader registered with the kongchen swagger-maven-plugin (see {@code training-rest/pom.xml}) for
 * generating the Swagger document at compile time. Switches the naming strategy of Swagger's shared
 * Jackson mapper to snake case, so model property names in the generated document appear in snake
 * case rather than the mapper's default.
 */
public class SnakeCaseSwaggerReader extends SpringMvcApiReader {

  public SnakeCaseSwaggerReader(Swagger swagger, Log log) {
    super(swagger, log);
    Json.mapper().setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
  }
}
