package cz.cyberrange.platform.training.rest.integration;

import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.enums.CheatingDetectionState;
import cz.cyberrange.platform.training.persistence.model.enums.CommandType;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.model.enums.SubmissionType;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.CheatingDetectionRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectedForbiddenCommandRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.DetectionEventParticipantRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Persists the training definitions, instances, runs, submissions and cheating detection findings
 * the training instance and cheating detection integration tests read back through the REST API.
 */
@Component
class TrainingInstanceDataSeeder {

  private static final long AUTHOR_USER_REF_ID_BASE = 5_000_000L;

  private long authorSequence;

  private final UserRefRepository userRefRepository;
  private final TrainingDefinitionRepository trainingDefinitionRepository;
  private final TrainingLevelRepository trainingLevelRepository;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final SubmissionRepository submissionRepository;
  private final CheatingDetectionRepository cheatingDetectionRepository;
  private final AbstractDetectionEventRepository abstractDetectionEventRepository;
  private final DetectionEventParticipantRepository detectionEventParticipantRepository;
  private final DetectedForbiddenCommandRepository detectedForbiddenCommandRepository;

  @Autowired
  TrainingInstanceDataSeeder(
      UserRefRepository userRefRepository,
      TrainingDefinitionRepository trainingDefinitionRepository,
      TrainingLevelRepository trainingLevelRepository,
      TrainingInstanceRepository trainingInstanceRepository,
      TrainingRunRepository trainingRunRepository,
      SubmissionRepository submissionRepository,
      CheatingDetectionRepository cheatingDetectionRepository,
      AbstractDetectionEventRepository abstractDetectionEventRepository,
      DetectionEventParticipantRepository detectionEventParticipantRepository,
      DetectedForbiddenCommandRepository detectedForbiddenCommandRepository) {
    this.userRefRepository = userRefRepository;
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.trainingLevelRepository = trainingLevelRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.submissionRepository = submissionRepository;
    this.cheatingDetectionRepository = cheatingDetectionRepository;
    this.abstractDetectionEventRepository = abstractDetectionEventRepository;
    this.detectionEventParticipantRepository = detectionEventParticipantRepository;
    this.detectedForbiddenCommandRepository = detectedForbiddenCommandRepository;
  }

  static LocalDateTime now() {
    return LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
  }

  UserRef userRef(long userRefId) {
    UserRef userRef = new UserRef();
    userRef.setUserRefId(userRefId);
    return userRefRepository.save(userRef);
  }

  TrainingDefinition definition(String title) {
    TrainingDefinition definition = new TrainingDefinition();
    definition.setTitle(title);
    definition.setState(TDState.RELEASED);
    definition.setLastEdited(now());
    definition.setLastEditedBy("Seed Author");
    definition.setCreatedAt(now());
    definition.setAuthors(Set.of(userRef(AUTHOR_USER_REF_ID_BASE + authorSequence++)));
    return trainingDefinitionRepository.save(definition);
  }

  TrainingLevel level(
      TrainingDefinition definition, int order, String title, String answerVariableName) {
    TrainingLevel level = new TrainingLevel();
    level.setTitle(title);
    level.setMaxScore(100);
    level.setOrder(order);
    level.setAnswer("secret");
    level.setAnswerVariableName(answerVariableName);
    level.setContent("content");
    level.setSolution("solution");
    level.setTrainingDefinition(definition);
    return trainingLevelRepository.save(level);
  }

  TrainingInstance instance(
      TrainingDefinition definition,
      String title,
      LocalDateTime startTime,
      LocalDateTime endTime,
      String accessToken,
      Long poolId,
      UserRef... organizers) {
    TrainingInstance instance = new TrainingInstance();
    instance.setTitle(title);
    instance.setStartTime(startTime);
    instance.setEndTime(endTime);
    instance.setAccessToken(accessToken);
    instance.setPoolId(poolId);
    instance.setLocalEnvironment(poolId == null);
    instance.setLastEdited(now());
    instance.setLastEditedBy("Seed Author");
    instance.setTrainingDefinition(definition);
    for (UserRef organizer : organizers) {
      instance.addOrganizer(organizer);
    }
    return trainingInstanceRepository.save(instance);
  }

  TrainingRun run(
      TrainingInstance instance,
      TrainingLevel currentLevel,
      UserRef participant,
      String sandboxId,
      LocalDateTime startTime,
      TRState state) {
    TrainingRun run = new TrainingRun();
    run.setStartTime(startTime);
    run.setEndTime(startTime.plusHours(1));
    run.setState(state);
    run.setCurrentLevel(currentLevel);
    run.setTrainingInstance(instance);
    run.setSandboxInstanceRefId(sandboxId);
    run.setParticipantRef(participant);
    return trainingRunRepository.save(run);
  }

  Submission submission(
      TrainingRun run,
      TrainingLevel level,
      SubmissionType type,
      String provided,
      LocalDateTime date,
      String ipAddress) {
    Submission submission = new Submission();
    submission.setTrainingRun(run);
    submission.setLevel(level);
    submission.setType(type);
    submission.setProvided(provided);
    submission.setDate(date);
    submission.setIpAddress(ipAddress);
    return submissionRepository.save(submission);
  }

  CheatingDetection detection(Long trainingInstanceId, LocalDateTime executeTime) {
    CheatingDetection detection = new CheatingDetection();
    detection.setTrainingInstanceId(trainingInstanceId);
    detection.setExecutedBy("Seed Organizer");
    detection.setExecuteTime(executeTime);
    detection.setResults(0L);
    detection.setProximityThreshold(120L);
    detection.setCurrentState(CheatingDetectionState.FINISHED);
    detection.setAnswerSimilarityState(CheatingDetectionState.FINISHED);
    detection.setLocationSimilarityState(CheatingDetectionState.FINISHED);
    detection.setTimeProximityState(CheatingDetectionState.FINISHED);
    detection.setMinimalSolveTimeState(CheatingDetectionState.FINISHED);
    detection.setForbiddenCommandsState(CheatingDetectionState.FINISHED);
    detection.setNoCommandsState(CheatingDetectionState.FINISHED);
    return cheatingDetectionRepository.save(detection);
  }

  private <E extends AbstractDetectionEvent> E fillEvent(
      E event,
      CheatingDetection detection,
      TrainingRun run,
      TrainingLevel level,
      DetectionEventType type,
      String participantNames,
      int participantCount) {
    event.setTrainingInstanceId(detection.getTrainingInstanceId());
    event.setCheatingDetectionId(detection.getId());
    event.setTrainingRunId(run.getId());
    event.setLevelId(level.getId());
    event.setLevelOrder(level.getOrder());
    event.setLevelTitle(level.getTitle());
    event.setDetectedAt(detection.getExecuteTime());
    event.setParticipantCount(participantCount);
    event.setDetectionEventType(type);
    event.setParticipants(participantNames);
    return event;
  }

  AnswerSimilarityDetectionEvent answerSimilarityEvent(
      CheatingDetection detection, TrainingRun run, TrainingLevel level) {
    AnswerSimilarityDetectionEvent event =
        fillEvent(
            new AnswerSimilarityDetectionEvent(),
            detection,
            run,
            level,
            DetectionEventType.ANSWER_SIMILARITY,
            "Alice Seed",
            1);
    event.setAnswer("copied-answer");
    event.setAnswerOwner("Bob Seed");
    return abstractDetectionEventRepository.save(event);
  }

  LocationSimilarityDetectionEvent locationSimilarityEvent(
      CheatingDetection detection, TrainingRun run, TrainingLevel level) {
    LocationSimilarityDetectionEvent event =
        fillEvent(
            new LocationSimilarityDetectionEvent(),
            detection,
            run,
            level,
            DetectionEventType.LOCATION_SIMILARITY,
            "Alice Seed",
            1);
    event.setIpAddress("10.0.0.7");
    event.setDns("host.example");
    return abstractDetectionEventRepository.save(event);
  }

  TimeProximityDetectionEvent timeProximityEvent(
      CheatingDetection detection, TrainingRun run, TrainingLevel level) {
    TimeProximityDetectionEvent event =
        fillEvent(
            new TimeProximityDetectionEvent(),
            detection,
            run,
            level,
            DetectionEventType.TIME_PROXIMITY,
            "Alice Seed",
            1);
    event.setThreshold(120L);
    return abstractDetectionEventRepository.save(event);
  }

  MinimalSolveTimeDetectionEvent minimalSolveTimeEvent(
      CheatingDetection detection, TrainingRun run, TrainingLevel level) {
    MinimalSolveTimeDetectionEvent event =
        fillEvent(
            new MinimalSolveTimeDetectionEvent(),
            detection,
            run,
            level,
            DetectionEventType.MINIMAL_SOLVE_TIME,
            "Alice Seed",
            1);
    event.setMinimalSolveTime(300L);
    return abstractDetectionEventRepository.save(event);
  }

  NoCommandsDetectionEvent noCommandsEvent(
      CheatingDetection detection, TrainingRun run, TrainingLevel level) {
    return abstractDetectionEventRepository.save(
        fillEvent(
            new NoCommandsDetectionEvent(),
            detection,
            run,
            level,
            DetectionEventType.NO_COMMANDS,
            "Alice Seed",
            1));
  }

  ForbiddenCommandsDetectionEvent forbiddenCommandsEvent(
      CheatingDetection detection, TrainingRun run, TrainingLevel level) {
    ForbiddenCommandsDetectionEvent event =
        fillEvent(
            new ForbiddenCommandsDetectionEvent(),
            detection,
            run,
            level,
            DetectionEventType.FORBIDDEN_COMMANDS,
            "Alice Seed",
            1);
    event.setCommandCount(1);
    return abstractDetectionEventRepository.save(event);
  }

  DetectionEventParticipant participant(
      AbstractDetectionEvent event, long userRefId, String participantName) {
    DetectionEventParticipant participant = new DetectionEventParticipant();
    participant.setDetectionEventId(event.getId());
    participant.setCheatingDetectionId(event.getCheatingDetectionId());
    participant.setUserId(userRefId);
    participant.setParticipantName(participantName);
    participant.setOccurredAt(now());
    participant.setIpAddress("10.0.0.7");
    return detectionEventParticipantRepository.save(participant);
  }

  DetectedForbiddenCommand detectedForbiddenCommand(
      AbstractDetectionEvent event, String command, String hostname) {
    DetectedForbiddenCommand detected = new DetectedForbiddenCommand();
    detected.setDetectionEventId(event.getId());
    detected.setCommand(command);
    detected.setType(CommandType.BASH);
    detected.setHostname(hostname);
    detected.setOccurredAt(now());
    return detectedForbiddenCommandRepository.save(detected);
  }
}
