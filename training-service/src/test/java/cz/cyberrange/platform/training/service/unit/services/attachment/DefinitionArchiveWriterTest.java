package cz.cyberrange.platform.training.service.unit.services.attachment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.service.export.JsonExportFormat;
import cz.cyberrange.platform.training.service.export.YamlExportFormat;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentMediaTypes;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveWriter;
import cz.cyberrange.platform.training.service.services.attachment.ExportedAttachment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

class DefinitionArchiveWriterTest {

  private final JsonMapper jsonMapper = JsonMapper.builder().build();
  private final DefinitionArchiveWriter writer =
      new DefinitionArchiveWriter(jsonMapper, new AttachmentMediaTypes());

  private static Map<String, byte[]> unzip(byte[] archive) throws IOException {
    Map<String, byte[]> entries = new LinkedHashMap<>();
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        entries.put(entry.getName(), zip.readAllBytes());
      }
    }
    return entries;
  }

  private static ExportedAttachment file(String fileName, String hash, String content) {
    return new ExportedAttachment(
        hash,
        fileName,
        "image/png",
        content.getBytes(StandardCharsets.UTF_8),
        null,
        null,
        null,
        null);
  }

  @Test
  void archiveNameIsTheCleanedTitleOrADefaultWhenNothingIsLeft() {
    assertEquals("My training", writer.archiveNameOf("My: training/"));
    assertEquals("training-definition", writer.archiveNameOf(null));
    assertEquals("training-definition", writer.archiveNameOf(" ../ "));
  }

  @Test
  void theArchiveHoldsTheDocumentTheMetadataAndOneEntryPerFile() throws Exception {
    byte[] archive =
        writer.write(
            "Name",
            new JsonExportFormat(jsonMapper),
            "{}".getBytes(StandardCharsets.UTF_8),
            List.of(file("a.png", "h1", "one")));

    Map<String, byte[]> entries = unzip(archive);

    assertEquals(
        List.of("Name.json", "attachments/a.png", "attachments.json"),
        new ArrayList<>(entries.keySet()));
    assertArrayEquals("one".getBytes(StandardCharsets.UTF_8), entries.get("attachments/a.png"));
    assertEquals(
        "{\"a.png\":{\"fileName\":\"a.png\",\"durationSeconds\":null,\"width\":null,"
            + "\"height\":null,\"poster\":null}}",
        new String(entries.get("attachments.json"), StandardCharsets.UTF_8));
  }

  @Test
  void aYamlDocumentCarriesTheYamlExtension() throws Exception {
    byte[] archive =
        writer.write(
            "Name",
            new YamlExportFormat(YAMLMapper.builder().build()),
            new byte[0],
            List.of(file("a.png", "h1", "one")));

    assertEquals(true, unzip(archive).containsKey("Name.yaml"));
  }

  @Test
  void entryNamesAreCleanedUniqueIgnoringCaseAndFallBackToTheHashWithAnExtension()
      throws Exception {
    byte[] archive =
        writer.write(
            "Name",
            new JsonExportFormat(jsonMapper),
            new byte[0],
            List.of(
                file("diagram.png", "h1", "1"),
                file("Diagram.png", "h2", "2"),
                file("../../evil.png", "h3", "3"),
                file("..", "h4", "4"),
                file(null, "h5", "5")));

    List<String> names = new ArrayList<>(unzip(archive).keySet());

    assertEquals(
        List.of(
            "Name.json",
            "attachments/diagram.png",
            "attachments/Diagram (1).png",
            "attachments/....evil.png",
            "attachments/h4.png",
            "attachments/h5.png",
            "attachments.json"),
        names);
  }

  @Test
  void aDocumentThatWouldBeNamedLikeTheMetadataFileIsNumbered() throws Exception {
    byte[] archive =
        writer.write(
            "attachments",
            new JsonExportFormat(jsonMapper),
            new byte[0],
            List.of(file("a.png", "h1", "1")));

    assertEquals(
        List.of("attachments (1).json", "attachments/a.png", "attachments.json"),
        new ArrayList<>(unzip(archive).keySet()));
  }

  @Test
  void aPosterIsWrittenAsBase64InTheMetadata() throws Exception {
    ExportedAttachment withPoster =
        new ExportedAttachment(
            "h1", "clip.mp4", "video/mp4", new byte[] {1}, 2.5, 640, 360, new byte[] {7, 8});

    byte[] archive =
        writer.write("Name", new JsonExportFormat(jsonMapper), new byte[0], List.of(withPoster));

    String metadata = new String(unzip(archive).get("attachments.json"), StandardCharsets.UTF_8);
    assertEquals(
        "{\"clip.mp4\":{\"fileName\":\"clip.mp4\",\"durationSeconds\":2.5,\"width\":640,"
            + "\"height\":360,\"poster\":\""
            + Base64.getEncoder().encodeToString(new byte[] {7, 8})
            + "\"}}",
        metadata);
  }

  @Test
  void moreFilesThanAnImportAcceptsAreRefused() {
    List<ExportedAttachment> tooMany = new ArrayList<>();
    for (int index = 0; index < 999; index++) {
      tooMany.add(file("f" + index + ".png", "h" + index, "c"));
    }

    assertThrows(
        BadRequestException.class,
        () -> writer.write("Name", new JsonExportFormat(jsonMapper), new byte[0], tooMany));
  }

  @Test
  void theMostFilesAnImportAcceptsAreWritten() throws Exception {
    List<ExportedAttachment> most = new ArrayList<>();
    for (int index = 0; index < 998; index++) {
      most.add(file("f" + index + ".png", "h" + index, "c"));
    }

    byte[] archive = writer.write("Name", new JsonExportFormat(jsonMapper), new byte[0], most);

    assertEquals(1000, unzip(archive).size());
  }

  @Test
  void metadataLargerThanAnImportAcceptsIsRefused() {
    byte[] poster = new byte[1024 * 1024];
    List<ExportedAttachment> withPosters = new ArrayList<>();
    for (int index = 0; index < 13; index++) {
      withPosters.add(
          new ExportedAttachment(
              "h" + index, "v" + index + ".mp4", "video/mp4", new byte[] {1}, 1.0, 1, 1, poster));
    }

    assertThrows(
        BadRequestException.class,
        () -> writer.write("Name", new JsonExportFormat(jsonMapper), new byte[0], withPosters));
  }

  @Test
  void anEntryNameLongerThanAZipEntryNameMayBeIsRefused() {
    String tooLongName = "a".repeat(70_000);

    assertThrows(
        BadRequestException.class,
        () ->
            writer.write(
                tooLongName,
                new JsonExportFormat(jsonMapper),
                new byte[0],
                List.of(file("a.png", "h", "c"))));
  }
}
