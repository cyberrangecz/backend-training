package cz.cyberrange.platform.training.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * Composite key of a {@link DefinitionAttachment}.
 *
 * @param trainingDefinitionId the primary key of the training definition using the file
 * @param fileHash the hash of the used {@link AttachmentFile}
 */
@Embeddable
public record DefinitionAttachmentId(
    @Column(name = "training_definition_id", nullable = false) Long trainingDefinitionId,
    @Column(name = "file_hash", nullable = false, length = 64) String fileHash)
    implements Serializable {}
