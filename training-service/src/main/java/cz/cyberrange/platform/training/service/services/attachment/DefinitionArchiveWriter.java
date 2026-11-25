package cz.cyberrange.platform.training.service.services.attachment;

import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.ATTACHMENT_FOLDER;
import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.MAXIMAL_ENTRIES;
import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.MAXIMAL_METADATA_BYTES;
import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.METADATA_ENTRY;

import cz.cyberrange.platform.training.api.dto.export.AttachmentArchiveEntryDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import cz.cyberrange.platform.training.service.export.ExportFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Packs an exported definition document and its attachment files into one ZIP archive: the
 * document, a JSON metadata file and one entry per file under a folder.
 */
@Component
public class DefinitionArchiveWriter {

  private static final String FALLBACK_ARCHIVE_NAME = "training-definition";
  private static final int MAXIMAL_ENTRY_NAME_BYTES = 0xFFFF;
  private static final int ENTRIES_BESIDES_ATTACHMENTS = 2;

  private final ObjectMapper objectMapper;
  private final AttachmentMediaTypes mediaTypes;

  /**
   * Creates the writer.
   *
   * @param objectMapper the mapper the metadata file is written with, as JSON
   * @param mediaTypes the media type helper that derives extensions
   */
  public DefinitionArchiveWriter(ObjectMapper objectMapper, AttachmentMediaTypes mediaTypes) {
    this.objectMapper = objectMapper;
    this.mediaTypes = mediaTypes;
  }

  /**
   * Derives the name an archive and its definition document are given from a definition title.
   *
   * @param title the definition title, may be {@code null}
   * @return the title cleaned for file names, or a default name when nothing is left
   */
  public String archiveNameOf(String title) {
    return ArchiveFileNames.cleanOrElse(title, FALLBACK_ARCHIVE_NAME);
  }

  /**
   * Writes the archive. Entry names are unique ignoring case; a clash receives a counter before the
   * last extension, and a document that would clash with the metadata file does too.
   *
   * @param archiveName the cleaned name the document entry is based on
   * @param format the format the document is written in
   * @param document the definition document
   * @param attachments the files to include, at least one
   * @return the archive bytes
   * @throws BadRequestException when the archive would hold more entries or a larger metadata file
   *     than an import accepts, or an entry name is longer than a ZIP entry name may be
   * @throws InternalServerErrorException when the archive cannot be written
   */
  public byte[] write(
      String archiveName,
      ExportFormat format,
      byte[] document,
      List<ExportedAttachment> attachments) {
    if (attachments.size() + ENTRIES_BESIDES_ATTACHMENTS > MAXIMAL_ENTRIES) {
      throw new BadRequestException(
          "The definition links "
              + attachments.size()
              + " files, more than the "
              + (MAXIMAL_ENTRIES - ENTRIES_BESIDES_ATTACHMENTS)
              + " an archive can hold");
    }
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bytes)) {
      UniqueEntryNames rootNames = new UniqueEntryNames();
      rootNames.claim(METADATA_ENTRY);
      writeEntry(zip, rootNames.claim(archiveName + format.getFileExtension()), document);

      UniqueEntryNames attachmentNames = new UniqueEntryNames();
      Map<String, AttachmentArchiveEntryDTO> metadata = new LinkedHashMap<>();
      for (ExportedAttachment attachment : attachments) {
        String entryName = attachmentNames.claim(entryNameOf(attachment));
        writeEntry(zip, ATTACHMENT_FOLDER + entryName, attachment.content());
        metadata.put(entryName, metadataOf(attachment));
      }
      byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);
      if (metadataBytes.length > MAXIMAL_METADATA_BYTES) {
        throw new BadRequestException(
            "The attachment metadata of the definition, posters included, is larger than "
                + MAXIMAL_METADATA_BYTES
                + " bytes, the most an archive can hold");
      }
      writeEntry(zip, METADATA_ENTRY, metadataBytes);
      zip.finish();
      return bytes.toByteArray();
    } catch (IOException | JacksonException e) {
      throw new InternalServerErrorException(
          "The .zip file was not created since there were some processing error.", e);
    }
  }

  private String entryNameOf(ExportedAttachment attachment) {
    return ArchiveFileNames.cleanOrElse(
        attachment.fileName(), attachment.hash() + mediaTypes.extensionOf(attachment.mediaType()));
  }

  private static AttachmentArchiveEntryDTO metadataOf(ExportedAttachment attachment) {
    return new AttachmentArchiveEntryDTO(
        attachment.fileName(),
        attachment.durationSeconds(),
        attachment.width(),
        attachment.height(),
        attachment.poster() == null
            ? null
            : Base64.getEncoder().encodeToString(attachment.poster()));
  }

  private static void writeEntry(ZipOutputStream zip, String name, byte[] content)
      throws IOException {
    if (name.getBytes(StandardCharsets.UTF_8).length > MAXIMAL_ENTRY_NAME_BYTES) {
      throw new BadRequestException(
          "An archive entry name is longer than " + MAXIMAL_ENTRY_NAME_BYTES + " bytes");
    }
    zip.putNextEntry(new ZipEntry(name));
    zip.write(content);
    zip.closeEntry();
  }
}
