package cz.muni.ics.kypo.training.api.dto.export;

import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelExportDTO}
 *
 */
@ApiModel(value = "AssessmentLevelExportDTO", description = "Exported assessment level.", parent = AbstractLevelExportDTO.class)
public class AssessmentLevelExportDTO extends AbstractLevelExportDTO {

    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    private String questions;
    @ApiModelProperty(value = "Assessment instructions for participant.", example = "Fill me up")
    private String instructions;
    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;

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
    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    /**
     * Sets assessment type.
     *
     * @param assessmentType {@link AssessmentType}
     */
    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    @Override
    public String toString() {
        return "AssessmentLevelExportDTO{" +
                "questions='" + questions + '\'' +
                ", instructions='" + instructions + '\'' +
                ", assessmentType=" + assessmentType +
                '}';
    }
}
