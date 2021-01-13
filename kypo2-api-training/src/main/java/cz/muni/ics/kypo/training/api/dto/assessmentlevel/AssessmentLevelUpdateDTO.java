package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Encapsulates information needed to update assessment level.
 *
 */
@ApiModel(value = "AssessmentLevelUpdateDTO", description = "Assessment level to update.")
public class AssessmentLevelUpdateDTO {

    @ApiModelProperty(value = "Main identifier of level.", required = true, example = "8")
    @NotNull(message = "{assessmentLevel.id.NotNull.message}")
    protected Long id;
    @ApiModelProperty(value = "Short textual description of the level.", required = true, example = "Assessment Level1")
    @NotEmpty(message = "{assessmentLevel.title.NotEmpty.message}")
    private String title;
    @ApiModelProperty(value = "Maximum score of assessment level to update. Have to be filled in range from 0 to 100.", required = true, example = "40")
    @NotNull(message = "{assessmentLevel.maxScore.NotNull.message}")
    @Min(value = 0, message = "{assessmentLevel.maxScore.Min.message}")
    @Max(value = 100, message = "{assessmentLevel.maxScore.Max.message}")
    private int maxScore;
    @ApiModelProperty(value = "Questions of assessment level to update.", example = "\"[{\"question_type\":\"FFQ\",\"text\":\"Which tool would you use to scan the open ports of a server?\",\"points\":6,\"penalty\":3,\"order\":0,\"answer_required\":true,\"correct_choices\":[\"nmap\",\"Nmap\"]}]\"")
    @NotNull(message = "{assessmentLevel.questions.NotNull.message}")
    private String questions;
    @ApiModelProperty(value = "Instructions of assessment level to update.", example = "Fill me up slowly")
    @NotNull(message = "{assessmentLevel.instructions.NotNull.message}")
    private String instructions;
    @ApiModelProperty(value = "Type of assessment level to update.", required = true, example = "TEST")
    @NotNull(message = "{assessmentLevel.type.NotNull.message}")
    private AssessmentType type;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", required = true, example = "5")
    private int estimatedDuration;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets max score.
     *
     * @return the max score
     */
    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Sets max score.
     *
     * @param maxScore the max score
     */
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    /**
     * Gets questions.
     *
     * @return the questions
     */
    public String getQuestions() {
        return questions;
    }

    /**
     * Sets questions.
     *
     * @param questions the questions
     */
    public void setQuestions(String questions) {
        this.questions = questions;
    }

    /**
     * Gets instructions.
     *
     * @return the instructions
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Sets instructions.
     *
     * @param instructions the instructions
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Gets assessment type.
     *
     * @return the {@link AssessmentType}
     */
    public AssessmentType getType() {
        return type;
    }

    /**
     * Sets assessment type.
     *
     * @param type the {@link AssessmentType}
     */
    public void setType(AssessmentType type) {
        this.type = type;
    }

    /**
     * Gets estimated duration.
     *
     * @return the estimated duration
     */
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated duration.
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @Override public String toString() {
        return "AssessmentLevelUpdateDTO{" + "id=" + id + ", title='" + title + '\'' + ", maxScore=" + maxScore + ", questions='"
            + questions + '\'' + ", instructions='" + instructions + '\'' + ", type=" + type + ", estimatedDuration=" + estimatedDuration
            + '}';
    }
}
