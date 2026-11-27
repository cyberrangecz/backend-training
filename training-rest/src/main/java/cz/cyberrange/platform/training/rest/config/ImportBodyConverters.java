package cz.cyberrange.platform.training.rest.config;

import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.service.export.YamlExportFormat;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.converter.yaml.JacksonYamlHttpMessageConverter;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Builds the JSON and YAML request body converters that read a training definition import with a
 * mapper refusing any field the import does not declare, while every other body type keeps the
 * lenient mapper the converter is built from.
 */
public final class ImportBodyConverters {

  private ImportBodyConverters() {}

  /**
   * Builds the JSON converter.
   *
   * @param lenientMapper the mapper reading every body type other than the import
   * @return the converter reading {@link ImportTrainingDefinitionDTO} strictly
   */
  public static JacksonJsonHttpMessageConverter json(JsonMapper lenientMapper) {
    JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(lenientMapper);
    converter.registerMappersForType(
        ImportTrainingDefinitionDTO.class,
        mappers ->
            mappers.put(
                MediaType.APPLICATION_JSON,
                lenientMapper
                    .rebuild()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .build()));
    return converter;
  }

  /**
   * Builds the YAML converter, serving every YAML media type.
   *
   * @param lenientMapper the mapper reading every body type other than the import
   * @return the converter reading {@link ImportTrainingDefinitionDTO} strictly
   */
  public static JacksonYamlHttpMessageConverter yaml(YAMLMapper lenientMapper) {
    JacksonYamlHttpMessageConverter converter = new JacksonYamlHttpMessageConverter(lenientMapper);
    converter.setSupportedMediaTypes(YamlExportFormat.MEDIA_TYPES);
    YAMLMapper strictMapper =
        lenientMapper.rebuild().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
    converter.registerMappersForType(
        ImportTrainingDefinitionDTO.class,
        mappers -> YamlExportFormat.MEDIA_TYPES.forEach(type -> mappers.put(type, strictMapper)));
    return converter;
  }
}
