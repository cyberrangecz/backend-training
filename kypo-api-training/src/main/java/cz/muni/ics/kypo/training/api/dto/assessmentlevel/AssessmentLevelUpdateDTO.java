package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information needed to update assessment level.
 *
 */
@ApiModel(value = "AssessmentLevelUpdateDTO", description = "Assessment level to update.")
@JsonIgnoreProperties(value = { "max_score" })
public class AssessmentLevelUpdateDTO extends AbstractLevelUpdateDTO {

    @ApiModelProperty(value = "Questions of assessment level to update.")
    @Valid
    private List<QuestionDTO> questions = new ArrayList<>();
    @ApiModelProperty(value = "Instructions of assessment level to update.", example = "Fill me up slowly")
    @NotNull(message = "{assessmentLevel.instructions.NotNull.message}")
    private String instructions;
    @ApiModelProperty(value = "Type of assessment level to update.", required = true, example = "TEST")
    @NotNull(message = "{assessmentLevel.type.NotNull.message}")
    private AssessmentType type;
    @ApiModelProperty(value = "Estimated time (minutes) taken by the player to solve the level.", required = true, example = "5")
    private int estimatedDuration;
    @ApiModelProperty(value = "Minimal possible solve time (minutes) that must be taken by the player to solve the level.", example = "5")
    protected Integer minimalPossibleSolveTime;

    public AssessmentLevelUpdateDTO() {
        this.levelType = LevelType.ASSESSMENT_LEVEL;
    }

    /**
     * Gets questions.
     *
     * @return the questions
     */
    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    /**
     * Sets questions.
     *
     * @param questions the questions
     */
    public void setQuestions(List<QuestionDTO> questions) {
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

    /**
     * Gets minimal possible solve time.
     *
     * @return the minimal possible solve time
     */
    public Integer getMinimalPossibleSolveTime() {
        return minimalPossibleSolveTime;
    }

    /**
     * Sets minimal possible solve time.
     *
     * @param minimalPossibleSolveTime the minimal possible solve time
     */
    public void setMinimalPossibleSolveTime(Integer minimalPossibleSolveTime) {
        this.minimalPossibleSolveTime = minimalPossibleSolveTime;
    }

    @Override
    public String toString() {
        return "AssessmentLevelUpdateDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", levelType=" + levelType +
                ", instructions='" + instructions + '\'' +
                ", type=" + type +
                ", estimatedDuration=" + estimatedDuration +
                ", minimalPossibleSolveTime=" + minimalPossibleSolveTime +
                '}';
    }
}
