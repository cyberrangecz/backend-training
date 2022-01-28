package cz.muni.ics.kypo.training.service;

import cz.muni.csirt.kypo.elasticsearch.service.AuditService;
import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * The type Audit events service.
 */
@Service
public class AuditEventsService {

    private final AuditService auditService;

    /**
     * Instantiates a new Audit events service.
     *
     * @param auditService the audit service
     */
    @Autowired
    public AuditEventsService(AuditService auditService) {
        this.auditService = auditService;
    }


    /**
     * Audit training run started action.
     *
     * @param trainingRun the training run
     */
    public void auditTrainingRunStartedAction(TrainingRun trainingRun) {
        TrainingRunStarted.TrainingRunStartedBuilder<?, ?> trainingRunStartedBuilder = (TrainingRunStarted.TrainingRunStartedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, TrainingRunStarted.builder());

        TrainingRunStarted trainingRunStarted = trainingRunStartedBuilder
                .trainingTime(0L)
                .build();
        auditService.saveTrainingRunEvent(trainingRunStarted, 0L);
    }

    /**
     * Audit level started action.
     *
     * @param trainingRun the training run
     */
    public void auditLevelStartedAction(TrainingRun trainingRun) {
        LevelStarted.LevelStartedBuilder<?, ?> levelStartedBuilder = (LevelStarted.LevelStartedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, LevelStarted.builder());

        LevelStarted levelStarted = levelStartedBuilder
                .levelType(getLevelType(trainingRun.getCurrentLevel()))
                .maxScore(trainingRun.getCurrentLevel().getMaxScore())
                .levelTitle(trainingRun.getCurrentLevel().getTitle())
                .build();
        auditService.saveTrainingRunEvent(levelStarted, 10L);
    }

    /**
     * Audit level completed action.
     *
     * @param trainingRun the training run
     */
    public void auditLevelCompletedAction(TrainingRun trainingRun) {
        LevelCompleted.LevelCompletedBuilder<?, ?> levelCompletedBuilder = (LevelCompleted.LevelCompletedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, LevelCompleted.builder());

        LevelCompleted levelCompleted = levelCompletedBuilder
                .levelType(getLevelType(trainingRun.getCurrentLevel()))
                .build();
        auditService.saveTrainingRunEvent(levelCompleted, 5L);
    }

    /**
     * Audit hint taken action.
     *
     * @param trainingRun the training run
     * @param hint        the hint
     */
    public void auditHintTakenAction(TrainingRun trainingRun, Hint hint) {
        HintTaken.HintTakenBuilder<?, ?> hintTakenBuilder = (HintTaken.HintTakenBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, HintTaken.builder());

        HintTaken hintTaken = hintTakenBuilder
                .hintId(hint.getId())
                .hintPenaltyPoints(hint.getHintPenalty())
                .hintTitle(hint.getTitle())
                .build();
        auditService.saveTrainingRunEvent(hintTaken, 0L);
    }

    /**
     * Audit solution displayed action.
     *
     * @param trainingRun the training run
     */
    public void auditSolutionDisplayedAction(TrainingRun trainingRun) {
        SolutionDisplayed.SolutionDisplayedBuilder<?, ?> solutionDisplayedBuilder = (SolutionDisplayed.SolutionDisplayedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, SolutionDisplayed.builder());

        SolutionDisplayed solutionDisplayed = solutionDisplayedBuilder
                .penaltyPoints(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty())
                .build();
        auditService.saveTrainingRunEvent(solutionDisplayed, 0L);
    }

    /**
     * Audit correct answer submitted action.
     *
     * @param trainingRun the training run
     * @param answer      the answer
     */
    public void auditCorrectAnswerSubmittedAction(TrainingRun trainingRun, String answer) {
        CorrectAnswerSubmitted.CorrectAnswerSubmittedBuilder<?, ?> correctAnswerSubmittedBuilder = (CorrectAnswerSubmitted.CorrectAnswerSubmittedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, CorrectAnswerSubmitted.builder());

        CorrectAnswerSubmitted correctAnswerSubmitted = correctAnswerSubmittedBuilder
                .answerContent(answer)
                .build();
        auditService.saveTrainingRunEvent(correctAnswerSubmitted, 0L);
    }

    /**
     * Audit wrong answer submitted action.
     *
     * @param trainingRun the training run
     * @param answer      the answer
     */
    public void auditWrongAnswerSubmittedAction(TrainingRun trainingRun, String answer) {
        WrongAnswerSubmitted.WrongAnswerSubmittedBuilder<?, ?> wrongAnswerSubmittedBuilder = (WrongAnswerSubmitted.WrongAnswerSubmittedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, WrongAnswerSubmitted.builder());

        WrongAnswerSubmitted wrongAnswerSubmitted = wrongAnswerSubmittedBuilder
                .answerContent(answer)
                .count(trainingRun.getIncorrectAnswerCount())
                .build();
        auditService.saveTrainingRunEvent(wrongAnswerSubmitted, 0L);
    }

    /**
     * Audit correct passkey submitted action.
     *
     * @param trainingRun the training run
     * @param passkey      the passkey
     */
    public void auditCorrectPasskeySubmittedAction(TrainingRun trainingRun, String passkey) {
        CorrectPasskeySubmitted.CorrectPasskeySubmittedBuilder<?, ?> correctPasskeySubmittedBuilder = (CorrectPasskeySubmitted.CorrectPasskeySubmittedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, CorrectPasskeySubmitted.builder());

        CorrectPasskeySubmitted correctPasskeySubmitted = correctPasskeySubmittedBuilder
                .passkeyContent(passkey)
                .build();
        auditService.saveTrainingRunEvent(correctPasskeySubmitted, 0L);
    }

    /**
     * Audit wrong passkey submitted action.
     *
     * @param trainingRun the training run
     * @param passkey      the passkey
     */
    public void auditWrongPasskeySubmittedAction(TrainingRun trainingRun, String passkey) {
        WrongPasskeySubmitted.WrongPasskeySubmittedBuilder<?, ?> wrongPasskeySubmittedBuilder = (WrongPasskeySubmitted.WrongPasskeySubmittedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, WrongPasskeySubmitted.builder());

        WrongPasskeySubmitted wrongPasskeySubmitted = wrongPasskeySubmittedBuilder
                .passkeyContent(passkey)
                .build();
        auditService.saveTrainingRunEvent(wrongPasskeySubmitted, 0L);
    }

    /**
     * Audit assessment answers action.
     *
     * @param trainingRun the training run
     * @param answers     the answers
     */
    public void auditAssessmentAnswersAction(TrainingRun trainingRun, String answers) {
        AssessmentAnswers.AssessmentAnswersBuilder<?, ?> assessmentAnswersBuilder = (AssessmentAnswers.AssessmentAnswersBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, AssessmentAnswers.builder());

        AssessmentAnswers assessmentAnswers = assessmentAnswersBuilder
                .answers(answers)
                .build();
        auditService.saveTrainingRunEvent(assessmentAnswers, 0L);
    }

    /**
     * Audit training run ended action.
     *
     * @param trainingRun the training run
     */
    public void auditTrainingRunEndedAction(TrainingRun trainingRun) {
        TrainingRunEnded.TrainingRunEndedBuilder<?, ?> trainingRunEndedBuilder = (TrainingRunEnded.TrainingRunEndedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, TrainingRunEnded.builder());

        TrainingRunEnded trainingRunEnded = trainingRunEndedBuilder
                .startTime(trainingRun.getStartTime().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
                .endTime(System.currentTimeMillis())
                .build();
        auditService.saveTrainingRunEvent(trainingRunEnded, 10L);
    }

    /**
     * Audit training run resumed action.
     *
     * @param trainingRun the training run
     */
    public void auditTrainingRunResumedAction(TrainingRun trainingRun) {
        TrainingRunResumed.TrainingRunResumedBuilder<?, ?> trainingRunResumedBuilder = (TrainingRunResumed.TrainingRunResumedBuilder<?, ?>)
                fillInCommonBuilderFields(trainingRun, TrainingRunResumed.builder());
        TrainingRunResumed trainingRunResumed = trainingRunResumedBuilder.build();
        auditService.saveTrainingRunEvent(trainingRunResumed, 0L);
    }

    private AbstractAuditPOJO.AbstractAuditPOJOBuilder<?, ?> fillInCommonBuilderFields(TrainingRun trainingRun, AbstractAuditPOJO.AbstractAuditPOJOBuilder<?, ?> builder) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        AbstractLevel trainingLevel = trainingRun.getCurrentLevel();
        builder.sandboxId(trainingRun.getSandboxInstanceRefId())
                .poolId(trainingInstance.getPoolId())
                .trainingRunId(trainingRun.getId())
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

    private LevelType getLevelType(AbstractLevel abstractLevel) {
        if (abstractLevel instanceof TrainingLevel) {
            return LevelType.TRAINING;
        } else if (abstractLevel instanceof InfoLevel) {
            return LevelType.INFO;
        } else if (abstractLevel instanceof AssessmentLevel) {
            return LevelType.ASSESSMENT;
        } else if (abstractLevel instanceof AccessLevel) {
            return LevelType.ACCESS;
        }
        return LevelType.PVP;
    }
}
