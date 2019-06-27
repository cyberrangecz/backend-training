package cz.muni.ics.kypo.training.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.aop.TrackTime;
import cz.muni.ics.kypo.training.annotations.security.IsTraineeOrAdmin;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.*;
import java.util.*;

/**
 * @author Dominik Pilar (445537) & Pavel Seda (441048)
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
    private RestTemplate restTemplate;
    private SecurityService securityService;

    @Autowired
    public TrainingRunServiceImpl(TrainingRunRepository trainingRunRepository, AbstractLevelRepository abstractLevelRepository,
                                  TrainingInstanceRepository trainingInstanceRepository, UserRefRepository participantRefRepository,
                                  HintRepository hintRepository, AuditEventsService auditEventsService, RestTemplate restTemplate,
                                  SecurityService securityService) {
        this.trainingRunRepository = trainingRunRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.participantRefRepository = participantRefRepository;
        this.hintRepository = hintRepository;
        this.auditEventsService = auditEventsService;
        this.restTemplate = restTemplate;
        this.securityService = securityService;
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
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
        return trainingRunRepository.findAll(predicate, pageable);
    }

    @Override
    @IsTraineeOrAdmin
    public Page<TrainingRun> findAllByParticipantRefLogin(Pageable pageable) {
        String login = securityService.getSubOfLoggedInUser();
        return trainingRunRepository.findAllByParticipantRefLogin(login, pageable);
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
        trainingRunRepository.save(trainingRun);
        //audit this action to theElasticSearch
        auditEventsService.auditLevelStartedAction(trainingRun);

        return abstractLevel;
    }

    @Override
    public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long definitionId, Pageable pageable) {
        Assert.notNull(definitionId, "Input training definition id must not be null.");
        return trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantRefLogin(definitionId, securityService.getSubOfLoggedInUser(), pageable);
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
    @TrackTime
    public TrainingRun accessTrainingRun(String accessToken) {
        Assert.hasLength(accessToken, "AccessToken cannot be null or empty.");
        Optional<TrainingRun> accessedTrainingRun = trainingRunRepository.findValidTrainingRunOfUser(accessToken, securityService.getSubOfLoggedInUser());
        if (accessedTrainingRun.isPresent()) {
            return resumeTrainingRun(accessedTrainingRun.get().getId());
        }
        TrainingInstance trainingInstance = trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
            .orElseThrow(() ->  new ServiceLayerException("There is no training instance with accessToken " + accessToken + ".", ErrorCode.RESOURCE_NOT_FOUND));
        if (trainingInstance.getPoolId() == null) {
            throw new ServiceLayerException("At first organizer must allocate sandboxes for training instance.", ErrorCode.RESOURCE_CONFLICT);
        }
        Optional<TrainingRun> tr = trainingRunRepository.findById(1L);
        Set<SandboxInstanceRef> freeSandboxes = trainingRunRepository.findFreeSandboxesOfTrainingInstance(trainingInstance.getId());
        if (!freeSandboxes.isEmpty()) {
            SandboxInstanceRef sandboxInstanceRef = getReadySandboxInstanceRef(freeSandboxes, trainingInstance.getPoolId());
            List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance.getTrainingDefinition().getId());
            if (levels.isEmpty()) throw new ServiceLayerException("No starting level available for this training definition.", ErrorCode.RESOURCE_NOT_FOUND);
            Collections.sort(levels, Comparator.comparing(AbstractLevel::getOrder));

            TrainingRun trainingRun = getNewTrainingRun(levels.get(0), securityService.getSubOfLoggedInUser(), trainingInstance,
                TRState.RUNNING, LocalDateTime.now(Clock.systemUTC()), trainingInstance.getEndTime(), sandboxInstanceRef);
            trainingRun = create(trainingRun);
            // audit this action to the Elasticsearch
            auditEventsService.auditTrainingRunStartedAction(trainingRun);
            auditEventsService.auditLevelStartedAction(trainingRun);
            return trainingRun;
        } else {
            throw new ServiceLayerException("There is no available sandbox, wait a minute and try again.", ErrorCode.NO_AVAILABLE_SANDBOX);
        }
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
        if (trainingRun.getSandboxInstanceRef() == null) {
            throw new ServiceLayerException("Sandbox of this training run was already deleted, you have to start new game.", ErrorCode.RESOURCE_CONFLICT);
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        try {
            ResponseEntity<SandboxInfo> response = restTemplate.exchange(kypoOpenStackURI + "/sandboxes/" + trainingRun.getSandboxInstanceRef().getSandboxInstanceRef() + "/", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<SandboxInfo>() {
                    });
            if (!Objects.requireNonNull(response.getBody()).getStatus().equals("CREATE_COMPLETE")) {
                throw new ServiceLayerException("Something happened with sandbox. Please contact organizer of training instance or administrator.", ErrorCode.RESOURCE_CONFLICT);
            }
        } catch (HttpClientErrorException ex) {
            throw new ServiceLayerException("Some error occurred during getting info about sandbox: " + ex.getStatusCode() + ". Please try later or contact administrator.", ErrorCode.UNEXPECTED_ERROR);
        }
        auditEventsService.auditTrainingRunResumedAction(trainingRun);
        return trainingRun;
    }

    private TrainingRun getNewTrainingRun(AbstractLevel currentLevel, String participantRefLogin, TrainingInstance trainingInstance,
                                          TRState state, LocalDateTime startTime, LocalDateTime endTime, SandboxInstanceRef sandboxInstanceRef) {
        TrainingRun newTrainingRun = new TrainingRun();
        newTrainingRun.setCurrentLevel(currentLevel);

        Optional<UserRef> userRef = participantRefRepository.findUserByUserRefLogin(participantRefLogin);
        if (userRef.isPresent()) {
            if(userRef.get().getUserRefGivenName() == null) {
                userRef.get().setUserRefGivenName(securityService.getGivenNameOfLoggedInUser());
                userRef.get().setUserRefFamilyName(securityService.getFamilyNameOfLoggedInUser());
            }
            newTrainingRun.setParticipantRef(userRef.get());
        } else {
            newTrainingRun.setParticipantRef(participantRefRepository.save(
                    new UserRef(participantRefLogin, securityService.getFullNameOfLoggedInUser(), securityService.getGivenNameOfLoggedInUser(), securityService.getFamilyNameOfLoggedInUser())
            ));
        }
        newTrainingRun.setAssessmentResponses("[]");
        newTrainingRun.setState(TRState.RUNNING);
        newTrainingRun.setTrainingInstance(trainingInstance);
        newTrainingRun.setStartTime(startTime);
        newTrainingRun.setEndTime(endTime);
        newTrainingRun.setSandboxInstanceRef(sandboxInstanceRef);
        return newTrainingRun;
    }

    private SandboxInstanceRef getReadySandboxInstanceRef(Set<SandboxInstanceRef> sandboxInstancePool, Long poolId) {
        List<Long> idsOfUnoccupiedSandboxes = new ArrayList<>();
        sandboxInstancePool.forEach(sandboxInstanceRef -> idsOfUnoccupiedSandboxes.add(sandboxInstanceRef.getSandboxInstanceRef()));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<List<SandboxInfo>> response = restTemplate.exchange(kypoOpenStackURI + "/pools/" + poolId + "/sandboxes/", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<List<SandboxInfo>>() {
                });
        if (response.getStatusCode().isError() || response.getBody() == null) {
            throw new ServiceLayerException("Some error occurred during getting info about sandboxes.", ErrorCode.UNEXPECTED_ERROR);
        }
        List<SandboxInfo> sandboxInfoList = response.getBody();
        sandboxInfoList.removeIf(sandboxInfo -> !sandboxInfo.getStatus().contains("COMPLETE") || !idsOfUnoccupiedSandboxes.contains(sandboxInfo.getId()));
        if (sandboxInfoList.isEmpty()) {
            throw new ServiceLayerException("There is no available sandbox, wait a minute and try again.", ErrorCode.NO_AVAILABLE_SANDBOX);
        } else {
            return sandboxInstancePool.stream().filter(sandboxInstanceRef -> sandboxInstanceRef.getSandboxInstanceRef().equals(sandboxInfoList.get(0).getId())).findFirst().get();
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
                trainingRun.decreaseTotalScore(trainingRun.getCurrentScore() - 1);
                trainingRun.setCurrentScore(1);
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
                trainingRun.decreaseCurrentScore(hint.getHintPenalty());
                trainingRun.decreaseTotalScore(hint.getHintPenalty());
                trainingRun.addHintInfo(new HintInfo(level.getId(), hint.getId(), hint.getTitle(), hint.getContent()));
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
        if(!trainingRun.isLevelAnswered()) {
            throw new ServiceLayerException("Cannot finish training run because current level is not answered.", ErrorCode.RESOURCE_CONFLICT);
        }

        trainingRun.setState(TRState.FINISHED);
        trainingRun.setEndTime(LocalDateTime.now(Clock.systemUTC()));
        if (trainingRun.getCurrentLevel() instanceof InfoLevel) {
            auditEventsService.auditLevelCompletedAction(trainingRun);
        }
        auditEventsService.auditTrainingRunEndedAction(trainingRun);
    }

    private TrainingRun findByIdWithLevel(Long trainingRunId) {
        Objects.requireNonNull(trainingRunId);
        return trainingRunRepository.findByIdWithLevel(trainingRunId).orElseThrow(() ->
                new ServiceLayerException("Training Run with id: " + trainingRunId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
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
            trainingRun.setCurrentScore(points);
            trainingRun.setTotalScore(trainingRun.getTotalScore() - (trainingRun.getCurrentLevel().getMaxScore() - trainingRun.getCurrentScore()));
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