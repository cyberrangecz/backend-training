package cz.cyberrange.platform.training.service.services.attachment;

import cz.cyberrange.platform.training.api.dto.attachment.AttachmentFileMetadataDTO;
import cz.cyberrange.platform.training.api.dto.attachment.TrainingMaterialDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.PayloadTooLargeException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AttachmentFile;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachment;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachmentId;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.AttachmentFileRepository;
import cz.cyberrange.platform.training.persistence.repository.DefinitionAttachmentRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

/**
 * Keeps the attachment links of a training definition in line with its materials and the files its
 * level content references, and stores and prunes the files behind them. Every operation joins the
 * caller's transaction.
 */
@Service
public class DefinitionAttachmentService {

  private final AbstractLevelRepository abstractLevelRepository;
  private final AttachmentFileRepository attachmentFileRepository;
  private final DefinitionAttachmentRepository definitionAttachmentRepository;
  private final AttachmentReferenceExtractor referenceExtractor;
  private final AttachmentMediaTypes mediaTypes;
  private final AttachmentMetadataValidator metadataValidator;
  private final DataSize budgetPerDefinition;

  /**
   * Creates the service.
   *
   * @param abstractLevelRepository the repository of the levels whose content is scanned
   * @param attachmentFileRepository the repository of stored files
   * @param definitionAttachmentRepository the repository of definition links
   * @param referenceExtractor the finder of references in level content
   * @param mediaTypes the media type detector
   * @param metadataValidator the checker of the media metadata that comes with a file
   * @param budgetPerDefinition the cap on the total size of the distinct files one definition links
   */
  @Autowired
  public DefinitionAttachmentService(
      AbstractLevelRepository abstractLevelRepository,
      AttachmentFileRepository attachmentFileRepository,
      DefinitionAttachmentRepository definitionAttachmentRepository,
      AttachmentReferenceExtractor referenceExtractor,
      AttachmentMediaTypes mediaTypes,
      AttachmentMetadataValidator metadataValidator,
      @Value("${attachments.budget.per.definition}") DataSize budgetPerDefinition) {
    this.abstractLevelRepository = abstractLevelRepository;
    this.attachmentFileRepository = attachmentFileRepository;
    this.definitionAttachmentRepository = definitionAttachmentRepository;
    this.referenceExtractor = referenceExtractor;
    this.mediaTypes = mediaTypes;
    this.metadataValidator = metadataValidator;
    this.budgetPerDefinition = budgetPerDefinition;
  }

  /**
   * Refuses levels whose content references a file that is not among the supplied ones, unless
   * unresolved references are kept. Unlike a save, an import resolves nothing against the stored
   * files.
   *
   * @param levels the levels whose markdown is scanned
   * @param suppliedFiles the files that came with the import
   * @param keepUnresolvedReferences whether a reference without a supplied file is accepted
   * @throws BadRequestException when a referenced hash has no supplied file and unresolved
   *     references are not kept
   */
  public void requireReferencesSupplied(
      Collection<? extends AbstractLevel> levels,
      Collection<SuppliedAttachment> suppliedFiles,
      boolean keepUnresolvedReferences) {
    if (keepUnresolvedReferences) {
      return;
    }
    Set<String> missing = new TreeSet<>(referenceExtractor.extract(levels));
    suppliedFiles.forEach(file -> missing.remove(file.hash()));
    if (!missing.isEmpty()) {
      throw new BadRequestException(
          "The level content references attachments the import does not contain: "
              + String.join(", ", missing));
    }
  }

  /**
   * Makes the definition's links exactly its materials plus every file its persisted levels
   * reference. Supplied files that are neither a material nor referenced are ignored. A material or
   * a reference resolves to a supplied file or to a file already stored; when unresolved references
   * are kept, a reference that resolves to neither stays in the level content and gets no link, and
   * a later upload of its file links it. Newly supplied files are
   * stored with a detected media type. A link takes the name of its material, else the supplied
   * file's name, else the name of its existing link, else the name of any other definition's link
   * to that file, else the hash with an extension of the media type. Files left without any link
   * are deleted.
   *
   * @param definition the persisted definition, whose levels are already written
   * @param materials the files the definition keeps whether or not content references them
   * @param suppliedFiles the files that came with the request, possibly empty
   * @param keepUnresolvedReferences whether a reference without a supplied or stored file is kept
   *     instead of refused
   * @throws BadRequestException when a material has neither a supplied nor a stored file, a
   *     reference has neither and unresolved references are not kept, or the media metadata of a kept supplied file is out of range or its poster is not a
   *     JPEG, WebP or PNG image of at most 1 MiB
   * @throws PayloadTooLargeException when the linked files exceed the budget of a definition
   */
  @Transactional
  public void synchronizeLinks(
      TrainingDefinition definition,
      Collection<TrainingMaterialDTO> materials,
      Collection<SuppliedAttachment> suppliedFiles,
      boolean keepUnresolvedReferences) {
    Set<String> referenced =
        new TreeSet<>(
            referenceExtractor.extract(
                abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definition.getId())));
    if (keepUnresolvedReferences) {
      referenced.removeAll(unresolvedAmong(referenced, suppliedHashes(suppliedFiles)));
    }
    Map<String, String> materialNames = materialNamesByHash(materials);
    Set<String> kept = new TreeSet<>(referenced);
    kept.addAll(materialNames.keySet());
    Map<String, SuppliedAttachment> suppliedByHash = suppliedByHash(suppliedFiles, kept);
    requireResolvable(kept, suppliedByHash.keySet());
    suppliedByHash.forEach(this::storeIfAbsent);

    List<DefinitionAttachment> currentLinks =
        definitionAttachmentRepository.findAllByTrainingDefinitionId(definition.getId());
    Set<String> unlinked = unlinkOutdated(currentLinks, kept);
    renameKept(currentLinks, materialNames);
    linkNew(definition, currentLinks, kept, materialNames, suppliedByHash);
    requireWithinBudget(definition);
    deleteOrphans(unlinked);
  }

  /**
   * Links the definition to a file that is already stored, under the given name, or renames the
   * link when the definition already has one.
   *
   * @param definition the persisted definition
   * @param hash the hash of the stored file
   * @param fileName the name the definition gives the file
   * @throws EntityNotFoundException when no file with that hash is stored
   * @throws PayloadTooLargeException when the linked files exceed the budget of a definition
   */
  @Transactional
  public void linkStored(TrainingDefinition definition, String hash, String fileName) {
    if (attachmentFileRepository.findExistingHashes(Set.of(hash)).isEmpty()) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              AttachmentFile.class,
              "hash",
              String.class,
              hash,
              "No attachment with this hash is stored, upload its bytes."));
    }
    link(definition, hash, fileName);
  }

  /**
   * Stores an uploaded file unless one with its hash is stored, fills its missing media metadata,
   * and links the definition to it under the file's name, else the name of a stored link, else the
   * hash with an extension of the media type.
   *
   * @param definition the persisted definition
   * @param file the uploaded file
   * @param claimedHash the hash the uploader computed for the file
   * @throws BadRequestException when the claimed hash is not the hash of the bytes, or the media
   *     metadata is out of range or its poster is not a JPEG, WebP or PNG image of at most 1 MiB
   * @throws PayloadTooLargeException when the linked files exceed the budget of a definition
   */
  @Transactional
  public void upload(TrainingDefinition definition, SuppliedAttachment file, String claimedHash) {
    if (!file.hash().equals(claimedHash)) {
      throw new BadRequestException(
          "The hash of the uploaded file is " + file.hash() + ", not " + claimedHash);
    }
    storeIfAbsent(file.hash(), file);
    String fileName =
        explicitName(null, file)
            .orElseGet(() -> storedNamesOf(Set.of(file.hash())).get(file.hash()));
    link(definition, file.hash(), fileName);
  }

  /**
   * Gives the target definition a link to every file the source definition links to, under the same
   * names.
   *
   * @param source the definition whose links are copied
   * @param target the persisted definition receiving the links
   */
  @Transactional
  public void copyLinks(TrainingDefinition source, TrainingDefinition target) {
    List<DefinitionAttachment> copies =
        definitionAttachmentRepository.findAllByTrainingDefinitionId(source.getId()).stream()
            .map(
                link ->
                    new DefinitionAttachment(
                        new DefinitionAttachmentId(target.getId(), link.getId().fileHash()),
                        link.getFileName()))
            .toList();
    definitionAttachmentRepository.saveAll(copies);
  }

  /**
   * Removes every link of the definition and deletes the files no other definition links to.
   *
   * @param definition the definition losing its links
   */
  @Transactional
  public void removeAllLinks(TrainingDefinition definition) {
    List<DefinitionAttachment> links =
        definitionAttachmentRepository.findAllByTrainingDefinitionId(definition.getId());
    deleteOrphans(unlinkOutdated(links, Set.of()));
  }

  private void link(TrainingDefinition definition, String hash, String fileName) {
    definitionAttachmentRepository.saveAndFlush(
        new DefinitionAttachment(new DefinitionAttachmentId(definition.getId(), hash), fileName));
    requireWithinBudget(definition);
  }

  private Map<String, String> materialNamesByHash(Collection<TrainingMaterialDTO> materials) {
    Map<String, String> namesByHash = new TreeMap<>();
    materials.forEach(material -> namesByHash.putIfAbsent(material.hash(), material.fileName()));
    return namesByHash;
  }

  private Map<String, SuppliedAttachment> suppliedByHash(
      Collection<SuppliedAttachment> suppliedFiles, Set<String> kept) {
    Map<String, SuppliedAttachment> suppliedByHash = new TreeMap<>();
    for (SuppliedAttachment file : suppliedFiles) {
      if (kept.contains(file.hash())) {
        suppliedByHash.putIfAbsent(file.hash(), file);
      }
    }
    return suppliedByHash;
  }

  private static Set<String> suppliedHashes(Collection<SuppliedAttachment> suppliedFiles) {
    return suppliedFiles.stream().map(SuppliedAttachment::hash).collect(Collectors.toSet());
  }

  private Set<String> unresolvedAmong(Set<String> hashes, Set<String> supplied) {
    Set<String> unresolved = new TreeSet<>(hashes);
    unresolved.removeAll(supplied);
    if (!unresolved.isEmpty()) {
      unresolved.removeAll(attachmentFileRepository.findExistingHashes(unresolved));
    }
    return unresolved;
  }

  private void requireResolvable(Set<String> kept, Set<String> supplied) {
    Set<String> unresolved = unresolvedAmong(kept, supplied);
    if (!unresolved.isEmpty()) {
      throw new BadRequestException(
          "Materials and content references name attachments that were neither supplied nor stored: "
              + String.join(", ", unresolved));
    }
  }

  private void storeIfAbsent(String hash, SuppliedAttachment file) {
    attachmentFileRepository.insertIfAbsent(
        hash,
        mediaTypes.detect(file.content(), file.fileName()),
        file.content().length,
        file.content());
    if (file.metadata() != null) {
      fillMissingMetadata(hash, file.metadata());
    }
  }

  private void fillMissingMetadata(String hash, AttachmentFileMetadataDTO metadata) {
    String posterMediaType = metadataValidator.validate(metadata);
    attachmentFileRepository.fillMissingMetadata(
        hash,
        metadata.durationSeconds(),
        metadata.width(),
        metadata.height(),
        metadata.poster(),
        posterMediaType);
  }

  private Set<String> unlinkOutdated(List<DefinitionAttachment> links, Set<String> keep) {
    List<DefinitionAttachment> outdated =
        links.stream().filter(link -> !keep.contains(link.getId().fileHash())).toList();
    definitionAttachmentRepository.deleteAll(outdated);
    definitionAttachmentRepository.flush();
    return outdated.stream().map(link -> link.getId().fileHash()).collect(Collectors.toSet());
  }

  private void linkNew(
      TrainingDefinition definition,
      List<DefinitionAttachment> currentLinks,
      Set<String> kept,
      Map<String, String> materialNames,
      Map<String, SuppliedAttachment> suppliedByHash) {
    Set<String> toLink = new TreeSet<>(kept);
    currentLinks.forEach(link -> toLink.remove(link.getId().fileHash()));
    if (toLink.isEmpty()) {
      return;
    }
    Map<String, String> names = new TreeMap<>();
    Set<String> unnamed = new TreeSet<>();
    for (String hash : toLink) {
      Optional<String> name = explicitName(materialNames.get(hash), suppliedByHash.get(hash));
      name.ifPresentOrElse(known -> names.put(hash, known), () -> unnamed.add(hash));
    }
    names.putAll(storedNamesOf(unnamed));
    List<DefinitionAttachment> created =
        names.entrySet().stream()
            .map(
                entry ->
                    new DefinitionAttachment(
                        new DefinitionAttachmentId(definition.getId(), entry.getKey()),
                        entry.getValue()))
            .toList();
    definitionAttachmentRepository.saveAll(created);
  }

  private Map<String, String> fileNamesElsewhere(Set<String> hashes) {
    return definitionAttachmentRepository.findAllByFileHashIn(hashes).stream()
        .collect(
            Collectors.toMap(
                link -> link.getId().fileHash(),
                DefinitionAttachment::getFileName,
                (first, second) -> first));
  }

  private void renameKept(List<DefinitionAttachment> links, Map<String, String> materialNames) {
    List<DefinitionAttachment> renamed =
        links.stream()
            .filter(link -> isRenamed(link, materialNames.get(link.getId().fileHash())))
            .toList();
    if (renamed.isEmpty()) {
      return;
    }
    renamed.forEach(link -> link.setFileName(materialNames.get(link.getId().fileHash())));
    definitionAttachmentRepository.saveAll(renamed);
  }

  private static boolean isRenamed(DefinitionAttachment link, String materialName) {
    return StringUtils.isNotBlank(materialName) && !materialName.equals(link.getFileName());
  }

  private static Optional<String> explicitName(String materialName, SuppliedAttachment supplied) {
    if (StringUtils.isNotBlank(materialName)) {
      return Optional.of(materialName);
    }
    return Optional.ofNullable(supplied)
        .map(SuppliedAttachment::fileName)
        .filter(StringUtils::isNotBlank);
  }

  private Map<String, String> storedNamesOf(Set<String> hashes) {
    if (hashes.isEmpty()) {
      return Map.of();
    }
    Map<String, String> names = fileNamesElsewhere(hashes);
    Set<String> withoutName = new TreeSet<>(hashes);
    withoutName.removeAll(names.keySet());
    attachmentFileRepository
        .findAllById(withoutName)
        .forEach(
            file ->
                names.put(
                    file.getHash(), file.getHash() + mediaTypes.extensionOf(file.getMediaType())));
    return names;
  }

  private void requireWithinBudget(TrainingDefinition definition) {
    long linkedBytes =
        definitionAttachmentRepository.sumFileSizesByTrainingDefinitionId(definition.getId());
    if (linkedBytes > budgetPerDefinition.toBytes()) {
      throw new PayloadTooLargeException(
          "Attachments of a training definition may total at most "
              + budgetPerDefinition.toMegabytes()
              + " MB, but the referenced files total "
              + DataSize.ofBytes(linkedBytes).toMegabytes()
              + " MB");
    }
  }

  private void deleteOrphans(Set<String> candidateHashes) {
    if (!candidateHashes.isEmpty()) {
      attachmentFileRepository.deleteUnlinkedByHashIn(candidateHashes);
    }
  }
}
