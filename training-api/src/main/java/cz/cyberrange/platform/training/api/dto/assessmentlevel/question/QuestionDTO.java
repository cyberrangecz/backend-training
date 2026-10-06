package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.ValidOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A question of an assessment level, together with the answer options relevant to its question type
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "An assessment question with the answer options its type calls for.")
public class QuestionDTO extends QuestionBasicDTO {

  @Schema(
      requiredMode = Schema.RequiredMode.REQUIRED,
      example = "What transport protocol is used for reliable transmission?")
  @NotEmpty(message = "{question.text.NotEmpty.message}")
  private String text = "Example Question";

  /** Answer choices offered when the question type is free-form or multiple-choice */
  @Valid @ValidOrder private List<QuestionChoiceDTO> choices = new ArrayList<>();

  /** Answer options offered when the question type is extended matching */
  @Valid @ValidOrder
  private List<ExtendedMatchingOptionDTO> extendedMatchingOptions = new ArrayList<>();

  /** Statements to be matched against options when the question type is extended matching */
  @Valid @ValidOrder
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
