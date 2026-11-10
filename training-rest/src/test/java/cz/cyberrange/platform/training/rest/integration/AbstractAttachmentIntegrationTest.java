package cz.cyberrange.platform.training.rest.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.hash.Hashing;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Shared fixtures of the integration tests that save, clone, export and import training definitions
 * with attachment files: sample bytes, response parsing, and reads of the attachment tables.
 */
abstract class AbstractAttachmentIntegrationTest extends AbstractIntegrationTest {

  static final String DEFINITIONS = "/training-definitions";

  static final byte[] PNG_BYTES =
      concat(new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'}, filler('x', 64));

  @Autowired JdbcTemplate jdbcTemplate;

  @AfterEach
  void removeCreatedRows() {
    jdbcTemplate.execute(
        "TRUNCATE TABLE training_definition, user_ref, definition_attachment, attachment_file"
            + " CASCADE");
  }

  static DocumentContext parse(MvcTestResult result, HttpStatus status) {
    assertThat(result).hasStatus(status).hasContentType(MediaType.APPLICATION_JSON);
    return JsonPath.parse(
        new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8));
  }

  static MockMultipartFile file(String fileName, byte[] content) {
    return new MockMultipartFile("files", fileName, "application/octet-stream", content);
  }

  Map<String, String> linkedFileNames(Long definitionId) {
    return jdbcTemplate
        .query(
            "SELECT file_hash, file_name FROM definition_attachment"
                + " WHERE training_definition_id = ?",
            (row, index) -> Map.entry(row.getString(1), row.getString(2)),
            definitionId)
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  List<String> storedHashes() {
    return jdbcTemplate.queryForList("SELECT hash FROM attachment_file", String.class);
  }

  long definitionCount() {
    return jdbcTemplate.queryForObject("SELECT count(*) FROM training_definition", Long.class);
  }

  static String hash(byte[] content) {
    return Hashing.sha256().hashBytes(content).toString();
  }

  static byte[] filler(char character, int length) {
    byte[] bytes = new byte[length];
    Arrays.fill(bytes, (byte) character);
    return bytes;
  }

  static byte[] concat(byte[] first, byte[] second) {
    byte[] joined = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, joined, first.length, second.length);
    return joined;
  }
}
