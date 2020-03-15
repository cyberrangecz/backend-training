package cz.muni.ics.kypo.training.service;

import cz.muni.csirt.kypo.elasticsearch.service.AuditService;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import cz.muni.ics.kypo.training.api.dto.AuditInfoDTO;
import cz.muni.ics.kypo.training.persistence.model.*;
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

    private AuditService auditService;

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
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        TrainingRunStarted trainingRunStarted = TrainingRunStarted.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(0L)
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .build();

        auditService.saveTrainingRunEvent(trainingRunStarted);
    }

    /**
     * Audit level started action.
     *
     * @param trainingRun the training run
     */
    public void auditLevelStartedAction(TrainingRun trainingRun) {
        LevelType levelType = getLevelType(trainingRun.getCurrentLevel());
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        LevelStarted levelStarted = LevelStarted.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .levelType(levelType)
                .maxScore(trainingRun.getCurrentLevel().getMaxScore())
                .levelTitle(trainingRun.getCurrentLevel().getTitle())
                .build();

        auditService.saveTrainingRunEvent(levelStarted);
    }

    /**
     * Audit level completed action.
     *
     * @param trainingRun the training run
     */
    public void auditLevelCompletedAction(TrainingRun trainingRun) {
        LevelType levelType = getLevelType(trainingRun.getCurrentLevel());
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        LevelCompleted levelCompleted = LevelCompleted.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .levelType(levelType)
                .build();

        auditService.saveTrainingRunEvent(levelCompleted);
    }

    /**
     * Audit hint taken action.
     *
     * @param trainingRun the training run
     * @param hint        the hint
     */
    public void auditHintTakenAction(TrainingRun trainingRun, Hint hint) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        HintTaken hintTaken = HintTaken.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .hintId(hint.getId())
                .hintPenaltyPoints(hint.getHintPenalty())
                .hintTitle(hint.getTitle())
                .build();
        auditService.saveTrainingRunEvent(hintTaken);
    }

    /**
     * Audit solution displayed action.
     *
     * @param trainingRun the training run
     */
    public void auditSolutionDisplayedAction(TrainingRun trainingRun) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        SolutionDisplayed solutionDisplayed = SolutionDisplayed.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .penaltyPoints(auditInfoDTO.getActualScoreInLevel())
                .build();
        auditService.saveTrainingRunEvent(solutionDisplayed);
    }

    /**
     * Audit correct flag submitted action.
     *
     * @param trainingRun the training run
     * @param flag        the flag
     */
    public void auditCorrectFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        CorrectFlagSubmitted correctFlagSubmitted = CorrectFlagSubmitted.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .flagContent(flag)
                .build();
        auditService.saveTrainingRunEvent(correctFlagSubmitted);
    }

    /**
     * Audit wrong flag submitted action.
     *
     * @param trainingRun the training run
     * @param flag        the flag
     */
    public void auditWrongFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        WrongFlagSubmitted wrongFlagSubmitted = WrongFlagSubmitted.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .flagContent(flag)
                .count(trainingRun.getIncorrectFlagCount())
                .build();
        auditService.saveTrainingRunEvent(wrongFlagSubmitted);
    }

    /**
     * Audit assessment answers action.
     *
     * @param trainingRun the training run
     * @param answers     the answers
     */
    public void auditAssessmentAnswersAction(TrainingRun trainingRun, String answers) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        AssessmentAnswers assessmentAnswers = AssessmentAnswers.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .answers(answers)
                .build();
        auditService.saveTrainingRunEvent(assessmentAnswers);
    }

    /**
     * Audit training run ended action.
     *
     * @param trainingRun the training run
     */
    public void auditTrainingRunEndedAction(TrainingRun trainingRun) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        TrainingRunEnded assessmentAnswers = TrainingRunEnded.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .startTime(trainingRun.getStartTime().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
                .endTime(System.currentTimeMillis())
                .build();

        auditService.saveTrainingRunEvent(assessmentAnswers);
    }

    /**
     * Audit training run resumed action.
     *
     * @param trainingRun the training run
     */
    public void auditTrainingRunResumedAction(TrainingRun trainingRun) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        TrainingRunResumed trainingRunResumed = TrainingRunResumed.builder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .poolId(auditInfoDTO.getPoolId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .build();
        auditService.saveTrainingRunEvent(trainingRunResumed);
    }

    private AuditInfoDTO createAuditUserInfo(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        AuditInfoDTO auditInfoDTO = new AuditInfoDTO();
        auditInfoDTO.setSandboxId(trainingRun.getSandboxInstanceRefId());
        auditInfoDTO.setPoolId(trainingInstance.getPoolId());
        auditInfoDTO.setTrainingRunId(trainingRun.getId());
        auditInfoDTO.setTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
        auditInfoDTO.setTrainingInstanceId(trainingInstance.getId());
        auditInfoDTO.setGameTime(computeGameTime(trainingRun.getStartTime()));
        auditInfoDTO.setUserRefId(trainingRun.getParticipantRef().getUserRefId());
        auditInfoDTO.setLevel(trainingRun.getCurrentLevel().getId());
        auditInfoDTO.setTotalScore(trainingRun.getTotalScore());
        auditInfoDTO.setActualScoreInLevel(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty());

        return auditInfoDTO;
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
        return LevelType.PVP;
    }
}
