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
 * Configures content negotiation to recognize the {@code .yml} and {@code .yaml} path extensions
 * alongside JSON, and registers a message converter for the media types those extensions resolve to
 */
@Configuration
public class WebConfigRestTraining implements WebMvcConfigurer {

  /** Media type matched by the {@code .yaml} path extension */
  private static final MediaType MEDIA_TYPE_YAML = MediaType.valueOf("text/yaml");

  /** Media type matched by the {@code .yml} path extension */
  private static final MediaType MEDIA_TYPE_YML = MediaType.valueOf("text/yml");

  @Autowired private ObjectMapper objectMapper;

  /**
   * Resolves the response content type from the request path extension or the {@code Accept}
   * header, defaulting to JSON, and additionally recognizes the {@code .yml} and {@code .yaml}
   * extensions
   */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer
        .favorPathExtension(true)
        .favorParameter(false)
        .ignoreAcceptHeader(false)
        .defaultContentType(MediaType.APPLICATION_JSON)
        .mediaType(MediaType.APPLICATION_JSON.getSubtype(), MediaType.APPLICATION_JSON)
        .mediaType(MEDIA_TYPE_YML.getSubtype(), MEDIA_TYPE_YML)
        .mediaType(MEDIA_TYPE_YAML.getSubtype(), MEDIA_TYPE_YAML);
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
