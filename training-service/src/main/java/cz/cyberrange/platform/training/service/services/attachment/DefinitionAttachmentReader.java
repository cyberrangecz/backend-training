package cz.cyberrange.platform.training.service.services.attachment;

import cz.cyberrange.platform.training.api.dto.attachment.AttachmentDownloadDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentListDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentMetadataDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentPosterDTO;
import cz.cyberrange.platform.training.api.dto.attachment.AttachmentSummaryDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.AttachmentFile;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachment;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachmentId;
import cz.cyberrange.platform.training.persistence.repository.AttachmentFileRepository;
import cz.cyberrange.platform.training.persistence.repository.AttachmentFileRepository.FileBytes;
import cz.cyberrange.platform.training.persistence.repository.DefinitionAttachmentRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

/** Reads the attachment files a training definition links to. */
@Service
public class DefinitionAttachmentReader {

  private final AttachmentFileRepository attachmentFileRepository;
  private final DefinitionAttachmentRepository definitionAttachmentRepository;
  private final DataSize budgetPerDefinition;

  /**
   * Creates the reader.
   *
   * @param attachmentFileRepository the repository of stored files
   * @param definitionAttachmentRepository the repository of definition links
   * @param budgetPerDefinition the cap on the total size of the distinct files one definition links
   */
  @Autowired
  public DefinitionAttachmentReader(
      AttachmentFileRepository attachmentFileRepository,
      DefinitionAttachmentRepository definitionAttachmentRepository,
      @Value("${attachments.budget.per.definition}") DataSize budgetPerDefinition) {
    this.attachmentFileRepository = attachmentFileRepository;
    this.definitionAttachmentRepository = definitionAttachmentRepository;
    this.budgetPerDefinition = budgetPerDefinition;
  }

  /**
   * Lists the files the definition links to, ordered by file name then hash, reading no file bytes.
   *
   * @param definitionId the primary key of the training definition
   * @return the materials with the budget of a definition and the bytes the definition uses
   */
  @Transactional(readOnly = true)
  public AttachmentListDTO list(Long definitionId) {
    List<DefinitionAttachment> links =
        definitionAttachmentRepository.findAllByTrainingDefinitionId(definitionId);
    Map<String, AttachmentFile> filesByHash = filesByHash(links);
    List<AttachmentSummaryDTO> materials =
        links.stream()
            .map(link -> toSummary(link, filesByHash.get(link.getId().fileHash())))
            .toList();
    return new AttachmentListDTO(
        materials,
        budgetPerDefinition.toBytes(),
        definitionAttachmentRepository.sumFileSizesByTrainingDefinitionId(definitionId));
  }

  /**
   * Lists one file the definition links to as a material, reading no file bytes.
   *
   * @param definitionId the primary key of the training definition
   * @param hash the hash of the file
   * @return the material
   * @throws EntityNotFoundException when the definition does not link to a file with that hash
   */
  @Transactional(readOnly = true)
  public AttachmentSummaryDTO findSummary(Long definitionId, String hash) {
    DefinitionAttachment link =
        definitionAttachmentRepository
            .findById(new DefinitionAttachmentId(definitionId, hash))
            .orElseThrow(() -> notLinked(hash));
    return toSummary(link, attachmentFileRepository.findById(hash).orElseThrow());
  }

  /**
   * Describes a file the definition links to, reading no file bytes.
   *
   * @param definitionId the primary key of the training definition
   * @param hash the hash of the file
   * @return the file name the definition gives the file, with its media type and size
   * @throws EntityNotFoundException when the definition does not link to a file with that hash
   */
  @Transactional(readOnly = true)
  public AttachmentMetadataDTO findMetadata(Long definitionId, String hash) {
    DefinitionAttachment link =
        definitionAttachmentRepository
            .findById(new DefinitionAttachmentId(definitionId, hash))
            .orElseThrow(() -> notLinked(hash));
    AttachmentFile file = attachmentFileRepository.findById(hash).orElseThrow();
    return new AttachmentMetadataDTO(
        link.getFileName(),
        file.getMediaType(),
        file.getSizeBytes(),
        file.getDurationSeconds(),
        file.getWidth(),
        file.getHeight());
  }

  /**
   * Reads the poster image of a file the definition links to.
   *
   * @param definitionId the primary key of the training definition
   * @param hash the hash of the file
   * @return the poster image with its media type
   * @throws EntityNotFoundException when the definition does not link to a file with that hash, or
   *     the file has no poster
   */
  @Transactional(readOnly = true)
  public AttachmentPosterDTO readPoster(Long definitionId, String hash) {
    findMetadata(definitionId, hash);
    AttachmentFile file = attachmentFileRepository.findById(hash).orElseThrow();
    byte[] poster =
        attachmentFileRepository.findPosterByHash(hash).orElseThrow(() -> noPoster(hash));
    return new AttachmentPosterDTO(file.getPosterMediaType(), poster);
  }

  /**
   * Reads a file the definition links to.
   *
   * @param definitionId the primary key of the training definition
   * @param hash the hash of the file
   * @return the file bytes with their description
   * @throws EntityNotFoundException when the definition does not link to a file with that hash
   */
  @Transactional(readOnly = true)
  public AttachmentDownloadDTO read(Long definitionId, String hash) {
    AttachmentMetadataDTO metadata = findMetadata(definitionId, hash);
    byte[] content =
        attachmentFileRepository.findContentByHash(hash).orElseThrow(() -> notLinked(hash));
    return new AttachmentDownloadDTO(metadata, content);
  }

  /**
   * Reads every file the definition links to, in the order of their file names, each with its bytes
   * and poster.
   *
   * @param definitionId the primary key of the training definition
   * @return the files with their bytes, empty when the definition links to none
   */
  @Transactional(readOnly = true)
  public List<ExportedAttachment> readAllForExport(Long definitionId) {
    List<DefinitionAttachment> links =
        definitionAttachmentRepository.findAllByTrainingDefinitionId(definitionId);
    if (links.isEmpty()) {
      return List.of();
    }
    Map<String, AttachmentFile> filesByHash = filesByHash(links);
    Map<String, FileBytes> bytesByHash =
        attachmentFileRepository.findBytesByHashIn(filesByHash.keySet()).stream()
            .collect(Collectors.toMap(FileBytes::getHash, Function.identity()));
    return links.stream()
        .map(
            link -> {
              String hash = link.getId().fileHash();
              return toExport(link, filesByHash.get(hash), bytesByHash.get(hash));
            })
        .toList();
  }

  private Map<String, AttachmentFile> filesByHash(List<DefinitionAttachment> links) {
    return attachmentFileRepository
        .findAllById(links.stream().map(link -> link.getId().fileHash()).toList())
        .stream()
        .collect(Collectors.toMap(AttachmentFile::getHash, Function.identity()));
  }

  private static AttachmentSummaryDTO toSummary(DefinitionAttachment link, AttachmentFile file) {
    return new AttachmentSummaryDTO(
        file.getHash(),
        link.getFileName(),
        file.getMediaType(),
        file.getSizeBytes(),
        file.getDurationSeconds(),
        file.getWidth(),
        file.getHeight(),
        file.getPosterMediaType() != null);
  }

  private static ExportedAttachment toExport(
      DefinitionAttachment link, AttachmentFile file, FileBytes bytes) {
    return new ExportedAttachment(
        file.getHash(),
        link.getFileName(),
        file.getMediaType(),
        bytes.getContent(),
        file.getDurationSeconds(),
        file.getWidth(),
        file.getHeight(),
        bytes.getPoster());
  }

  private static EntityNotFoundException noPoster(String hash) {
    return new EntityNotFoundException(
        new EntityErrorDetail(
            AttachmentFile.class,
            "hash",
            String.class,
            hash,
            "The attachment with this hash has no poster."));
  }

  private static EntityNotFoundException notLinked(String hash) {
    return new EntityNotFoundException(
        new EntityErrorDetail(
            DefinitionAttachment.class,
            "hash",
            String.class,
            hash,
            "The training definition does not use an attachment with this hash."));
  }
}
