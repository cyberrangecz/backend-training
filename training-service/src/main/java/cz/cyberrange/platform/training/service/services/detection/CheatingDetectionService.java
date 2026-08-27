package cz.cyberrange.platform.training.service.services.detection;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.AbstractEntity;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.enums.CheatingDetectionState;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.CheatingDetectionRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectedForbiddenCommandRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectionEventParticipantRepository;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Runs a cheating detection sweep across its six kind-specific detectors, tracking a state per
 * detector so a re-execution can skip what already finished and leave out what was disabled, and
 * manages the lifecycle of sweep records and their findings
 */
@Service
public class CheatingDetectionService {

  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final AbstractDetectionEventRepository detectionEventRepository;
  private final CheatingDetectionRepository cheatingDetectionRepository;
  private final DetectionEventParticipantRepository detectionEventParticipantRepository;
  private final DetectedForbiddenCommandRepository detectedForbiddenCommandRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final TrainingRunService trainingRunService;
  private final UserService userService;
  private final AnswerSimilarityService answerSimilarityService;
  private final LocationSimilarityService locationSimilarityService;
  private final MinimalSolveTimeService minimalSolveTimeService;
  private final TimeProximityService timeProximityService;
  private final NoCommandsService noCommandsService;
  private final ForbiddenCommandsService forbiddenCommandsService;

  /**
   * Creates the service with the repositories and per-kind detection services it coordinates across
   * a cheating detection sweep
   */
  @Autowired
  public CheatingDetectionService(
      AbstractDetectionEventRepository abstractDetectionEventRepository,
      CheatingDetectionRepository cheatingDetectionRepository,
      DetectionEventParticipantRepository detectionEventParticipantRepository,
      DetectedForbiddenCommandRepository detectedForbiddenCommandRepository,
      TrainingRunRepository trainingRunRepository,
      TrainingRunService trainingRunService,
      UserService userService,
      AnswerSimilarityService answerSimilarityService,
      LocationSimilarityService locationSimilarityService,
      MinimalSolveTimeService minimalSolveTimeService,
      TimeProximityService timeProximityService,
      NoCommandsService noCommandsService,
      ForbiddenCommandsService forbiddenCommandsService) {
    this.detectionEventRepository = abstractDetectionEventRepository;
    this.cheatingDetectionRepository = cheatingDetectionRepository;
    this.detectionEventParticipantRepository = detectionEventParticipantRepository;
    this.detectedForbiddenCommandRepository = detectedForbiddenCommandRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.userService = userService;
    this.answerSimilarityService = answerSimilarityService;
    this.locationSimilarityService = locationSimilarityService;
    this.minimalSolveTimeService = minimalSolveTimeService;
    this.timeProximityService = timeProximityService;
    this.noCommandsService = noCommandsService;
    this.forbiddenCommandsService = forbiddenCommandsService;
  }

  /**
   * Stamps a sweep record with the display name of the current caller, a zero result count and the
   * current time, then saves it. The per-detector states set on the record beforehand are left
   * untouched.
   *
   * @param cheatingDetection the sweep record to persist
   */
  public void createCheatingDetection(CheatingDetection cheatingDetection) {
    cheatingDetection.setExecutedBy(userService.getUserRefFromUserAndGroup().getUserRefFullName());
    cheatingDetection.setResults(0L);
    cheatingDetection.setExecuteTime(LocalDateTime.now());
    cheatingDetectionRepository.save(cheatingDetection);
  }

  /**
   * Runs every one of the sweep's six detectors that is queued, in a fixed order, then marks the
   * sweep itself finished. A detector left disabled or already finished is skipped.
   *
   * @param cd the sweep to execute
   */
  public void executeCheatingDetection(CheatingDetection cd) {
    cd.setCurrentState(CheatingDetectionState.RUNNING);
    executeSelectedCheatingDetectionMethods(cd);
    cd.setCurrentState(CheatingDetectionState.FINISHED);
    updateCheatingDetection(cd);
  }

  /**
   * Deletes one sweep record together with its events, their participants and any detected
   * forbidden commands. As a side effect it clears the {@code hasDetectionEvent} flag on every
   * training run of {@code trainingInstanceId}, not only the runs implicated by this particular
   * sweep.
   *
   * @param cheatingDetectionId the id of the sweep to delete
   * @param trainingInstanceId the training instance whose runs have the flag cleared
   */
  public void deleteCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
    trainingRunService.findAllByTrainingInstanceId(trainingInstanceId).stream()
        .peek(run -> run.setHasDetectionEvent(false))
        .forEach(trainingRunRepository::save);
    List<AbstractDetectionEvent> events =
        detectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    detectionEventParticipantRepository.deleteAllParticipantsByCheatingDetectionId(
        cheatingDetectionId);
    events.stream()
        .map(AbstractDetectionEvent::getId)
        .forEach(detectedForbiddenCommandRepository::deleteAllByDetectionEventId);
    detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
    cheatingDetectionRepository.deleteCheatingDetectionById(cheatingDetectionId);
  }

  /**
   * Deletes every sweep record of a training instance, one at a time through {@link
   * #deleteCheatingDetection}. Because that method clears the {@code hasDetectionEvent} flag on
   * every run of the instance, the flag gets cleared once per existing sweep rather than once
   * overall.
   *
   * @param trainingInstanceId the training instance whose sweeps are deleted
   */
  public void deleteAllCheatingDetectionsOfTrainingInstance(Long trainingInstanceId) {
    cheatingDetectionRepository.findAllByTrainingInstanceId(trainingInstanceId).stream()
        .map(CheatingDetection::getId)
        .forEach(detectionId -> deleteCheatingDetection(detectionId, trainingInstanceId));
  }

  /**
   * Returns the sweep record with the given primary key, or {@code null} if none matches.
   *
   * @param cheatingDetectionId the primary key of the sweep
   * @return the matching sweep record, or {@code null}
   */
  public CheatingDetection findCheatingDetectionById(Long cheatingDetectionId) {
    return cheatingDetectionRepository.findCheatingDetectionById(cheatingDetectionId);
  }

  /**
   * Re-runs an existing sweep from scratch: queues every detector that is not disabled, discards
   * the sweep's existing detection events, their participants and any detected forbidden commands,
   * resets the execute time and result count, then executes it again.
   *
   * @param cheatingDetectionId id of the sweep to re-run
   * @throws EntityNotFoundException if no sweep with that id exists
   */
  public void reExecuteCheatingDetection(Long cheatingDetectionId) {
    CheatingDetection cd =
        Optional.ofNullable(
                cheatingDetectionRepository.findCheatingDetectionById(cheatingDetectionId))
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            CheatingDetection.class,
                            "id",
                            cheatingDetectionId.getClass(),
                            cheatingDetectionId)));

    cd.setExecuteStates();

    detectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId).stream()
        .map(AbstractEntity::getId)
        .forEach(detectedForbiddenCommandRepository::deleteAllByDetectionEventId);

    detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
    detectionEventParticipantRepository.deleteAllParticipantsByCheatingDetectionId(
        cheatingDetectionId);

    cd.setExecuteTime(LocalDateTime.now());
    cd.setResults(0L);
    cheatingDetectionRepository.save(cd);
    executeCheatingDetection(cd);
  }

  /**
   * Returns every participant implicated by any detection event of one sweep, across every kind of
   * finding, in no defined order.
   *
   * @param cheatingDetectionId the sweep whose participants are returned
   * @return the matching participants
   */
  public List<DetectionEventParticipant> findAllParticipantsOfCheatingDetection(
      Long cheatingDetectionId) {
    return detectionEventParticipantRepository.findAllParticipantsOfCheatingDetection(
        cheatingDetectionId);
  }

  /**
   * Returns, as one page, the sweep records of one training instance, ordered by their execute
   * time.
   *
   * @param trainingInstanceId the training instance whose sweeps are returned
   * @param pageable the page to return
   * @return the matching page of sweep records
   */
  public Page<CheatingDetection> findAllCheatingDetectionsOfTrainingInstance(
      Long trainingInstanceId, Pageable pageable) {
    return cheatingDetectionRepository.findAllByTrainingInstanceId(trainingInstanceId, pageable);
  }

  private void updateCheatingDetection(CheatingDetection cd) {
    cheatingDetectionRepository.save(cd);
  }

  private void executeSelectedCheatingDetectionMethods(CheatingDetection cd) {
    updateCheatingDetection(cd);
    handleAnswerSimilarityExecution(cd);
    handleLocationSimilarityExecution(cd);
    handleTimeProximityExecution(cd);
    handleMinimalSolveTimeExecution(cd);
    handleNoCommandsExecution(cd);
    handleForbiddenCommandsExecution(cd);
    cd.setResults(detectionEventRepository.getNumberOfDetections(cd.getId()));
  }

  private void handleForbiddenCommandsExecution(CheatingDetection cd) {
    if (cd.getForbiddenCommandsState() == CheatingDetectionState.QUEUED) {
      cd.setForbiddenCommandsState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      forbiddenCommandsService.executeCheatingDetectionOfForbiddenCommands(cd);
      cd.setForbiddenCommandsState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleNoCommandsExecution(CheatingDetection cd) {
    if (cd.getNoCommandsState() == CheatingDetectionState.QUEUED) {
      cd.setNoCommandsState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      noCommandsService.executeCheatingDetectionOfNoCommands(cd);
      cd.setNoCommandsState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleMinimalSolveTimeExecution(CheatingDetection cd) {
    if (cd.getMinimalSolveTimeState() == CheatingDetectionState.QUEUED) {
      cd.setMinimalSolveTimeState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      minimalSolveTimeService.executeCheatingDetectionOfMinimalSolveTime(cd);
      cd.setMinimalSolveTimeState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  /**
   * Runs the time proximity detector when it is queued, defaulting the sweep's proximity threshold
   * to 120 seconds first if none was set
   */
  private void handleTimeProximityExecution(CheatingDetection cd) {
    if (cd.getTimeProximityState() == CheatingDetectionState.QUEUED) {
      if (cd.getProximityThreshold() == null) {
        cd.setProximityThreshold(120L);
      }
      cd.setTimeProximityState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      timeProximityService.executeCheatingDetectionOfTimeProximity(cd);
      cd.setTimeProximityState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleLocationSimilarityExecution(CheatingDetection cd) {
    if (cd.getLocationSimilarityState() == CheatingDetectionState.QUEUED) {
      cd.setLocationSimilarityState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      locationSimilarityService.executeCheatingDetectionOfLocationSimilarity(cd);
      cd.setLocationSimilarityState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }

  private void handleAnswerSimilarityExecution(CheatingDetection cd) {
    if (cd.getAnswerSimilarityState() == CheatingDetectionState.QUEUED) {
      cd.setAnswerSimilarityState(CheatingDetectionState.RUNNING);
      updateCheatingDetection(cd);
      answerSimilarityService.executeCheatingDetectionOfAnswerSimilarity(cd);
      cd.setAnswerSimilarityState(CheatingDetectionState.FINISHED);
      updateCheatingDetection(cd);
    }
  }
}
