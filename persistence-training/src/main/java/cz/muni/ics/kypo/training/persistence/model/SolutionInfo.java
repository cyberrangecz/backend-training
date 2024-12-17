package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Class represents information of hint associated with current level of training run
 */
@Embeddable
public class SolutionInfo {
    @Column(name = "training_level_id", nullable = false)
    private long trainingLevelId;
    @Column(name = "solution_content", nullable = false)
    private String solutionContent;

    /**
     * Instantiates a new Hint info
     */
    public SolutionInfo() {
    }

    /**
     * Instantiates a new Hint info
     *
     * @param trainingLevelId unique identification number of Training level associated with hint
     * @param solutionContent content of Hint
     */
    public SolutionInfo(long trainingLevelId, String solutionContent) {
        this.trainingLevelId = trainingLevelId;
        this.solutionContent = solutionContent;
    }

    /**
     * Gets unique identification number of Training level associated with hint
     *
     * @return the training level id
     */
    public long getTrainingLevelId() {
        return trainingLevelId;
    }

    /**
     * Sets unique identification number of Training level associated with hint
     *
     * @param trainingLevelId the training level id
     */
    public void setTrainingLevelId(long trainingLevelId) {
        this.trainingLevelId = trainingLevelId;
    }

    /**
     * Gets content of solution
     *
     * @return the solution content
     */
    public String getSolutionContent() {
        return solutionContent;
    }

    /**
     * Sets content of solution
     *
     * @param solutionContent the solution content
     */
    public void setSolutionContent(String solutionContent) {
        this.solutionContent = solutionContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolutionInfo that = (SolutionInfo) o;
        return trainingLevelId == that.trainingLevelId && solutionContent.equals(that.solutionContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingLevelId, solutionContent);
    }

    @Override
    public String toString() {
        return "SolutionInfo{" +
                "trainingLevelId=" + trainingLevelId +
                ", solutionContent='" + solutionContent + '\'' +
                '}';
    }
}
