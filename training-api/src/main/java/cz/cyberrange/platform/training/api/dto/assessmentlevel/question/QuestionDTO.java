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

  @ApiModelProperty(value = "Choices displayed to the participant in case of FFQ or MCQ.")
  @Valid
  @ValidOrder
  private List<QuestionChoiceDTO> choices = new ArrayList<>();

  @ApiModelProperty(value = "Options displayed to the participant in case of EMI.")
  @Valid
  @ValidOrder
  private List<ExtendedMatchingOptionDTO> extendedMatchingOptions = new ArrayList<>();

  @ApiModelProperty(value = "Statements displayed to the participant in case of EMI.")
  @Valid
  @ValidOrder
  private List<ExtendedMatchingStatementDTO> extendedMatchingStatements = new ArrayList<>();

  public void setChoices(List<QuestionChoiceDTO> choices) {
    this.choices = choices;
    this.choices.sort(Comparator.comparingInt(QuestionChoiceDTO::getOrder));
  }

  public void setExtendedMatchingOptions(List<ExtendedMatchingOptionDTO> extendedMatchingOptions) {
    this.extendedMatchingOptions = extendedMatchingOptions;
    this.extendedMatchingOptions.sort(Comparator.comparingInt(ExtendedMatchingOptionDTO::getOrder));
  }

  public void setExtendedMatchingStatements(
      List<ExtendedMatchingStatementDTO> extendedMatchingStatements) {
    this.extendedMatchingStatements = extendedMatchingStatements;
    this.extendedMatchingStatements.sort(
        Comparator.comparingInt(ExtendedMatchingStatementDTO::getOrder));
  }
}
