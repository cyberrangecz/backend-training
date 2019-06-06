package cz.muni.ics.kypo.training.service.impl;

import cz.muni.csirt.kypo.elasticsearch.service.AuditService;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import cz.muni.ics.kypo.training.persistence.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
@Service
public class AuditEventsService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditEventsService.class);
    private AuditService auditService;

    @Autowired
    public AuditEventsService(AuditService auditService) {
        this.auditService = auditService;
    }


    void auditTrainingRunStartedAction(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();

        TrainingRunStarted trainingRunStarted = new TrainingRunStarted.TrainingRunStartedBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(0L)
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .build();

        auditService.saveTrainingRunEvent(trainingRunStarted, trainingDefinitionId, trainingInstance.getId());

    }

    void auditLevelStartedAction(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();
        LevelType levelType = getLevelType(trainingRun.getCurrentLevel());

        LevelStarted levelStarted = new LevelStarted.LevelStartedBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .levelType(levelType)
                .maxScore(trainingRun.getCurrentLevel().getMaxScore())
                .levelTitle(trainingInstance.getTitle())
                .build();

        auditService.saveTrainingRunEvent(levelStarted, trainingDefinitionId, trainingInstance.getId());

    }

    void auditLevelCompletedAction(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();
        LevelType levelType = getLevelType(trainingRun.getCurrentLevel());

        LevelCompleted levelCompleted = new LevelCompleted.LevelCompletedBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .levelType(levelType)
                .build();

        auditService.saveTrainingRunEvent(levelCompleted, trainingDefinitionId, trainingInstance.getId());
    }

    void auditHintTakenAction(TrainingRun trainingRun, Hint hint) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();
        HintTaken hintTaken = new HintTaken.HintTakenBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .hintId(hint.getId())
                .hintPenaltyPoints(hint.getHintPenalty())
                .hintTitle(hint.getTitle())
                .build();
        auditService.saveTrainingRunEvent(hintTaken, trainingDefinitionId, trainingInstance.getId());
    }

    void auditSolutionDisplayedAction(TrainingRun trainingRun, GameLevel gameLevel) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();
        SolutionDisplayed solutionDisplayed = new SolutionDisplayed.SolutionDisplayedBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .penaltyPoints(gameLevel.getMaxScore() - trainingRun.getCurrentScore())
                .build();
        auditService.saveTrainingRunEvent(solutionDisplayed, trainingDefinitionId, trainingInstance.getId());

    }

    void auditCorrectFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();
        CorrectFlagSubmitted correctFlagSubmitted = new CorrectFlagSubmitted.CorrectFlagSubmittedBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore()) // requires to set total and actual score in level from training run entity
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .flagContent(flag)
                .build();
        auditService.saveTrainingRunEvent(correctFlagSubmitted, trainingDefinitionId, trainingInstance.getId());
    }

    void auditWrongFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();

        WrongFlagSubmitted wrongFlagSubmitted = new WrongFlagSubmitted.WrongFlagSubmittedBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .flagContent(flag)
                .count(trainingRun.getIncorrectFlagCount())
                .build();
        auditService.saveTrainingRunEvent(wrongFlagSubmitted, trainingDefinitionId, trainingInstance.getId());

    }

    void auditAssessmentAnswersAction(TrainingRun trainingRun, String answers) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();
        AssessmentAnswers assessmentAnswers = new AssessmentAnswers.AssessmentAnswersBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .answers(answers)
                .build();
        auditService.saveTrainingRunEvent(assessmentAnswers, trainingDefinitionId, trainingInstance.getId());
    }

    void auditTrainingRunEndedAction(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();

        TrainingRunEnded assessmentAnswers = new TrainingRunEnded.TrainingRunEndedBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .startTime(trainingRun.getStartTime().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
                .endTime(System.currentTimeMillis())
                .build();

        auditService.saveTrainingRunEvent(assessmentAnswers, trainingDefinitionId, trainingInstance.getId());
    }

    void auditTrainingRunResumedAction(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long trainingDefinitionId = trainingInstance.getTrainingDefinition().getId();
        Long sandboxId = trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId();
        TrainingRunResumed trainingRunResumed = new TrainingRunResumed.TrainingRunResumedBuilder()
                .sandboxId(sandboxId)
                .trainingDefinitionId(trainingDefinitionId)
                .trainingInstanceId(trainingInstance.getId())
                .trainingRunId(trainingRun.getId())
                .gameTime(computeGameTime(trainingRun.getStartTime()))
                .playerLogin(trainingRun.getParticipantRef().getUserRefLogin())
                .fullName(trainingRun.getParticipantRef().getUserRefFullName())
                .fullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() + " " + trainingRun.getParticipantRef().getUserRefFamilyName())
                .totalScore(trainingRun.getTotalScore())
                .actualScoreInLevel(trainingRun.getCurrentScore())
                .level(trainingRun.getCurrentLevel().getId())
                .build();
        auditService.saveTrainingRunEvent(trainingRunResumed, trainingDefinitionId, trainingInstance.getId());
    }

    private long computeGameTime(LocalDateTime gameStartedTime) {
        return ChronoUnit.MILLIS.between(gameStartedTime, LocalDateTime.now(Clock.systemUTC()));
    }

    private LevelType getLevelType(AbstractLevel abstractLevel) {
        if (abstractLevel instanceof GameLevel) {
            return LevelType.GAME;
        } else if (abstractLevel instanceof InfoLevel) {
            return LevelType.INFO;
        } else if (abstractLevel instanceof AssessmentLevel) {
            return LevelType.ASSESSMENT;
        }
        return LevelType.PVP; //no one knows what PVP is
    }
}
