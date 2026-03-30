package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Correct answer(s) for a single question within an assessment level.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "QuestionCorrectAnswerDTO", description = "Correct answer(s) for one question in an assessment level")
public class QuestionCorrectAnswerDTO {

    @ApiModelProperty(value = "Question identifier", example = "1")
    private Long questionId;
    @ApiModelProperty(value = "Question text", example = "What is the capital of France?")
    private String questionText;
    @ApiModelProperty(value = "Correct answer(s). For MCQ/FFQ: list of correct choice texts. For EMI: list of 'statement -> option' strings.")
    private List<String> correctAnswers;
}
