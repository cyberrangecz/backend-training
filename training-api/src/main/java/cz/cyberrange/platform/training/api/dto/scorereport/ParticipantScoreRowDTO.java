package cz.cyberrange.platform.training.api.dto.scorereport;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One training run of the instance, with the trainee behind it, the span it occupied, and every
 * score and tally derived for it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(
    value = "ParticipantScoreRowDTO",
    description = "A single participant's standing in the training instance.")
public class ParticipantScoreRowDTO {

  @ApiModelProperty(
      value = "Position by total score descending, resolving ties in favour of the shorter run.",
      example = "1")
  private int rank;

  @ApiModelProperty(value = "Identifier of the training run.", example = "1")
  @JsonProperty("training_run_id")
  private Long trainingRunId;

  @ApiModelProperty(
      value = "Reference to the trainee in the user-and-group microservice.",
      example = "1")
  @JsonProperty("user_ref_id")
  private Long userRefId;

  @ApiModelProperty(
      value = "Trainee login, empty when the reference could not be resolved.",
      example = "999999@mail.example.cz")
  private String login;

  @ApiModelProperty(
      value = "Trainee display name, falling back to the login and then the reference id.",
      example = "Mgr. John Doe")
  private String name;

  @ApiModelProperty(
      value = "Trainee email, empty when the reference could not be resolved.",
      example = "johndoe@mail.example.cz")
  private String mail;

  @ApiModelProperty(
      value =
          "Whether the run has ended, either by its own state or because its instance has ended.",
      example = "true")
  private boolean finished;

  @ApiModelProperty(value = "Run start, in epoch milliseconds.", example = "1665136789000")
  @JsonProperty("started_at")
  private long startedAt;

  @ApiModelProperty(
      value = "Run end capped at the instance end, in epoch milliseconds; null while running.",
      example = "1665140389000")
  @JsonProperty("ended_at")
  private Long endedAt;

  @ApiModelProperty(
      value = "Length of the run in whole seconds; null while running.",
      example = "3600")
  @JsonProperty("duration_seconds")
  private Long durationSeconds;

  @ApiModelProperty(
      value = "Score attained per level, keyed by level id, omitting levels never completed.")
  @JsonProperty("score_by_level_id")
  private Map<Long, Integer> scoreByLevelId = new HashMap<>();

  @ApiModelProperty(value = "Cumulative score across training levels.", example = "80")
  @JsonProperty("training_score")
  private int trainingScore;

  @ApiModelProperty(value = "Cumulative score across assessment levels.", example = "20")
  @JsonProperty("assessment_score")
  private int assessmentScore;

  @ApiModelProperty(value = "Combined training and assessment score.", example = "100")
  @JsonProperty("total_score")
  private int totalScore;

  @ApiModelProperty(value = "Hints taken across the whole run.", example = "4")
  @JsonProperty("hints_taken")
  private int hintsTaken;

  @ApiModelProperty(
      value = "Wrong answers submitted across the whole run, excluding passkey retries.",
      example = "2")
  @JsonProperty("wrong_answers")
  private int wrongAnswers;

  @ApiModelProperty(value = "Solutions revealed across the whole run.", example = "1")
  @JsonProperty("solutions_displayed")
  private int solutionsDisplayed;
}
