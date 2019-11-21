package cz.muni.ics.kypo.training.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.aop.TrackTime;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsTraineeOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.responses.PageResultResourcePython;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.enums.SandboxStates;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.RestTemplateException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Pavel Seda
 * @author Dominik Pilar
 */
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
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
    public TrainingRun findById(Long runId) {
        Objects.requireNonNull(runId);
        return trainingRunRepository.findById(runId)
                .orElseThrow(() -> new ServiceLayerException("Training Run with runId: " + runId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
    public TrainingRun findByIdWithLevel(Long runId) {
        Objects.requireNonNull(runId);
        return trainingRunRepository.findByIdWithLevel(runId).orElseThrow(() ->
                new ServiceLayerException("Training Run with id: " + runId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
        return trainingRunRepository.findAll(predicate, pageable);
    }

    @Override
    @IsOrganizerOrAdmin
    public void deleteTrainingRuns(List<Long> trainingRunIds) {
        trainingRunIds.forEach(this::deleteTrainingRun);
    }

    @Override
    @IsOrganizerOrAdmin
    public void deleteTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunRepository.findById(trainingRunId).orElseThrow(() -> new ServiceLayerException("Training Run with runId: " + trainingRunId + " could not be deleted because it is not in the database.", ErrorCode.RESOURCE_NOT_FOUND));
        if (trainingRun.getSandboxInstanceRefId() == null) {
            try {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
                ResponseEntity<SandboxInfo> response = pythonRestTemplate.exchange(kypoOpenStackURI + "/sandboxes/" + trainingRun.getPreviousSandboxInstanceRefId() + "/", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                        new ParameterizedTypeReference<SandboxInfo>() {
                        });
                if (response.getStatusCode().is2xxSuccessful()) {
                    throw new ServiceLayerException("Sandbox (id:" + trainingRun.getPreviousSandboxInstanceRefId() + ") previously assigned to the training run (id: " + trainingRunId + ") was not deleted in OpenStack. Please delete sandbox in OpenStack before you delete training run.", ErrorCode.RESOURCE_CONFLICT);
                }
            } catch (RestTemplateException ex) {
                if (!ex.getStatusCode().equals(HttpStatus.NOT_FOUND.toString())) {
                    throw new ServiceLayerException("Error when calling Python API to obtain info about sandbox (ID: " + trainingRun.getPreviousSandboxInstanceRefId() + "): " + ex.getStatusCode() + " - " + ex.getMessage() + ".", ErrorCode.PYTHON_API_ERROR);
                }
                LOG.debug("Sandbox (ID:" + trainingRun.getPreviousSandboxInstanceRefId() + ") previously assigned to the training run (ID: " + trainingRunId + ") is not found in OpenStack because it was successfully deleted.");
            }
            trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
            trainingRunRepository.delete(trainingRun);
        } else {
            throw new ServiceLayerException("Could not delete training run (id: " + trainingRunId + ") with associated sandbox (id: " + trainingRun.getSandboxInstanceRefId() +
                    "). Please firstly, delete associated sandbox.", ErrorCode.RESOURCE_CONFLICT);
        }
    }

    @Override
    @IsTraineeOrAdmin
    public Page<TrainingRun> findAllByParticipantRefUserRefId(Pageable pageable) {
        return trainingRunRepository.findAllByParticipantRefId(securityService.getUserRefIdFromUserAndGroup(), pageable);
    }

    private TrainingRun create(TrainingRun trainingRun) {
        Assert.notNull(trainingRun, "Input training run must not be empty.");
        return trainingRunRepository.save(trainingRun);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
    public AbstractLevel getNextLevel(Long runId) {
        Assert.notNull(runId, MUST_NOT_BE_NULL);
        TrainingRun trainingRun = findByIdWithLevel(runId);
        int currentLevelOrder = trainingRun.getCurrentLevel().getOrder();
        int maxLevelOrder = abstractLevelRepository.getCurrentMaxOrder(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        if (!trainingRun.isLevelAnswered()) {
            throw new ServiceLayerException("At first you need to answer the level.", ErrorCode.RESOURCE_CONFLICT);
        }
        if (currentLevelOrder == maxLevelOrder) {
            throw new ServiceLayerException("There is no next level.", ErrorCode.NO_NEXT_LEVEL);
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
        //audit this action to theElasticSearch
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
    @IsTraineeOrAdmin
    public List<AbstractLevel> getLevels(Long definitionId) {
        Assert.notNull(definitionId, "Id of training definition must not be null.");
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    }


    @Override
    @IsTraineeOrAdmin
    @TransactionalWO
    @TrackTime
    public TrainingRun accessTrainingRun(String accessToken) {
        Assert.hasLength(accessToken, "AccessToken cannot be null or empty.");
        TrainingInstance trainingInstance = trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
                .orElseThrow(() -> new ServiceLayerException("There is no active game session matching your keyword: " + accessToken + ".", ErrorCode.RESOURCE_NOT_FOUND));
        if (trainingInstance.getPoolId() == null) {
            throw new ServiceLayerException("At first organizer must allocate sandboxes for training instance.", ErrorCode.RESOURCE_CONFLICT);
        }
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        Optional<TrainingRun> accessedTrainingRun = trainingRunRepository.findValidTrainingRunOfUser(trainingInstance.getAccessToken(), participantRefId);
        if (accessedTrainingRun.isPresent()) {
            return resumeTrainingRun(accessedTrainingRun.get().getId());
        }
        try {
            trAcquisitionLockRepository.save(new TRAcquisitionLock(participantRefId, trainingInstance.getId(), LocalDateTime.now()));
        } catch (DataIntegrityViolationException ex) {
            throw new ServiceLayerException("Training run has been already accessed and cannot be created again. Please resume Training Run", ErrorCode.TR_ACQUIRED_LOCK);
        }

        List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
        if (levels.isEmpty()) {
            throw new ServiceLayerException("No starting level available for this training definition.", ErrorCode.RESOURCE_NOT_FOUND);
        }
        levels.sort(Comparator.comparing(AbstractLevel::getOrder));
        TrainingRun trainingRun = getNewTrainingRun(levels.get(0), trainingInstance, TRState.RUNNING,
                LocalDateTime.now(Clock.systemUTC()), trainingInstance.getEndTime(), participantRefId);
        // audit this action to the Elasticsearch
        return trainingRunRepository.save(trainingRun);
    }

    @Override
    @IsTraineeOrAdmin
    @TransactionalWO
    public TrainingRun assignSandbox(TrainingRun trainingRun){
        Long sandboxInstanceRef = getReadySandboxInstanceRef(trainingRun.getTrainingInstance().getPoolId());
        try {
            trainingRun.setSandboxInstanceRefId(sandboxInstanceRef);
            auditEventsService.auditTrainingRunStartedAction(trainingRun);
            auditEventsService.auditLevelStartedAction(trainingRun);
        }catch(ServiceLayerException ex){
            trainingRunRepository.delete(trainingRun);
            throw ex;
        }
        return trainingRunRepository.save(trainingRun);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TrackTime
    public TrainingRun resumeTrainingRun(Long trainingRunId) {
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        if (trainingRun.getState().equals(TRState.FINISHED) || trainingRun.getState().equals(TRState.ARCHIVED)) {
            throw new ServiceLayerException("Cannot resume finished training run.", ErrorCode.RESOURCE_CONFLICT);
        }
        if (trainingRun.getTrainingInstance().getEndTime().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
            throw new ServiceLayerException("Cannot resume training run after end of training instance.", ErrorCode.RESOURCE_CONFLICT);
        }
        if (trainingRun.getSandboxInstanceRefId() == null) {
            throw new ServiceLayerException("Sandbox of this training run was already deleted, you have to start new game.", ErrorCode.RESOURCE_CONFLICT);
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
        List<Long> readySandboxesIds = new ArrayList<>();
        while(true) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            String url = kypoOpenStackURI + "/pools/" + poolId + "/sandboxes/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            builder.queryParam("page", 1);
            builder.queryParam("page_size", PYTHON_RESULT_PAGE_SIZE);
            ResponseEntity<PageResultResourcePython<SandboxInfo>> response = pythonRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<PageResultResourcePython<SandboxInfo>>() {
                    });
            PageResultResourcePython<SandboxInfo> sandboxInfoPageResult = Objects.requireNonNull(response.getBody());
            List<SandboxInfo> sandboxInfoList = Objects.requireNonNull(sandboxInfoPageResult.getResults());

            readySandboxesIds.clear();
            sandboxInfoList.forEach(sandboxInfo -> {
                if (sandboxInfo.getStatus().contains(SandboxStates.FULL_BUILD_COMPLETE.getName()) && !sandboxInfo.isLocked()) {
                    readySandboxesIds.add(sandboxInfo.getId());
                }
            });
            if (readySandboxesIds.isEmpty()) {
                throw new ServiceLayerException("There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.", ErrorCode.NO_AVAILABLE_SANDBOX);
            } else {
                Long readySandboxId = readySandboxesIds.get(0);
                //TODO why they need empty body ??
                String requestJson = "{}";
                try{
                    httpHeaders = new HttpHeaders();
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    pythonRestTemplate.exchange(kypoOpenStackURI + "/sandboxes/" + readySandboxId + "/lock/",
                            HttpMethod.POST, new HttpEntity<>(requestJson, httpHeaders), String.class);
                } catch (RestTemplateException ex){
                    LOG.error("Error when calling Python API to obtain lock status of sandbox (ID: {}): {}.", readySandboxId, ex.getStatusCode() + ex.getMessage());
                    //TODO do not continue when get error, you are trying to lock sandbox when you expect it is not locked.
                    continue;
                }
                return readySandboxId;
            }
        }
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
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
            throw new ServiceLayerException("Current level is not game level and does not have flag.", ErrorCode.WRONG_LEVEL_TYPE);
        }
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
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
        throw new ServiceLayerException("Current level is not game level and does not have flag.", ErrorCode.WRONG_LEVEL_TYPE);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
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
            auditEventsService.auditSolutionDisplayedAction(trainingRun, (GameLevel) level);
            return ((GameLevel) level).getSolution();
        } else {
            throw new ServiceLayerException("Current level is not game level and does not have solution.", ErrorCode.WRONG_LEVEL_TYPE);
        }
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public Hint getHint(Long trainingRunId, Long hintId) {
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        Assert.notNull(hintId, "Input hint id must not be null.");
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof GameLevel) {
            Hint hint = hintRepository.findById(hintId).orElseThrow(() -> new ServiceLayerException("Hint with id " + hintId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
            if (hint.getGameLevel().getId().equals(level.getId())) {
                trainingRun.increaseCurrentPenalty(hint.getHintPenalty());
                trainingRun.addHintInfo(new HintInfo(level.getId(), hint.getId(), hint.getTitle(), hint.getContent(), hint.getOrder()));
                auditEventsService.auditHintTakenAction(trainingRun, hint);
                return hint;
            }
            throw new ServiceLayerException("Hint with id " + hintId + " is not in current level of training run: " + trainingRunId + ".", ErrorCode.RESOURCE_CONFLICT);
        } else {
            throw new ServiceLayerException("Current level is not game level and does not have hints.", ErrorCode.WRONG_LEVEL_TYPE);
        }
    }

    @Override
    @IsTraineeOrAdmin
    public int getMaxLevelOrder(Long definitionId) {
        return abstractLevelRepository.getCurrentMaxOrder(definitionId);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    public void finishTrainingRun(Long trainingRunId) {
        Assert.notNull(trainingRunId, MUST_NOT_BE_NULL);
        TrainingRun trainingRun = findById(trainingRunId);
        int maxOrder = abstractLevelRepository.getCurrentMaxOrder(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        if (trainingRun.getCurrentLevel().getOrder() != maxOrder) {
            throw new ServiceLayerException("Cannot finish training run because current level is not last.", ErrorCode.RESOURCE_CONFLICT);
        }
        if (!trainingRun.isLevelAnswered()) {
            throw new ServiceLayerException("Cannot finish training run because current level is not answered.", ErrorCode.RESOURCE_CONFLICT);
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
            throw new ServiceLayerException(ex.getMessage(), ErrorCode.UNEXPECTED_ERROR);
        }
    }
}