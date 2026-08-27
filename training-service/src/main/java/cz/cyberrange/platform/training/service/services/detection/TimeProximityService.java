package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.extractParticipant;
import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.generateParticipantString;

import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.TimeProximityDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Detects a run of correct submissions on the same training level that arrived within a configured
 * time threshold of one another. Every level of a training instance is scanned independently: its
 * correct submissions, ordered by date, are chained into a group while each consecutive pair is
 * closer together than {@code cd.getProximityThreshold()}; a group naming more than one trainee is
 * recorded as a {@link TimeProximityDetectionEvent}. That check is bypassed on one path: when a
 * chain breaks because the next pair exceeds the threshold, the accumulated group is audited
 * unconditionally, so an event can end up naming only one trainee.
 */
@Service
public class TimeProximityService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final TrainingLevelRepository trainingLevelRepository;
  private final SubmissionRepository submissionRepository;
  private final TimeProximityDetectionEventRepository timeProximityDetectionEventRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final TrainingRunService trainingRunService;
  private final TrainingInstanceService trainingInstanceService;
  private final DetectionEventService detectionEventService;

  /**
   * Creates the service with the repositories and collaborators it uses to chain a level's correct
   * submissions into time-proximity groups
   */
  @Autowired
  public TimeProximityService(
      TrainingLevelRepository trainingLevelRepository,
      SubmissionRepository submissionRepository,
      TimeProximityDetectionEventRepository timeProximityDetectionEventRepository,
      TrainingRunRepository trainingRunRepository,
      TrainingRunService trainingRunService,
      TrainingInstanceService trainingInstanceService,
      DetectionEventService detectionEventService) {
    this.trainingLevelRepository = trainingLevelRepository;
    this.submissionRepository = submissionRepository;
    this.timeProximityDetectionEventRepository = timeProximityDetectionEventRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.trainingInstanceService = trainingInstanceService;
    this.detectionEventService = detectionEventService;
  }

  /**
   * Finds every time proximity event recorded under the given cheating detection.
   *
   * @param cheatingDetectionId the cheating detection id
   * @return the matching events
   */
  public List<TimeProximityDetectionEvent> findAllTimeProximityEventsOfDetection(
      Long cheatingDetectionId) {
    return timeProximityDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
  }

  /**
   * Finds a time proximity event by its id.
   *
   * @param eventId the event id
   * @return the matching event
   */
  public TimeProximityDetectionEvent findTimeProximityEventById(Long eventId) {
    return timeProximityDetectionEventRepository.findTimeProximityEventById(eventId);
  }

  /**
   * For every level of the cheating detection's training instance, chains its correct submissions
   * by time proximity and, when the chain still open at the end of a level names more than one
   * trainee, persists a {@link TimeProximityDetectionEvent} for it. A chain left open at the end of
   * a level that names only one trainee is not discarded: it and its participant carry over into
   * the next level's scan instead, together with whatever that level's own chaining adds.
   *
   * @param cd the cheating detection whose training instance is scanned
   */
  void executeCheatingDetectionOfTimeProximity(CheatingDetection cd) {
    List<Submission> detectedGroup = new ArrayList<>();
    Set<DetectionEventParticipant> participants = new HashSet<>();

    for (var level :
        trainingLevelRepository.findAllByTrainingDefinitionId(
            trainingInstanceService
                .findById(cd.getTrainingInstanceId())
                .getTrainingDefinition()
                .getId())) {
      generateSuspiciousGroup(cd, detectedGroup, participants, level);
      for (var submission : detectedGroup) {
        generateEventParticipants(participants, submission);
      }
      if (!detectedGroup.isEmpty() && participants.size() > 1) {
        auditTimeProximityEvent(detectedGroup.get(0), cd, participants);
        detectedGroup.clear();
        participants.clear();
      }
    }
  }

  /**
   * Marks the submission's run as having a detection event, and adds the submission's trainee to
   * {@code participants} when not already present.
   */
  private void generateEventParticipants(
      Set<DetectionEventParticipant> participants, Submission submission) {
    DetectionEventParticipant participant =
        extractParticipant(submission, detectionEventService.getUserFullName(submission));
    if (!CheatingDetectionUtils.checkIfContainsParticipant(participants, participant)) {
      participants.add(participant);
    }
    trainingRunService.auditRunHasDetectionEvent(submission.getTrainingRun());
  }

  /**
   * Compares each pair of the level's correct submissions, ordered by date, and grows {@code
   * detectedGroup} while consecutive submissions stay closer together than {@code
   * cd.getProximityThreshold()}. When a pair exceeds the threshold, every submission accumulated so
   * far is turned into a participant and, if the group is non-empty, a {@link
   * TimeProximityDetectionEvent} is persisted for it immediately regardless of how many distinct
   * trainees ended up in {@code participants}; both collections are then cleared. A group still
   * open once every pair has been compared is left in {@code detectedGroup} and {@code
   * participants} for the caller to handle.
   */
  private void generateSuspiciousGroup(
      CheatingDetection cd,
      List<Submission> detectedGroup,
      Set<DetectionEventParticipant> participants,
      TrainingLevel level) {
    List<Submission> submissions;
    submissions =
        submissionRepository.getAllTimeProximitySubmissionsOfLevel(
            cd.getTrainingInstanceId(), level.getId());
    for (int submissionIndex = 1; submissionIndex < submissions.size(); submissionIndex++) {
      int previousIndex = submissionIndex - 1;
      var first = submissions.get(previousIndex);
      var second = submissions.get(submissionIndex);
      long timeProximity = Duration.between(first.getDate(), second.getDate()).toSeconds();
      if (timeProximity < cd.getProximityThreshold()) {
        if (detectedGroup.isEmpty()) {
          detectedGroup.add(first);
        }
        detectedGroup.add(second);
      } else {
        for (var submission : detectedGroup)
          participants.add(
              extractParticipant(submission, detectionEventService.getUserFullName(submission)));
        if (!detectedGroup.isEmpty()) {
          auditTimeProximityEvent(detectedGroup.get(0), cd, participants);
          detectedGroup.clear();
          participants.clear();
        }
      }
    }
  }

  /**
   * Marks the submission's run as having a detection event, then persists a {@link
   * TimeProximityDetectionEvent} for it carrying the cheating detection's proximity threshold and
   * the given participants
   */
  private void auditTimeProximityEvent(
      Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    TimeProximityDetectionEvent event = new TimeProximityDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.TIME_PROXIMITY, participants.size());
    event.setThreshold(cd.getProximityThreshold());
    event.setParticipants(generateParticipantString(participants));
    detectionEventService.saveParticipants(
        participants, timeProximityDetectionEventRepository.save(event).getId(), cd.getId());
  }
}
