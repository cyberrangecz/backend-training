package cz.muni.ics.kypo.training.api.dto.archive;

import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "AssessmentLevelArchiveDTO", description = "A questionnaire or a test that is displayed to the participant.", parent = AbstractLevelArchiveDTO.class)
public class AssessmentLevelArchiveDTO extends AbstractLevelArchiveDTO{
    private String questions;
    private String instructions;
    private AssessmentType assessmentType;

    @ApiModelProperty(value = "List of questions in this assessment as JSON.", example = "What is my mothers name?")
    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
    }

    @ApiModelProperty(value = "Assessment instructions for participant.", example = "Fill me up")
    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    @ApiModelProperty(value = "Type of assessment.", example = "TEST")
    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    @Override
    public String toString() {
        return "AssessmentLevelArchiveDTO{" +
                "questions='" + questions + '\'' +
                ", instructions='" + instructions + '\'' +
                ", assessmentType=" + assessmentType +
                '}';
    }
}
