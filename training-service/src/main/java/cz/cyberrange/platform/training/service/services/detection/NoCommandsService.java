package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.extractParticipant;
import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.generateParticipantString;

import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.NoCommandsDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Detects a level marked as requiring console commands being solved without any command recorded
 * for it. Scans every training run of the instance, skipping a level whose solution was revealed,
 * and groups every submission caught this way by level id into one finding per level, so a finding
 * can implicate several trainees.
 */
@Service
public class NoCommandsService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final TrainingLevelRepository trainingLevelRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final SubmissionRepository submissionRepository;
  private final NoCommandsDetectionEventRepository noCommandsDetectionEventRepository;
  private final TrainingRunService trainingRunService;
  private final TrainingInstanceService trainingInstanceService;
  private final DetectionEventService detectionEventService;
  private final TrainingEventsService trainingEventsService;
  private final CommandEventsService commandEventsService;

  /**
   * Creates the service with the repositories and collaborators it uses to find a run's completed
   * levels that recorded no console commands
   */
  @Autowired
  public NoCommandsService(
      TrainingLevelRepository trainingLevelRepository,
      TrainingRunRepository trainingRunRepository,
      SubmissionRepository submissionRepository,
      NoCommandsDetectionEventRepository noCommandsDetectionEventRepository,
      TrainingRunService trainingRunService,
      TrainingInstanceService trainingInstanceService,
      DetectionEventService detectionEventService,
      TrainingEventsService trainingEventsService,
      CommandEventsService commandEventsService) {
    this.trainingLevelRepository = trainingLevelRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.submissionRepository = submissionRepository;
    this.noCommandsDetectionEventRepository = noCommandsDetectionEventRepository;
    this.trainingRunService = trainingRunService;
    this.trainingInstanceService = trainingInstanceService;
    this.detectionEventService = detectionEventService;
    this.trainingEventsService = trainingEventsService;
    this.commandEventsService = commandEventsService;
  }

  /**
   * Returns every no-commands finding of one sweep.
   *
   * @param cheatingDetectionId the sweep whose findings are returned
   * @return the matching findings
   */
  public List<NoCommandsDetectionEvent> findAllNoCommandsEventsOfDetection(
      Long cheatingDetectionId) {
    return noCommandsDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
  }

  /**
   * Returns the no-commands finding with the given primary key.
   *
   * @param eventId the primary key of the finding
   * @return the matching finding
   */
  public NoCommandsDetectionEvent findNoCommandsEventById(Long eventId) {
    return noCommandsDetectionEventRepository.findNoCommandsEventById(eventId);
  }

  /**
   * Runs the no-commands detector over every training run of the sweep's instance, then records one
   * finding per level for which at least one submission was caught, implicating every trainee
   * caught on that level.
   *
   * @param cd the sweep being executed
   */
  void executeCheatingDetectionOfNoCommands(CheatingDetection cd) {
    Long trainingInstanceId = cd.getTrainingInstanceId();
    Map<Long, TrainingLevel> trainingLevelsById =
        trainingLevelRepository
            .findAllByTrainingDefinitionId(
                trainingInstanceService
                    .findById(trainingInstanceId)
                    .getTrainingDefinition()
                    .getId())
            .stream()
            .collect(Collectors.toMap(TrainingLevel::getId, level -> level));
    Map<Long, List<Submission>> detectedSubmissionsByLevels = new HashMap<>();
    for (var run :
        new ArrayList<>(trainingRunService.findAllByTrainingInstanceId(trainingInstanceId))) {
      executeNoCommandsDetectionForRun(trainingLevelsById, detectedSubmissionsByLevels, run);
    }
    Set<DetectionEventParticipant> participants;
    for (var submissions : detectedSubmissionsByLevels.entrySet()) {
      participants = new HashSet<>();
      generateParticipantsOfEvent(participants, submissions);
      auditNoCommandsEvent(submissions.getValue().get(0), cd, participants);
    }
  }

  /**
   * Reports whether one of the given audit events records the solution of the submission's level
   * having been revealed.
   */
  private boolean wasSolutionDisplayed(List<AbstractAuditPOJO> events, Submission submission) {
    for (AbstractAuditPOJO event : events) {
      if (event.getLevel() == submission.getLevel().getId()
          && event.getType().contains("SolutionDisplayed")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Builds a participant record for every submission caught on one level and marks each
   * submission's training run as carrying a detection event.
   */
  private void generateParticipantsOfEvent(
      Set<DetectionEventParticipant> participants, Map.Entry<Long, List<Submission>> submissions) {
    for (var submission : submissions.getValue()) {
      participants.add(
          extractParticipant(submission, detectionEventService.getUserFullName(submission)));
      trainingRunService.auditRunHasDetectionEvent(submission.getTrainingRun());
    }
  }

  /**
   * Evaluates one run's correct submissions except the last, each against the interval since the
   * previous correct submission or, for the first one, the run's start. The last correct submission
   * is never evaluated, so a level solved last in a run is never caught by this detector for that
   * run.
   */
  private void executeNoCommandsDetectionForRun(
      Map<Long, TrainingLevel> trainingLevelsById,
      Map<Long, List<Submission>> detectedSubmissionsByLevels,
      TrainingRun run) {
    List<Submission> submissions;
    List<AbstractAuditPOJO> events =
        trainingEventsService.findAllEventsFromTrainingRun(run.getId());
    submissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
    for (int i = 0; i < submissions.size() - 1; i++) {
      evaluateNoCommandsSubmissionsOfTrainingRun(
          trainingLevelsById, detectedSubmissionsByLevels, run, submissions, events, i);
    }
  }

  /**
   * Evaluates one submission against the interval since the previous one (or the run's start).
   * Passes over a level absent from {@code trainingLevelsById}, a level not marked as requiring
   * commands, and a submission whose level had its solution revealed. Otherwise, when no console
   * command was recorded for the run's sandbox in the interval, appends the submission to the
   * accumulated list kept under its level id.
   */
  private void evaluateNoCommandsSubmissionsOfTrainingRun(
      Map<Long, TrainingLevel> trainingLevelsById,
      Map<Long, List<Submission>> detectedSubmissionsByLevels,
      TrainingRun run,
      List<Submission> submissions,
      List<AbstractAuditPOJO> events,
      int i) {
    Submission submission;
    LocalDateTime from;
    submission = submissions.get(i);
    from = (i == 0) ? run.getStartTime() : submissions.get(i - 1).getDate();
    Long currentId = submission.getLevel().getId();
    if (!trainingLevelsById.containsKey(currentId)
        || !trainingLevelsById.get(currentId).isCommandsRequired()) {
      return;
    }
    if (wasSolutionDisplayed(events, submission)) {
      return;
    }
    if (evalCheatOfNoCommands(run.getSandboxInstanceRefId(), from, submission)) {
      if (detectedSubmissionsByLevels.containsKey(submission.getLevel().getId())) {
        var tempSubmissions = detectedSubmissionsByLevels.get(submission.getLevel().getId());
        tempSubmissions.add(submission);
        detectedSubmissionsByLevels.put(submission.getLevel().getId(), tempSubmissions);
      } else {
        List<Submission> subs = new ArrayList<>();
        subs.add(submission);
        detectedSubmissionsByLevels.put(submission.getLevel().getId(), subs);
      }
    }
  }

  /**
   * Reports whether no console command was recorded for the run's sandbox between {@code from} and
   * the submission's own date
   */
  private boolean evalCheatOfNoCommands(
      String sandboxId, LocalDateTime from, Submission submission) {
    long fromMilli = from.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    long toMilli = submission.getDate().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    return commandEventsService
        .findAllConsoleCommandsBySandboxAndTimeRange(sandboxId, fromMilli, toMilli)
        .isEmpty();
  }

  /**
   * Marks the training run of the first submission as carrying a detection event, then records a
   * finding for that submission's level implicating every given participant.
   */
  private void auditNoCommandsEvent(
      Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    NoCommandsDetectionEvent event = new NoCommandsDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.NO_COMMANDS, participants.size());
    event.setParticipants(generateParticipantString(participants));
    detectionEventService.saveParticipants(
        participants, noCommandsDetectionEventRepository.save(event).getId(), cd.getId());
  }
}
