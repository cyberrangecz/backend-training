package cz.cyberrange.platform.training.service.services.attachment;

import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.ATTACHMENT_FOLDER;
import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.MAXIMAL_DOCUMENT_BYTES;
import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.MAXIMAL_ENTRIES;
import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.MAXIMAL_METADATA_BYTES;
import static cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveLayout.METADATA_ENTRY;

import cz.cyberrange.platform.training.api.dto.attachment.AttachmentFileMetadataDTO;
import cz.cyberrange.platform.training.api.dto.export.AttachmentArchiveEntryDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.PayloadTooLargeException;
import cz.cyberrange.platform.training.service.export.ExportFormat;
import cz.cyberrange.platform.training.service.export.ExportFormats;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads an exported definition archive for import and refuses any archive that breaks its layout.
 * Entries are read as a stream and the unpacked bytes are counted while reading, so the sizes an
 * archive declares are never trusted, and an entry name is only ever compared as text, never used
 * as a path.
 */
@Component
public class DefinitionArchiveReader {

  private static final int MAXIMAL_FILE_NAME_LENGTH = 255;
  private static final int MAXIMAL_READ_BYTES = Integer.MAX_VALUE - 8;

  private final ObjectMapper objectMapper;
  private final ExportFormats exportFormats;
  private final Validator validator;
  private final DataSize budgetPerDefinition;

  /**
   * Creates the reader.
   *
   * @param objectMapper the mapper the metadata file is parsed with, as JSON
   * @param exportFormats the formats a definition document may be written in
   * @param validator the validator that checks the parsed definition
   * @param budgetPerDefinition the cap on the total size of the distinct files of one archive
   */
  @Autowired
  public DefinitionArchiveReader(
      ObjectMapper objectMapper,
      ExportFormats exportFormats,
      Validator validator,
      @Value("${attachments.budget.per.definition}") DataSize budgetPerDefinition) {
    this.objectMapper = objectMapper;
    this.exportFormats = exportFormats;
    this.validator = validator;
    this.budgetPerDefinition = budgetPerDefinition;
  }

  /**
   * Unpacks an archive, parses and validates its definition document and pairs every attachment
   * file with its metadata. The archive must hold exactly one definition document at its root, the
   * metadata file, and the attachment files under their folder, each listed in the metadata and
   * each listing backed by a file. Nothing else, no duplicate entry and no more than the entry cap
   * is allowed. The distinct attachment files, counted once per content, may not exceed the
   * attachment budget of a definition; the definition document and the metadata file are bounded on
   * their own.
   *
   * @param archive the ZIP bytes
   * @return the definition and its attachment files
   * @throws BadRequestException when the archive breaks the layout, an entry name is not valid
   *     UTF-8, the metadata file is not a valid mapping of entry names to metadata, or a file name
   *     or poster is malformed
   * @throws PayloadTooLargeException when the distinct attachment files exceed the budget
   * @throws ConstraintViolationException when the definition document fails validation
   */
  public DefinitionArchive read(byte[] archive) {
    Unpacked unpacked = unpack(archive);
    Map<String, AttachmentArchiveEntryDTO> metadata = parseMetadata(unpacked.metadata());
    List<SuppliedAttachment> attachments = new ArrayList<>();
    for (Map.Entry<String, SuppliedAttachment> file : unpacked.files().entrySet()) {
      attachments.add(supplied(file.getKey(), file.getValue(), metadata));
    }
    require(
        metadata.isEmpty(),
        "has "
            + METADATA_ENTRY
            + " entries that name no file: "
            + String.join(", ", metadata.keySet()));
    return new DefinitionArchive(parseDefinition(unpacked), attachments);
  }

  private Unpacked unpack(byte[] archive) {
    Unpacked unpacked = new Unpacked();
    Set<String> seenNames = new HashSet<>();
    Map<String, SuppliedAttachment> distinctFiles = new HashMap<>();
    long distinctBytes = 0;
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        String name = entry.getName();
        require(
            seenNames.size() < MAXIMAL_ENTRIES, "has more than " + MAXIMAL_ENTRIES + " entries");
        require(!entry.isDirectory(), "has the folder entry " + name);
        require(seenNames.add(name.toLowerCase(Locale.ROOT)), "has the entry " + name + " twice");
        if (name.startsWith(ATTACHMENT_FOLDER)) {
          SuppliedAttachment file = readFile(zip);
          if (distinctFiles.putIfAbsent(file.hash(), file) == null) {
            distinctBytes += file.content().length;
            requireWithinBudget(distinctBytes);
          }
          unpacked.addFile(name, distinctFiles.get(file.hash()));
        } else {
          unpacked.add(name, readNonFile(zip, name), exportFormats);
        }
      }
    } catch (IOException e) {
      throw new BadRequestException("The archive is not a readable ZIP file", e);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("The archive has an entry name that is not valid UTF-8", e);
    }
    return unpacked;
  }

  private SuppliedAttachment readFile(ZipInputStream zip) throws IOException {
    long limit = budgetPerDefinition.toBytes();
    byte[] content = zip.readNBytes((int) Math.min(limit + 1, MAXIMAL_READ_BYTES));
    requireWithinBudget(content.length);
    return SuppliedAttachment.of(null, content);
  }

  private static byte[] readNonFile(ZipInputStream zip, String name) throws IOException {
    boolean isMetadata = name.equals(METADATA_ENTRY);
    int limit = isMetadata ? MAXIMAL_METADATA_BYTES : MAXIMAL_DOCUMENT_BYTES;
    byte[] content = zip.readNBytes(limit + 1);
    require(
        content.length <= limit,
        "has "
            + (isMetadata ? METADATA_ENTRY : "a definition document")
            + " larger than "
            + limit
            + " bytes");
    return content;
  }

  private void requireWithinBudget(long bytes) {
    if (bytes > budgetPerDefinition.toBytes()) {
      throw new PayloadTooLargeException(
          "The distinct attachment files may total at most the attachment budget of a definition");
    }
  }

  private Map<String, AttachmentArchiveEntryDTO> parseMetadata(byte[] metadataBytes) {
    try {
      Map<String, AttachmentArchiveEntryDTO> parsed =
          objectMapper
              .readerFor(new TypeReference<LinkedHashMap<String, AttachmentArchiveEntryDTO>>() {})
              .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
              .with(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
              .readValue(metadataBytes);
      require(parsed != null, "has an empty " + METADATA_ENTRY);
      return parsed;
    } catch (JacksonException e) {
      throw new BadRequestException(METADATA_ENTRY + " is not valid: " + e.getOriginalMessage(), e);
    }
  }

  private SuppliedAttachment supplied(
      String entryName,
      SuppliedAttachment unnamed,
      Map<String, AttachmentArchiveEntryDTO> metadata) {
    require(
        metadata.containsKey(entryName),
        "has the file " + entryName + ", which " + METADATA_ENTRY + " does not list");
    AttachmentArchiveEntryDTO entry = metadata.remove(entryName);
    require(entry != null, "has no metadata for " + entryName + " in " + METADATA_ENTRY);
    String fileName = ArchiveFileNames.clean(entry.fileName());
    require(
        fileName.length() <= MAXIMAL_FILE_NAME_LENGTH,
        "gives " + entryName + " a file name longer than " + MAXIMAL_FILE_NAME_LENGTH);
    return new SuppliedAttachment(
        fileName.isEmpty() ? null : fileName,
        unnamed.content(),
        unnamed.hash(),
        new AttachmentFileMetadataDTO(
            unnamed.hash(),
            entry.durationSeconds(),
            entry.width(),
            entry.height(),
            decodePoster(entryName, entry.poster())));
  }

  private static byte[] decodePoster(String entryName, String poster) {
    if (poster == null) {
      return null;
    }
    try {
      return Base64.getDecoder().decode(poster);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("The poster of " + entryName + " is not valid Base64", e);
    }
  }

  private ImportTrainingDefinitionDTO parseDefinition(Unpacked unpacked) {
    ImportTrainingDefinitionDTO definition;
    try {
      definition =
          unpacked.format().readDocument(unpacked.document(), ImportTrainingDefinitionDTO.class);
    } catch (JacksonException e) {
      throw new BadRequestException(
          "The definition document is not valid: " + e.getOriginalMessage(), e);
    }
    require(definition != null, "has an empty definition document");
    Set<ConstraintViolation<ImportTrainingDefinitionDTO>> violations =
        validator.validate(definition);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
    return definition;
  }

  private static void require(boolean valid, String rule) {
    if (!valid) {
      throw new BadRequestException("The archive is refused: it " + rule);
    }
  }

  /** The entries of an archive sorted into the roles its layout gives them. */
  private static final class Unpacked {

    private final Map<String, SuppliedAttachment> files = new LinkedHashMap<>();
    private byte[] metadata;
    private byte[] document;
    private ExportFormat format;

    Map<String, SuppliedAttachment> files() {
      return files;
    }

    byte[] metadata() {
      require(metadata != null, "has no " + METADATA_ENTRY);
      return metadata;
    }

    byte[] document() {
      require(document != null, "has no definition document");
      return document;
    }

    ExportFormat format() {
      return format;
    }

    void add(String name, byte[] content, ExportFormats exportFormats) {
      if (name.equals(METADATA_ENTRY)) {
        metadata = content;
      } else {
        addDocument(name, content, exportFormats.byFileName(name));
      }
    }

    void addFile(String name, SuppliedAttachment content) {
      String entryName = name.substring(ATTACHMENT_FOLDER.length());
      require(
          !entryName.isEmpty() && entryName.indexOf('/') < 0,
          "has the nested entry " + ATTACHMENT_FOLDER + entryName);
      files.put(entryName, content);
    }

    private void addDocument(String name, byte[] content, Optional<ExportFormat> documentFormat) {
      require(
          name.indexOf('/') < 0 && documentFormat.isPresent(), "has the unexpected entry " + name);
      require(document == null, "has more than one definition document");
      document = content;
      format = documentFormat.get();
    }
  }
}
