package cz.muni.ics.kypo.training.api.dto.archive;

import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;


/**
 * Encapsulates information about assessment level. Inherits from {@link AbstractLevelArchiveDTO}
 * Used for archiving.
 */
@ApiModel(value = "AssessmentLevelArchiveDTO", description = "Archived assessment level.", parent = AbstractLevelArchiveDTO.class)
public class AssessmentLevelArchiveDTO extends AbstractLevelArchiveDTO{
    @ApiModelProperty(value = "List of questions in this assessment.", example = "What is my mothers name?")
    private List<QuestionDTO> questions;
    @ApiModelProperty(value = "Assessment instructions for participant.", example = "Fill me up")
    private String instructions;
    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    private AssessmentType assessmentType;

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
     * @return the assessment type
     */
    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    /**
     * Sets assessment type.
     *
     * @param assessmentType the assessment type
     */
    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    @Override
    public String toString() {
        return "AssessmentLevelArchiveDTO{" +
                ", instructions='" + instructions + '\'' +
                ", assessmentType=" + assessmentType +
                '}';
    }
}
