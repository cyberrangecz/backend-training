package cz.muni.ics.kypo.training.api.dto;


import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.model.TrainingRun;
import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotEmpty;

/**
 *
 * @author Dominik Pilár (445537)
 *
 */
@ApiModel(value = "AssessmentLevelDTO", description = ".")
public class AssessmentLevelDTO extends AbstractLevelDTO {
    @NotEmpty
    private String questions;
    @NotEmpty
    private String instructions;
    @NotEmpty
    private AssessmentType type;


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

    public AssessmentType getType() {
        return type;
    }

    public void setType(AssessmentType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AssessmentLevelDTO [id=" + id + ", title=" + title + ", maxScore=" + maxScore + ", nextLevel=" + nextLevel
                + ", trainingDefinition=" + trainingDefinition + ", preHook=" + preHook + ", postHook=" + postHook + ", questions=" + questions +
                ", instructions=" + instructions + ", type=" + type.name() + "]";
    }
}


