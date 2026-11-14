package cz.cyberrange.platform.training.rest.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Integration tests of the attachment download endpoint, covering who may read a file a definition
 * links to, the answers for files and definitions that do not exist, and the response headers of
 * both the download and its HEAD counterpart.
 */
@DisplayName("Training definition attachment download")
class TrainingDefinitionAttachmentDownloadIT extends AbstractIntegrationTest {

  private static final long ADMINISTRATOR_REF_ID = 710_001L;
  private static final long AUTHOR_REF_ID = 710_002L;
  private static final long OTHER_DESIGNER_REF_ID = 710_003L;
  private static final long ORGANIZER_REF_ID = 710_004L;
  private static final long TRAINEE_REF_ID = 710_005L;
  private static final long OTHER_TRAINEE_REF_ID = 710_006L;
  private static final long UNKNOWN_DEFINITION_ID = 987_654L;

  private static final byte[] FILE_BYTES = "attachment bytes".getBytes(StandardCharsets.UTF_8);
  private static final String FILE_HASH = sha256Hex(FILE_BYTES);
  private static final String MEDIA_TYPE = "text/plain";
  private static final String FILE_NAME = "Příloha 1.txt";
  private static final String ENCODED_DISPOSITION =
      "attachment; filename=\"Priloha 1.txt\"; filename*=UTF-8''P%C5%99%C3%ADloha%201.txt";
  private static final byte[] OTHER_BYTES = "second material".getBytes(StandardCharsets.UTF_8);
  private static final String OTHER_HASH = sha256Hex(OTHER_BYTES);
  private static final long BUDGET_BYTES = 1024L * 1024L;
  private static final String UNLINKED_HASH =
      sha256Hex("not linked".getBytes(StandardCharsets.UTF_8));

  @Autowired private ApplicationContext applicationContext;
  @Autowired private JdbcTemplate jdbcTemplate;

  private TrainingDefinitionSeeder seeder;
  private Long definitionId;
  private Long otherDefinitionId;

  @BeforeEach
  void seedDefinitionWithAttachment() {
    seeder = new TrainingDefinitionSeeder(applicationContext);
    definitionId = seeder.definition("With attachment", TDState.UNRELEASED, AUTHOR_REF_ID);
    Long levelId = seeder.infoLevel(definitionId, 0, 1);
    Long instanceId =
        seeder.instance(
            definitionId, LocalDateTime.now(Clock.systemUTC()).plusDays(1), ORGANIZER_REF_ID);
    seeder.run(instanceId, levelId, TRAINEE_REF_ID);
    otherDefinitionId = seeder.definition("Other", TDState.UNRELEASED, OTHER_DESIGNER_REF_ID);
    Long otherLevelId = seeder.infoLevel(otherDefinitionId, 0, 1);
    Long otherInstanceId =
        seeder.instance(
            otherDefinitionId, LocalDateTime.now(Clock.systemUTC()).plusDays(1), ORGANIZER_REF_ID);
    seeder.run(otherInstanceId, otherLevelId, OTHER_TRAINEE_REF_ID);
    storeFile(FILE_BYTES, FILE_HASH);
    linkFile(definitionId, FILE_HASH, FILE_NAME);
  }

  @AfterEach
  void removeSeededRows() {
    jdbcTemplate.execute("TRUNCATE TABLE definition_attachment, attachment_file CASCADE");
    seeder.clear();
  }

  @Test
  @DisplayName("An administrator downloads the file with its headers")
  void download_administrator_returnsBytesAndHeaders() throws Exception {
    call(ADMINISTRATOR_REF_ID, RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR, get(path(FILE_HASH)))
        .andExpect(status().isOk())
        .andExpect(content().bytes(FILE_BYTES))
        .andExpect(header().string("Content-Type", MEDIA_TYPE))
        .andExpect(header().longValue("Content-Length", FILE_BYTES.length))
        .andExpect(header().string("Content-Disposition", ENCODED_DISPOSITION))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"));
  }

  @Test
  @DisplayName("An author designer downloads the file")
  void download_authorDesigner_returnsBytes() throws Exception {
    call(AUTHOR_REF_ID, RoleTypeSecurity.ROLE_TRAINING_DESIGNER, get(path(FILE_HASH)))
        .andExpect(status().isOk())
        .andExpect(content().bytes(FILE_BYTES));
  }

  @Test
  @DisplayName("An organizer of an instance of the definition downloads the file")
  void download_instanceOrganizer_returnsBytes() throws Exception {
    call(ORGANIZER_REF_ID, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER, get(path(FILE_HASH)))
        .andExpect(status().isOk())
        .andExpect(content().bytes(FILE_BYTES));
  }

  @Test
  @DisplayName("A trainee with a run in an instance of the definition downloads the file")
  void download_traineeWithRun_returnsBytes() throws Exception {
    call(TRAINEE_REF_ID, RoleTypeSecurity.ROLE_TRAINING_TRAINEE, get(path(FILE_HASH)))
        .andExpect(status().isOk())
        .andExpect(content().bytes(FILE_BYTES));
  }

  @Test
  @DisplayName("A designer who does not author the definition is forbidden")
  void download_unrelatedDesigner_isForbidden() throws Exception {
    call(OTHER_DESIGNER_REF_ID, RoleTypeSecurity.ROLE_TRAINING_DESIGNER, get(path(FILE_HASH)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("A trainee without a run in the definition is forbidden")
  void download_traineeWithoutRun_isForbidden() throws Exception {
    call(OTHER_TRAINEE_REF_ID, RoleTypeSecurity.ROLE_TRAINING_TRAINEE, get(path(FILE_HASH)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("A hash the definition does not link answers not found")
  void download_unlinkedHash_isNotFound() throws Exception {
    call(
            ADMINISTRATOR_REF_ID,
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR,
            get(path(UNLINKED_HASH)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("A file stored but linked by another definition only answers not found")
  void download_fileLinkedElsewhere_isNotFound() throws Exception {
    call(
            ADMINISTRATOR_REF_ID,
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR,
            get("/training-definitions/{id}/attachments/{hash}", otherDefinitionId, FILE_HASH))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("An unknown definition answers not found")
  void download_unknownDefinition_isNotFound() throws Exception {
    call(
            ADMINISTRATOR_REF_ID,
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR,
            get("/training-definitions/{id}/attachments/{hash}", UNKNOWN_DEFINITION_ID, FILE_HASH))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("HEAD answers the headers of the download with an empty body")
  void head_administrator_returnsHeadersWithoutBody() throws Exception {
    call(ADMINISTRATOR_REF_ID, RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR, head(path(FILE_HASH)))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", MEDIA_TYPE))
        .andExpect(header().longValue("Content-Length", FILE_BYTES.length))
        .andExpect(header().string("Content-Disposition", ENCODED_DISPOSITION))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(content().bytes(new byte[0]));
  }

  @Test
  @DisplayName("HEAD by a trainee with a run succeeds and by one without a run is forbidden")
  void head_trainees_followTheDownloadAccess() throws Exception {
    call(TRAINEE_REF_ID, RoleTypeSecurity.ROLE_TRAINING_TRAINEE, head(path(FILE_HASH)))
        .andExpect(status().isOk());
    call(OTHER_TRAINEE_REF_ID, RoleTypeSecurity.ROLE_TRAINING_TRAINEE, head(path(FILE_HASH)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("HEAD on a hash the definition does not link answers not found")
  void head_unlinkedHash_isNotFound() throws Exception {
    call(
            ADMINISTRATOR_REF_ID,
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR,
            head(path(UNLINKED_HASH)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("The list describes each material with the budget and the bytes used")
  void list_administrator_returnsMaterialsBudgetAndUsedBytes() throws Exception {
    storeFile(OTHER_BYTES, OTHER_HASH);
    linkFile(definitionId, OTHER_HASH, "a-first.txt");
    jdbcTemplate.update(
        "UPDATE attachment_file SET duration_seconds = 1.5, width = 4, height = 3,"
            + " poster = ?, poster_media_type = 'image/png' WHERE hash = ?",
        new byte[] {1},
        OTHER_HASH);

    call(ADMINISTRATOR_REF_ID, RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR, get(listPath()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.materials.length()").value(2))
        .andExpect(jsonPath("$.materials[0].hash").value(OTHER_HASH))
        .andExpect(jsonPath("$.materials[0].file_name").value("a-first.txt"))
        .andExpect(jsonPath("$.materials[0].media_type").value(MEDIA_TYPE))
        .andExpect(jsonPath("$.materials[0].size_bytes").value(OTHER_BYTES.length))
        .andExpect(jsonPath("$.materials[0].duration_seconds").value(1.5))
        .andExpect(jsonPath("$.materials[0].width").value(4))
        .andExpect(jsonPath("$.materials[0].height").value(3))
        .andExpect(jsonPath("$.materials[0].has_poster").value(true))
        .andExpect(jsonPath("$.materials[1].hash").value(FILE_HASH))
        .andExpect(jsonPath("$.materials[1].file_name").value(FILE_NAME))
        .andExpect(jsonPath("$.materials[1].has_poster").value(false))
        .andExpect(jsonPath("$.budget_bytes").value(BUDGET_BYTES))
        .andExpect(jsonPath("$.used_bytes").value(FILE_BYTES.length + OTHER_BYTES.length));
  }

  @Test
  @DisplayName("The list follows the download access rules")
  void list_followsTheDownloadAccess() throws Exception {
    call(AUTHOR_REF_ID, RoleTypeSecurity.ROLE_TRAINING_DESIGNER, get(listPath()))
        .andExpect(status().isOk());
    call(ORGANIZER_REF_ID, RoleTypeSecurity.ROLE_TRAINING_ORGANIZER, get(listPath()))
        .andExpect(status().isOk());
    call(TRAINEE_REF_ID, RoleTypeSecurity.ROLE_TRAINING_TRAINEE, get(listPath()))
        .andExpect(status().isOk());
    call(OTHER_DESIGNER_REF_ID, RoleTypeSecurity.ROLE_TRAINING_DESIGNER, get(listPath()))
        .andExpect(status().isForbidden());
    call(OTHER_TRAINEE_REF_ID, RoleTypeSecurity.ROLE_TRAINING_TRAINEE, get(listPath()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("The list of an unknown definition answers not found")
  void list_unknownDefinition_isNotFound() throws Exception {
    call(
            ADMINISTRATOR_REF_ID,
            RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR,
            get("/training-definitions/{id}/attachments", UNKNOWN_DEFINITION_ID))
        .andExpect(status().isNotFound());
  }

  private String listPath() {
    return "/training-definitions/" + definitionId + "/attachments";
  }

  private String path(String hash) {
    return "/training-definitions/" + definitionId + "/attachments/" + hash;
  }

  private ResultActions call(
      long callerRefId, RoleTypeSecurity role, MockHttpServletRequestBuilder request)
      throws Exception {
    stubLoggedInUser(callerRefId);
    return mockMvc.perform(request.with(callerWithRoles(role)));
  }

  private void storeFile(byte[] content, String hash) {
    jdbcTemplate.update(
        "INSERT INTO attachment_file (hash, media_type, size_bytes, content) VALUES (?, ?, ?, ?)",
        hash,
        MEDIA_TYPE,
        content.length,
        content);
  }

  private void linkFile(Long linkedDefinitionId, String hash, String fileName) {
    jdbcTemplate.update(
        "INSERT INTO definition_attachment (training_definition_id, file_hash, file_name)"
            + " VALUES (?, ?, ?)",
        linkedDefinitionId,
        hash,
        fileName);
  }

  private static String sha256Hex(byte[] content) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
