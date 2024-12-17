package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CorrectAnswerDTO {

    @ApiModelProperty(value = "Main identifier of the training level.", example = "1")
    private Long levelId;
    @ApiModelProperty(value = "Short textual description of the training level.", example = "Training Level1")
    private String levelTitle;
    @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
    private Integer levelOrder;
    @ApiModelProperty(value = "Correct answer (static or variable) of the training level.", example = "john")
    private String correctAnswer;
    @ApiModelProperty(value = "Identifier of the variant answer.", example = "username")
    private String variableName;

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getLevelTitle() {
        return levelTitle;
    }

    public void setLevelTitle(String levelTitle) {
        this.levelTitle = levelTitle;
    }

    public Integer getLevelOrder() {
        return levelOrder;
    }

    public void setLevelOrder(Integer levelOrder) {
        this.levelOrder = levelOrder;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorrectAnswerDTO that = (CorrectAnswerDTO) o;
        return Objects.equals(getLevelId(), that.getLevelId()) && Objects.equals(getLevelTitle(), that.getLevelTitle()) && Objects.equals(getLevelOrder(), that.getLevelOrder()) && Objects.equals(getCorrectAnswer(), that.getCorrectAnswer()) && Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLevelId(), getLevelTitle(), getLevelOrder(), getCorrectAnswer(), getVariableName());
    }

    @Override
    public String toString() {
        return "CorrectAnswerDTO{" +
                "levelId=" + levelId +
                ", levelTitle='" + levelTitle + '\'' +
                ", levelOrder=" + levelOrder +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", variableName='" + variableName + '\'' +
                '}';
    }
}
