package cz.muni.ics.kypo.training.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.exceptions.errors.PythonApiError;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TrainingRunServiceImpl implements TrainingRunService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunServiceImpl.class);
    private static final String MUST_NOT_BE_NULL = "Input training run id must not be null.";
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
    @Qualifier("pythonRestTemplate")
    private RestTemplate pythonRestTemplate;
    private static final int PYTHON_RESULT_PAGE_SIZE = 1000;

    @Autowired
    public TrainingRunServiceImpl(TrainingRunRepository trainingRunRepository, AbstractLevelRepository abstractLevelRepository,
                                  TrainingInstanceRepository trainingInstanceRepository, UserRefRepository participantRefRepository,
                                  HintRepository hintRepository, AuditEventsService auditEventsService, SecurityService securityService,
                                  RestTemplate pythonRestTemplate, TRAcquisitionLockRepository trAcquisitionLockRepository) {
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

    @Override
    public TrainingRun findById(Long runId) {
        Objects.requireNonNull(runId);
        return trainingRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId,
                        "Training run not found.")));
    }

    @Override
    public TrainingRun findByIdWithLevel(Long runId) {
        Objects.requireNonNull(runId);
        return trainingRunRepository.findByIdWithLevel(runId).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId, "Training run not found.")));
    }

    @Override
    public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
        return trainingRunRepository.findAll(predicate, pageable);
    }

    @Override
    public void deleteTrainingRuns(List<Long> trainingRunIds) {
        trainingRunIds.forEach(this::deleteTrainingRun);
    }

    @Override
    public void deleteTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunRepository.findById(trainingRunId).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId, "Training run not found.")));
        if (trainingRun.getSandboxInstanceRefId() == null) {
            try {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
                ResponseEntity<SandboxInfo> response = pythonRestTemplate.exchange(kypoOpenStackURI + "/sandboxes/" + trainingRun.getPreviousSandboxInstanceRefId() + "/", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                        new ParameterizedTypeReference<SandboxInfo>() {
                        });
                if (response.getStatusCode().is2xxSuccessful()) {
                    throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRun ,
                            "Sandbox (id:" + trainingRun.getPreviousSandboxInstanceRefId() + ") previously assigned to the training run was not deleted in OpenStack. " +
                                    "Please delete sandbox in OpenStack before you delete training run."));
                }
            } catch (RestTemplateException ex) {
                if (!ex.getStatusCode().equals(HttpStatus.NOT_FOUND.toString())) {
                    throw new MicroserviceApiException("Error when calling Python API to obtain info about sandbox (ID: " + trainingRun.getPreviousSandboxInstanceRefId() + ")", new PythonApiError(ex.getMessage()));
                }
                LOG.debug("Sandbox (ID:" + trainingRun.getPreviousSandboxInstanceRefId() + ") previously assigned to the training run (ID: " + trainingRunId + ") is not found in OpenStack because it was successfully deleted.");
            }
            trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
            trainingRunRepository.delete(trainingRun);
        } else {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Could not delete training run with associated sandbox (id: " + trainingRun.getSandboxInstanceRefId() +
                    "). Please firstly, delete associated sandbox."));
        }
    }

    @Override
    public Page<TrainingRun> findAllByParticipantRefUserRefId(Pageable pageable) {
        return trainingRunRepository.findAllByParticipantRefId(securityService.getUserRefIdFromUserAndGroup(), pageable);
    }

    private TrainingRun create(TrainingRun trainingRun) {
        Assert.notNull(trainingRun, "Input training run must not be empty.");
        return trainingRunRepository.save(trainingRun);
    }

    @Override
    public AbstractLevel getNextLevel(Long runId) {
        Assert.notNull(runId, MUST_NOT_BE_NULL);
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

    @Override
    public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long definitionId, Pageable pageable) {
        Assert.notNull(definitionId, "Input training definition id must not be null.");
        return trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantUserRefId(definitionId, securityService.getUserRefIdFromUserAndGroup(), pageable);
    }

    @Override
    public Page<TrainingRun> findAllByTrainingDefinition(Long definitionId, Pageable pageable) {
        Assert.notNull(definitionId, "Input training definition id must not be null.");
        return trainingRunRepository.findAllByTrainingDefinitionId(definitionId, pageable);
    }

    @Override
    public List<AbstractLevel> getLevels(Long definitionId) {
        Assert.notNull(definitionId, "Id of training definition must not be null.");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    }


    @Override
    public TrainingRun accessTrainingRun(String accessToken) {
        Assert.hasLength(accessToken, "AccessToken cannot be null or empty.");
        TrainingInstance trainingInstance = trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                        "There is no active game session matching access token.")));
        if (trainingInstance.getPoolId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "At first organizer must allocate sandboxes for training instance."));
        }
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        Optional<TrainingRun> accessedTrainingRun = trainingRunRepository.findValidTrainingRunOfUser(trainingInstance.getAccessToken(), participantRefId);
        if (accessedTrainingRun.isPresent()) {
            return resumeTrainingRun(accessedTrainingRun.get().getId());
        }
        try {
            trAcquisitionLockRepository.save(new TRAcquisitionLock(participantRefId, trainingInstance.getId(), LocalDateTime.now()));
        } catch (DataIntegrityViolationException ex) {
            throw new TooManyRequestsException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                    "Training run has been already accessed and cannot be created again. Please resume Training Run"));
        }

        List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
        if (levels.isEmpty()) {
            throw new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", Long.class, trainingInstance.getTrainingDefinition().getId(),
                    "No starting level available for this training definition."));
        }
        levels.sort(Comparator.comparing(AbstractLevel::getOrder));
        TrainingRun trainingRun = getNewTrainingRun(levels.get(0), trainingInstance, TRState.RUNNING,
                LocalDateTime.now(Clock.systemUTC()), trainingInstance.getEndTime(), participantRefId);
        return trainingRunRepository.save(trainingRun);
    }

    @Override
    public TrainingRun assignSandbox(TrainingRun trainingRun) {
        try {
            Long sandboxInstanceRef = getReadySandboxInstanceRef(trainingRun.getTrainingInstance().getPoolId());
            trainingRun.setSandboxInstanceRefId(sandboxInstanceRef);
            auditEventsService.auditTrainingRunStartedAction(trainingRun);
            auditEventsService.auditLevelStartedAction(trainingRun);
        } catch (ForbiddenException | InternalServerErrorException ex) {
            trainingRunRepository.delete(trainingRun);
            throw ex;
        }
        return trainingRunRepository.save(trainingRun);
    }

    @Override
    public TrainingRun resumeTrainingRun(Long trainingRunId) {
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
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

    private TrainingRun getNewTrainingRun(AbstractLevel currentLevel, TrainingInstance trainingInstance,
                                          TRState state, LocalDateTime startTime, LocalDateTime endTime, Long participantRefId) {
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

    private Long getReadySandboxInstanceRef(Long poolId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String url = kypoOpenStackURI + "/pools/" + poolId + "/sandboxes/unlocked/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        try {
            ResponseEntity<SandboxInfo> response = pythonRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(httpHeaders), SandboxInfo.class);
            SandboxInfo sandboxInfoResult = Objects.requireNonNull(response.getBody(), "There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.");
            return sandboxInfoResult.getId();
        } catch (NullPointerException ex) {
            throw new ForbiddenException(ex.getMessage());
        } catch (RestTemplateException ex) {
            if (ex.getStatusCode().equals(HttpStatus.CONFLICT.toString())) {
                throw new ForbiddenException("There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.");
            }
            throw new MicroserviceApiException("Error when calling Python API to get unlocked sandbox from pool (ID: " + poolId + ")", new PythonApiError(ex.getMessage()));
        }
    }

    @Override
    public boolean isCorrectFlag(Long runId, String flag) {
        Assert.notNull(runId, MUST_NOT_BE_NULL);
        Assert.hasLength(flag, "Submitted flag must not be nul nor empty.");
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

    @Override
    public int getRemainingAttempts(Long trainingRunId) {
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
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

    @Override
    public String getSolution(Long trainingRunId) {
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
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

    @Override
    public Hint getHint(Long trainingRunId, Long hintId) {
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        Assert.notNull(hintId, "Input hint id must not be null.");
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

    @Override
    public int getMaxLevelOrder(Long definitionId) {
        return abstractLevelRepository.getCurrentMaxOrder(definitionId);
    }

    @Override
    public void finishTrainingRun(Long trainingRunId) {
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
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

    @Override
    public void evaluateResponsesToAssessment(Long trainingRunId, String responsesAsString) {
        Assert.notNull(responsesAsString, "Response to assessment must not be null.");
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