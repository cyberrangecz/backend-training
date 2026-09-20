package cz.cyberrange.platform.training.api.dto.imports;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Encapsulates information about assessment level. Inherits from {@link AbstractLevelImportDTO} */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "An assessment level to create, with the questions it asks")
public class AssessmentLevelImportDTO extends AbstractLevelImportDTO {

  /**
   * Has no effect unless {@link #assessmentType} is {@code TEST}, where each question's points are
   * summed into the created level's maximum score, and every extended-matching-item question must
   * carry a correct option order for each of its statements or the import is rejected. May be left
   * out of a submitted file, which leaves it empty, but a null value is refused.
   */
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description =
          "Their points make up the level's maximum score when the assessment is a test")
  @NotNull(message = "{assessmentLevel.questions.NotNull.message}")
  private List<QuestionDTO> questions = new ArrayList<>();

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Fill me up slowly")
  @NotNull(message = "{assessmentLevel.instructions.NotNull.message}")
  private String instructions;

  /**
   * When {@code TEST}, the created level's maximum score is computed from {@link #questions} rather
   * than accepted directly, and every extended-matching-item statement is checked and resolved to
   * its correct option. Other values leave the maximum score unset.
   */
  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      description =
          "A test is scored from its questions, and every matching statement has to name its"
              + " correct option",
      example = "TEST")
  @NotNull(message = "{assessmentLevel.type.NotNull.message}")
  private AssessmentType assessmentType;
}
