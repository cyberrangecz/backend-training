package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One generated answer belonging to a single sandbox, as a variable name paired with the value
 * expected for it. Consumers key on the variable name and compare the content against what a
 * trainee submitted.
 */
public class VariantAnswer {

  @Schema(example = "nmap 192.168.0.1")
  @JsonProperty("answer_content")
  private String answerContent;

  @Schema(example = "sandbox-1-2-answer")
  @JsonProperty("answer_variable_name")
  private String answerVariableName;

  public String getAnswerContent() {
    return answerContent;
  }

  public void setAnswerContent(String answerContent) {
    this.answerContent = answerContent;
  }

  public String getAnswerVariableName() {
    return answerVariableName;
  }

  public void setAnswerVariableName(String answerVariableName) {
    this.answerVariableName = answerVariableName;
  }

  @Override
  public String toString() {
    return "VariantAnswer{"
        + "answerContent='"
        + answerContent
        + '\''
        + ", answerVariableName='"
        + answerVariableName
        + '\''
        + '}';
  }
}
