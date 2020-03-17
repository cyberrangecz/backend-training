package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.exceptions.errors.PythonApiError;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The type Training run service.
 */
@Service
public class TrainingRunService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunService.class);
    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;

    private TrainingRunRepository trainingRunRepository;
    private AbstractLevelRepository abstractLevelRepository;
    private TrainingInstanceRepository trainingInstanceRepository;
    private UserRefRepository participantRefRepository;
    private HintRepository hintRepository;
    private AuditEventsService auditEventsService;
    private SecurityService securityService;
    private TRAcquisitionLockRepository trAcquisitionLockRepository;
    private RestTemplate pythonRestTemplate;
    private static final int PYTHON_RESULT_PAGE_SIZE = 1000;

    /**
     * Instantiates a new Training run service.
     *
     * @param trainingRunRepository       the training run repository
     * @param abstractLevelRepository     the abstract level repository
     * @param trainingInstanceRepository  the training instance repository
     * @param participantRefRepository    the participant ref repository
     * @param hintRepository              the hint repository
     * @param auditEventsService          the audit events service
     * @param securityService             the security service
     * @param pythonRestTemplate          the python rest template
     * @param trAcquisitionLockRepository the tr acquisition lock repository
     */
    @Autowired
    public TrainingRunService(TrainingRunRepository trainingRunRepository, AbstractLevelRepository abstractLevelRepository,
                              TrainingInstanceRepository trainingInstanceRepository, UserRefRepository participantRefRepository,
                              HintRepository hintRepository, AuditEventsService auditEventsService, SecurityService securityService,
                              @Qualifier("pythonRestTemplate") RestTemplate pythonRestTemplate, TRAcquisitionLockRepository trAcquisitionLockRepository) {
        this.trainingRunRepository = trainingRunRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.participantRefRepository = participantRefRepository;
        this.hintRepository = hintRepository;
        this.auditEventsService = auditEventsService;
        this.securityService = securityService;
        this.pythonRestTemplate = pythonRestTemplate;
        this.trAcquisitionLockRepository = trAcquisitionLockRepository;
    }

    /**
     * Finds specific Training Run by id.
     *
     * @param runId of a Training Run that would be returned
     * @return specific {@link TrainingRun} by id
     * @throws EntityNotFoundException training run is not found.
     */
    public TrainingRun findById(Long runId) {
        return trainingRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId,
                        "Training run not found.")));
    }

    /**
     * /**
     * Finds specific Training Run by id including current level.
     *
     * @param runId of a Training Run with level that would be returned
     * @return specific {@link TrainingRun} by id
     * @throws EntityNotFoundException training run is not found.
     */
    public TrainingRun findByIdWithLevel(Long runId) {
        return trainingRunRepository.findByIdWithLevel(runId).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId, "Training run not found.")));
    }

    /**
     * Find all Training Runs.
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return all {@link TrainingRun}s
     */
    public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
        return trainingRunRepository.findAll(predicate, pageable);
    }

    /**
     * Delete selected training run.
     *
     * @param trainingRunId training run to delete
     * @param forceDelete   delete training run in a force manner
     */
    public void deleteTrainingRun(Long trainingRunId, boolean forceDelete) {
        TrainingRun trainingRun = findById(trainingRunId);
        if (!forceDelete && trainingRun.getState().equals(TRState.RUNNING)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRun.getId().getClass(), trainingRun.getId(),
                    "Cannot delete training run that is running. Consider force delete."));
        }
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
        trainingRunRepository.delete(trainingRun);
    }

    /**
     * Checks whether any trainin runs exists for particular training instance
     *
     * @param trainingInstanceId the training instance id
     * @return boolean boolean
     */
    public boolean existsAnyForTrainingInstance(Long trainingInstanceId) {
        return trainingRunRepository.existsAnyForTrainingInstance(trainingInstanceId);
    }


    /**
     * Finds all Training Runs of logged in user.
     *
     * @param pageable pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of logged in user.
     */
    public Page<TrainingRun> findAllByParticipantRefUserRefId(Pageable pageable) {
        return trainingRunRepository.findAllByParticipantRefId(securityService.getUserRefIdFromUserAndGroup(), pageable);
    }

    /**
     * Finds all Training Runs of particular training instance.
     *
     * @param trainingInstanceId the training instance id
     * @return the set
     */
    public Set<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId) {
        return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId);
    }

    private TrainingRun create(TrainingRun trainingRun) {
        return trainingRunRepository.save(trainingRun);
    }

    /**
     * Gets next level of given Training Run and set new current level.
     *
     * @param runId id of Training Run whose next level should be returned.
     * @return {@link AbstractLevel}
     * @throws EntityNotFoundException training run or level is not found.
     */
    public AbstractLevel getNextLevel(Long runId) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        int currentLevelOrder = trainingRun.getCurrentLevel().getOrder();
        int maxLevelOrder = abstractLevelRepository.getCurrentMaxOrder(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        if (!trainingRun.isLevelAnswered()) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId,
                    "You need to answer the level to move to the next level."));
        }
        if (currentLevelOrder == maxLevelOrder) {
            throw new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "There is no next level for current training run (ID: " + runId + ")."));
        }
        List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        Collections.sort(levels, Comparator.comparing(AbstractLevel::getOrder));
        int nextLevelIndex = levels.indexOf(trainingRun.getCurrentLevel()) + 1;
        AbstractLevel abstractLevel = levels.get(nextLevelIndex);

        if (trainingRun.getCurrentLevel() instanceof InfoLevel) {
            auditEventsService.auditLevelCompletedAction(trainingRun);
        }
        trainingRun.setCurrentLevel(abstractLevel);
        trainingRun.setIncorrectFlagCount(0);
        trainingRunRepository.save(trainingRun);
        auditEventsService.auditLevelStartedAction(trainingRun);

        return abstractLevel;
    }

    /**
     * Finds all Training Runs of specific Training Definition of logged in user.
     *
     * @param definitionId id of Training Definition
     * @param pageable     pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific Training Definition of logged in user
     */
    public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long definitionId, Pageable pageable) {
        return trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantUserRefId(definitionId, securityService.getUserRefIdFromUserAndGroup(), pageable);
    }

    /**
     * Finds all Training Runs of specific training definition.
     *
     * @param definitionId id of Training Definition whose Training Runs would be returned.
     * @param pageable     pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific Training Definition
     */
    public Page<TrainingRun> findAllByTrainingDefinition(Long definitionId, Pageable pageable) {
        return trainingRunRepository.findAllByTrainingDefinitionId(definitionId, pageable);
    }

    /**
     * Gets list of all levels in Training Definition.
     *
     * @param definitionId must be id of first level of some Training Definition.
     * @return List of {@link AbstractLevel}s
     * @throws EntityNotFoundException one of the levels is not found.
     */
    public List<AbstractLevel> getLevels(Long definitionId) {
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    }

    /**
     * Access training run based on given accessToken.
     *
     * @param trainingInstance the training instance
     * @param participantRefId the participant ref id
     * @return accessed {@link TrainingRun}
     * @throws EntityNotFoundException  no active training instance for given access token, no starting level in training definition.
     * @throws EntityConflictException  pool of sandboxes is not created for training instance.
     * @throws TooManyRequestsException training run has been already accessed.
     */
    public TrainingRun accessTrainingRun(TrainingInstance trainingInstance, Long participantRefId) {
        List<AbstractLevel> levels = getAllLevelsForTRSortedByOrder(trainingInstance.getTrainingDefinition().getId());
        TrainingRun trainingRun = getNewTrainingRun(levels.get(0), trainingInstance, LocalDateTime.now(Clock.systemUTC()), trainingInstance.getEndTime(), participantRefId);

        assignSandbox(trainingRun, trainingInstance.getPoolId());
        auditEventsService.auditTrainingRunStartedAction(trainingRun);
        auditEventsService.auditLevelStartedAction(trainingRun);
        return trainingRunRepository.save(trainingRun);
    }

    /**
     * Find running training run of user optional.
     *
     * @param accessToken      the access token
     * @param participantRefId the participant ref id
     * @return the optional
     */
    public Optional<TrainingRun> findRunningTrainingRunOfUser(String accessToken, Long participantRefId) {
        return trainingRunRepository.findRunningTrainingRunOfUser(accessToken, participantRefId);
    }

    /**
     * Gets training instance for particular access token.
     *
     * @param accessToken the access token
     * @return the training instance for particular access token
     */
    public TrainingInstance getTrainingInstanceForParticularAccessToken(String accessToken) {
        TrainingInstance trainingInstance = trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                        "There is no active game session matching access token.")));
        if (trainingInstance.getPoolId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "At first organizer must allocate sandboxes for training instance."));
        }
        return trainingInstance;
    }

    /**
     * Tr acquisition lock to prevent many requests from the same user. This method is called in a new transaction that means that the existing one is suspended.
     *
     * @param participantRefId   the participant ref id
     * @param trainingInstanceId the training instance id
     * @param accessToken        the access token
     */
    @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
    public void trAcquisitionLockToPreventManyRequestsFromSameUser(Long participantRefId, Long trainingInstanceId, String accessToken) {
        try {
            trAcquisitionLockRepository.save(new TRAcquisitionLock(participantRefId, trainingInstanceId, LocalDateTime.now(Clock.systemUTC())));
        } catch (DataIntegrityViolationException ex) {
            throw new TooManyRequestsException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                    "Training run has been already accessed and cannot be created again. Please resume Training Run"));
        }
    }

    private List<AbstractLevel> getAllLevelsForTRSortedByOrder(Long trainingDefinitionId) {
        List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinitionId);
        if (levels.isEmpty()) {
            throw new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", Long.class, trainingDefinitionId,
                    "No starting level available for this training definition."));
        }
        levels.sort(Comparator.comparing(AbstractLevel::getOrder));
        return levels;
    }

    private TrainingRun getNewTrainingRun(AbstractLevel currentLevel, TrainingInstance trainingInstance, LocalDateTime startTime, LocalDateTime endTime, Long participantRefId) {
        TrainingRun newTrainingRun = new TrainingRun();
        newTrainingRun.setCurrentLevel(currentLevel);

        Optional<UserRef> userRefOpt = participantRefRepository.findUserByUserRefId(participantRefId);
        if (userRefOpt.isPresent()) {
            newTrainingRun.setParticipantRef(userRefOpt.get());
        } else {
            newTrainingRun.setParticipantRef(participantRefRepository.save(securityService.createUserRefEntityByInfoFromUserAndGroup()));
        }
        newTrainingRun.setAssessmentResponses("[]");
        newTrainingRun.setState(TRState.RUNNING);
        newTrainingRun.setTrainingInstance(trainingInstance);
        newTrainingRun.setStartTime(startTime);
        newTrainingRun.setEndTime(endTime);
        return newTrainingRun;
    }

    /**
     * Connects available sandbox with given Training run.
     *
     * @param trainingRun that will be connected with sandbox
     * @param poolId      the pool id
     * @return Training run with assigned sandbox
     */
    public TrainingRun assignSandbox(TrainingRun trainingRun, long poolId) {
        Long sandboxInstanceRef = getAndLockSandboxForTrainingRun(poolId);
        trainingRun.setSandboxInstanceRefId(sandboxInstanceRef);
        return trainingRunRepository.save(trainingRun);
    }

    private Long getAndLockSandboxForTrainingRun(Long poolId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(kypoOpenStackURI + "/pools/" + poolId + "/sandboxes/get-and-lock/");
        try {
            ResponseEntity<SandboxInfo> response = pythonRestTemplate.getForEntity(builder.toString(), SandboxInfo.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody().getId();
            } else {
                throw new MicroserviceApiException("There is no available sandbox now for pool with (ID: " + poolId + ").", null);
            }
        } catch (NullPointerException ex) {
            throw new ForbiddenException(ex.getMessage());
        } catch (CustomRestTemplateException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ForbiddenException("There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.");
            }
            throw new MicroserviceApiException("Error when calling Python API to get unlocked sandbox from pool (ID: " + poolId + ")", ex.getApiSubError());
        }
    }

    /**
     * Resume previously closed training run.
     *
     * @param trainingRunId id of training run to be resumed.
     * @return {@link TrainingRun}
     * @throws EntityNotFoundException training run is not found.
     */
    public TrainingRun resumeTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        if (trainingRun.getState().equals(TRState.FINISHED) || trainingRun.getState().equals(TRState.ARCHIVED)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot resume finished training run."));
        }
        if (trainingRun.getTrainingInstance().getEndTime().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot resume training run after end of training instance."));
        }
        if (trainingRun.getSandboxInstanceRefId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Sandbox of this training run was already deleted, you have to start new game."));
        }
        auditEventsService.auditTrainingRunResumedAction(trainingRun);
        return trainingRun;
    }

    /**
     * Check given flag of given Training Run.
     *
     * @param runId id of Training Run to check flag.
     * @param flag  string which player submit.
     * @return true if flag is correct, false if flag is wrong.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException     the current level of training run is not game level.
     */
    public boolean isCorrectFlag(Long runId, String flag) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof GameLevel) {
            if (((GameLevel) level).getFlag().equals(flag)) {
                trainingRun.setLevelAnswered(true);
                trainingRun.increaseTotalScore(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty());
                auditEventsService.auditCorrectFlagSubmittedAction(trainingRun, flag);
                auditEventsService.auditLevelCompletedAction(trainingRun);
                return true;
            } else if (trainingRun.getIncorrectFlagCount() == ((GameLevel) level).getIncorrectFlagLimit()) {
                auditEventsService.auditWrongFlagSubmittedAction(trainingRun, flag);
            } else {
                trainingRun.setIncorrectFlagCount(trainingRun.getIncorrectFlagCount() + 1);
                auditEventsService.auditWrongFlagSubmittedAction(trainingRun, flag);
            }
            return false;
        } else {
            throw new BadRequestException("Current level is not game level and does not have flag.");
        }
    }

    /**
     * Gets remaining attempts to solve current level of training run.
     *
     * @param trainingRunId the training run id
     * @return the remaining attempts
     */
    public int getRemainingAttempts(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof GameLevel) {
            if (trainingRun.isSolutionTaken()) {
                return 0;
            }
            return ((GameLevel) level).getIncorrectFlagLimit() - trainingRun.getIncorrectFlagCount();
        }
        throw new BadRequestException("Current level is not game level and does not have flag.");
    }

    /**
     * Gets solution of current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets solution for.
     * @return solution of current level.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException     the current level of training run is not game level.
     */
    public String getSolution(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof GameLevel) {
            if (!trainingRun.isSolutionTaken()) {
                trainingRun.setSolutionTaken(true);
                if (((GameLevel) level).isSolutionPenalized())
                    trainingRun.setCurrentPenalty(trainingRun.getMaxLevelScore());
                trainingRunRepository.save(trainingRun);
            }
            auditEventsService.auditSolutionDisplayedAction(trainingRun);
            return ((GameLevel) level).getSolution();
        } else {
            throw new BadRequestException("Current level is not game level and does not have solution.");
        }
    }

    /**
     * Gets hint of given current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @param hintId        id of hint to be returned.
     * @return {@link Hint}
     * @throws EntityNotFoundException training run or hint is not found.
     * @throws BadRequestException     the current level of training run is not game level.
     */
    public Hint getHint(Long trainingRunId, Long hintId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof GameLevel) {
            Hint hint = hintRepository.findById(hintId).orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Hint.class, "id", hintId.getClass(), hintId,
                    "Hint not found.")));
            if (hint.getGameLevel().getId().equals(level.getId())) {
                trainingRun.increaseCurrentPenalty(hint.getHintPenalty());
                trainingRun.addHintInfo(new HintInfo(level.getId(), hint.getId(), hint.getTitle(), hint.getContent(), hint.getOrder()));
                auditEventsService.auditHintTakenAction(trainingRun, hint);
                return hint;
            }
            throw new EntityConflictException(new EntityErrorDetail(Hint.class, "id", hintId.getClass(), hintId,
                    "Hint is not in current level of training run: " + trainingRunId + "."));
        } else {
            throw new BadRequestException("Current level is not game level and does not have hints.");
        }
    }

    /**
     * Gets max level order of levels from definition.
     *
     * @param definitionId id of training definition.
     * @return max order of levels.
     */
    public int getMaxLevelOrder(Long definitionId) {
        return abstractLevelRepository.getCurrentMaxOrder(definitionId);
    }

    /**
     * Finish training run.
     *
     * @param trainingRunId id of training run to be finished.
     * @throws EntityNotFoundException training run is not found.
     */
    public void finishTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = findById(trainingRunId);
        int maxOrder = abstractLevelRepository.getCurrentMaxOrder(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        if (trainingRun.getCurrentLevel().getOrder() != maxOrder) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot finish training run because current level is not last."));
        }
        if (!trainingRun.isLevelAnswered()) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot finish training run because current level is not answered."));
        }

        trainingRun.setState(TRState.FINISHED);
        trainingRun.setEndTime(LocalDateTime.now(Clock.systemUTC()));
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
        if (trainingRun.getCurrentLevel() instanceof InfoLevel) {
            auditEventsService.auditLevelCompletedAction(trainingRun);
        }
        auditEventsService.auditTrainingRunEndedAction(trainingRun);
    }

    /**
     * Evaluate and store responses to assessment.
     *
     * @param trainingRunId     id of training run to be finished.
     * @param responsesAsString response to assessment to be evaluated
     * @throws EntityNotFoundException training run is not found.
     */
    public void evaluateResponsesToAssessment(Long trainingRunId, String responsesAsString) {
        JSONArray responses = isResponseValid(responsesAsString);
        int points = 0;
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        if (!(trainingRun.getCurrentLevel() instanceof AssessmentLevel)) {
            throw new BadRequestException("Current level is not assessment level and cannot be evaluated.");
        }
        if (trainingRun.isLevelAnswered())
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Current level of the training run has been already answered."));
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
            trainingRun.increaseTotalScore(points);
        }
        responsesJSON.put(responseToCurrentAssessment);
        trainingRun.setAssessmentResponses(responsesJSON.toString());
        trainingRun.setLevelAnswered(true);
        auditEventsService.auditAssessmentAnswersAction(trainingRun, responsesAsString);
        auditEventsService.auditLevelCompletedAction(trainingRun);
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
            throw new InternalServerErrorException(ex.getMessage());
        }
    }
}
