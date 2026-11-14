package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.DefinitionAttachment;
import cz.cyberrange.platform.training.persistence.model.DefinitionAttachmentId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The JPA repository interface to manage {@link DefinitionAttachment} instances. A link is looked
 * up by its {@link DefinitionAttachmentId} through {@code findById}.
 */
@Repository
public interface DefinitionAttachmentRepository
    extends JpaRepository<DefinitionAttachment, DefinitionAttachmentId>,
        QuerydslPredicateExecutor<DefinitionAttachment> {

  /**
   * Finds the links of one training definition, ordered by file name then file hash.
   *
   * @param trainingDefinitionId the primary key of the training definition
   * @return the links, empty when the definition uses no file
   */
  @Query(
      "SELECT l FROM DefinitionAttachment l WHERE l.id.trainingDefinitionId = :trainingDefinitionId"
          + " ORDER BY l.fileName, l.id.fileHash")
  List<DefinitionAttachment> findAllByTrainingDefinitionId(
      @Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Sums the sizes of the files one training definition links to, reading no file bytes.
   *
   * @param trainingDefinitionId the primary key of the training definition
   * @return the total size in bytes, 0 when the definition uses no file
   */
  @Query(
      "SELECT COALESCE(SUM(f.sizeBytes), 0) FROM DefinitionAttachment l JOIN AttachmentFile f"
          + " ON f.hash = l.id.fileHash WHERE l.id.trainingDefinitionId = :trainingDefinitionId")
  long sumFileSizesByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId);

  /**
   * Finds the links of any definition to the given files, ordered by definition then file hash,
   * which gives the names designers chose for the files elsewhere.
   *
   * @param fileHashes the hashes of the files
   * @return the links to those files, empty when none is linked
   */
  @Query(
      "SELECT l FROM DefinitionAttachment l WHERE l.id.fileHash IN :fileHashes"
          + " ORDER BY l.id.trainingDefinitionId, l.id.fileHash")
  List<DefinitionAttachment> findAllByFileHashIn(
      @Param("fileHashes") Collection<String> fileHashes);
}
