package cz.cyberrange.platform.training.service.services;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import cz.cyberrange.platform.training.api.exceptions.TooManyRequestsException;
import cz.cyberrange.platform.training.api.responses.ActiveSandboxSummaryDTO;
import cz.cyberrange.platform.training.api.responses.SandboxInfo;
import cz.cyberrange.platform.training.persistence.model.*;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.enums.SubmissionType;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.AssessmentLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TRAcquisitionLockRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.ElasticsearchApiService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Training run service.
 */
@Service
public class TrainingRunService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunService.class);
    private static final String X_REAL_IP_HEADER = "x-real-ip";

    private final TrainingRunRepository trainingRunRepository;
    private final AbstractLevelRepository abstractLevelRepository;
    private final AssessmentLevelRepository assessmentLevelRepository;
    private final TrainingInstanceRepository trainingInstanceRepository;
    private final UserRefRepository participantRefRepository;
    private final HintRepository hintRepository;
    private final AuditEventsService auditEventsService;
    private final ElasticsearchApiService elasticsearchApiService;
    private final AnswersStorageApiService answersStorageApiService;
    private final SecurityService securityService;
    private final TRAcquisitionLockRepository trAcquisitionLockRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final SandboxApiService sandboxApiService;
    private final SubmissionRepository submissionRepository;

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
     * @param sandboxApiService           the python rest template
     * @param trAcquisitionLockRepository the tr acquisition lock repository
     */
    @Autowired
    public TrainingRunService(TrainingRunRepository trainingRunRepository,
                              AbstractLevelRepository abstractLevelRepository,
                              AssessmentLevelRepository assessmentLevelRepository,
                              TrainingInstanceRepository trainingInstanceRepository,
                              UserRefRepository participantRefRepository,
                              HintRepository hintRepository,
                              AuditEventsService auditEventsService,
                              ElasticsearchApiService elasticsearchApiService,
                              AnswersStorageApiService answersStorageApiService,
                              SecurityService securityService,
                              QuestionAnswerRepository questionAnswerRepository,
                              SandboxApiService sandboxApiService,
                              TRAcquisitionLockRepository trAcquisitionLockRepository,
                              SubmissionRepository submissionRepository) {
        this.trainingRunRepository = trainingRunRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.assessmentLevelRepository = assessmentLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.participantRefRepository = participantRefRepository;
        this.hintRepository = hintRepository;
        this.auditEventsService = auditEventsService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.answersStorageApiService = answersStorageApiService;
        this.securityService = securityService;
        this.questionAnswerRepository = questionAnswerRepository;
        this.sandboxApiService = sandboxApiService;
        this.trAcquisitionLockRepository = trAcquisitionLockRepository;
        this.submissionRepository = submissionRepository;
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
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId)));
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
                new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId)));
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
    public TrainingRun deleteTrainingRun(Long trainingRunId, boolean forceDelete, boolean deleteDataFromElasticsearch) {
        TrainingRun trainingRun = findById(trainingRunId);
        if (!forceDelete && trainingRun.getState().equals(TRState.RUNNING)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRun.getId().getClass(), trainingRun.getId(),
                    "Cannot delete training run that is running. Consider force delete."));
        }
        questionAnswerRepository.deleteAllByTrainingRunId(trainingRunId);
        submissionRepository.deleteAllByTrainingRunId(trainingRunId);
        if(deleteDataFromElasticsearch) {
            deleteDataFromElasticsearch(trainingRun);
        }
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
        Integer allocationUnitId = trainingRun.getSandboxInstanceAllocationId();
        if (allocationUnitId != null) {
            sandboxApiService.deleteLockForAllocationUnit(allocationUnitId);
        }
        trainingRunRepository.delete(trainingRun);
        return trainingRun;
    }

    private void deleteDataFromElasticsearch(TrainingRun trainingRun) {
        if(trainingRun.getTrainingInstance().isLocalEnvironment()) {
            String accessToken = trainingRun.getTrainingInstance().getAccessToken();
            Long userId = trainingRun.getParticipantRef().getUserRefId();
            elasticsearchApiService.deleteCommandsByAccessTokenAndUserId(accessToken, userId);
        } else {
            String sandboxId = trainingRun.getSandboxInstanceRefId() == null ? trainingRun.getPreviousSandboxInstanceRefId() : trainingRun.getSandboxInstanceRefId();
            elasticsearchApiService.deleteCommandsBySandbox(sandboxId);
        }
        elasticsearchApiService.deleteEventsFromTrainingRun(trainingRun.getTrainingInstance().getId(), trainingRun.getId());
    }

    /**
     * Checks whether any training runs exists for particular training instance
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
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of logged in user.
     */
    public Page<TrainingRun> findAllByParticipantRefUserRefId(Predicate predicate, Pageable pageable) {
        return trainingRunRepository.findAllByParticipantRefId(securityService.getUserRefIdFromUserAndGroup(), predicate, pageable);
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

    /**
     * Move to the next level of given Training Run by setting up a new current level.
     *
     * @param runId id of Training Run whose next level should be returned.
     * @return {@link AbstractLevel}
     * @throws EntityNotFoundException training run or level is not found.
     */
    public TrainingRun moveToNextLevel(Long runId) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        int currentLevelOrder = trainingRun.getCurrentLevel().getOrder();
        int maxLevelOrder = abstractLevelRepository.getCurrentMaxOrder(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        if (!(trainingRun.getCurrentLevel() instanceof InfoLevel) && !trainingRun.isLevelAnswered()) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId,
                    "You need to answer the level to move to the next level."));
        }
        if (currentLevelOrder == maxLevelOrder) {
            throw new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "There is no next level for current training run (ID: " + runId + ")."));
        }
        List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        int nextLevelIndex = levels.indexOf(trainingRun.getCurrentLevel()) + 1;
        AbstractLevel abstractLevel = levels.get(nextLevelIndex);
        if (trainingRun.getCurrentLevel() instanceof InfoLevel) {
            auditEventsService.auditLevelCompletedAction(trainingRun);
        }
        trainingRun.setCurrentLevel(abstractLevel);
        trainingRun.setIncorrectAnswerCount(0);
        trainingRunRepository.save(trainingRun);
        auditEventsService.auditLevelStartedAction(trainingRun);

        return trainingRun;
    }

    /**
     * Get previous/current level (visited) of given Training Run.
     *
     * @param runId ID of Training Run whose visited level should be returned.
     * @param levelId ID of the visited level that should be returned.
     * @return {@link AbstractLevel}
     */
    public AbstractLevel getVisitedLevel(Long runId, Long levelId) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        AbstractLevel abstractLevel = abstractLevelRepository.findById(levelId).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", levelId.getClass(), levelId, "Level not found")));
        TrainingDefinition trainingRunDefinition = trainingRun.getTrainingInstance().getTrainingDefinition();
        if (!abstractLevel.getTrainingDefinition().getId().equals(trainingRunDefinition.getId())) {
            throw new EntityConflictException(new EntityErrorDetail("Requested level (ID: " + levelId + ") is not part of the training run (ID: " + runId + ")."));
        }
        if (abstractLevel.getOrder() > trainingRun.getCurrentLevel().getOrder()) {
            throw new EntityConflictException(new EntityErrorDetail("Requested level (ID: " + levelId + ") hasn't been visited yet"));
        }
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
     * @throws EntityNotFoundException no active training instance for given access token, no starting level in training definition.
     * @throws EntityConflictException pool of sandboxes is not created for training instance.
     */
    public TrainingRun createTrainingRun(TrainingInstance trainingInstance, Long participantRefId) {
        AbstractLevel initialLevel = findFirstLevelForTrainingRun(trainingInstance.getTrainingDefinition().getId());
        TrainingRun trainingRun = getNewTrainingRun(initialLevel, trainingInstance, LocalDateTime.now(Clock.systemUTC()), trainingInstance.getEndTime(), participantRefId);
        return trainingRunRepository.save(trainingRun);
    }

    /**
     * Same as {@link #createTrainingRun} but runs in a new transaction (REQUIRES_NEW) so the run
     * is committed and visible to {@link #attachRunToAllocationInNewTransaction}. Used only in the
     * managed-flow "no existing run" path where we attach an allocation in a separate transaction.
     * Loads the training instance in this transaction to avoid using a detached entity.
     */
    @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
    public TrainingRun createTrainingRunInNewTransaction(TrainingInstance trainingInstance, Long participantRefId) {
        TrainingInstance ti = trainingInstanceRepository.findById(trainingInstance.getId())
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId())));
        AbstractLevel initialLevel = findFirstLevelForTrainingRun(ti.getTrainingDefinition().getId());
        TrainingRun trainingRun = getNewTrainingRun(initialLevel, ti, LocalDateTime.now(Clock.systemUTC()), ti.getEndTime(), participantRefId);
        return trainingRunRepository.save(trainingRun);
    }

    public void auditTrainingRunStarted(TrainingRun trainingRun) {
        auditEventsService.auditTrainingRunStartedAction(trainingRun);
        auditEventsService.auditLevelStartedAction(trainingRun);
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
     * Find running training run for this token and user, with or without sandbox.
     * Used when entering run so we can return the run with allowAllocate after admin removed sandbox.
     */
    public Optional<TrainingRun> findRunningTrainingRunOfUserWithOrWithoutSandbox(String accessToken, Long participantRefId) {
        return trainingRunRepository.findRunningTrainingRunOfUserWithOrWithoutSandbox(accessToken, participantRefId);
    }

    /** Grace period (minutes) after instance end_time in which access by token still succeeds (clock skew / in-flight requests). */
    private static final int ACCESS_TOKEN_END_GRACE_MINUTES = 2;

    /**
     * Gets training instance for particular access token.
     * Uses a short grace period after end_time so that requests in flight when the instance ends, or with clock skew, still succeed.
     *
     * @param accessToken the access token
     * @return the training instance for particular access token
     */
    public TrainingInstance getTrainingInstanceForParticularAccessToken(String accessToken) {
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        LocalDateTime endTimeMin = now.minusMinutes(ACCESS_TOKEN_END_GRACE_MINUTES);
        TrainingInstance trainingInstance = trainingInstanceRepository.findByAccessTokenActiveWithGrace(now, endTimeMin, accessToken)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                        "There is no active training session matching access token.")));
        if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() == null) {
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
            trAcquisitionLockRepository.saveAndFlush(new TRAcquisitionLock(participantRefId, trainingInstanceId, LocalDateTime.now(Clock.systemUTC())));
        } catch (DataIntegrityViolationException ex) {
            throw new TooManyRequestsException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                    "Training run has been already accessed and cannot be created again. Please resume Training Run"));
        }
    }

    @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
    public void deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(Long participantRefId, Long trainingInstanceId) {
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(participantRefId, trainingInstanceId);
    }

    private AbstractLevel findFirstLevelForTrainingRun(Long trainingDefinitionId) {
        List<AbstractLevel> levels = abstractLevelRepository.findFirstLevelByTrainingDefinitionId(trainingDefinitionId, PageRequest.of(0, 1));
        if (levels.isEmpty()) {
            throw new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", Long.class, trainingDefinitionId,
                    "No starting level available for this training definition."));
        }
        return levels.get(0);
    }

    private TrainingRun getNewTrainingRun(AbstractLevel currentLevel, TrainingInstance trainingInstance, LocalDateTime startTime, LocalDateTime endTime, Long participantRefId) {
        TrainingRun newTrainingRun = new TrainingRun();
        newTrainingRun.setCurrentLevel(currentLevel);

        UserRef userRef = participantRefRepository.createOrGet(participantRefId);
        newTrainingRun.setParticipantRef(userRef);

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
     * @throws ForbiddenException       no available sandbox.
     * @throws MicroserviceApiException error calling OpenStack Sandbox Service API
     */
    public TrainingRun assignSandbox(TrainingRun trainingRun, long poolId) {
        SandboxInfo info = sandboxApiService.getAndLockSandbox(poolId, trainingRun.getTrainingInstance().getAccessToken());
        trainingRun.setSandboxInstanceRefId(info.getId());
        trainingRun.setSandboxInstanceAllocationId(info.getAllocationUnitId());
        return trainingRunRepository.save(trainingRun);
    }

    /**
     * Returns active sandbox allocation units for the given user (OIDC sub).
     * Used for single-sandbox-per-user: at most one active sandbox per trainee.
     *
     * @param userSub OIDC sub of the trainee
     * @return list of active allocation units (may be empty)
     */
    public List<ActiveSandboxSummaryDTO> getActiveSandboxesForUser(String userSub) {
        if (userSub == null || userSub.isBlank()) {
            return List.of();
        }
        return sandboxApiService.listActiveAllocationUnitsByCreatorSub(userSub.trim());
    }

    /**
     * Fallback for single-sandbox-per-user: returns synthetic active-sandbox list from this user's
     * training runs that have an allocation (running, not finished). Includes runs where sandbox is
     * still building (sandbox_instance_ref_id null) so /user-active-sandboxes and access response
     * show the allocation and its stages.
     */
    public List<ActiveSandboxSummaryDTO> getActiveSandboxesFromRunsForUser(Long participantRefId) {
        if (participantRefId == null) {
            return List.of();
        }
        List<TrainingRun> runs = trainingRunRepository.findRunningWithAllocationByParticipantRefId(participantRefId);
        if (runs == null || runs.isEmpty()) {
            return List.of();
        }
        List<ActiveSandboxSummaryDTO> list = new java.util.ArrayList<>();
        for (TrainingRun run : runs) {
            Integer allocationId = run.getSandboxInstanceAllocationId();
            if (allocationId == null) continue;
            ActiveSandboxSummaryDTO dto = new ActiveSandboxSummaryDTO();
            dto.setId(allocationId);
            dto.setSandboxId(run.getSandboxInstanceRefId());
            dto.setPoolId(run.getTrainingInstance().getPoolId());
            dto.setAllowRemove(false);
            dto.setTrainingInstanceTitle(run.getTrainingInstance().getTitle());
            dto.setTrainingInstanceId(run.getTrainingInstance().getId());
            dto.setTrainingInstanceManaged(run.getTrainingInstance().isManaged());
            list.add(dto);
        }
        return list;
    }

    /**
     * Fills training_instance_title (and training_instance_id) for active sandboxes that don't have it,
     * by looking up a training run for this participant that has the sandbox.
     */
    public void enrichActiveSandboxesWithTrainingInstanceTitles(List<ActiveSandboxSummaryDTO> sandboxes, Long participantRefId) {
        if (sandboxes == null || participantRefId == null) return;
        for (ActiveSandboxSummaryDTO s : sandboxes) {
            if (s.getSandboxId() == null || s.getSandboxId().isBlank()) continue;
            if (s.getTrainingInstanceTitle() != null && !s.getTrainingInstanceTitle().isBlank()) continue;
            List<TrainingRun> runs = trainingRunRepository.findBySandboxInstanceRefId(s.getSandboxId());
            if (runs != null) {
                runs.stream()
                        .filter(r -> r.getParticipantRef() != null && participantRefId.equals(r.getParticipantRef().getUserRefId()))
                        .findFirst()
                        .ifPresent(r -> {
                            s.setTrainingInstanceTitle(r.getTrainingInstance().getTitle());
                            s.setTrainingInstanceId(r.getTrainingInstance().getId());
                            s.setTrainingInstanceManaged(r.getTrainingInstance().isManaged());
                        });
            }
        }
    }

    /**
     * Returns merged list of active sandboxes for the user (by-creator from sandbox-service + from runs fallback),
     * deduplicated by sandbox ID. Used to enforce single-sandbox-per-user before resume or new allocation.
     */
    public List<ActiveSandboxSummaryDTO> getMergedActiveSandboxesForUser(String userSub, Long participantRefId) {
        List<ActiveSandboxSummaryDTO> fromCreator = getActiveSandboxesForUser(userSub);
        List<ActiveSandboxSummaryDTO> fromRuns = getActiveSandboxesFromRunsForUser(participantRefId);
        java.util.Set<String> seenSandboxIds = new java.util.LinkedHashSet<>();
        java.util.Set<Integer> seenUnitIds = new java.util.LinkedHashSet<>();
        List<ActiveSandboxSummaryDTO> merged = new java.util.ArrayList<>();
        for (ActiveSandboxSummaryDTO s : fromCreator != null ? fromCreator : List.<ActiveSandboxSummaryDTO>of()) {
            if (s.getId() != null && seenUnitIds.add(s.getId())) {
                merged.add(s);
                if (s.getSandboxId() != null && !s.getSandboxId().isBlank()) {
                    seenSandboxIds.add(s.getSandboxId());
                }
            }
        }
        for (ActiveSandboxSummaryDTO s : fromRuns != null ? fromRuns : List.<ActiveSandboxSummaryDTO>of()) {
            if (s.getSandboxId() != null && !s.getSandboxId().isBlank() && !seenSandboxIds.contains(s.getSandboxId())) {
                seenSandboxIds.add(s.getSandboxId());
                if (s.getId() != null) seenUnitIds.add(s.getId());
                merged.add(s);
            } else if (s.getId() != null && seenUnitIds.add(s.getId())) {
                merged.add(s);
            }
        }
        return merged;
    }

    /**
     * Fetches allocation_request and cleanup_request (stages) from sandbox-service for any active sandbox that has an allocation unit id.
     * When the unit is gone (404), detaches the run and removes the item from the list so it does not appear in user-active-sandboxes.
     * Call this before enrichActiveSandboxesWithAllowRemove so allow_remove and stages are correct.
     *
     * @param sandboxes        list to enrich (modified in place; entries may be removed when allocation is 404)
     * @param participantRefId current user's participant ref id (used to detach run when unit is 404; may be null)
     */
    public void enrichActiveSandboxesWithAllocationRequest(List<ActiveSandboxSummaryDTO> sandboxes, Long participantRefId) {
        if (sandboxes == null) return;
        java.util.Iterator<ActiveSandboxSummaryDTO> it = sandboxes.iterator();
        while (it.hasNext()) {
            ActiveSandboxSummaryDTO s = it.next();
            if (s.getId() == null) continue;
            ActiveSandboxSummaryDTO unit = sandboxApiService.getAllocationUnitById(s.getId());
            if (unit == null) {
                // Allocation unit gone (e.g. 404); detach run so it disappears from list, remove from result
                detachRunsByAllocationId(participantRefId, s.getId());
                it.remove();
                continue;
            }
            if (s.getAllocationRequest() == null && unit.getAllocationRequest() != null) {
                s.setAllocationRequest(unit.getAllocationRequest());
            }
            if (s.getCleanupRequest() == null && unit.getCleanupRequest() != null) {
                s.setCleanupRequest(unit.getCleanupRequest());
            }
        }
    }

    /**
     * Sets allowRemove on each active sandbox: true when build completed (all FINISHED) or failed (any FAILED);
     * false when building (any RUNNING or IN_QUEUE). Frontend shows "Remove sandbox" only when allowRemove.
     */
    public void enrichActiveSandboxesWithAllowRemove(List<ActiveSandboxSummaryDTO> sandboxes) {
        if (sandboxes == null) return;
        for (ActiveSandboxSummaryDTO s : sandboxes) {
            s.setAllowRemove(computeAllowRemove(s));
        }
    }

    private static boolean computeAllowRemove(ActiveSandboxSummaryDTO s) {
        if (Boolean.TRUE.equals(s.getTrainingInstanceManaged())) {
            return false;
        }
        var cleanupReq = s.getCleanupRequest();
        if (cleanupReq != null && cleanupReq.getStages() != null && !cleanupReq.getStages().isEmpty()) {
            var stages = cleanupReq.getStages();
            boolean anyRunning = stages.stream().anyMatch(st -> "RUNNING".equalsIgnoreCase(st) || "IN_QUEUE".equalsIgnoreCase(st));
            if (anyRunning) return false;
            boolean anyFailed = stages.stream().anyMatch("FAILED"::equalsIgnoreCase);
            boolean allFinished = stages.stream().allMatch("FINISHED"::equalsIgnoreCase);
            if (anyFailed || allFinished) return true;
            return false;
        }
        var req = s.getAllocationRequest();
        if (req == null || req.getStages() == null || req.getStages().isEmpty()) {
            return s.getSandboxId() != null && !s.getSandboxId().isBlank();
        }
        var stages = req.getStages();
        boolean anyFailed = stages.stream().anyMatch("FAILED"::equalsIgnoreCase);
        boolean anyRunning = stages.stream().anyMatch(st -> "RUNNING".equalsIgnoreCase(st) || "IN_QUEUE".equalsIgnoreCase(st));
        return anyFailed || !anyRunning;
    }

    /**
     * True when the allocation has a sandbox_id and all allocation stages are FINISHED.
     * Used to avoid attaching or entering a run with a sandbox that is still deploying (topology would 404).
     */
    public static boolean isAllocationFullyReady(ActiveSandboxSummaryDTO s) {
        if (s == null || s.getSandboxId() == null || s.getSandboxId().isBlank()) {
            return false;
        }
        var req = s.getAllocationRequest();
        if (req == null || req.getStages() == null || req.getStages().isEmpty()) {
            return false;
        }
        return req.getStages().stream().allMatch("FINISHED"::equalsIgnoreCase);
    }

    private static final int ALLOCATION_POLL_ATTEMPTS = 60;
    private static final long ALLOCATION_POLL_INTERVAL_MS = 2000;

    /**
     * Starts sandbox allocation for the training run without waiting (single-sandbox-per-user).
     * Creates an allocation unit and attaches its id to the run; sandbox_id is set when allocation completes.
     * Caller should return a "stay on overview" response; user re-submits access code when ready to enter.
     */
    public TrainingRun startSandboxAllocationForTrainingRun(TrainingRun trainingRun, String userSub) {
        Long poolId = trainingRun.getTrainingInstance().getPoolId();
        if (poolId == null) {
            throw new BadRequestException("Training instance has no pool assigned.");
        }
        ActiveSandboxSummaryDTO unit = sandboxApiService.createAllocationUnitWithCreator(poolId, userSub);
        if (unit == null || unit.getId() == null) {
            throw new MicroserviceApiException(HttpStatus.BAD_GATEWAY, JavaApiError.of("Failed to create sandbox allocation unit for pool " + poolId));
        }
        trainingRun.setSandboxInstanceAllocationId(unit.getId());
        trainingRun.setSandboxInstanceRefId(null);
        return trainingRunRepository.save(trainingRun);
    }

    /**
     * If the run has an allocation id but no sandbox id yet, check if allocation is fully ready (all stages FINISHED) and attach if so.
     * @return true if sandbox was attached, false if still building or no allocation id
     */
    public boolean attachSandboxIfAllocationReady(TrainingRun trainingRun) {
        Integer allocationId = trainingRun.getSandboxInstanceAllocationId();
        if (allocationId == null) {
            return false;
        }
        ActiveSandboxSummaryDTO current = sandboxApiService.getAllocationUnitById(allocationId);
        if (current != null && isAllocationFullyReady(current)) {
            trainingRun.setSandboxInstanceRefId(current.getSandboxId());
            trainingRunRepository.save(trainingRun);
            return true;
        }
        return false;
    }

    /**
     * Attaches an existing allocation (with sandbox) to the given run. Used for managed instances
     * where the trainee uses a sandbox allocated by Admin instead of creating one.
     * Uses saveAndFlush so the unique constraint (training_run_one_allocation_per_instance) is
     * checked immediately; otherwise the violation would occur at commit and escape the retry loop.
     */
    public TrainingRun attachRunToAllocation(TrainingRun trainingRun, Integer allocationUnitId, String sandboxId) {
        if (allocationUnitId == null || sandboxId == null || sandboxId.isBlank()) {
            throw new BadRequestException("Allocation id and sandbox id are required.");
        }
        trainingRun.setSandboxInstanceAllocationId(allocationUnitId);
        trainingRun.setSandboxInstanceRefId(sandboxId);
        return trainingRunRepository.saveAndFlush(trainingRun);
    }

    /**
     * Same as {@link #attachRunToAllocation} but runs in a new transaction (REQUIRES_NEW).
     * Used by the managed-flow retry loop so that when the unique constraint is violated,
     * only this transaction rolls back and the outer transaction can continue (next retry).
     */
    @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
    public TrainingRun attachRunToAllocationInNewTransaction(Long trainingRunId, Integer allocationUnitId, String sandboxId) {
        if (allocationUnitId == null || sandboxId == null || sandboxId.isBlank()) {
            throw new BadRequestException("Allocation id and sandbox id are required.");
        }
        TrainingRun run = trainingRunRepository.findById(trainingRunId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId)));
        run.setSandboxInstanceAllocationId(allocationUnitId);
        run.setSandboxInstanceRefId(sandboxId);
        return trainingRunRepository.saveAndFlush(run);
    }

    /**
     * For managed instances: reserve one sandbox in the pool via get-and-lock (SELECT FOR UPDATE SKIP LOCKED).
     * Does not attach to any run; used when we need to check availability before creating a run.
     *
     * @param poolId      pool id (must be locked with accessToken)
     * @param accessToken training instance access token
     * @return sandbox info (id, allocationUnitId) if a free sandbox was locked, or empty if none
     */
    public Optional<SandboxInfo> tryReserveSandboxForManaged(Long poolId, String accessToken) {
        return sandboxApiService.getAndLockSandboxForManaged(poolId, accessToken);
    }

    /**
     * For managed instances: reserve one sandbox in the pool via get-and-lock (SELECT FOR UPDATE SKIP LOCKED)
     * and attach it to the given run. No allocation is created; sandbox must already exist in the pool.
     *
     * @param run         existing training run to attach
     * @param poolId      pool id (must be locked with accessToken)
     * @param accessToken training instance access token
     * @return the run with allocation attached, or empty if no free sandbox in the pool
     */
    public Optional<TrainingRun> reserveAndAttachSandboxForManaged(TrainingRun run, Long poolId, String accessToken) {
        return sandboxApiService.getAndLockSandboxForManaged(poolId, accessToken)
                .map(info -> attachRunToAllocationInNewTransaction(run.getId(), info.getAllocationUnitId(), info.getId()));
    }

    /**
     * Finds the first allocation in the given pool that belongs to the user (by-creator) and has a fully ready
     * sandbox (all allocation stages FINISHED), and is not yet attached to any run of this user.
     *
     * @param participantRefId user's participant ref id
     * @param poolId            instance pool id
     * @param userSub           user's OIDC sub
     * @return first free allocation in pool for this user, or empty if none
     */
    public Optional<ActiveSandboxSummaryDTO> findFirstFreeAllocationInPoolForUser(Long participantRefId, Long poolId, String userSub) {
        if (poolId == null || userSub == null || userSub.isBlank()) {
            return Optional.empty();
        }
        List<ActiveSandboxSummaryDTO> merged = getMergedActiveSandboxesForUser(userSub.trim(), participantRefId);
        if (merged == null) return Optional.empty();
        for (ActiveSandboxSummaryDTO s : merged) {
            if (!poolId.equals(s.getPoolId()) || s.getSandboxId() == null || s.getSandboxId().isBlank() || s.getId() == null) {
                continue;
            }
            List<TrainingRun> runsWithAllocation = trainingRunRepository.findRunningByParticipantRefIdAndAllocationId(participantRefId, s.getId());
            if (runsWithAllocation != null && !runsWithAllocation.isEmpty()) {
                continue;
            }
            ActiveSandboxSummaryDTO current = sandboxApiService.getAllocationUnitById(s.getId());
            if (current != null && isAllocationFullyReady(current)) {
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the first allocation in the given pool that has a fully ready sandbox (all allocation stages FINISHED)
     * and is not attached to any training run. Used for managed instances where sandboxes are allocated by Admin.
     *
     * @param poolId instance pool id
     * @return first free allocation in pool, or empty if none
     */
    public Optional<ActiveSandboxSummaryDTO> findFirstFreeAllocationInPoolForManaged(Long poolId) {
        return findFirstFreeAllocationInPoolForManaged(poolId, null);
    }

    /**
     * Same as {@link #findFirstFreeAllocationInPoolForManaged(Long)} but skips the allocation with the given id.
     * Use when retrying after a constraint violation so the next attempt picks a different allocation.
     *
     * @param poolId                instance pool id
     * @param excludeAllocationId   allocation id to skip (e.g. the one that was already attached by another request)
     * @return first free allocation in pool excluding the given id, or empty if none
     */
    public Optional<ActiveSandboxSummaryDTO> findFirstFreeAllocationInPoolForManaged(Long poolId, Integer excludeAllocationId) {
        if (poolId == null) {
            return Optional.empty();
        }
        List<ActiveSandboxSummaryDTO> units = sandboxApiService.listAllocationUnitsByPoolId(poolId);
        if (units == null) return Optional.empty();
        for (ActiveSandboxSummaryDTO s : units) {
            if (s.getId() == null || s.getSandboxId() == null || s.getSandboxId().isBlank()) {
                continue;
            }
            if (excludeAllocationId != null && excludeAllocationId.equals(s.getId())) {
                continue;
            }
            List<TrainingRun> runsWithAllocation = trainingRunRepository.findRunningByAllocationId(s.getId());
            if (runsWithAllocation != null && !runsWithAllocation.isEmpty()) {
                continue;
            }
            ActiveSandboxSummaryDTO current = sandboxApiService.getAllocationUnitById(s.getId());
            if (current != null && isAllocationFullyReady(current)) {
                return Optional.of(current);
            }
        }
        return Optional.empty();
    }

    /**
     * On-demand allocation of one sandbox for the training run (single-sandbox-per-user).
     * Creates an allocation unit with created_by_sub, polls until sandbox is ready, then attaches to run.
     *
     * @param trainingRun the run to attach the sandbox to
     * @param userSub     OIDC sub of the trainee
     * @return updated training run
     */
    public TrainingRun allocateSandboxForTrainingRun(TrainingRun trainingRun, String userSub) {
        Long poolId = trainingRun.getTrainingInstance().getPoolId();
        if (poolId == null) {
            throw new BadRequestException("Training instance has no pool assigned.");
        }
        ActiveSandboxSummaryDTO unit = sandboxApiService.createAllocationUnitWithCreator(poolId, userSub);
        if (unit == null || unit.getId() == null) {
            throw new MicroserviceApiException(HttpStatus.BAD_GATEWAY, JavaApiError.of("Failed to create sandbox allocation unit for pool " + poolId));
        }
        for (int i = 0; i < ALLOCATION_POLL_ATTEMPTS; i++) {
            ActiveSandboxSummaryDTO current = sandboxApiService.getAllocationUnitById(unit.getId());
            if (current != null && isAllocationFullyReady(current)) {
                trainingRun.setSandboxInstanceRefId(current.getSandboxId());
                trainingRun.setSandboxInstanceAllocationId(unit.getId());
                return trainingRunRepository.save(trainingRun);
            }
            try {
                Thread.sleep(ALLOCATION_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MicroserviceApiException(HttpStatus.INTERNAL_SERVER_ERROR, JavaApiError.of("Sandbox allocation interrupted."));
            }
        }
        throw new MicroserviceApiException(HttpStatus.GATEWAY_TIMEOUT, JavaApiError.of("Sandbox allocation did not complete in time. Please try again later."));
    }

    /**
     * Request cleanup of a sandbox owned by the trainee (single-sandbox-per-user).
     * Verifies ownership via training runs (this user's run has this sandbox) or sandbox-service created_by_sub.
     * If the sandbox was already removed by an admin (404 from sandbox-service), we just detach from runs.
     *
     * @param sandboxId        sandbox UUID
     * @param userSub          OIDC sub of the current user
     * @param participantRefId participant user ref id (to verify run ownership)
     * @throws ForbiddenException if the sandbox is not owned by the user
     */
    public void requestTraineeSandboxCleanup(String sandboxId, String userSub, Long participantRefId) {
        if (sandboxId == null || sandboxId.isBlank()) {
            throw new ForbiddenException("Invalid request.");
        }
        List<TrainingRun> runsWithSandbox = trainingRunRepository.findBySandboxInstanceRefId(sandboxId);
        java.util.List<TrainingRun> myRuns = runsWithSandbox == null ? List.of() : runsWithSandbox.stream()
                .filter(r -> r.getParticipantRef() != null && participantRefId != null && participantRefId.equals(r.getParticipantRef().getUserRefId()))
                .toList();
        Integer allocationUnitId = null;
        if (!myRuns.isEmpty()) {
            allocationUnitId = myRuns.get(0).getSandboxInstanceAllocationId();
        }
        if (allocationUnitId == null && (userSub != null && !userSub.isBlank())) {
            List<ActiveSandboxSummaryDTO> units = sandboxApiService.listAllocationUnitsByCreatorSub(userSub.trim(), false);
            ActiveSandboxSummaryDTO owned = units.stream()
                    .filter(u -> sandboxId.equals(u.getSandboxId()))
                    .findFirst()
                    .orElse(null);
            if (owned != null) {
                allocationUnitId = owned.getId();
            }
        }
        if (myRuns.isEmpty() && allocationUnitId == null) {
            throw new ForbiddenException("Sandbox not found or you do not own it.");
        }
        if (allocationUnitId != null) {
            try {
                sandboxApiService.requestCleanupForAllocationUnit(allocationUnitId);
            } catch (MicroserviceApiException e) {
                HttpStatus status = e.getStatusCode();
                if (status == HttpStatus.NOT_FOUND || status == HttpStatus.GONE) {
                    // Sandbox/allocation already removed by admin; detach so run disappears from active list
                    detachRunsByAllocationId(participantRefId, allocationUnitId);
                } else {
                    throw e;
                }
            }
        }
        // Do not detach here: keep allocation id on run so it stays in user-active-sandboxes and we can show cleanup stages
    }

    /**
     * Request cleanup by allocation unit id (when sandbox_id is null, e.g. building or already removed by admin).
     * Verifies the current user owns a run with this allocation id, then requests cleanup. Run stays in active list to show cleanup stages.
     */
    public void requestTraineeSandboxCleanupByAllocationId(Integer allocationUnitId, Long participantRefId) {
        if (allocationUnitId == null) {
            throw new ForbiddenException("Invalid request.");
        }
        List<TrainingRun> myRuns = trainingRunRepository.findRunningByParticipantRefIdAndAllocationId(participantRefId, allocationUnitId);
        if (myRuns == null || myRuns.isEmpty()) {
            throw new ForbiddenException("Allocation not found or you do not own it.");
        }
        try {
            sandboxApiService.requestCleanupForAllocationUnit(allocationUnitId);
        } catch (MicroserviceApiException e) {
            HttpStatus status = e.getStatusCode();
            if (status == HttpStatus.NOT_FOUND || status == HttpStatus.GONE) {
                detachRunsByAllocationId(participantRefId, allocationUnitId);
            } else {
                throw e;
            }
        }
        // Do not detach: keep allocation id so run stays in user-active-sandboxes and shows cleanup stages
    }

    /**
     * Detach allocation from runs that have this allocation unit id (for the given participant). Used when allocation is gone (404) or already removed.
     */
    public void detachRunsByAllocationId(Long participantRefId, Integer allocationUnitId) {
        if (participantRefId == null || allocationUnitId == null) return;
        List<TrainingRun> runs = trainingRunRepository.findRunningByParticipantRefIdAndAllocationId(participantRefId, allocationUnitId);
        if (runs == null) return;
        for (TrainingRun run : runs) {
            run.setSandboxInstanceRefId(null);
            run.setSandboxInstanceAllocationId(null);
            trainingRunRepository.save(run);
        }
    }

    /**
     * Verifies that the run's sandbox allocation still exists in the sandbox-service (e.g. not removed by admin).
     * If the allocation is gone (404) or no longer has a sandbox_id, detaches the sandbox from the run and returns false.
     * Call this when entering a training run so we never show a sandbox that is not there.
     *
     * @param trainingRun the run (must have trainingInstance loaded)
     * @return true if the run is still valid (sandbox exists, or local env, or no sandbox); false if we detached because sandbox was removed
     */
    public boolean verifySandboxStillExistsAndDetachIfNot(TrainingRun trainingRun) {
        if (trainingRun == null || trainingRun.getTrainingInstance() == null) {
            return true;
        }
        if (trainingRun.getTrainingInstance().isLocalEnvironment()) {
            return true;
        }
        Integer allocationId = trainingRun.getSandboxInstanceAllocationId();
        if (allocationId == null) {
            return true;
        }
        ActiveSandboxSummaryDTO unit = sandboxApiService.getAllocationUnitById(allocationId);
        if (unit != null && unit.getSandboxId() != null && !unit.getSandboxId().isBlank()) {
            return true;
        }
        trainingRun.setSandboxInstanceRefId(null);
        trainingRun.setSandboxInstanceAllocationId(null);
        trainingRunRepository.save(trainingRun);
        return false;
    }

    /**
     * Resume previously closed training run.
     *
     * @param trainingRunId id of training run to be resumed.
     * @return {@link TrainingRun}
     * @throws EntityNotFoundException training run is not found.
     */
    public TrainingRun resumeTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunRepository.findByIdWithLevelForResume(trainingRunId).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId)));
        validateResumeAndAuditResumed(trainingRun, trainingRunId);
        return trainingRun;
    }

    /**
     * Load training run by id in a new transaction so we see the latest committed state (e.g. after attach in REQUIRES_NEW).
     * Used by managed flow after attach so we don't use a stale run from the persistence context.
     */
    @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
    public TrainingRun findByIdWithLevelForResumeInNewTransaction(Long trainingRunId) {
        return trainingRunRepository.findByIdWithLevelForResume(trainingRunId).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId)));
    }

    /**
     * Same validation and audit as resumeTrainingRun, for an already-loaded run (e.g. after loading in a new transaction).
     */
    public void validateResumeAndAuditResumed(TrainingRun trainingRun, Long trainingRunId) {
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingRun.getState().equals(TRState.FINISHED) || trainingRun.getState().equals(TRState.ARCHIVED)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot resume finished training run."));
        }
        if (trainingInstance.getEndTime().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot resume training run after end of training instance."));
        }
        if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "The pool assignment of the appropriate training instance has been probably canceled. Please contact the organizer."));
        }
        if (!trainingInstance.isLocalEnvironment() && trainingRun.getSandboxInstanceRefId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Sandbox of this training run was already deleted, you have to start new training."));
        }
        auditEventsService.auditTrainingRunResumedAction(trainingRun);
    }

    /**
     * Returns true if the given user has at least one running (not finished/archived) training run that uses this sandbox.
     * Used by sandbox-service to verify a trainee may access a specific sandbox (e.g. topology) when pool is locked.
     *
     * @param userRefId            participant user ref id (from JWT)
     * @param sandboxInstanceRefId sandbox UUID
     * @return true if user has an active run with this sandbox
     */
    public boolean hasActiveRunWithSandboxForUser(Long userRefId, String sandboxInstanceRefId) {
        if (userRefId == null || sandboxInstanceRefId == null || sandboxInstanceRefId.isBlank()) {
            return false;
        }
        return trainingRunRepository.existsRunningRunByUserRefIdAndSandboxInstanceRefId(userRefId, sandboxInstanceRefId.trim());
    }

    /**
     * Check given answer of given Training Run.
     *
     * @param runId  id of Training Run to check answer.
     * @param answer string which player submit.
     * @return true if answer is correct, false if answer is wrong.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException     the current level of training run is not training level.
     */
    public boolean isCorrectAnswer(Long runId, String answer) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level.getClass() != TrainingLevel.class) {
            throw new BadRequestException("Current level is not training level and does not have answer.");
        } else if (trainingRun.isLevelAnswered()) {
                throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", Long.class, runId, "The answer of the current level of training run has been already corrected."));
        }
        return evaluateTrainingLevelAnswer(trainingRun, answer);
    }

    private boolean evaluateTrainingLevelAnswer(TrainingRun trainingRun, String answer) {
        TrainingLevel trainingLevel = (TrainingLevel) trainingRun.getCurrentLevel();
        String correctAnswer = getTrainingLevelCorrectAnswer(trainingLevel, trainingRun);
        if (correctAnswer.equals(answer)) {
            trainingRun.setLevelAnswered(true);
            trainingRun.increaseTotalTrainingScore(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty());
            auditEventsService.auditCorrectAnswerSubmittedAction(trainingRun, answer);
            auditEventsService.auditLevelCompletedAction(trainingRun);
            auditSubmission(trainingRun, SubmissionType.CORRECT, answer);
            return true;
        } else if (trainingRun.getIncorrectAnswerCount() != trainingLevel.getIncorrectAnswerLimit()) {
            trainingRun.setIncorrectAnswerCount(trainingRun.getIncorrectAnswerCount() + 1);
        }
        auditSubmission(trainingRun, SubmissionType.INCORRECT, answer);
        auditEventsService.auditWrongAnswerSubmittedAction(trainingRun, answer);
        return false;
    }

    /**
     * Check given passkey of given Training Run.
     *
     * @param runId  id of Training Run to check passkey.
     * @param passkey string which player submit.
     * @return true if passkey is correct, false if passkey is wrong.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException     the current level of training run is not access level.
     */
    public boolean isCorrectPassKey(Long runId, String passkey) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level.getClass() != AccessLevel.class) {
            throw new BadRequestException("Current level is not access level and does not have passkey.");
        } else if (trainingRun.isLevelAnswered()) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", Long.class, runId, "The passkey of the current level of training run has been already corrected."));
        }
        return evaluateAccessLevelPasskey(trainingRun, passkey);
    }

    private boolean evaluateAccessLevelPasskey(TrainingRun trainingRun, String passkey) {
        AccessLevel accessLevel = (AccessLevel) trainingRun.getCurrentLevel();
        if (accessLevel.getPasskey().equals(passkey)) {
            trainingRun.setLevelAnswered(true);
            auditEventsService.auditCorrectPasskeySubmittedAction(trainingRun, passkey);
            auditEventsService.auditLevelCompletedAction(trainingRun);
            return true;
        }
        auditEventsService.auditWrongPasskeySubmittedAction(trainingRun, passkey);
        return false;
    }

    private void auditSubmission(TrainingRun trainingRun, SubmissionType submissionType, String answer) {
        Submission submission = new Submission();
        submission.setDate(LocalDateTime.now(Clock.systemUTC()));
        submission.setLevel(trainingRun.getCurrentLevel());
        submission.setTrainingRun(trainingRun);
        submission.setProvided(answer);
        submission.setType(submissionType);
        submission.setIpAddress(getUserIpAddress());
        submissionRepository.save(submission);
    }

    private String getUserIpAddress() {
        ServletRequestAttributes requestAttributes =  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (requestAttributes != null && requestAttributes.getRequest().getHeader(X_REAL_IP_HEADER) != null) {
            return requestAttributes.getRequest().getHeader(X_REAL_IP_HEADER);
        }
        return "";
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
        if (level instanceof TrainingLevel) {
            if (trainingRun.isSolutionTaken()) {
                return 0;
            }
            return ((TrainingLevel) level).getIncorrectAnswerLimit() - trainingRun.getIncorrectAnswerCount();
        }
        throw new BadRequestException("Current level is not training level and does not have answer.");
    }

    /**
     * Gets solution of current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets solution for.
     * @return solution of current level.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException     the current level of training run is not training level.
     */
    public String getSolution(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof TrainingLevel trainingLevel) {
            if (!trainingRun.isSolutionTaken()) {
                trainingRun.setSolutionTaken(true);
                trainingRun.addSolutionInfo(new SolutionInfo(trainingLevel.getId(), trainingLevel.getSolution()));
                if (trainingLevel.isSolutionPenalized()) {
                    trainingRun.setCurrentPenalty(trainingRun.getMaxLevelScore());
                }

                trainingRunRepository.save(trainingRun);
                auditEventsService.auditSolutionDisplayedAction(trainingRun);
            }
            return getSolutionWithReplacedVariable(trainingLevel, trainingRun);
        } else {
            throw new BadRequestException("Current level is not training level and does not have solution.");
        }
    }

    private String getSolutionWithReplacedVariable(TrainingLevel trainingLevel, TrainingRun trainingRun) {
        if(!trainingLevel.getSolution().contains("${ANSWER}")) {
            return trainingLevel.getSolution();
        }
        return trainingLevel.getSolution().replaceAll("\\$\\{ANSWER\\}", getTrainingLevelCorrectAnswer(trainingLevel, trainingRun));
    }

    /**
     * Gets correct answer of the training level based on the Training Run parameters.
     *
     * @param trainingLevel Training Level whose correct answer to get.
     * @param trainingRun Training Run of the particular trainee used to obtain variant answer
     * @return static or variant answer based on the Training Run parameters
     */
    public String getTrainingLevelCorrectAnswer(TrainingLevel trainingLevel, TrainingRun trainingRun) {
        if (trainingLevel.isVariantAnswers()) {
            return trainingRun.getTrainingInstance().isLocalEnvironment() ?
                    answersStorageApiService.getCorrectAnswerByLocalSandboxIdAndVariableName(trainingRun.getTrainingInstance().getAccessToken(),
                        trainingRun.getParticipantRef().getUserRefId(), trainingLevel.getAnswerVariableName()) :
                    answersStorageApiService.getCorrectAnswerByCloudSandboxIdAndVariableName(trainingRun.getSandboxInstanceRefId(), trainingLevel.getAnswerVariableName());
        }
        return trainingLevel.getAnswer();
    }

    /**
     * Returns the list of correct answer strings for an assessment question (from the question definition).
     * Used when exporting correct answers for assessment levels.
     *
     * @param question the question (with choices and/or extendedMatchingStatements loaded)
     * @return list of correct answer strings; for EMI each element is "statementText -> optionText"
     */
    public List<String> getCorrectAnswersForQuestion(Question question) {
        if (question == null) {
            return List.of();
        }
        if (question.getQuestionType() == QuestionType.MCQ) {
            return question.getChoices().stream()
                    .filter(QuestionChoice::isCorrect)
                    .map(QuestionChoice::getText)
                    .collect(Collectors.toList());
        }
        if (question.getQuestionType() == QuestionType.FFQ) {
            return question.getChoices().stream()
                    .map(QuestionChoice::getText)
                    .collect(Collectors.toList());
        }
        if (question.getQuestionType() == QuestionType.EMI) {
            return question.getExtendedMatchingStatements().stream()
                    .map(s -> s.getText() + " -> " + (s.getExtendedMatchingOption() != null ? s.getExtendedMatchingOption().getText() : ""))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * Loads an assessment level with its questions (for use when building correct-answers export).
     *
     * @param levelId assessment level id
     * @return the assessment level with questions loaded, or empty if not found
     */
    public Optional<AssessmentLevel> getAssessmentLevelWithQuestions(Long levelId) {
        return assessmentLevelRepository.findByIdWithQuestions(levelId);
    }

    /**
     * Gets hint of given current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @param hintId        id of hint to be returned.
     * @return {@link Hint}
     * @throws EntityNotFoundException training run or hint is not found.
     * @throws BadRequestException     the current level of training run is not training level.
     */
    public Hint getHint(Long trainingRunId, Long hintId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof TrainingLevel) {
            Hint hint = hintRepository.findById(hintId)
                    .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Hint.class, "id", hintId.getClass(), hintId,
                            "Hint not found.")));
            if (hint.getTrainingLevel().getId().equals(level.getId())) {
                trainingRun.increaseCurrentPenalty(hint.getHintPenalty());
                trainingRun.addHintInfo(new HintInfo(level.getId(), hint.getId(), hint.getTitle(), hint.getContent(), hint.getOrder()));
                auditEventsService.auditHintTakenAction(trainingRun, hint);
                return hint;
            }
            throw new EntityConflictException(new EntityErrorDetail(Hint.class, "id", hintId.getClass(), hintId,
                    "Hint is not in current level of training run: " + trainingRunId + "."));
        } else {
            throw new BadRequestException("Current level is not training level and does not have hints.");
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
    public TrainingRun finishTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        int maxOrder = abstractLevelRepository.getCurrentMaxOrder(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        if (trainingRun.getCurrentLevel().getOrder() != maxOrder) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot finish training run because current level is not last."));
        }
        if (!(trainingRun.getCurrentLevel() instanceof InfoLevel) && !trainingRun.isLevelAnswered()) {
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
        return trainingRun;
    }

    /**
     * Archive training run.
     *
     * @param trainingRunId id of training run to be archived.
     * @throws EntityNotFoundException training run is not found.
     */
    public void archiveTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = findById(trainingRunId);
        trainingRun.setState(TRState.ARCHIVED);
        trainingRun.setPreviousSandboxInstanceRefId(trainingRun.getSandboxInstanceRefId());
        trainingRun.setSandboxInstanceRefId(null);
        trainingRun.setSandboxInstanceAllocationId(null);
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
        trainingRunRepository.save(trainingRun);
    }

    /**
     * Check if run event logging works
     *
     * @param run run to check
     * @return resulting boolean
     */
    public boolean checkRunEventLogging(TrainingRun run) {
        return !elasticsearchApiService.findAllEventsFromTrainingRun(run).isEmpty();
    }

    /**
     * Check if run command logging works
     *
     * @param run run to check
     * @return resulting boolean
     */
    public boolean checkRunCommandLogging(TrainingRun run) {
        List<Map<String, Object>> runCommands;
        if (run.getTrainingInstance().isLocalEnvironment()) {
            String accessToken = run.getTrainingInstance().getAccessToken();
            Long userId = run.getParticipantRef().getUserRefId();
            runCommands = elasticsearchApiService.findAllConsoleCommandsByAccessTokenAndUserId(accessToken, userId);
        } else {
            String sandboxId = run.getSandboxInstanceRefId() == null ? run.getPreviousSandboxInstanceRefId() : run.getSandboxInstanceRefId();
            runCommands = elasticsearchApiService.findAllConsoleCommandsBySandbox(sandboxId);
        }

        return !runCommands.isEmpty();
    }

    /**
     * Evaluate and store responses to assessment.
     *
     * @param trainingRunId      id of training run to be finished.
     * @param answersToQuestions response to assessment to be evaluated
     * @throws EntityNotFoundException training run is not found.
     */
    public void evaluateResponsesToAssessment(Long trainingRunId, Map<Long, QuestionAnswerDTO> answersToQuestions) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        if (!(trainingRun.getCurrentLevel() instanceof AssessmentLevel)) {
            throw new BadRequestException("Current level is not assessment level and cannot be evaluated.");
        }
        if (trainingRun.isLevelAnswered())
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Current level of the training run has been already answered."));
        List<QuestionAnswer> userAnswersToQuestions;
        if (((AssessmentLevel) trainingRun.getCurrentLevel()).getAssessmentType() == AssessmentType.TEST) {
            userAnswersToQuestions = this.gatherAndEvaluateAnswers(trainingRun, answersToQuestions);
        } else {
            userAnswersToQuestions = this.gatherAnswers(trainingRun, answersToQuestions);
        }
        trainingRun.setLevelAnswered(true);
        questionAnswerRepository.saveAll(userAnswersToQuestions);
        auditEventsService.auditAssessmentAnswersAction(trainingRun, userAnswersToQuestions.toString());
        auditEventsService.auditLevelCompletedAction(trainingRun);
    }

    private List<QuestionAnswer> gatherAndEvaluateAnswers(TrainingRun trainingRun, Map<Long, QuestionAnswerDTO> answersToQuestions) {
        int score = 0;
        List<QuestionAnswer> userAnswersToQuestions = new ArrayList<>();
        for (Question question : ((AssessmentLevel) trainingRun.getCurrentLevel()).getQuestions()) {
            QuestionAnswerDTO questionAnswerDTO = answersToQuestions.get(question.getId());
            if (questionAnswerDTO == null) {
                throw new BadRequestException("The question '" + question.getText() + "' must be answered.");
            }
            userAnswersToQuestions.add(this.createQuestionAnswer(question, trainingRun, answersToQuestions.get(question.getId())));
            switch (question.getQuestionType()) {
                case MCQ:
                    score += evaluateMCQ(question, answersToQuestions.get(question.getId()));
                    break;
                case FFQ:
                    score += evaluateFFQ(question, answersToQuestions.get(question.getId()));
                    break;
                case EMI:
                    score += evaluateEMI(question, answersToQuestions.get(question.getId()));
                    break;
                default:
                    break;
            }
        }
        trainingRun.setCurrentPenalty(trainingRun.getMaxLevelScore() - score);
        trainingRun.increaseTotalAssessmentScore(score);
        return userAnswersToQuestions;
    }

    private List<QuestionAnswer> gatherAnswers(TrainingRun trainingRun, Map<Long, QuestionAnswerDTO> answersToQuestions) {
        List<QuestionAnswer> userAnswersToQuestions = new ArrayList<>();
        for (Question question : ((AssessmentLevel) trainingRun.getCurrentLevel()).getQuestions()) {
            QuestionAnswerDTO questionAnswerDTO = answersToQuestions.get(question.getId());
            if (questionAnswerDTO != null) {
                userAnswersToQuestions.add(this.createQuestionAnswer(question, trainingRun, questionAnswerDTO));
            } else if (question.isAnswerRequired()) {
                throw new BadRequestException("The question '" + question.getText() + "' must be answered.");
            }
        }
        return userAnswersToQuestions;
    }

    private QuestionAnswer createQuestionAnswer(Question question, TrainingRun trainingRun, QuestionAnswerDTO answersToQuestion) {
        QuestionAnswer questionAnswer = new QuestionAnswer(question, trainingRun);
        if (question.getQuestionType() == QuestionType.EMI) {
            Set<String> answers = question.getExtendedMatchingStatements().stream()
                    .filter(statement -> answersToQuestion.getExtendedMatchingPairs().containsKey(statement.getOrder()))
                    .map(statement -> "{ \"statementOrder\": " + statement.getOrder() + ", \"optionOrder\": " + answersToQuestion.getExtendedMatchingPairs().get(statement.getOrder()) + " }")
                    .collect(Collectors.toSet());
            questionAnswer.setAnswers(answers);
        } else {
            questionAnswer.setAnswers(answersToQuestion.getAnswers());
        }
        return questionAnswer;
    }

    private int evaluateFFQ(Question question, QuestionAnswerDTO userAnswer) {
        List<String> correctAnswers = question.getChoices().stream()
                .map(QuestionChoice::getText)
                .toList();
        return correctAnswers.containsAll(userAnswer.getAnswers()) ? question.getPoints() : (-1) * question.getPenalty();
    }

    private int evaluateMCQ(Question question, QuestionAnswerDTO userAnswer) {
        List<String> correctAnswers = question.getChoices().stream()
                .filter(QuestionChoice::isCorrect)
                .map(QuestionChoice::getText)
                .toList();
        return userAnswer.getAnswers().size() == correctAnswers.size() &&
                userAnswer.getAnswers().containsAll(correctAnswers) ? question.getPoints() : (-1) * question.getPenalty();
    }

    private int evaluateEMI(Question question, QuestionAnswerDTO userAnswer) {
        for (ExtendedMatchingStatement extendedMatchingStatement : question.getExtendedMatchingStatements()) {
            int expectedOptionOrder = extendedMatchingStatement.getExtendedMatchingOption().getOrder();
            int answeredOptionOrder = userAnswer.getExtendedMatchingPairs().get(extendedMatchingStatement.getOrder());
            if (expectedOptionOrder != answeredOptionOrder) {
                return (-1) * question.getPenalty();
            }
        }
        return question.getPoints();
    }

    public void auditRunHasDetectionEvent(TrainingRun run) {
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
    }

    public List<QuestionAnswer> getQuestionAnswersByTrainingRunId(Long runId) {
        return questionAnswerRepository.getAllByTrainingRunId(runId);
    }


}
