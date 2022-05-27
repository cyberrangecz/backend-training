package cz.muni.ics.kypo.training.api.dto.assessmentlevel.preview;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelDTO}
 *
 */
@ApiModel(value = "AssessmentLevelPreviewDTO", description = "A questionnaire or a test that is displayed to the participant.", parent = AbstractLevelDTO.class)
public class AssessmentLevelPreviewDTO extends AbstractLevelDTO {

    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    private List<QuestionPreviewDTO> questions;
    @ApiModelProperty(value = "Assessment instructions for participant.", example = "Fill me up")
    private String instructions;
    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;

    /**
     * Gets questions.
     *
     * @return the questions
     */
    public List<QuestionPreviewDTO> getQuestions() {
        return questions;
    }

    /**
     * Sets questions.
     *
     * @param questions the questions
     */
    public void setQuestions(List<QuestionPreviewDTO> questions) {
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
    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    /**
     * Sets assessment type.
     *
     * @param assessmentType the {@link AssessmentType}
     */
    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    @Override public String toString() {
        return "AssessmentLevelPreviewDTO{" + "questions='" + questions + '\'' + ", instructions='" + instructions + '\'' + ", assessmentType="
            + assessmentType + ", id=" + id + ", title='" + title + '\'' + ", maxScore=" + maxScore + ", snapshotHook=" + snapshotHook
            + ", levelType=" + levelType + ", estimatedDuration=" + estimatedDuration + ", trainingDefinition=" + trainingDefinition + '}';
    }
}
