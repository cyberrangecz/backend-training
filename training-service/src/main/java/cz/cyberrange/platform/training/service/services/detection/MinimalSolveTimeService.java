package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.extractParticipant;
import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.generateParticipantString;

import cz.cyberrange.platform.training.opensearch.events.training.model.LevelStarted;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Detects a correct submission entered faster than a training level's configured minimal solve
 * time. Every correct submission of a training instance is timed against when its trainee started
 * the level, read from the run's level-started audit event in OpenSearch (or, without that event,
 * the date of the run's previous correct submission, or the run's start time when there is none); a
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
  private final TrainingEventsService trainingEventsService;

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
      DetectionEventService detectionEventService,
      TrainingEventsService trainingEventsService) {
    this.submissionRepository = submissionRepository;
    this.minimalSolveTimeDetectionEventRepository = minimalSolveTimeDetectionEventRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.detectionEventService = detectionEventService;
    this.trainingEventsService = trainingEventsService;
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
   * timing each submission whose level carries a minimal solve time from the moment its run started
   * that level, as recorded by the run's level-started audit event. Without such an event the
   * submission is timed from the date of the previous correct submission of the same run, or from
   * the run's start time when there is none. A submission timed under its level's minimal solve
   * time (in minutes, converted to seconds) is added to {@code detectedByLevel} under its level id
   * and to {@code submissionTimes} under its own id.
   */
  private void aggregateMinimalSolveTimeSubmissionsByLevels(
      CheatingDetection cd,
      Map<Long, List<Submission>> detectedByLevel,
      Map<Long, Long> submissionTimes) {
    Submission previous = null;
    for (Submission current :
        submissionRepository.getCorrectSubmissionsOfTrainingInstance(cd.getTrainingInstanceId())) {
      if (current.getLevel().getMinimalPossibleSolveTime() != null) {
        LocalDateTime earliestLevelStart =
            previous != null && current.getTrainingRun().equals(previous.getTrainingRun())
                ? previous.getDate()
                : current.getTrainingRun().getStartTime();
        LocalDateTime levelStart =
            findLevelStartTime(cd.getTrainingInstanceId(), current, earliestLevelStart)
                .orElse(earliestLevelStart);
        long levelDuration = Duration.between(levelStart, current.getDate()).toSeconds();
        if (levelDuration < current.getLevel().getMinimalPossibleSolveTime() * 60) {
          addMinimalSolveTimeDataToMaps(detectedByLevel, submissionTimes, current, levelDuration);
        }
      }
      previous = current;
    }
  }

  /**
   * Returns when the submission's run started the submission's level, taken from the run's
   * level-started audit event recorded at or after {@code earliestLevelStart}, or empty when no
   * such event is found
   */
  private Optional<LocalDateTime> findLevelStartTime(
      Long trainingInstanceId, Submission submission, LocalDateTime earliestLevelStart) {
    TrainingRun run = submission.getTrainingRun();
    long levelId = submission.getLevel().getId();
    long sinceTimestamp = earliestLevelStart.toInstant(ZoneOffset.UTC).toEpochMilli() - 1;
    return trainingEventsService
        .findFilteredTrainingEvents(
            trainingInstanceId,
            LevelStarted.TYPE,
            sinceTimestamp,
            run.getParticipantRef().getUserRefId())
        .stream()
        .filter(event -> event.getTrainingRunId() == run.getId() && event.getLevel() == levelId)
        .findFirst()
        .map(event -> Instant.ofEpochMilli(event.getTimestamp()))
        .map(timestamp -> LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC));
  }

  /**
   * Appends the submission to the level's list in {@code detectedByLevel}, creating it if absent,
   * and records its solve duration in {@code submissionTimes} under its submission id
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
   * trainees ended up as participants
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
