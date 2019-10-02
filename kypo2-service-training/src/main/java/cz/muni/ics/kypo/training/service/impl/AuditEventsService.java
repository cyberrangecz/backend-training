package cz.muni.ics.kypo.training.service.impl;

import cz.muni.csirt.kypo.elasticsearch.service.AuditService;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import cz.muni.ics.kypo.commons.security.mapping.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.AuditInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.persistence.model.*;
import org.codehaus.jackson.map.ObjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTags;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Pavel Seda
 */
@Service
public class AuditEventsService {

    private AuditService auditService;
    private RestTemplate restTemplate;
    private HttpServletRequest servletRequest;

    @Value("${user-and-group-server.uri}")
    private String userAndGroupUrl;

    @Autowired
    public AuditEventsService(AuditService auditService, RestTemplate restTemplate, HttpServletRequest servletRequest) {
        this.auditService = auditService;
        this.restTemplate = restTemplate;
        this.servletRequest = servletRequest;
    }


    public void auditTrainingRunStartedAction(TrainingRun trainingRun) {
        AuditInfoDTO auditInfoDTO = createAuditUserInfo(trainingRun);

        TrainingRunStarted trainingRunStarted = new TrainingRunStarted.TrainingRunStartedBuilder()
                .sandboxId(auditInfoDTO.getSandboxId())
                .trainingDefinitionId(auditInfoDTO.getTrainingDefinitionId())
                .trainingInstanceId(auditInfoDTO.getTrainingInstanceId())
                .trainingRunId(auditInfoDTO.getTrainingRunId())
                .gameTime(0L)
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .penaltyPoints(auditInfoDTO.getActualScoreInLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
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
                .userRefId(auditInfoDTO.getUserRefId())
                .totalScore(auditInfoDTO.getTotalScore())
                .actualScoreInLevel(auditInfoDTO.getActualScoreInLevel())
                .level(auditInfoDTO.getLevel())
                .iss(auditInfoDTO.getIss())
                .build();
        auditService.saveTrainingRunEvent(trainingRunResumed, auditInfoDTO.getTrainingDefinitionId(), auditInfoDTO.getTrainingInstanceId());
    }

    private AuditInfoDTO createAuditUserInfo(TrainingRun trainingRun){
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        AuditInfoDTO auditInfoDTO = new AuditInfoDTO();

        String userAndGroupUserInfoById = userAndGroupUrl + "/users/" + trainingRun.getParticipantRef().getUserRefId();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        try {
            ResponseEntity<UserRefDTO> response =
                    restTemplate.exchange(userAndGroupUserInfoById, HttpMethod.GET, entity, UserRefDTO.class);
            UserRefDTO participantInfo = response.getBody();
            Objects.requireNonNull(participantInfo);

            if(participantInfo.getUserRefLogin() != null)
                auditInfoDTO.setPlayerLogin(participantInfo.getUserRefLogin());
            else auditInfoDTO.setPlayerLogin("");

            if(participantInfo.getUserRefFullName() != null)
                auditInfoDTO.setFullName(participantInfo.getUserRefFullName());
            else auditInfoDTO.setFullName("");

            if(participantInfo.getUserRefGivenName() != null && participantInfo.getUserRefFamilyName() != null)
                auditInfoDTO.setFullNameWithoutTitles(participantInfo.getUserRefGivenName() +" "+ participantInfo.getUserRefFamilyName());
            else auditInfoDTO.setFullNameWithoutTitles("");

            if(participantInfo.getIss() != null )
                auditInfoDTO.setIss(participantInfo.getIss());
            else auditInfoDTO.setIss("");
        } catch (HttpClientErrorException ex) {
            throw new SecurityException("Error while getting info about user (user_ref_id: " + trainingRun.getParticipantRef().getUserRefId() + ") : " + ex.getStatusCode());
        }



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
