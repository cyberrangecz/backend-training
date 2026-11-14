package cz.cyberrange.platform.training.service.unit.services.attachment;

import static cz.cyberrange.platform.training.service.unit.services.attachment.ArchiveBytes.archive;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.cyberrange.platform.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.PayloadTooLargeException;
import cz.cyberrange.platform.training.service.export.ExportFormats;
import cz.cyberrange.platform.training.service.export.JsonExportFormat;
import cz.cyberrange.platform.training.service.export.YamlExportFormat;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentHashes;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentMediaTypes;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionArchive;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveReader;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveWriter;
import cz.cyberrange.platform.training.service.services.attachment.ExportedAttachment;
import cz.cyberrange.platform.training.service.services.attachment.SuppliedAttachment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

class DefinitionArchiveReaderTest {

  private static final String DOCUMENT = "{\"title\":\"Definition\",\"levels\":[]}";
  private static final String METADATA_OF_ONE =
      "{\"a.txt\":{\"fileName\":\"a.txt\",\"durationSeconds\":1.5,\"width\":2,\"height\":3,"
          + "\"poster\":null}}";

  private final JsonMapper jsonMapper =
      JsonMapper.builder()
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .build();
  private final YAMLMapper yamlMapper =
      YAMLMapper.builder()
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .build();
  private final ExportFormats exportFormats =
      new ExportFormats(new JsonExportFormat(jsonMapper), new YamlExportFormat(yamlMapper));
  private final Validator validator = mock(Validator.class);

  private DefinitionArchiveReader readerWithBudget(long budgetBytes) {
    when(validator.validate(org.mockito.ArgumentMatchers.<Object>any())).thenReturn(Set.of());
    return new DefinitionArchiveReader(
        jsonMapper, exportFormats, validator, DataSize.ofBytes(budgetBytes));
  }

  private DefinitionArchiveReader reader() {
    return readerWithBudget(1_000_000);
  }

  private static ArchiveBytes validArchive() {
    return archive()
        .entry("Definition.json", DOCUMENT)
        .entry("attachments.json", METADATA_OF_ONE)
        .entry("attachments/a.txt", "content");
  }

  private void assertRefused(byte[] archive) {
    assertThrows(BadRequestException.class, () -> reader().read(archive));
  }

  @Test
  void aWellFormedArchiveYieldsTheDefinitionAndTheFilesWithTheirMetadata() {
    DefinitionArchive read = reader().read(validArchive().build());

    assertEquals("Definition", read.definition().getTitle());
    SuppliedAttachment file = read.attachments().get(0);
    assertEquals("a.txt", file.fileName());
    assertArrayEquals("content".getBytes(StandardCharsets.UTF_8), file.content());
    assertEquals(AttachmentHashes.sha256Hex(file.content()), file.hash());
    assertEquals(1.5, file.metadata().durationSeconds());
    assertEquals(2, file.metadata().width());
    assertEquals(3, file.metadata().height());
    assertNull(file.metadata().poster());
    assertEquals(file.hash(), file.metadata().hash());
  }

  @Test
  void aYamlDocumentIsReadByItsExtension() {
    DefinitionArchive read =
        reader()
            .read(
                archive()
                    .entry("x.yaml", "title: Yaml definition\nlevels: []\n")
                    .entry("attachments.json", "{}")
                    .build());

    assertEquals("Yaml definition", read.definition().getTitle());
    assertTrue(read.attachments().isEmpty());
  }

  @Test
  void theWriterAndTheReaderRoundTripNamesClashesAndMetadata() throws Exception {
    byte[] poster = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1};
    List<ExportedAttachment> exported =
        List.of(
            exportedFile("diagram.png", "one", 4.5, 10, 20, poster),
            exportedFile("diagram.png", "two", null, null, null, null),
            exportedFile("Diagram.PNG", "three", null, null, null, null),
            exportedFile("diagram (1).png", "four", null, null, null, null));
    ExportTrainingDefinitionAndLevelsDTO document = new ExportTrainingDefinitionAndLevelsDTO();
    document.setTitle("Round trip");
    JsonExportFormat format = new JsonExportFormat(jsonMapper);
    DefinitionArchiveWriter writer =
        new DefinitionArchiveWriter(jsonMapper, new AttachmentMediaTypes());

    byte[] archive = writer.write("Round trip", format, format.writeDocument(document), exported);
    DefinitionArchive read = reader().read(archive);

    assertEquals("Round trip", read.definition().getTitle());
    assertEquals(
        List.of("diagram.png", "diagram.png", "Diagram.PNG", "diagram (1).png"),
        read.attachments().stream().map(SuppliedAttachment::fileName).toList());
    assertEquals(
        List.of("one", "two", "three", "four"),
        read.attachments().stream()
            .map(file -> new String(file.content(), StandardCharsets.UTF_8))
            .toList());
    assertEquals(4.5, read.attachments().get(0).metadata().durationSeconds());
    assertEquals(10, read.attachments().get(0).metadata().width());
    assertEquals(20, read.attachments().get(0).metadata().height());
    assertArrayEquals(poster, read.attachments().get(0).metadata().poster());
  }

  @Test
  void anArchiveWithoutADefinitionDocumentIsRefused() {
    assertRefused(archive().entry("attachments.json", "{}").build());
  }

  @Test
  void anArchiveWithTwoDefinitionDocumentsIsRefused() {
    assertRefused(validArchive().entry("Other.yaml", "title: x").build());
  }

  @Test
  void anArchiveWithoutTheMetadataFileIsRefused() {
    assertRefused(archive().entry("Definition.json", DOCUMENT).build());
  }

  @Test
  void anAttachmentFileThatTheMetadataDoesNotListIsRefused() {
    assertRefused(validArchive().entry("attachments/b.txt", "more").build());
  }

  @Test
  void aListedEntryWithoutAFileIsRefused() {
    assertRefused(
        archive()
            .entry("Definition.json", DOCUMENT)
            .entry("attachments.json", METADATA_OF_ONE)
            .build());
  }

  @Test
  void aFolderEntryIsRefused() {
    assertRefused(validArchive().entry("attachments/", new byte[0]).build());
  }

  @Test
  void anyOtherEntryIsRefused() {
    assertRefused(validArchive().entry("readme.txt", "hello").build());
    assertRefused(validArchive().entry("other/Definition.json", DOCUMENT).build());
  }

  @Test
  void anEntryNestedBelowTheAttachmentFolderIsRefused() {
    assertRefused(validArchive().entry("attachments/sub/b.txt", "x").build());
    assertRefused(validArchive().entry("attachments/../../evil.txt", "x").build());
    assertRefused(validArchive().entry("attachments/..\\evil.txt/", "x").build());
  }

  @Test
  void entryNamesThatDifferOnlyInCaseCountAsDuplicates() {
    assertRefused(validArchive().entry("ATTACHMENTS/A.TXT", "x").build());
    assertRefused(validArchive().entry("attachments/A.txt", "x").build());
  }

  @Test
  void anEntryNamedLikeAParentFolderIsOnlyALabelAndNeverAPath() {
    DefinitionArchive read =
        reader()
            .read(
                archive()
                    .entry("Definition.json", DOCUMENT)
                    .entry("attachments.json", "{\"..\":{\"fileName\":\"../../etc/passwd\"}}")
                    .entry("attachments/..", "x")
                    .build());

    assertEquals("....etcpasswd", read.attachments().get(0).fileName());
  }

  @Test
  void aBlankFileNameInTheMetadataLeavesTheStoredNameUnset() {
    DefinitionArchive read =
        reader()
            .read(
                archive()
                    .entry("Definition.json", DOCUMENT)
                    .entry("attachments.json", "{\"a.txt\":{\"fileName\":\"//\"}}")
                    .entry("attachments/a.txt", "x")
                    .build());

    assertNull(read.attachments().get(0).fileName());
  }

  @Test
  void aFileNameLongerThanTheLimitIsRefused() {
    assertRefused(
        archive()
            .entry("Definition.json", DOCUMENT)
            .entry("attachments.json", "{\"a.txt\":{\"fileName\":\"" + "n".repeat(256) + "\"}}")
            .entry("attachments/a.txt", "x")
            .build());
  }

  @Test
  void metadataWithAnUnknownFieldIsRefused() {
    assertRefused(
        archive()
            .entry("Definition.json", DOCUMENT)
            .entry("attachments.json", "{\"a.txt\":{\"fileName\":\"a\",\"sizeBytes\":1}}")
            .entry("attachments/a.txt", "x")
            .build());
  }

  @Test
  void metadataThatIsNotAMappingOfEntriesIsRefused() {
    for (String metadata : new String[] {"[]", "null", "{", "{\"a.txt\":null}", "{\"a.txt\":5}"}) {
      assertRefused(
          archive()
              .entry("Definition.json", DOCUMENT)
              .entry("attachments.json", metadata)
              .entry("attachments/a.txt", "x")
              .build());
    }
  }

  @Test
  void aPosterThatIsNotBase64IsRefused() {
    assertRefused(
        archive()
            .entry("Definition.json", DOCUMENT)
            .entry("attachments.json", "{\"a.txt\":{\"poster\":\"not base64 !\"}}")
            .entry("attachments/a.txt", "x")
            .build());
  }

  @Test
  void aDefinitionDocumentThatCannotBeParsedIsRefused() {
    assertRefused(archive().entry("Definition.json", "{").entry("attachments.json", "{}").build());
  }

  @Test
  void aDefinitionDocumentThatFailsValidationRaisesTheConstraintViolation() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    doReturn(Set.of(violation))
        .when(validator)
        .validate(org.mockito.ArgumentMatchers.<Object>any());
    DefinitionArchiveReader strictReader =
        new DefinitionArchiveReader(
            jsonMapper, exportFormats, validator, DataSize.ofBytes(1_000_000));

    assertThrows(
        ConstraintViolationException.class, () -> strictReader.read(validArchive().build()));
  }

  @Test
  void bytesThatAreNotAZipArchiveAreRefused() {
    assertRefused("not a zip".getBytes(StandardCharsets.UTF_8));
    assertRefused(new byte[0]);
  }

  @Test
  void moreEntriesThanTheCapAreRefused() {
    ArchiveBytes tooMany = archive().entry("Definition.json", DOCUMENT);
    for (int index = 0; index < 1000; index++) {
      tooMany.entry("attachments/file" + index + ".txt", "");
    }
    tooMany.entry("attachments.json", "{}");

    assertRefused(tooMany.build());
  }

  @Test
  void unpackedBytesAboveTheBudgetAreRefusedWhateverTheHeadersDeclare() {
    byte[] archive = validArchive().entry("attachments/big.bin", new byte[2000]).build();

    assertThrows(PayloadTooLargeException.class, () -> readerWithBudget(1000).read(archive));
  }

  @Test
  void aHighlyCompressedBombIsRefusedAsSoonAsTheBudgetIsPassed() {
    byte[] archive =
        archive()
            .entry("Definition.json", DOCUMENT)
            .entry("attachments.json", "{\"bomb\":{}}")
            .entry("attachments/bomb", new byte[50 * 1024 * 1024])
            .build();

    assertTrue(archive.length < 1024 * 1024);
    assertThrows(PayloadTooLargeException.class, () -> readerWithBudget(1000).read(archive));
  }

  @Test
  void aMetadataFileAboveItsCapIsRefusedBeforeParsing() {
    byte[] oversized = new byte[16 * 1024 * 1024 + 1];
    java.util.Arrays.fill(oversized, (byte) ' ');
    byte[] archive =
        archive().entry("Definition.json", DOCUMENT).entry("attachments.json", oversized).build();

    assertThrows(
        BadRequestException.class, () -> readerWithBudget(64L * 1024 * 1024).read(archive));
  }

  @Test
  void aBase64PosterIsDecoded() {
    byte[] poster = {1, 2, 3};
    DefinitionArchive read =
        reader()
            .read(
                archive()
                    .entry("Definition.json", DOCUMENT)
                    .entry(
                        "attachments.json",
                        "{\"a.txt\":{\"poster\":\""
                            + Base64.getEncoder().encodeToString(poster)
                            + "\"}}")
                    .entry("attachments/a.txt", "x")
                    .build());

    assertArrayEquals(poster, read.attachments().get(0).metadata().poster());
  }

  @Test
  void aFileRepeatedUnderSeveralNamesCountsOnceAgainstTheBudget() {
    byte[] content = new byte[600];
    String metadata = "{\"a.bin\":{\"fileName\":\"a.bin\"},\"b.bin\":{\"fileName\":\"b.bin\"}}";
    byte[] archive =
        archive()
            .entry("Definition.json", DOCUMENT)
            .entry("attachments.json", metadata)
            .entry("attachments/a.bin", content)
            .entry("attachments/b.bin", content)
            .build();

    assertEquals(2, readerWithBudget(1000).read(archive).attachments().size());
  }

  @Test
  void theDocumentAndTheMetadataDoNotCountAgainstTheBudget() {
    byte[] archive =
        archive()
            .entry("Definition.json", DOCUMENT)
            .entry("attachments.json", "{\"a.bin\":{\"fileName\":\"a.bin\"}}")
            .entry("attachments/a.bin", new byte[1000])
            .build();

    assertEquals(1, readerWithBudget(1000).read(archive).attachments().size());
  }

  @Test
  void aDefinitionDocumentAboveItsCapIsRefused() {
    byte[] oversized = new byte[16 * 1024 * 1024 + 1];
    java.util.Arrays.fill(oversized, (byte) ' ');
    byte[] archive =
        archive().entry("Definition.json", oversized).entry("attachments.json", "{}").build();

    assertRefused(archive);
  }

  @Test
  void anEntryNameThatIsNotValidUtf8IsRefusedAsBadRequest() {
    byte[] malformed = archive().entry("XX.json", DOCUMENT).build();
    for (int index = 0; index < malformed.length - 1; index++) {
      if (malformed[index] == 'X' && malformed[index + 1] == 'X') {
        malformed[index] = (byte) 0xC3;
        malformed[index + 1] = 0x28;
      }
    }

    assertRefused(malformed);
  }

  private static ExportedAttachment exportedFile(
      String fileName,
      String content,
      Double duration,
      Integer width,
      Integer height,
      byte[] poster) {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    return new ExportedAttachment(
        AttachmentHashes.sha256Hex(bytes),
        fileName,
        "text/plain",
        bytes,
        duration,
        width,
        height,
        poster);
  }
}
