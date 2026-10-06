package cz.cyberrange.platform.training.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Class representing attachments of Training Level */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attachment")
public class Attachment extends AbstractEntity<Long> {

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "creation_time", nullable = false)
  private LocalDateTime creationTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_level_id")
  private TrainingLevel trainingLevel;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Attachment that = (Attachment) o;
    return Objects.equals(content, that.content) && Objects.equals(creationTime, that.creationTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, creationTime);
  }

  @Override
  public String toString() {
    return "Attachment{"
        + "id="
        + super.getId()
        + ", content='"
        + content
        + '\''
        + ", creationTime="
        + creationTime
        + '}';
  }
}
