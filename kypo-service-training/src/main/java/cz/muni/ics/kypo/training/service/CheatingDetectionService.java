package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.api.responses.VariantAnswer;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.detection.*;
import cz.muni.ics.kypo.training.persistence.model.enums.CheatingDetectionState;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;
import cz.muni.ics.kypo.training.persistence.model.enums.DetectionEventType;
import cz.muni.ics.kypo.training.persistence.repository.SubmissionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingLevelRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.detection.*;
import cz.muni.ics.kypo.training.service.api.AnswersStorageApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import org.apache.tomcat.jni.Local;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


/**
 * The type Cheating detection service.
 */
@Service
public class CheatingDetectionService {

    private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);

    private final AbstractDetectionEventRepository detectionEventRepository;
    private final CheatingDetectionRepository cheatingDetectionRepository;
    private final TrainingLevelRepository trainingLevelRepository;
    private final SubmissionRepository submissionRepository;

    private final AnswerSimilarityDetectionEventRepository answerSimilarityDetectionEventRepository;
    private final LocationSimilarityDetectionEventRepository locationSimilarityDetectionEventRepository;
    private final TimeProximityDetectionEventRepository timeProximityDetectionEventRepository;
    private final MinimalSolveTimeDetectionEventRepository minimalSolveTimeDetectionEventRepository;
    private final NoCommandsDetectionEventRepository noCommandsDetectionEventRepository;
    private final ForbiddenCommandsDetectionEventRepository forbiddenCommandsDetectionEventRepository;
    private final TrainingRunRepository trainingRunRepository;
    private final AnswersStorageApiService answersStorageApiService;
    private final TrainingRunService trainingRunService;
    private final TrainingInstanceService trainingInstanceService;
    private final ElasticsearchApiService elasticsearchApiService;
    private final UserService userService;

    /**
     * Instantiates a new Cheating detection service.
     *
     * @param abstractDetectionEventRepository           the cheat repository
     * @param cheatingDetectionRepository                the cheating detection repository
     * @param trainingLevelRepository                    the training level repository
     * @param submissionRepository                       the submission repository
     * @param answerSimilarityDetectionEventRepository   the answer similarity detection event repository
     * @param locationSimilarityDetectionEventRepository the location similarity detection event repository
     * @param timeProximityDetectionEventRepository      the time proximity detection event repository
     * @param minimalSolveTimeDetectionEventRepository   the minimal solve time detection event repository
     * @param noCommandsDetectionEventRepository         the no commands detection event repository
     * @param forbiddenCommandsDetectionEventRepository  the forbidden commands detection event repository
     * @param trainingRunRepository                      the training run repository
     * @param answersStorageApiService                   the answers storage api service
     * @param trainingRunService                         the training run service
     * @param trainingInstanceService                    the training instance service
     * @param elasticsearchApiService                    the elastic search api service
     * @param userService                                the user service
     */
    @Autowired
    public CheatingDetectionService(AbstractDetectionEventRepository abstractDetectionEventRepository,
                                    CheatingDetectionRepository cheatingDetectionRepository,
                                    TrainingLevelRepository trainingLevelRepository,
                                    SubmissionRepository submissionRepository,
                                    AnswerSimilarityDetectionEventRepository answerSimilarityDetectionEventRepository,
                                    LocationSimilarityDetectionEventRepository locationSimilarityDetectionEventRepository,
                                    TimeProximityDetectionEventRepository timeProximityDetectionEventRepository,
                                    MinimalSolveTimeDetectionEventRepository minimalSolveTimeDetectionEventRepository,
                                    NoCommandsDetectionEventRepository noCommandsDetectionEventRepository,
                                    ForbiddenCommandsDetectionEventRepository forbiddenCommandsDetectionEventRepository,
                                    TrainingRunRepository trainingRunRepository,
                                    AnswersStorageApiService answersStorageApiService,
                                    TrainingRunService trainingRunService,
                                    ElasticsearchApiService elasticsearchApiService,
                                    TrainingInstanceService trainingInstanceService,
                                    UserService userService) {
        this.detectionEventRepository = abstractDetectionEventRepository;
        this.cheatingDetectionRepository = cheatingDetectionRepository;
        this.trainingLevelRepository = trainingLevelRepository;
        this.submissionRepository = submissionRepository;
        this.answerSimilarityDetectionEventRepository = answerSimilarityDetectionEventRepository;
        this.locationSimilarityDetectionEventRepository = locationSimilarityDetectionEventRepository;
        this.timeProximityDetectionEventRepository = timeProximityDetectionEventRepository;
        this.minimalSolveTimeDetectionEventRepository = minimalSolveTimeDetectionEventRepository;
        this.noCommandsDetectionEventRepository = noCommandsDetectionEventRepository;
        this.forbiddenCommandsDetectionEventRepository = forbiddenCommandsDetectionEventRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.answersStorageApiService = answersStorageApiService;
        this.trainingRunService = trainingRunService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.trainingInstanceService = trainingInstanceService;
        this.userService = userService;
    }

    public void createCheatingDetection(CheatingDetection cheatingDetection) {
        cheatingDetection.setExecutedBy(userService.getUserRefFromUserAndGroup().getUserRefFullName());
        cheatingDetection.setResults(0L);
        cheatingDetection.setExecuteTime(LocalDateTime.now());
        cheatingDetectionRepository.save(cheatingDetection);
    }

    public void executeCheatingDetection(CheatingDetection cd) {
        List<TrainingRun> trainingRuns = new ArrayList<>(trainingRunService.findAllByTrainingInstanceId(cd.getTrainingInstanceId()));
        for (var run : trainingRuns) {
            run.setHasDetectionEvent(false);
            trainingRunRepository.save(run);
        }
        cd.setCurrentState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        if (cd.getAnswerSimilarityState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfAnswerSimilarity(cd);
        }
        if (cd.getLocationSimilarityState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfLocationSimilarity(cd);
        }
        if (cd.getTimeProximityState() == CheatingDetectionState.QUEUED) {
            if (cd.getProximityThreshold() == null) {
                cd.setProximityThreshold(120L);
            }
            executeCheatingDetectionOfTimeProximity(cd);
        }
        if (cd.getMinimalSolveTimeState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfMinimalSolveTime(cd);
        }
        if (cd.getNoCommandsState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfNoCommands(cd);
        }
        //if (cd.getForbiddenCommandsState() == CheatingDetectionState.QUEUED) {
        //    executeCheatingDetectionOfForbiddenCommands(cd);
        //}
        cd.setResults(detectionEventRepository.getNumberOfDetections(cd.getId()));
        cd.setCurrentState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }


    public void executeCheatingDetectionOfAnswerSimilarity(CheatingDetection cd) {
        cd.setAnswerSimilarityState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        Set<TrainingRun> runs = trainingRunService.findAllByTrainingInstanceId(trainingInstanceId);
        Map<String, List<VariantAnswer>> answerMap = new HashMap<>();
        List<TrainingLevel> trainingLevels = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService
                        .findById(trainingInstanceId).getTrainingDefinition().getId());
        Map<Long, TrainingLevel> trainingLevelsById = new HashMap<>();
        String sandboxId;
        for (var run : runs) {
            sandboxId = run.getSandboxInstanceRefId();
            answerMap.put(sandboxId, answersStorageApiService.getAnswersBySandboxId(sandboxId).getVariantAnswers());
        }
        for (var level : trainingLevels) {
            trainingLevelsById.put(level.getId(), level);
        }
        for (Submission submission : submissionRepository.getIncorrectSubmissionsOfTrainingInstance(trainingInstanceId)) {
            Long currentId = submission.getLevel().getId();
            if (!trainingLevelsById.containsKey(currentId)) {
                continue;
            }
            for (TrainingRun run : runs) {
                evalCheatOfAnswerSimilarity(run, submission, answerMap.get(run.getSandboxInstanceRefId()),
                        trainingLevelsById.get(currentId).getAnswerVariableName(), cd);
            }
        }
        cd.setAnswerSimilarityState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private void evalCheatOfAnswerSimilarity(TrainingRun run, Submission submission, List<VariantAnswer> answers,
                                             String answerVariable, CheatingDetection cd) {
        String submissionSandboxId = submission.getTrainingRun().getSandboxInstanceRefId();
        String sandboxId = run.getSandboxInstanceRefId();
        Set<DetectionEventParticipant> participants;
        if (sandboxId.equals(submissionSandboxId)) {
            return;
        }
        for (var answer : answers) {
            if (answer.getAnswerContent().equals(submission.getProvided()) && answerVariable.equals(answer.getAnswerVariableName())) {
                //TODO will change with next batch
                participants = new HashSet<>();
                participants.add(extractParticipant(submission));
                auditAnswerSimilarityEvent(submission, cd, participants, submissionSandboxId);
            }
        }
    }

    /**
     * Executes a cheating detection of type LOCATION_SIMILARITY
     *
     * @param cd the training instance id
     */
    public void executeCheatingDetectionOfLocationSimilarity(CheatingDetection cd) {
        cd.setLocationSimilarityState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<TrainingLevel> trainingLevels = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId());
        for (var level : trainingLevels) {
            evaluateLocationSimilarityByLevels(submissionRepository.getSubmissionsByLevelAndInstance(trainingInstanceId, level.getId()),
                    level, trainingInstanceId, cd);
        }
        cd.setLocationSimilarityState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private void evaluateLocationSimilarityByLevels(List<Submission> submissions, TrainingLevel level, Long trainingInstanceId, CheatingDetection cd) {
        boolean wasPut;
        List<List<Submission>> groups = new ArrayList<>();
        Set<DetectionEventParticipant> participants;
        for (var submission : submissions) {
            wasPut = false;
            for (var group : groups) {
                if (checkLocationSimilarity(group.get(0).getIpAddress(), submission.getIpAddress())) {
                    group.add(submission);
                    wasPut = true;
                }
            }
            if (!wasPut) {
                groups.add(new ArrayList<Submission>() {{
                    add(submission);
                }});
            }
        }
        for (var group : groups) {
            participants = new HashSet<>();
            for (var value : group) {

                participants.add(extractParticipant(value));
            }
            auditLocationSimilarityEvent(group.get(0), cd, participants);
        }
    }

    public void executeCheatingDetectionOfTimeProximity(CheatingDetection cd) {
        cd.setTimeProximityState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<TrainingLevel> trainingLevels = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId());
        List<Submission> submissions = new ArrayList<>();
        List<Submission> detectedGroup = new ArrayList<>();
        Set<DetectionEventParticipant> participants = new HashSet<>();

        for (var level : trainingLevels) {
            submissions = submissionRepository.getAllTimeProximitySubmissionsOfLevel(trainingInstanceId, level.getId());
            for (int i = 0; i < submissions.size() - 1; i++) {
                int j = i + 1;
                var first = submissions.get(i);
                var second = submissions.get(j);
                Long timeProximity = Duration.between(first.getDate(), second.getDate()).toSeconds();
                if (timeProximity < 120) {
                    if (detectedGroup.isEmpty()) {
                        detectedGroup.add(first);
                    }
                    detectedGroup.add(second);
                } else {
                    for (var sub : detectedGroup) {
                        participants.add(extractParticipant(sub));
                    }
                    auditTimeProximityEvent(detectedGroup.get(0), cd, participants);
                    detectedGroup.clear();
                    participants.clear();
                }
            }
        }
        cd.setTimeProximityState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    public void executeCheatingDetectionOfMinimalSolveTime(CheatingDetection cd) {
        cd.setMinimalSolveTimeState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<Submission> submissions = submissionRepository.getCorrectSubmissionsOfTrainingInstance(trainingInstanceId);
        Map<Long, List<Submission>> detectedByLevel = new HashMap<>();
        Map<Long, Long> submissionTimes = new HashMap<>();
        Set<DetectionEventParticipant> participants;
        for (int i = 1; i < submissions.size(); i++) {
            Submission first = submissions.get(i - 1);
            Submission second = submissions.get(i);
            if (first.getTrainingRun().equals(second.getTrainingRun())) {
                Long levelDuration = Duration.between(first.getDate(), second.getDate()).toSeconds();
                Long minimalSolveTime = first.getLevel().getMinimalPossibleSolveTime() * 60;
                if (levelDuration < minimalSolveTime) {
                    var arr = detectedByLevel.get(first.getLevel().getId());
                    if (arr == null) {
                        arr = new ArrayList<>();
                    }
                    arr.add(first);
                    detectedByLevel.put(first.getLevel().getId(), arr);
                    submissionTimes.put(first.getId(), levelDuration);
                }
            }
        }
        for (var set : detectedByLevel.entrySet()) {
            participants = new HashSet<>();
            for (var value : set.getValue()) {
                participants.add(extractParticipant(value, true, submissionTimes.get(value.getId())));
            }
            Submission s = set.getValue().get(0);
            auditMinimalSolveTimeEvent(s, cd, participants, s.getLevel().getMinimalPossibleSolveTime());
        }
        cd.setMinimalSolveTimeState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    /**
     * Executes a cheating detection of type NO_COMMANDS
     *
     * @param cd the training instance id
     */
    public void executeCheatingDetectionOfNoCommands(CheatingDetection cd) {
        cd.setNoCommandsState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<TrainingRun> trainingRuns = new ArrayList<>(trainingRunService.findAllByTrainingInstanceId(trainingInstanceId));
        List<TrainingLevel> trainingLevels = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId());
        Map<Long, TrainingLevel> trainingLevelsById = new HashMap<>();
        Map<Long, List<Submission>> emptySubmissionsByLevels = new HashMap<>();
        for (var level : trainingLevels) {
            trainingLevelsById.put(level.getId(), level);
        }
        List<Submission> submissions = new ArrayList<>();
        LocalDateTime from;
        Submission submission;
        for (var run : trainingRuns) {
            submissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
            for (int i = 0; i < submissions.size(); i++) {
                submission = submissions.get(i);
                from = (i == 0) ? run.getStartTime() : submissions.get(i - 1).getDate();
                Long currentId = submission.getLevel().getId();
                if (!trainingLevelsById.containsKey(currentId)) {
                    continue;
                }
                if (evalCheatOfNoCommands(run.getSandboxInstanceRefId(), from, submission,
                        trainingLevelsById.get(currentId), trainingInstanceId)) {
                    if (emptySubmissionsByLevels.containsKey(submission.getLevel().getId())) {
                        var tempSubmissions = emptySubmissionsByLevels.get(submission.getLevel().getId());
                        tempSubmissions.add(submission);
                        emptySubmissionsByLevels.put(submission.getLevel().getId(), tempSubmissions);
                    } else {
                        List<Submission> subs = new ArrayList<>();
                        subs.add(submission);
                        emptySubmissionsByLevels.put(submission.getLevel().getId(), subs);
                    }
                }
            }
        }
        Set<DetectionEventParticipant> runs;
        for (var set : emptySubmissionsByLevels.entrySet()) {
            runs = new HashSet<>();
            for (var value : set.getValue()) {
                runs.add(extractParticipant(value));
            }
            auditNoCommandsEvent(set.getValue().get(0), cd, runs);
        }
        cd.setNoCommandsState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private boolean evalCheatOfNoCommands(String sandboxId, LocalDateTime from, Submission submission, TrainingLevel level, Long instanceId) {
        var results = elasticsearchApiService.findAllConsoleCommandsBySandboxAndTimeRange(
                sandboxId, from.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
                submission.getDate().atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
        if ((level.isCommandsRequired()) && results.isEmpty()) {
            return true;
        }
        return false;
    }

    private DetectionEventParticipant extractParticipant(Submission s) {
        return extractParticipant(s, false, 0L);
    }

    private DetectionEventParticipant extractParticipant(Submission s, boolean isMinimal, Long solvedInTime) {
        DetectionEventParticipant participant = new DetectionEventParticipant();
        participant.setIpAddress(s.getIpAddress());
        participant.setUserId(s.getTrainingRun().getParticipantRef().getUserRefId());
        participant.setOccurredAt(s.getDate());
        participant.setParticipantName(userService.getUserRefDTOByUserRefId(s.getTrainingRun().getParticipantRef().getUserRefId()).getUserRefFullName());
        if (isMinimal) {
            participant.setSolvedInTime(solvedInTime);
        }
        return participant;
    }

    /**
     * Executes a cheating detection of type FORBIDDEN_COMMANDS
     *
     * @param cd the cheating detection
     */
    public void executeCheatingDetectionOfForbiddenCommands(CheatingDetection cd) {
        cd.setForbiddenCommandsState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<Submission> submissions;
        List<Map<String, Object>> submittedCommands;
        LocalDateTime from;
        Submission currentSubmission;
        String[] forbiddenCommands;

        for (var run : trainingRunService.findAllByTrainingInstanceId(trainingInstanceId)) {
            submissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
            for (int i = 0; i < submissions.size(); i++) {
                currentSubmission = submissions.get(i);
                from = (i == 0) ? run.getStartTime() : submissions.get(i - 1).getDate();
                submittedCommands = elasticsearchApiService.findAllConsoleCommandsBySandboxAndTimeRange(
                        run.getSandboxInstanceRefId(),
                        from.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
                        currentSubmission.getDate().atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
                for (var command : submittedCommands) {
                    forbiddenCommands = evaluateForbiddenCommand(cd.getForbiddenCommands(), command, currentSubmission, cd);
                    auditForbiddenCommandsEvent(currentSubmission, cd, extractParticipant(currentSubmission), forbiddenCommands);
                }
            }
        }
        cd.setForbiddenCommandsState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private String[] evaluateForbiddenCommand(Set<ForbiddenCommand> fc, Map<String, Object> commandMap, Submission s, CheatingDetection cd) {
        String command = commandMap.get("cmd").toString();
        String type = commandMap.get("cmd_type").toString();
        List<String> cmds = new ArrayList<>();
        for (var forbiddenCommand : fc) {
            if (!(type.equals(forbiddenCommand.getType().toString()))) {
                continue;
            }
            if (command != null) {
                if (command.contains(forbiddenCommand.getCommand())) {
                    cmds.add(command);
                }
            }
        }
        return cmds.toArray(new String[0]);
    }

    public void deleteDetectionEvents(Long cheatingDetectionId) {
        detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
    }

    public void deleteCheatingDetection(Long cheatingDetectionId) {
        detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
        cheatingDetectionRepository.deleteCheatingDetectionById(cheatingDetectionId);
    }

    public void deleteAllCheatingDetectionsOfTrainingInstance(Long trainingInstanceId) {
        detectionEventRepository.deleteDetectionEventsOfTrainingInstance(trainingInstanceId);
        cheatingDetectionRepository.deleteAllCheatingDetectionsOfTrainingInstance(trainingInstanceId);
    }

    public void rerunCheatingDetection(Long cheatingDetectionId) {
        CheatingDetection cd = cheatingDetectionRepository.findCheatingDetectionById(cheatingDetectionId);
        cd.setCurrentState(CheatingDetectionState.RUNNING);
        if (cd.getAnswerSimilarityState() != CheatingDetectionState.DISABLED) {
            cd.setAnswerSimilarityState(CheatingDetectionState.QUEUED);
        }
        if (cd.getLocationSimilarityState() != CheatingDetectionState.DISABLED) {
            cd.setLocationSimilarityState(CheatingDetectionState.QUEUED);
        }
        if (cd.getTimeProximityState() != CheatingDetectionState.DISABLED) {
            cd.setTimeProximityState(CheatingDetectionState.QUEUED);
        }
        if (cd.getMinimalSolveTimeState() != CheatingDetectionState.DISABLED) {
            cd.setMinimalSolveTimeState(CheatingDetectionState.QUEUED);
        }
        if (cd.getNoCommandsState() != CheatingDetectionState.DISABLED) {
            cd.setNoCommandsState(CheatingDetectionState.QUEUED);
        }
        if (cd.getForbiddenCommandsState() != CheatingDetectionState.DISABLED) {
            cd.setForbiddenCommandsState(CheatingDetectionState.QUEUED);
        }
        detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cd.getId());
        cd.setExecuteTime(LocalDateTime.now());
        cd.setResults(0L);
        cheatingDetectionRepository.save(cd);
        executeCheatingDetection(cd);
    }

    private CheatingDetectionState[] getStatesByFlags(boolean[] flags) {
        CheatingDetectionState[] states = new CheatingDetectionState[6];
        for (int i = 0; i < flags.length; i++) {
            states[i] = flags[i] ? CheatingDetectionState.QUEUED : CheatingDetectionState.DISABLED;
        }
        return states;
    }

    public Page<AbstractDetectionEvent> findAllDetectionEventsOfCheatingDetection(Long cheatingDetectionId,
                                                                                  Pageable pageable) {
        return detectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId, pageable);
    }

    public Page<CheatingDetection> findAllCheatingDetectionsOfTrainingInstance(Long trainingInstanceId, Pageable pageable) {
        return cheatingDetectionRepository.findAllByTrainingInstanceId(trainingInstanceId, pageable);
    }

    private boolean checkLocationSimilarity(String ip, String otherIp) {
        try {
            InetAddress firstIp = InetAddress.getByName(ip);
            InetAddress secondIp = InetAddress.getByName(otherIp);
            return firstIp.equals(secondIp);
        } catch (UnknownHostException e) {
            return ip.equals(otherIp);
        }
    }

    private void auditAnswerSimilarityEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants, String answerOwner) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        AnswerSimilarityDetectionEvent event = new AnswerSimilarityDetectionEvent();
        event.setAnswer(submission.getProvided());
        event.setParticipants(participants);
        event.setAnswerOwner(answerOwner);
        event.setCheatingDetectionId(cd.getId());
        event.setDetectedAt(cd.getExecuteTime());
        event.setLevelId(submission.getLevel().getId());
        event.setLevelTitle(submission.getLevel().getTitle());
        event.setParticipantCount(participants.size());
        event.setTrainingInstanceId(cd.getTrainingInstanceId());
        answerSimilarityDetectionEventRepository.save(event);
    }

    private void auditLocationSimilarityEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        LocationSimilarityDetectionEvent event = new LocationSimilarityDetectionEvent();
        event.setDns("TODO");
        event.setIpAddress(submission.getIpAddress());
        event.setParticipants(participants);
        event.setId(Long.MIN_VALUE);
        //TODO
        event.setIsAddressDeploy(true);
        event.setCheatingDetectionId(cd.getId());
        event.setDetectedAt(cd.getExecuteTime());
        event.setLevelId(submission.getLevel().getId());
        event.setLevelTitle(submission.getLevel().getTitle());
        event.setParticipantCount(participants.size());
        event.setTrainingInstanceId(cd.getTrainingInstanceId());
        locationSimilarityDetectionEventRepository.save(event);
    }


    private void auditMinimalSolveTimeEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants,
                                            Long minimalSolveTime) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        MinimalSolveTimeDetectionEvent event = new MinimalSolveTimeDetectionEvent();
        event.setMinimalSolveTime(minimalSolveTime);
        event.setParticipants(participants);
        event.setCheatingDetectionId(cd.getId());
        event.setDetectedAt(cd.getExecuteTime());
        event.setLevelId(submission.getLevel().getId());
        event.setLevelTitle(submission.getLevel().getTitle());
        event.setParticipantCount(1);
        event.setTrainingInstanceId(cd.getTrainingInstanceId());
        minimalSolveTimeDetectionEventRepository.save(event);
    }

    private void auditTimeProximityEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        TimeProximityDetectionEvent event = new TimeProximityDetectionEvent();
        event.setThreshold(cd.getProximityThreshold());
        event.setParticipants(participants);
        event.setCheatingDetectionId(cd.getId());
        event.setDetectedAt(cd.getExecuteTime());
        event.setLevelId(submission.getLevel().getId());
        event.setLevelTitle(submission.getLevel().getTitle());
        event.setParticipantCount(participants.size());
        event.setTrainingInstanceId(cd.getTrainingInstanceId());
        timeProximityDetectionEventRepository.save(event);
    }

    private void auditNoCommandsEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        NoCommandsDetectionEvent event = new NoCommandsDetectionEvent();
        event.setParticipants(participants);
        event.setCheatingDetectionId(cd.getId());
        event.setDetectedAt(cd.getExecuteTime());
        event.setLevelId(submission.getLevel().getId());
        event.setLevelTitle(submission.getLevel().getTitle());
        event.setParticipantCount(participants.size());
        event.setTrainingInstanceId(cd.getTrainingInstanceId());
        noCommandsDetectionEventRepository.save(event);
    }

    private void auditForbiddenCommandsEvent(Submission submission, CheatingDetection cd, DetectionEventParticipant participant,
                                             String[] forbiddenCommands) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        ForbiddenCommandsDetectionEvent event = new ForbiddenCommandsDetectionEvent();
        event.setForbiddenCommands(forbiddenCommands);
        event.setParticipant(participant);
        event.setCheatingDetectionId(cd.getId());
        event.setDetectedAt(cd.getExecuteTime());
        event.setLevelId(submission.getLevel().getId());
        event.setLevelTitle(submission.getLevel().getTitle());
        event.setParticipantCount(1);
        event.setTrainingInstanceId(cd.getTrainingInstanceId());
        forbiddenCommandsDetectionEventRepository.save(event);
    }

    private void updateCheatingDetection(CheatingDetection cd) {
        cheatingDetectionRepository.save(cd);
    }
}

