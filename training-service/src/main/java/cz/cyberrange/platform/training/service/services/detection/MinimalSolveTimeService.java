package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.extractParticipant;
import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.generateParticipantString;

import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.MinimalSolveTimeDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Detects a correct submission entered faster than a training level's configured minimal solve
 * time. Every correct submission of a training instance is timed against when its trainee started
 * the level (the run's start time when the current submission's run differs from the previous
 * submission processed, or the date of the trainee's previous correct submission otherwise); a
 * submission faster than the level's {@code minimalPossibleSolveTime} (in minutes) is recorded,
 * grouped by level, into a {@link MinimalSolveTimeDetectionEvent}. Nothing here requires more than
 * one trainee to be implicated before an event is recorded, so an event commonly names a single
 * trainee.
 */
@Service
public class MinimalSolveTimeService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final SubmissionRepository submissionRepository;
  private final MinimalSolveTimeDetectionEventRepository minimalSolveTimeDetectionEventRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final TrainingRunService trainingRunService;
  private final DetectionEventService detectionEventService;

  /**
   * Creates the service with the repositories and collaborators it uses to time a level's correct
   * submissions against its minimal solve time
   */
  @Autowired
  public MinimalSolveTimeService(
      SubmissionRepository submissionRepository,
      MinimalSolveTimeDetectionEventRepository minimalSolveTimeDetectionEventRepository,
      TrainingRunRepository trainingRunRepository,
      TrainingRunService trainingRunService,
      DetectionEventService detectionEventService) {
    this.submissionRepository = submissionRepository;
    this.minimalSolveTimeDetectionEventRepository = minimalSolveTimeDetectionEventRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.detectionEventService = detectionEventService;
  }

  /**
   * Finds every minimal solve time event recorded under the given cheating detection.
   *
   * @param cheatingDetectionId the cheating detection id
   * @return the matching events
   */
  public List<MinimalSolveTimeDetectionEvent> findAllMinimalSolveTimeEventsOfDetection(
      Long cheatingDetectionId) {
    return minimalSolveTimeDetectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId);
  }

  /**
   * Finds a minimal solve time event by its id.
   *
   * @param eventId the event id
   * @return the matching event
   */
  public MinimalSolveTimeDetectionEvent findMinimalSolveTimeEventById(Long eventId) {
    return minimalSolveTimeDetectionEventRepository.findMinimalSolveTimeEventById(eventId);
  }

  /**
   * Finds every minimal solve time event of the cheating detection that names at least one of the
   * given participant ids among its saved participants.
   *
   * @param cheatingDetectionId the cheating detection id
   * @param participants the participant ids ({@code userRefId}, not the local user primary key)
   * @return the matching events
   */
  List<MinimalSolveTimeDetectionEvent> findAllMinimalSolveTimeEventsOfGroup(
      Long cheatingDetectionId, List<Long> participants) {
    List<MinimalSolveTimeDetectionEvent> minimalSolveTimeEvents =
        findAllMinimalSolveTimeEventsOfDetection(cheatingDetectionId);
    List<MinimalSolveTimeDetectionEvent> result = new ArrayList<>();

    for (var event : minimalSolveTimeEvents) {
      if (!Collections.disjoint(
          detectionEventService.findAllParticipantsOfEvent(event.getId()).stream()
              .map(DetectionEventParticipant::getUserId)
              .toList(),
          participants)) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Finds every correct submission of the cheating detection's training instance solved faster than
   * its level's minimal solve time, and persists a {@link MinimalSolveTimeDetectionEvent} per level
   * carrying every such submission found for that level.
   *
   * @param cd the cheating detection whose training instance is scanned
   */
  void executeCheatingDetectionOfMinimalSolveTime(CheatingDetection cd) {
    Map<Long, List<Submission>> suspiciousSubmissionsByLevel = new HashMap<>();
    Map<Long, Long> submissionTimes = new HashMap<>();
    aggregateMinimalSolveTimeSubmissionsByLevels(cd, suspiciousSubmissionsByLevel, submissionTimes);
    generateMinimalSolveTimeEvents(cd, suspiciousSubmissionsByLevel, submissionTimes);
  }

  /**
   * Marks the submission's run as having a detection event, then persists a {@link
   * MinimalSolveTimeDetectionEvent} carrying the given solve time and participants
   */
  private void auditMinimalSolveTimeEvent(
      Submission submission,
      CheatingDetection cd,
      Set<DetectionEventParticipant> participants,
      Long minimalSolveTime) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    MinimalSolveTimeDetectionEvent event = new MinimalSolveTimeDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.MINIMAL_SOLVE_TIME, participants.size());
    event.setMinimalSolveTime(minimalSolveTime);
    event.setParticipants(generateParticipantString(participants));
    detectionEventService.saveParticipants(
        participants, minimalSolveTimeDetectionEventRepository.save(event).getId(), cd.getId());
  }

  /**
   * Walks every correct submission of the training instance, ordered by training run then date,
   * timing each submission whose level carries a minimal solve time against the moment its trainee
   * started that level: the run's start time when the current submission's run differs from the
   * previous correct submission's run (eligible or not), otherwise the date of that previous
   * correct submission. A submission timed under its level's minimal solve time (in minutes,
   * converted to seconds) is added to {@code detectedByLevel} under its level id and to {@code
   * submissionTimes} under its own id.
   */
  private void aggregateMinimalSolveTimeSubmissionsByLevels(
      CheatingDetection cd,
      Map<Long, List<Submission>> detectedByLevel,
      Map<Long, Long> submissionTimes) {
    boolean isNewParticipant = true;
    LocalDateTime levelStart;
    Submission current;
    Submission previous = new Submission();
    for (Submission submission :
        submissionRepository.getCorrectSubmissionsOfTrainingInstance(cd.getTrainingInstanceId())) {
      current = submission;
      if (current.getLevel().getMinimalPossibleSolveTime() != null) {
        if (isNewParticipant) {
          levelStart = current.getTrainingRun().getStartTime();
          isNewParticipant = false;
        } else {
          if (current.getTrainingRun().equals(previous.getTrainingRun())) {
            levelStart = previous.getDate();
          } else {
            levelStart = current.getTrainingRun().getStartTime();
          }
        }
        long levelDuration = Duration.between(levelStart, current.getDate()).toSeconds();
        if (levelDuration < current.getLevel().getMinimalPossibleSolveTime() * 60) {
          addMinimalSolveTimeDataToMaps(detectedByLevel, submissionTimes, current, levelDuration);
        }
      }
      previous = current;
    }
  }

  /**
   * Appends the submission to the level's list in {@code detectedByLevel}, creating it if absent,
   * and records its solve duration in {@code submissionTimes} under its submission id.
   */
  private static void addMinimalSolveTimeDataToMaps(
      Map<Long, List<Submission>> detectedByLevel,
      Map<Long, Long> submissionTimes,
      Submission current,
      long levelDuration) {
    var submissions = detectedByLevel.get(current.getLevel().getId());
    if (submissions == null) {
      submissions = new ArrayList<>();
    }
    submissions.add(current);
    detectedByLevel.put(current.getLevel().getId(), submissions);
    submissionTimes.put(current.getId(), levelDuration);
  }

  /**
   * For each level in {@code suspiciousSubmissionsByLevel}, marks every implicated run as having a
   * detection event, builds one participant per submission carrying its recorded solve time, and
   * persists a single {@link MinimalSolveTimeDetectionEvent} for the level regardless of how many
   * trainees ended up as participants.
   */
  private void generateMinimalSolveTimeEvents(
      CheatingDetection cd,
      Map<Long, List<Submission>> suspiciousSubmissionsByLevel,
      Map<Long, Long> submissionTimes) {
    Set<DetectionEventParticipant> participants;
    for (var submissions : suspiciousSubmissionsByLevel.entrySet()) {
      participants = new HashSet<>();
      for (var submission : submissions.getValue()) {
        trainingRunService.auditRunHasDetectionEvent(submission.getTrainingRun());
        participants.add(
            extractParticipant(
                submission,
                true,
                submissionTimes.get(submission.getId()),
                detectionEventService.getUserFullName(submission)));
      }
      Submission s = submissions.getValue().get(0);
      auditMinimalSolveTimeEvent(
          s, cd, participants, s.getLevel().getMinimalPossibleSolveTime() * 60);
    }
  }
}
