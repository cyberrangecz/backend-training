package cz.cyberrange.platform.training.service.services.detection;

import static cz.cyberrange.platform.training.service.utils.CheatingDetectionUtils.*;

import cz.cyberrange.platform.training.api.responses.VariantAnswer;
import cz.cyberrange.platform.training.persistence.model.Submission;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.persistence.model.enums.DetectionEventType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.AnswerSimilarityDetectionEventRepository;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Detects a trainee whose incorrect submission matches, verbatim, another trainee's stored variant
 * answer for the same training level and answer variable. Every incorrect submission recorded in a
 * training instance is compared against the per-sandbox variant answers of every other trainee's
 * run under that instance; a match is recorded as an {@link AnswerSimilarityDetectionEvent}. A
 * submission whose text matches one of the submitter's own sandbox answers for any level is treated
 * as a mistaken entry rather than a match. An event can implicate a single trainee: nothing here
 * requires more than one participant before an event is recorded.
 */
@Service
public class AnswerSimilarityService {
  private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
  private final TrainingLevelRepository trainingLevelRepository;
  private final SubmissionRepository submissionRepository;
  private final AnswerSimilarityDetectionEventRepository answerSimilarityDetectionEventRepository;
  private final TrainingRunRepository trainingRunRepository;
  private final AnswersStorageApiService answersStorageApiService;
  private final TrainingRunService trainingRunService;
  private final TrainingInstanceService trainingInstanceService;
  private final UserService userService;
  private final DetectionEventService detectionEventService;

  /**
   * Creates the service with the repositories and collaborators it uses to compare submissions
   * against stored variant answers across a training instance's runs
   */
  @Autowired
  public AnswerSimilarityService(
      TrainingLevelRepository trainingLevelRepository,
      SubmissionRepository submissionRepository,
      AnswerSimilarityDetectionEventRepository answerSimilarityDetectionEventRepository,
      TrainingRunRepository trainingRunRepository,
      AnswersStorageApiService answersStorageApiService,
      TrainingRunService trainingRunService,
      TrainingInstanceService trainingInstanceService,
      UserService userService,
      DetectionEventService detectionEventService) {
    this.trainingLevelRepository = trainingLevelRepository;
    this.submissionRepository = submissionRepository;
    this.answerSimilarityDetectionEventRepository = answerSimilarityDetectionEventRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.answersStorageApiService = answersStorageApiService;
    this.trainingRunService = trainingRunService;
    this.trainingInstanceService = trainingInstanceService;
    this.userService = userService;
    this.detectionEventService = detectionEventService;
  }

  /**
   * Finds every answer similarity event recorded under the given cheating detection.
   *
   * @param cheatingDetectionId the cheating detection id
   * @return the matching events
   */
  public List<AnswerSimilarityDetectionEvent> findAllAnswerSimilarityEventsOfDetection(
      Long cheatingDetectionId) {
    return answerSimilarityDetectionEventRepository.findAllByCheatingDetectionId(
        cheatingDetectionId);
  }

  /**
   * Finds an answer similarity event by its id.
   *
   * @param eventId the event id
   * @return the matching event
   */
  public AnswerSimilarityDetectionEvent findAnswerSimilarityEventById(Long eventId) {
    return answerSimilarityDetectionEventRepository.findAnswerSimilarityEventById(eventId);
  }

  /**
   * Compares every incorrect submission of the cheating detection's training instance against every
   * trainee run's stored variant answers, and persists an {@link AnswerSimilarityDetectionEvent}
   * for each answer-similarity match found.
   *
   * @param cd the cheating detection whose training instance is scanned
   */
  void executeCheatingDetectionOfAnswerSimilarity(CheatingDetection cd) {
    Long trainingInstanceId = cd.getTrainingInstanceId();
    Set<TrainingRun> runs = trainingRunService.findAllByTrainingInstanceId(trainingInstanceId);
    Map<String, List<VariantAnswer>> answers = new HashMap<>();
    Map<Long, TrainingLevel> trainingLevelsById =
        aggregateTrainingLevelsById(trainingInstanceId, runs, answers);
    for (Submission submission :
        submissionRepository.getIncorrectSubmissionsOfTrainingInstance(trainingInstanceId)) {
      evaluateAnswerSimilarityForSubmission(cd, runs, answers, trainingLevelsById, submission);
    }
  }

  /**
   * Fetches, for every run of the training instance, the run sandbox's full list of variant answers
   * into {@code answers} keyed by sandbox id, and returns every training level of the instance's
   * training definition keyed by level id
   */
  private Map<Long, TrainingLevel> aggregateTrainingLevelsById(
      Long trainingInstanceId, Set<TrainingRun> runs, Map<String, List<VariantAnswer>> answers) {
    runs.forEach(
        run -> {
          String sandboxId = run.getSandboxInstanceRefId();
          answers.put(
              sandboxId,
              answersStorageApiService.getAnswersBySandboxId(sandboxId).getVariantAnswers());
        });

    Long trainingDefinitionId =
        trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId();
    return trainingLevelRepository.findAllByTrainingDefinitionId(trainingDefinitionId).stream()
        .collect(Collectors.toMap(TrainingLevel::getId, level -> level));
  }

  /**
   * Skips the submission when its text matches one of the submitter's own sandbox variant answers
   * for any level, or when its level is absent from the instance's training levels; otherwise
   * compares the submission against every run of the instance
   */
  private void evaluateAnswerSimilarityForSubmission(
      CheatingDetection cd,
      Set<TrainingRun> runs,
      Map<String, List<VariantAnswer>> answers,
      Map<Long, TrainingLevel> trainingLevelsById,
      Submission submission) {
    Long currentId = submission.getLevel().getId();
    String sandboxId = submission.getTrainingRun().getSandboxInstanceRefId();

    if (checkIfAnswerBelongsToDifferentLevel(answers.get(sandboxId), submission.getProvided())
        || !trainingLevelsById.containsKey(currentId)) {
      return;
    }

    runs.stream()
        .map(run -> Map.entry(run, answers.get(run.getSandboxInstanceRefId())))
        .forEach(
            entry ->
                validateAndLogAnswerSimilarityEvent(
                    entry.getKey(),
                    submission,
                    entry.getValue(),
                    trainingLevelsById.get(currentId).getAnswerVariableName(),
                    cd));
  }

  /**
   * Reports whether the provided text equals the content of any variant answer in the list,
   * regardless of which level or answer variable that variant answer belongs to
   */
  private boolean checkIfAnswerBelongsToDifferentLevel(
      List<VariantAnswer> answers, String provided) {
    return answers.stream().map(VariantAnswer::getAnswerContent).anyMatch(provided::equals);
  }

  /**
   * Skips a run sharing the submitter's own sandbox, then records the submitter as a participant
   * when the submitted text matches the run's variant answer for the submitted level's answer
   * variable, and persists an event when it does
   */
  private void validateAndLogAnswerSimilarityEvent(
      TrainingRun run,
      Submission submission,
      List<VariantAnswer> answers,
      String answerVariable,
      CheatingDetection cd) {
    String submissionSandboxId = submission.getTrainingRun().getSandboxInstanceRefId();
    String sandboxId = run.getSandboxInstanceRefId();
    Set<DetectionEventParticipant> participants = new HashSet<>();
    if (sandboxId.equals(submissionSandboxId)) {
      return;
    }
    populateParticipants(submission, answers, answerVariable, participants);
    if (!participants.isEmpty()) {
      generateAnswerSimilarityEvent(run, submission, cd, participants);
    }
  }

  /**
   * Adds the run's owner as a further participant for every correct submission the owner made on
   * the same level as the flagged submission, then persists the event and marks the run as having a
   * detection event
   */
  private void generateAnswerSimilarityEvent(
      TrainingRun run,
      Submission submission,
      CheatingDetection cd,
      Set<DetectionEventParticipant> participants) {
    String answerOwner =
        userService
            .getUserRefDTOByUserRefId(run.getParticipantRef().getUserRefId())
            .getUserRefFullName();
    List<Submission> ownerSubmissions =
        submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
    for (var ownerSubmission : ownerSubmissions) {
      if (Objects.equals(ownerSubmission.getLevel().getId(), submission.getLevel().getId())) {
        participants.add(
            extractParticipant(
                ownerSubmission,
                userService
                    .getUserRefDTOByUserRefId(
                        ownerSubmission.getTrainingRun().getParticipantRef().getUserRefId())
                    .getUserRefFullName()));
      }
    }
    auditAnswerSimilarityEvent(submission, cd, participants, answerOwner);
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
  }

  /**
   * Adds the submitter as a participant when one of the given variant answers has both the
   * submitted text and the given answer variable name, and the submitter is not already present
   */
  private void populateParticipants(
      Submission submission,
      List<VariantAnswer> answers,
      String answerVariable,
      Set<DetectionEventParticipant> participants) {
    for (var answer : answers) {
      if (answer.getAnswerContent().equals(submission.getProvided())
          && answerVariable.equals(answer.getAnswerVariableName())) {
        DetectionEventParticipant participant =
            extractParticipant(
                submission,
                userService
                    .getUserRefDTOByUserRefId(
                        submission.getTrainingRun().getParticipantRef().getUserRefId())
                    .getUserRefFullName());
        if (!checkIfContainsParticipant(participants, participant)) {
          participants.add(participant);
        }
      }
    }
  }

  /**
   * Marks the submitting run as having a detection event, persists an {@link
   * AnswerSimilarityDetectionEvent} for the submission carrying the given participants and answer
   * owner, and saves each participant against the new event
   */
  private void auditAnswerSimilarityEvent(
      Submission submission,
      CheatingDetection cd,
      Set<DetectionEventParticipant> participants,
      String answerOwner) {
    TrainingRun run = submission.getTrainingRun();
    run.setHasDetectionEvent(true);
    trainingRunRepository.save(run);
    AnswerSimilarityDetectionEvent event = new AnswerSimilarityDetectionEvent();
    event.setCommonDetectionEventParameters(
        submission, cd, DetectionEventType.ANSWER_SIMILARITY, participants.size());
    event.setAnswer(submission.getProvided());
    event.setAnswerOwner(answerOwner);
    event.setParticipants(generateParticipantString(participants));
    answerSimilarityDetectionEventRepository.save(event);
    detectionEventService.saveParticipants(participants, event.getId(), cd.getId());
  }
}
