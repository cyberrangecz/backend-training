package cz.cyberrange.platform.training.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.querydsl.core.BooleanBuilder;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@DataJpaTest
class PageableIdTiebreakerTest {

  private static final int DEFINITION_COUNT = 7;
  private static final int PAGE_SIZE = 3;
  private static final long AUTHOR_USER_REF_ID = 41L;

  private enum PagedQuery {
    PREDICATE_WITH_ENTITY_GRAPH(
        (repository, pageable) -> repository.findAll(new BooleanBuilder(), pageable)),
    STATE_QUERY((repository, pageable) -> repository.findAllByState(TDState.RELEASED, pageable)),
    QUERYDSL_FRAGMENT(
        (repository, pageable) -> repository.findAll(null, pageable, AUTHOR_USER_REF_ID));

    private final BiFunction<TrainingDefinitionRepository, Pageable, Page<TrainingDefinition>>
        query;

    PagedQuery(BiFunction<TrainingDefinitionRepository, Pageable, Page<TrainingDefinition>> query) {
      this.query = query;
    }
  }

  @Autowired private TestEntityManager entityManager;
  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingDefinitionRepository trainingDefinitionRepository;

  private List<Long> definitionIdsAscending;

  @BeforeEach
  void persistDefinitionsSharingOneTitle() {
    UserRef firstAuthor = persistedAuthor(AUTHOR_USER_REF_ID);
    UserRef secondAuthor = persistedAuthor(AUTHOR_USER_REF_ID + 1);
    List<Long> definitionIds = new ArrayList<>();
    for (int index = 0; index < DEFINITION_COUNT; index++) {
      TrainingDefinition definition = testDataFactory.getReleasedDefinition();
      definition.setTitle("same title");
      definition.addAuthor(firstAuthor);
      definition.addAuthor(secondAuthor);
      entityManager.persist(definition);
      definitionIds.add(definition.getId());
    }
    entityManager.flush();
    entityManager.clear();
    definitionIdsAscending = definitionIds.stream().sorted(Comparator.naturalOrder()).toList();
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PagedQuery.class)
  @DisplayName("ties on the requested title sort: pages hold every definition once, no gap")
  void pagedQuery_tiesOnRequestedSort_pagesHoldEveryDefinitionOnce(PagedQuery pagedQuery) {
    assertEquals(definitionIdsAscending, idsOfAllPages(pagedQuery, Sort.by("title")));
  }

  @ParameterizedTest(name = "id requested descending = {0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("findAllByState query: ascending id without a sort, the requested id direction kept")
  void findAllByState_idDirection_followsRequestedDirectionOrAscendingDefault(
      boolean idRequestedDescending) {
    Sort requestedSort = idRequestedDescending ? Sort.by(Direction.DESC, "id") : Sort.unsorted();

    List<Long> pagedIds = idsOfAllPages(PagedQuery.STATE_QUERY, requestedSort);

    assertEquals(
        idRequestedDescending ? definitionIdsAscending.reversed() : definitionIdsAscending,
        pagedIds);
  }

  @Test
  @DisplayName("findAllByIdInOrderById list, ids given in descending order: ascending id")
  void findAllByIdInOrderById_idsGivenDescending_returnsAscendingIdOrder() {
    List<Long> ids =
        trainingDefinitionRepository
            .findAllByIdInOrderById(definitionIdsAscending.reversed())
            .stream()
            .map(TrainingDefinition::getId)
            .toList();

    assertEquals(definitionIdsAscending, ids);
  }

  private UserRef persistedAuthor(long userRefId) {
    UserRef author = new UserRef();
    author.setUserRefId(userRefId);
    return entityManager.persist(author);
  }

  private List<Long> idsOfAllPages(PagedQuery pagedQuery, Sort requestedSort) {
    List<Long> ids = new ArrayList<>();
    Page<TrainingDefinition> page;
    int pageNumber = 0;
    do {
      page =
          pagedQuery.query.apply(
              trainingDefinitionRepository, PageRequest.of(pageNumber++, PAGE_SIZE, requestedSort));
      page.forEach(definition -> ids.add(definition.getId()));
    } while (page.hasNext());
    return ids;
  }
}
