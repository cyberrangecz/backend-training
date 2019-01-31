package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * @author Dominik Pilár (445537)
 */
@ApiModel(value = "AssessmentLevelDTO", description = "A questionnaire or a test that is displayed to the participant.", parent = AbstractLevelDTO.class)
public class AssessmentLevelDTO extends AbstractLevelDTO {

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
        return "AssessmentLevelDTO [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel + ", snapshotHook="
                + snapshotHook + ", questions=" + questions + ", instructions=" + instructions + ", type="
                + assessmentType.name() + "]";
    }

}
