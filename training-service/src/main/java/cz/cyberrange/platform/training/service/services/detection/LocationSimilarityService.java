package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.*;

import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.LocationSimilarityDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Detects groups of trainees whose submissions on the same training level came from the same or an
 * equivalent IP address. Every level of a training instance is scanned independently: its
 * submissions are grouped by resolved IP address, and a group naming more than one trainee is
 * recorded as a {@link LocationSimilarityDetectionEvent}.
 */
@Service
public class LocationSimilarityService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final TrainingLevelRepository trainingLevelRepository;
  private final SubmissionRepository submissionRepository;
  private final LocationSimilarityDetectionEventRepository
      locationSimilarityDetectionEventRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final TrainingRunService trainingRunService;
  private final TrainingInstanceService trainingInstanceService;
  private final DetectionEventService detectionEventService;
  @Autowired Environment environment;

  /**
   * Creates the service with the repositories and collaborators it uses to group a level's
   * submissions by resolved IP address
   */
  @Autowired
  public LocationSimilarityService(
      TrainingLevelRepository trainingLevelRepository,
      SubmissionRepository submissionRepository,
      LocationSimilarityDetectionEventRepository locationSimilarityDetectionEventRepository,
      TrainingRunRepository trainingRunRepository,
      TrainingRunService trainingRunService,
      TrainingInstanceService trainingInstanceService,
      DetectionEventService detectionEventService) {
    this.trainingLevelRepository = trainingLevelRepository;
    this.submissionRepository = submissionRepository;
    this.locationSimilarityDetectionEventRepository = locationSimilarityDetectionEventRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.trainingInstanceService = trainingInstanceService;
    this.detectionEventService = detectionEventService;
  }

  /**
   * Finds every location similarity event recorded under the given cheating detection.
   *
   * @param cheatingDetectionId the cheating detection id
   * @return the matching events
   */
  public List<LocationSimilarityDetectionEvent> findAllLocationSimilarityEventsOfDetection(
      Long cheatingDetectionId) {
    return locationSimilarityDetectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId);
  }

  /**
   * Finds a location similarity event by its id.
   *
   * @param eventId the event id
   * @return the matching event
   */
  public LocationSimilarityDetectionEvent findLocationSimilarityEventById(Long eventId) {
    return locationSimilarityDetectionEventRepository.findLocationSimilarityEventById(eventId);
  }

  /**
   * For every level of the cheating detection's training instance, groups that level's submissions
   * by IP-address similarity and persists a {@link LocationSimilarityDetectionEvent} for each group
   * naming more than one trainee.
   *
   * @param cd the cheating detection whose training instance is scanned
   */
  void executeCheatingDetectionOfLocationSimilarity(CheatingDetection cd) {
    Long trainingInstanceId = cd.getTrainingInstanceId();
    trainingLevelRepository
        .findAllByTrainingDefinitionId(
            trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId())
        .stream()
        .map(
            level ->
                submissionRepository.getSubmissionsByLevelAndInstance(
                    trainingInstanceId, level.getId()))
        .forEach(submissions -> evaluateLocationSimilarityByLevels(submissions, cd));
  }

  /** Groups the level's submissions by IP-address similarity and audits each resulting group */
  private void evaluateLocationSimilarityByLevels(
      List<Submission> submissions, CheatingDetection cd) {

    List<List<Submission>> groups = new ArrayList<>();
    generateLocationSimilarityGroups(submissions, groups);
    for (var group : groups) {
      generateEventFromGroup(cd, group);
    }
  }

  /**
   * Marks every run in the group as having a detection event, and, when the group names more than
   * one distinct trainee, persists a {@link LocationSimilarityDetectionEvent} for it. A group of
   * fewer than 2 submissions is skipped entirely.
   */
  private void generateEventFromGroup(CheatingDetection cd, List<Submission> group) {
    List<Long> runIds;
    Set<DetectionEventParticipant> participants;
    if (group.size() < 2) {
      return;
    }
    participants = new HashSet<>();
    runIds = new ArrayList<>();
    for (var submission : group) {
      Long submissionRunId = submission.getTrainingRun().getId();
      if (!runIds.contains(submissionRunId)) {
        DetectionEventParticipant participant =
            extractParticipant(submission, detectionEventService.getUserFullName(submission));
        if (!checkIfContainsParticipant(participants, participant)) {
          participants.add(participant);
        }
        runIds.add(submissionRunId);
      }
      trainingRunService.auditRunHasDetectionEvent(submission.getTrainingRun());
    }
    if (participants.size() > 1) {
      auditLocationSimilarityEvent(group.get(0), cd, participants);
    }
  }

  /**
   * Assigns each submission to every existing group whose first submission's IP address it resolves
   * as similar to. A submission matching no group starts a new one, unless the level being scanned
   * carries exactly one submission in total. A submission can end up added to several groups at
   * once.
   */
  private void generateLocationSimilarityGroups(
      List<Submission> submissions, List<List<Submission>> groups) {
    boolean hasSimilarIPToExistingGroup;
    for (var submission : submissions) {
      hasSimilarIPToExistingGroup = false;
      for (var group : groups) {
        if (checkLocationSimilarity(group.get(0).getIpAddress(), submission.getIpAddress())) {
          group.add(submission);
          hasSimilarIPToExistingGroup = true;
        }
      }
      if (hasSimilarIPToExistingGroup || submissions.size() == 1) {
        continue;
      }
      groups.add(
          new ArrayList<>() {
            {
              add(submission);
            }
          });
    }
  }

  /**
   * Reports whether the two addresses resolve to the same {@link InetAddress}, falling back to a
   * literal string comparison when either fails to resolve. Reports {@code false} when either
   * address is {@code null}.
   */
  private boolean checkLocationSimilarity(String ip, String otherIp) {
    if (ip != null && otherIp != null) {
      try {
        InetAddress firstIp = InetAddress.getByName(ip);
        InetAddress secondIp = InetAddress.getByName(otherIp);
        return firstIp.equals(secondIp);
      } catch (UnknownHostException e) {
        return ip.equals(otherIp);
      }
    }
    return false;
  }

  /**
   * Marks the group's first submission's run as having a detection event, then persists a {@link
   * LocationSimilarityDetectionEvent} built from that submission and saves each of the given
   * participants against it
   */
  private void auditLocationSimilarityEvent(
      Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    LocationSimilarityDetectionEvent event = new LocationSimilarityDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.LOCATION_SIMILARITY, participants.size());
    extractLocationSimilaritySpecificInfo(submission, event);
    event.setParticipants(generateParticipantString(participants));
    detectionEventService.saveParticipants(
        participants, locationSimilarityDetectionEventRepository.save(event).getId(), cd.getId());
  }

  /**
   * Resolves the submission's IP address and the {@code server.address} property to host names and
   * sets {@code addressDeploy} to whether the two host names are equal. When either address fails
   * to resolve, sets {@code addressDeploy} to {@code false} and {@code dns} to the literal {@code
   * "unspecified"} instead of leaving it unset. Sets {@code ipAddress} to the submission's raw IP
   * address unconditionally.
   */
  private void extractLocationSimilaritySpecificInfo(
      Submission submission, LocationSimilarityDetectionEvent event) {
    String submissionDomainName;
    try {
      InetAddress envAddress = InetAddress.getByName(environment.getProperty("server.address"));
      submissionDomainName = InetAddress.getByName(submission.getIpAddress()).getHostName();
      event.setAddressDeploy(envAddress.getHostName().equals(submissionDomainName));
    } catch (UnknownHostException e) {
      submissionDomainName = "unspecified";
      event.setAddressDeploy(false);
    }
    event.setDns(submissionDomainName);
    event.setIpAddress(submission.getIpAddress());
  }
}
