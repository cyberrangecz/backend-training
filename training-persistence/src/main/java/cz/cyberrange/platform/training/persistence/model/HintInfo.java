package cz.cyberrange.platform.training.persistence.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Snapshot of a {@link Hint} taken by a trainee, copied from the hint at the moment it was
 * requested and held in the owning training run's set of taken hints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class HintInfo {
  @Column(name = "training_level_id", nullable = false)
  private Long trainingLevelId;

  @Column(name = "hint_id", nullable = false)
  private long hintId;

  @Column(name = "hint_title", nullable = false)
  private String hintTitle;

  @Column(name = "hint_content", nullable = false)
  private String hintContent;

  @Column(name = "order_in_level", nullable = false)
  private int order;

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof HintInfo)) return false;
    HintInfo hintInfo = (HintInfo) object;
    return Objects.equals(getTrainingLevelId(), hintInfo.getTrainingLevelId())
        && Objects.equals(getHintId(), hintInfo.getHintId())
        && Objects.equals(getHintTitle(), hintInfo.getHintTitle());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTrainingLevelId(), getHintId(), getHintTitle());
  }

  @Override
  public String toString() {
    return "HintInfo{"
        + "trainingLevelId="
        + trainingLevelId
        + ", hintId="
        + hintId
        + ", hintTitle='"
        + hintTitle
        + '\''
        + ", hintContent='"
        + hintContent
        + '\''
        + ", order="
        + order
        + '}';
  }
}
