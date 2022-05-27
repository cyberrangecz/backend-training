package cz.muni.ics.kypo.training.api.dto.visualization.analytical;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Analytical dashboard - training level details.
 */
@ApiModel(value = "LevelAnalyticalDashboardDTO", description = "Analytical dashboard - training level details.")
public class LevelAnalyticalDashboardDTO {

    @ApiModelProperty(value = "Identifier of the level.", example = "1")
    private Long levelId;
    @ApiModelProperty(value = "Level correct answer", example = "Secret answer")
    private String correctAnswer;
    @ApiModelProperty(value = "Overall number of correctly submitted answers. How many participants solved level.", example = "10")
    private int correctAnswersSubmitted;
    @ApiModelProperty(value = "List of all wrong answers submitted by participants.")
    private Set<String> wrongAnswers = new HashSet<>();

    public LevelAnalyticalDashboardDTO() {
    }

    public LevelAnalyticalDashboardDTO(Long levelId, String correctAnswer) {
        this.levelId = levelId;
        this.correctAnswer = correctAnswer;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public int getCorrectAnswersSubmitted() {
        return correctAnswersSubmitted;
    }

    public void setCorrectAnswersSubmitted(int correctAnswersSubmitted) {
        this.correctAnswersSubmitted = correctAnswersSubmitted;
    }

    public void addCorrectAnswerSubmit() {
        this.correctAnswersSubmitted++;
    }

    public Set<String> getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(Set<String> wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    @Override
    public String toString() {
        return "LevelAnalyticalDashboardDTO{" +
                "correctAnswer='" + correctAnswer + '\'' +
                ", correctAnswersSubmitted=" + correctAnswersSubmitted +
                ", wrongAnswers=" + wrongAnswers +
                '}';
    }
}
