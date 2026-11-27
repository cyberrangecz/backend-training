package cz.cyberrange.platform.training.rest.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.service.config.ObjectMappersConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

class ImportBodyConvertersTest {

  private static final String JSON_WITH_UNKNOWN_FIELD =
      "{\"title\": \"Imported\", \"bogus_field\": 1}";
  private static final String YAML_WITH_UNKNOWN_FIELD = "title: Imported\nbogus_field: 1\n";

  private final ObjectMappersConfiguration configuration = new ObjectMappersConfiguration();

  record LenientBody(String title) {}

  private JsonMapper productionJsonMapper() {
    JsonMapper.Builder builder = JsonMapper.builder();
    configuration.snakeCaseJsonMapperCustomizer().customize(builder);
    return builder.build();
  }

  private Object read(
      AbstractJacksonHttpMessageConverter<?> converter,
      Class<?> targetType,
      MediaType mediaType,
      String body)
      throws IOException {
    MockHttpInputMessage message = new MockHttpInputMessage(body.getBytes(StandardCharsets.UTF_8));
    message.getHeaders().setContentType(mediaType);
    return converter.read(ResolvableType.forClass(targetType), message, null);
  }

  private void assertRefusesBogusField(HttpMessageNotReadableException exception) {
    UnrecognizedPropertyException cause =
        assertInstanceOf(UnrecognizedPropertyException.class, exception.getCause());
    assertEquals("bogus_field", cause.getPropertyName());
  }

  @Test
  void jsonImportRefusesUnknownField() {
    var converter = ImportBodyConverters.json(productionJsonMapper());

    assertRefusesBogusField(
        assertThrows(
            HttpMessageNotReadableException.class,
            () ->
                read(
                    converter,
                    ImportTrainingDefinitionDTO.class,
                    MediaType.APPLICATION_JSON,
                    JSON_WITH_UNKNOWN_FIELD)));
  }

  @Test
  void jsonImportRefusesUnknownFieldWithCharsetParameter() {
    var converter = ImportBodyConverters.json(productionJsonMapper());

    assertRefusesBogusField(
        assertThrows(
            HttpMessageNotReadableException.class,
            () ->
                read(
                    converter,
                    ImportTrainingDefinitionDTO.class,
                    MediaType.parseMediaType("application/json;charset=UTF-8"),
                    JSON_WITH_UNKNOWN_FIELD)));
  }

  @Test
  void jsonOtherBodyIgnoresUnknownField() throws IOException {
    var converter = ImportBodyConverters.json(productionJsonMapper());

    Object body =
        read(converter, LenientBody.class, MediaType.APPLICATION_JSON, JSON_WITH_UNKNOWN_FIELD);

    assertEquals(new LenientBody("Imported"), body);
  }

  @Test
  void yamlImportRefusesUnknownFieldUnderEveryYamlMediaType() {
    var converter = ImportBodyConverters.yaml(productionYamlMapper());

    for (MediaType mediaType :
        new MediaType[] {
          MediaType.APPLICATION_YAML,
          MediaType.parseMediaType("text/yaml"),
          MediaType.parseMediaType("text/yml")
        }) {
      assertRefusesBogusField(
          assertThrows(
              HttpMessageNotReadableException.class,
              () ->
                  read(
                      converter,
                      ImportTrainingDefinitionDTO.class,
                      mediaType,
                      YAML_WITH_UNKNOWN_FIELD)));
    }
  }

  @Test
  void yamlOtherBodyIgnoresUnknownField() throws IOException {
    var converter = ImportBodyConverters.yaml(productionYamlMapper());

    Object body =
        read(converter, LenientBody.class, MediaType.APPLICATION_YAML, YAML_WITH_UNKNOWN_FIELD);

    assertEquals(new LenientBody("Imported"), body);
  }

  private YAMLMapper productionYamlMapper() {
    return configuration.yamlMapper();
  }
}
