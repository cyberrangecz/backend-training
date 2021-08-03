package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Class represents information of hint associated with current level of training run
 */
@Embeddable
public class HintInfo {
    @Column(name = "training_level_id", nullable = false)
    private long trainingLevelId;
    @Column(name = "hint_id", nullable = false)
    private long hintId;
    @Column(name = "hint_title", nullable = false)
    private String hintTitle;
    @Column(name = "hint_content", nullable = false)
    private String hintContent;
    @Column(name = "order_in_level", nullable = false)
    private int order;

    /**
     * Instantiates a new Hint info
     */
    public HintInfo() {
    }

    /**
     * Instantiates a new Hint info
     *
     * @param trainingLevelId unique identification number of Training level associated with hint
     * @param hintId      unique identification number of Hint
     * @param hintTitle   title of Hint
     * @param hintContent content of Hint
     * @param order       the order
     */
    public HintInfo(long trainingLevelId, long hintId, String hintTitle, String hintContent, int order) {
        this.trainingLevelId = trainingLevelId;
        this.hintId = hintId;
        this.hintTitle = hintTitle;
        this.hintContent = hintContent;
        this.order = order;
    }

    /**
     * Gets unique identification number of Training level associated with hint
     *
     * @return the training level id
     */
    public Long getTrainingLevelId() {
        return trainingLevelId;
    }

    /**
     * Sets unique identification number of Training level associated with hint
     *
     * @param trainingLevelId the training level id
     */
    public void setTrainingLevelId(Long trainingLevelId) {
        this.trainingLevelId = trainingLevelId;
    }

    /**
     * Gets unique identification number of Hint
     *
     * @return the hint id
     */
    public Long getHintId() {
        return hintId;
    }

    /**
     * Sets unique identification number of Hint
     *
     * @param hintId the hint id
     */
    public void setHintId(Long hintId) {
        this.hintId = hintId;
    }

    /**
     * Gets title of Hint
     *
     * @return the hint title
     */
    public String getHintTitle() {
        return hintTitle;
    }

    /**
     * Sets title of Hint
     *
     * @param hintTitle the hint title
     */
    public void setHintTitle(String hintTitle) {
        this.hintTitle = hintTitle;
    }

    /**
     * Gets content of Hint
     *
     * @return the hint content
     */
    public String getHintContent() {
        return hintContent;
    }

    /**
     * Sets content of Hint
     *
     * @param hintContent the hint content
     */
    public void setHintContent(String hintContent) {
        this.hintContent = hintContent;
    }

    /**
     * Sets training level id.
     *
     * @param trainingLevelId the training level id
     */
    public void setTrainingLevelId(long trainingLevelId) {
        this.trainingLevelId = trainingLevelId;
    }

    /**
     * Sets hint id.
     *
     * @param hintId the hint id
     */
    public void setHintId(long hintId) {
        this.hintId = hintId;
    }

    /**
     * Gets order.
     *
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets order.
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HintInfo)) return false;
        HintInfo hintInfo = (HintInfo) object;
        return Objects.equals(getTrainingLevelId(), hintInfo.getTrainingLevelId()) &&
                Objects.equals(getHintId(), hintInfo.getHintId()) &&
                Objects.equals(getHintTitle(), hintInfo.getHintTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTrainingLevelId(), getHintId(), getHintTitle());
    }

    @Override
    public String toString() {
        return "HintInfo{" +
                "trainingLevelId=" + trainingLevelId +
                ", hintId=" + hintId +
                ", hintTitle='" + hintTitle + '\'' +
                ", hintContent='" + hintContent + '\'' +
                ", order=" + order +
                '}';
    }
}
