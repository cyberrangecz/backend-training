package cz.cyberrange.platform.training.service.export;

import com.google.common.primitives.Bytes;
import cz.cyberrange.platform.training.service.utils.AbstractFileExtensions;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.core.util.MinimalPrettyPrinter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exports as JSON through the application's JSON mapper. A record is one compact JSON value on its
 * own line.
 */
@Component
public class JsonExportFormat extends ExportFormat {

  private static final byte[] RECORD_SEPARATOR =
      System.lineSeparator().getBytes(StandardCharsets.UTF_8);

  /**
   * Creates the format.
   *
   * @param jsonMapper the application's JSON mapper
   */
  public JsonExportFormat(JsonMapper jsonMapper) {
    super(
        "json",
        List.of(MediaType.APPLICATION_JSON),
        AbstractFileExtensions.JSON_FILE_EXTENSION,
        jsonMapper);
  }

  @Override
  public byte[] writeRecord(Object value) {
    return Bytes.concat(
        mapper.writer().with(new MinimalPrettyPrinter()).writeValueAsBytes(value),
        RECORD_SEPARATOR);
  }
}
