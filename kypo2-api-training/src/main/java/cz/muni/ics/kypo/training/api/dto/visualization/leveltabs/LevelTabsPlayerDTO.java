package cz.muni.ics.kypo.training.api.dto.visualization.leveltabs;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LevelTabsPlayerDTO {

    private Long id;
    private long participantLevelScore;
    private Integer hints;
    private List<String> wrongFlags = new ArrayList<>();
    private long time;
    private Boolean displayedSolution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getParticipantLevelScore() {
        return participantLevelScore;
    }

    public void setParticipantLevelScore(long participantLevelScore) {
        this.participantLevelScore = participantLevelScore;
    }

    public Integer getHints() {
        return hints;
    }

    public void addHint() {
        if(this.hints == null) {
            this.hints = 0;
        }
        this.hints++;
    }

    public void setHints(Integer hints) {
        this.hints = hints;
    }

    public List<String> getWrongFlags() {
        return wrongFlags;
    }

    public void addWrongFlag(String wrongFlag) {
        this.wrongFlags.add(wrongFlag);
    }

    public void setWrongFlags(List<String> wrongFlags) {
        this.wrongFlags = wrongFlags;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Boolean isDisplayedSolution() {
        return displayedSolution;
    }

    public void setDisplayedSolution(Boolean displayedSolution) {
        this.displayedSolution = displayedSolution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LevelTabsPlayerDTO that = (LevelTabsPlayerDTO) o;
        return getParticipantLevelScore() == that.getParticipantLevelScore() &&
                getTime() == that.getTime() &&
                getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getParticipantLevelScore(), getTime());
    }

    @Override
    public String toString() {
        return "LevelTabPlayerDTO{" +
                "id=" + id +
                ", participantLevelScore=" + participantLevelScore +
                ", hints=" + hints +
                ", wrongFlags=" + wrongFlags +
                ", time=" + time +
                ", displayedSolution=" + displayedSolution +
                '}';
    }
}
