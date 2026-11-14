package cz.cyberrange.platform.training.rest.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Integration tests of how clone, delete, export and import keep a definition's attachment files
 * and links consistent. The attachment budget of the test context is 1 MB.
 */
@DisplayName("Training definition lifecycle with attachments")
class TrainingDefinitionAttachmentLifecycleIT extends AbstractAttachmentIntegrationTest {

  private static final String EXPORT = "/exports/training-definitions/{id}";
  private static final String IMPORT = "/imports/training-definitions";
  private static final long CALLER_REF_ID = 730_001L;

  private static final byte[] CONFIG_BYTES = "key: value\n".getBytes(StandardCharsets.UTF_8);

  private MockMvcTester tester;

  @BeforeEach
  void prepareTester() {
    tester = MockMvcTester.create(mockMvc);
    stubLoggedInUser(CALLER_REF_ID);
  }

  @Test
  @DisplayName("an exported archive imported into a fresh definition renders the same references")
  void exportImport_definitionWithImageAndConfigFile_roundTripsFilesIntoFreshDefinition()
      throws IOException {
    Long originalId = createWithImageAndConfig();
    byte[] archive = exportArchive(originalId);
    Map<String, byte[]> entries = unzip(archive);
    assertThat(entries.keySet())
        .containsExactlyInAnyOrder(
            "With files.json",
            "attachments.json",
            "attachments/diagram.png",
            "attachments/settings.yml");
    assertThat(entries.get("attachments/diagram.png")).isEqualTo(PNG_BYTES);
    assertThat(new String(entries.get("With files.json"), StandardCharsets.UTF_8))
        .doesNotContain("\"attachments\"");
    delete(originalId);
    assertThat(storedHashes()).isEmpty();

    DocumentContext imported = importArchive(archive, HttpStatus.OK);

    Long importedId = imported.<Number>read("$.id").longValue();
    assertThat(importedId).isNotEqualTo(originalId);
    assertThat(imported.<String>read("$.levels[0].content")).contains(hash(PNG_BYTES));
    assertThat(imported.<String>read("$.levels[1].solution")).contains(hash(CONFIG_BYTES));
    assertThat(linkedFileNames(importedId))
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(hash(PNG_BYTES), "diagram.png", hash(CONFIG_BYTES), "settings.yml"));
    assertThat(download(importedId, hash(PNG_BYTES))).isEqualTo(PNG_BYTES);
    assertThat(download(importedId, hash(CONFIG_BYTES))).isEqualTo(CONFIG_BYTES);
  }

  @Test
  @DisplayName("an archive holding a file its metadata does not list answers 400")
  void importArchive_unlistedFile_returnsBadRequestAndStoresNothing() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    entries.put("attachments/extra.txt", CONFIG_BYTES);

    importArchive(zip(entries), HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive whose metadata lists a file it does not hold answers 400")
  void importArchive_listedFileMissing_returnsBadRequestAndStoresNothing() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    entries.remove("attachments/settings.yml");

    importArchive(zip(entries), HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive without its metadata file answers 400")
  void importArchive_noMetadataFile_returnsBadRequestAndStoresNothing() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    entries.remove("attachments.json");

    importArchive(zip(entries), HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive with a second document at its root answers 400")
  void importArchive_secondDocument_returnsBadRequestAndStoresNothing() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    entries.put("other.json", entries.get("With files.json"));

    importArchive(zip(entries), HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive entry named with a traversal path answers 400 and is never written")
  void importArchive_traversalEntryNames_returnBadRequestAndStoreNothing() throws IOException {
    for (String name :
        List.of("../evil.json", "attachments/../evil.txt", "/etc/passwd", "attachments/a/b.txt")) {
      Map<String, byte[]> entries = exportedEntriesThenWipe();
      entries.put(name, CONFIG_BYTES);

      importArchive(zip(entries), HttpStatus.BAD_REQUEST);

      assertNothingImported();
    }
  }

  @Test
  @DisplayName("an archive with a folder entry answers 400")
  void importArchive_folderEntry_returnsBadRequest() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    entries.put("attachments/", new byte[0]);

    importArchive(zip(entries), HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive unpacking beyond the attachment budget answers 413")
  void importArchive_unpackedBytesOverBudget_returnsPayloadTooLarge() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    entries.put("attachments/settings.yml", new byte[2 * 1024 * 1024]);

    byte[] archive = zip(entries);
    assertThat(archive.length).isLessThan(1024 * 1024);
    importArchive(archive, HttpStatus.PAYLOAD_TOO_LARGE);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive whose metadata holds an unknown field answers 400")
  void importArchive_unknownMetadataField_returnsBadRequest() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    String metadata = new String(entries.get("attachments.json"), StandardCharsets.UTF_8);
    entries.put(
        "attachments.json",
        metadata.replaceFirst("\\{", "{\"unknown\": 1,").getBytes(StandardCharsets.UTF_8));

    importArchive(zip(entries), HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive referencing a file it does not hold answers 400")
  void importArchive_danglingReference_returnsBadRequest() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    entries.keySet().removeIf(name -> name.startsWith("attachments/"));
    entries.put("attachments.json", "{}".getBytes(StandardCharsets.UTF_8));

    importArchive(zip(entries), HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive repeating one file under several names counts that file once")
  void importArchive_duplicatedFile_countsDistinctBytesOnly() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();
    String metadata = new String(entries.get("attachments.json"), StandardCharsets.UTF_8);
    StringBuilder extended = new StringBuilder(metadata.substring(0, metadata.length() - 1));
    for (String name : List.of("first.bin", "second.bin")) {
      extended.append(",\"").append(name).append("\":{\"fileName\":\"").append(name).append("\"}");
      entries.put("attachments/" + name, new byte[600 * 1024]);
    }
    entries.put(
        "attachments.json", extended.append('}').toString().getBytes(StandardCharsets.UTF_8));

    importArchive(zip(entries), HttpStatus.OK);

    assertThat(storedHashes()).hasSize(3);
  }

  @Test
  @DisplayName("a request body over the budget plus the margin answers 413 without being read")
  void importArchive_bodyOverBudgetPlusMargin_returnsPayloadTooLarge() {
    importArchive(new byte[34 * 1024 * 1024], HttpStatus.PAYLOAD_TOO_LARGE);

    assertNothingImported();
  }

  @Test
  @DisplayName("an archive entry name that is not valid UTF-8 answers 400")
  void importArchive_malformedEntryName_returnsBadRequest() throws IOException {
    byte[] archive = zip(Map.of("XX.json", "{}".getBytes(StandardCharsets.UTF_8)));
    byte[] malformed = archive.clone();
    for (int index = 0; index < malformed.length - 1; index++) {
      if (malformed[index] == 'X' && malformed[index + 1] == 'X') {
        malformed[index] = (byte) 0xC3;
        malformed[index + 1] = 0x28;
      }
    }

    importArchive(malformed, HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("a plain JSON import referencing a stored but unsupplied file answers 400")
  void importDefinition_referenceToStoredFileNotSupplied_returnsBadRequest() throws IOException {
    Map<String, byte[]> entries = unzip(exportArchive(createWithImageAndConfig()));

    importDefinition(
        new String(entries.get("With files.json"), StandardCharsets.UTF_8), HttpStatus.BAD_REQUEST);

    assertThat(definitionCount()).isEqualTo(1);
    assertThat(storedHashes()).hasSize(2);
  }

  @Test
  @DisplayName("a plain JSON import referencing a file nobody supplied or stored answers 400")
  void importDefinition_referenceToUnknownFile_returnsBadRequest() throws IOException {
    Map<String, byte[]> entries = exportedEntriesThenWipe();

    importDefinition(
        new String(entries.get("With files.json"), StandardCharsets.UTF_8), HttpStatus.BAD_REQUEST);

    assertNothingImported();
  }

  @Test
  @DisplayName("a clone links the same files under the same names without storing any file")
  void clone_definitionWithFiles_sharesFilesAndLinksWithoutNewFileRows() {
    Long originalId = createWithImageAndConfig();

    DocumentContext clone =
        parse(
            tester
                .post()
                .uri(DEFINITIONS + "/{id}?title=Copy", originalId)
                .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR))
                .exchange(),
            HttpStatus.OK);

    Long cloneId = clone.<Number>read("$.id").longValue();
    assertThat(linkedFileNames(cloneId)).isEqualTo(linkedFileNames(originalId));
    assertThat(storedHashes()).hasSize(2);
    assertThat(download(cloneId, hash(PNG_BYTES))).isEqualTo(PNG_BYTES);
  }

  @Test
  @DisplayName("a delete keeps a file another definition still links and removes the last copy")
  void delete_fileSharedByTwoDefinitions_keepsFileUntilLastLinkIsGone() {
    Long first = createWithImageAndConfig();
    Long second = createWithImageAndConfig();
    assertThat(storedHashes()).hasSize(2);

    delete(first);

    assertThat(storedHashes()).containsExactlyInAnyOrder(hash(PNG_BYTES), hash(CONFIG_BYTES));
    assertThat(linkedFileNames(first)).isEmpty();
    assertThat(linkedFileNames(second)).hasSize(2);

    delete(second);

    assertThat(storedHashes()).isEmpty();
  }

  @Test
  @DisplayName("a material no content references is cloned, exported and imported as a material")
  void cloneExportImport_storedOnlyMaterial_isCarriedAlong() throws IOException {
    Long originalId = createWithMaterialOnly();

    DocumentContext clone =
        parse(
            tester
                .post()
                .uri(DEFINITIONS + "/{id}?title=Copy", originalId)
                .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR))
                .exchange(),
            HttpStatus.OK);
    assertThat(linkedFileNames(clone.<Number>read("$.id").longValue()))
        .containsExactlyEntriesOf(Map.of(hash(CONFIG_BYTES), "handout.yml"));

    byte[] archive = exportArchive(originalId);
    assertThat(unzip(archive).keySet()).contains("attachments/handout.yml");
    wipeDefinitions();
    Long importedId = importArchive(archive, HttpStatus.OK).<Number>read("$.id").longValue();

    assertThat(linkedFileNames(importedId))
        .containsExactlyEntriesOf(Map.of(hash(CONFIG_BYTES), "handout.yml"));
    assertThat(storedHashes()).containsExactly(hash(CONFIG_BYTES));
  }

  @Test
  @DisplayName("a delete keeps a material file another definition still links")
  void delete_materialSharedByTwoDefinitions_keepsFileUntilLastLinkIsGone() {
    Long first = createWithMaterialOnly();
    Long second = createWithMaterialOnly();

    delete(first);

    assertThat(storedHashes()).containsExactly(hash(CONFIG_BYTES));
    assertThat(linkedFileNames(second)).hasSize(1);

    delete(second);

    assertThat(storedHashes()).isEmpty();
  }

  private Long createWithMaterialOnly() {
    String body =
        """
        {"title": "With material", "state": "UNRELEASED",
         "materials": [{"hash": "%s", "file_name": "handout.yml"}], "levels": [
          {"title": "Info", "level_type": "INFO_LEVEL", "estimated_duration": 5,
           "content": "No reference"}]}
        """
            .formatted(hash(CONFIG_BYTES));
    return parse(
            tester
                .post()
                .uri(DEFINITIONS)
                .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER))
                .multipart()
                .file(
                    new MockMultipartFile(
                        "definition",
                        "",
                        MediaType.APPLICATION_JSON_VALUE,
                        body.getBytes(StandardCharsets.UTF_8)))
                .file(file("handout.yml", CONFIG_BYTES))
                .exchange(),
            HttpStatus.OK)
        .<Number>read("$.id")
        .longValue();
  }

  private Long createWithImageAndConfig() {
    String body =
        """
        {"title": "With files", "state": "UNRELEASED", "levels": [
          {"title": "Info", "level_type": "INFO_LEVEL", "estimated_duration": 5,
           "content": "![image](attachment:%s)"},
          {"title": "Training", "level_type": "TRAINING_LEVEL", "max_score": 50, "answer": "a",
           "content": "Training content", "solution": "Use attachment:%s",
           "solution_penalized": true, "incorrect_answer_limit": 3, "estimated_duration": 7,
           "variant_answers": false, "commands_required": false, "hints": [],
           "mitre_techniques": []}]}
        """
            .formatted(hash(PNG_BYTES), hash(CONFIG_BYTES));
    DocumentContext created =
        parse(
            tester
                .post()
                .uri(DEFINITIONS)
                .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER))
                .multipart()
                .file(
                    new MockMultipartFile(
                        "definition",
                        "",
                        MediaType.APPLICATION_JSON_VALUE,
                        body.getBytes(StandardCharsets.UTF_8)))
                .file(file("diagram.png", PNG_BYTES))
                .file(file("settings.yml", CONFIG_BYTES))
                .exchange(),
            HttpStatus.OK);
    return created.<Number>read("$.id").longValue();
  }

  private byte[] exportArchive(Long definitionId) {
    MvcTestResult result =
        tester
            .get()
            .uri(EXPORT, definitionId)
            .accept(MediaType.ALL)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR))
            .exchange();
    assertThat(result).hasStatusOk().hasContentType(MediaType.parseMediaType("application/zip"));
    assertThat(result.getResponse().getHeader("Content-Disposition")).contains(".zip\"");
    return result.getResponse().getContentAsByteArray();
  }

  private Map<String, byte[]> exportedEntriesThenWipe() throws IOException {
    Map<String, byte[]> entries = unzip(exportArchive(createWithImageAndConfig()));
    wipeDefinitions();
    return entries;
  }

  private void wipeDefinitions() {
    jdbcTemplate.execute(
        "TRUNCATE TABLE training_definition, definition_attachment, attachment_file CASCADE");
  }

  private void assertNothingImported() {
    assertThat(definitionCount()).isZero();
    assertThat(storedHashes()).isEmpty();
  }

  private DocumentContext importArchive(byte[] archive, HttpStatus expectedStatus) {
    return parse(
        tester
            .post()
            .uri(IMPORT)
            .contentType(MediaType.parseMediaType("application/zip"))
            .accept(MediaType.APPLICATION_JSON)
            .content(archive)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER))
            .exchange(),
        expectedStatus);
  }

  private DocumentContext importDefinition(String body, HttpStatus expectedStatus) {
    return parse(
        tester
            .post()
            .uri(IMPORT)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(body)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_DESIGNER))
            .exchange(),
        expectedStatus);
  }

  private static Map<String, byte[]> unzip(byte[] archive) throws IOException {
    Map<String, byte[]> entries = new LinkedHashMap<>();
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        entries.put(entry.getName(), zip.readAllBytes());
      }
    }
    return entries;
  }

  private static byte[] zip(Map<String, byte[]> entries) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        zip.putNextEntry(new ZipEntry(entry.getKey()));
        zip.write(entry.getValue());
        zip.closeEntry();
      }
    }
    return bytes.toByteArray();
  }

  private void delete(Long definitionId) {
    assertThat(
            tester
                .delete()
                .uri(DEFINITIONS + "/{id}", definitionId)
                .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR))
                .exchange())
        .hasStatusOk();
  }

  private byte[] download(Long definitionId, String hash) {
    MvcTestResult result =
        tester
            .get()
            .uri(DEFINITIONS + "/{id}/attachments/{hash}", definitionId, hash)
            .with(callerWithRoles(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR))
            .exchange();
    assertThat(result).hasStatusOk();
    return result.getResponse().getContentAsByteArray();
  }

  private Long onlyDefinitionId() {
    return jdbcTemplate.queryForObject("SELECT id FROM training_definition", Long.class);
  }
}
