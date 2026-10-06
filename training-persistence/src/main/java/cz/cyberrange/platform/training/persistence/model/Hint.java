package cz.cyberrange.platform.training.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class representing hints associated with training level that can be displayed by trainee if they
 * are in need of help with solving given level
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hint")
@NamedQueries({
  @NamedQuery(
      name = "Hint.deleteHintsByLevelId",
      query = "DELETE FROM Hint h WHERE h.trainingLevel.id = :levelId"),
})
public class Hint extends AbstractEntity<Long> {

  @Column(name = "title", nullable = false)
  private String title;

  @Lob
  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  /** Added to the training run's current penalty for the level each time this hint is taken */
  @Column(name = "hint_penalty", nullable = false)
  private Integer hintPenalty;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_level_id")
  private TrainingLevel trainingLevel;

  @Column(name = "order_in_level", nullable = false)
  private int order;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Hint)) return false;
    Hint hint = (Hint) o;
    return Objects.equals(getTitle(), hint.getTitle())
        && Objects.equals(getContent(), hint.getContent())
        && Objects.equals(getHintPenalty(), hint.getHintPenalty())
        && getOrder() == hint.getOrder();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTitle(), getContent(), getHintPenalty(), getOrder());
  }

  @Override
  public String toString() {
    return "Hint{"
        + "id="
        + super.getId()
        + ", title='"
        + title
        + '\''
        + ", content='"
        + content
        + '\''
        + ", hintPenalty="
        + hintPenalty
        + ", order="
        + order
        + '}';
  }
}
