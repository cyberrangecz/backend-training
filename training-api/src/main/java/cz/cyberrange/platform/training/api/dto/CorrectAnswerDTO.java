package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@Getter
@Setter
@ToString
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
    @ApiModelProperty(value = "Level type: TRAINING or ASSESSMENT. Omitted for backward compatibility when TRAINING.")
    private String levelType;
    @ApiModelProperty(value = "For ASSESSMENT levels: correct answers per question. Empty or null for TRAINING levels.")
    private List<QuestionCorrectAnswerDTO> questionCorrectAnswers;
}
