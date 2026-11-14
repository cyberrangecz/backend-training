package cz.cyberrange.platform.training.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/**
 * Metadata of a stored attachment file, identified by the lowercase hexadecimal SHA-256 of its
 * bytes. The bytes and the poster image live in the same table but are not mapped, so loading this
 * entity never reads them; they are read and written through {@link
 * cz.cyberrange.platform.training.persistence.repository.AttachmentFileRepository}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "attachment_file")
public class AttachmentFile implements Serializable {

  @Id
  @Column(name = "hash", nullable = false, length = 64)
  private String hash;

  @Column(name = "media_type", nullable = false)
  private String mediaType;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "duration_seconds")
  private Double durationSeconds;

  @Column(name = "width")
  private Integer width;

  @Column(name = "height")
  private Integer height;

  @Column(name = "poster_media_type")
  private String posterMediaType;
}
