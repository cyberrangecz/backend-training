package cz.cyberrange.platform.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.BasicLevelInfoDTO;
import cz.cyberrange.platform.training.api.dto.hint.TakenHintDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** Encapsulates information about Training Run, intended as a response to run accessing */
@Data
@Schema(description = "The run's current level and the context a trainee needs to work through it")
public class AccessTrainingRunDTO {

  @Schema(example = "1")
  private Long trainingRunID;

  @Schema(example = "false")
  private boolean showStepperBar;

  /** Plain sandbox UUID, never hashed; the caller is always this run's owner or an admin */
  @Schema(
      description = "UUID of the sandbox assigned to this run.",
      example = "d2f6b1c4-9a3e-4c07-8b52-1e7a5c9d3f80")
  private String sandboxInstanceRefId;

  /** The training run's current level, not necessarily the first level of the definition */
  private AbstractLevelDTO abstractLevelDTO;

  private List<BasicLevelInfoDTO> infoAboutLevels;

  @Schema(example = "1")
  private Long instanceId;

  @Schema(example = "2016-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime startTime;

  /**
   * The current training level's solution text, set only when that level is a training level and
   * its solution has been taken; {@code null} otherwise. Despite its name, this does not hold a
   * boolean sign.
   */
  @Schema(
      description = "Solution text already revealed for the current training level, if any.",
      example = "Scan the target with nmap and read the open ports.")
  private String takenSolution;

  /**
   * Hints already taken in the current level. Populated only when resuming an existing run; empty
   * when a run is newly created.
   */
  private List<TakenHintDTO> takenHints = new ArrayList<>();

  @Schema(
      description =
          "True when sandboxes come from the sandbox definition locally instead of from a pool.",
      example = "true")
  private boolean localEnvironment;

  @Schema(example = "2")
  private Long sandboxDefinitionId;

  @Schema(example = "true")
  private boolean backwardMode;

  @Schema(example = "true")
  private boolean isLevelAnswered;

  /**
   * Appends the given hint to the list of hints taken so far.
   *
   * @param takenHintDTO the {@link TakenHintDTO} to append
   */
  public void addTakenHint(TakenHintDTO takenHintDTO) {
    this.takenHints.add(takenHintDTO);
  }
}
