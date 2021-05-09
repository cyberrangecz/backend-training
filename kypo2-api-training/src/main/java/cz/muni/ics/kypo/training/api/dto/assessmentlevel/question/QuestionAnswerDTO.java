package cz.muni.ics.kypo.training.api.dto.assessmentlevel.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApiModel(
        value = "QuestionAnswerDTO"
)
public class QuestionAnswerDTO {

    @ApiModelProperty(value = "ID of answered question", example = "1")
    @NotNull(message = "{questionAnswer.questionId.NotNull.message}")
    private Long questionId;
    @ApiModelProperty(value = "Answers to the question", example = "[\"An answer\"]")
    private Set<String> answers;
    @ApiModelProperty(value = "Mapping of the answers to question of type extended matching items", example = "{ \"1\": [2, 3]")
    private Map<Integer, Integer> extendedMatchingPairs;


    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Set<String> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<String> answers) {
        this.answers = answers;
    }

    public Map<Integer, Integer> getExtendedMatchingPairs() {
        return extendedMatchingPairs;
    }

    public void setExtendedMatchingPairs(Map<Integer, Integer> extendedMatchingPairs) {
        this.extendedMatchingPairs = extendedMatchingPairs;
    }

    @Override
    public String toString() {
        return "QuestionAnswerDTO{" +
                "questionId=" + questionId +
                ", answers='" + answers + '\'' +
                '}';
    }
}
