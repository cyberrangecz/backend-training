package cz.muni.ics.kypo.training.api.dto;

import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Dominik Pilár (445537)
 *
 */
@ApiModel(value = "AssessmentLevelDTO", description = ".")
public class AssessmentLevelDTO extends AbstractLevelDTO {
    @NotEmpty(message = "Questions cannot be empty")
    private String questions;
    @NotEmpty
    private String instructions;
    @NotNull(message = "Assessment type must be set")
    private AssessmentType assessmentType;


    public AssessmentLevelDTO() {}


    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    @Override
    public String toString() {
        return "AssessmentLevelDTO [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel
                + ", preHook=" + preHook + ", postHook=" + postHook + ", questions=" + questions +
                ", instructions=" + instructions + ", type=" + assessmentType.name() + "]";
    }
}


