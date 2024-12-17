package cz.muni.ics.kypo.training.api.dto.visualization.assessment.answer;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@ApiModel(value = "FFQAnswerDTO", description = "Holds a data about the specific FFQ answer.")
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public class FFQAnswerDTO extends AbstractAnswerDTO {

    @ApiModelProperty(value = "Indicates the correctness of the MCQ option.", example = "false")
    private boolean isCorrect;
    @ApiModelProperty(value = "List of the participants that have chosen this FFQ answer.")
    private List<UserRefDTO> participants;

}
