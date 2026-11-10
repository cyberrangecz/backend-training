package cz.cyberrange.platform.training.rest.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMultipartMvcRequestBuilder;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;

/**
 * Integration tests of linking a stored attachment file by its hash and of uploading one file to a
 * training definition: the answers for known and unknown hashes, the hash check, the stored media
 * metadata, the budget and who may call. The attachment budget of the test context is 1 MB.
 */
@DisplayName("Training definition attachment link and upload")
class TrainingDefinitionAttachmentUploadIT extends AbstractAttachmentIntegrationTest {

  private static final long CALLER_REF_ID = 730_001L;
  private static final long OTHER_CALLER_REF_ID = 730_002L;
  private static final int NEARLY_THE_BUDGET = 700_000;
  private static final byte[] TEXT_BYTES = "plain notes".getBytes(StandardCharsets.UTF_8);

  private MockMvcTester tester;

  @BeforeEach
  void prepareTester() {
    tester = MockMvcTester.create(mockMvc);
    stubLoggedInUser(CALLER_REF_ID);
  }

  @Test
  @DisplayName("linking an unknown hash answers 404 so the caller uploads the bytes")
  void link_unknownHash_returnsNotFound() {
    Long definitionId = createDefinition();

    parse(
        link(definitionId, RoleTypeSecurity.ROLE_TRAINING_DESIGNER, hash(TEXT_BYTES), "notes.txt")
            .exchange(),
        HttpStatus.NOT_FOUND);

    assertThat(linkedFileNames(definitionId)).isEmpty();
  }

  @Test
  @DisplayName("an upload stores the file with its metadata and links it as a material")
  void upload_validFile_storesMetadataAndLinks() {
    Long definitionId = createDefinition();

    DocumentContext material =
        parse(
            upload(
                    definitionId,
                    RoleTypeSecurity.ROLE_TRAINING_DESIGNER,
                    filePart("diagram.bin", PNG_BYTES),
                    metadata(hash(PNG_BYTES), 4, 5))
                .exchange(),
            HttpStatus.OK);

    assertThat(material.<String>read("$.hash")).isEqualTo(hash(PNG_BYTES));
    assertThat(material.<String>read("$.file_name")).isEqualTo("diagram.bin");
    assertThat(material.<String>read("$.media_type")).isEqualTo("image/png");
    assertThat(material.<Number>read("$.width").intValue()).isEqualTo(4);
    assertThat(linkedFileNames(definitionId)).containsEntry(hash(PNG_BYTES), "diagram.bin");
    assertThat(storedHashes()).containsExactly(hash(PNG_BYTES));
  }

  @Test
  @DisplayName("a stored hash links to another definition without bytes, under the sent name")
  void link_storedHash_linksFileOfAnotherDefinition() {
    Long firstId = createDefinition();
    parse(
        upload(
                firstId,
                RoleTypeSecurity.ROLE_TRAINING_DESIGNER,
                filePart("diagram.png", PNG_BYTES),
                metadata(hash(PNG_BYTES), 4, 5))
            .exchange(),
        HttpStatus.OK);
    Long secondId = createDefinition();

    DocumentContext material =
        parse(
            link(secondId, RoleTypeSecurity.ROLE_TRAINING_DESIGNER, hash(PNG_BYTES), "copy.png")
                .exchange(),
            HttpStatus.OK);

    assertThat(material.<String>read("$.file_name")).isEqualTo("copy.png");
    assertThat(linkedFileNames(secondId)).isEqualTo(Map.of(hash(PNG_BYTES), "copy.png"));
    assertThat(storedHashes()).containsExactly(hash(PNG_BYTES));
  }

  @Test
  @DisplayName("an upload whose hash is not the hash of the bytes answers 400 and stores nothing")
  void upload_hashMismatch_returnsBadRequest() {
    Long definitionId = createDefinition();

    parse(
        upload(
                definitionId,
                RoleTypeSecurity.ROLE_TRAINING_DESIGNER,
                filePart("notes.txt", TEXT_BYTES),
                metadata(hash(PNG_BYTES), null, null))
            .exchange(),
        HttpStatus.BAD_REQUEST);

    assertThat(storedHashes()).isEmpty();
    assertThat(linkedFileNames(definitionId)).isEmpty();
  }

  @Test
  @DisplayName("an upload past the budget answers 413 and keeps no file or link")
  void upload_overBudget_returnsPayloadTooLargeAndRollsBack() {
    Long definitionId = createDefinition();
    byte[] first = filler('a', NEARLY_THE_BUDGET);
    byte[] second = filler('b', NEARLY_THE_BUDGET);
    parse(
        upload(
                definitionId,
                RoleTypeSecurity.ROLE_TRAINING_DESIGNER,
                filePart("first.bin", first),
                metadata(hash(first), null, null))
            .exchange(),
        HttpStatus.OK);

    parse(
        upload(
                definitionId,
                RoleTypeSecurity.ROLE_TRAINING_DESIGNER,
                filePart("second.bin", second),
                metadata(hash(second), null, null))
            .exchange(),
        HttpStatus.PAYLOAD_TOO_LARGE);

    assertThat(storedHashes()).containsExactly(hash(first));
    assertThat(linkedFileNames(definitionId)).containsOnlyKeys(hash(first));
  }

  @Test
  @DisplayName("linking a stored file past the budget answers 413")
  void link_overBudget_returnsPayloadTooLarge() {
    Long firstId = createDefinition();
    Long secondId = createDefinition();
    byte[] first = filler('a', NEARLY_THE_BUDGET);
    byte[] second = filler('b', NEARLY_THE_BUDGET);
    for (byte[] content : new byte[][] {first, second}) {
      parse(
          upload(
                  firstId,
                  RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR,
                  filePart("f.bin", content),
                  metadata(hash(content), null, null))
              .exchange(),
          content == first ? HttpStatus.OK : HttpStatus.PAYLOAD_TOO_LARGE);
    }
    parse(
        upload(
                secondId,
                RoleTypeSecurity.ROLE_TRAINING_DESIGNER,
                filePart("s.bin", second),
                metadata(hash(second), null, null))
            .exchange(),
        HttpStatus.OK);

    parse(
        link(secondId, RoleTypeSecurity.ROLE_TRAINING_DESIGNER, hash(first), "first.bin")
            .exchange(),
        HttpStatus.PAYLOAD_TOO_LARGE);

    assertThat(linkedFileNames(secondId)).containsOnlyKeys(hash(second));
  }

  @Test
  @DisplayName("a designer who is not an author of the definition may neither link nor upload")
  void linkAndUpload_designerOfAnotherDefinition_returnsForbidden() {
    Long definitionId = createDefinition();
    stubLoggedInUser(OTHER_CALLER_REF_ID);

    parse(
        link(definitionId, RoleTypeSecurity.ROLE_TRAINING_DESIGNER, hash(TEXT_BYTES), "n.txt")
            .exchange(),
        HttpStatus.FORBIDDEN);
    parse(
        upload(
                definitionId,
                RoleTypeSecurity.ROLE_TRAINING_DESIGNER,
                filePart("notes.txt", TEXT_BYTES),
                metadata(hash(TEXT_BYTES), null, null))
            .exchange(),
        HttpStatus.FORBIDDEN);

    assertThat(storedHashes()).isEmpty();
  }

  private Long createDefinition() {
    DocumentContext created =
        parse(
            tester
                .post()
                .uri(DEFINITIONS)
                .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title": "Uploads", "state": "UNRELEASED", "materials": [], "levels": [
                      {"title": "Info", "level_type": "INFO_LEVEL", "content": "c",
                       "estimated_duration": 5}]}
                    """)
                .exchange(),
            HttpStatus.OK);
    return created.<Number>read("$.id").longValue();
  }

  private MockMvcRequestBuilder link(
      Long definitionId, RoleTypeSecurity role, String hash, String fileName) {
    return tester
        .post()
        .uri(DEFINITIONS + "/{id}/attachments/link", definitionId)
        .with(callerWithRoles(role))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"hash\": \"%s\", \"file_name\": \"%s\"}".formatted(hash, fileName));
  }

  private MockMultipartMvcRequestBuilder upload(
      Long definitionId,
      RoleTypeSecurity role,
      MockMultipartFile file,
      MockMultipartFile metadata) {
    return tester
        .post()
        .uri(DEFINITIONS + "/{id}/attachments", definitionId)
        .with(callerWithRoles(role))
        .multipart()
        .file(file)
        .file(metadata);
  }

  private static MockMultipartFile metadata(String hash, Integer width, Integer height) {
    String json =
        "{\"hash\": \"%s\", \"width\": %s, \"height\": %s}".formatted(hash, width, height);
    return new MockMultipartFile(
        "metadata", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8));
  }

  private static MockMultipartFile filePart(String fileName, byte[] content) {
    return new MockMultipartFile("file", fileName, "application/octet-stream", content);
  }
}
