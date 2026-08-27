package cz.cyberrange.platform.training.api.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Hint taken event, carrying the {@code type} value {@code hint_taken}. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(value = "HintTakenDTO", description = "Hint taken event")
public class HintTakenDTO extends TrainingEventDTO {

  @ApiModelProperty(value = "Hint ID")
  @JsonProperty("hint_id")
  private Long hintId;

  @ApiModelProperty(value = "Hint title")
  @JsonProperty("hint_title")
  private String hintTitle;

  @ApiModelProperty(value = "Hint penalty points")
  @JsonProperty("hint_penalty_points")
  private Integer hintPenaltyPoints;
}
