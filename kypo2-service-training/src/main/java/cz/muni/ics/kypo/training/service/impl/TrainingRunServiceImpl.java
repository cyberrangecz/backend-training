package cz.muni.ics.kypo.training.service.impl;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.utils.SandboxInfo;

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
    private ParticipantRefRepository participantRefRepository;
    private HintRepository hintRepository;
    private RestTemplate restTemplate;
    private AuditService auditService;

    @Autowired
    public TrainingRunServiceImpl(TrainingRunRepository trainingRunRepository, AbstractLevelRepository abstractLevelRepository,
                                  TrainingInstanceRepository trainingInstanceRepository, ParticipantRefRepository participantRefRepository,
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
        if (nextLevelId == null) {
            throw new ServiceLayerException("There is no next level.", ErrorCode.NO_NEXT_LEVEL);
        }
        AbstractLevel abstractLevel = abstractLevelRepository.findById(nextLevelId).orElseThrow(() ->
                new ServiceLayerException("Level with id: " + nextLevelId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
        trainingRun.setCurrentLevel(abstractLevel);
        trainingRun.setSolutionTaken(false);
        trainingRunRepository.save(trainingRun);
        //audit this action to theElasticSearch
        auditLevelStartedAction(trainingRun.getTrainingInstance(), trainingRun);
        auditLevelCompletedAction(trainingRun);
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
    public AbstractLevel accessTrainingRun(String password) {
        LOG.debug("accessTrainingRun({})", password);
        Assert.hasLength(password, "Password cannot be null or empty.");
        List<TrainingInstance> trainingInstances = trainingInstanceRepository.findAll();
        for (TrainingInstance trainingInstance : trainingInstances) {

            //check hash of password not String
            if (trainingInstance.getPassword().equals(password)) {
                Set<SandboxInstanceRef> sandboxInstancePool = trainingInstance.getSandboxInstanceRefs();
                Set<SandboxInstanceRef> allocatedSandboxInstances = trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(trainingInstance.getId());
                sandboxInstancePool.removeAll(allocatedSandboxInstances);
                if (!sandboxInstancePool.isEmpty()) {
                    SandboxInstanceRef sandboxInstanceRef = getReadySandboxInstanceRef(sandboxInstancePool);
                    AbstractLevel al = abstractLevelRepository.findById(trainingInstance.getTrainingDefinition().getStartingLevel()).orElseThrow(() -> new ServiceLayerException("No starting level available for this training definition", ErrorCode.RESOURCE_NOT_FOUND));
                    TrainingRun trainingRun =
                            getNewTrainingRun(al, getSubOfLoggedInUser(), trainingInstance, TRState.NEW, LocalDateTime.now(), trainingInstance.getEndTime(), sandboxInstanceRef);
                    trainingRun = create(trainingRun);
                    // audit this action to the Elasticsearch
                    auditTrainingRunStartedAction(trainingInstance, trainingRun);
                    auditLevelStartedAction(trainingInstance, trainingRun);
                    return al;
                } else {
                    throw new ServiceLayerException("There is no available sandbox, wait a minute and try again.", ErrorCode.NO_AVAILABLE_SANDBOX);
                }
            }
        }
        throw new ServiceLayerException("There is no training instance with password " + password + ".", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Override
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or  @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public AbstractLevel resumeTrainingRun(Long trainingRunId) {
        LOG.debug("resumeTrainingRun({})", trainingRunId);
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        if (trainingRun.getState().equals(TRState.ARCHIVED)) {
            throw new ServiceLayerException("Cannot resumed archived training run.", ErrorCode.RESOURCE_CONFLICT);
        }
        auditTrainingRunResumedAction(trainingRun);
        return trainingRun.getCurrentLevel();
    }

    private TrainingRun getNewTrainingRun(AbstractLevel currentLevel, String participantRefLogin, TrainingInstance trainingInstance,
                                          TRState state, LocalDateTime startTime, LocalDateTime endTime, SandboxInstanceRef sandboxInstanceRef) {
        TrainingRun tR = new TrainingRun();
        tR.setCurrentLevel(currentLevel);
        tR.setParticipantRef(participantRefRepository.findByParticipantRefLogin(participantRefLogin)
                .orElse(participantRefRepository.save(new ParticipantRef(participantRefLogin))));
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
                //tR.setIncorrectFlagCount(0);
                trainingRunRepository.save(trainingRun);
                auditCorrectFlagSubmittedAction(trainingRun, flag);
                return true;
            } else {
                trainingRun.setIncorrectFlagCount(trainingRun.getIncorrectFlagCount() + 1);
                trainingRunRepository.save(trainingRun);
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
            auditSolutionDisplayedAction(trainingRun, (GameLevel) level);
            trainingRun.setSolutionTaken(true);
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
                // audit this action to the Elasticsearch
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
        if(trainingRun.getCurrentLevel().getNextLevel() != null) {
            throw new ServiceLayerException("Cannot archive training run because current level is not last.", ErrorCode.RESOURCE_CONFLICT);
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
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();

            TrainingRunStarted trainingRunStarted = TrainingRunStarted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
                    .level(trainingRun.getCurrentLevel().getId())
                    .build();

            auditService.save(trainingRunStarted);
        }
    }

    private void auditLevelStartedAction(TrainingInstance trainingInstance, TrainingRun trainingRun) {
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();
            LevelType levelType = getLevelType(trainingRun.getCurrentLevel());

            LevelStarted levelStarted = LevelStarted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
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
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();
            LevelType levelType = getLevelType(trainingRun.getCurrentLevel());

            LevelCompleted levelCompleted = LevelCompleted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
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
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();

            HintTaken hintTaken = HintTaken.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
                    .level(trainingRun.getCurrentLevel().getId())
                    .hintId(hint.getId())
                    .hintPenaltyPoints(hint.getHintPenalty())
                    .hintTitle(hint.getTitle())
                    .build();

            auditService.save(hintTaken);
        }
    }

    private void auditSolutionDisplayedAction(TrainingRun trainingRun, GameLevel gameLevel) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();

            SolutionDisplayed solutionDisplayed = SolutionDisplayed.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
                    .level(trainingRun.getCurrentLevel().getId())
                    .penaltyPoints(111) //TODO repair, no attribute
                    .build();
            auditService.save(solutionDisplayed);
        }
    }

    private void auditCorrectFlagSubmittedAction(TrainingRun trainingRun, String flag) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingInstance != null) {
            TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
            Long trainingDefinitionId = trainingDefinition.getId();
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();

            CorrectFlagSubmitted correctFlagSubmitted = CorrectFlagSubmitted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) // requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
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
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();

            WrongFlagSubmitted wrongFlagSubmitted = WrongFlagSubmitted.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
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
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();

            AssessmentAnswers assessmentAnswers = AssessmentAnswers.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
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
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();

            TrainingRunEnded assessmentAnswers = TrainingRunEnded.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
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
            Long sandboxId = trainingDefinition.getSandBoxDefinitionRef().getId();

            TrainingRunResumed trainingRunResumed = TrainingRunResumed.builder()
                    .sandboxId(sandboxId)
                    .trainingDefinitionId(trainingDefinitionId)
                    .trainingInstanceId(trainingInstance.getId())
                    .trainingRunId(trainingRun.getId())
                    .playerLogin(getSubOfLoggedInUser())
                    .totalScore(0) //TODO requires to set total and actual score in level from training run entity
                    .actualScoreInLevel(0)
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
}
