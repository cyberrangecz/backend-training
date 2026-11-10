package cz.cyberrange.platform.training.rest.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMultipartMvcRequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Integration tests of the media metadata and poster a multipart save stores per attachment file,
 * and of the HEAD, GET and poster reads that serve them.
 */
@DisplayName("Training definition attachment media metadata")
class TrainingDefinitionAttachmentMediaMetadataIT extends AbstractAttachmentIntegrationTest {

  private static final long CALLER_REF_ID = 730_001L;
  private static final byte[] VIDEO_BYTES = "pretend video".getBytes(StandardCharsets.UTF_8);
  private static final byte[] POSTER_BYTES = PNG_BYTES;

  private MockMvcTester tester;

  @BeforeEach
  void prepareTester() {
    tester = MockMvcTester.create(mockMvc);
    stubLoggedInUser(CALLER_REF_ID);
  }

  @Test
  @DisplayName("a save with metadata stores it and HEAD, GET and the poster read serve it")
  void create_withMetadata_servesHeadersAndPoster() throws Exception {
    Long definitionId = createWith(metadataJson(hash(VIDEO_BYTES), 12.5, 1280, 720, POSTER_BYTES));

    for (MockHttpServletRequestBuilder request :
        new MockHttpServletRequestBuilder[] {
          head(path(definitionId, hash(VIDEO_BYTES))), get(path(definitionId, hash(VIDEO_BYTES)))
        }) {
      read(request)
          .andExpect(status().isOk())
          .andExpect(header().string("X-Attachment-Duration", "12.5"))
          .andExpect(header().string("X-Attachment-Width", "1280"))
          .andExpect(header().string("X-Attachment-Height", "720"));
    }
    read(get(path(definitionId, hash(VIDEO_BYTES)) + "?poster"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "image/png"))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(content().bytes(POSTER_BYTES));
  }

  @Test
  @DisplayName("a file saved without metadata has no metadata headers and no poster")
  void create_withoutMetadata_hasNoHeadersAndPosterIsNotFound() throws Exception {
    Long definitionId = createWith(null);

    read(head(path(definitionId, hash(VIDEO_BYTES))))
        .andExpect(status().isOk())
        .andExpect(header().doesNotExist("X-Attachment-Duration"))
        .andExpect(header().doesNotExist("X-Attachment-Width"))
        .andExpect(header().doesNotExist("X-Attachment-Height"));
    read(get(path(definitionId, hash(VIDEO_BYTES)) + "?poster")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("a poster that is not an image is refused and nothing is stored")
  void create_posterNotAnImage_isBadRequest() {
    MockMultipartMvcRequestBuilder request =
        request(metadataJson(hash(VIDEO_BYTES), 1.0, 10, 10, VIDEO_BYTES));

    parse(request.exchange(), HttpStatus.BAD_REQUEST);

    assertThat(storedHashes()).isEmpty();
  }

  @Test
  @DisplayName("a negative duration is refused")
  void create_negativeDuration_isBadRequest() {
    parse(
        request(metadataJson(hash(VIDEO_BYTES), -1.0, 10, 10, null)).exchange(),
        HttpStatus.BAD_REQUEST);
  }

  private Long createWith(String metadataJson) {
    return parse(request(metadataJson).exchange(), HttpStatus.OK).<Number>read("$.id").longValue();
  }

  private MockMultipartMvcRequestBuilder request(String metadataJson) {
    MockMultipartMvcRequestBuilder request =
        tester
            .post()
            .uri(DEFINITIONS)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER))
            .multipart();
    request.file(
        new MockMultipartFile(
            "definition",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            definitionJson().getBytes(StandardCharsets.UTF_8)));
    request.file(file("clip.mp4", VIDEO_BYTES));
    if (metadataJson != null) {
      request.file(
          new MockMultipartFile(
              "fileMetadata",
              "",
              MediaType.APPLICATION_JSON_VALUE,
              metadataJson.getBytes(StandardCharsets.UTF_8)));
    }
    return request;
  }

  private ResultActions read(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(
        request.with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)));
  }

  private static String path(Long definitionId, String hash) {
    return DEFINITIONS + "/" + definitionId + "/attachments/" + hash;
  }

  private static String definitionJson() {
    return """
        {"title": "With media", "state": "UNRELEASED", "levels": [
          {"title": "Info", "level_type": "INFO_LEVEL", "content": "attachment:%s",
           "estimated_duration": 5}]}
        """
        .formatted(hash(VIDEO_BYTES));
  }

  private static String metadataJson(
      String hash, double duration, int width, int height, byte[] poster) {
    String posterField =
        poster == null ? "null" : "\"" + Base64.getEncoder().encodeToString(poster) + "\"";
    return """
        [{"hash": "%s", "duration_seconds": %s, "width": %d, "height": %d, "poster": %s}]
        """
        .formatted(hash, duration, width, height, posterField);
  }
}
