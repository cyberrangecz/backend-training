package cz.cyberrange.platform.training.rest.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
 * Integration tests of the multipart save of a training definition: files that arrive with the
 * levels are stored, linked, pruned and deduplicated in the same transaction as the level write.
 * The attachment budget of the test context is 1 MB.
 */
@DisplayName("Training definition save with attachments")
class TrainingDefinitionAttachmentSaveIT extends AbstractAttachmentIntegrationTest {

  private static final long CALLER_REF_ID = 720_001L;

  private static final byte[] TEXT_BYTES = "plain notes".getBytes(StandardCharsets.UTF_8);
  private static final byte[] UNUSED_BYTES = "never referenced".getBytes(StandardCharsets.UTF_8);
  private static final int NEARLY_THE_BUDGET = 700_000;

  private MockMvcTester tester;

  @BeforeEach
  void prepareTester() {
    tester = MockMvcTester.create(mockMvc);
    stubLoggedInUser(CALLER_REF_ID);
  }

  @Test
  @DisplayName("a create stores and links the files its levels reference and ignores the others")
  void create_filesReferencedFromDifferentLevels_storesAndLinksThem() {
    DocumentContext created =
        respond(
            create(
                createBody(
                    infoLevel("Look: ![image](attachment:" + hash(PNG_BYTES) + ")"),
                    trainingLevel("Read attachment:" + hash(TEXT_BYTES))),
                file("diagram.png", PNG_BYTES),
                file("notes.txt", TEXT_BYTES),
                file("unused.txt", UNUSED_BYTES)),
            HttpStatus.OK);

    Long definitionId = created.<Number>read("$.id").longValue();
    assertThat(linkedFileNames(definitionId))
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(hash(PNG_BYTES), "diagram.png", hash(TEXT_BYTES), "notes.txt"));
    assertThat(storedHashes()).containsExactlyInAnyOrder(hash(PNG_BYTES), hash(TEXT_BYTES));
    assertThat(mediaTypeOf(hash(PNG_BYTES))).isEqualTo("image/png");
    assertThat(mediaTypeOf(hash(TEXT_BYTES))).isEqualTo("text/plain");
  }

  @Test
  @DisplayName("an update that drops a reference removes the link and deletes the orphaned file")
  void update_referenceRemoved_prunesLinkAndDeletesOrphan() {
    DocumentContext created =
        respond(
            create(
                createBody(infoLevel("Look: attachment:" + hash(PNG_BYTES))),
                file("diagram.png", PNG_BYTES)),
            HttpStatus.OK);
    Long definitionId = created.<Number>read("$.id").longValue();

    respond(
        update(definitionId, updateBody(created, infoLevelWithId(created, "No reference now"))),
        HttpStatus.OK);

    assertThat(linkedFileNames(definitionId)).isEmpty();
    assertThat(storedHashes()).isEmpty();
  }

  @Test
  @DisplayName("an update adds a file next to the one already linked")
  void update_newFileReferenced_linksItBesideTheExistingOne() {
    DocumentContext created =
        respond(
            create(
                createBody(infoLevel("attachment:" + hash(PNG_BYTES))),
                file("diagram.png", PNG_BYTES)),
            HttpStatus.OK);
    Long definitionId = created.<Number>read("$.id").longValue();

    respond(
        update(
            definitionId,
            updateBody(
                created,
                infoLevelWithId(
                    created, "attachment:" + hash(PNG_BYTES) + " attachment:" + hash(TEXT_BYTES))),
            file("notes.txt", TEXT_BYTES)),
        HttpStatus.OK);

    assertThat(linkedFileNames(definitionId))
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(hash(PNG_BYTES), "diagram.png", hash(TEXT_BYTES), "notes.txt"));
  }

  @Test
  @DisplayName("a material no content references is stored and survives a later save")
  void update_storedOnlyMaterial_survivesSaveWithoutResupply() {
    DocumentContext created =
        respond(
            create(
                createBodyKeeping(material(UNUSED_BYTES, "handout.txt"), infoLevel("No reference")),
                file("upload.txt", UNUSED_BYTES)),
            HttpStatus.OK);
    Long definitionId = created.<Number>read("$.id").longValue();
    assertThat(linkedFileNames(definitionId))
        .containsExactlyEntriesOf(Map.of(hash(UNUSED_BYTES), "handout.txt"));

    respond(
        update(
            definitionId,
            updateBodyKeeping(
                created,
                material(UNUSED_BYTES, "renamed.txt"),
                infoLevelWithId(created, "Still no reference"))),
        HttpStatus.OK);

    assertThat(linkedFileNames(definitionId))
        .containsExactlyEntriesOf(Map.of(hash(UNUSED_BYTES), "renamed.txt"));
    assertThat(storedHashes()).containsExactly(hash(UNUSED_BYTES));
  }

  @Test
  @DisplayName("an update leaving a material out removes its link and deletes its file")
  void update_materialRemoved_deletesTheFileWhenNoDefinitionLinksIt() {
    DocumentContext created =
        respond(
            create(
                createBodyKeeping(material(UNUSED_BYTES, "handout.txt"), infoLevel("Text")),
                file("upload.txt", UNUSED_BYTES)),
            HttpStatus.OK);
    Long definitionId = created.<Number>read("$.id").longValue();

    respond(
        update(definitionId, updateBody(created, infoLevelWithId(created, "Text"))), HttpStatus.OK);

    assertThat(linkedFileNames(definitionId)).isEmpty();
    assertThat(storedHashes()).isEmpty();
  }

  @Test
  @DisplayName("a material neither supplied nor stored answers status 400 and stores nothing")
  void create_unresolvedMaterial_returnsBadRequestAndStoresNothing() {
    long definitionsBefore = definitionCount();

    DocumentContext error =
        respond(
            create(createBodyKeeping(material(TEXT_BYTES, "lost.txt"), infoLevel("Text"))),
            HttpStatus.BAD_REQUEST);

    assertThat(error.<String>read("$.message")).contains(hash(TEXT_BYTES));
    assertThat(definitionCount()).isEqualTo(definitionsBefore);
    assertThat(storedHashes()).isEmpty();
  }

  @Test
  @DisplayName("identical bytes saved for 2 definitions are stored once and linked twice")
  void create_sameBytesForTwoDefinitions_storesOneFileAndTwoLinks() {
    Long first =
        respond(
                create(
                    createBody(infoLevel("attachment:" + hash(TEXT_BYTES))),
                    file("first.txt", TEXT_BYTES)),
                HttpStatus.OK)
            .<Number>read("$.id")
            .longValue();
    Long second =
        respond(
                create(
                    createBody(infoLevel("attachment:" + hash(TEXT_BYTES))),
                    file("second.txt", TEXT_BYTES)),
                HttpStatus.OK)
            .<Number>read("$.id")
            .longValue();

    assertThat(storedHashes()).containsExactly(hash(TEXT_BYTES));
    assertThat(linkedFileNames(first)).containsEntry(hash(TEXT_BYTES), "first.txt");
    assertThat(linkedFileNames(second)).containsEntry(hash(TEXT_BYTES), "second.txt");
  }

  @Test
  @DisplayName("a plain JSON save keeps the links of the files its content still references")
  void update_plainJson_keepsReferencedStoredFilesLinked() {
    DocumentContext created =
        respond(
            create(
                createBody(infoLevel("attachment:" + hash(TEXT_BYTES))),
                file("notes.txt", TEXT_BYTES)),
            HttpStatus.OK);
    Long definitionId = created.<Number>read("$.id").longValue();

    respond(
        tester
            .put()
            .uri(DEFINITIONS + "/{id}", definitionId)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                updateBody(created, infoLevelWithId(created, "attachment:" + hash(TEXT_BYTES)))),
        HttpStatus.OK);

    assertThat(linkedFileNames(definitionId)).containsEntry(hash(TEXT_BYTES), "notes.txt");
    assertThat(storedHashes()).containsExactly(hash(TEXT_BYTES));
  }

  @Test
  @DisplayName("a plain JSON create may reference a file another definition already stored")
  void create_plainJsonReferencingStoredFile_linksIt() {
    respond(
        create(
            createBody(infoLevel("attachment:" + hash(TEXT_BYTES))), file("notes.txt", TEXT_BYTES)),
        HttpStatus.OK);

    Long second =
        respond(
                tester
                    .post()
                    .uri(DEFINITIONS)
                    .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createBody(infoLevel("attachment:" + hash(TEXT_BYTES)))),
                HttpStatus.OK)
            .<Number>read("$.id")
            .longValue();

    assertThat(linkedFileNames(second)).containsEntry(hash(TEXT_BYTES), "notes.txt");
  }

  @Test
  @DisplayName("a reference no file answers status 400 and stores nothing")
  void create_unresolvedReference_returnsBadRequestAndStoresNothing() {
    long definitionsBefore = definitionCount();

    DocumentContext error =
        respond(
            create(
                createBody(infoLevel("attachment:" + hash(PNG_BYTES))),
                file("unused.txt", UNUSED_BYTES)),
            HttpStatus.BAD_REQUEST);

    assertThat(error.<String>read("$.message")).contains(hash(PNG_BYTES));
    assertThat(definitionCount()).isEqualTo(definitionsBefore);
    assertThat(storedHashes()).isEmpty();
  }

  @Test
  @DisplayName("files over the budget answer status 413 and leave no new file or link")
  void create_linkedFilesOverBudget_returnsPayloadTooLargeAndRollsBack() {
    byte[] stored = filler('a', NEARLY_THE_BUDGET);
    byte[] additional = filler('b', NEARLY_THE_BUDGET);
    respond(
        create(createBody(infoLevel("attachment:" + hash(stored))), file("stored.txt", stored)),
        HttpStatus.OK);
    long definitionsBefore = definitionCount();

    respond(
        create(
            createBody(infoLevel("attachment:" + hash(stored) + " attachment:" + hash(additional))),
            file("additional.txt", additional)),
        HttpStatus.PAYLOAD_TOO_LARGE);

    assertThat(definitionCount()).isEqualTo(definitionsBefore);
    assertThat(storedHashes()).containsExactly(hash(stored));
    assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM definition_attachment", Long.class))
        .isEqualTo(1L);
  }

  @Test
  @DisplayName("a save failing a level check after the files were read leaves no file or link")
  void create_levelFailsValidation_leavesNoFileOrLink() {
    long definitionsBefore = definitionCount();

    respond(
        create(
            """
            {"title": "Broken", "state": "UNRELEASED", "levels": [
              {"title": "Bad", "level_type": "INFO_LEVEL", "content": "attachment:%s",
               "estimated_duration": 5, "id": 1}]}
            """
                .formatted(hash(TEXT_BYTES)),
            file("notes.txt", TEXT_BYTES)),
        HttpStatus.BAD_REQUEST);

    assertThat(definitionCount()).isEqualTo(definitionsBefore);
    assertThat(storedHashes()).isEmpty();
  }

  private MockMultipartMvcRequestBuilder multipart(
      MockMultipartMvcRequestBuilder request, String definitionJson, MockMultipartFile... files) {
    request.file(
        new MockMultipartFile(
            "definition",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            definitionJson.getBytes(StandardCharsets.UTF_8)));
    Arrays.stream(files).forEach(request::file);
    return request;
  }

  private MockMultipartMvcRequestBuilder create(String definitionJson, MockMultipartFile... files) {
    return multipart(
        tester
            .post()
            .uri(DEFINITIONS)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER))
            .multipart(),
        definitionJson,
        files);
  }

  private MockMultipartMvcRequestBuilder update(
      Long definitionId, String definitionJson, MockMultipartFile... files) {
    return multipart(
        tester
            .put()
            .uri(DEFINITIONS + "/{id}", definitionId)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR))
            .multipart(),
        definitionJson,
        files);
  }

  private static DocumentContext respond(
      MockMultipartMvcRequestBuilder request, HttpStatus status) {
    return parse(request.exchange(), status);
  }

  private static DocumentContext respond(MockMvcRequestBuilder request, HttpStatus status) {
    return parse(request.exchange(), status);
  }

  private static String createBody(String... levels) {
    return createBodyKeeping("", levels);
  }

  private static String createBodyKeeping(String materialsJson, String... levels) {
    return """
        {"title": "With files", "state": "UNRELEASED", "materials": [%s], "levels": [%s]}
        """
        .formatted(materialsJson, String.join(",", levels));
  }

  private static String updateBody(DocumentContext created, String... levels) {
    return updateBodyKeeping(created, "", levels);
  }

  private static String updateBodyKeeping(
      DocumentContext created, String materialsJson, String... levels) {
    return """
        {"title": "Renamed", "description": "d", "prerequisites": [], "outcomes": [],
         "version": %d, "materials": [%s], "levels": [%s]}
        """
        .formatted(
            created.<Number>read("$.version").longValue(), materialsJson, String.join(",", levels));
  }

  private static String material(byte[] content, String fileName) {
    return """
        {"hash": "%s", "file_name": "%s"}
        """
        .formatted(hash(content), fileName);
  }

  private static String infoLevel(String content) {
    return """
        {"title": "Info", "level_type": "INFO_LEVEL", "content": "%s", "estimated_duration": 5}
        """
        .formatted(content);
  }

  private static String infoLevelWithId(DocumentContext created, String content) {
    return """
        {"id": %d, "title": "Info", "level_type": "INFO_LEVEL", "content": "%s",
         "estimated_duration": 5}
        """
        .formatted(created.<Number>read("$.levels[0].id").longValue(), content);
  }

  private static String trainingLevel(String solution) {
    return """
        {"title": "Training", "level_type": "TRAINING_LEVEL", "max_score": 50, "answer": "a",
         "content": "Training content", "solution": "%s", "solution_penalized": true,
         "incorrect_answer_limit": 3, "estimated_duration": 7, "variant_answers": false,
         "commands_required": false, "hints": [], "mitre_techniques": []}
        """
        .formatted(solution);
  }

  private String mediaTypeOf(String hash) {
    return jdbcTemplate.queryForObject(
        "SELECT media_type FROM attachment_file WHERE hash = ?", String.class, hash);
  }
}
