package cz.cyberrange.platform.training.api.dto.export;

import cz.cyberrange.platform.training.api.dto.imports.AttachmentImportDTO;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Encapsulates information about training level. Inherits from {@link AbstractLevelExportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Schema(description = "An exported training level, the task a trainee solves")
public class TrainingLevelExportDTO extends AbstractLevelExportDTO {

  @Schema(
      description = "The correct answer, when every trainee is given the same one",
      example = "secretAnswer")
  private String answer;

  @Schema(
      description = "Identifier each trainee's own answer is fetched under, when answers vary",
      example = "username")
  private String answerVariableName;

  @Schema(example = "Play me")
  private String content;

  @Schema(example = "This is how you do it")
  private String solution;

  @Schema(
      description = "Whether displaying the solution costs the trainee the level's whole score",
      example = "true")
  private boolean solutionPenalized;

  private Set<HintExportDTO> hints = new HashSet<>();

  @Schema(example = "5")
  private int incorrectAnswerLimit;

  /** Carries only each attachment's {@code content}; no other attachment field survives export */
  @Schema(description = "Only each attachment's content survives the export", example = "[]")
  private List<AttachmentImportDTO> attachments;

  @Schema(
      description = "Points for solving the level, before any hint or solution penalty",
      example = "20")
  private int maxScore;

  @Schema(description = "Whether each trainee is given their own correct answer", example = "false")
  private boolean variantAnswers;

  /** Each entry carries no id; only the technique's own data is exported */
  private Set<MitreTechniqueDTO> mitreTechniques;

  private Set<String> expectedCommands;

  @Schema(
      description = "Whether a correct answer with no command run beforehand counts as cheating",
      example = "true")
  private boolean commandsRequired;
}
