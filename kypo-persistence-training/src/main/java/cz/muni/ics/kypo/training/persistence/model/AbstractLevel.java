package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class representing levels from Training definition.
 * This class is extended by TrainingLevel, InfoLevel and AssessmentLevel.
 */
@Entity
@Table(name = "abstract_level")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
        @NamedQuery(
                name = "AbstractLevel.findByIdIncludingDefinition",
                query = "SELECT l FROM AbstractLevel l " +
                        "JOIN FETCH l.trainingDefinition td " +
                        "JOIN FETCH td.authors " +
                        "LEFT OUTER JOIN FETCH td.betaTestingGroup btg " +
                        "LEFT OUTER JOIN FETCH btg.organizers " +
                        "WHERE l.id = :levelId"
        ),
        @NamedQuery(
                name = "AbstractLevel.getCurrentMaxOrder",
                query = "SELECT COALESCE(MAX(l.order), -1) FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId"
        ),
        @NamedQuery(
                name = "AbstractLevel.findAllLevelsByTrainingDefinitionId",
                query = "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId ORDER BY l.order"
        ),
        @NamedQuery(
                name = "AbstractLevel.findFirstLevelByTrainingDefinitionId",
                query = "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId ORDER BY l.order"
        ),
        @NamedQuery(
                name = "AbstractLevel.findLevelInDefinition",
                query = "SELECT l FROM AbstractLevel l WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.id = :levelId"
        ),
        @NamedQuery(
                name = "AbstractLevel.increaseOrderOfLevels",
                query = "UPDATE AbstractLevel l SET l.order = l.order + 1 " +
                        "WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.order >= :fromOrder AND l.order <= :toOrder"
        ),
        @NamedQuery(
                name = "AbstractLevel.decreaseOrderOfLevels",
                query = "UPDATE AbstractLevel l SET l.order = l.order - 1 " +
                        "WHERE l.trainingDefinition.id = :trainingDefinitionId AND l.order >= :fromOrder AND l.order <= :toOrder"
        )
})
public abstract class AbstractLevel extends AbstractEntity<Long> {

    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "max_score", nullable = false)
    private int maxScore;
    @Column(name = "estimated_duration")
    private long estimatedDuration;
    @Column(name = "order_in_training_definition", nullable = false)
    private int order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_definition_id")
    private TrainingDefinition trainingDefinition;

    /**
     * Instantiates a new Abstract level
     */
    public AbstractLevel() {
    }

    /**
     * Gets unique identification number of level
     *
     * @return the id
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets unique identification number of level
     *
     * @param id the id
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets title of level
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title of level
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets maximal number of points that player can score in level
     *
     * @return the max score
     */
    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Sets maximal number of points that player can score in level
     *
     * @param maxScore the max score
     */
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    /**
     * Gets estimated time in minutes that it takes to solve level
     *
     * @return the estimated duration
     */
    public long getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated time in minutes that it takes to solve level
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets order number of level that is compared with order numbers of other levels associated with same definition.
     * First level from definition has order of 0
     *
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets order number of level that is compared with order numbers of other levels associated with same definition.
     * First level from definition has order of 0
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Gets training definition that is associated with level
     *
     * @return the training definition
     */
    public TrainingDefinition getTrainingDefinition() {
        return trainingDefinition;
    }

    /**
     * Sets training definition that is associated with level
     *
     * @param trainingDefinition the training definition
     */
    public void setTrainingDefinition(TrainingDefinition trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractLevel)) return false;
        AbstractLevel that = (AbstractLevel) o;
        return getMaxScore() == that.getMaxScore() &&
                Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMaxScore());
    }

    @Override
    public String toString() {
        return "AbstractLevel{" +
                "id=" + super.getId() +
                "title='" + title + '\'' +
                ", maxScore=" + maxScore +
                ", estimatedDuration=" + estimatedDuration +
                ", order=" + order +
                '}';
    }
}
