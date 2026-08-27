package cz.cyberrange.platform.training.service.services.detection;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectedForbiddenCommandRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectionEventParticipantRepository;
import cz.cyberrange.platform.training.service.services.UserService;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Reads and persists detection event rows and their participants, on behalf of the per-kind
 * detectors and the sweep's own service and export path. Holds no detection logic of its own.
 */
@Service
public class DetectionEventService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final AbstractDetectionEventRepository detectionEventRepository;
  private final DetectionEventParticipantRepository detectionEventParticipantRepository;
  private final DetectedForbiddenCommandRepository detectedForbiddenCommandRepository;
  private final UserService userService;

  @Autowired
  public DetectionEventService(
      AbstractDetectionEventRepository abstractDetectionEventRepository,
      DetectionEventParticipantRepository detectionEventParticipantRepository,
      DetectedForbiddenCommandRepository detectedForbiddenCommandRepository,
      UserService userService) {
    this.detectionEventRepository = abstractDetectionEventRepository;
    this.detectionEventParticipantRepository = detectionEventParticipantRepository;
    this.detectedForbiddenCommandRepository = detectedForbiddenCommandRepository;
    this.userService = userService;
  }

  /**
   * Deletes every detection event of one sweep in a single bulk statement, across every finding
   * kind. Leaves the sweep's participant and detected-forbidden-command rows untouched.
   *
   * @param cheatingDetectionId the sweep whose events are deleted
   */
  public void deleteDetectionEvents(Long cheatingDetectionId) {
    detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
  }

  /**
   * Returns, as one page of distinct rows, the detection events of one sweep that also satisfy
   * the given predicate.
   *
   * @param cheatingDetectionId the sweep whose events are returned
   * @param pageable the page to return
   * @param predicate an extra condition ANDed onto the sweep filter
   * @return the matching page of detection events
   */
  public Page<AbstractDetectionEvent> findAllDetectionEventsOfCheatingDetection(
      Long cheatingDetectionId, Pageable pageable, Predicate predicate) {
    return detectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId, pageable, predicate);
  }

  /**
   * Returns every participant of one detection event, ordered by the moment their submission
   * occurred.
   *
   * @param eventId the detection event whose participants are returned
   * @return the matching participants
   */
  public List<DetectionEventParticipant> findAllParticipantsOfEvent(Long eventId) {
    return detectionEventParticipantRepository.findAllByEventId(eventId);
  }

  /**
   * Returns, as one page, the detected forbidden commands of one detection event, in no defined
   * order.
   *
   * @param eventId the detection event whose matched commands are returned
   * @param pageable the page to return
   * @return the matching page of detected forbidden commands
   */
  public Page<DetectedForbiddenCommand> findAllForbiddenCommandsOfDetectionEvent(
      Long eventId, Pageable pageable) {
    return detectedForbiddenCommandRepository.findAllByEventId(eventId, pageable);
  }

  /**
   * Returns every detected forbidden command of one detection event, in no defined order.
   *
   * @param eventId the detection event whose matched commands are returned
   * @return the matching detected forbidden commands
   */
  public List<DetectedForbiddenCommand> findAllForbiddenCommandsOfDetectionEvent(Long eventId) {
    return detectedForbiddenCommandRepository.findAllByEventId(eventId);
  }

  /**
   * Returns the detection event with the given primary key, as its concrete finding subtype.
   *
   * @param eventId the primary key of the detection event
   * @return the matching detection event
   */
  public AbstractDetectionEvent findDetectionEventById(Long eventId) {
    return detectionEventRepository.findDetectionEventById(eventId);
  }

  /**
   * Stamps each participant with the detection event and sweep it belongs to, then saves it.
   *
   * @param participants the participants to stamp and save
   * @param eventId the detection event the participants are implicated in
   * @param cheatingDetectionId the sweep the participants belong to
   */
  void saveParticipants(
      Set<DetectionEventParticipant> participants, Long eventId, Long cheatingDetectionId) {
    for (var participant : participants) {
      participant.setDetectionEventId(eventId);
      participant.setCheatingDetectionId(cheatingDetectionId);
      detectionEventParticipantRepository.save(participant);
    }
  }

  /**
   * Returns, as one page, the participants of one detection event, ordered by the moment their
   * submission occurred.
   *
   * @param eventId the detection event whose participants are returned
   * @param pageable the page to return
   * @return the matching page of participants
   */
  public Page<DetectionEventParticipant> findAllParticipantsOfEvent(
      Long eventId, Pageable pageable) {
    return detectionEventParticipantRepository.findAllByEventId(eventId, pageable);
  }

  /**
   * Asks the user-and-group service for the display name of the trainee who made the submission,
   * resolved through the trainee's {@code userRefId} rather than the local key of their user row.
   *
   * @param currentSubmission the submission whose trainee's name is resolved
   * @return that trainee's display name
   */
  String getUserFullName(Submission currentSubmission) {
    return userService
        .getUserRefDTOByUserRefId(
            currentSubmission.getTrainingRun().getParticipantRef().getUserRefId())
        .getUserRefFullName();
  }
}
