package cz.cyberrange.platform.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.cyberrange.platform.training.api.dto.export.AbstractLevelExportDTO;
import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about training level. Inherits from {@link AbstractLevelImportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ApiModel(
    value = "TrainingLevelImportDTO",
    description = "Imported training level.",
    parent = AbstractLevelExportDTO.class)
@JsonIgnoreProperties({"reference_solution", "order"})
public class TrainingLevelImportDTO extends AbstractLevelImportDTO {

  /**
   * Blank is normalized to {@code null} on import. Required, and {@link #answerVariableName} must
   * then be {@code null}, when {@link #variantAnswers} is {@code false}; forbidden otherwise.
   */
  @ApiModelProperty(
      value = "Keyword found in training, used for access next level.",
      example = "secretAnswer")
  @Size(max = 50, message = "{trainingLevel.answer.Size.message}")
  @JsonAlias({"flag"})
  private String answer;

  /**
   * Blank is normalized to {@code null} on import. Required, and {@link #answer} must then be
   * {@code null}, when {@link #variantAnswers} is {@code true}; forbidden otherwise.
   */
  @ApiModelProperty(
      value = "Identifier that is used to obtain answer from remote storage.",
      example = "username")
  private String answerVariableName;

  @ApiModelProperty(
      value = "The information and experiences that are directed towards a participant.",
      example = "Play me")
  @NotEmpty(message = "{trainingLevel.content.NotEmpty.message}")
  private String content;

  @ApiModelProperty(
      value = "Instruction how to get answer in training.",
      example = "This is how you do it")
  @NotEmpty(message = "{trainingLevel.solution.NotEmpty.message}")
  private String solution;

  @ApiModelProperty(value = "Sign if displaying of solution is penalized.", example = "true")
  @NotNull(message = "{trainingLevel.solutionPenalized.NotNull.message}")
  private Boolean solutionPenalized;

  /** Rejected on import if the sum of every hint's penalty exceeds {@link #maxScore} */
  @Valid
  @ApiModelProperty(value = "Information which helps player resolve the level.")
  private Set<HintImportDTO> hints = new HashSet<>();

  @ApiModelProperty(
      value = "How many times player can submit incorrect answer before displaying solution.",
      example = "5")
  @NotNull(message = "{trainingLevel.incorrectAnswerLimit.NotNull.message}")
  @Min(value = 0, message = "{trainingLevel.incorrectAnswerLimit.Min.message}")
  @Max(value = 100, message = "{trainingLevel.incorrectAnswerLimit.Max.message}")
  @JsonAlias({"incorrect_flag_limit"})
  private Integer incorrectAnswerLimit;

  @Valid
  @ApiModelProperty(value = "List of attachments.", example = "[]")
  private List<AttachmentImportDTO> attachments;

  /** Caps the total penalty the level's {@link #hints} may carry; see {@link #hints} */
  @ApiModelProperty(
      value = "The maximum score a participant can achieve during a level.",
      example = "20")
  @NotNull(message = "{abstractLevel.maxScore.NotNull.message}")
  @Min(value = 0, message = "{abstractLevel.maxScore.Min.message}")
  @Max(value = 100, message = "{abstractLevel.maxScore.Max.message}")
  protected Integer maxScore;

  /**
   * Selects which of {@link #answer} or {@link #answerVariableName} the import requires: {@code
   * true} requires {@link #answerVariableName}, {@code false} requires {@link #answer}
   */
  @ApiModelProperty(
      value =
          "Marking if flags/answers are randomly generated and are different for each trainee. Default is false.",
      example = "false")
  @NotNull(message = "{trainingLevel.variantAnswers.NotNull.message}")
  private Boolean variantAnswers;

  @Valid
  @ApiModelProperty(value = "List of mitre techniques used in the training level.")
  private List<MitreTechniqueDTO> mitreTechniques;

  @ApiModelProperty(
      value = "Set of the expected commands to be executed during the training level.")
  private Set<String> expectedCommands;

  @ApiModelProperty(
      value =
          "Indicates if at least one command has to be executed to complete the level. Default is true.",
      example = "true")
  @NotNull(message = "{trainingLevel.commandsRequired.NotNull.message}")
  private Boolean commandsRequired;
}
