package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelDTO}
 *
 * @author Dominik Pilár (445537)
 */
@ApiModel(value = "AssessmentLevelDTO", description = "A questionnaire or a test that is displayed to the participant.", parent = AbstractLevelDTO.class)
public class AssessmentLevelDTO extends AbstractLevelDTO {

    private String questions;
    private String instructions;
    private AssessmentType assessmentType;

    /**
     * Gets questions.
     *
     * @return the questions
     */
    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
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
    @ApiModelProperty(value = "Assessment instructions for participant.", example = "Fill me up")
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
    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
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
        return "AssessmentLevelDTO{" + "questions='" + questions + '\'' + ", instructions='" + instructions + '\'' + ", assessmentType="
            + assessmentType + ", id=" + id + ", title='" + title + '\'' + ", maxScore=" + maxScore + ", snapshotHook=" + snapshotHook
            + ", levelType=" + levelType + ", estimatedDuration=" + estimatedDuration + ", trainingDefinition=" + trainingDefinition + '}';
    }
}
