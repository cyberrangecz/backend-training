package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Class represents information of hint associated with current level of training run
 */
@Embeddable
public class HintInfo {
    @Column(name = "game_level_id", nullable = false)
    private long gameLevelId;
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
     * @param gameLevelId unique identification number of Game level associated with hint
     * @param hintId      unique identification number of Hint
     * @param hintTitle   title of Hint
     * @param hintContent content of Hint
     */
    public HintInfo(long gameLevelId, long hintId, String hintTitle, String hintContent, int order) {
        this.gameLevelId = gameLevelId;
        this.hintId = hintId;
        this.hintTitle = hintTitle;
        this.hintContent = hintContent;
        this.order = order;
    }

    /**
     * Gets unique identification number of Game level associated with hint
     *
     * @return the game level id
     */
    public Long getGameLevelId() {
        return gameLevelId;
    }

    /**
     * Sets unique identification number of Game level associated with hint
     *
     * @param gameLevelId the game level id
     */
    public void setGameLevelId(Long gameLevelId) {
        this.gameLevelId = gameLevelId;
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

    public void setGameLevelId(long gameLevelId) {
        this.gameLevelId = gameLevelId;
    }

    public void setHintId(long hintId) {
        this.hintId = hintId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HintInfo)) return false;
        HintInfo hintInfo = (HintInfo) object;
        return Objects.equals(getGameLevelId(), hintInfo.getGameLevelId()) &&
                Objects.equals(getHintId(), hintInfo.getHintId()) &&
                Objects.equals(getHintTitle(), hintInfo.getHintTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGameLevelId(), getHintId(), getHintTitle());
    }

    @Override
    public String toString() {
        return "HintInfo{" +
                "gameLevelId=" + gameLevelId +
                ", hintId=" + hintId +
                ", hintTitle='" + hintTitle + '\'' +
                ", hintContent='" + hintContent + '\'' +
                ", order=" + order +
                '}';
    }
}
