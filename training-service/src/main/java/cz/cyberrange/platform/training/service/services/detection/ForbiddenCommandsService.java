package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.extractParticipant;

import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommand;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.CommandType;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectedForbiddenCommandRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectionEventParticipantRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.ForbiddenCommandsDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Detects a trainee running one of the sweep's forbidden console commands. Scans every training
 * run of the instance over the intervals between its correct submissions, comparing the console
 * commands recorded for the run's sandbox in each interval against the sweep's forbidden command
 * list. Every finding implicates exactly one trainee.
 */
@Service
public class ForbiddenCommandsService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final SubmissionRepository submissionRepository;
  private final ForbiddenCommandsDetectionEventRepository forbiddenCommandsDetectionEventRepository;
  private final DetectionEventParticipantRepository detectionEventParticipantRepository;
  private final DetectedForbiddenCommandRepository detectedForbiddenCommandRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final TrainingRunService trainingRunService;
  private final DetectionEventService detectionEventService;
  private final CommandEventsService commandEventsService;

  @Autowired
  public ForbiddenCommandsService(
      SubmissionRepository submissionRepository,
      ForbiddenCommandsDetectionEventRepository forbiddenCommandsDetectionEventRepository,
      DetectionEventParticipantRepository detectionEventParticipantRepository,
      DetectedForbiddenCommandRepository detectedForbiddenCommandRepository,
      TrainingRunRepository trainingRunRepository,
      TrainingRunService trainingRunService,
      DetectionEventService detectionEventService,
      CommandEventsService commandEventsService) {
    this.submissionRepository = submissionRepository;
    this.forbiddenCommandsDetectionEventRepository = forbiddenCommandsDetectionEventRepository;
    this.detectionEventParticipantRepository = detectionEventParticipantRepository;
    this.detectedForbiddenCommandRepository = detectedForbiddenCommandRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.trainingRunService = trainingRunService;
    this.detectionEventService = detectionEventService;
    this.commandEventsService = commandEventsService;
  }

  /**
   * Returns every forbidden-commands finding of one sweep.
   *
   * @param cheatingDetectionId the sweep whose findings are returned
   * @return the matching findings
   */
  public List<ForbiddenCommandsDetectionEvent> findAllForbiddenCommandsEventsOfDetection(
      Long cheatingDetectionId) {
    return forbiddenCommandsDetectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId);
  }

  /**
   * Returns the forbidden-commands finding with the given primary key.
   *
   * @param eventId the primary key of the finding
   * @return the matching finding
   */
  public ForbiddenCommandsDetectionEvent findForbiddenCommandsEventById(Long eventId) {
    return forbiddenCommandsDetectionEventRepository.findForbiddenCommandsEventById(eventId);
  }

  /**
   * Runs the forbidden-commands detector over every training run of the sweep's instance.
   *
   * @param cd the sweep being executed
   */
  void executeCheatingDetectionOfForbiddenCommands(CheatingDetection cd) {
    for (var run : trainingRunService.findAllByTrainingInstanceId(cd.getTrainingInstanceId())) {
      executeForbiddenCommandsMethodForRun(cd, run);
    }
  }

  /**
   * Evaluates one run's correct submissions in sequence, each against the interval since the
   * previous correct submission (or the run's start, for the first one), plus a trailing interval
   * from the last correct submission to now while the run is still running. A run with no correct
   * submissions is skipped.
   */
  private void executeForbiddenCommandsMethodForRun(CheatingDetection cd, TrainingRun run) {
    List<Submission> submissions;
    submissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
    if (submissions.isEmpty()) {
      return;
    }
    for (int i = 0; i < submissions.size() + 1; i++) {
      evaluateForbiddenCommandsForSubmission(cd, run, submissions, i);
    }
  }

  /**
   * Resolves the interval one submission index covers and generates a finding from it. The index
   * one past the last submission covers the trailing interval to now, and is skipped once the run
   * is no longer running.
   */
  private void evaluateForbiddenCommandsForSubmission(
      CheatingDetection cd, TrainingRun run, List<Submission> submissions, int submissionIndex) {
    LocalDateTime from;
    LocalDateTime to;
    Submission currentSubmission;
    if (submissionIndex == submissions.size()) {
      currentSubmission = submissions.get(submissionIndex - 1);
      if (run.getState() == TRState.RUNNING) {
        from = currentSubmission.getDate();
        to = LocalDateTime.now();
      } else {
        return;
      }
    } else {
      currentSubmission = submissions.get(submissionIndex);
      from =
          (submissionIndex == 0)
              ? run.getStartTime()
              : submissions.get(submissionIndex - 1).getDate();
      to = currentSubmission.getDate();
    }
    generateForbiddenCommandEvent(cd, run, from, to, currentSubmission);
  }

  /**
   * Matches the sandbox's console commands over the interval against the sweep's forbidden
   * command list and, only if at least one match is found, records a finding implicating the
   * trainee behind the submission.
   */
  private void generateForbiddenCommandEvent(
      CheatingDetection cd,
      TrainingRun run,
      LocalDateTime from,
      LocalDateTime to,
      Submission currentSubmission) {
    List<DetectedForbiddenCommand> forbiddenCommands =
        evaluateForbiddenCommands(
            cd.getCommands(), getSubmittedCommandsFromRunInInterval(run, from, to));
    if (!forbiddenCommands.isEmpty()) {
      DetectionEventParticipant participant =
          extractParticipant(
              currentSubmission, detectionEventService.getUserFullName(currentSubmission));
      auditForbiddenCommandsEvent(currentSubmission, cd, participant, forbiddenCommands);
    }
  }

  private List<TrainingCommand> getSubmittedCommandsFromRunInInterval(
      TrainingRun run, LocalDateTime from, LocalDateTime to) {
    List<TrainingCommand> submittedCommands;
    submittedCommands =
        commandEventsService.findAllConsoleCommandsBySandboxAndTimeRange(
            run.getSandboxInstanceRefId(),
            from.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
            to.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
    return submittedCommands;
  }

  /**
   * Checks every submitted command against every forbidden command and collects one detected
   * entry per match; a submitted command matching several forbidden commands yields several
   * entries.
   */
  private List<DetectedForbiddenCommand> evaluateForbiddenCommands(
      List<ForbiddenCommand> forbiddenCommands, List<TrainingCommand> submittedCommands) {
    List<DetectedForbiddenCommand> commandsList = new ArrayList<>();
    for (TrainingCommand commandObj : submittedCommands) {
      if (commandObjContainsNull(commandObj)) continue;
      String command = commandObj.getCommand();
      LocalDateTime localDateTime = commandObj.getTimestamp();

      for (var forbiddenCommand : forbiddenCommands) {
        detectForbiddenCommands(commandsList, commandObj, command, localDateTime, forbiddenCommand);
      }
    }
    return commandsList;
  }

  /**
   * Adds a detected entry to {@code commandsList} when the submitted command's type matches the
   * forbidden command's type and its text contains the forbidden command's text.
   */
  private static void detectForbiddenCommands(
      List<DetectedForbiddenCommand> commandsList,
      TrainingCommand commandObj,
      String command,
      LocalDateTime localDateTime,
      ForbiddenCommand forbiddenCommand) {
    String type = forbiddenCommand.getType() == CommandType.BASH ? "bash-command" : "msf-command";
    if (commandObj.getCmdType().equals(type)
        && command != null
        && command.contains(forbiddenCommand.getCommand())) {
      DetectedForbiddenCommand detectedCommand = new DetectedForbiddenCommand();
      detectedCommand.setCommand(command);
      detectedCommand.setType(forbiddenCommand.getType());
      detectedCommand.setHostname(commandObj.getHostname());
      detectedCommand.setOccurredAt(localDateTime);
      commandsList.add(detectedCommand);
    }
  }

  private static boolean commandObjContainsNull(TrainingCommand commandObj) {
    return commandObj == null
        || commandObj.getCommand() == null
        || commandObj.getCmdType() == null
        || commandObj.getHostname() == null;
  }

  private void auditForbiddenCommandsEvent(
      Submission submission,
      CheatingDetection cd,
      DetectionEventParticipant participant,
      List<DetectedForbiddenCommand> detectedForbiddenCommands) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    ForbiddenCommandsDetectionEvent event = new ForbiddenCommandsDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.FORBIDDEN_COMMANDS, 1);
    event.setCommandCount(detectedForbiddenCommands.size());
    event.setTrainingRunId(submission.getTrainingRun().getId());
    event.setParticipants(participant.getParticipantName());
    Long eventId = forbiddenCommandsDetectionEventRepository.save(event).getId();
    participant.setDetectionEventId(eventId);
    detectedForbiddenCommands.forEach(
        command -> {
          command.setDetectionEventId(eventId);
          detectedForbiddenCommandRepository.save(command);
        });
    participant.setCheatingDetectionId(cd.getId());
    detectionEventParticipantRepository.save(participant);
  }
}
