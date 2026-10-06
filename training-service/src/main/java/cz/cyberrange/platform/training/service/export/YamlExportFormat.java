package cz.cyberrange.platform.training.service.export;

import cz.cyberrange.platform.training.service.utils.AbstractFileExtensions;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Exports as YAML through the application's YAML mapper. A record is one YAML document opened by a
 * {@code ---} marker, so a file of records is a multi-document YAML stream.
 */
@Component
public class YamlExportFormat extends ExportFormat {

  /** Media type {@code text/yaml} */
  public static final String TEXT_YAML_VALUE = "text/yaml";

  /** Media type {@code text/yml} */
  public static final String TEXT_YML_VALUE = "text/yml";

  /** Media types YAML is requested and sent by, {@code application/yaml} first */
  public static final List<MediaType> MEDIA_TYPES =
      List.of(
          MediaType.APPLICATION_YAML,
          MediaType.valueOf(TEXT_YAML_VALUE),
          MediaType.valueOf(TEXT_YML_VALUE));

  /**
   * Creates the format.
   *
   * @param yamlMapper the application's YAML mapper
   */
  public YamlExportFormat(YAMLMapper yamlMapper) {
    super("yaml", MEDIA_TYPES, AbstractFileExtensions.YAML_FILE_EXTENSION, yamlMapper);
  }

  @Override
  public byte[] writeRecord(Object value) {
    return writeDocument(value);
  }
}
