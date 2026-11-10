package cz.cyberrange.platform.training.service.unit.services.attachment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import cz.cyberrange.platform.training.api.dto.attachment.AttachmentListDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentSummaryDTO;
import cz.cyberrange.platform.training.persistence.model.AttachmentFile;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachment;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachmentId;
import cz.cyberrange.platform.training.persistence.repository.AttachmentFileRepository;
import cz.cyberrange.platform.training.persistence.repository.AttachmentFileRepository.FileBytes;
import cz.cyberrange.platform.training.persistence.repository.DefinitionAttachmentRepository;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionAttachmentReader;
import cz.cyberrange.platform.training.service.services.attachment.ExportedAttachment;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;

@ExtendWith(MockitoExtension.class)
class DefinitionAttachmentReaderTest {

  private static final long DEFINITION_ID = 7L;
  private static final String HASH = "a".repeat(64);
  private static final byte[] CONTENT = "plain text".getBytes(StandardCharsets.UTF_8);

  @Mock private AttachmentFileRepository attachmentFileRepository;
  @Mock private DefinitionAttachmentRepository definitionAttachmentRepository;

  private DefinitionAttachmentReader reader;

  @BeforeEach
  void setUp() {
    reader =
        new DefinitionAttachmentReader(
            attachmentFileRepository, definitionAttachmentRepository, DataSize.ofBytes(1000));
  }

  @Test
  void listDescribesEveryLinkWithPosterPresenceBudgetAndUsedBytes() {
    DefinitionAttachment link =
        new DefinitionAttachment(new DefinitionAttachmentId(DEFINITION_ID, HASH), "clip.mp4");
    AttachmentFile file = new AttachmentFile();
    file.setHash(HASH);
    file.setMediaType("video/mp4");
    file.setSizeBytes(300);
    file.setDurationSeconds(12.5);
    file.setWidth(640);
    file.setHeight(360);
    file.setPosterMediaType("image/png");
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(List.of(link));
    given(attachmentFileRepository.findAllById(List.of(HASH))).willReturn(List.of(file));
    given(definitionAttachmentRepository.sumFileSizesByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(300L);

    AttachmentListDTO listed = reader.list(DEFINITION_ID);

    assertEquals(
        new AttachmentListDTO(
            List.of(
                new AttachmentSummaryDTO(HASH, "clip.mp4", "video/mp4", 300, 12.5, 640, 360, true)),
            1000,
            300),
        listed);
  }

  @Test
  void readAllForExportReadsEveryLinkedFileWithItsLinkNameAndMediaType() {
    DefinitionAttachment link =
        new DefinitionAttachment(new DefinitionAttachmentId(DEFINITION_ID, HASH), "notes.txt");
    AttachmentFile file = new AttachmentFile();
    file.setHash(HASH);
    file.setMediaType("text/plain");
    file.setSizeBytes(CONTENT.length);
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(List.of(link));
    given(attachmentFileRepository.findAllById(List.of(HASH))).willReturn(List.of(file));
    given(attachmentFileRepository.findBytesByHashIn(Set.of(HASH)))
        .willReturn(List.of(fileBytes(CONTENT, null)));

    List<ExportedAttachment> exported = reader.readAllForExport(DEFINITION_ID);

    assertEquals(1, exported.size());
    ExportedAttachment only = exported.get(0);
    assertEquals(HASH, only.hash());
    assertEquals("notes.txt", only.fileName());
    assertEquals("text/plain", only.mediaType());
    assertArrayEquals(CONTENT, only.content());
    assertNull(only.poster());
  }

  @Test
  void readAllForExportCarriesDurationDimensionsAndPoster() {
    DefinitionAttachment link =
        new DefinitionAttachment(new DefinitionAttachmentId(DEFINITION_ID, HASH), "clip.mp4");
    AttachmentFile file = new AttachmentFile();
    file.setHash(HASH);
    file.setMediaType("video/mp4");
    file.setSizeBytes(CONTENT.length);
    file.setDurationSeconds(3.5);
    file.setWidth(640);
    file.setHeight(360);
    byte[] poster = {1, 2, 3};
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(List.of(link));
    given(attachmentFileRepository.findAllById(List.of(HASH))).willReturn(List.of(file));
    given(attachmentFileRepository.findBytesByHashIn(Set.of(HASH)))
        .willReturn(List.of(fileBytes(CONTENT, poster)));

    ExportedAttachment exported = reader.readAllForExport(DEFINITION_ID).get(0);

    assertEquals(3.5, exported.durationSeconds());
    assertEquals(640, exported.width());
    assertEquals(360, exported.height());
    assertArrayEquals(poster, exported.poster());
  }

  @Test
  void readAllForExportOfADefinitionWithoutFilesIsEmpty() {
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(List.of());

    assertTrue(reader.readAllForExport(DEFINITION_ID).isEmpty());
  }

  @Test
  void readAllForExportReadsFilesAndBytesInOneQueryEach() {
    String otherHash = "b".repeat(64);
    List<DefinitionAttachment> links =
        List.of(
            new DefinitionAttachment(new DefinitionAttachmentId(DEFINITION_ID, HASH), "a.txt"),
            new DefinitionAttachment(
                new DefinitionAttachmentId(DEFINITION_ID, otherHash), "b.txt"));
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(links);
    given(attachmentFileRepository.findAllById(List.of(HASH, otherHash)))
        .willReturn(List.of(storedFile(HASH), storedFile(otherHash)));
    given(attachmentFileRepository.findBytesByHashIn(Set.of(HASH, otherHash)))
        .willReturn(List.of(fileBytes(otherHash, CONTENT, null), fileBytes(HASH, CONTENT, null)));

    List<ExportedAttachment> exported = reader.readAllForExport(DEFINITION_ID);

    assertEquals(
        List.of("a.txt", "b.txt"), exported.stream().map(ExportedAttachment::fileName).toList());
    verify(attachmentFileRepository, never()).findContentByHash(anyString());
    verify(attachmentFileRepository, never()).findPosterByHash(anyString());
  }

  private static AttachmentFile storedFile(String hash) {
    AttachmentFile file = new AttachmentFile();
    file.setHash(hash);
    file.setMediaType("text/plain");
    return file;
  }

  private static FileBytes fileBytes(byte[] content, byte[] poster) {
    return fileBytes(HASH, content, poster);
  }

  private static FileBytes fileBytes(String hash, byte[] content, byte[] poster) {
    return new StoredBytes(hash, content, poster);
  }

  private record StoredBytes(String hash, byte[] content, byte[] poster) implements FileBytes {

    @Override
    public String getHash() {
      return hash;
    }

    @Override
    public byte[] getContent() {
      return content;
    }

    @Override
    public byte[] getPoster() {
      return poster;
    }
  }
}
