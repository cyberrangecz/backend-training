package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

@Embeddable
public class HintInfo {
    @Column(name = "game_level_id", nullable = false)
    private long gameLevelId;
    @Column(name = "hint_id", nullable = false)
    private long hintId;
    @Column(name = "hint_title", nullable = false)
    private String hintTitle;
    @Lob
    @Column(name = "hint_content", nullable = false)
    private String hintContent;

    public HintInfo() {}

    public HintInfo(long gameLevelId, long hintId, String hintTitle, String hintContent) {
        this.gameLevelId = gameLevelId;
        this.hintId = hintId;
        this.hintTitle = hintTitle;
        this.hintContent = hintContent;
    }

    public Long getGameLevelId() {
        return gameLevelId;
    }

    public void setGameLevelId(Long gameLevelId) {
        this.gameLevelId = gameLevelId;
    }

    public Long getHintId() {
        return hintId;
    }

    public void setHintId(Long hintId) {
        this.hintId = hintId;
    }

    public String getHintTitle() {
        return hintTitle;
    }

    public void setHintTitle(String hintTitle) {
        this.hintTitle = hintTitle;
    }

    public String getHintContent() {
        return hintContent;
    }

    public void setHintContent(String hintContent) {
        this.hintContent = hintContent;
    }
}
