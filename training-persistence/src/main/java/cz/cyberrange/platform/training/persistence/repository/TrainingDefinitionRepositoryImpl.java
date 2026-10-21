package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.cyberrange.platform.training.persistence.model.QTrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.QUserRef;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the {@link TrainingDefinition} lookup declared by {@link
 * TrainingDefinitionRepositoryCustom}
 */
@Repository
public class TrainingDefinitionRepositoryImpl extends QuerydslRepositorySupport
    implements TrainingDefinitionRepositoryCustom {

  @PersistenceContext private EntityManager entityManager;

  /**
   * Configures the QueryDSL support base class to build queries against {@link TrainingDefinition}
   */
  public TrainingDefinitionRepositoryImpl() {
    super(TrainingDefinition.class);
  }

  /**
   * Joins each candidate definition to its authors and keeps only the definitions where an author
   * has the given cross-service {@code userRefId}. Deduplicates the joined rows before applying the
   * given predicate and paging.
   */
  @Override
  @Transactional
  public Page<TrainingDefinition> findAll(
      Predicate predicate, Pageable pageable, Long loggedInUserId) {
    Objects.requireNonNull(loggedInUserId, "Input logged in user ID must not be null.");
    QTrainingDefinition trainingDefinition = QTrainingDefinition.trainingDefinition;
    QUserRef authors = new QUserRef("authors");

    JPQLQuery<TrainingDefinition> query =
        new JPAQueryFactory(entityManager)
            .selectFrom(trainingDefinition)
            .distinct()
            .join(trainingDefinition.authors, authors)
            .where(authors.userRefId.eq(loggedInUserId));

    if (predicate != null) {
      query.where(predicate);
    }
    return getPage(query, pageable);
  }

  private <T> Page getPage(JPQLQuery<T> query, Pageable pageable) {
    if (pageable == null) {
      pageable = PageRequest.of(0, 20);
    }
    query = getQuerydsl().applyPagination(pageable, query);
    long count = query.fetchCount();
    return new PageImpl<>(query.fetch(), pageable, count);
  }
}
