package cz.cyberrange.platform.training.api.dto.visualization.assessment;

import cz.cyberrange.platform.training.api.dto.visualization.assessment.answer.AbstractAnswerDTO;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information about question of the specific assessment. Data used for visualizations.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "QuestionVisualizationDTO", description = "Information needed to visualize assessments statistic.")
public class QuestionVisualizationDTO {

    @ApiModelProperty(value = "Main identifier of the question.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Type of the question.", required = true, example = "FFQ")
    private QuestionType questionType;
    @ApiModelProperty(value = "The content of the question.", example = "What transport protocol is used for reliable transmission?")
    private String text = "Example Question";
    @ApiModelProperty(value = "Order of the question, starts with 0", example = "0")
    private int order;
    @ApiModelProperty(value = "Free form question answers to questions submitted by participants.")
    private List<? extends AbstractAnswerDTO> answers = new ArrayList<>();
}
