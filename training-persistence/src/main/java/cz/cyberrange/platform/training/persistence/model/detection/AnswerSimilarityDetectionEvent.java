package cz.cyberrange.platform.training.persistence.model.detection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A finding that a trainee's incorrect submission matched another trainee's stored variant answer
 * for the same level, evidenced by the matched answer text and the trainee it was generated for
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "answer_similarity_detection_event")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({
  @NamedQuery(
      name = "AnswerSimilarityDetectionEvent.findAnswerSimilarityEventById",
      query = "SELECT asde FROM AnswerSimilarityDetectionEvent asde WHERE asde.id = :eventId"),
  @NamedQuery(
      name = "AnswerSimilarityDetectionEvent.findAllByCheatingDetectionId",
      query =
          "SELECT asde FROM AnswerSimilarityDetectionEvent asde WHERE asde.cheatingDetectionId = :cheatingDetectionId")
})
public class AnswerSimilarityDetectionEvent extends AbstractDetectionEvent {

  /** The answer value that was submitted, as the trainee entered it */
  @Column(name = "answer")
  private String answer;

  /** Display name of the trainee the submitted answer was generated for */
  @Column(name = "answer_owner", nullable = false)
  private String answerOwner;
}
