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

  /**
   * Instantiates a new Cheating detection service.
   *
   * @param submissionRepository the submission repository
   * @param forbiddenCommandsDetectionEventRepository the forbidden commands detection event
   *     repository
   * @param detectionEventParticipantRepository the detection event participant repository
   * @param detectedForbiddenCommandRepository the detected forbidden commands repository
   * @param trainingRunRepository the training run repository
   * @param trainingRunService the training run service
   * @param detectionEventService the detection events service
   */
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
   * finds all forbidden command events of cheating detection
   *
   * @param cheatingDetectionId the cheating detection id
   * @return list of events
   */
  public List<ForbiddenCommandsDetectionEvent> findAllForbiddenCommandsEventsOfDetection(
      Long cheatingDetectionId) {
    return forbiddenCommandsDetectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId);
  }

  /**
   * finds forbidden command event by id
   *
   * @param eventId the event id
   * @return the event
   */
  public ForbiddenCommandsDetectionEvent findForbiddenCommandsEventById(Long eventId) {
    return forbiddenCommandsDetectionEventRepository.findForbiddenCommandsEventById(eventId);
  }

  /**
   * Executes a cheating detection of type FORBIDDEN_COMMANDS
   *
   * @param cd the cheating detection
   */
  void executeCheatingDetectionOfForbiddenCommands(CheatingDetection cd) {
    for (var run : trainingRunService.findAllByTrainingInstanceId(cd.getTrainingInstanceId())) {
      executeForbiddenCommandsMethodForRun(cd, run);
    }
  }

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
