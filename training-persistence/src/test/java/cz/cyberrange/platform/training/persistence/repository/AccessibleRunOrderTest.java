package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.querydsl.core.BooleanBuilder;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@DataJpaTest
class AccessibleRunOrderTest {

  private static final long PARTICIPANT_USER_REF_ID = 77L;
  private static final int RUNS_PER_INSTANCE = 2;
  private static final int PAGE_SIZE = 3;
  private static final String INSTANCE_TITLE_PROPERTY = "trainingInstance.title";
  private static final List<String> INSTANCE_TITLES = List.of("beta", "alpha", "beta");

  private interface RunIdFinder {
    List<Long> find(TrainingRunRepository repository, Long trainingInstanceId);
  }

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingRunRepository trainingRunRepository;

  private final Map<Long, String> instanceTitleByRunId = new HashMap<>();
  private List<Long> runIdsAscending;
  private List<Long> firstInstanceRunIdsAscending;
  private Long firstInstanceId;

  @BeforeEach
  void persistRunsOfInstancesSharingTitles() {
    TrainingDefinition definition = entityManager.persist(testDataFactory.getArchivedDefinition());
    InfoLevel level = entityManager.persist(testDataFactory.getInfoLevel1());
    UserRef participant = new UserRef();
    participant.setUserRefId(PARTICIPANT_USER_REF_ID);
    entityManager.persist(participant);

    List<Long> runIds = new ArrayList<>();
    List<Long> firstInstanceRunIds = new ArrayList<>();
    int sandboxCounter = 0;
    for (int instanceIndex = 0; instanceIndex < INSTANCE_TITLES.size(); instanceIndex++) {
      TrainingInstance instance = testDataFactory.getOngoingInstance();
      instance.setTitle(INSTANCE_TITLES.get(instanceIndex));
      instance.setAccessToken("token-" + instanceIndex);
      instance.setTrainingDefinition(definition);
      entityManager.persist(instance);
      if (instanceIndex == 0) {
        firstInstanceId = instance.getId();
      }
      for (int runIndex = 0; runIndex < RUNS_PER_INSTANCE; runIndex++) {
        TrainingRun run = testDataFactory.getRunningRun();
        run.setCurrentLevel(level);
        run.setParticipantRef(participant);
        run.setTrainingInstance(instance);
        run.setIncorrectAnswerCount(sandboxCounter);
        run.setSandboxInstanceRefId("sandbox-" + sandboxCounter);
        run.setSandboxInstanceAllocationId(sandboxCounter++);
        entityManager.persist(run);
        runIds.add(run.getId());
        if (instanceIndex == 0) {
          firstInstanceRunIds.add(run.getId());
        }
        instanceTitleByRunId.put(run.getId(), instance.getTitle());
      }
    }
    entityManager.flush();
    entityManager.clear();
    runIdsAscending = runIds.stream().sorted().toList();
    firstInstanceRunIdsAscending = firstInstanceRunIds.stream().sorted().toList();
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Direction.class,
      names = {"ASC", "DESC"})
  @DisplayName("accessible runs sorted by instance title: ties keep ascending run id across pages")
  void findAllByParticipantRefId_sortedByInstanceTitle_tiesOrderedByRunId(Direction direction) {
    Comparator<Long> byInstanceTitle = Comparator.comparing(instanceTitleByRunId::get);
    List<Long> expectedIds =
        runIdsAscending.stream()
            .sorted(direction.isAscending() ? byInstanceTitle : byInstanceTitle.reversed())
            .toList();

    List<Long> pagedIds = new ArrayList<>();
    Page<TrainingRun> page;
    int pageNumber = 0;
    do {
      page =
          trainingRunRepository.findAllByParticipantRefId(
              PARTICIPANT_USER_REF_ID,
              new BooleanBuilder(),
              PageRequest.of(pageNumber++, PAGE_SIZE, Sort.by(direction, INSTANCE_TITLE_PROPERTY)));
      page.forEach(run -> pagedIds.add(run.getId()));
    } while (page.hasNext());

    assertEquals(expectedIds, pagedIds);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unsortedRunFinders")
  @DisplayName("runs without a requested sort come in ascending id order")
  void runFinder_noSortRequested_returnsRunsInAscendingIdOrder(
      String finderName, RunIdFinder finder, boolean confinedToFirstInstance) {
    List<Long> expectedIds =
        confinedToFirstInstance ? firstInstanceRunIdsAscending : runIdsAscending;

    assertEquals(expectedIds, finder.find(trainingRunRepository, firstInstanceId));
  }

  private static Stream<Arguments> unsortedRunFinders() {
    return Stream.of(
        Arguments.of(
            "page of one instance",
            (RunIdFinder)
                (repository, instanceId) ->
                    repository
                        .findAllByTrainingInstanceId(instanceId, PageRequest.of(0, PAGE_SIZE))
                        .map(TrainingRun::getId)
                        .getContent(),
            true),
        Arguments.of(
            "set of one instance",
            (RunIdFinder)
                (repository, instanceId) ->
                    repository.findAllByTrainingInstanceIdOrderById(instanceId).stream()
                        .map(TrainingRun::getId)
                        .toList(),
            true),
        Arguments.of(
            "list of one participant",
            (RunIdFinder)
                (repository, instanceId) ->
                    repository.findAllByParticipantRefId(PARTICIPANT_USER_REF_ID).stream()
                        .map(TrainingRun::getId)
                        .toList(),
            false));
  }
}
