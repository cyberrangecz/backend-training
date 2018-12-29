package cz.muni.ics.kypo.training.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.utils.SandboxInfo;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dominik Pilar (445537)
 */
@Service
public class TrainingRunServiceImpl implements TrainingRunService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunServiceImpl.class);
    private static final String SANDBOX_INFO_ENDPOINT = "kypo-openstack/api/v1/sandboxes?ids={ids}";
    private static final String MUST_NOT_BE_NULL = "Input training run id must not be null.";
    @Value("${server.url}")
    private String serverUrl;

    private TrainingRunRepository trainingRunRepository;
    private AbstractLevelRepository abstractLevelRepository;
    private TrainingInstanceRepository trainingInstanceRepository;
    private UserRefRepository participantRefRepository;
    private HintRepository hintRepository;
    private RestTemplate restTemplate;
    private AuditService auditService;

    @Autowired
    public TrainingRunServiceImpl(TrainingRunRepository trainingRunRepository, AbstractLevelRepository abstractLevelRepository,
                                  TrainingInstanceRepository trainingInstanceRepository, UserRefRepository participantRefRepository,
                                  RestTemplate restTemplate, HintRepository hintRepository, AuditService auditService) {
        this.trainingRunRepository = trainingRunRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.participantRefRepository = participantRefRepository;
        this.hintRepository = hintRepository;
        this.restTemplate = restTemplate;
        this.auditService = auditService;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isTraineeOfGivenTrainingRun(#id)")
    public TrainingRun findById(Long id) {
        LOG.debug("findById({})", id);
        Objects.requireNonNull(id);
        return trainingRunRepository.findById(id)
                .orElseThrow(() -> new ServiceLayerException("Training Run with id: " + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAll({},{})", predicate, pageable);
        return trainingRunRepository.findAll(predicate, pageable);

    }

    @Override
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or hasAuthority({T(cz.muni.ics.kypo.training.persistence.model.enums.RoleType).TRAINEE})")
    public Page<TrainingRun> findAllByParticipantRefLogin(Pageable pageable) {
        String login = getSubOfLoggedInUser();
        LOG.debug("findAllByParticipantRefLogin({})", login);
        return trainingRunRepository.findAllByParticipantRefLogin(login, pageable);
    }


    private TrainingRun create(TrainingRun trainingRun) {
        LOG.debug("create({})", trainingRun);
        Assert.notNull(trainingRun, "Input training run must not be empty.");
        return trainingRunRepository.save(trainingRun);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public AbstractLevel getNextLevel(Long trainingRunId) {
        LOG.debug("getNextLevel({})", trainingRunId);
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        Long nextLevelId = trainingRun.getCurrentLevel().getNextLevel();
        if (!trainingRun.isLevelAnswered()) {
            throw new ServiceLayerException("At first you need to answer the level.", ErrorCode.RESOURCE_CONFLICT);
        }
        if (nextLevelId == null) {
            throw new ServiceLayerException("There is no next level.", ErrorCode.NO_NEXT_LEVEL);
        }
        AbstractLevel abstractLevel = abstractLevelRepository.findById(nextLevelId).orElseThrow(() ->
                new ServiceLayerException("Level with id: " + nextLevelId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
        if (trainingRun.getCurrentLevel() instanceof InfoLevel) {
            auditLevelCompletedAction(trainingRun);
        }
        trainingRun.setCurrentLevel(abstractLevel);
        trainingRunRepository.save(trainingRun);
        //audit this action to theElasticSearch
        auditLevelStartedAction(trainingRun.getTrainingInstance(), trainingRun);

        return abstractLevel;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinitionId)")
    public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable) {
        LOG.debug("findAllByTrainingDefinitionAndParticipant({})", trainingDefinitionId);
        Assert.notNull(trainingDefinitionId, "Input training definition id must not be null.");
        return trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantRefLogin(trainingDefinitionId, getSubOfLoggedInUser(), pageable);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinitionId)")
    public Page<TrainingRun> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
        LOG.debug("findAllByTrainingDefinition({},{})", trainingDefinitionId, pageable);
        Assert.notNull(trainingDefinitionId, "Input training definition id must not be null.");
        return trainingRunRepository.findAllByTrainingDefinitionId(trainingDefinitionId, pageable);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('USER')")
    public List<AbstractLevel> getLevels(Long levelId) {
        Assert.notNull(levelId, "Id of first level must not be null.");
        List<AbstractLevel> levels = new ArrayList<>();
        do {

            Optional<AbstractLevel> optionalAbstractLevel = abstractLevelRepository.findById(levelId);
            if (!optionalAbstractLevel.isPresent()) {
                throw new ServiceLayerException("Level with id: " + levelId + " not found.", ErrorCode.RESOURCE_NOT_FOUND);
            }
            levelId = optionalAbstractLevel.get().getNextLevel();
            levels.add(optionalAbstractLevel.get());
        } while (levelId != null);
        return levels;
    }


    @Override
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or hasAuthority('USER')")
    public TrainingRun accessTrainingRun(String accessToken) {
        LOG.debug("accessTrainingRun({})", accessToken);
        Assert.hasLength(accessToken, "AccessToken cannot be null or empty.");
        List<TrainingInstance> trainingInstances = trainingInstanceRepository.findAllByStartTimeAfterAndEndTimeBefore(LocalDateTime.now());
        for (TrainingInstance trainingInstance : trainingInstances) {
            if (trainingInstance.getAccessToken().equals(accessToken)) {
                Set<SandboxInstanceRef> freeSandboxes = trainingRunRepository.findFreeSandboxesOfTrainingInstance(trainingInstance.getId());
                if (!freeSandboxes.isEmpty()) {
                    SandboxInstanceRef sandboxInstanceRef = getReadySandboxInstanceRef(freeSandboxes);
                    AbstractLevel al = abstractLevelRepository.findById(trainingInstance.getTrainingDefinition().getStartingLevel()).orElseThrow(() -> new ServiceLayerException("No starting level available for this training definition", ErrorCode.RESOURCE_NOT_FOUND));
                    TrainingRun trainingRun = getNewTrainingRun(al, getSubOfLoggedInUser(), trainingInstance, TRState.NEW, LocalDateTime.now(), trainingInstance.getEndTime(), sandboxInstanceRef);
                    trainingRun = create(trainingRun);
                    // audit this action to the Elasticsearch
                    auditTrainingRunStartedAction(trainingInstance, trainingRun);
                    auditLevelStartedAction(trainingInstance, trainingRun);
                    return trainingRun;
                } else {
                    throw new ServiceLayerException("There is no available sandbox, wait a minute and try again.", ErrorCode.NO_AVAILABLE_SANDBOX);
                }
            }
        }
        throw new ServiceLayerException("There is no training instance with accessToken " + accessToken + ".", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Override
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or  @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public TrainingRun resumeTrainingRun(Long trainingRunId) {
        LOG.debug("resumeTrainingRun({})", trainingRunId);
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        if (trainingRun.getState().equals(TRState.ARCHIVED)) {
            throw new ServiceLayerException("Cannot resumed archived training run.", ErrorCode.RESOURCE_CONFLICT);
        }
        auditTrainingRunResumedAction(trainingRun);
        return trainingRun;
    }

    private TrainingRun getNewTrainingRun(AbstractLevel currentLevel, String participantRefLogin, TrainingInstance trainingInstance,
                                          TRState state, LocalDateTime startTime, LocalDateTime endTime, SandboxInstanceRef sandboxInstanceRef) {
        TrainingRun tR = new TrainingRun();
        tR.setCurrentLevel(currentLevel);
        tR.setParticipantRef(participantRefRepository.findByUserRefLogin(participantRefLogin)
                .orElse(participantRefRepository.save(new UserRef(participantRefLogin))));
        tR.setAssessmentResponses("[]");
        //TODO what state set at the begining
        tR.setState(TRState.NEW);
        tR.setTrainingInstance(trainingInstance);
        tR.setStartTime(startTime);
        tR.setEndTime(endTime);
        tR.setSandboxInstanceRef(sandboxInstanceRef);
        return tR;
    }

    private SandboxInstanceRef getReadySandboxInstanceRef(Set<SandboxInstanceRef> sandboxInstancePool) {
        List<Long> idsOfNotAllocatedSandboxes = new ArrayList<>();
        for (SandboxInstanceRef sIR : sandboxInstancePool) {
            idsOfNotAllocatedSandboxes.add(sIR.getSandboxInstanceRef());
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        String listOfIds = idsOfNotAllocatedSandboxes.stream().map(Object::toString).collect(Collectors.joining(","));
        ResponseEntity<List<SandboxInfo>> response = restTemplate.exchange(serverUrl + SANDBOX_INFO_ENDPOINT, HttpMethod.GET, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>() {
        }, listOfIds);
        if (response.getStatusCode().isError()) {
            throw new ServiceLayerException("Some error occurred during getting info about sandboxes.", ErrorCode.UNEXPECTED_ERROR);
        }
        List<SandboxInfo> sandboxInfoList = response.getBody();
        sandboxInfoList.removeIf(s -> !s.getState().equals("READY"));
        if (sandboxInfoList.isEmpty()) {
            throw new ServiceLayerException("There is no available sandbox, wait a minute and try again.", ErrorCode.NO_AVAILABLE_SANDBOX);
        } else {
            sandboxInstancePool.removeIf(sandboxInstanceRef -> sandboxInstanceRef.getSandboxInstanceRef() != sandboxInfoList.get(0).getId());
            return sandboxInstancePool.iterator().next();
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public boolean isCorrectFlag(Long trainingRunId, String flag) {
        LOG.debug("isCorrectFlag({})", trainingRunId);
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        Assert.hasLength(flag, "Submitted flag must not be nul nor empty.");
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof GameLevel) {
            if (((GameLevel) level).getFlag().equals(flag)) {
                trainingRun.setLevelAnswered(true);
                auditCorrectFlagSubmittedAction(trainingRun, flag);
                auditLevelCompletedAction(trainingRun);
                return true;
            } else {
                trainingRun.setIncorrectFlagCount(trainingRun.getIncorrectFlagCount() + 1);
                auditWrongFlagSubmittedAction(trainingRun, flag);
                return false;
            }
        } else {
            throw new ServiceLayerException("Current level is not game level and does not have flag.", ErrorCode.WRONG_LEVEL_TYPE);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public int getRemainingAttempts(Long trainingRunId) {
        LOG.debug("getRemainingAttempts({})", trainingRunId);
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        TrainingRun tR = findByIdWithLevel(trainingRunId);
        AbstractLevel level = tR.getCurrentLevel();
        if (level instanceof GameLevel) {
            if (tR.isSolutionTaken()) {
                return 0;
            }
            return ((GameLevel) level).getIncorrectFlagLimit() - tR.getIncorrectFlagCount();
        }
        throw new ServiceLayerException("Current level is not game level and does not have flag.", ErrorCode.WRONG_LEVEL_TYPE);
    }


    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public String getSolution(Long trainingRunId) {
        LOG.debug("getSolution({})", trainingRunId);
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof GameLevel) {
            //audit
            trainingRun.setSolutionTaken(true);
            trainingRun.decreaseTotalScore(trainingRun.getCurrentScore() - 1);
            trainingRun.setCurrentScore(1);
            auditSolutionDisplayedAction(trainingRun, (GameLevel) level);
            trainingRunRepository.save(trainingRun);
            return ((GameLevel) level).getSolution();
        } else {
            throw new ServiceLayerException("Current level is not game level and does not have solution.", ErrorCode.WRONG_LEVEL_TYPE);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public Hint getHint(Long trainingRunId, Long hintId) {
        LOG.debug("getHint({},{})", trainingRunId, hintId);
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        Assert.notNull(hintId, "Input hint id must not be null.");
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof GameLevel) {
            Hint hint = hintRepository.findById(hintId).orElseThrow(() -> new ServiceLayerException("Hint with id " + hintId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
            if (hint.getGameLevel().getId().equals(level.getId())) {
                trainingRun.decreaseCurrentScore(hint.getHintPenalty());
                trainingRun.decreaseTotalScore(hint.getHintPenalty());
                auditHintTakenAction(trainingRun, hint);
                return hint;
            }
            throw new ServiceLayerException("Hint with id " + hintId + " is not in current level of training run: " + trainingRunId + ".", ErrorCode.RESOURCE_CONFLICT);
        } else {
            throw new ServiceLayerException("Current level is not game level and does not have hints.", ErrorCode.WRONG_LEVEL_TYPE);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority({T(cz.muni.ics.kypo.training.persistence.model.enums.RoleType).TRAINEE})")
    public int getLevelOrder(Long idOfFirstLevel, Long actualLevel) {
        LOG.debug("getLevelOrder({}, {})", idOfFirstLevel, actualLevel);
        Assert.notNull(idOfFirstLevel, "Input id of first level must not be null.");
        Assert.notNull(actualLevel, "Input id of actual level must not be null.");
        int order = 0;
        AbstractLevel abstractLevel = abstractLevelRepository.findById(idOfFirstLevel).orElseThrow(() -> new ServiceLayerException("Level with id " + idOfFirstLevel + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
        while (!abstractLevel.getId().equals(actualLevel)) {
            order++;
            if (abstractLevel.getNextLevel() == null) {
                throw new IllegalArgumentException("Wrong parameters entered.");
            }
            abstractLevel = abstractLevelRepository.findById(abstractLevel.getNextLevel()).orElseThrow(() -> new ServiceLayerException("Level with id " + idOfFirstLevel + " not found.", ErrorCode.RESOURCE_NOT_FOUND));

        }
        return order;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public void archiveTrainingRun(Long trainingRunId) {
        LOG.debug("archiveTrainingRun({})", trainingRunId);
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        TrainingRun trainingRun = findById(trainingRunId);
        if (trainingRun.getCurrentLevel().getNextLevel() != null || !trainingRun.isLevelAnswered()) {
            throw new ServiceLayerException("Cannot archive training run because current level is not last or is not answered.", ErrorCode.RESOURCE_CONFLICT);
        }

        trainingRun.setState(TRState.ARCHIVED);
        auditLevelCompletedAction(trainingRun);
        auditTrainingRunEndedAction(trainingRun);
    }

    private TrainingRun findByIdWithLevel(Long trainingRunId) {
        LOG.debug("findById({})", trainingRunId);
        Objects.requireNonNull(trainingRunId);
        return trainingRunRepository.findByIdWithLevel(trainingRunId).orElseThrow(() ->
                new ServiceLayerException("Training Run with id: " + trainingRunId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    private String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("sub").getAsString();
    }

    private void auditTrainingRunStartedAction(TrainingInstance trainingInstance, TrainingRun trainingRun) {
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();

            TrainingRunStarted trainingRunStarted = TrainingRunStarted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .build();

            auditService.save(trainingRunStarted);
        }
    }

    private void auditLevelStartedAction(TrainingInstance trainingInstance, TrainingRun trainingRun) {
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();
            LevelType levelType = getLevelType(trainingRun.getCurrentLevel());

            LevelStarted levelStarted = LevelStarted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .levelType(levelType)
                    .maxScore(trainingRun.getCurrentLevel().getMaxScore())
                    .levelTitle(trainingInstance.getTitle())
                    .build();

            auditService.save(levelStarted);
        }
    }

    private void auditLevelCompletedAction(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();
            LevelType levelType = getLevelType(trainingRun.getCurrentLevel());

            LevelCompleted levelCompleted = LevelCompleted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .levelType(levelType)
                    .build();

            auditService.save(levelCompleted);
        }
    }

    private void auditHintTakenAction(TrainingRun trainingRun, Hint hint) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();
            HintTaken hintTaken = HintTaken.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .hintId(hint.getId())
                    .hintPenaltyPoints(hint.getHintPenalty())
                    .hintTitle(hint.getTitle())
                    .build();
            LOG.info("AUDIT AFT");
            auditService.save(hintTaken);
        }
    }

    private void auditSolutionDisplayedAction(TrainingRun trainingRun, GameLevel gameLevel) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();

            SolutionDisplayed solutionDisplayed = SolutionDisplayed.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .penaltyPoints(gameLevel.getMaxScore() - trainingRun.getCurrentScore())
                    .build();
            auditService.save(solutionDisplayed);
        }
    }

    private void auditCorrectFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();

            CorrectFlagSubmitted correctFlagSubmitted = CorrectFlagSubmitted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore()) // requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .flagContent(flag)
                    .build();
            auditService.save(correctFlagSubmitted);
        }
    }

    private void auditWrongFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();

            WrongFlagSubmitted wrongFlagSubmitted = WrongFlagSubmitted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .flagContent(flag)
                    .count(trainingRun.getIncorrectFlagCount())
                    .build();
            auditService.save(wrongFlagSubmitted);
        }
    }

    private void auditAssessmentAnswersAction(TrainingRun trainingRun, String answers) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();

            AssessmentAnswers assessmentAnswers = AssessmentAnswers.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .answers(answers)
                    .build();
            auditService.save(assessmentAnswers);
        }
    }

    private void auditTrainingRunEndedAction(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();

            TrainingRunEnded assessmentAnswers = TrainingRunEnded.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .build();
            auditService.save(assessmentAnswers);
        }
    }

    private void auditTrainingRunResumedAction(TrainingRun trainingRun) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandboxDefinitionRefId();

            TrainingRunResumed trainingRunResumed = TrainingRunResumed.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(trainingRun.getTotalScore())
                    .actualScoreInLevel(trainingRun.getCurrentScore())
                    .level(trainingRun.getCurrentLevel().getId())
                    .build();
            auditService.save(trainingRunResumed);
        }
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

    @Override
    public void evaluateResponsesToAssessment(Long trainingRunId, String responsesAsString) {
        LOG.info("evaluateAndStoreResponse({})", trainingRunId);
        Assert.notNull(responsesAsString, "Response to assessment must not be null.");
        JSONArray responses = isResponseValid(responsesAsString);
        int points = 0;
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        if (!(trainingRun.getCurrentLevel() instanceof AssessmentLevel)) {
            throw new ServiceLayerException("Current level is not assessment level and cannot be evaluated.", ErrorCode.WRONG_LEVEL_TYPE);
        }
        if (trainingRun.isLevelAnswered())
            throw new ServiceLayerException("Current level has been already answered.", ErrorCode.RESOURCE_CONFLICT);
        if (trainingRun.getAssessmentResponses() == null) {
            trainingRun.setAssessmentResponses("[]");
        }
        AssessmentLevel assessmentLevel = (AssessmentLevel) trainingRun.getCurrentLevel();
        JSONArray responsesJSON = new JSONArray(trainingRun.getAssessmentResponses());
        JSONObject responseToCurrentAssessment = new JSONObject();
        responseToCurrentAssessment.put("assessmentLevelId", trainingRun.getCurrentLevel().getId());


        AssessmentUtil util = new AssessmentUtil();
        responseToCurrentAssessment.put("answers", "[" + responses.toString() + "]");
        if (assessmentLevel.getAssessmentType().equals(AssessmentType.QUESTIONNAIRE)) {
            responseToCurrentAssessment.put("receivedPoints", 0);
        } else {
            points = util.evaluateTest(new JSONArray(assessmentLevel.getQuestions()), responses);
            responseToCurrentAssessment.put("receivedPoints", points);
            trainingRun.setCurrentScore(points);
            trainingRun.setTotalScore(trainingRun.getTotalScore() - (trainingRun.getCurrentLevel().getMaxScore() - trainingRun.getCurrentScore()));
        }
        responsesJSON.put(responseToCurrentAssessment);
        trainingRun.setAssessmentResponses(responsesJSON.toString());
        trainingRun.setLevelAnswered(true);
        //TODO what is answers in audit action
        auditAssessmentAnswersAction(trainingRun, responsesAsString);
        auditLevelCompletedAction(trainingRun);
    }

    private JSONArray isResponseValid(String responses) {
        try {
            JsonNode n = JsonLoader.fromString(responses);
            final JsonNode jsonSchema = JsonLoader.fromResource("/responses-schema.json");
            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            JsonValidator v = factory.getValidator();
            ProcessingReport report = v.validate(jsonSchema, n);
            if (report.toString().contains("success")) {
                return new JSONArray(responses);
            } else {
                throw new IllegalArgumentException("Given responses are not valid. \n" + report.iterator().next().toString());
            }

        } catch (IOException | ProcessingException ex) {
            throw new ServiceLayerException(ex.getMessage(), ErrorCode.UNEXPECTED_ERROR);
        }
    }
}
