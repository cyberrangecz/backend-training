package cz.muni.ics.kypo.training.api.dto.visualization.progress;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.ics.kypo.training.api.enums.LevelState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "LevelProgress", description = "Progress in level of player.")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LevelProgress {

    @ApiModelProperty(value = "User Ref ID.", required = true)
    @JsonProperty(value = "id", required = true)
    private long levelId;
    @ApiModelProperty(value = "Levels ID.", required = true)
    @JsonProperty(value = "state", required = true)
    private LevelState state;
    @ApiModelProperty(value = "Start time.", required = true)
    @JsonProperty(value = "start_time", required = true)
    private long startTime;
    @ApiModelProperty(value = "End time.", required = true)
    @JsonProperty(value = "end_time", required = true)
    private Long endTime;
    @ApiModelProperty(value = "Taken hints.", required = true)
    @JsonProperty(value = "hints_taken", required = true)
    private List<Long> hintsTaken = new ArrayList<>();
    @ApiModelProperty(value = "Number of wrong answers.", required = true)
    @JsonProperty(value = "wrong_answers_number", required = true)
    private Long wrongAnswersNumber;
    @ApiModelProperty(value = "Events belong to the respective level.", required = true)
    @JsonProperty(value = "events", required = true)
    private List<AbstractAuditPOJO> events;
    @ApiModelProperty(value = "Keyword found in game, used for access next level.", example = "secretAnswer")
    private String answer;

    public long getLevelId() {
        return levelId;
    }

    public void setLevelId(long levelId) {
        this.levelId = levelId;
    }

    public LevelState getState() {
        return state;
    }

    public void setState(LevelState state) {
        this.state = state;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public List<Long> getHintsTaken() {
        return hintsTaken;
    }

    public void setHintsTaken(List<Long> hintsTaken) {
        this.hintsTaken = hintsTaken;
    }

    public void addHintTaken(Long hintId) {
        this.hintsTaken.add(hintId);
    }

    public Long getWrongAnswersNumber() {
        return wrongAnswersNumber;
    }

    public void setWrongAnswersNumber(Long wrongAnswersNumber) {
        this.wrongAnswersNumber = wrongAnswersNumber;
    }

    public void increaseWrongAnswersNumber() {
        this.wrongAnswersNumber++;
    }

    public List<AbstractAuditPOJO> getEvents() {
        return events;
    }

    public void setEvents(List<AbstractAuditPOJO> events) {
        this.events = events;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "LevelProgress{" +
                "levelId=" + levelId +
                ", state=" + state +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", hintsTaken=" + hintsTaken +
                ", wrongAnswersNumber=" + wrongAnswersNumber +
                ", events=" + events +
                '}';
    }
}
