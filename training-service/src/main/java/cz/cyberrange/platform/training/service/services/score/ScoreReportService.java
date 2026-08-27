package cz.cyberrange.platform.training.service.services.score;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.ParticipantScoreRowDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.TrainingInstanceScoreReportDTO;
import cz.cyberrange.platform.training.opensearch.events.training.model.HintTaken;
import cz.cyberrange.platform.training.opensearch.events.training.model.LevelCompleted;
import cz.cyberrange.platform.training.opensearch.events.training.model.SolutionDisplayed;
import cz.cyberrange.platform.training.opensearch.events.training.model.WrongAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingScoreAggregationService;
import cz.cyberrange.platform.training.opensearch.events.training.query.projection.RunEventAggregate;
import cz.cyberrange.platform.training.opensearch.events.training.query.projection.RunScoreSnapshot;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ScoreReportMapper;
import cz.cyberrange.platform.training.service.services.ExportImportService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Builds the score report of a training instance by enriching its runs with the score snapshots and
 * activity counts held in the audit index.
 *
 * <p>Every run of the instance yields a row, so a trainee whose events never reached the index is
 * still reported in full, with nothing scored. Trainee identities are resolved by the caller and
 * passed in, keeping the external user lookup outside this service's read-only transaction, and the
 * clock is likewise read by the caller so that every run of one report is measured against a single
 * reference.
 */
@Service
public class ScoreReportService {

  /** Event whose most recent occurrence on a level states the score reached on that level. */
  private static final String COMPLETION_EVENT_TYPE = LevelCompleted.TYPE;

  /** Events counted per run and reported as activity columns. */
  private static final List<String> COUNTED_EVENT_TYPES =
      List.of(HintTaken.TYPE, SolutionDisplayed.TYPE, WrongAnswerSubmitted.TYPE);

  private final ExportImportService exportImportService;
  private final TrainingDefinitionService trainingDefinitionService;
  private final UserRefRepository userRefRepository;
  private final TrainingScoreAggregationService trainingScoreAggregationService;
  private final LevelMapper levelMapper;
  private final ScoreReportMapper scoreReportMapper;

  /**
   * Creates the service with the repositories, mappers and collaborators it uses to build a
   * training instance's score report
   */
  @Autowired
  public ScoreReportService(
      ExportImportService exportImportService,
      TrainingDefinitionService trainingDefinitionService,
      UserRefRepository userRefRepository,
      TrainingScoreAggregationService trainingScoreAggregationService,
      LevelMapper levelMapper,
      ScoreReportMapper scoreReportMapper) {
    this.exportImportService = exportImportService;
    this.trainingDefinitionService = trainingDefinitionService;
    this.userRefRepository = userRefRepository;
    this.trainingScoreAggregationService = trainingScoreAggregationService;
    this.levelMapper = levelMapper;
    this.scoreReportMapper = scoreReportMapper;
  }

  /**
   * Finds the trainees taking part in an instance, so that they can be resolved before the report
   * is built.
   *
   * @param trainingInstanceId the instance whose participants are wanted.
   * @return the participants' user reference ids.
   */
  @TransactionalRO
  public Set<Long> findParticipantRefIds(Long trainingInstanceId) {
    return userRefRepository.findParticipantsRefIdsByTrainingInstanceId(trainingInstanceId);
  }

  /**
   * Builds the report of a training instance.
   *
   * @param trainingInstanceId the instance to report on.
   * @param participants resolved trainees, keyed by their user reference id; a run whose trainee is
   *     absent still yields a row.
   * @param nowMillis the current instant in epoch milliseconds.
   * @return the report, its rows ordered by rank.
   */
  @TransactionalRO
  public TrainingInstanceScoreReportDTO createReport(
      Long trainingInstanceId, Map<Long, UserRefDTO> participants, long nowMillis) {
    TrainingInstance instance = exportImportService.findInstanceById(trainingInstanceId);
    Set<TrainingRun> runs = exportImportService.findRunsByInstanceId(trainingInstanceId);
    List<AbstractLevel> definitionLevels =
        trainingDefinitionService.findAllLevelsFromDefinition(
            instance.getTrainingDefinition().getId());

    List<AbstractLevel> scoredLevels = selectScoredLevels(definitionLevels);
    Set<Long> scoredLevelIds = idsOf(scoredLevels);
    Set<Long> accessLevelIds = idsOf(selectAccessLevels(definitionLevels));

    Map<Long, RunEventAggregate> aggregates =
        trainingScoreAggregationService.aggregateRuns(
            trainingInstanceId,
            runs.stream().map(TrainingRun::getId).collect(Collectors.toSet()),
            idsOf(definitionLevels),
            COMPLETION_EVENT_TYPE,
            COUNTED_EVENT_TYPES);

    List<UnrankedRow> unranked =
        runs.stream()
            .map(
                run ->
                    toUnrankedRow(
                        run,
                        instance,
                        participants,
                        aggregates.get(run.getId()),
                        scoredLevelIds,
                        accessLevelIds,
                        nowMillis))
            .toList();

    TrainingInstanceScoreReportDTO report = new TrainingInstanceScoreReportDTO();
    report.setTrainingInstanceId(trainingInstanceId);
    report.setInstanceEndAt(instance.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli());
    report.setScoredLevels(levelMapper.mapToBasicDtoList(scoredLevels));
    report.setRows(rank(unranked));
    return report;
  }

  /**
   * Builds one run's row, resolved against the {@code userRefId} of its participant reference
   * rather than its local primary key, and left unscored when the run produced no audit events.
   */
  private UnrankedRow toUnrankedRow(
      TrainingRun run,
      TrainingInstance instance,
      Map<Long, UserRefDTO> participants,
      RunEventAggregate aggregate,
      Set<Long> scoredLevelIds,
      Set<Long> accessLevelIds,
      long nowMillis) {
    RunWindow window = RunWindow.resolve(run, instance, nowMillis);
    UserRefDTO participant = participants.get(run.getParticipantRef().getUserRefId());
    ParticipantScoreRowDTO row = scoreReportMapper.mapToScoreRow(run, window, participant);
    if (aggregate != null) {
      applyScores(row, aggregate, scoredLevelIds, accessLevelIds);
    }
    return new UnrankedRow(row, window.getElapsedMillis());
  }

  /**
   * Fills a row's scored part from the run's audit events. Only score-bearing levels contribute a
   * per-level entry, and wrong answers submitted on a passkey-gated level are left out, since such
   * a level records one for every passkey attempt, the successful one included.
   */
  private static void applyScores(
      ParticipantScoreRowDTO row,
      RunEventAggregate aggregate,
      Set<Long> scoredLevelIds,
      Set<Long> accessLevelIds) {
    RunScoreSnapshot snapshot = aggregate.latestScore();
    if (snapshot != null) {
      row.setTrainingScore(snapshot.totalTrainingScore());
      row.setAssessmentScore(snapshot.totalAssessmentScore());
      row.setTotalScore(snapshot.totalTrainingScore() + snapshot.totalAssessmentScore());
    }
    row.setScoreByLevelId(
        aggregate.scoreByLevelId().entrySet().stream()
            .filter(entry -> scoredLevelIds.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    row.setHintsTaken(Math.toIntExact(aggregate.countOf(HintTaken.TYPE)));
    row.setSolutionsDisplayed(Math.toIntExact(aggregate.countOf(SolutionDisplayed.TYPE)));
    row.setWrongAnswers(
        Math.toIntExact(
            aggregate.countOfExcludingLevels(WrongAnswerSubmitted.TYPE, accessLevelIds)));
  }

  /**
   * Selects the levels able to award score, in definition order: every training level, plus
   * assessment levels graded as a test. Info and access levels award nothing, and a questionnaire
   * assessment reports its full maximum whatever the responses given, so it is left out rather than
   * shown as a perfect result.
   */
  private static List<AbstractLevel> selectScoredLevels(List<AbstractLevel> definitionLevels) {
    return definitionLevels.stream()
        .filter(
            level ->
                level instanceof TrainingLevel
                    || (level instanceof AssessmentLevel assessment
                        && assessment.getAssessmentType() == AssessmentType.TEST))
        .sorted(Comparator.comparingInt(AbstractLevel::getOrder))
        .toList();
  }

  private static List<AbstractLevel> selectAccessLevels(List<AbstractLevel> definitionLevels) {
    return definitionLevels.stream().filter(AccessLevel.class::isInstance).toList();
  }

  private static Set<Long> idsOf(List<AbstractLevel> levels) {
    return levels.stream().map(AbstractLevel::getId).collect(Collectors.toSet());
  }

  /**
   * Orders the rows by total score descending, resolving ties in favour of the shorter run and then
   * by run id so that a report of the same data always reads the same way, and numbers them from
   * one.
   */
  private static List<ParticipantScoreRowDTO> rank(List<UnrankedRow> unranked) {
    List<ParticipantScoreRowDTO> ordered =
        unranked.stream()
            .sorted(
                Comparator.comparingInt((UnrankedRow entry) -> entry.row().getTotalScore())
                    .reversed()
                    .thenComparingLong(UnrankedRow::elapsedMillis)
                    .thenComparing(entry -> entry.row().getTrainingRunId()))
            .map(UnrankedRow::row)
            .toList();
    int rank = 1;
    for (ParticipantScoreRowDTO row : ordered) {
      row.setRank(rank++);
    }
    return ordered;
  }

  /** A row awaiting its rank, holding the run length the ranking breaks ties on. */
  private record UnrankedRow(ParticipantScoreRowDTO row, long elapsedMillis) {}
}
