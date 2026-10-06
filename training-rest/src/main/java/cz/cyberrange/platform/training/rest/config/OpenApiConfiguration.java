package cz.cyberrange.platform.training.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import cz.cyberrange.platform.training.api.dto.event.AssessmentAnsweredDTO;
import cz.cyberrange.platform.training.rest.utils.error.ApiMicroserviceError;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Optional;
import org.springdoc.core.customizers.QuerydslPredicateOperationCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.web.method.HandlerMethod;

/**
 * Supplies the generated OpenAPI document's title, version and bearer authentication scheme, the
 * schema resolver that names generated model properties after the application's JSON wire format,
 * and the document generator settings for QueryDSL predicates and polymorphic properties.
 */
@Configuration
public class OpenApiConfiguration {

  private static final String BEARER_AUTH_SCHEME_NAME = "bearerAuth";

  /*
   * Documents each property of the listed models as a reference to the property's declared type
   * rather than as a oneOf of that type's subtypes.
   */
  static {
    SpringDocUtils.getConfig()
        .addParentType(
            AssessmentAnsweredDTO.class.getSimpleName(),
            ApiMicroserviceError.class.getSimpleName());
  }

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
   * resolving property names in snake case, so a schema's property names match the wire format.
   *
   * @return the schema resolver registered with the document generator
   */
  @Bean
  public ModelResolver modelResolver() {
    ObjectMapper schemaObjectMapper = new ObjectMapper();
    schemaObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    return new ModelResolver(schemaObjectMapper);
  }

  /**
   * Builds the document generator's QueryDSL operation customizer as one that leaves operations
   * unchanged, so a QueryDSL predicate argument is documented as the single {@code predicate} query
   * parameter rather than as one query parameter per field of the predicate's root entity.
   *
   * @param querydslBindingsFactory the factory of the QueryDSL bindings predicates are bound with
   * @param springDocConfigProperties the document generator's settings
   * @return the customizer registered in place of the generator's default one
   */
  @Bean
  public QuerydslPredicateOperationCustomizer querydslPredicateOperationCustomizer(
      QuerydslBindingsFactory querydslBindingsFactory,
      SpringDocConfigProperties springDocConfigProperties) {
    return new QuerydslPredicateOperationCustomizer(
        querydslBindingsFactory, springDocConfigProperties) {
      @Override
      public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        return operation;
      }
    };
  }
}
