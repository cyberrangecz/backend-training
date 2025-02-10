package cz.cyberrange.platform.training.api.dto.traininglevel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ValidateAnswerDTO {
    @ApiModelProperty(value = "Answer to be validated.", required = true, example = "answer")
    @NotEmpty(message = "{answerToValidate.answer.NotEmpty.message}")
    private String answer;
}
