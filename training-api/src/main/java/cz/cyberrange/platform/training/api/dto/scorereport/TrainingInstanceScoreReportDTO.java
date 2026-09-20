package cz.cyberrange.platform.training.api.dto.scorereport;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standings of every participant of one training instance, alongside the level columns those
 * standings are broken down by
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standings of every participant of one training instance")
public class TrainingInstanceScoreReportDTO {

  @Schema(example = "1")
  @JsonProperty("training_instance_id")
  private Long trainingInstanceId;

  /** The instant every row's end is capped to when it falls beyond it */
  @Schema(
      description = "Epoch milliseconds every run's end is capped to.",
      example = "1665140389000")
  @JsonProperty("instance_end_at")
  private long instanceEndAt;

  /**
   * Every level able to award score, in definition order; info levels, access levels and assessment
   * levels of any kind other than a test are absent
   */
  @JsonProperty("scored_levels")
  private List<AbstractLevelBasicDTO> scoredLevels;

  /** One row per run of the instance, ordered by descending total score */
  private List<ParticipantScoreRowDTO> rows;
}
