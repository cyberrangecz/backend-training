package cz.cyberrange.platform.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import io.swagger.annotations.ApiModelProperty;
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
public class QuestionPreviewDTO {

  @ApiModelProperty(value = "Main identifier of the question.", example = "1")
  private Long id;

  @ApiModelProperty(value = "Type of the question.", required = true, example = "FFQ")
  private QuestionType questionType = QuestionType.FFQ;

  @ApiModelProperty(
      value = "The content of the question.",
      example = "What transport protocol is used for reliable transmission?")
  private String text = "Example Question";

  @ApiModelProperty(value = "Order of the question, starts with 0", example = "0")
  private int order;

  @ApiModelProperty(
      value = "Sign if the question must be answered by the participant or not.",
      example = "true")
  private boolean answerRequired;

  /**
   * Choices displayed to the participant in case of FFQ or MCQ. Cleared to empty for a free-form
   * question in this preview, since the same choices double as the accepted answer texts; kept for
   * a multiple-choice question.
   */
  @ApiModelProperty(value = "Choices displayed to the participant in case of FFQ or MCQ.")
  private List<QuestionChoicePreviewDTO> choices = new ArrayList<>();

  @ApiModelProperty(value = "Options displayed to the participant in case of EMI.")
  private List<ExtendedMatchingOptionDTO> extendedMatchingOptions = new ArrayList<>();

  /**
   * Statements displayed to the participant in case of EMI, each carrying the participant's chosen
   * option order but never the correct one
   */
  @ApiModelProperty(value = "Statements displayed to the participant in case of EMI.")
  private List<ExtendedMatchingStatementPreviewDTO> extendedMatchingStatements = new ArrayList<>();

  /**
   * The participant's submitted answers for a free-form or multiple-choice question; left unset for
   * an extended matching question, whose answers are carried on the statements instead
   */
  @ApiModelProperty(value = "User answers to the question", example = "[\"An answer\"]")
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
