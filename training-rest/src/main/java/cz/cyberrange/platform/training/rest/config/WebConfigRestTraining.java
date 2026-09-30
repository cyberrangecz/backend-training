package cz.cyberrange.platform.training.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures content negotiation from the {@code Accept} header, defaulting to JSON, and registers a
 * message converter for the {@code text/yaml} and {@code text/yml} media types
 */
@Configuration
public class WebConfigRestTraining implements WebMvcConfigurer {

  /** Media type {@code text/yaml} */
  private static final MediaType MEDIA_TYPE_YAML = MediaType.valueOf("text/yaml");

  /** Media type {@code text/yml} */
  private static final MediaType MEDIA_TYPE_YML = MediaType.valueOf("text/yml");

  @Autowired private ObjectMapper objectMapper;

  /**
   * Resolves the response content type from the {@code Accept} header. Without one, JSON is
   * preferred, falling back to any type the handler produces. The path extension is not consulted,
   * so a path ending in {@code .yaml}, such as {@code /v3/api-docs.yaml}, keeps the media type its
   * handler produces
   */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer
        .favorPathExtension(false)
        .favorParameter(false)
        .ignoreAcceptHeader(false)
        .defaultContentType(MediaType.APPLICATION_JSON, MediaType.ALL);
  }

  /**
   * Appends a converter that serializes a response declared under the {@code text/yaml} or {@code
   * text/yml} media type through the application's autowired {@link ObjectMapper}
   */
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter yamlConverter =
        new MappingJackson2HttpMessageConverter(objectMapper);
    yamlConverter.setSupportedMediaTypes(List.of(MEDIA_TYPE_YAML, MEDIA_TYPE_YML));
    converters.add(yamlConverter);
  }
}
