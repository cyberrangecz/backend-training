package cz.muni.ics.kypo.training.service.impl;

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
 * @author Pavel Seda
 */
@Service
public class AuditEventsService {

    private AuditService auditService;

    @Autowired
    public AuditEventsService(AuditService auditService) {
        this.auditService = auditService;
    }


    public void auditTrainingRunStartedAction(TrainingRun trainingRun) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        TrainingRunStarted trainingRunStarted = new TrainingRunStarted.TrainingRunStartedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(0L)
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .iss(auditInfoDTO.getIss())
                .build();

        auditService.saveTrainingRunEvent(trainingRunStarted, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditLevelStartedAction(TrainingRun trainingRun) {
        LevelType levelType = getLevelType(trainingRun.getCurrentLevel());
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        LevelStarted levelStarted = new LevelStarted.LevelStartedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .levelType(levelType)
                .maxScore(trainingRun.getCurrentLevel().getMaxScore())
                .levelTitle(trainingRun.getCurrentLevel().getTitle())
                .iss(auditInfoDTO.getIss())
                .build();

        auditService.saveTrainingRunEvent(levelStarted, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditLevelCompletedAction(TrainingRun trainingRun) {
        LevelType levelType = getLevelType(trainingRun.getCurrentLevel());
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        LevelCompleted levelCompleted = new LevelCompleted.LevelCompletedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .levelType(levelType)
                .iss(auditInfoDTO.getIss())
                .build();

        auditService.saveTrainingRunEvent(levelCompleted, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditHintTakenAction(TrainingRun trainingRun, Hint hint) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        HintTaken hintTaken = new HintTaken.HintTakenBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .hintId(hint.getId())
                .hintPenaltyPoints(hint.getHintPenalty())
                .hintTitle(hint.getTitle())
                .iss(auditInfoDTO.getIss())
                .build();
        auditService.saveTrainingRunEvent(hintTaken, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditSolutionDisplayedAction(TrainingRun trainingRun, GameLevel gameLevel) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        SolutionDisplayed solutionDisplayed = new SolutionDisplayed.SolutionDisplayedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .penaltyPoints(trainingRun.getCurrentPenalty())
                .iss(auditInfoDTO.getIss())
                .build();
        auditService.saveTrainingRunEvent(solutionDisplayed, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditCorrectFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        CorrectFlagSubmitted correctFlagSubmitted = new CorrectFlagSubmitted.CorrectFlagSubmittedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .flagContent(flag)
                .iss(auditInfoDTO.getIss())
                .build();
        auditService.saveTrainingRunEvent(correctFlagSubmitted, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditWrongFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        WrongFlagSubmitted wrongFlagSubmitted = new WrongFlagSubmitted.WrongFlagSubmittedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .flagContent(flag)
                .count(trainingRun.getIncorrectFlagCount())
                .iss(auditInfoDTO.getIss())
                .build();
        auditService.saveTrainingRunEvent(wrongFlagSubmitted, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditAssessmentAnswersAction(TrainingRun trainingRun, String answers) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        AssessmentAnswers assessmentAnswers = new AssessmentAnswers.AssessmentAnswersBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .answers(answers)
                .iss(auditInfoDTO.getIss())
                .build();
        auditService.saveTrainingRunEvent(assessmentAnswers, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditTrainingRunEndedAction(TrainingRun trainingRun) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        TrainingRunEnded assessmentAnswers = new TrainingRunEnded.TrainingRunEndedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .startTime(trainingRun.getStartTime().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli())
                .endTime(System.currentTimeMillis())
                .iss(auditInfoDTO.getIss())
                .build();

        auditService.saveTrainingRunEvent(assessmentAnswers, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    public void auditTrainingRunResumedAction(TrainingRun trainingRun) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        TrainingRunResumed trainingRunResumed = new TrainingRunResumed.TrainingRunResumedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(auditInfoDTO.getGameTime())
                .playerLogin(auditInfoDTO.getPlayerLogin())
                .fullName(auditInfoDTO.getFullName())
                .fullNameWithoutTitles(auditInfoDTO.getFullNameWithoutTitles())
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getTotalScore())
                .iss(auditInfoDTO.getIss())
                .build();
        auditService.saveTrainingRunEvent(trainingRunResumed, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    private AuditInfoDTO createAuditUserInfo(TrainingRun trainingRun){
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        AuditInfoDTO auditInfoDTO = new AuditInfoDTO();

        if(trainingRun.getParticipantRef() != null && trainingRun.getParticipantRef().getUserRefLogin() != null)
            auditInfoDTO.setPlayerLogin(trainingRun.getParticipantRef().getUserRefLogin());
        else auditInfoDTO.setPlayerLogin("");

        if(trainingRun.getParticipantRef() != null && trainingRun.getParticipantRef().getUserRefFullName() != null)
            auditInfoDTO.setFullName(trainingRun.getParticipantRef().getUserRefFullName());
        else auditInfoDTO.setFullName("");

        if(trainingRun.getParticipantRef() != null && trainingRun.getParticipantRef().getUserRefGivenName() != null && trainingRun.getParticipantRef().getUserRefFamilyName() != null)
            auditInfoDTO.setFullNameWithoutTitles(trainingRun.getParticipantRef().getUserRefGivenName() +" "+ trainingRun.getParticipantRef().getUserRefFamilyName());
        else auditInfoDTO.setFullNameWithoutTitles("");

        if(trainingRun.getParticipantRef() != null && trainingRun.getParticipantRef().getIss() != null)
            auditInfoDTO.setIss(trainingRun.getParticipantRef().getIss());
        else auditInfoDTO.setIss("");

        auditInfoDTO.setUserRefId(trainingRun.getParticipantRef().getUserRefId());
        auditInfoDTO.setSandboxId(trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId());
        auditInfoDTO.setTrainingRunId(trainingRun.getId());
        auditInfoDTO.setTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
        auditInfoDTO.setTrainingInstanceId(trainingInstance.getId());
        auditInfoDTO.setGameTime(computeGameTime(trainingRun.getStartTime()));
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
        return LevelType.PVP; //no one knows what PVP is
    }
}
