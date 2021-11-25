package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsTrainee;
import cz.muni.ics.kypo.training.annotations.security.IsTraineeOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.LevelReferenceSolutionDTO;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.responses.VariantAnswer;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.service.*;
import cz.muni.ics.kypo.training.service.api.AnswersStorageApiService;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Training run facade.
 */
@Service
public class TrainingRunFacade {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunFacade.class);
    private static final int TIME_TO_PROPAGATE_EVENTS = 5;

    private final TrainingRunService trainingRunService;
    private final TrainingDefinitionService trainingDefinitionService;
    private final AnswersStorageApiService answersStorageApiService;
    private final SecurityService securityService;
    private final UserService userService;
    private final TrainingFeedbackApiService trainingFeedbackApiService;
    private final TrainingRunMapper trainingRunMapper;
    private final LevelMapper levelMapper;
    private final HintMapper hintMapper;


    /**
     * Instantiates a new Training run facade.
     *
     * @param trainingRunService the training run service
     * @param securityService    the security service
     * @param userService        the user service
     * @param trainingRunMapper  the training run mapper
     * @param levelMapper        the level mapper
     * @param hintMapper         the hint mapper
     */
    @Autowired
    public TrainingRunFacade(TrainingRunService trainingRunService,
                             TrainingDefinitionService trainingDefinitionService,
                             AnswersStorageApiService answersStorageApiService,
                             SecurityService securityService,
                             UserService userService,
                             TrainingFeedbackApiService trainingFeedbackApiService,
                             TrainingRunMapper trainingRunMapper,
                             LevelMapper levelMapper,
                             HintMapper hintMapper) {
        this.trainingRunService = trainingRunService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.answersStorageApiService = answersStorageApiService;
        this.securityService = securityService;
        this.userService = userService;
        this.trainingRunMapper = trainingRunMapper;
        this.trainingFeedbackApiService = trainingFeedbackApiService;
        this.levelMapper = levelMapper;
        this.hintMapper = hintMapper;
    }

    /**
     * Finds specific Training Run by id
     *
     * @param id of a Training Run that would be returned
     * @return specific {@link TrainingRunByIdDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#id)")
    @TransactionalRO
    public TrainingRunByIdDTO findById(Long id) {
        TrainingRun trainingRun = trainingRunService.findById(id);
        TrainingRunByIdDTO trainingRunByIdDTO = trainingRunMapper.mapToFindByIdDTO(trainingRun);
        trainingRunByIdDTO.setDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
        trainingRunByIdDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
        trainingRunByIdDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunByIdDTO.getParticipantRef().getUserRefId()));
        return trainingRunByIdDTO;
    }

    /**
     * Find all Training Runs.
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingRunDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable) {
        PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRunService.findAll(predicate, pageable));
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    /**
     * Delete selected training runs.
     *
     * @param trainingRunIds training runs to delete
     * @param forceDelete    indicates if this training run should be force deleted.
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public void deleteTrainingRuns(List<Long> trainingRunIds, boolean forceDelete) {
        if(trainingRunIds.isEmpty()) {
            return;
        }
        TrainingInstance trainingInstance = null;
        for (Long trainingRunId : trainingRunIds) {
            trainingInstance = trainingRunService.deleteTrainingRun(trainingRunId, forceDelete).getTrainingInstance();
            trainingFeedbackApiService.deleteTraineeGraph(trainingRunId);
        }
        trainingFeedbackApiService.deleteSummaryGraph(trainingInstance.getId());
        trainingFeedbackApiService.createSummaryGraph(trainingInstance.getTrainingDefinition().getId(), trainingInstance.getId());
    }

    /**
     * Delete selected training run.
     *
     * @param trainingRunId training run to delete
     * @param forceDelete   indicates if this training run should be force deleted.
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public void deleteTrainingRun(Long trainingRunId, boolean forceDelete) {
        TrainingRun deletedTrainingRun = trainingRunService.deleteTrainingRun(trainingRunId, forceDelete);
        trainingFeedbackApiService.deleteTraineeGraph(trainingRunId);
        TrainingInstance trainingInstance = deletedTrainingRun.getTrainingInstance();
        TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
        trainingFeedbackApiService.deleteSummaryGraph(trainingInstance.getId());
        trainingFeedbackApiService.createSummaryGraph(trainingDefinition.getId(), trainingInstance.getId());
    }

    /**
     * Finds all Training Runs of logged in user.
     *
     * @param pageable    pageable parameter with information about pagination.
     * @param sortByTitle optional parameter. "asc" for ascending sort, "desc" for descending and null if sort is not wanted
     * @return Page of all {@link AccessedTrainingRunDTO} of logged in user.
     */
    @IsTraineeOrAdmin
    @TransactionalRO
    public PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable, String sortByTitle) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByParticipantRefUserRefId(pageable);
        return convertToAccessedRunDTO(trainingRuns, sortByTitle);
    }

    /**
     * Resume given training run.
     *
     * @param trainingRunId id of Training Run to be resumed.
     * @return {@link AccessTrainingRunDTO} response
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public AccessTrainingRunDTO resumeTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
        AccessTrainingRunDTO accessTrainingRunDTO = convertToAccessTrainingRunDTO(trainingRun);
        if (trainingRun.getCurrentLevel() instanceof TrainingLevel) {
            if (trainingRun.isSolutionTaken()) {
                accessTrainingRunDTO.setTakenSolution(((TrainingLevel) trainingRun.getCurrentLevel()).getSolution());
            }
            trainingRun.getHintInfoList().forEach(hintInfo -> {
                        if (hintInfo.getTrainingLevelId().equals(trainingRun.getCurrentLevel().getId())) {
                            accessTrainingRunDTO.getTakenHints().add(hintMapper.mapToDTO(hintInfo));
                        }
                    }
            );
        }
        return accessTrainingRunDTO;
    }

    /**
     * Access Training Run by logged in user based on given accessToken.
     *
     * @param accessToken of one training instance
     * @return {@link AccessTrainingRunDTO} response
     */
    @IsTraineeOrAdmin
    @Transactional
    public AccessTrainingRunDTO accessTrainingRun(String accessToken) {
        TrainingInstance trainingInstance = trainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
        // checking if the user is not accessing to his existing training run (resume action)
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        Optional<TrainingRun> accessedTrainingRun = trainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId);
        if (accessedTrainingRun.isPresent()) {
            TrainingRun trainingRun = trainingRunService.resumeTrainingRun(accessedTrainingRun.get().getId());
            return convertToAccessTrainingRunDTO(trainingRun);
        }
        // Check if the user already clicked access training run, in that case, it returns an exception (it prevents concurrent accesses).
        trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId(), accessToken);
        try {
            // During this action we create a new TrainingRun and lock and get sandbox from OpenStack Sandbox API
            TrainingRun trainingRun = trainingRunService.createTrainingRun(trainingInstance, participantRefId);
            trainingRunService.assignSandbox(trainingRun, trainingInstance.getPoolId());
            return convertToAccessTrainingRunDTO(trainingRun);
        } catch (Exception e) {
            // delete/rollback acquisition lock when no training run either sandbox is assigned
            trainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId());
            throw e;
        }
    }

    private AccessTrainingRunDTO convertToAccessTrainingRunDTO(TrainingRun trainingRun) {
        AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
        accessTrainingRunDTO.setTrainingRunID(trainingRun.getId());
        accessTrainingRunDTO.setAbstractLevelDTO(getCorrectAbstractLevelDTO(trainingRun.getCurrentLevel()));
        accessTrainingRunDTO.setShowStepperBar(trainingRun.getTrainingInstance().getTrainingDefinition().isShowStepperBar());
        accessTrainingRunDTO.setInfoAboutLevels(getInfoAboutLevels(trainingRun.getCurrentLevel().getTrainingDefinition().getId()));
        accessTrainingRunDTO.setSandboxInstanceRefId(trainingRun.getSandboxInstanceRefId());
        accessTrainingRunDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
        accessTrainingRunDTO.setStartTime(trainingRun.getStartTime());
        return accessTrainingRunDTO;
    }

    private List<BasicLevelInfoDTO> getInfoAboutLevels(Long definitionId) {
        List<BasicLevelInfoDTO> infoAboutLevels = new ArrayList<>();
        List<AbstractLevel> levels = trainingRunService.getLevels(definitionId);
        for (AbstractLevel abstractLevel : levels) {
            if (abstractLevel instanceof AssessmentLevel) {
                infoAboutLevels.add(new BasicLevelInfoDTO(abstractLevel.getId(), abstractLevel.getTitle(), LevelType.ASSESSMENT_LEVEL, abstractLevel.getOrder()));
            } else if (abstractLevel instanceof TrainingLevel) {
                infoAboutLevels.add(new BasicLevelInfoDTO(abstractLevel.getId(), abstractLevel.getTitle(), LevelType.TRAINING_LEVEL, abstractLevel.getOrder()));
            } else {
                infoAboutLevels.add(new BasicLevelInfoDTO(abstractLevel.getId(), abstractLevel.getTitle(), LevelType.INFO_LEVEL, abstractLevel.getOrder()));
            }
        }
        return infoAboutLevels;
    }

    /**
     * Finds all Training Runs by specific Training Definition and logged in user.
     *
     * @param trainingDefinitionId id of Training Definition
     * @param pageable             pageable parameter with information about pagination.
     * @return Page of all {@link AccessedTrainingRunDTO} of logged in user and given definition.
     */
    @IsTrainee
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinitionId, pageable);
        PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRuns);
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    /**
     * Finds all Training Runs of specific training definition.
     *
     * @param trainingDefinitionId id of Training Definition whose Training Runs would be returned.
     * @param pageable             pageable parameter with information about pagination.
     * @return Page of all {@link AccessedTrainingRunDTO} of given definition.
     */
    @IsTrainee
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinition(trainingDefinitionId, pageable);
        PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRuns);
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    /**
     * Gets next level of given Training Run and set new current level.
     *
     * @param trainingRunId id of Training Run whose next level should be returned.
     * @return {@link AbstractLevelDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public AbstractLevelDTO getNextLevel(Long trainingRunId) {
        AbstractLevel abstractLevel;
        abstractLevel = trainingRunService.getNextLevel(trainingRunId);
        return getCorrectAbstractLevelDTO(abstractLevel);
    }

    /**
     * Gets solution of current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets solution for.
     * @return solution of current level.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public String getSolution(Long trainingRunId) {
        return trainingRunService.getSolution(trainingRunId);
    }

    /**
     * Gets hint of given current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @param hintId        id of hint to be returned.
     * @return {@link HintDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public HintDTO getHint(Long trainingRunId, Long hintId) {
        return hintMapper.mapToDTO(trainingRunService.getHint(trainingRunId, hintId));
    }

    /**
     * Check given answer of given Training Run.
     *
     * @param trainingRunId id of Training Run to check answer.
     * @param answer          string which player submit.
     * @return true if answer is correct, false if answer is wrong.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public IsCorrectAnswerDTO isCorrectAnswer(Long trainingRunId, String answer) {
        IsCorrectAnswerDTO correctAnswerDTO = new IsCorrectAnswerDTO();
        correctAnswerDTO.setCorrect(trainingRunService.isCorrectAnswer(trainingRunId, answer));
        correctAnswerDTO.setRemainingAttempts(trainingRunService.getRemainingAttempts(trainingRunId));
        if (correctAnswerDTO.getRemainingAttempts() == 0) {
            correctAnswerDTO.setSolution(getSolution(trainingRunId));
        }
        return correctAnswerDTO;
    }

    /**
     * Finish training run.
     *
     * @param trainingRunId id of Training Run to be finished.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public void finishTrainingRun(Long trainingRunId) {
        TrainingRun finishedTrainingRun = trainingRunService.finishTrainingRun(trainingRunId);
        waitToPropagateEvents();
        createTraineeGraphAndUpdateSummaryGraph(finishedTrainingRun);
    }

    private void createTraineeGraphAndUpdateSummaryGraph(TrainingRun run) {
        TrainingInstance instance = run.getTrainingInstance();
        TrainingDefinition definition = instance.getTrainingDefinition();
        AtomicBoolean isAnyReferenceSolution = new AtomicBoolean(false);
        List<LevelReferenceSolutionDTO> referenceSolution = this.trainingDefinitionService
                .getAllTrainingLevels(definition.getId())
                .stream()
                .peek(l -> isAnyReferenceSolution.set(!l.getReferenceSolution().isEmpty()))
                .map(l -> new LevelReferenceSolutionDTO(l.getId(), l.getOrder(), new ArrayList<>(ReferenceSolutionNodeMapper.INSTANCE.mapToSetDTO(l.getReferenceSolution()))))
                .collect(Collectors.toList());
        if(isAnyReferenceSolution.get()) {
            this.trainingFeedbackApiService.createTraineeGraph(definition.getId(), instance.getId(), run.getId(), referenceSolution);
            this.trainingFeedbackApiService.deleteSummaryGraph(instance.getId());
            this.trainingFeedbackApiService.createSummaryGraph(definition.getId(), instance.getId());
        }
    }

    /**
     * Archive training run.
     *
     * @param trainingRunId id of Training Run to be archived.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public void archiveTrainingRun(Long trainingRunId) {
        trainingRunService.archiveTrainingRun(trainingRunId);
    }

    /**
     * Evaluate and store responses to assessment.
     *
     * @param trainingRunId     id of Training Run to be finish.
     * @param responsesToQuestions responses to assessment
     */
    @IsTrainee
    @TransactionalWO
    public void evaluateResponsesToAssessment(Long trainingRunId, List<QuestionAnswerDTO> responsesToQuestions) {
        trainingRunService.evaluateResponsesToAssessment(trainingRunId, responsesToQuestions.stream()
                .collect(Collectors.toMap(QuestionAnswerDTO::getQuestionId, Function.identity())));
    }

    /**
     * Retrieve info about the participant of the given training run.
     *
     * @param trainingRunId id of the training run whose participant you want to get.
     * @return returns participant of the given training run.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalRO
    public UserRefDTO getParticipant(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
        return userService.getUserRefDTOByUserRefId(trainingRun.getParticipantRef().getUserRefId());
    }

    /**
     * Gets correct answers of the given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @return {@link CorrectAnswerDTO[]}
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public List<CorrectAnswerDTO> getCorrectAnswers(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
        List<TrainingLevel> trainingLevels = trainingRunService.getLevels(trainingRun.getTrainingInstance().getTrainingDefinition().getId()).stream()
                .filter(abstractLevel -> abstractLevel.getClass() == TrainingLevel.class)
                .map(abstractLevel -> (TrainingLevel) abstractLevel)
                .collect(Collectors.toList());
        boolean isVariantSandboxes = trainingLevels.stream().anyMatch(TrainingLevel::isVariantAnswers);
        Map<String, String> variantAnswers = isVariantSandboxes ? getVariantAnswers(trainingRun.getSandboxInstanceRefId()) : new HashMap<>();
        return trainingLevels.stream()
                .map(trainingLevel -> mapToCorrectAnswerDTO(trainingLevel, variantAnswers))
                .collect(Collectors.toList());
    }

    private Map<String, String> getVariantAnswers(Long sandboxId) {
        return answersStorageApiService.getAnswersBySandboxId(sandboxId)
                .getVariantAnswers().stream()
                .collect(Collectors.toMap(VariantAnswer::getAnswerVariableName, VariantAnswer::getAnswerContent));
    }

    private CorrectAnswerDTO mapToCorrectAnswerDTO(TrainingLevel trainingLevel, Map<String, String> variantAnswers) {
        CorrectAnswerDTO correctAnswerDTO = new CorrectAnswerDTO();
        correctAnswerDTO.setLevelId(trainingLevel.getId());
        correctAnswerDTO.setLevelTitle(trainingLevel.getTitle());
        correctAnswerDTO.setLevelOrder(trainingLevel.getOrder());
        if(trainingLevel.isVariantAnswers()) {
            correctAnswerDTO.setCorrectAnswer(variantAnswers.get(trainingLevel.getAnswerVariableName()) );
            correctAnswerDTO.setVariableName(trainingLevel.getAnswerVariableName());
        } else {
            correctAnswerDTO.setCorrectAnswer(trainingLevel.getAnswer());
        }
        return correctAnswerDTO;
    }

    private void addParticipantsToTrainingRunDTOs(List<TrainingRunDTO> trainingRunDTOS) {
        trainingRunDTOS.forEach(trainingRunDTO ->
                trainingRunDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunDTO.getParticipantRef().getUserRefId())));
    }

    private PageResultResource<AccessedTrainingRunDTO> convertToAccessedRunDTO(Page<TrainingRun> trainingRuns, String sortByTitle) {
        List<AccessedTrainingRunDTO> accessedTrainingRunDTOS = new ArrayList<>();
        for (TrainingRun trainingRun : trainingRuns) {
            AccessedTrainingRunDTO accessedTrainingRunDTO = generateAccessedTrainingRunDTO(trainingRun);
            accessedTrainingRunDTOS.add(accessedTrainingRunDTO);
        }
        return new PageResultResource<>(sortByTitle(accessedTrainingRunDTOS, sortByTitle), trainingRunMapper.createPagination(trainingRuns));
    }

    private List<AccessedTrainingRunDTO> sortByTitle(List<AccessedTrainingRunDTO> runs, String sortByTitle) {
        if (sortByTitle != null && !sortByTitle.isBlank()) {
            if (!runs.isEmpty()) {
                if (sortByTitle.equals("asc")) {
                    runs.sort(Comparator.comparing(AccessedTrainingRunDTO::getTitle));
                } else if (sortByTitle.equals("desc")) {
                    runs.sort(Comparator.comparing(AccessedTrainingRunDTO::getTitle).reversed());
                }
            }
        }
        return runs;
    }

    private AccessedTrainingRunDTO generateAccessedTrainingRunDTO(TrainingRun trainingRun) {
        AccessedTrainingRunDTO accessedTrainingRunDTO = new AccessedTrainingRunDTO();
        accessedTrainingRunDTO.setId(trainingRun.getId());
        accessedTrainingRunDTO.setTitle(trainingRun.getTrainingInstance().getTitle());
        accessedTrainingRunDTO.setTrainingInstanceStartDate(trainingRun.getTrainingInstance().getStartTime());
        accessedTrainingRunDTO.setTrainingInstanceEndDate(trainingRun.getTrainingInstance().getEndTime());
        accessedTrainingRunDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
        accessedTrainingRunDTO.setNumberOfLevels(trainingRunService.getMaxLevelOrder(trainingRun.getTrainingInstance().getTrainingDefinition().getId()) + 1);
        accessedTrainingRunDTO.setCurrentLevelOrder(trainingRun.getCurrentLevel().getOrder() + 1);
        accessedTrainingRunDTO.setPossibleAction(resolvePossibleActions(accessedTrainingRunDTO, trainingRun.getState()));
        return accessedTrainingRunDTO;
    }

    private Actions resolvePossibleActions(AccessedTrainingRunDTO trainingRunDTO, TRState trainingRunState) {
        boolean isTrainingInstanceRunning = LocalDateTime.now(Clock.systemUTC()).isBefore(trainingRunDTO.getTrainingInstanceEndDate());
        if (trainingRunState == TRState.FINISHED || !isTrainingInstanceRunning) {
            return Actions.RESULTS;
        } else {
            return Actions.RESUME;
        }
    }

    private AbstractLevelDTO getCorrectAbstractLevelDTO(AbstractLevel abstractLevel) {
        AbstractLevelDTO abstractLevelDTO;
        if (abstractLevel instanceof AssessmentLevel) {
            AssessmentLevel assessmentLevel = (AssessmentLevel) abstractLevel;
            abstractLevelDTO = levelMapper.mapToAssessmentLevelDTO(assessmentLevel);
            abstractLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
            deleteInfoAboutCorrectnessFromQuestions((AssessmentLevelDTO) abstractLevelDTO);
        } else if (abstractLevel instanceof TrainingLevel) {
            TrainingLevel trainingLevel = (TrainingLevel) abstractLevel;
            abstractLevelDTO = levelMapper.mapToViewDTO(trainingLevel);
            abstractLevelDTO.setLevelType(LevelType.TRAINING_LEVEL);
        } else {
            InfoLevel infoLevel = (InfoLevel) abstractLevel;
            abstractLevelDTO = levelMapper.mapToInfoLevelDTO(infoLevel);
            abstractLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        }
        return abstractLevelDTO;
    }

    private void deleteInfoAboutCorrectnessFromQuestions(AssessmentLevelDTO assessmentLevelDTO) {
        assessmentLevelDTO.getQuestions().forEach(questionDTO -> {
            questionDTO.getChoices().forEach(questionChoiceDTO -> questionChoiceDTO.setCorrect(null));
            questionDTO.getExtendedMatchingStatements().forEach(statementDTO -> statementDTO.setCorrectOptionOrder(null));
        });
    }
    private void waitToPropagateEvents() {
        try {
            TimeUnit.SECONDS.sleep(TIME_TO_PROPAGATE_EVENTS);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}
