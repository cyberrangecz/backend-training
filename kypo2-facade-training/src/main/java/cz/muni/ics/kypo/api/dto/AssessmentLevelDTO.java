package cz.muni.ics.kypo.api.dto;


import cz.muni.ics.kypo.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.model.TrainingDefinition;
import cz.muni.ics.kypo.model.TrainingRun;
import cz.muni.ics.kypo.model.enums.AssessmentType;
import io.swagger.annotations.ApiModel;

import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author Dominik Pilár (445537)
 *
 */
@ApiModel(value = "AssessmentLevelDTO", description = ".")
public class AssessmentLevelDTO extends AbstractLevelDTO {

    private String questions;
    private String instructions;
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
                + ", preHook=" + preHook + ", postHook=" + postHook + ", questions=" + questions +
                ", instructions=" + instructions + ", type=" + type.name() + "]";
    }
}


