package cz.cyberrange.platform.training.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One statement-to-option pairing chosen for an extended-matching question, deserialized from the
 * raw JSON stored in a submitted question answer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEMIAnswer {

  /**
   * Index into the question's list of extended-matching statements naming the matched statement
   */
  @JsonProperty("statementOrder")
  private Integer statementOrder;

  /** Index into the question's list of extended-matching options naming the matched option */
  @JsonProperty("optionOrder")
  private Integer optionOrder;
}
