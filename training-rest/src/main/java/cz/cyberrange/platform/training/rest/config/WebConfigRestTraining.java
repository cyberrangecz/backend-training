package cz.cyberrange.platform.training.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Configures content negotiation from the {@code Accept} header, defaulting to JSON, and reads and
 * writes YAML bodies under the {@code application/yaml}, {@code text/yaml} and {@code text/yml}
 * media types
 */
@Configuration
public class WebConfigRestTraining implements WebMvcConfigurer {

  @Autowired private JsonMapper jsonMapper;
  @Autowired private YAMLMapper yamlMapper;

  /**
   * Resolves the response content type from the {@code Accept} header. Without one, JSON is
   * preferred, falling back to any type the handler produces. The path extension is not consulted,
   * so a path ending in {@code .yaml}, such as {@code /v3/api-docs.yaml}, keeps the media type its
   * handler produces
   */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer
        .favorParameter(false)
        .ignoreAcceptHeader(false)
        .defaultContentType(MediaType.APPLICATION_JSON, MediaType.ALL);
  }

  /**
   * Reads and writes JSON and YAML bodies through the application's mappers, which ignore unknown
   * fields everywhere except in the body of a training definition import
   */
  @Override
  public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
    builder
        .withJsonConverter(ImportBodyConverters.json(jsonMapper))
        .withYamlConverter(ImportBodyConverters.yaml(yamlMapper));
  }
}
