package cz.cyberrange.platform.training.service.unit.services.attachment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import cz.cyberrange.platform.training.api.dto.attachment.TrainingMaterialDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.PayloadTooLargeException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AttachmentFile;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachment;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachmentId;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.AttachmentFileRepository;
import cz.cyberrange.platform.training.persistence.repository.DefinitionAttachmentRepository;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentMediaTypes;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentMetadataValidator;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentReferenceExtractor;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionAttachmentService;
import cz.cyberrange.platform.training.service.services.attachment.SuppliedAttachment;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;

@ExtendWith(MockitoExtension.class)
class DefinitionAttachmentServiceTest {

  private static final long DEFINITION_ID = 7L;
  private static final long OTHER_DEFINITION_ID = 8L;
  private static final byte[] PNG_BYTES = {
    (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0
  };
  private static final byte[] TEXT_BYTES = "plain text".getBytes(StandardCharsets.UTF_8);
  private static final String PNG_HASH = sha256Hex(PNG_BYTES);
  private static final String TEXT_HASH = sha256Hex(TEXT_BYTES);
  private static final String STORED_HASH = "f".repeat(64);
  private static final String MISSING_HASH = "1".repeat(64);
  private static final String OTHER_MISSING_HASH = "0".repeat(64);
  private static final DataSize BUDGET = DataSize.ofBytes(1000);

  @Mock private AbstractLevelRepository abstractLevelRepository;
  @Mock private AttachmentFileRepository attachmentFileRepository;
  @Mock private DefinitionAttachmentRepository definitionAttachmentRepository;
  @Captor private ArgumentCaptor<Iterable<DefinitionAttachment>> savedLinks;
  @Captor private ArgumentCaptor<Iterable<DefinitionAttachment>> deletedLinks;
  @Captor private ArgumentCaptor<Set<String>> orphanCandidates;

  private DefinitionAttachmentService service;
  private TrainingDefinition definition;
  private TrainingDefinition otherDefinition;

  private static String sha256Hex(byte[] content) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static DefinitionAttachment link(long definitionId, String hash, String fileName) {
    return new DefinitionAttachment(new DefinitionAttachmentId(definitionId, hash), fileName);
  }

  @BeforeEach
  void setUp() {
    service =
        new DefinitionAttachmentService(
            abstractLevelRepository,
            attachmentFileRepository,
            definitionAttachmentRepository,
            new AttachmentReferenceExtractor(),
            new AttachmentMediaTypes(),
            new AttachmentMetadataValidator(new AttachmentMediaTypes()),
            BUDGET);
    definition = new TrainingDefinition();
    definition.setId(DEFINITION_ID);
    otherDefinition = new TrainingDefinition();
    otherDefinition.setId(OTHER_DEFINITION_ID);
  }

  private void givenLevelsReferencing(String... hashes) {
    InfoLevel level = new InfoLevel();
    level.setContent(
        java.util.Arrays.stream(hashes)
            .map(hash -> "![x](attachment:" + hash + ")")
            .reduce("", (left, right) -> left + " " + right));
    given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(List.<AbstractLevel>of(level));
  }

  private void givenCurrentLinks(DefinitionAttachment... links) {
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(List.of(links));
  }

  @Test
  void storesSuppliedReferencedFileWithDetectedMediaTypeAndSize() {
    givenLevelsReferencing(PNG_HASH);
    givenCurrentLinks();

    service.synchronizeLinks(
        definition, List.of(), List.of(SuppliedAttachment.of("logo.bin", PNG_BYTES)), false);

    then(attachmentFileRepository)
        .should()
        .insertIfAbsent(PNG_HASH, "image/png", PNG_BYTES.length, PNG_BYTES);
  }

  @Test
  void ignoresSuppliedFilesNoContentReferences() {
    givenLevelsReferencing(PNG_HASH);
    givenCurrentLinks();

    service.synchronizeLinks(
        definition,
        List.of(),
        List.of(
            SuppliedAttachment.of("logo.png", PNG_BYTES),
            SuppliedAttachment.of("unused.txt", TEXT_BYTES)), false);

    then(attachmentFileRepository).should().insertIfAbsent(eq(PNG_HASH), any(), anyLong(), any());
    then(attachmentFileRepository)
        .should(never())
        .insertIfAbsent(eq(TEXT_HASH), any(), anyLong(), any());
    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    assertEquals(1, toList(savedLinks.getValue()).size());
  }

  @Test
  void storesSuppliedFilesInAscendingHashOrderWhateverTheSupplyOrder() {
    givenLevelsReferencing(PNG_HASH, TEXT_HASH);
    givenCurrentLinks();
    List<SuppliedAttachment> descendingByHash =
        Stream.of(
                SuppliedAttachment.of("logo.png", PNG_BYTES),
                SuppliedAttachment.of("notes.txt", TEXT_BYTES))
            .sorted(Comparator.comparing(SuppliedAttachment::hash).reversed())
            .toList();

    service.synchronizeLinks(definition, List.of(), descendingByHash, false);

    var order = inOrder(attachmentFileRepository);
    Stream.of(PNG_HASH, TEXT_HASH)
        .sorted()
        .forEach(
            hash ->
                order
                    .verify(attachmentFileRepository)
                    .insertIfAbsent(eq(hash), any(), anyLong(), any()));
  }

  @Test
  void resolvesReferenceToAlreadyStoredFileWithoutSupply() {
    givenLevelsReferencing(STORED_HASH);
    givenCurrentLinks();
    given(attachmentFileRepository.findExistingHashes(anyCollection()))
        .willReturn(Set.of(STORED_HASH));
    AttachmentFile stored = new AttachmentFile();
    stored.setHash(STORED_HASH);
    stored.setMediaType("image/png");
    given(attachmentFileRepository.findAllById(anyCollection())).willReturn(List.of(stored));

    assertDoesNotThrow(() -> service.synchronizeLinks(definition, List.of(), List.of(), false));

    then(attachmentFileRepository).should(never()).insertIfAbsent(any(), any(), anyLong(), any());
    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    assertEquals(STORED_HASH, savedLinks.getValue().iterator().next().getId().fileHash());
  }

  @Test
  void failsNamingEveryUnresolvedHash() {
    givenLevelsReferencing(MISSING_HASH, OTHER_MISSING_HASH, STORED_HASH, PNG_HASH);
    given(attachmentFileRepository.findExistingHashes(anyCollection()))
        .willReturn(Set.of(STORED_HASH));

    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                service.synchronizeLinks(
                    definition, List.of(), List.of(SuppliedAttachment.of("logo.png", PNG_BYTES)), false));

    assertTrue(exception.getMessage().contains(MISSING_HASH));
    assertTrue(exception.getMessage().contains(OTHER_MISSING_HASH));
    assertFalse(exception.getMessage().contains(STORED_HASH));
    assertFalse(exception.getMessage().contains(PNG_HASH));
    then(attachmentFileRepository).should(never()).insertIfAbsent(any(), any(), anyLong(), any());
  }

  @Test
  void keepsUnresolvedReferencesUnlinkedAndLinksTheResolvedOnes() {
    givenLevelsReferencing(MISSING_HASH, STORED_HASH, PNG_HASH);
    givenCurrentLinks();
    given(attachmentFileRepository.findExistingHashes(anyCollection()))
        .willReturn(Set.of(STORED_HASH));

    service.synchronizeLinks(
        definition, List.of(), List.of(SuppliedAttachment.of("logo.png", PNG_BYTES)), true);

    then(attachmentFileRepository).should().insertIfAbsent(eq(PNG_HASH), any(), anyLong(), any());
    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    assertEquals(
        Set.of(STORED_HASH, PNG_HASH),
        toList(savedLinks.getValue()).stream()
            .map(created -> created.getId().fileHash())
            .collect(java.util.stream.Collectors.toSet()));
  }

  @Test
  void keepingUnresolvedReferencesStillRefusesAMaterialWithoutAFile() {
    givenLevelsReferencing(MISSING_HASH);
    given(attachmentFileRepository.findExistingHashes(anyCollection())).willReturn(Set.of());

    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                service.synchronizeLinks(
                    definition,
                    List.of(new TrainingMaterialDTO(OTHER_MISSING_HASH, "lost.txt")),
                    List.of(),
                    true));

    assertTrue(exception.getMessage().contains(OTHER_MISSING_HASH));
    assertFalse(exception.getMessage().contains(MISSING_HASH));
  }

  @Test
  void keptUnresolvedReferenceCreatesNoLinkAndLaterUploadLinksIt() {
    givenLevelsReferencing(MISSING_HASH);
    givenCurrentLinks();
    given(attachmentFileRepository.findExistingHashes(anyCollection())).willReturn(Set.of());

    service.synchronizeLinks(definition, List.of(), List.of(), true);

    then(definitionAttachmentRepository).should(never()).saveAll(any());

    service.upload(definition, SuppliedAttachment.of("logo.png", PNG_BYTES), PNG_HASH);

    assertSavedLink(PNG_HASH, "logo.png");
  }

  @Test
  void requireReferencesSuppliedAcceptsAnUnsuppliedReferenceWhenKept() {
    InfoLevel level = new InfoLevel();
    level.setContent("![x](attachment:" + MISSING_HASH + ")");

    assertDoesNotThrow(() -> service.requireReferencesSupplied(List.of(level), List.of(), true));
  }

  @Test
  void requireReferencesSuppliedRefusesAnUnsuppliedReferenceByDefault() {
    InfoLevel level = new InfoLevel();
    level.setContent("![x](attachment:" + MISSING_HASH + ")");

    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> service.requireReferencesSupplied(List.of(level), List.of(), false));

    assertTrue(exception.getMessage().contains(MISSING_HASH));
  }

  @Test
  void newLinkTakesSuppliedFileName() {
    givenLevelsReferencing(PNG_HASH);
    givenCurrentLinks();

    service.synchronizeLinks(
        definition, List.of(), List.of(SuppliedAttachment.of("logo.png", PNG_BYTES)), false);

    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    DefinitionAttachment created = savedLinks.getValue().iterator().next();
    assertEquals("logo.png", created.getFileName());
    assertEquals(new DefinitionAttachmentId(DEFINITION_ID, PNG_HASH), created.getId());
  }

  @Test
  void unreferencedMaterialKeepsTheFileNameItsEntryCarries() {
    givenLevelsReferencing();
    givenCurrentLinks();
    SuppliedAttachment diagram = SuppliedAttachment.of("diagram.png", PNG_BYTES);

    service.synchronizeLinks(
        definition,
        List.of(new TrainingMaterialDTO(diagram.hash(), diagram.fileName())),
        List.of(diagram), false);

    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    assertEquals("diagram.png", savedLinks.getValue().iterator().next().getFileName());
  }

  @Test
  void namedLinksLookNeitherForOtherDefinitionsNamesNorForStoredFiles() {
    givenLevelsReferencing(PNG_HASH);
    givenCurrentLinks();

    service.synchronizeLinks(
        definition, List.of(), List.of(SuppliedAttachment.of("logo.png", PNG_BYTES)), false);
    service.upload(definition, SuppliedAttachment.of("logo.png", PNG_BYTES), PNG_HASH);

    then(definitionAttachmentRepository).should(never()).findAllByFileHashIn(anyCollection());
    then(attachmentFileRepository).should(never()).findAllById(anyCollection());
  }

  @Test
  void newLinkTakesNameOfExistingLinkOfAnotherDefinition() {
    givenLevelsReferencing(STORED_HASH);
    givenCurrentLinks();
    given(attachmentFileRepository.findExistingHashes(anyCollection()))
        .willReturn(Set.of(STORED_HASH));
    given(definitionAttachmentRepository.findAllByFileHashIn(anyCollection()))
        .willReturn(
            List.of(
                link(OTHER_DEFINITION_ID, STORED_HASH, "diagram.svg"),
                link(9L, STORED_HASH, "later.svg")));

    service.synchronizeLinks(definition, List.of(), List.of(), false);

    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    assertEquals("diagram.svg", savedLinks.getValue().iterator().next().getFileName());
  }

  @Test
  void newLinkFallsBackToHashWithMediaTypeExtension() {
    givenLevelsReferencing(STORED_HASH);
    givenCurrentLinks();
    given(attachmentFileRepository.findExistingHashes(anyCollection()))
        .willReturn(Set.of(STORED_HASH));
    given(definitionAttachmentRepository.findAllByFileHashIn(anyCollection()))
        .willReturn(List.of());
    AttachmentFile stored = new AttachmentFile();
    stored.setHash(STORED_HASH);
    stored.setMediaType("image/png");
    given(attachmentFileRepository.findAllById(anyCollection())).willReturn(List.of(stored));

    service.synchronizeLinks(definition, List.of(), List.of(), false);

    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    assertEquals(STORED_HASH + ".png", savedLinks.getValue().iterator().next().getFileName());
  }

  @Test
  void keepsExistingLinkUntouchedAndAddsOnlyNewOnes() {
    givenLevelsReferencing(PNG_HASH, TEXT_HASH);
    givenCurrentLinks(link(DEFINITION_ID, PNG_HASH, "kept.png"));

    service.synchronizeLinks(
        definition,
        List.of(),
        List.of(
            SuppliedAttachment.of("renamed.png", PNG_BYTES),
            SuppliedAttachment.of("notes.txt", TEXT_BYTES)), false);

    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    List<DefinitionAttachment> created = List.copyOf(toList(savedLinks.getValue()));
    assertEquals(1, created.size());
    assertEquals(TEXT_HASH, created.get(0).getId().fileHash());
    assertEquals("notes.txt", created.get(0).getFileName());
  }

  @Test
  void acceptsLinkedTotalEqualToBudget() {
    givenLevelsReferencing(PNG_HASH);
    givenCurrentLinks();
    given(definitionAttachmentRepository.sumFileSizesByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(BUDGET.toBytes());

    assertDoesNotThrow(
        () ->
            service.synchronizeLinks(
                definition, List.of(), List.of(SuppliedAttachment.of("logo.png", PNG_BYTES)), false));
  }

  @Test
  void rejectsLinkedTotalAboveBudget() {
    givenLevelsReferencing(PNG_HASH);
    givenCurrentLinks();
    given(definitionAttachmentRepository.sumFileSizesByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(BUDGET.toBytes() + 1);

    assertThrows(
        PayloadTooLargeException.class,
        () ->
            service.synchronizeLinks(
                definition, List.of(), List.of(SuppliedAttachment.of("logo.png", PNG_BYTES)), false));
    then(attachmentFileRepository).should(never()).deleteUnlinkedByHashIn(anyCollection());
  }

  @Test
  void unlinksOutdatedLinksAndDeletesOnlyTheirOrphanCandidates() {
    givenLevelsReferencing(PNG_HASH);
    DefinitionAttachment outdated = link(DEFINITION_ID, STORED_HASH, "old.png");
    givenCurrentLinks(link(DEFINITION_ID, PNG_HASH, "logo.png"), outdated);
    given(attachmentFileRepository.findExistingHashes(anyCollection()))
        .willReturn(Set.of(PNG_HASH));

    service.synchronizeLinks(definition, List.of(), List.of(), false);

    then(definitionAttachmentRepository).should().deleteAll(deletedLinks.capture());
    assertEquals(List.of(outdated), toList(deletedLinks.getValue()));
    then(attachmentFileRepository).should().deleteUnlinkedByHashIn(orphanCandidates.capture());
    assertEquals(Set.of(STORED_HASH), orphanCandidates.getValue());
  }

  @Test
  void skipsOrphanDeletionWhenNothingWasUnlinked() {
    givenLevelsReferencing(PNG_HASH);
    givenCurrentLinks(link(DEFINITION_ID, PNG_HASH, "logo.png"));
    given(attachmentFileRepository.findExistingHashes(anyCollection()))
        .willReturn(Set.of(PNG_HASH));

    service.synchronizeLinks(definition, List.of(), List.of(), false);

    then(attachmentFileRepository).should(never()).deleteUnlinkedByHashIn(anyCollection());
  }

  @Test
  void storedOnlyMaterialIsLinkedUnderItsNameWithoutAReference() {
    givenLevelsReferencing();
    givenCurrentLinks();

    service.synchronizeLinks(
        definition,
        List.of(new TrainingMaterialDTO(TEXT_HASH, "handout.txt")),
        List.of(SuppliedAttachment.of("upload.txt", TEXT_BYTES)), false);

    then(attachmentFileRepository).should().insertIfAbsent(eq(TEXT_HASH), any(), anyLong(), any());
    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    DefinitionAttachment created = savedLinks.getValue().iterator().next();
    assertEquals(new DefinitionAttachmentId(DEFINITION_ID, TEXT_HASH), created.getId());
    assertEquals("handout.txt", created.getFileName());
  }

  @Test
  void existingMaterialLinkIsKeptAndRenamed() {
    givenLevelsReferencing();
    DefinitionAttachment existing = link(DEFINITION_ID, STORED_HASH, "old.txt");
    givenCurrentLinks(existing);
    given(attachmentFileRepository.findExistingHashes(anyCollection()))
        .willReturn(Set.of(STORED_HASH));

    service.synchronizeLinks(
        definition, List.of(new TrainingMaterialDTO(STORED_HASH, "new.txt")), List.of(), false);

    assertEquals("new.txt", existing.getFileName());
    then(definitionAttachmentRepository).should().saveAll(List.of(existing));
    then(attachmentFileRepository).should(never()).deleteUnlinkedByHashIn(anyCollection());
  }

  @Test
  void materialNeitherSuppliedNorStoredFailsTheSave() {
    givenLevelsReferencing();
    given(attachmentFileRepository.findExistingHashes(anyCollection())).willReturn(Set.of());

    assertThrows(
        BadRequestException.class,
        () ->
            service.synchronizeLinks(
                definition, List.of(new TrainingMaterialDTO(MISSING_HASH, "lost.txt")), List.of(), false));
  }

  @Test
  void removedMaterialIsUnlinkedAndOnlyItsFileIsAnOrphanCandidate() {
    givenLevelsReferencing();
    DefinitionAttachment removed = link(DEFINITION_ID, STORED_HASH, "old.txt");
    givenCurrentLinks(removed);

    service.synchronizeLinks(definition, List.of(), List.of(), false);

    then(definitionAttachmentRepository).should().deleteAll(deletedLinks.capture());
    assertEquals(List.of(removed), toList(deletedLinks.getValue()));
    then(attachmentFileRepository).should().deleteUnlinkedByHashIn(orphanCandidates.capture());
    assertEquals(Set.of(STORED_HASH), orphanCandidates.getValue());
  }

  @Test
  void copyLinksGivesTargetTheSameFilesUnderTheSameNames() {
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(
            List.of(
                link(DEFINITION_ID, PNG_HASH, "logo.png"),
                link(DEFINITION_ID, TEXT_HASH, "notes.txt")));

    service.copyLinks(definition, otherDefinition);

    then(definitionAttachmentRepository).should().saveAll(savedLinks.capture());
    List<DefinitionAttachment> copies = toList(savedLinks.getValue());
    assertEquals(
        List.of(
            new DefinitionAttachmentId(OTHER_DEFINITION_ID, PNG_HASH),
            new DefinitionAttachmentId(OTHER_DEFINITION_ID, TEXT_HASH)),
        copies.stream().map(DefinitionAttachment::getId).toList());
    assertEquals(
        List.of("logo.png", "notes.txt"),
        copies.stream().map(DefinitionAttachment::getFileName).toList());
  }

  @Test
  void removeAllLinksDeletesEveryLinkThenOrphanCandidates() {
    List<DefinitionAttachment> links =
        List.of(
            link(DEFINITION_ID, PNG_HASH, "logo.png"), link(DEFINITION_ID, TEXT_HASH, "notes.txt"));
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(links);

    service.removeAllLinks(definition);

    var order = inOrder(definitionAttachmentRepository, attachmentFileRepository);
    order.verify(definitionAttachmentRepository).deleteAll(deletedLinks.capture());
    order.verify(definitionAttachmentRepository).flush();
    order.verify(attachmentFileRepository).deleteUnlinkedByHashIn(orphanCandidates.capture());
    assertEquals(links, toList(deletedLinks.getValue()));
    assertEquals(Set.of(PNG_HASH, TEXT_HASH), orphanCandidates.getValue());
  }

  @Test
  void removeAllLinksOfDefinitionWithoutLinksDeletesNothing() {
    given(definitionAttachmentRepository.findAllByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(List.of());

    service.removeAllLinks(definition);

    then(attachmentFileRepository).should(never()).deleteUnlinkedByHashIn(anyCollection());
  }

  @Test
  void linkStoredLinksStoredFileUnderGivenName() {
    given(attachmentFileRepository.findExistingHashes(Set.of(STORED_HASH)))
        .willReturn(Set.of(STORED_HASH));

    service.linkStored(definition, STORED_HASH, "manual.pdf");

    then(attachmentFileRepository).should(never()).insertIfAbsent(any(), any(), anyLong(), any());
    assertSavedLink(STORED_HASH, "manual.pdf");
  }

  @Test
  void linkStoredRejectsUnknownHash() {
    given(attachmentFileRepository.findExistingHashes(Set.of(MISSING_HASH))).willReturn(Set.of());

    assertThrows(
        EntityNotFoundException.class,
        () -> service.linkStored(definition, MISSING_HASH, "manual.pdf"));

    then(definitionAttachmentRepository).should(never()).saveAndFlush(any());
  }

  @Test
  void linkStoredRejectsLinkedTotalAboveBudget() {
    given(attachmentFileRepository.findExistingHashes(Set.of(STORED_HASH)))
        .willReturn(Set.of(STORED_HASH));
    given(definitionAttachmentRepository.sumFileSizesByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(BUDGET.toBytes() + 1);

    assertThrows(
        PayloadTooLargeException.class,
        () -> service.linkStored(definition, STORED_HASH, "manual.pdf"));
  }

  @Test
  void uploadStoresFileAndLinksItUnderItsName() {
    service.upload(definition, SuppliedAttachment.of("logo.bin", PNG_BYTES), PNG_HASH);

    then(attachmentFileRepository)
        .should()
        .insertIfAbsent(PNG_HASH, "image/png", PNG_BYTES.length, PNG_BYTES);
    assertSavedLink(PNG_HASH, "logo.bin");
  }

  @Test
  void uploadWithoutFileNameLinksUnderHashWithExtension() {
    AttachmentFile stored = new AttachmentFile();
    stored.setHash(PNG_HASH);
    stored.setMediaType("image/png");
    given(attachmentFileRepository.findAllById(anyCollection())).willReturn(List.of(stored));

    service.upload(definition, SuppliedAttachment.of("", PNG_BYTES), PNG_HASH);

    assertSavedLink(PNG_HASH, PNG_HASH + ".png");
  }

  @Test
  void uploadRejectsHashThatIsNotTheHashOfTheBytes() {
    assertThrows(
        BadRequestException.class,
        () -> service.upload(definition, SuppliedAttachment.of("logo.png", PNG_BYTES), TEXT_HASH));

    then(attachmentFileRepository).shouldHaveNoInteractions();
    then(definitionAttachmentRepository).shouldHaveNoInteractions();
  }

  @Test
  void uploadRejectsLinkedTotalAboveBudget() {
    given(definitionAttachmentRepository.sumFileSizesByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(BUDGET.toBytes() + 1);

    assertThrows(
        PayloadTooLargeException.class,
        () -> service.upload(definition, SuppliedAttachment.of("logo.png", PNG_BYTES), PNG_HASH));
  }

  private void assertSavedLink(String expectedHash, String expectedFileName) {
    ArgumentCaptor<DefinitionAttachment> saved =
        ArgumentCaptor.forClass(DefinitionAttachment.class);
    then(definitionAttachmentRepository).should().saveAndFlush(saved.capture());
    assertEquals(DEFINITION_ID, saved.getValue().getId().trainingDefinitionId());
    assertEquals(expectedHash, saved.getValue().getId().fileHash());
    assertEquals(expectedFileName, saved.getValue().getFileName());
  }

  private static List<DefinitionAttachment> toList(Iterable<DefinitionAttachment> links) {
    List<DefinitionAttachment> result = new java.util.ArrayList<>();
    links.forEach(result::add);
    return result;
  }
}
