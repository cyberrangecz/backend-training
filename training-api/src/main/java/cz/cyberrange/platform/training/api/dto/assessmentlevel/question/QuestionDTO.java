package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.ValidOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A question of an assessment level, together with the answer options relevant to its question
 * type.
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QuestionDTO extends QuestionBasicDTO {

  @ApiModelProperty(
      value = "The content of the question.",
      example = "What transport protocol is used for reliable transmission?")
  @NotEmpty(message = "{question.text.NotEmpty.message}")
  private String text = "Example Question";

  /** Answer choices offered when the question type is free-form or multiple-choice. */
  @ApiModelProperty(value = "Choices displayed to the participant in case of FFQ or MCQ.")
  @Valid
  @ValidOrder
  private List<QuestionChoiceDTO> choices = new ArrayList<>();

  /** Answer options offered when the question type is extended matching. */
  @ApiModelProperty(value = "Options displayed to the participant in case of EMI.")
  @Valid
  @ValidOrder
  private List<ExtendedMatchingOptionDTO> extendedMatchingOptions = new ArrayList<>();

  /** Statements to be matched against options when the question type is extended matching. */
  @ApiModelProperty(value = "Statements displayed to the participant in case of EMI.")
  @Valid
  @ValidOrder
  private List<ExtendedMatchingStatementDTO> extendedMatchingStatements = new ArrayList<>();

  /**
   * Assigns the question's choices, sorted by each choice's order.
   *
   * @param choices the choices to assign
   */
  public void setChoices(List<QuestionChoiceDTO> choices) {
    this.choices = choices;
    this.choices.sort(Comparator.comparingInt(QuestionChoiceDTO::getOrder));
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
      List<ExtendedMatchingStatementDTO> extendedMatchingStatements) {
    this.extendedMatchingStatements = extendedMatchingStatements;
    this.extendedMatchingStatements.sort(
        Comparator.comparingInt(ExtendedMatchingStatementDTO::getOrder));
  }
}
