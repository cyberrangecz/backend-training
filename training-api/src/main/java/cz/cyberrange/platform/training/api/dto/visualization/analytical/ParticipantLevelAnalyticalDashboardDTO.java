package cz.cyberrange.platform.training.api.dto.visualization.analytical;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Analytical dashboard - level details of the particular participant.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "ParticipantLevelAnalyticalDashboardDTO", description = "Analytical dashboard - level details of the particular participant.")
public class ParticipantLevelAnalyticalDashboardDTO {

    @ApiModelProperty(value = "Identifier of the level.", example = "1")
    private Long levelId;
    @ApiModelProperty(value = "The title of the level.", example = "Find open ports")
    private String levelTitle;
    @ApiModelProperty(value = "Number of hint taken during level.", example = "Play me")
    private int hintsTaken;
    @ApiModelProperty(value = "Time spent in the level.", example = "This is how you do it")
    private long duration;
    @ApiModelProperty(value = "Keyword found in training, used for access next level.", example = "secretAnswer")
    private int score;
    @ApiModelProperty(value = "Indicates if the participant submitted a wrong answer.", example = "true")
    private List<String> wrongAnswers = new ArrayList<>();

    public void increaseHintTaken() {
        this.hintsTaken++;
    }

    public void addWrongAnswer(String wrongAnswer) {
        this.wrongAnswers.add(wrongAnswer);
    }

}
