package cz.cyberrange.platform.training.opensearch.events.training.model.enums;

import cz.cyberrange.platform.training.opensearch.events.training.model.AssessmentAnswered;
import cz.cyberrange.platform.training.opensearch.events.training.model.CorrectAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.model.HintTaken;
import cz.cyberrange.platform.training.opensearch.events.training.model.LevelCompleted;
import cz.cyberrange.platform.training.opensearch.events.training.model.LevelStarted;
import cz.cyberrange.platform.training.opensearch.events.training.model.SolutionDisplayed;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunFinished;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunResumed;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunStarted;

public enum EventType {
  ASSESSMENT_ANSWERED(AssessmentAnswered.TYPE),
  CORRECT_ANSWER_SUBMITTED(CorrectAnswerSubmitted.TYPE),
  WRONG_ANSWER_SUBMITTED(CorrectAnswerSubmitted.TYPE),
  HINT_TAKEN(HintTaken.TYPE),
  LEVEL_COMPLETED(LevelCompleted.TYPE),
  LEVEL_STARTED(LevelStarted.TYPE),
  SOLUTION_DISPLAYED(SolutionDisplayed.TYPE),
  TRAINING_RUN_STARTED(TrainingRunStarted.TYPE),
  TRAINING_RUN_RESUMED(TrainingRunResumed.TYPE),
  TRAINING_RUN_FINISHED(TrainingRunFinished.TYPE);

  private final String type;

  EventType(String type) {
    this.type = type;
  }
}
