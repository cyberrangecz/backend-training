package cz.cyberrange.platform.training.api.dto.assessmentlevel;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import java.util.List;

/**
 * A payload that defines the questions of an assessment level. Implemented by {@link
 * AssessmentLevelContentDTO} and {@code AssessmentLevelImportDTO}.
 */
public interface AssessmentQuestionsSource {

  /**
   * Lists the questions of the assessment level in presentation order.
   *
   * @return the question payloads, whose list positions are their orders
   */
  List<QuestionDTO> getQuestions();
}
