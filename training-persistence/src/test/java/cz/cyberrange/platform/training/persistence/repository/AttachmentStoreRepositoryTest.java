package cz.cyberrange.platform.training.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import cz.cyberrange.platform.training.persistence.model.AttachmentFile;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachment;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachmentId;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
public class AttachmentStoreRepositoryTest {

  private static final String FIRST_HASH = "a".repeat(64);
  private static final String SECOND_HASH = "b".repeat(64);
  private static final String THIRD_HASH = "c".repeat(64);
  private static final String PDF = "application/pdf";

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private AttachmentFileRepository fileRepository;
  @Autowired private DefinitionAttachmentRepository linkRepository;

  private TrainingDefinition definition;
  private TrainingDefinition otherDefinition;

  @BeforeEach
  public void init() {
    definition = entityManager.persist(testDataFactory.getUnreleasedDefinition());
    otherDefinition = entityManager.persist(testDataFactory.getReleasedDefinition());
  }

  private int store(String hash, String content) {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    int stored = fileRepository.insertIfAbsent(hash, PDF, bytes.length, bytes);
    entityManager.clear();
    return stored;
  }

  private void link(TrainingDefinition target, String hash, String fileName) {
    linkRepository.saveAndFlush(
        new DefinitionAttachment(new DefinitionAttachmentId(target.getId(), hash), fileName));
    entityManager.clear();
  }

  @Test
  public void insertIfAbsentStoresMetadataAndBytes() {
    assertThat(store(FIRST_HASH, "hello")).isEqualTo(1);

    AttachmentFile file = fileRepository.findById(FIRST_HASH).orElseThrow();
    assertThat(file.getMediaType()).isEqualTo(PDF);
    assertThat(file.getSizeBytes()).isEqualTo(5);
    assertThat(fileRepository.findContentByHash(FIRST_HASH))
        .hasValue("hello".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void insertIfAbsentKeepsTheFirstFileOnDuplicateHash() {
    store(FIRST_HASH, "hello");

    assertThat(store(FIRST_HASH, "hello")).isZero();

    assertThat(fileRepository.findAll()).hasSize(1);
  }

  @Test
  public void findContentByHashUnknownHashIsEmpty() {
    assertThat(fileRepository.findContentByHash(FIRST_HASH)).isEmpty();
  }

  @Test
  public void findAllByIdReadsMetadataOfTheGivenHashes() {
    store(FIRST_HASH, "one");
    store(SECOND_HASH, "three");

    List<AttachmentFile> files = fileRepository.findAllById(Set.of(FIRST_HASH, THIRD_HASH));

    assertThat(files).extracting(AttachmentFile::getHash).containsExactly(FIRST_HASH);
  }

  @Test
  public void findExistingHashesReturnsOnlyStoredHashes() {
    store(FIRST_HASH, "one");
    store(SECOND_HASH, "two");

    assertThat(fileRepository.findExistingHashes(Set.of(FIRST_HASH, THIRD_HASH)))
        .containsExactly(FIRST_HASH);
  }

  @Test
  public void findAllByTrainingDefinitionIdReturnsOnlyThatDefinitionsLinksByName() {
    store(FIRST_HASH, "one");
    store(SECOND_HASH, "two");
    link(definition, FIRST_HASH, "zebra.pdf");
    link(definition, SECOND_HASH, "alpha.pdf");
    link(otherDefinition, FIRST_HASH, "other.pdf");

    List<DefinitionAttachment> links =
        linkRepository.findAllByTrainingDefinitionId(definition.getId());

    assertThat(links)
        .extracting(DefinitionAttachment::getFileName)
        .containsExactly("alpha.pdf", "zebra.pdf");
  }

  @Test
  public void findByIdFindsTheLinkOfDefinitionAndHash() {
    store(FIRST_HASH, "one");
    link(definition, FIRST_HASH, "one.pdf");

    assertThat(linkRepository.findById(new DefinitionAttachmentId(definition.getId(), FIRST_HASH)))
        .isPresent();
    assertThat(
            linkRepository.findById(
                new DefinitionAttachmentId(otherDefinition.getId(), FIRST_HASH)))
        .isEmpty();
  }

  @Test
  public void sumFileSizesAddsSizesOfTheDefinitionsLinksOnly() {
    store(FIRST_HASH, "12345");
    store(SECOND_HASH, "1234567");
    store(THIRD_HASH, "123");
    link(definition, FIRST_HASH, "a");
    link(definition, SECOND_HASH, "b");
    link(otherDefinition, THIRD_HASH, "c");

    assertThat(linkRepository.sumFileSizesByTrainingDefinitionId(definition.getId())).isEqualTo(12);
  }

  @Test
  public void sumFileSizesWithoutLinksIsZero() {
    assertThat(linkRepository.sumFileSizesByTrainingDefinitionId(definition.getId())).isZero();
  }

  @Test
  public void deleteUnlinkedByHashInDeletesOnlyUnlinkedFilesOfTheGivenHashes() {
    store(FIRST_HASH, "linked");
    store(SECOND_HASH, "unlinked");
    store(THIRD_HASH, "unlinked but not asked");
    link(definition, FIRST_HASH, "linked.pdf");

    int deleted = fileRepository.deleteUnlinkedByHashIn(Set.of(FIRST_HASH, SECOND_HASH));
    entityManager.clear();

    assertThat(deleted).isEqualTo(1);
    assertThat(fileRepository.findExistingHashes(Set.of(FIRST_HASH, SECOND_HASH, THIRD_HASH)))
        .containsExactlyInAnyOrder(FIRST_HASH, THIRD_HASH);
  }
}
