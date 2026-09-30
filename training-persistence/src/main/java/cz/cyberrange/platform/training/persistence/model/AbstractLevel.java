package cz.cyberrange.platform.training.persistence.model;

import java.util.Objects;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base of the level hierarchy shared by {@link TrainingLevel}, {@link InfoLevel}, {@link
 * AccessLevel} and {@link AssessmentLevel}, each one step in a training definition's ordered
 * sequence of levels. Each subclass is persisted with the JOINED inheritance strategy: it has its
 * own table joined to abstract_level through a shared primary key, so the concrete kind of a given
 * row is determined by which subtype table holds a matching id, not by a discriminator column.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "abstract_level")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
  @NamedQuery(
      name = "AbstractLevel.findByIdIncludingDefinition",
      query =
          "SELECT l FROM AbstractLevel l "
              + "JOIN FETCH l.trainingDefinition td "
              + "JOIN FETCH td.authors "
              + "LEFT OUTER JOIN FETCH td.betaTestingGroup btg "
              + "LEFT OUTER JOIN FETCH btg.organizers "
              + "WHERE l.id = :levelId"),
  @NamedQuery(
      name = "AbstractLevel.getCurrentMaxOrder",
      query =
          "SELECT COALESCE(MAX(l.order), -1) FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId"),
  @NamedQuery(
      name = "AbstractLevel.findAllLevelsByTrainingDefinitionId",
      query =
          "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId ORDER BY l.order"),
  @NamedQuery(
      name = "AbstractLevel.findAllLevelsByTrainingDefinitionIdIn",
      query =
          "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id IN :trainingDefinitionIds "
              + "ORDER BY l.order"),
  @NamedQuery(
      name = "AbstractLevel.findFirstLevelByTrainingDefinitionId",
      query =
          "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId ORDER BY l.order"),
  @NamedQuery(
      name = "AbstractLevel.findLevelInDefinition",
      query =
          "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.id = :levelId"),
  @NamedQuery(
      name = "AbstractLevel.increaseOrderOfLevels",
      query =
          "UPDATE AbstractLevel l SET l.order = l.order + 1 "
              + "WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.order >= :fromOrder AND l.order <= :toOrder"),
  @NamedQuery(
      name = "AbstractLevel.decreaseOrderOfLevels",
      query =
          "UPDATE AbstractLevel l SET l.order = l.order - 1 "
              + "WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.order >= :fromOrder AND l.order <= :toOrder")
})
public abstract class AbstractLevel extends AbstractEntity<Long> {

  @Column(name = "title", nullable = false)
  private String title;

  /** Score credited for solving the level, before any hint or solution penalty is subtracted */
  @Column(name = "max_score", nullable = false)
  private int maxScore;

  /**
   * Added to the owning training definition's total estimated duration whenever the level is added,
   * removed, or edited
   */
  @Column(name = "estimated_duration")
  private long estimatedDuration;

  /**
   * Threshold, in minutes, below which a correct submission's elapsed solve time triggers the
   * MINIMAL_SOLVE_TIME cheating detection. Does not otherwise affect whether or how the level can
   * be solved.
   */
  @Column(name = "minimal_possible_solve_time")
  private Long minimalPossibleSolveTime;

  /** Zero-based position of the level within its training definition's sequence of levels */
  @Column(name = "order_in_training_definition", nullable = false)
  private int order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_definition_id")
  private TrainingDefinition trainingDefinition;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AbstractLevel)) return false;
    AbstractLevel that = (AbstractLevel) o;
    return getMaxScore() == that.getMaxScore() && Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getMaxScore());
  }

  @Override
  public String toString() {
    return "AbstractLevel{"
        + "id="
        + super.getId()
        + "title='"
        + title
        + '\''
        + ", maxScore="
        + maxScore
        + ", estimatedDuration="
        + estimatedDuration
        + ", order="
        + order
        + '}';
  }
}
