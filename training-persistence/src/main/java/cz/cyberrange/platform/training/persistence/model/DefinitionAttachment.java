package cz.cyberrange.platform.training.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Use of an {@link AttachmentFile} by a training definition, under the file name its designer gave
 * it there. Identified by the definition and the file together.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "definition_attachment")
public class DefinitionAttachment implements Serializable {

  @EmbeddedId private DefinitionAttachmentId id;

  @Column(name = "file_name", nullable = false)
  private String fileName;
}
