package cz.cyberrange.platform.training.service.unit.services.score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.ParticipantScoreRowDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.TrainingInstanceScoreReportDTO;
import cz.cyberrange.platform.training.opensearch.events.training.model.HintTaken;
import cz.cyberrange.platform.training.opensearch.events.training.model.SolutionDisplayed;
import cz.cyberrange.platform.training.opensearch.events.training.model.WrongAnswerSubmitted;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingScoreAggregationService;
import cz.cyberrange.platform.training.opensearch.events.training.query.projection.RunEventAggregate;
import cz.cyberrange.platform.training.opensearch.events.training.query.projection.RunScoreSnapshot;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.mapping.mapstruct.*;
import cz.cyberrange.platform.training.service.services.ExportImportService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.score.ScoreReportService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Unit tests for {@link ScoreReportService}.
 *
 * <p>External collaborators (repository-backed lookups and the OpenSearch aggregation) are mocked;
 * the MapStruct mappers are the real generated implementations, since they carry no I/O of their
 * own and mocking them would hide the row-assembly logic under test.
 */
@DisplayName("ScoreReportService")
@SpringBootTest(
    classes = {
      EnumMapperImpl.class,
      TrainingDefinitionMapperImpl.class,
      UserRefMapperImpl.class,
      LevelMapperImpl.class,
      HintMapperImpl.class,
      BetaTestingGroupMapperImpl.class,
      QuestionMapperImpl.class,
      AttachmentMapperImpl.class,
      MitreTechniqueMapperImpl.class,
      ScoreReportMapperImpl.class
    })
class ScoreReportServiceTest {

  private static final LocalDateTime BASE = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
  private static final Long INSTANCE_ID = 1L;
  private static final Long DEFINITION_ID = 2L;

  @Autowired private LevelMapperImpl levelMapper;
  @Autowired private ScoreReportMapperImpl scoreReportMapper;

  @MockBean private ExportImportService exportImportService;
  @MockBean private TrainingDefinitionService trainingDefinitionService;
  @MockBean private UserRefRepository userRefRepository;
  @MockBean private TrainingScoreAggregationService trainingScoreAggregationService;

  private ScoreReportService sut;
  private TrainingInstance instance;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    sut =
        new ScoreReportService(
            exportImportService,
            trainingDefinitionService,
            userRefRepository,
            trainingScoreAggregationService,
            levelMapper,
            scoreReportMapper);

    TrainingDefinition definition = new TrainingDefinition();
    definition.setId(DEFINITION_ID);

    instance = new TrainingInstance();
    instance.setId(INSTANCE_ID);
    instance.setEndTime(BASE.plusHours(1));
    instance.setTrainingDefinition(definition);

    given(exportImportService.findInstanceById(INSTANCE_ID)).willReturn(instance);
  }

  private long toEpochMillis(LocalDateTime dateTime) {
    return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
  }

  private TrainingRun run(long id, LocalDateTime start, LocalDateTime end, TRState state) {
    UserRef participantRef = new UserRef();
    participantRef.setUserRefId(id * 100);

    TrainingRun run = new TrainingRun();
    run.setId(id);
    run.setStartTime(start);
    run.setEndTime(end);
    run.setState(state);
    run.setParticipantRef(participantRef);
    return run;
  }

  private TrainingLevel trainingLevel(long id, int order) {
    TrainingLevel level = new TrainingLevel();
    level.setId(id);
    level.setOrder(order);
    return level;
  }

  private AssessmentLevel assessmentLevel(long id, int order, AssessmentType type) {
    AssessmentLevel level = new AssessmentLevel();
    level.setId(id);
    level.setOrder(order);
    level.setAssessmentType(type);
    return level;
  }

  private InfoLevel infoLevel(long id, int order) {
    InfoLevel level = new InfoLevel();
    level.setId(id);
    level.setOrder(order);
    return level;
  }

  private AccessLevel accessLevel(long id, int order) {
    AccessLevel level = new AccessLevel();
    level.setId(id);
    level.setOrder(order);
    return level;
  }

  private void stubLevels(AbstractLevel... levels) {
    given(trainingDefinitionService.findAllLevelsFromDefinition(DEFINITION_ID))
        .willReturn(List.of(levels));
  }

  private void stubRuns(TrainingRun... runs) {
    given(exportImportService.findRunsByInstanceId(INSTANCE_ID))
        .willReturn(new HashSet<>(Set.of(runs)));
  }

  private void stubAggregate(Map<Long, RunEventAggregate> aggregateByRunId) {
    given(
            trainingScoreAggregationService.aggregateRuns(
                eq(INSTANCE_ID), anyCollection(), anyCollection(), any(), any()))
        .willReturn(aggregateByRunId);
  }

  private ParticipantScoreRowDTO rowFor(TrainingInstanceScoreReportDTO report, long trainingRunId) {
    return report.getRows().stream()
        .filter(row -> trainingRunId == row.getTrainingRunId())
        .findFirst()
        .orElseThrow();
  }

  @Test
  @DisplayName("yields exactly one row per training run of the instance")
  void yieldsOneRowPerTrainingRun() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(
        run(1, BASE, BASE.plusSeconds(5), TRState.FINISHED),
        run(2, BASE, BASE.plusSeconds(5), TRState.FINISHED),
        run(3, BASE, BASE.plusSeconds(5), TRState.FINISHED));
    stubAggregate(Map.of());

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    assertEquals(3, report.getRows().size());
  }

  @Test
  @DisplayName("an instance with no runs still yields a well-formed report")
  void noRunsStillYieldsWellFormedReport() {
    stubLevels(trainingLevel(10, 0), assessmentLevel(11, 1, AssessmentType.TEST));
    stubRuns();
    stubAggregate(Map.of());

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    assertTrue(report.getRows().isEmpty());
    assertEquals(2, report.getScoredLevels().size());
    assertEquals(toEpochMillis(instance.getEndTime()), report.getInstanceEndAt());
  }

  @Test
  @DisplayName("a trainee whose events never indexed still appears with everything at zero")
  void unindexedTraineeStillAppearsWithZeros() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(run(5, BASE, BASE.plusSeconds(5), TRState.FINISHED));
    stubAggregate(Map.of());

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    ParticipantScoreRowDTO row = rowFor(report, 5L);
    assertEquals(0, row.getTrainingScore());
    assertEquals(0, row.getAssessmentScore());
    assertEquals(0, row.getTotalScore());
    assertEquals(0, row.getHintsTaken());
    assertEquals(0, row.getWrongAnswers());
    assertEquals(0, row.getSolutionsDisplayed());
    assertTrue(row.getScoreByLevelId().isEmpty());
    assertTrue(row.isFinished());
    assertEquals(toEpochMillis(BASE), row.getStartedAt());
  }

  @Test
  @DisplayName("run totals come from the latest event snapshot")
  void runTotalsComeFromLatestSnapshot() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(run(6, BASE, BASE.plusSeconds(5), TRState.FINISHED));
    stubAggregate(
        Map.of(6L, new RunEventAggregate(new RunScoreSnapshot(40, 10), Map.of(), Map.of())));

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    ParticipantScoreRowDTO row = rowFor(report, 6L);
    assertEquals(40, row.getTrainingScore());
    assertEquals(10, row.getAssessmentScore());
    assertEquals(50, row.getTotalScore());
  }

  @Test
  @DisplayName("per-level score reflects the most recent completion of that level")
  void perLevelScoreReflectsMostRecentCompletion() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(run(7, BASE, BASE.plusSeconds(5), TRState.FINISHED));
    stubAggregate(Map.of(7L, new RunEventAggregate(null, Map.of(10L, 30), Map.of())));

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    ParticipantScoreRowDTO row = rowFor(report, 7L);
    assertEquals(30, row.getScoreByLevelId().get(10L));
  }

  @Test
  @DisplayName("only score-bearing levels get a scored-level column and a per-run score entry")
  void onlyScoreBearingLevelsGetColumnAndScoreEntry() {
    InfoLevel info = infoLevel(1, 0);
    AccessLevel access = accessLevel(2, 1);
    TrainingLevel training = trainingLevel(3, 2);
    AssessmentLevel test = assessmentLevel(4, 3, AssessmentType.TEST);
    AssessmentLevel questionnaire = assessmentLevel(5, 4, AssessmentType.QUESTIONNAIRE);
    stubLevels(info, access, training, test, questionnaire);
    stubRuns(run(8, BASE, BASE.plusSeconds(5), TRState.FINISHED));
    stubAggregate(
        Map.of(
            8L, new RunEventAggregate(null, Map.of(1L, 5, 2L, 5, 3L, 5, 4L, 5, 5L, 5), Map.of())));

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    List<Long> scoredLevelIds =
        report.getScoredLevels().stream().map(AbstractLevelBasicDTO::getId).toList();
    assertThat(scoredLevelIds).containsExactly(3L, 4L);

    ParticipantScoreRowDTO row = rowFor(report, 8L);
    assertThat(row.getScoreByLevelId()).containsOnlyKeys(3L, 4L);
  }

  @Test
  @DisplayName(
      "scored levels are listed in definition order regardless of the order they are supplied in")
  void scoredLevelsAreListedInDefinitionOrder() {
    stubLevels(assessmentLevel(20, 5, AssessmentType.TEST), trainingLevel(21, 1));
    stubRuns();
    stubAggregate(Map.of());

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    List<Long> orderedIds =
        report.getScoredLevels().stream().map(AbstractLevelBasicDTO::getId).toList();
    assertThat(orderedIds).containsExactly(21L, 20L);
  }

  @Test
  @DisplayName("an unattempted level is distinguishable from a level scored zero")
  void unattemptedLevelDistinguishableFromZeroScore() {
    stubLevels(trainingLevel(30, 0));
    stubRuns(
        run(40, BASE, BASE.plusSeconds(5), TRState.FINISHED),
        run(41, BASE, BASE.plusSeconds(5), TRState.FINISHED));
    stubAggregate(
        Map.of(
            40L, new RunEventAggregate(null, Map.of(), Map.of()),
            41L, new RunEventAggregate(null, Map.of(30L, 0), Map.of())));

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    assertFalse(rowFor(report, 40L).getScoreByLevelId().containsKey(30L));
    assertTrue(rowFor(report, 41L).getScoreByLevelId().containsKey(30L));
    assertEquals(0, rowFor(report, 41L).getScoreByLevelId().get(30L));
  }

  @Test
  @DisplayName("an archived run counts as finished under an instance that has not ended")
  void archivedRunCountsAsFinished() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(run(9, BASE, BASE.plusSeconds(5), TRState.ARCHIVED));
    stubAggregate(Map.of());

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    assertTrue(rowFor(report, 9L).isFinished());
  }

  @Test
  @DisplayName("passkey retries on an access level do not inflate the wrong-answer tally")
  void passkeyRetriesDoNotInflateWrongAnswerTally() {
    AccessLevel access = accessLevel(60, 0);
    TrainingLevel training = trainingLevel(61, 1);
    stubLevels(access, training);
    stubRuns(run(50, BASE, BASE.plusSeconds(5), TRState.FINISHED));
    stubAggregate(
        Map.of(
            50L,
            new RunEventAggregate(
                null, Map.of(), Map.of(WrongAnswerSubmitted.TYPE, Map.of(61L, 2L, 60L, 3L)))));

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    assertEquals(2, rowFor(report, 50L).getWrongAnswers());
  }

  @Test
  @DisplayName("hints and solutions are counted without altering the total score")
  void hintsAndSolutionsAreCountedWithoutAlteringScore() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(run(51, BASE, BASE.plusSeconds(5), TRState.FINISHED));
    stubAggregate(
        Map.of(
            51L,
            new RunEventAggregate(
                new RunScoreSnapshot(20, 5),
                Map.of(),
                Map.of(
                    HintTaken.TYPE, Map.of(10L, 4L),
                    SolutionDisplayed.TYPE, Map.of(10L, 1L)))));

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    ParticipantScoreRowDTO row = rowFor(report, 51L);
    assertEquals(4, row.getHintsTaken());
    assertEquals(1, row.getSolutionsDisplayed());
    assertEquals(20, row.getTrainingScore());
    assertEquals(5, row.getAssessmentScore());
    assertEquals(25, row.getTotalScore());
  }

  @Test
  @DisplayName("ranking favours score, then speed, with consecutive ranks from one in rank order")
  void rankingFavoursScoreThenSpeed() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(
        run(70, BASE, BASE.plusSeconds(100), TRState.FINISHED),
        run(71, BASE, BASE.plusSeconds(50), TRState.FINISHED));
    stubAggregate(
        Map.of(
            70L, new RunEventAggregate(new RunScoreSnapshot(30, 0), Map.of(), Map.of()),
            71L, new RunEventAggregate(new RunScoreSnapshot(30, 0), Map.of(), Map.of())));

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE.plusHours(1)));

    List<ParticipantScoreRowDTO> rows = report.getRows();
    assertEquals(2, rows.size());
    assertEquals(71L, rows.get(0).getTrainingRunId());
    assertEquals(1, rows.get(0).getRank());
    assertEquals(70L, rows.get(1).getTrainingRunId());
    assertEquals(2, rows.get(1).getRank());
  }

  @Test
  @DisplayName("a running run is ranked by its elapsed time although it reports no duration")
  void runningRunIsRankedByElapsedTime() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(
        run(80, BASE, BASE.plusSeconds(100), TRState.FINISHED),
        run(81, BASE.plusSeconds(60), BASE.plusHours(1), TRState.RUNNING));
    stubAggregate(
        Map.of(
            80L, new RunEventAggregate(new RunScoreSnapshot(30, 0), Map.of(), Map.of()),
            81L, new RunEventAggregate(new RunScoreSnapshot(30, 0), Map.of(), Map.of())));

    long nowMillis = toEpochMillis(BASE.plusSeconds(100));
    TrainingInstanceScoreReportDTO report = sut.createReport(INSTANCE_ID, Map.of(), nowMillis);

    assertEquals(81L, report.getRows().get(0).getTrainingRunId());
  }

  @Test
  @DisplayName("a higher score outranks a shorter but lower-scoring run")
  void higherScoreOutranksShorterRun() {
    stubLevels(trainingLevel(10, 0));
    stubRuns(
        run(90, BASE, BASE.plusSeconds(200), TRState.FINISHED),
        run(91, BASE, BASE.plusSeconds(10), TRState.FINISHED));
    stubAggregate(
        Map.of(
            90L, new RunEventAggregate(new RunScoreSnapshot(50, 0), Map.of(), Map.of()),
            91L, new RunEventAggregate(new RunScoreSnapshot(20, 0), Map.of(), Map.of())));

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE.plusHours(1)));

    assertEquals(90L, report.getRows().get(0).getTrainingRunId());
  }

  @Test
  @DisplayName("participants are identified by their resolved login, display name and mail")
  void participantsAreIdentified() {
    stubLevels(trainingLevel(10, 0));
    TrainingRun trainingRun = run(100, BASE, BASE.plusSeconds(5), TRState.FINISHED);
    stubRuns(trainingRun);
    stubAggregate(Map.of());

    UserRefDTO participant = new UserRefDTO();
    participant.setUserRefSub("jane.doe");
    participant.setUserRefFullName("Jane Doe");
    participant.setMail("jane.doe@example.cz");

    TrainingInstanceScoreReportDTO report =
        sut.createReport(
            INSTANCE_ID,
            Map.of(trainingRun.getParticipantRef().getUserRefId(), participant),
            toEpochMillis(BASE));

    ParticipantScoreRowDTO row = rowFor(report, 100L);
    assertEquals("jane.doe", row.getLogin());
    assertEquals("Jane Doe", row.getName());
    assertEquals("jane.doe@example.cz", row.getMail());
  }

  @Test
  @DisplayName("the report states the instance end and the requested instance id")
  void reportStatesInstanceEndAndId() {
    stubLevels(trainingLevel(10, 0));
    stubRuns();
    stubAggregate(Map.of());

    TrainingInstanceScoreReportDTO report =
        sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    assertEquals(INSTANCE_ID, report.getTrainingInstanceId());
    assertEquals(toEpochMillis(instance.getEndTime()), report.getInstanceEndAt());
  }

  @Test
  @DisplayName("aggregation is asked for exactly the run ids and level ids of the instance")
  void aggregationIsAskedForRunsAndLevelsOfInstance() {
    AbstractLevel levelA = trainingLevel(90, 0);
    AbstractLevel levelB = assessmentLevel(91, 1, AssessmentType.TEST);
    AbstractLevel levelC = infoLevel(92, 2);
    stubLevels(levelA, levelB, levelC);
    stubRuns(
        run(80, BASE, BASE.plusSeconds(5), TRState.FINISHED),
        run(81, BASE, BASE.plusSeconds(5), TRState.FINISHED));

    ArgumentCaptor<Collection<Long>> runIdsCaptor = ArgumentCaptor.forClass(Collection.class);
    ArgumentCaptor<Collection<Long>> levelIdsCaptor = ArgumentCaptor.forClass(Collection.class);
    given(
            trainingScoreAggregationService.aggregateRuns(
                eq(INSTANCE_ID), runIdsCaptor.capture(), levelIdsCaptor.capture(), any(), any()))
        .willReturn(Map.of());

    sut.createReport(INSTANCE_ID, Map.of(), toEpochMillis(BASE));

    assertThat(runIdsCaptor.getValue()).containsExactlyInAnyOrder(80L, 81L);
    assertThat(levelIdsCaptor.getValue()).containsExactlyInAnyOrder(90L, 91L, 92L);
  }

  @Test
  @DisplayName("reports exactly the participant ref ids the repository holds for the instance")
  void findParticipantRefIdsReturnsRepositoryIds() {
    given(userRefRepository.findParticipantsRefIdsByTrainingInstanceId(INSTANCE_ID))
        .willReturn(Set.of(100L, 200L, 300L));

    Set<Long> participantRefIds = sut.findParticipantRefIds(INSTANCE_ID);

    assertThat(participantRefIds).containsExactlyInAnyOrder(100L, 200L, 300L);
  }

  @Test
  @DisplayName("an instance with no participants yields an empty set")
  void findParticipantRefIdsReturnsEmptySetForInstanceWithNoParticipants() {
    given(userRefRepository.findParticipantsRefIdsByTrainingInstanceId(INSTANCE_ID))
        .willReturn(Set.of());

    Set<Long> participantRefIds = sut.findParticipantRefIds(INSTANCE_ID);

    assertTrue(participantRefIds.isEmpty());
  }
}
