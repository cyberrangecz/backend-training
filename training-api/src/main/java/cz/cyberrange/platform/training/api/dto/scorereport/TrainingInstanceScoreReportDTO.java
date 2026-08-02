package cz.cyberrange.platform.training.api.dto.scorereport;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Standings of every participant of one training instance, alongside the level columns those
 * standings are broken down by.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(
    value = "TrainingInstanceScoreReportDTO",
    description = "Per-participant score report of a training instance.")
public class TrainingInstanceScoreReportDTO {

  @ApiModelProperty(value = "Identifier of the reported training instance.", example = "1")
  @JsonProperty("training_instance_id")
  private Long trainingInstanceId;

  @ApiModelProperty(
      value = "Instance end used to cap every run, in epoch milliseconds.",
      example = "1665140389000")
  @JsonProperty("instance_end_at")
  private long instanceEndAt;

  @ApiModelProperty(value = "Score-bearing levels, in definition order.")
  @JsonProperty("scored_levels")
  private List<AbstractLevelBasicDTO> scoredLevels;

  @ApiModelProperty(value = "Participants, ordered by rank.")
  private List<ParticipantScoreRowDTO> rows;
}
