package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.opensearch.events.training.logging.AuditService;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.opensearch.events.training.model.AssessmentAnswered;
import cz.cyberrange.platform.training.opensearch.events.training.model.CorrectAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.model.EventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.HintTaken;
import cz.cyberrange.platform.training.opensearch.events.training.model.LevelCompleted;
import cz.cyberrange.platform.training.opensearch.events.training.model.LevelStarted;
import cz.cyberrange.platform.training.opensearch.events.training.model.SolutionDisplayed;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunFinished;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunResumed;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunStarted;
import cz.cyberrange.platform.training.opensearch.events.training.model.WrongAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.model.enums.EventLevelType;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Builds one audit event POJO per training run action and hands it to {@link AuditService}, which
 * writes it as a JSON line to the application log. Every event carries the fields filled in by
 * {@link #fillInCommonBuilderFields}; some methods also pass a priority so that several events
 * emitted for the same run action keep a stable relative order.
 */
@Service
public class AuditEventsService {

  private final AuditService auditService;

  @Autowired
  public AuditEventsService(AuditService auditService) {
    this.auditService = auditService;
  }

  /**
   * Emits a training-run-started event with its training time forced to zero.
   *
   * @param trainingRun the run being started
   */
  public void auditTrainingRunStartedAction(TrainingRun trainingRun) {
    TrainingRunStarted.TrainingRunStartedBuilder<?, ?> trainingRunStartedBuilder =
        (TrainingRunStarted.TrainingRunStartedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, TrainingRunStarted.builder());

    TrainingRunStarted trainingRunStarted = trainingRunStartedBuilder.trainingTime(0L).build();
    auditService.saveTrainingRunEvent(trainingRunStarted);
  }

  /**
   * Emits a level-started event for the run's current level, with a timestamp ordered after a
   * level-completed event emitted for the same instant.
   *
   * @param trainingRun the run whose current level is starting
   */
  public void auditLevelStartedAction(TrainingRun trainingRun) {
    LevelStarted.LevelStartedBuilder<?, ?> levelStartedBuilder =
        (LevelStarted.LevelStartedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, LevelStarted.builder());

    LevelStarted levelStarted =
        levelStartedBuilder
            .levelType(getLevelType(trainingRun.getCurrentLevel()))
            .maxScore(trainingRun.getCurrentLevel().getMaxScore())
            .levelTitle(trainingRun.getCurrentLevel().getTitle())
            .build();
    auditService.saveTrainingRunEvent(levelStarted, 2);
  }

  /**
   * Emits a level-completed event for the run's current level, with a timestamp ordered before a
   * level-started or training-run-finished event emitted for the same instant.
   *
   * @param trainingRun the run whose current level has finished
   */
  public void auditLevelCompletedAction(TrainingRun trainingRun) {
    LevelCompleted.LevelCompletedBuilder<?, ?> levelCompletedBuilder =
        (LevelCompleted.LevelCompletedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, LevelCompleted.builder());

    LevelCompleted levelCompleted =
        levelCompletedBuilder.levelType(getLevelType(trainingRun.getCurrentLevel())).build();
    auditService.saveTrainingRunEvent(levelCompleted, 1);
  }

  /**
   * Emits a hint-taken event carrying the hint's identifier, penalty and title.
   *
   * @param trainingRun the run the hint was taken in
   * @param hint the hint that was taken
   */
  public void auditHintTakenAction(TrainingRun trainingRun, Hint hint) {
    HintTaken.HintTakenBuilder<?, ?> hintTakenBuilder =
        (HintTaken.HintTakenBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, HintTaken.builder());

    HintTaken hintTaken =
        hintTakenBuilder
            .hintId(hint.getId())
            .hintPenaltyPoints(hint.getHintPenalty())
            .hintTitle(hint.getTitle())
            .build();
    auditService.saveTrainingRunEvent(hintTaken);
  }

  /**
   * Emits a solution-displayed event carrying the current level's remaining score, its maximum
   * score minus the run's accumulated penalty in that level.
   *
   * @param trainingRun the run the solution was displayed in
   */
  public void auditSolutionDisplayedAction(TrainingRun trainingRun) {
    SolutionDisplayed.SolutionDisplayedBuilder<?, ?> solutionDisplayedBuilder =
        (SolutionDisplayed.SolutionDisplayedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, SolutionDisplayed.builder());

    SolutionDisplayed solutionDisplayed =
        solutionDisplayedBuilder
            .penaltyPoints(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty())
            .build();
    auditService.saveTrainingRunEvent(solutionDisplayed);
  }

  /**
   * Emits a correct-answer-submitted event carrying the submitted answer text.
   *
   * @param trainingRun the run the answer was submitted in
   * @param answer the submitted answer text
   */
  public void auditCorrectAnswerSubmittedAction(TrainingRun trainingRun, String answer) {
    CorrectAnswerSubmitted.CorrectAnswerSubmittedBuilder<?, ?> correctAnswerSubmittedBuilder =
        (CorrectAnswerSubmitted.CorrectAnswerSubmittedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, CorrectAnswerSubmitted.builder());

    CorrectAnswerSubmitted correctAnswerSubmitted =
        correctAnswerSubmittedBuilder.answerContent(answer).build();
    auditService.saveTrainingRunEvent(correctAnswerSubmitted);
  }

  /**
   * Emits a wrong-answer-submitted event carrying the submitted answer text and the run's
   * incorrect-answer count.
   *
   * @param trainingRun the run the answer was submitted in
   * @param answer the submitted answer text
   */
  public void auditWrongAnswerSubmittedAction(TrainingRun trainingRun, String answer) {
    WrongAnswerSubmitted.WrongAnswerSubmittedBuilder<?, ?> wrongAnswerSubmittedBuilder =
        (WrongAnswerSubmitted.WrongAnswerSubmittedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, WrongAnswerSubmitted.builder());

    WrongAnswerSubmitted wrongAnswerSubmitted =
        wrongAnswerSubmittedBuilder
            .answerContent(answer)
            .count(trainingRun.getIncorrectAnswerCount())
            .build();
    auditService.saveTrainingRunEvent(wrongAnswerSubmitted);
  }

  /**
   * Emits an assessment-answered event carrying the submitted per-question answers.
   *
   * @param trainingRun the run the answers were submitted in
   * @param answers the typed per-question answers submitted by the trainee
   */
  public void auditAssessmentAnswersAction(TrainingRun trainingRun, List<EventAnswer> answers) {
    AssessmentAnswered.AssessmentAnsweredBuilder<?, ?> assessmentAnswersBuilder =
        (AssessmentAnswered.AssessmentAnsweredBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, AssessmentAnswered.builder());

    AssessmentAnswered assessmentAnswers = assessmentAnswersBuilder.answers(answers).build();
    auditService.saveTrainingRunEvent(assessmentAnswers);
  }

  /**
   * Emits a training-run-finished event carrying the run's start time and the current moment as end
   * time, both as UTC epoch milliseconds, with a timestamp ordered after a level-completed event
   * emitted for the same instant.
   *
   * @param trainingRun the run that finished
   */
  public void auditTrainingRunEndedAction(TrainingRun trainingRun) {
    TrainingRunFinished.TrainingRunFinishedBuilder<?, ?> trainingRunEndedBuilder =
        (TrainingRunFinished.TrainingRunFinishedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, TrainingRunFinished.builder());

    TrainingRunFinished trainingRunFinished =
        trainingRunEndedBuilder
            .startTime(
                trainingRun.getStartTime().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
            .endTime(System.currentTimeMillis())
            .build();
    auditService.saveTrainingRunEvent(trainingRunFinished, 3);
  }

  /**
   * Emits a training-run-resumed event.
   *
   * @param trainingRun the run that resumed
   */
  public void auditTrainingRunResumedAction(TrainingRun trainingRun) {
    TrainingRunResumed.TrainingRunResumedBuilder<?, ?> trainingRunResumedBuilder =
        (TrainingRunResumed.TrainingRunResumedBuilder<?, ?>)
            fillInCommonBuilderFields(trainingRun, TrainingRunResumed.builder());
    TrainingRunResumed trainingRunResumed = trainingRunResumedBuilder.build();
    auditService.saveTrainingRunEvent(trainingRunResumed);
  }

  /**
   * Fills the fields shared by every audit event: the run, instance, definition and current level's
   * identifiers, the participant's cross-service user identifier, elapsed training time, both
   * accumulated scores, the current level's remaining score (its maximum score minus the run's
   * accumulated penalty in that level), the current level's order, the instance's pool id, and the
   * run's sandbox reference id.
   *
   * @param trainingRun the run the event is being built for
   * @param builder the builder to fill
   * @return {@code builder}, for chaining the event-specific fields
   */
  private AbstractAuditPOJO.AbstractAuditPOJOBuilder<?, ?> fillInCommonBuilderFields(
      TrainingRun trainingRun, AbstractAuditPOJO.AbstractAuditPOJOBuilder<?, ?> builder) {
    TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
    AbstractLevel trainingLevel = trainingRun.getCurrentLevel();
    builder
        .trainingRunId(trainingRun.getId())
        .poolId(trainingInstance.getPoolId())
        .sandboxId(trainingRun.getSandboxInstanceRefId())
        .trainingInstanceId(trainingInstance.getId())
        .trainingDefinitionId(trainingInstance.getTrainingDefinition().getId())
        .trainingTime(computeTrainingTime(trainingRun.getStartTime()))
        .userRefId(trainingRun.getParticipantRef().getUserRefId())
        .level(trainingLevel.getId())
        .levelOrder(trainingLevel.getOrder())
        .totalTrainingScore(trainingRun.getTotalTrainingScore())
        .totalAssessmentScore(trainingRun.getTotalAssessmentScore())
        .actualScoreInLevel(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty());
    return builder;
  }

  private long computeTrainingTime(LocalDateTime trainingStartedTime) {
    return ChronoUnit.MILLIS.between(trainingStartedTime, LocalDateTime.now(Clock.systemUTC()));
  }

  private EventLevelType getLevelType(AbstractLevel abstractLevel) {
    if (abstractLevel instanceof TrainingLevel) {
      return EventLevelType.TRAINING;
    } else if (abstractLevel instanceof InfoLevel) {
      return EventLevelType.INFO;
    } else if (abstractLevel instanceof AssessmentLevel) {
      return EventLevelType.ASSESSMENT;
    } else if (abstractLevel instanceof AccessLevel) {
      return EventLevelType.ACCESS;
    }
    return EventLevelType.PVP;
  }
}
