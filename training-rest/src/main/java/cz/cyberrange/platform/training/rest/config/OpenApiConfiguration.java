package cz.cyberrange.platform.training.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Optional;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supplies the generated OpenAPI document's title, version and bearer authentication scheme, and
 * the schema resolver that names generated model properties after the application's JSON wire
 * format.
 */
@Configuration
public class OpenApiConfiguration {

  private static final String BEARER_AUTH_SCHEME_NAME = "bearerAuth";

  /**
   * Builds the OpenAPI document description consumed by the springdoc document generator, naming
   * the API, carrying its version only where the build recorded one, and declaring a bearer
   * authentication scheme.
   *
   * @param buildProperties the application's build metadata, present only when the build recorded
   *     it
   * @return the OpenAPI document description registered with the document generator
   */
  @Bean
  public OpenAPI openApi(Optional<BuildProperties> buildProperties) {
    Info info = new Info().title("Training - API Reference");
    buildProperties.ifPresent(properties -> info.setVersion(properties.getVersion()));
    return new OpenAPI()
        .info(info)
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_AUTH_SCHEME_NAME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Bearer token issued by the configured OIDC provider.")));
  }

  /**
   * Builds the schema resolver the document generator uses to derive a model's OpenAPI schema,
   * backed by the mapper Spring MVC serializes JSON responses with, so a schema's property names
   * match the wire format.
   *
   * @param objectMapper the mapper Spring MVC serializes JSON responses with
   * @return the schema resolver registered with the document generator
   */
  @Bean
  public ModelResolver modelResolver(ObjectMapper objectMapper) {
    return new ModelResolver(objectMapper);
  }
}
