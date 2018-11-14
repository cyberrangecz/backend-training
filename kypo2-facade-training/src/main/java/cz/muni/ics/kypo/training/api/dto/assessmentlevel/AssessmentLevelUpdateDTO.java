package cz.muni.ics.kypo.training.api.dto.assessmentlevel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "AssessmentLevelUpdateDTO", description = "Assessment level to update.")
public class AssessmentLevelUpdateDTO {


    @NotNull(message = "{assessmentlevelupdate.id.NotNull.message}")
    protected Long id;
    @NotEmpty(message = "{assessmentlevelupdate.title.NotEmpty.message}")
    private String title;
    @NotNull(message = "{assessmentlevelupdate.maxScore.NotNull.message}")
    @Min(value = 0, message = "{assessmentlevelupdate.maxScore.Min.message}")
    @Max(value = 100, message = "{assessmentlevelupdate.maxScore.Max.message}")
    private Integer maxScore;
    private String questions;
    private String instructions;
    @NotNull(message = "{assessmentlevelupdate.type.NotNull.message}")
    private AssessmentType type;

    @ApiModelProperty(value = "Main identifier of level.", required = true)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Short textual description of the level.", required = true)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Maximum score of assessment level to update. Have to be filled in range from 0 to 100.", required = true)
    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    @ApiModelProperty(value = "Questions of assessment level to update.")
    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
    }

    @ApiModelProperty(value = "Instructions of assessment level to update.")
    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    @ApiModelProperty(value = "Type of assessment level to update.", required = true)
    public AssessmentType getType() {
        return type;
    }

    public void setType(AssessmentType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AssessmentLevelUpdateDTO [id=");
        builder.append(id);
        builder.append(", title=");
        builder.append(title);
        builder.append(", maxScore=");
        builder.append(maxScore);
        builder.append(", questions=");
        builder.append(questions);
        builder.append(", instructions=");
        builder.append(instructions);
        builder.append(", type=");
        builder.append(type);
        builder.append("]");
        return builder.toString();
    }

}
