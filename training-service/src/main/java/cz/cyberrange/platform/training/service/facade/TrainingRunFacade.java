package cz.cyberrange.platform.training.service.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.BasicLevelInfoDTO;
import cz.cyberrange.platform.training.api.dto.CorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.IsCorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.QuestionCorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelViewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.AssessmentLevelPreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessedTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunByIdDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.LevelReferenceSolutionDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelPreviewDTO;
import cz.cyberrange.platform.training.api.enums.Actions;
import cz.cyberrange.platform.training.api.enums.LevelType;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import cz.cyberrange.platform.training.api.responses.ActiveSandboxSummaryDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.api.responses.SandboxInfo;
import cz.cyberrange.platform.training.api.responses.VariantAnswer;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.SolutionInfo;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.service.annotations.security.IsOrganizerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.security.IsTrainee;
import cz.cyberrange.platform.training.service.annotations.security.IsTraineeOrAdmin;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ReferenceSolutionNodeMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingRunMapper;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.TrainingFeedbackApiService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Training run facade.
 */
@Service
public class TrainingRunFacade {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunFacade.class);
    private static final int TIME_TO_PROPAGATE_EVENTS = 5;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${central.syslog.ip:127.0.0.1}")
    private String centralSyslogIp;

    @Value("${single.sandbox.per.user.enabled:false}")
    private boolean singleSandboxPerUserEnabled;

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
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
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
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
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

        if (!securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
            for (Long trainingRunId : trainingRunIds) {
                if (!securityService.isOrganizerOfGivenTrainingRun(trainingRunId)) {
                    throw new SecurityException("Cannot delete training runs from different instance.");
                }
            }
        }

        TrainingInstance trainingInstance = null;
        for (Long trainingRunId : trainingRunIds) {
            trainingInstance = trainingRunService.deleteTrainingRun(trainingRunId, forceDelete, true).getTrainingInstance();
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
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
        "or @securityService.isOrganizerOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public void deleteTrainingRun(Long trainingRunId, boolean forceDelete) {
        TrainingRun deletedTrainingRun = trainingRunService.deleteTrainingRun(trainingRunId, forceDelete, true);
        trainingFeedbackApiService.deleteTraineeGraph(trainingRunId);
        TrainingInstance trainingInstance = deletedTrainingRun.getTrainingInstance();
        TrainingDefinition trainingDefinition = trainingInstance.getTrainingDefinition();
        trainingFeedbackApiService.deleteSummaryGraph(trainingInstance.getId());
        trainingFeedbackApiService.createSummaryGraph(trainingDefinition.getId(), trainingInstance.getId());
    }

    /**
     * Finds all Training Runs of logged in user.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable    pageable parameter with information about pagination.
     * @param sortByTitle optional parameter. "asc" for ascending sort, "desc" for descending and null if sort is not wanted
     * @return Page of all {@link AccessedTrainingRunDTO} of logged in user.
     */
    @IsTraineeOrAdmin
    @TransactionalRO
    public PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Predicate predicate, Pageable pageable, String sortByTitle) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByParticipantRefUserRefId(predicate, pageable);
        return convertToAccessedRunDTO(trainingRuns, sortByTitle);
    }

    /**
     * Resume given training run.
     *
     * @param trainingRunId id of Training Run to be resumed.
     * @return {@link AccessTrainingRunDTO} response
     */
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public AccessTrainingRunDTO resumeTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();

        // Managed instances: reuse managed flow semantics for resume as well – if sandbox is gone or not yet ready,
        // keep user on overview instead of entering the run with no environment.
        if (trainingInstance.isManaged()) {
            String userSub = securityService.getOidcSub();
            if (trainingRun.getSandboxInstanceRefId() != null && !trainingRun.getSandboxInstanceRefId().isBlank()) {
                if (trainingRunService.verifySandboxStillExistsAndDetachIfNot(trainingRun)) {
                    trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
                    AccessTrainingRunDTO dto = convertToAccessTrainingRunDTO(trainingRun);
                    dto.setManaged(true);
                    return dto;
                }
                AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, trainingRunId, List.of());
                dto.setManaged(true);
                return dto;
            }
            if (trainingRun.getSandboxInstanceAllocationId() != null) {
                if (trainingRunService.attachSandboxIfAllocationReady(trainingRun)) {
                    trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
                    AccessTrainingRunDTO dto = convertToAccessTrainingRunDTO(trainingRun);
                    dto.setManaged(true);
                    return dto;
                }
                List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, trainingRunId, activeSandboxes);
                dto.setManaged(true);
                return dto;
            }
            // No sandbox and no allocation – behave like "no free sandbox" in managed access flow.
            AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, trainingRunId, List.of());
            dto.setManaged(true);
            return dto;
        }

        // Non-managed instances with single-sandbox-per-user enabled: require a valid sandbox before resuming.
        if (singleSandboxPerUserEnabled && !trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() != null) {
            String userSub = securityService.getOidcSub();
            // If run has a sandbox, verify it still exists.
            if (trainingRun.getSandboxInstanceRefId() != null && !trainingRun.getSandboxInstanceRefId().isBlank()) {
                if (!trainingRunService.verifySandboxStillExistsAndDetachIfNot(trainingRun)) {
                    // Sandbox was removed (e.g. by cleanup/admin) – treat as no sandbox below.
                    trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
                } else {
                    trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
                    return enrichWithHintsAndSolution(trainingRun);
                }
            }

            // No sandbox ref at this point.
            if (trainingRun.getSandboxInstanceAllocationId() != null) {
                // There is an allocation; check if it is ready now.
                if (trainingRunService.attachSandboxIfAllocationReady(trainingRun)) {
                    trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
                    return enrichWithHintsAndSolution(trainingRun);
                }
                // Still building – return stay-on-overview with active sandboxes and stages.
                List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                return buildStayOnOverviewDTO(trainingInstance, trainingRunId, activeSandboxes);
            }

            // No sandbox and no allocation – user must allocate via access code first.
            List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
            if (activeSandboxes == null) {
                activeSandboxes = new ArrayList<>();
            }
            trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
            trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
            trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
            AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, trainingRunId, activeSandboxes);
            // Signal that user is allowed to allocate (frontend can show a message like
            // “Please allocate a sandbox using your access code before resuming this training.”).
            dto.setAllowAllocate(true);
            return dto;
        }

        // Fallback: environments that do not use single-sandbox-per-user (or local), keep existing behaviour.
        trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
        return enrichWithHintsAndSolution(trainingRun);
    }

    private AccessTrainingRunDTO enrichWithHintsAndSolution(TrainingRun trainingRun) {
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
     * Returns true if the current user (from JWT) has an active training run that uses the given sandbox.
     * Used by sandbox-service to verify a trainee may access a specific sandbox (e.g. topology) when pool is locked.
     *
     * @param sandboxId sandbox UUID
     * @return true if the current user has an active run with this sandbox
     */
    @IsTraineeOrAdmin
    @TransactionalRO
    public boolean verifySandboxAccessForCurrentUser(String sandboxId) {
        Long userRefId = securityService.getUserRefIdFromUserAndGroup();
        return trainingRunService.hasActiveRunWithSandboxForUser(userRefId, sandboxId);
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
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        TrainingInstance trainingInstance;
        if (singleSandboxPerUserEnabled) {
            // When single-sandbox is enabled, user may have an existing run for an instance whose time window has expired.
            // Resolve instance from existing run if any, so they get stay-on-overview or resume instead of EntityNotFoundException.
            Optional<TrainingRun> existingRun = trainingRunService.findRunningTrainingRunOfUserWithOrWithoutSandbox(accessToken, participantRefId);
            if (existingRun.isPresent()) {
                trainingInstance = existingRun.get().getTrainingInstance();
            } else {
                trainingInstance = trainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
            }
        } else {
            trainingInstance = trainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
        }
        // First: check if this is a managed instance — trainee cannot allocate; only use a sandbox allocated by Admin.
        if (trainingInstance.isManaged()) {
            return accessTrainingRunManaged(trainingInstance, accessToken, participantRefId);
        }
        // Second: single-sandbox-per-user — check active sandboxes before allowing resume or new allocation.
        // Number of allocations per student is controlled here: we never create a second allocation when they already have one.
        if (singleSandboxPerUserEnabled) {
            String userSub = securityService.getOidcSub();
            List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
            if (activeSandboxes == null) {
                activeSandboxes = new ArrayList<>();
            }
            trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
            trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
            trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
            // Managed-instance sandboxes do not count toward single-allocation quota for non-managed instances.
            List<ActiveSandboxSummaryDTO> nonManagedSandboxes = activeSandboxes.stream()
                    .filter(s -> !Boolean.TRUE.equals(s.getTrainingInstanceManaged()))
                    .collect(Collectors.toList());
            if (!nonManagedSandboxes.isEmpty()) {
                Optional<TrainingRun> runForThisToken = trainingRunService.findRunningTrainingRunOfUserWithOrWithoutSandbox(accessToken, participantRefId);
                // Allow resume when the user has a fully ready non-managed sandbox that belongs to this token's run
                // (by sandbox ref or by allocation id). When run has allocation id but no sandbox ref yet, we fall
                // through so the Second block can attach and resume.
                TrainingRun run = runForThisToken.orElse(null);
                boolean mayResume = false;
                if (run != null) {
                    for (ActiveSandboxSummaryDTO s : nonManagedSandboxes) {
                        boolean runHasSandboxRef = run.getSandboxInstanceRefId() != null && !run.getSandboxInstanceRefId().isBlank();
                        boolean sandboxMatchesRef = runHasSandboxRef && run.getSandboxInstanceRefId().equals(s.getSandboxId());
                        boolean runHasAllocationNoRef = !runHasSandboxRef
                                && run.getSandboxInstanceAllocationId() != null
                                && run.getSandboxInstanceAllocationId().equals(s.getId());
                        if ((sandboxMatchesRef || (runHasAllocationNoRef && s.isAllowRemove()))
                                && TrainingRunService.isAllocationFullyReady(s)) {
                            mayResume = true;
                            break;
                        }
                    }
                }
                if (!mayResume) {
                    AccessTrainingRunDTO dto = new AccessTrainingRunDTO();
                    dto.setInstanceId(trainingInstance.getId());
                    dto.setTrainingInstanceTitle(trainingInstance.getTitle());
                    dto.setAllowAllocate(false);
                    dto.setActiveSandboxes(activeSandboxes);
                    return dto;
                }
                // Matching sandbox for this token's run — fall through to resume below
            }
        }
        // Second: if user has an existing run for this token, enter it (resume or return with allowAllocate if sandbox was removed).
        Optional<TrainingRun> accessedTrainingRun = trainingRunService.findRunningTrainingRunOfUserWithOrWithoutSandbox(accessToken, participantRefId);
        if (accessedTrainingRun.isPresent()) {
            TrainingRun trainingRun = accessedTrainingRun.get();
            // If run has a sandbox, verify it still exists (e.g. not removed by admin). If gone, detach and let user allocate again.
            if (trainingRun.getSandboxInstanceRefId() != null && !trainingRun.getSandboxInstanceRefId().isBlank()) {
                if (!trainingRunService.verifySandboxStillExistsAndDetachIfNot(trainingRun)) {
                    // Sandbox was removed (e.g. by admin). Start allocation without waiting; user stays on /run.
                    trainingRun = trainingRunService.findByIdWithLevel(trainingRun.getId());
                    if (singleSandboxPerUserEnabled && !trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() != null) {
                        String userSub = securityService.getOidcSub();
                        List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                        if (activeSandboxes == null) activeSandboxes = new ArrayList<>();
                        trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                        trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                        trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                        List<ActiveSandboxSummaryDTO> nonManagedSandboxes = activeSandboxes.stream()
                                .filter(s -> !Boolean.TRUE.equals(s.getTrainingInstanceManaged()))
                                .collect(Collectors.toList());
                        if (nonManagedSandboxes.isEmpty()) {
                            trainingRun = trainingRunService.startSandboxAllocationForTrainingRun(trainingRun, userSub);
                            activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                            trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                            trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                            trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                            return buildStayOnOverviewDTO(trainingInstance, trainingRun.getId(), activeSandboxes);
                        }
                    }
                    AccessTrainingRunDTO dto = convertToAccessTrainingRunDTO(trainingRun);
                    dto.setAllowAllocate(true);
                    return dto;
                }
            }
            if (trainingRun.getSandboxInstanceRefId() == null || trainingRun.getSandboxInstanceRefId().isBlank()) {
                // Run exists but has no sandbox. If it has an allocation id, check if ready now (user re-submitted access code).
                if (trainingRun.getSandboxInstanceAllocationId() != null) {
                    if (trainingRunService.attachSandboxIfAllocationReady(trainingRun)) {
                        trainingRun = trainingRunService.resumeTrainingRun(trainingRun.getId());
                        return convertToAccessTrainingRunDTO(trainingRun);
                    }
                    // Still building: return stay-on-overview response (active sandboxes with stages)
                    String userSub = securityService.getOidcSub();
                    List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                    trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                    trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                    trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                    return buildStayOnOverviewDTO(trainingInstance, trainingRun.getId(), activeSandboxes);
                }
                // No allocation yet (e.g. admin removed). Start allocation without waiting; user re-submits when ready.
                if (singleSandboxPerUserEnabled && !trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() != null) {
                    String userSub = securityService.getOidcSub();
                    List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                    if (activeSandboxes == null) activeSandboxes = new ArrayList<>();
                    trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                    trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                    trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                    List<ActiveSandboxSummaryDTO> nonManagedSandboxes = activeSandboxes.stream()
                            .filter(s -> !Boolean.TRUE.equals(s.getTrainingInstanceManaged()))
                            .collect(Collectors.toList());
                    if (nonManagedSandboxes.isEmpty()) {
                        trainingRun = trainingRunService.startSandboxAllocationForTrainingRun(trainingRun, userSub);
                        activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                        trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                        trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                        trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                        return buildStayOnOverviewDTO(trainingInstance, trainingRun.getId(), activeSandboxes);
                    }
                }
                trainingRun = trainingRunService.findByIdWithLevel(trainingRun.getId());
                AccessTrainingRunDTO dto = convertToAccessTrainingRunDTO(trainingRun);
                dto.setAllowAllocate(true);
                return dto;
            }
            trainingRun = trainingRunService.resumeTrainingRun(trainingRun.getId());
            return convertToAccessTrainingRunDTO(trainingRun);
        }
        // Third: single-sandbox-per-user already allowed (no active sandboxes) or disabled — check lock and create run
        // With single-sandbox we only start allocation (no wait); user stays on /run and re-submits access code when ready.
        trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId(), accessToken);
        try {
            TrainingRun trainingRun = trainingRunService.createTrainingRun(trainingInstance, participantRefId);
            if (!trainingInstance.isLocalEnvironment()) {
                if (singleSandboxPerUserEnabled) {
                    String userSub = securityService.getOidcSub();
                    trainingRun = trainingRunService.startSandboxAllocationForTrainingRun(trainingRun, userSub);
                    trainingRunService.auditTrainingRunStarted(trainingRun);
                    List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                    trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                    trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                    trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                    return buildStayOnOverviewDTO(trainingInstance, trainingRun.getId(), activeSandboxes);
                } else {
                    trainingRunService.assignSandbox(trainingRun, trainingInstance.getPoolId());
                }
            }
            trainingRunService.auditTrainingRunStarted(trainingRun);
            return convertToAccessTrainingRunDTO(trainingRun);
        } catch (Exception e) {
            trainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId());
            throw e;
        }
    }

    /** Builds a response that tells the frontend to stay on /run (show message and active sandboxes). No level content. */
    private AccessTrainingRunDTO buildStayOnOverviewDTO(TrainingInstance trainingInstance, Long trainingRunId, List<ActiveSandboxSummaryDTO> activeSandboxes) {
        AccessTrainingRunDTO dto = new AccessTrainingRunDTO();
        dto.setInstanceId(trainingInstance.getId());
        dto.setTrainingInstanceTitle(trainingInstance.getTitle());
        dto.setTrainingRunID(trainingRunId);
        dto.setAllowAllocate(false);
        dto.setActiveSandboxes(activeSandboxes != null ? activeSandboxes : List.of());
        dto.setShowStepperBar(false);
        dto.setSandboxInstanceRefId(null);
        dto.setAccessToken(trainingInstance.getAccessToken());
        return dto;
    }

    /**
     * Access flow for managed training instances: trainee cannot allocate a sandbox; only use one
     * already in the pool. Reserve via get-and-lock (one call, no allocation created). If no free
     * sandbox, stay on overview with message only.
     */
    private AccessTrainingRunDTO accessTrainingRunManaged(TrainingInstance trainingInstance, String accessToken, Long participantRefId) {
        String userSub = securityService.getOidcSub();
        Long poolId = trainingInstance.getPoolId();
        if (poolId == null || trainingInstance.isLocalEnvironment()) {
            AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, null, List.of());
            dto.setManaged(true);
            return dto;
        }
        Optional<TrainingRun> existingRunOpt = trainingRunService.findRunningTrainingRunOfUserWithOrWithoutSandbox(accessToken, participantRefId);
        if (existingRunOpt.isPresent()) {
            TrainingRun run = existingRunOpt.get();
            if (run.getSandboxInstanceRefId() != null && !run.getSandboxInstanceRefId().isBlank()) {
                if (trainingRunService.verifySandboxStillExistsAndDetachIfNot(run)) {
                    run = trainingRunService.resumeTrainingRun(run.getId());
                    AccessTrainingRunDTO dto = convertToAccessTrainingRunDTO(run);
                    dto.setManaged(true);
                    return dto;
                }
                AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, run.getId(), List.of());
                dto.setManaged(true);
                return dto;
            }
            if (run.getSandboxInstanceAllocationId() != null) {
                if (trainingRunService.attachSandboxIfAllocationReady(run)) {
                    run = trainingRunService.resumeTrainingRun(run.getId());
                    AccessTrainingRunDTO dto = convertToAccessTrainingRunDTO(run);
                    dto.setManaged(true);
                    return dto;
                }
                List<ActiveSandboxSummaryDTO> activeSandboxes = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
                trainingRunService.enrichActiveSandboxesWithAllocationRequest(activeSandboxes, participantRefId);
                trainingRunService.enrichActiveSandboxesWithAllowRemove(activeSandboxes);
                trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(activeSandboxes, participantRefId);
                AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, run.getId(), activeSandboxes);
                dto.setManaged(true);
                return dto;
            }
            Optional<TrainingRun> attachedOpt = trainingRunService.reserveAndAttachSandboxForManaged(run, poolId, accessToken);
            if (attachedOpt.isPresent()) {
                // Load run in new transaction so we see the committed attach (avoid stale persistence context)
                run = trainingRunService.findByIdWithLevelForResumeInNewTransaction(run.getId());
                run = entityManager.merge(run);
                trainingRunService.validateResumeAndAuditResumed(run, run.getId());
                AccessTrainingRunDTO dto = convertToAccessTrainingRunDTO(run);
                dto.setManaged(true);
                return dto;
            }
            AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, run.getId(), List.of());
            dto.setManaged(true);
            return dto;
        }
        // No existing run: reserve one sandbox via get-and-lock first; if none free, stay on overview without creating a run.
        Optional<SandboxInfo> sandboxOpt = trainingRunService.tryReserveSandboxForManaged(poolId, accessToken);
        if (sandboxOpt.isEmpty()) {
            AccessTrainingRunDTO dto = buildStayOnOverviewDTO(trainingInstance, null, List.of());
            dto.setManaged(true);
            return dto;
        }
        SandboxInfo sandboxInfo = sandboxOpt.get();
        trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId(), accessToken);
        try {
            TrainingRun trainingRun = trainingRunService.createTrainingRunInNewTransaction(trainingInstance, participantRefId);
            trainingRunService.attachRunToAllocationInNewTransaction(trainingRun.getId(), sandboxInfo.getAllocationUnitId(), sandboxInfo.getId());
            trainingRun = trainingRunService.findByIdWithLevelForResumeInNewTransaction(trainingRun.getId());
            trainingRun = entityManager.merge(trainingRun);
            trainingRunService.auditTrainingRunStarted(trainingRun);
            AccessTrainingRunDTO dto = convertToAccessTrainingRunDTO(trainingRun);
            dto.setManaged(true);
            return dto;
        } catch (Exception e) {
            trainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId());
            throw e;
        }
    }

    private AccessTrainingRunDTO convertToAccessTrainingRunDTO(TrainingRun trainingRun) {
        AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
        accessTrainingRunDTO.setTrainingRunID(trainingRun.getId());
        accessTrainingRunDTO.setAbstractLevelDTO(getAbstractLevelDTO(trainingRun.getCurrentLevel()));
        accessTrainingRunDTO.setShowStepperBar(trainingRun.getTrainingInstance().isShowStepperBar());
        accessTrainingRunDTO.setInfoAboutLevels(getInfoAboutLevels(trainingRun.getCurrentLevel().getTrainingDefinition().getId()));
        accessTrainingRunDTO.setSandboxInstanceRefId(trainingRun.getSandboxInstanceRefId());
        accessTrainingRunDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
        accessTrainingRunDTO.setStartTime(trainingRun.getStartTime());
        accessTrainingRunDTO.setLocalEnvironment(trainingRun.getTrainingInstance().isLocalEnvironment());
        accessTrainingRunDTO.setBackwardMode(trainingRun.getTrainingInstance().isBackwardMode());
        accessTrainingRunDTO.setSandboxDefinitionId(trainingRun.getTrainingInstance().getSandboxDefinitionId());
        accessTrainingRunDTO.setLevelAnswered(trainingRun.isLevelAnswered());
        accessTrainingRunDTO.setAllowAllocate(true);
        accessTrainingRunDTO.setTrainingInstanceTitle(trainingRun.getTrainingInstance().getTitle());
        accessTrainingRunDTO.setAccessToken(trainingRun.getTrainingInstance().getAccessToken());
        if (trainingRun.getCurrentLevel().getClass() == AccessLevel.class) {
            replacePlaceholders(
                    (AccessLevelViewDTO) accessTrainingRunDTO.getAbstractLevelDTO(),
                    trainingRun.getTrainingInstance().getAccessToken(),
                    securityService.getBearerToken(),
                    trainingRun.getParticipantRef().getUserRefId(),
                    trainingRun.getTrainingInstance().getSandboxDefinitionId()
            );
        }
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
            } else if (abstractLevel instanceof AccessLevel) {
                infoAboutLevels.add(new BasicLevelInfoDTO(abstractLevel.getId(), abstractLevel.getTitle(), LevelType.ACCESS_LEVEL, abstractLevel.getOrder()));
            } else {
                infoAboutLevels.add(new BasicLevelInfoDTO(abstractLevel.getId(), abstractLevel.getTitle(), LevelType.INFO_LEVEL, abstractLevel.getOrder()));
            }
        }
        return infoAboutLevels;
    }

    /**
     * Request cleanup of a sandbox owned by the current trainee (single-sandbox-per-user).
     * Verifies ownership via sandbox-service; only the owner can request cleanup.
     *
     * @param sandboxId sandbox UUID
     */
    @IsTraineeOrAdmin
    @Transactional
    public void requestTraineeSandboxCleanup(String sandboxId) {
        String userSub = securityService.getOidcSub();
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        trainingRunService.requestTraineeSandboxCleanup(sandboxId, userSub, participantRefId);
    }

    @IsTraineeOrAdmin
    @Transactional
    public void requestTraineeSandboxCleanupByAllocationId(Integer allocationUnitId) {
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        trainingRunService.requestTraineeSandboxCleanupByAllocationId(allocationUnitId, participantRefId);
    }

    /**
     * Returns active sandboxes for the current user (single-sandbox-per-user).
     * Used by Run Overview to display deployed sandboxes with stages and delete. Empty when feature is disabled.
     */
    @IsTraineeOrAdmin
    @TransactionalRO
    public List<ActiveSandboxSummaryDTO> getUserActiveSandboxes() {
        if (!singleSandboxPerUserEnabled) {
            return List.of();
        }
        String userSub = securityService.getOidcSub();
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        List<ActiveSandboxSummaryDTO> list = trainingRunService.getMergedActiveSandboxesForUser(userSub, participantRefId);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        trainingRunService.enrichActiveSandboxesWithAllocationRequest(list, participantRefId);
        trainingRunService.enrichActiveSandboxesWithAllowRemove(list);
        trainingRunService.enrichActiveSandboxesWithTrainingInstanceTitles(list, participantRefId);
        return list;
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
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public AbstractLevelDTO getNextLevel(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.moveToNextLevel(trainingRunId);
        AbstractLevelDTO abstractLevelDTO = getAbstractLevelDTO(trainingRun.getCurrentLevel());
        if (abstractLevelDTO.getClass() == AccessLevelViewDTO.class) {
            replacePlaceholders(
                    (AccessLevelViewDTO) abstractLevelDTO,
                    trainingRun.getTrainingInstance().getAccessToken(),
                    securityService.getBearerToken(),
                    trainingRun.getParticipantRef().getUserRefId(),
                    trainingRun.getTrainingInstance().getSandboxDefinitionId()
            );
        }
        abstractLevelDTO.setTrainingDefinition(null);
        return abstractLevelDTO;
    }

    /**
     * Gets solution of current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets solution for.
     * @return solution of current level.
     */
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
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
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
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
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
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
     * Check given passkey of given Training Run.
     *
     * @param trainingRunId id of Training Run to check passkey.
     * @param passkey          string which player submit.
     * @return true if passkey is correct, false if passkey is wrong.
     */
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public Boolean isCorrectPasskey(Long trainingRunId, String passkey) {
        return trainingRunService.isCorrectPassKey(trainingRunId, passkey);
    }

    /**
     * Finish training run.
     *
     * @param trainingRunId id of Training Run to be finished.
     */
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
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
        boolean isAnyReferenceSolution = false;
        List<LevelReferenceSolutionDTO> referenceSolution = new ArrayList<>();
        for (TrainingLevel level : this.trainingDefinitionService.getAllTrainingLevels(definition.getId())) {
            isAnyReferenceSolution = isAnyReferenceSolution || !level.getReferenceSolution().isEmpty();
            referenceSolution.add(createLevelReferenceSolutionDTO(level));
        }
        if(isAnyReferenceSolution) {
            this.trainingFeedbackApiService.createTraineeGraph(definition.getId(), instance.getId(), run.getId(), referenceSolution,
                    run.getTrainingInstance().isLocalEnvironment() ? run.getTrainingInstance().getAccessToken() : null);
            this.trainingFeedbackApiService.deleteSummaryGraph(instance.getId());
            this.trainingFeedbackApiService.createSummaryGraph(definition.getId(), instance.getId());
        }
    }

    private LevelReferenceSolutionDTO createLevelReferenceSolutionDTO(TrainingLevel trainingLevel) {
        return new LevelReferenceSolutionDTO(
                trainingLevel.getId(),
                trainingLevel.getOrder(),
                new ArrayList<>(ReferenceSolutionNodeMapper.INSTANCE.mapToSetDTO(trainingLevel.getReferenceSolution()))
        );
    }

    /**
     * Archive training run.
     *
     * @param trainingRunId id of Training Run to be archived.
     */
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
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
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalRO
    public UserRefDTO getParticipant(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
        return userService.getUserRefDTOByUserRefId(trainingRun.getParticipantRef().getUserRefId());
    }

    /**
     * Gets visited level of given Training Run.
     *
     * @param trainingRunId id of Training Run whose visited level should be returned.
     * @param levelId ID of the visited level.
     * @return {@link AbstractLevelDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public AbstractLevelDTO getVisitedLevel(Long trainingRunId, Long levelId) {
        AbstractLevel abstractLevel = trainingRunService.getVisitedLevel(trainingRunId, levelId);
        TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
        AbstractLevelDTO abstractLevelDTO = getAbstractLevelPreviewDTO(abstractLevel, trainingRun);
        abstractLevelDTO.setTrainingDefinition(null);
        return abstractLevelDTO;
    }

    /**
     * Gets correct answers of the given Training Run.
     * Includes both Training levels (single correct answer per level) and Assessment levels
     * (correct answers per question: MCQ, FFQ, EMI).
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @return {@link CorrectAnswerDTO[]} one entry per level, in level order
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public List<CorrectAnswerDTO> getCorrectAnswers(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
        Long definitionId = trainingRun.getTrainingInstance().getTrainingDefinition().getId();
        List<AbstractLevel> allLevels = trainingRunService.getLevels(definitionId);
        List<TrainingLevel> trainingLevels = allLevels.stream()
                .filter(abstractLevel -> abstractLevel.getClass() == TrainingLevel.class)
                .map(abstractLevel -> (TrainingLevel) abstractLevel)
                .toList();
        boolean isVariantAnswers = trainingLevels.stream().anyMatch(TrainingLevel::isVariantAnswers);
        Map<String, String> variantAnswers = isVariantAnswers ? getVariantAnswers(trainingRun) : new HashMap<>();

        List<CorrectAnswerDTO> result = new ArrayList<>();
        for (AbstractLevel level : allLevels) {
            if (level instanceof TrainingLevel tl) {
                result.add(mapToCorrectAnswerDTO(tl, variantAnswers));
            } else if (level instanceof AssessmentLevel) {
                trainingRunService.getAssessmentLevelWithQuestions(level.getId()).ifPresent(assessmentLevel -> {
                    List<QuestionCorrectAnswerDTO> questionDtos = assessmentLevel.getQuestions().stream()
                            .map(q -> {
                                QuestionCorrectAnswerDTO dto = new QuestionCorrectAnswerDTO();
                                dto.setQuestionId(q.getId());
                                dto.setQuestionText(q.getText());
                                dto.setCorrectAnswers(trainingRunService.getCorrectAnswersForQuestion(q));
                                return dto;
                            })
                            .collect(Collectors.toList());
                    CorrectAnswerDTO dto = new CorrectAnswerDTO();
                    dto.setLevelId(assessmentLevel.getId());
                    dto.setLevelTitle(assessmentLevel.getTitle());
                    dto.setLevelOrder(assessmentLevel.getOrder());
                    dto.setLevelType("ASSESSMENT");
                    dto.setQuestionCorrectAnswers(questionDtos);
                    result.add(dto);
                });
            }
        }
        return result;
    }

    private Map<String, String> getVariantAnswers(TrainingRun trainingRun) {
        TrainingInstance instance = trainingRun.getTrainingInstance();
        return instance.isLocalEnvironment() ?
                answersStorageApiService.getAnswersByAccessTokenAndUserId(instance.getAccessToken(), trainingRun.getParticipantRef().getUserRefId())
                        .getVariantAnswers().stream()
                        .collect(Collectors.toMap(VariantAnswer::getAnswerVariableName, VariantAnswer::getAnswerContent)) :
                answersStorageApiService.getAnswersBySandboxId(trainingRun.getSandboxInstanceRefId())
                        .getVariantAnswers().stream()
                        .collect(Collectors.toMap(VariantAnswer::getAnswerVariableName, VariantAnswer::getAnswerContent));
    }

    private CorrectAnswerDTO mapToCorrectAnswerDTO(TrainingLevel trainingLevel, Map<String, String> variantAnswers) {
        CorrectAnswerDTO correctAnswerDTO = new CorrectAnswerDTO();
        correctAnswerDTO.setLevelId(trainingLevel.getId());
        correctAnswerDTO.setLevelTitle(trainingLevel.getTitle());
        correctAnswerDTO.setLevelOrder(trainingLevel.getOrder());
        correctAnswerDTO.setLevelType("TRAINING");
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

    private AbstractLevelDTO getAbstractLevelDTO(AbstractLevel abstractLevel) {
        if (abstractLevel == null) {
            return null;
        }
        AbstractLevel level = (AbstractLevel) Hibernate.unproxy(abstractLevel);
        AbstractLevelDTO abstractLevelDTO;
        if (level instanceof AssessmentLevel assessmentLevel) {
            abstractLevelDTO = levelMapper.mapToAssessmentLevelDTO(assessmentLevel);
            deleteInfoAboutCorrectnessFromQuestions((AssessmentLevelDTO) abstractLevelDTO);
        } else if (level instanceof TrainingLevel trainingLevel) {
            abstractLevelDTO = levelMapper.mapToViewDTO(trainingLevel);
        } else if (level instanceof AccessLevel accessLevel) {
            abstractLevelDTO = levelMapper.mapToViewDTO(accessLevel);
        } else if (level instanceof InfoLevel infoLevel) {
            abstractLevelDTO = levelMapper.mapToInfoLevelDTO(infoLevel);
        } else {
            throw new IllegalStateException("Unknown level type: " + level.getClass().getName());
        }
        return abstractLevelDTO;
    }

    private AbstractLevelDTO getAbstractLevelPreviewDTO(AbstractLevel abstractLevel, TrainingRun trainingRun) {
        if (abstractLevel == null) {
            return null;
        }
        AbstractLevel level = (AbstractLevel) Hibernate.unproxy(abstractLevel);
        if (level instanceof InfoLevel infoLevel) {
            return levelMapper.mapToInfoLevelDTO(infoLevel);
        } else if (level instanceof TrainingLevel trainingLevel) {
            return mapToTrainingLevelPreviewDTO(trainingLevel, trainingRun);
        } else if (level instanceof AccessLevel accessLevel) {
            return mapToAccessLevelViewDTO(accessLevel, trainingRun);
        } else if (level instanceof AssessmentLevel assessmentLevel) {
            return mapToAssessmentLevelPreviewDTO(assessmentLevel, trainingRun);
        } else {
            throw new IllegalStateException("Unknown level type: " + level.getClass().getName());
        }
    }

    private TrainingLevelPreviewDTO mapToTrainingLevelPreviewDTO(TrainingLevel trainingLevel, TrainingRun trainingRun) {
        TrainingLevelPreviewDTO trainingLevelPreviewDTO = levelMapper.mapToPreviewDTO(trainingLevel);
        trainingLevelPreviewDTO.setHints(hintMapper.mapToSetInfoDTO(trainingRun.getHintInfoList().stream()
                .filter(hintInfo ->  hintInfo.getTrainingLevelId().equals(trainingLevel.getId()))
                .toList())
        );
        String takenSolution = trainingRun.getSolutionInfoList().stream()
                .filter(solutionInfo -> trainingLevel.getId().equals(solutionInfo.getTrainingLevelId()))
                .map(SolutionInfo::getSolutionContent)
                .findFirst().orElse(null);
        if (takenSolution != null && takenSolution.contains("${ANSWER}")) {
            takenSolution = takenSolution.replaceAll("\\$\\{ANSWER\\}", trainingRunService.getTrainingLevelCorrectAnswer(trainingLevel, trainingRun));
        }
        trainingLevelPreviewDTO.setSolution(takenSolution);
        return trainingLevelPreviewDTO;
    }

    private AccessLevelViewDTO mapToAccessLevelViewDTO(AccessLevel accessLevel, TrainingRun trainingRun) {
        AccessLevelViewDTO accessLevelViewDTO = levelMapper.mapToViewDTO(accessLevel);
        replacePlaceholders(
                accessLevelViewDTO,
                trainingRun.getTrainingInstance().getAccessToken(),
                securityService.getBearerToken(),
                trainingRun.getParticipantRef().getUserRefId(),
                trainingRun.getTrainingInstance().getSandboxDefinitionId()
        );
        return accessLevelViewDTO;
    }

    private AssessmentLevelPreviewDTO mapToAssessmentLevelPreviewDTO(AssessmentLevel assessmentLevel, TrainingRun trainingRun) {
        Map<Long, Set<String>> userAnswersByQuestionId = trainingRunService.getQuestionAnswersByTrainingRunId(trainingRun.getId()).stream()
                .collect(Collectors.toMap(q -> q.getQuestion().getId(), QuestionAnswer::getAnswers));
        AssessmentLevelPreviewDTO assessmentPreviewDTO = levelMapper.mapToAssessmentLevelPreviewDTO(assessmentLevel);
        assessmentPreviewDTO.getQuestions().forEach(previewQuestionDTO -> {
            if (previewQuestionDTO.getQuestionType() == QuestionType.EMI) {
                Set<String> emiAnswers = userAnswersByQuestionId.getOrDefault(previewQuestionDTO.getId(), new HashSet<>());
                emiAnswers.stream()
                        .map(this::convertEmiAnswerToMap)
                        .forEach(emiAnswerMap -> previewQuestionDTO.getExtendedMatchingStatements().get(emiAnswerMap.get("statementOrder")).setUserOptionOrder(emiAnswerMap.get("optionOrder")));
            } else {
                if (previewQuestionDTO.getQuestionType() == QuestionType.FFQ) {
                    previewQuestionDTO.setChoices(new ArrayList<>());
                }
                previewQuestionDTO.setUserAnswers(userAnswersByQuestionId.get(previewQuestionDTO.getId()));
            }
        });
        return assessmentPreviewDTO;
    }

    private Map<String, Integer> convertEmiAnswerToMap(String emiAnswer) {
        emiAnswer = emiAnswer.replaceAll("\"", "");
        emiAnswer = emiAnswer.substring(1, emiAnswer.length() -1);
        return Arrays.stream( emiAnswer.split(",") )
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(s -> s[0].trim(), s -> Integer.parseInt(s[1].trim())));
    }

    private void replacePlaceholders(AccessLevelViewDTO accessLevelViewDTO, String accessToken, String bearerToken, Long userId, Long sandboxDefinitionId) {
        String localContent = accessLevelViewDTO.getLocalContent();
        localContent = localContent.replaceAll("\\$\\{ACCESS_TOKEN\\}", accessToken);
        localContent = localContent.replaceAll("\\$\\{BEARER_TOKEN\\}", bearerToken);
        localContent = localContent.replaceAll("\\$\\{USER_ID\\}", userId.toString());
        localContent = localContent.replaceAll("\\$\\{SANDBOX_DEFINITION_ID\\}", sandboxDefinitionId == null ? "" : sandboxDefinitionId.toString());
        localContent = localContent.replaceAll("\\$\\{CENTRAL_SYSLOG_IP\\}", centralSyslogIp);
        accessLevelViewDTO.setLocalContent(localContent);
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
