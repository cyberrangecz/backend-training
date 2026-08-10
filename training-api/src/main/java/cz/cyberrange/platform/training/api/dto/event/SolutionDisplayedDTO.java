package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "SolutionDisplayedDTO", description = "Solution displayed event")
public class SolutionDisplayedDTO extends TrainingEventDTO {

  @ApiModelProperty(value = "Penalty points")
  @JsonProperty("penalty_points")
  private Integer penaltyPoints;
}
