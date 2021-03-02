package cz.muni.ics.kypo.training.api.dto.visualization.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.ics.kypo.training.api.enums.LevelState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "LevelProgress", description = "Progress in level of player.")
@JsonRootName(value = "level_progress")
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
    private long endTime;
    @ApiModelProperty(value = "Taken hints.", required = true)
    @JsonProperty(value = "hints_taken", required = true)
    private List<Long> hintsTaken;
    @ApiModelProperty(value = "Number of wrong flags.", required = true)
    @JsonProperty(value = "wrong_flags_number", required = true)
    private long wrongFlagsNumber;
    @ApiModelProperty(value = "Events belong to the respective level.", required = true)
    @JsonProperty(value = "events", required = true)
    private List<AbstractAuditPOJO> events;


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

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<Long> getHintsTaken() {
        return hintsTaken;
    }

    public void setHintsTaken(List<Long> hintsTaken) {
        this.hintsTaken = hintsTaken;
    }

    public long getWrongFlagsNumber() {
        return wrongFlagsNumber;
    }

    public void setWrongFlagsNumber(long wrongFlagsNumber) {
        this.wrongFlagsNumber = wrongFlagsNumber;
    }

    public List<AbstractAuditPOJO> getEvents() {
        return events;
    }

    public void setEvents(List<AbstractAuditPOJO> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "LevelProgress{" +
                "levelId=" + levelId +
                ", state=" + state +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", hintsTaken=" + hintsTaken +
                ", wrongFlagsNumber=" + wrongFlagsNumber +
                ", events=" + events +
                '}';
    }
}
