package cz.cyberrange.platform.training.api.dto.archive;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * One participant's submitted answers to a single question, written as one JSON line into the
 * assessment answers folder of the training instance archive
 */
@Data
@AllArgsConstructor
public class QuestionAnswerArchiveDTO {

  /** Text of the question, not its identifier */
  private String question;

  /**
   * The values submitted for this question. For an extended-matching question, holds one formatted
   * statement-and-option pair per matched statement instead of the raw submitted values.
   */
  private Set<String> answer = new HashSet<>();
}
