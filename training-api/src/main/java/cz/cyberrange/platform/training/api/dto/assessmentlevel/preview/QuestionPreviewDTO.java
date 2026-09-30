package cz.cyberrange.platform.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A question as shown to a participant looking at a level they have already reached, which may be
 * the level they are working on right now, carrying whatever they have submitted but never the
 * correct answers
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "A question shown to a participant, with their answers but no correct ones.")
public class QuestionPreviewDTO {

  @Schema(example = "1")
  private Long id;

  @Schema(
      description = "Free-form (FFQ), multiple choice (MCQ) or extended matching (EMI).",
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "FFQ")
  private QuestionType questionType = QuestionType.FFQ;

  @Schema(example = "What transport protocol is used for reliable transmission?")
  private String text = "Example Question";

  @Schema(description = "Zero-based position of the question in its level.", example = "0")
  private int order;

  @Schema(example = "true")
  private boolean answerRequired;

  /**
   * Choices displayed to the participant in case of FFQ or MCQ. Cleared to empty for a free-form
   * question in this preview, since the same choices double as the accepted answer texts; kept for
   * a multiple-choice question.
   */
  private List<QuestionChoicePreviewDTO> choices = new ArrayList<>();

  private List<ExtendedMatchingOptionDTO> extendedMatchingOptions = new ArrayList<>();

  /**
   * Statements displayed to the participant in case of EMI, each carrying the participant's chosen
   * option order but never the correct one
   */
  private List<ExtendedMatchingStatementPreviewDTO> extendedMatchingStatements = new ArrayList<>();

  /**
   * The participant's submitted answers for a free-form or multiple-choice question; left unset for
   * an extended matching question, whose answers are carried on the statements instead
   */
  @Schema(
      description = "The participant's answers; unset for an extended matching question.",
      example = "[\"An answer\"]")
  private Set<String> userAnswers;

  /**
   * Assigns the question's choices, sorted by each choice's order.
   *
   * @param choices the choices to assign
   */
  public void setChoices(List<QuestionChoicePreviewDTO> choices) {
    this.choices = choices;
    this.choices.sort(Comparator.comparingInt(QuestionChoicePreviewDTO::getOrder));
  }

  /**
   * Assigns the question's extended matching options, sorted by each option's order.
   *
   * @param extendedMatchingOptions the options to assign
   */
  public void setExtendedMatchingOptions(List<ExtendedMatchingOptionDTO> extendedMatchingOptions) {
    this.extendedMatchingOptions = extendedMatchingOptions;
    this.extendedMatchingOptions.sort(Comparator.comparingInt(ExtendedMatchingOptionDTO::getOrder));
  }

  /**
   * Assigns the question's extended matching statements, sorted by each statement's order.
   *
   * @param extendedMatchingStatements the statements to assign
   */
  public void setExtendedMatchingStatements(
      List<ExtendedMatchingStatementPreviewDTO> extendedMatchingStatements) {
    this.extendedMatchingStatements = extendedMatchingStatements;
    this.extendedMatchingStatements.sort(
        Comparator.comparingInt(ExtendedMatchingStatementPreviewDTO::getOrder));
  }
}
