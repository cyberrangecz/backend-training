package cz.cyberrange.platform.training.api.dto.scorereport;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One training run of the instance, with the trainee behind it, the span it occupied, and every
 * score and tally derived for it
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "One participant's standing in a training instance's score report")
public class ParticipantScoreRowDTO {

  /** Position among the rows of the same report; one-based, lower is better */
  @Schema(example = "1")
  private int rank;

  @Schema(example = "1")
  @JsonProperty("training_run_id")
  private Long trainingRunId;

  /** The id the trainee is known by outside this service, not the local {@code UserRef} key */
  @Schema(description = "The id the participant is known by outside this service.", example = "1")
  @JsonProperty("user_ref_id")
  private Long userRefId;

  @Schema(example = "999999@mail.example.cz")
  private String login;

  /**
   * Falls back to the login when no display name resolved, and further to the string form of {@link
   * #userRefId} when the login is empty too
   */
  @Schema(example = "Mgr. John Doe")
  private String name;

  @Schema(example = "johndoe@mail.example.cz")
  private String mail;

  @Schema(example = "true")
  private boolean finished;

  @Schema(description = "Epoch milliseconds at which the run started.", example = "1665136789000")
  @JsonProperty("started_at")
  private long startedAt;

  @Schema(description = "Epoch milliseconds at which the run ended.", example = "1665140389000")
  @JsonProperty("ended_at")
  private Long endedAt;

  @Schema(example = "3600")
  @JsonProperty("duration_seconds")
  private Long durationSeconds;

  /**
   * Holds an entry only for a score-bearing level the run has completed at least once; a
   * score-bearing level never completed, and any level unable to award score, has none
   */
  @JsonProperty("score_by_level_id")
  private Map<Long, Integer> scoreByLevelId = new HashMap<>();

  /** Zero when the run produced no audit events at all, rather than left unset */
  @Schema(example = "80")
  @JsonProperty("training_score")
  private int trainingScore;

  /** Zero when the run produced no audit events at all, rather than left unset */
  @Schema(example = "20")
  @JsonProperty("assessment_score")
  private int assessmentScore;

  @Schema(example = "100")
  @JsonProperty("total_score")
  private int totalScore;

  /** Zero when the run recorded no hint being taken, rather than left unset */
  @Schema(example = "4")
  @JsonProperty("hints_taken")
  private int hintsTaken;

  /**
   * Excludes wrong answers recorded on a passkey-gated level, since such a level records one for
   * every attempt at its passkey, the successful attempt included
   */
  @Schema(example = "2")
  @JsonProperty("wrong_answers")
  private int wrongAnswers;

  /** Zero when the run recorded no solution being revealed, rather than left unset */
  @Schema(example = "1")
  @JsonProperty("solutions_displayed")
  private int solutionsDisplayed;
}
