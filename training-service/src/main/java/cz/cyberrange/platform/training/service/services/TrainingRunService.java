package cz.cyberrange.platform.training.service.services;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.TooManyRequestsException;
import cz.cyberrange.platform.training.api.responses.SandboxInfo;
import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.opensearch.events.training.model.AnswerSelection;
import cz.cyberrange.platform.training.opensearch.events.training.model.EventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.ExtendedMatchingEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.FreeFormEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.MultipleChoiceEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
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
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TRAcquisitionLockRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Business logic behind a trainee's progression through a training run: level access and
 * completion, answer and passkey evaluation, hints, solutions, assessment scoring, and lifecycle
 * transitions between running, finished and archived.
 */
@Service
public class TrainingRunService {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingRunService.class);
  private static final String X_REAL_IP_HEADER = "x-real-ip";

  private final TrainingRunRepository trainingRunRepository;
  private final AbstractLevelRepository abstractLevelRepository;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final UserRefRepository participantRefRepository;
  private final HintRepository hintRepository;
  private final AuditEventsService auditEventsService;
  private final AnswersStorageApiService answersStorageApiService;
  private final SecurityService securityService;
  private final TRAcquisitionLockRepository trAcquisitionLockRepository;
  private final QuestionAnswerRepository questionAnswerRepository;
  private final SandboxApiService sandboxApiService;
  private final SubmissionRepository submissionRepository;
  private final CommandEventsService commandEventsService;
  private final TrainingEventsService trainingEventsService;

  /**
   * Creates the service with the repositories and collaborators it uses to drive a training run's
   * progression, evaluation and lifecycle
   */
  @Autowired
  public TrainingRunService(
      TrainingRunRepository trainingRunRepository,
      AbstractLevelRepository abstractLevelRepository,
      TrainingInstanceRepository trainingInstanceRepository,
      UserRefRepository participantRefRepository,
      HintRepository hintRepository,
      AuditEventsService auditEventsService,
      AnswersStorageApiService answersStorageApiService,
      SecurityService securityService,
      QuestionAnswerRepository questionAnswerRepository,
      SandboxApiService sandboxApiService,
      TRAcquisitionLockRepository trAcquisitionLockRepository,
      SubmissionRepository submissionRepository,
      CommandEventsService commandEventsService,
      TrainingEventsService trainingEventsService) {
    this.trainingRunRepository = trainingRunRepository;
    this.abstractLevelRepository = abstractLevelRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.participantRefRepository = participantRefRepository;
    this.hintRepository = hintRepository;
    this.auditEventsService = auditEventsService;
    this.answersStorageApiService = answersStorageApiService;
    this.securityService = securityService;
    this.questionAnswerRepository = questionAnswerRepository;
    this.sandboxApiService = sandboxApiService;
    this.trAcquisitionLockRepository = trAcquisitionLockRepository;
    this.submissionRepository = submissionRepository;
    this.commandEventsService = commandEventsService;
    this.trainingEventsService = trainingEventsService;
  }

  /**
   * Finds a training run by its primary key, with its participant reference and training instance
   * loaded eagerly. The current level is not loaded and reads lazily.
   *
   * @param runId the training run's primary key
   * @return the matching {@link TrainingRun}
   * @throws EntityNotFoundException when no training run carries that id
   */
  public TrainingRun findById(Long runId) {
    return trainingRunRepository
        .findById(runId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId)));
  }

  /**
   * Finds a training run by its primary key, with its current level, training instance and that
   * instance's training definition loaded eagerly. Takes a pessimistic write lock on the row for
   * the rest of the transaction.
   *
   * @param runId the training run's primary key
   * @return the matching {@link TrainingRun}
   * @throws EntityNotFoundException when no training run carries that id
   */
  public TrainingRun findByIdWithLevel(Long runId) {
    return trainingRunRepository
        .findByIdWithLevel(runId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId)));
  }

  /**
   * Finds the training runs matching the given predicate, with each run's participant reference
   * loaded eagerly.
   *
   * @param predicate the filter applied to the query
   * @param pageable the requested page
   * @return the matching page of {@link TrainingRun}s
   */
  public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
    return trainingRunRepository.findAll(predicate, pageable);
  }

  /**
   * Deletes a training run, its question answers, its submissions and its acquisition lock.
   * Optionally deletes its recorded command and training event data from OpenSearch.
   *
   * @param trainingRunId the training run to delete
   * @param forceDelete when false, refuses to delete a run whose state is {@link TRState#RUNNING}
   * @param deleteDataFromOpenSearch whether to also delete the run's command and training event
   *     data from OpenSearch
   * @return the deleted {@link TrainingRun}
   * @throws EntityNotFoundException when no training run carries that id
   * @throws EntityConflictException when the run is running and {@code forceDelete} is false
   */
  public TrainingRun deleteTrainingRun(
      Long trainingRunId, boolean forceDelete, boolean deleteDataFromOpenSearch) {
    TrainingRun trainingRun = findById(trainingRunId);
    if (!forceDelete && trainingRun.getState().equals(TRState.RUNNING)) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRun.getId().getClass(),
              trainingRun.getId(),
              "Cannot delete training run that is running. Consider force delete."));
    }
    questionAnswerRepository.deleteAllByTrainingRunId(trainingRunId);
    submissionRepository.deleteAllByTrainingRunId(trainingRunId);
    if (deleteDataFromOpenSearch) {
      deleteDataFromOpenSearch(trainingRun);
    }
    trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(
        trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
    trainingRunRepository.delete(trainingRun);
    return trainingRun;
  }

  /**
   * Deletes the OpenSearch command data of a training run's sandbox, falling back to its previous
   * sandbox reference when no current one is set, and deletes the run's training event data.
   */
  private void deleteDataFromOpenSearch(TrainingRun trainingRun) {
    String sandboxId =
        trainingRun.getSandboxInstanceRefId() == null
            ? trainingRun.getPreviousSandboxInstanceRefId()
            : trainingRun.getSandboxInstanceRefId();
    commandEventsService.deleteCommandsBySandbox(sandboxId);

    trainingEventsService.deleteEventsFromTrainingRun(
        trainingRun.getTrainingInstance().getId(), trainingRun.getId());
  }

  /**
   * Checks whether any training run exists for the given training instance.
   *
   * @param trainingInstanceId the training instance's primary key
   * @return true when at least one training run belongs to that instance
   */
  public boolean existsAnyForTrainingInstance(Long trainingInstanceId) {
    return trainingRunRepository.existsAnyForTrainingInstance(trainingInstanceId);
  }

  /**
   * Finds the training runs, additionally narrowed by the given predicate, whose participant is the
   * currently authenticated user, matched by the cross-service user reference id.
   *
   * @param predicate the filter applied to the query
   * @param pageable the requested page
   * @return the matching page of {@link TrainingRun}s
   */
  public Page<TrainingRun> findAllByParticipantRefUserRefId(
      Predicate predicate, Pageable pageable) {
    return trainingRunRepository.findAllByParticipantRefId(
        securityService.getUserRefIdFromUserAndGroup(), predicate, pageable);
  }

  /**
   * Finds every training run of the given training instance, with each run's participant reference
   * loaded eagerly. The result carries no guaranteed order.
   *
   * @param trainingInstanceId the training instance's primary key
   * @return the matching {@link TrainingRun}s, or an empty set if none exist
   */
  public Set<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId) {
    return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId);
  }

  /**
   * Finds all Training Runs by their ids.
   *
   * @param ids the training run ids
   * @return the list of {@link TrainingRun}
   */
  public List<TrainingRun> findAllByIds(List<Long> ids) {
    return trainingRunRepository.findAllById(ids);
  }

  /**
   * Advances a training run to the next level of its training definition, in level order, and
   * resets its incorrect answer count. Audits the outgoing level as completed only when it is an
   * {@link InfoLevel}, then audits the new current level as started.
   *
   * @param runId the training run's primary key
   * @return the {@link TrainingRun} with its new current level
   * @throws EntityNotFoundException when the training run does not exist, or its current level is
   *     already the last one of the training definition
   * @throws EntityConflictException when the current level has not been answered yet
   */
  public TrainingRun moveToNextLevel(Long runId) {
    TrainingRun trainingRun = findByIdWithLevel(runId);
    int currentLevelOrder = trainingRun.getCurrentLevel().getOrder();
    int maxLevelOrder =
        abstractLevelRepository.getCurrentMaxOrder(
            trainingRun.getCurrentLevel().getTrainingDefinition().getId());
    if (!trainingRun.isLevelAnswered()) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              runId.getClass(),
              runId,
              "You need to answer the level to move to the next level."));
    }
    if (currentLevelOrder == maxLevelOrder) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              AbstractLevel.class,
              "There is no next level for current training run (ID: " + runId + ")."));
    }
    List<AbstractLevel> levels =
        abstractLevelRepository.findAllLevelsByTrainingDefinitionId(
            trainingRun.getCurrentLevel().getTrainingDefinition().getId());
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
   * Finds a level of the training run's own training definition that the run has already reached,
   * meaning its order does not exceed that of the run's current level.
   *
   * @param runId the training run's primary key
   * @param levelId the primary key of the level to retrieve
   * @return the matching {@link AbstractLevel}
   * @throws EntityNotFoundException when the training run or the level does not exist
   * @throws EntityConflictException when the level does not belong to the run's training
   *     definition, or its order exceeds that of the run's current level
   */
  public AbstractLevel getVisitedLevel(Long runId, Long levelId) {
    TrainingRun trainingRun = findByIdWithLevel(runId);
    AbstractLevel abstractLevel =
        abstractLevelRepository
            .findById(levelId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            AbstractLevel.class,
                            "id",
                            levelId.getClass(),
                            levelId,
                            "Level not found")));
    TrainingDefinition trainingRunDefinition =
        trainingRun.getTrainingInstance().getTrainingDefinition();
    if (!abstractLevel.getTrainingDefinition().getId().equals(trainingRunDefinition.getId())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              "Requested level (ID: "
                  + levelId
                  + ") is not part of the training run (ID: "
                  + runId
                  + ")."));
    }
    if (abstractLevel.getOrder() > trainingRun.getCurrentLevel().getOrder()) {
      throw new EntityConflictException(
          new EntityErrorDetail("Requested level (ID: " + levelId + ") hasn't been visited yet"));
    }
    return abstractLevel;
  }

  /**
   * Finds the training runs of the given training definition whose participant is the currently
   * authenticated user, matched by the cross-service user reference id.
   *
   * @param definitionId the training definition's primary key
   * @param pageable the requested page
   * @return the matching page of {@link TrainingRun}s
   */
  public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(
      Long definitionId, Pageable pageable) {
    return trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantUserRefId(
        definitionId, securityService.getUserRefIdFromUserAndGroup(), pageable);
  }

  /**
   * Finds every training run of the given training definition.
   *
   * @param definitionId the training definition's primary key
   * @param pageable the requested page
   * @return the matching page of {@link TrainingRun}s
   */
  public Page<TrainingRun> findAllByTrainingDefinition(Long definitionId, Pageable pageable) {
    return trainingRunRepository.findAllByTrainingDefinitionId(definitionId, pageable);
  }

  /**
   * Gets every level of the given training definition, ordered by level order.
   *
   * @param definitionId the training definition's primary key
   * @return the training definition's {@link AbstractLevel}s
   */
  public List<AbstractLevel> getLevels(Long definitionId) {
    return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
  }

  /**
   * Creates a new, running training run on the given training instance's first level, for the
   * participant carrying the given cross-service user reference id. Creates that participant's
   * local {@link UserRef} row if it does not already exist.
   *
   * @param trainingInstance the training instance the run belongs to
   * @param participantRefId the cross-service user reference id of the participant
   * @return the newly created {@link TrainingRun}
   * @throws EntityNotFoundException when the training definition has no starting level
   */
  public TrainingRun createTrainingRun(TrainingInstance trainingInstance, Long participantRefId) {
    AbstractLevel initialLevel =
        findFirstLevelForTrainingRun(trainingInstance.getTrainingDefinition().getId());
    TrainingRun trainingRun =
        getNewTrainingRun(
            initialLevel,
            trainingInstance,
            LocalDateTime.now(Clock.systemUTC()),
            trainingInstance.getEndTime(),
            participantRefId);
    return trainingRunRepository.save(trainingRun);
  }

  /**
   * Audits the training run as started, followed by its current level as started.
   *
   * @param trainingRun the training run that was started
   */
  public void auditTrainingRunStarted(TrainingRun trainingRun) {
    auditEventsService.auditTrainingRunStartedAction(trainingRun);
    auditEventsService.auditLevelStartedAction(trainingRun);
  }

  /**
   * Finds the training run of the given training instance access token whose participant carries
   * the given cross-service user reference id, has a sandbox assigned and is not finished. Loads
   * the run's training instance, participant reference and current level eagerly.
   *
   * @param accessToken the training instance access token
   * @param participantRefId the cross-service user reference id of the participant
   * @return the matching {@link TrainingRun}, empty when none matches
   */
  public Optional<TrainingRun> findRunningTrainingRunOfUser(
      String accessToken, Long participantRefId) {
    return trainingRunRepository.findRunningTrainingRunOfUser(accessToken, participantRefId);
  }

  /**
   * Finds the training instance carrying the given access token whose start time is in the past and
   * whose end time is in the future, with its training definition loaded eagerly.
   *
   * @param accessToken the training instance access token
   * @return the matching {@link TrainingInstance}
   * @throws EntityNotFoundException when no training instance is currently active for that token
   * @throws EntityConflictException when the instance is not local and has no sandbox pool
   *     allocated
   */
  public TrainingInstance getTrainingInstanceForParticularAccessToken(String accessToken) {
    TrainingInstance trainingInstance =
        trainingInstanceRepository
            .findByStartTimeAfterAndEndTimeBeforeAndAccessToken(
                LocalDateTime.now(Clock.systemUTC()), accessToken)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            TrainingInstance.class,
                            "accessToken",
                            accessToken.getClass(),
                            accessToken,
                            "There is no active training session matching access token.")));
    if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() == null) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              trainingInstance.getId().getClass(),
              trainingInstance.getId(),
              "At first organizer must allocate sandboxes for training instance."));
    }
    return trainingInstance;
  }

  /**
   * Inserts an acquisition lock row for the given participant and training instance, in a new,
   * independent transaction. The row's unique constraint rejects a second concurrent lock for the
   * same participant and instance.
   *
   * @param participantRefId the cross-service user reference id of the participant
   * @param trainingInstanceId the training instance's primary key
   * @param accessToken the training instance access token, carried only in the failure message
   * @throws TooManyRequestsException when a lock already exists for that participant and instance
   */
  @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
  public void trAcquisitionLockToPreventManyRequestsFromSameUser(
      Long participantRefId, Long trainingInstanceId, String accessToken) {
    try {
      trAcquisitionLockRepository.saveAndFlush(
          new TRAcquisitionLock(
              participantRefId, trainingInstanceId, LocalDateTime.now(Clock.systemUTC())));
    } catch (DataIntegrityViolationException ex) {
      throw new TooManyRequestsException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "accessToken",
              accessToken.getClass(),
              accessToken,
              "Training run has been already accessed and cannot be created again. Please resume Training Run"));
    }
  }

  /**
   * Deletes the acquisition lock row of the given participant and training instance, in a new,
   * independent transaction.
   *
   * @param participantRefId the cross-service user reference id of the participant
   * @param trainingInstanceId the training instance's primary key
   */
  @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
  public void deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(
      Long participantRefId, Long trainingInstanceId) {
    trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(
        participantRefId, trainingInstanceId);
  }

  /**
   * Finds the first, lowest-order level of the given training definition.
   *
   * @throws EntityNotFoundException when the training definition has no levels
   */
  private AbstractLevel findFirstLevelForTrainingRun(Long trainingDefinitionId) {
    List<AbstractLevel> levels =
        abstractLevelRepository.findFirstLevelByTrainingDefinitionId(
            trainingDefinitionId, PageRequest.of(0, 1));
    if (levels.isEmpty()) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              TrainingDefinition.class,
              "id",
              Long.class,
              trainingDefinitionId,
              "No starting level available for this training definition."));
    }
    return levels.get(0);
  }

  /**
   * Builds a new, running training run on the given level and instance, spanning the given start
   * and end time, for the participant carrying the given cross-service user reference id. Resolves
   * that id to the participant's local {@link UserRef} row, creating it first if absent.
   */
  private TrainingRun getNewTrainingRun(
      AbstractLevel currentLevel,
      TrainingInstance trainingInstance,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Long participantRefId) {
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
   * Requests and locks an available sandbox from the given pool for the training run's training
   * instance access token, and records the sandbox and its allocation unit on the run.
   *
   * @param trainingRun the training run to assign a sandbox to
   * @param poolId the sandbox pool to request from
   * @return the {@link TrainingRun} with its sandbox reference set
   * @throws ForbiddenException when the pool has no available sandbox
   * @throws MicroserviceApiException when the call to the sandbox service fails for another reason
   */
  public TrainingRun assignSandbox(TrainingRun trainingRun, long poolId) {
    SandboxInfo info =
        sandboxApiService.getAndLockSandbox(
            poolId, trainingRun.getTrainingInstance().getAccessToken());
    trainingRun.setSandboxInstanceRefId(info.getId());
    trainingRun.setSandboxInstanceAllocationId(info.getAllocationUnitId());
    return trainingRunRepository.save(trainingRun);
  }

  /**
   * Resumes a training run that is neither finished nor archived, its training instance not yet
   * ended, and, unless the instance is local, with a sandbox pool allocated and a sandbox already
   * assigned to the run.
   *
   * @param trainingRunId the training run's primary key
   * @return the resumed {@link TrainingRun}
   * @throws EntityNotFoundException when no training run carries that id
   * @throws EntityConflictException when the run is finished or archived, its training instance has
   *     already ended, its training instance has no sandbox pool allocated, or the run itself has
   *     no sandbox assigned
   */
  public TrainingRun resumeTrainingRun(Long trainingRunId) {
    TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
    TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
    if (trainingRun.getState().equals(TRState.FINISHED)
        || trainingRun.getState().equals(TRState.ARCHIVED)) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRunId.getClass(),
              trainingRunId,
              "Cannot resume finished training run."));
    }
    if (trainingInstance.getEndTime().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRunId.getClass(),
              trainingRunId,
              "Cannot resume training run after end of training instance."));
    }
    if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() == null) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRunId.getClass(),
              trainingRunId,
              "The pool assignment of the appropriate training instance has been probably canceled. Please contact the organizer."));
    }

    if (!trainingInstance.isLocalEnvironment() && trainingRun.getSandboxInstanceRefId() == null) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRunId.getClass(),
              trainingRunId,
              "Sandbox of this training run was already deleted, you have to start new training."));
    }
    auditEventsService.auditTrainingRunResumedAction(trainingRun);
    return trainingRun;
  }

  /**
   * Checks the given answer against the training run's current level, which must be a {@link
   * TrainingLevel} not yet answered.
   *
   * @param runId the training run's primary key
   * @param answer the submitted answer
   * @return true when the answer is correct
   * @throws EntityNotFoundException when no training run carries that id
   * @throws BadRequestException when the current level is not a {@link TrainingLevel}
   * @throws EntityConflictException when the current level has already been answered
   */
  public boolean isCorrectAnswer(Long runId, String answer) {
    TrainingRun trainingRun = findByIdWithLevel(runId);
    AbstractLevel level = trainingRun.getCurrentLevel();
    if (level.getClass() != TrainingLevel.class) {
      throw new BadRequestException(
          "Current level is not training level and does not have answer.");
    } else if (trainingRun.isLevelAnswered()) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              Long.class,
              runId,
              "The answer of the current level of training run has been already corrected."));
    }
    return evaluateTrainingLevelAnswer(trainingRun, answer);
  }

  /**
   * Compares the submitted answer against the level's correct answer. On a match, marks the level
   * answered, adds the level's maximum score minus its current penalty to the run's total score,
   * audits a correct answer and a completed level, and records a correct submission. Otherwise
   * increases the run's incorrect answer count, unless it already equals the level's incorrect
   * answer limit, audits a wrong answer and records an incorrect submission.
   */
  private boolean evaluateTrainingLevelAnswer(TrainingRun trainingRun, String answer) {
    TrainingLevel trainingLevel = (TrainingLevel) trainingRun.getCurrentLevel();
    String correctAnswer = getTrainingLevelCorrectAnswer(trainingLevel, trainingRun);
    if (correctAnswer.equals(answer)) {
      trainingRun.setLevelAnswered(true);
      trainingRun.increaseTotalTrainingScore(
          trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty());
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
   * Checks the given passkey against the training run's current level, which must be an {@link
   * AccessLevel} not yet answered.
   *
   * @param runId the training run's primary key
   * @param passkey the submitted passkey
   * @return true when the passkey is correct
   * @throws EntityNotFoundException when no training run carries that id
   * @throws BadRequestException when the current level is not an {@link AccessLevel}
   * @throws EntityConflictException when the current level has already been answered
   */
  public boolean isCorrectPassKey(Long runId, String passkey) {
    TrainingRun trainingRun = findByIdWithLevel(runId);
    AbstractLevel level = trainingRun.getCurrentLevel();
    if (level.getClass() != AccessLevel.class) {
      throw new BadRequestException("Current level is not access level and does not have passkey.");
    } else if (trainingRun.isLevelAnswered()) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              Long.class,
              runId,
              "The passkey of the current level of training run has been already corrected."));
    }
    return evaluateAccessLevelPasskey(trainingRun, passkey);
  }

  /**
   * Compares the submitted passkey against the level's passkey. Audits a wrong answer submission on
   * both a match and a mismatch; on a match it also marks the level answered and audits the level
   * as completed.
   */
  private boolean evaluateAccessLevelPasskey(TrainingRun trainingRun, String passkey) {
    AccessLevel accessLevel = (AccessLevel) trainingRun.getCurrentLevel();
    if (accessLevel.getPasskey().equals(passkey)) {
      trainingRun.setLevelAnswered(true);
      auditEventsService.auditWrongAnswerSubmittedAction(trainingRun, passkey);
      auditEventsService.auditLevelCompletedAction(trainingRun);
      return true;
    }
    auditEventsService.auditWrongAnswerSubmittedAction(trainingRun, passkey);
    return false;
  }

  /**
   * Records a submission for the training run's current level, carrying the submitted answer, its
   * type, the current time and the caller's IP address.
   */
  private void auditSubmission(
      TrainingRun trainingRun, SubmissionType submissionType, String answer) {
    Submission submission = new Submission();
    submission.setDate(LocalDateTime.now(Clock.systemUTC()));
    submission.setLevel(trainingRun.getCurrentLevel());
    submission.setTrainingRun(trainingRun);
    submission.setProvided(answer);
    submission.setType(submissionType);
    submission.setIpAddress(getUserIpAddress());
    submissionRepository.save(submission);
  }

  /**
   * Reads the current request's {@code x-real-ip} header.
   *
   * @return the header value, or an empty string when there is no current request or the header is
   *     absent
   */
  private String getUserIpAddress() {
    ServletRequestAttributes requestAttributes =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
    if (requestAttributes != null
        && requestAttributes.getRequest().getHeader(X_REAL_IP_HEADER) != null) {
      return requestAttributes.getRequest().getHeader(X_REAL_IP_HEADER);
    }
    return "";
  }

  /**
   * Gets the remaining incorrect-answer attempts on the training run's current level, which must be
   * a {@link TrainingLevel}: 0 once the solution has been taken, otherwise the level's incorrect
   * answer limit minus the run's incorrect answer count so far.
   *
   * @param trainingRunId the training run's primary key
   * @return the remaining attempts
   * @throws BadRequestException when the current level is not a {@link TrainingLevel}
   */
  public int getRemainingAttempts(Long trainingRunId) {
    TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
    AbstractLevel level = trainingRun.getCurrentLevel();
    if (level instanceof TrainingLevel) {
      if (trainingRun.isSolutionTaken()) {
        return 0;
      }
      return ((TrainingLevel) level).getIncorrectAnswerLimit()
          - trainingRun.getIncorrectAnswerCount();
    }
    throw new BadRequestException("Current level is not training level and does not have answer.");
  }

  /**
   * Gets the solution of the training run's current level, which must be a {@link TrainingLevel}.
   * The first call records the solution as taken and, when the level penalizes taking its solution,
   * sets the run's current penalty to the level's maximum score, zeroing the score obtainable from
   * it. Any {@code ${ANSWER}} placeholder in the solution text is replaced with the level's correct
   * answer for this run.
   *
   * @param trainingRunId the training run's primary key
   * @return the current level's solution, with its answer placeholder resolved
   * @throws EntityNotFoundException when no training run carries that id
   * @throws BadRequestException when the current level is not a {@link TrainingLevel}
   */
  public String getSolution(Long trainingRunId) {
    TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
    AbstractLevel level = trainingRun.getCurrentLevel();
    if (level instanceof TrainingLevel trainingLevel) {
      if (!trainingRun.isSolutionTaken()) {
        trainingRun.setSolutionTaken(true);
        trainingRun.addSolutionInfo(
            new SolutionInfo(trainingLevel.getId(), trainingLevel.getSolution()));
        if (trainingLevel.isSolutionPenalized()) {
          trainingRun.setCurrentPenalty(trainingRun.getMaxLevelScore());
        }

        trainingRunRepository.save(trainingRun);
        auditEventsService.auditSolutionDisplayedAction(trainingRun);
      }
      return getSolutionWithReplacedVariable(trainingLevel, trainingRun);
    } else {
      throw new BadRequestException(
          "Current level is not training level and does not have solution.");
    }
  }

  /**
   * Returns the level's solution text, with any {@code ${ANSWER}} placeholder replaced by the
   * level's correct answer for the given training run.
   */
  private String getSolutionWithReplacedVariable(
      TrainingLevel trainingLevel, TrainingRun trainingRun) {
    if (!trainingLevel.getSolution().contains("${ANSWER}")) {
      return trainingLevel.getSolution();
    }
    return trainingLevel
        .getSolution()
        .replaceAll("\\$\\{ANSWER\\}", getTrainingLevelCorrectAnswer(trainingLevel, trainingRun));
  }

  /**
   * Gets the correct answer of the given training level for the given training run. When the level
   * has no per-participant variant answers, returns its static answer. Otherwise asks the
   * answer-storage service: by the run's access token and the participant's cross-service user
   * reference id when the training instance is local, or by the run's sandbox reference id
   * otherwise.
   *
   * @param trainingLevel the training level whose correct answer to get
   * @param trainingRun the training run to resolve a variant answer for
   * @return the level's static or variant correct answer
   */
  public String getTrainingLevelCorrectAnswer(
      TrainingLevel trainingLevel, TrainingRun trainingRun) {
    if (trainingLevel.isVariantAnswers()) {
      return trainingRun.getTrainingInstance().isLocalEnvironment()
          ? answersStorageApiService.getCorrectAnswerByLocalSandboxIdAndVariableName(
              trainingRun.getTrainingInstance().getAccessToken(),
              trainingRun.getParticipantRef().getUserRefId(),
              trainingLevel.getAnswerVariableName())
          : answersStorageApiService.getCorrectAnswerByCloudSandboxIdAndVariableName(
              trainingRun.getSandboxInstanceRefId(), trainingLevel.getAnswerVariableName());
    }
    return trainingLevel.getAnswer();
  }

  /**
   * Gets a hint of the training run's current level, which must be a {@link TrainingLevel} and must
   * own the hint. Increases the run's current penalty by the hint's penalty and records the hint as
   * taken.
   *
   * @param trainingRunId the training run's primary key
   * @param hintId the hint's primary key
   * @return the requested {@link Hint}
   * @throws EntityNotFoundException when the training run or the hint does not exist
   * @throws BadRequestException when the current level is not a {@link TrainingLevel}
   * @throws EntityConflictException when the hint does not belong to the current level
   */
  public Hint getHint(Long trainingRunId, Long hintId) {
    TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
    AbstractLevel level = trainingRun.getCurrentLevel();
    if (level instanceof TrainingLevel) {
      Hint hint =
          hintRepository
              .findById(hintId)
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          new EntityErrorDetail(
                              Hint.class, "id", hintId.getClass(), hintId, "Hint not found.")));
      if (hint.getTrainingLevel().getId().equals(level.getId())) {
        trainingRun.increaseCurrentPenalty(hint.getHintPenalty());
        trainingRun.addHintInfo(
            new HintInfo(
                level.getId(), hint.getId(), hint.getTitle(), hint.getContent(), hint.getOrder()));
        auditEventsService.auditHintTakenAction(trainingRun, hint);
        return hint;
      }
      throw new EntityConflictException(
          new EntityErrorDetail(
              Hint.class,
              "id",
              hintId.getClass(),
              hintId,
              "Hint is not in current level of training run: " + trainingRunId + "."));
    } else {
      throw new BadRequestException("Current level is not training level and does not have hints.");
    }
  }

  /**
   * Gets the highest level order used within the given training definition.
   *
   * @param definitionId the training definition's primary key
   * @return the highest level order, or -1 when the definition has no levels
   */
  public int getMaxLevelOrder(Long definitionId) {
    return abstractLevelRepository.getCurrentMaxOrder(definitionId);
  }

  /**
   * Finishes a training run whose current level is the last of its training definition and has been
   * answered. Sets its state to {@link TRState#FINISHED} and its end time to now, deletes its
   * acquisition lock, audits its current level as completed when it is an {@link InfoLevel}, then
   * audits the run as ended.
   *
   * @param trainingRunId the training run's primary key
   * @return the finished {@link TrainingRun}
   * @throws EntityNotFoundException when no training run carries that id
   * @throws EntityConflictException when the current level is not the last one, or has not been
   *     answered
   */
  public TrainingRun finishTrainingRun(Long trainingRunId) {
    TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
    int maxOrder =
        abstractLevelRepository.getCurrentMaxOrder(
            trainingRun.getCurrentLevel().getTrainingDefinition().getId());
    if (trainingRun.getCurrentLevel().getOrder() != maxOrder) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRunId.getClass(),
              trainingRunId,
              "Cannot finish training run because current level is not last."));
    }
    if (!trainingRun.isLevelAnswered()) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRunId.getClass(),
              trainingRunId,
              "Cannot finish training run because current level is not answered."));
    }
    trainingRun.setState(TRState.FINISHED);
    trainingRun.setEndTime(LocalDateTime.now(Clock.systemUTC()));
    trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(
        trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
    if (trainingRun.getCurrentLevel() instanceof InfoLevel) {
      auditEventsService.auditLevelCompletedAction(trainingRun);
    }
    auditEventsService.auditTrainingRunEndedAction(trainingRun);
    return trainingRun;
  }

  /**
   * Archives a training run: sets its state to {@link TRState#ARCHIVED}, moves its current sandbox
   * reference to its previous sandbox reference and clears its sandbox and allocation unit
   * references, and deletes its acquisition lock.
   *
   * @param trainingRunId the training run's primary key
   * @throws EntityNotFoundException when no training run carries that id
   */
  public void archiveTrainingRun(Long trainingRunId) {
    TrainingRun trainingRun = findById(trainingRunId);
    trainingRun.setState(TRState.ARCHIVED);
    trainingRun.setPreviousSandboxInstanceRefId(trainingRun.getSandboxInstanceRefId());
    trainingRun.setSandboxInstanceRefId(null);
    trainingRun.setSandboxInstanceAllocationId(null);
    trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(
        trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
    trainingRunRepository.save(trainingRun);
  }

  /**
   * Checks whether OpenSearch holds any training event recorded for the given run.
   *
   * @param run the training run to check
   * @return true when at least one training event exists for the run
   */
  public boolean checkRunEventLogging(TrainingRun run) {
    return trainingEventsService.hasRunEvents(run.getId());
  }

  /**
   * Checks whether OpenSearch holds any console command recorded for the run's sandbox, falling
   * back to its previous sandbox reference when no current one is set.
   *
   * @param run the training run to check
   * @return true when at least one console command exists for the sandbox
   */
  public boolean checkRunCommandLogging(TrainingRun run) {
    String sandboxId =
        run.getSandboxInstanceRefId() == null
            ? run.getPreviousSandboxInstanceRefId()
            : run.getSandboxInstanceRefId();
    return commandEventsService.hasConsoleCommandsBySandbox(sandboxId);
  }

  /**
   * Evaluates and stores the given answers against the training run's current level, which must be
   * an {@link AssessmentLevel} not yet answered. For a {@link AssessmentType#TEST} level, every
   * question must have a submitted answer; each is scored, the run's current penalty is set to the
   * level's maximum score minus the total points gained, and that total is added to the run's
   * assessment score. For a {@link AssessmentType#QUESTIONNAIRE} level, answers are recorded
   * unscored and only a question marked as required must have a submitted answer. Marks the level
   * answered, saves the resulting {@link QuestionAnswer} rows, and audits the assessment answers
   * followed by the level as completed.
   *
   * @param trainingRunId the training run's primary key
   * @param answersToQuestions the submitted answers, keyed by question id
   * @throws EntityNotFoundException when no training run carries that id
   * @throws BadRequestException when the current level is not an {@link AssessmentLevel}, or a
   *     question that must be answered is missing from {@code answersToQuestions}
   * @throws EntityConflictException when the current level has already been answered
   */
  public void evaluateResponsesToAssessment(
      Long trainingRunId, Map<Long, QuestionAnswerDTO> answersToQuestions) {
    TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
    if (!(trainingRun.getCurrentLevel() instanceof AssessmentLevel)) {
      throw new BadRequestException(
          "Current level is not assessment level and cannot be evaluated.");
    }
    if (trainingRun.isLevelAnswered())
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingRun.class,
              "id",
              trainingRunId.getClass(),
              trainingRunId,
              "Current level of the training run has been already answered."));
    List<QuestionAnswer> userAnswersToQuestions;
    Map<Long, AnswerEvaluation> evaluations;
    if (((AssessmentLevel) trainingRun.getCurrentLevel()).getAssessmentType()
        == AssessmentType.TEST) {
      AssessmentEvaluation assessmentEvaluation =
          this.gatherAndEvaluateAnswers(trainingRun, answersToQuestions);
      userAnswersToQuestions = assessmentEvaluation.answers();
      evaluations = assessmentEvaluation.evaluations();
    } else {
      userAnswersToQuestions = this.gatherAnswers(trainingRun, answersToQuestions);
      evaluations = Map.of();
    }
    trainingRun.setLevelAnswered(true);
    questionAnswerRepository.saveAll(userAnswersToQuestions);
    List<EventAnswer> eventAnswers =
        this.buildEventAnswers(
            (AssessmentLevel) trainingRun.getCurrentLevel(), answersToQuestions, evaluations);
    auditEventsService.auditAssessmentAnswersAction(trainingRun, eventAnswers);
    auditEventsService.auditLevelCompletedAction(trainingRun);
  }

  /**
   * Builds one {@link EventAnswer} per question of the level that has a submitted answer, carrying
   * that question's evaluation when one is present.
   */
  private List<EventAnswer> buildEventAnswers(
      AssessmentLevel assessmentLevel,
      Map<Long, QuestionAnswerDTO> answersToQuestions,
      Map<Long, AnswerEvaluation> evaluations) {
    List<EventAnswer> eventAnswers = new ArrayList<>();
    for (Question question : assessmentLevel.getQuestions()) {
      QuestionAnswerDTO submittedAnswer = answersToQuestions.get(question.getId());
      if (submittedAnswer != null) {
        eventAnswers.add(
            this.toEventAnswer(question, submittedAnswer, evaluations.get(question.getId())));
      }
    }
    return eventAnswers;
  }

  /**
   * Builds the free-form, multiple-choice or extended-matching {@link EventAnswer} matching the
   * question's type, carrying the points gained and, when an evaluation is given, the correctness
   * of the submitted selections.
   */
  private EventAnswer toEventAnswer(
      Question question, QuestionAnswerDTO submittedAnswer, AnswerEvaluation evaluation) {
    boolean scored = evaluation != null;
    EventAnswer eventAnswer =
        switch (question.getQuestionType()) {
          case FFQ ->
              FreeFormEventAnswer.builder()
                  .questionId(question.getId())
                  .answer(this.freeFormSelection(question, submittedAnswer.getAnswers(), scored))
                  .build();
          case MCQ ->
              MultipleChoiceEventAnswer.builder()
                  .questionId(question.getId())
                  .selectedOptions(
                      this.selectedOptions(question, submittedAnswer.getAnswers(), scored))
                  .build();
          case EMI ->
              ExtendedMatchingEventAnswer.builder()
                  .questionId(question.getId())
                  .pairs(
                      this.pairSelections(
                          question, submittedAnswer.getExtendedMatchingPairs(), scored))
                  .build();
        };
    eventAnswer.setPointsGained(scored ? evaluation.pointsGained() : 0);
    if (scored) {
      eventAnswer.setCorrect(evaluation.correct());
    }
    return eventAnswer;
  }

  /**
   * Builds the free-form answer selection from the first of the given answers, or null when none
   * were submitted. When scored, marks it correct if it equals the text of any choice of the
   * question, regardless of that choice's own correct flag.
   */
  private AnswerSelection<String> freeFormSelection(
      Question question, Set<String> answers, boolean scored) {
    String answer = this.singleAnswer(answers);
    if (answer == null) {
      return null;
    }
    Boolean correct = scored ? this.isFreeFormAnswerCorrect(question, answer) : null;
    return AnswerSelection.<String>builder().value(answer).correct(correct).build();
  }

  /** Returns one answer from the given set, or null when it is null or empty. */
  private String singleAnswer(Set<String> answers) {
    return answers == null || answers.isEmpty() ? null : answers.iterator().next();
  }

  /**
   * Checks whether the answer equals the text of any choice of the question, regardless of that
   * choice's own correct flag.
   */
  private boolean isFreeFormAnswerCorrect(Question question, String answer) {
    return question.getChoices().stream().map(QuestionChoice::getText).anyMatch(answer::equals);
  }

  /**
   * Builds one selection per choice of the question whose text is among the selected texts, ordered
   * by choice order, carrying that choice's correct flag when scored.
   */
  private List<AnswerSelection<Integer>> selectedOptions(
      Question question, Set<String> selectedTexts, boolean scored) {
    if (selectedTexts == null) {
      return new ArrayList<>();
    }
    return question.getChoices().stream()
        .filter(choice -> selectedTexts.contains(choice.getText()))
        .sorted(Comparator.comparingInt(QuestionChoice::getOrder))
        .map(
            choice ->
                AnswerSelection.<Integer>builder()
                    .value(choice.getOrder())
                    .correct(scored ? choice.isCorrect() : null)
                    .build())
        .toList();
  }

  /**
   * Builds one selection per submitted statement-to-option pair, keyed by statement order, carrying
   * whether the submitted option matches the question's expected option for that statement when
   * scored.
   */
  private Map<Integer, AnswerSelection<Integer>> pairSelections(
      Question question, Map<Integer, Integer> submittedPairs, boolean scored) {
    Map<Integer, AnswerSelection<Integer>> selections = new TreeMap<>();
    if (submittedPairs == null) {
      return selections;
    }
    Map<Integer, Integer> expectedByStatement =
        scored ? this.expectedOptionByStatement(question) : Map.of();
    submittedPairs.forEach(
        (statementOrder, optionOrder) -> {
          Boolean correct =
              scored ? optionOrder.equals(expectedByStatement.get(statementOrder)) : null;
          selections.put(
              statementOrder,
              AnswerSelection.<Integer>builder().value(optionOrder).correct(correct).build());
        });
    return selections;
  }

  /** Maps each extended-matching statement's order to its expected option's order. */
  private Map<Integer, Integer> expectedOptionByStatement(Question question) {
    Map<Integer, Integer> expected = new HashMap<>();
    for (ExtendedMatchingStatement statement : question.getExtendedMatchingStatements()) {
      expected.put(statement.getOrder(), statement.getExtendedMatchingOption().getOrder());
    }
    return expected;
  }

  /**
   * Builds and scores a {@link QuestionAnswer} for every question of the training run's current
   * assessment level, requiring a submitted answer for each. Sets the run's current penalty to the
   * level's maximum score minus the total points gained and adds that total to the run's assessment
   * score.
   *
   * @throws BadRequestException when a question has no submitted answer
   */
  private AssessmentEvaluation gatherAndEvaluateAnswers(
      TrainingRun trainingRun, Map<Long, QuestionAnswerDTO> answersToQuestions) {
    int score = 0;
    List<QuestionAnswer> userAnswersToQuestions = new ArrayList<>();
    Map<Long, AnswerEvaluation> evaluations = new HashMap<>();
    for (Question question : ((AssessmentLevel) trainingRun.getCurrentLevel()).getQuestions()) {
      QuestionAnswerDTO questionAnswerDTO = answersToQuestions.get(question.getId());
      if (questionAnswerDTO == null) {
        throw new BadRequestException(
            "The question '" + question.getText() + "' must be answered.");
      }
      userAnswersToQuestions.add(
          this.createQuestionAnswer(question, trainingRun, questionAnswerDTO));
      AnswerEvaluation evaluation = this.evaluateAnswer(question, questionAnswerDTO);
      evaluations.put(question.getId(), evaluation);
      score += evaluation.pointsGained();
    }
    trainingRun.setCurrentPenalty(trainingRun.getMaxLevelScore() - score);
    trainingRun.increaseTotalAssessmentScore(score);
    return new AssessmentEvaluation(userAnswersToQuestions, evaluations);
  }

  /**
   * Builds a {@link QuestionAnswer} for every question of the training run's current assessment
   * level that has a submitted answer.
   *
   * @throws BadRequestException when a question marked as required has no submitted answer
   */
  private List<QuestionAnswer> gatherAnswers(
      TrainingRun trainingRun, Map<Long, QuestionAnswerDTO> answersToQuestions) {
    List<QuestionAnswer> userAnswersToQuestions = new ArrayList<>();
    for (Question question : ((AssessmentLevel) trainingRun.getCurrentLevel()).getQuestions()) {
      QuestionAnswerDTO questionAnswerDTO = answersToQuestions.get(question.getId());
      if (questionAnswerDTO != null) {
        userAnswersToQuestions.add(
            this.createQuestionAnswer(question, trainingRun, questionAnswerDTO));
      } else if (question.isAnswerRequired()) {
        throw new BadRequestException(
            "The question '" + question.getText() + "' must be answered.");
      }
    }
    return userAnswersToQuestions;
  }

  /**
   * Builds a {@link QuestionAnswer} for the given question and training run. For an extended
   * matching question, encodes each submitted statement-to-option pair as one JSON-like string per
   * answer; for any other type, stores the submitted answers as given.
   */
  private QuestionAnswer createQuestionAnswer(
      Question question, TrainingRun trainingRun, QuestionAnswerDTO answersToQuestion) {
    QuestionAnswer questionAnswer = new QuestionAnswer(question, trainingRun);
    if (question.getQuestionType() == QuestionType.EMI) {
      Set<String> answers =
          question.getExtendedMatchingStatements().stream()
              .filter(
                  statement ->
                      answersToQuestion
                          .getExtendedMatchingPairs()
                          .containsKey(statement.getOrder()))
              .map(
                  statement ->
                      "{ \"statementOrder\": "
                          + statement.getOrder()
                          + ", \"optionOrder\": "
                          + answersToQuestion.getExtendedMatchingPairs().get(statement.getOrder())
                          + " }")
              .collect(Collectors.toSet());
      questionAnswer.setAnswers(answers);
    } else {
      questionAnswer.setAnswers(answersToQuestion.getAnswers());
    }
    return questionAnswer;
  }

  /**
   * Evaluates the given answer against the question, by its type, and returns whether it is correct
   * together with the points to award: the question's points when correct, or its penalty taken as
   * a negative amount otherwise.
   */
  private AnswerEvaluation evaluateAnswer(Question question, QuestionAnswerDTO userAnswer) {
    boolean correct =
        switch (question.getQuestionType()) {
          case FFQ -> this.isFreeFormCorrect(question, userAnswer);
          case MCQ -> this.isMultipleChoiceCorrect(question, userAnswer);
          case EMI -> this.isExtendedMatchingCorrect(question, userAnswer);
        };
    int pointsGained = correct ? question.getPoints() : (-1) * question.getPenalty();
    return new AnswerEvaluation(correct, pointsGained);
  }

  /**
   * Checks whether every submitted answer text matches the text of some choice of the question,
   * treating every choice as an accepted answer regardless of that choice's own correct flag.
   */
  private boolean isFreeFormCorrect(Question question, QuestionAnswerDTO userAnswer) {
    List<String> correctAnswers =
        question.getChoices().stream().map(QuestionChoice::getText).toList();
    return correctAnswers.containsAll(userAnswer.getAnswers());
  }

  /**
   * Checks whether the submitted answer texts are exactly the texts of the question's choices
   * marked correct, regardless of order.
   */
  private boolean isMultipleChoiceCorrect(Question question, QuestionAnswerDTO userAnswer) {
    List<String> correctAnswers =
        question.getChoices().stream()
            .filter(QuestionChoice::isCorrect)
            .map(QuestionChoice::getText)
            .toList();
    return userAnswer.getAnswers().size() == correctAnswers.size()
        && userAnswer.getAnswers().containsAll(correctAnswers);
  }

  /**
   * Checks whether every extended matching statement of the question is paired, in the submitted
   * answer, with its expected option.
   */
  private boolean isExtendedMatchingCorrect(Question question, QuestionAnswerDTO userAnswer) {
    for (ExtendedMatchingStatement extendedMatchingStatement :
        question.getExtendedMatchingStatements()) {
      int expectedOptionOrder = extendedMatchingStatement.getExtendedMatchingOption().getOrder();
      int answeredOptionOrder =
          userAnswer.getExtendedMatchingPairs().get(extendedMatchingStatement.getOrder());
      if (expectedOptionOrder != answeredOptionOrder) {
        return false;
      }
    }
    return true;
  }

  /** The outcome of scoring one question's submitted answer. */
  private record AnswerEvaluation(boolean correct, int pointsGained) {}

  /** The stored answers and their evaluations produced while scoring a test assessment level. */
  private record AssessmentEvaluation(
      List<QuestionAnswer> answers, Map<Long, AnswerEvaluation> evaluations) {}

  /**
   * Marks the given training run as having a detection event.
   *
   * @param run the training run to mark
   */
  public void auditRunHasDetectionEvent(TrainingRun run) {
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
  }

  /**
   * Gets every question answer recorded for the given training run, in no guaranteed order.
   *
   * @param runId the training run's primary key
   * @return the run's {@link QuestionAnswer}s, or an empty list if none exist
   */
  public List<QuestionAnswer> getQuestionAnswersByTrainingRunId(Long runId) {
    return questionAnswerRepository.getAllByTrainingRunId(runId);
  }
}
