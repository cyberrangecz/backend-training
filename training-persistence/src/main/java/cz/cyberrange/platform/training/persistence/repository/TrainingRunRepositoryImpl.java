package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.cyberrange.platform.training.persistence.model.QTrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.QTrainingInstance;
import cz.cyberrange.platform.training.persistence.model.QTrainingRun;
import cz.cyberrange.platform.training.persistence.model.QUserRef;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Implements the {@link TrainingRun} lookups declared by {@link TrainingRunRepositoryCustom} */
public class TrainingRunRepositoryImpl extends QuerydslRepositorySupport
    implements TrainingRunRepositoryCustom {

  @PersistenceContext private EntityManager entityManager;

  /** Configures the QueryDSL support base class to build queries against {@link TrainingRun}. */
  public TrainingRunRepositoryImpl() {
    super(TrainingRun.class);
  }

  /**
   * Left-joins each candidate run to its participant reference, its training instance, and that
   * instance's training definition, then keeps only the runs whose participant reference has the
   * given cross-service {@code userRefId}. Deduplicates the joined rows before applying the given
   * predicate and paging.
   */
  @Override
  @Transactional
  public Page<TrainingRun> findAllByParticipantRefId(
      @Param("userRefId") Long userRefId, Predicate predicate, Pageable pageable) {

    QTrainingRun trainingRun = QTrainingRun.trainingRun;
    QUserRef participantRef = new QUserRef("participantRef");
    QTrainingInstance trainingInstance = new QTrainingInstance("trainingInstance");
    QTrainingDefinition trainingDefinition = new QTrainingDefinition("trainingDefinition");

    JPQLQuery<TrainingRun> query =
        new JPAQueryFactory(entityManager)
            .selectFrom(trainingRun)
            .distinct()
            .leftJoin(trainingRun.participantRef, participantRef)
            .leftJoin(trainingRun.trainingInstance, trainingInstance)
            .leftJoin(trainingInstance.trainingDefinition, trainingDefinition)
            .where(participantRef.userRefId.eq(userRefId));

    if (predicate != null) {
      query.where(predicate);
    }
    return getPage(query, pageable);
  }

  /**
   * Keeps the runs whose primary key is one of the given ids and whose participant reference has
   * the given cross-service {@code userRefId}. Runs in a read-only transaction; returns an empty
   * list when none match.
   */
  @Override
  @Transactional(readOnly = true)
  public List<TrainingRun> findAllByIdInAndParticipantRefId(
      @Param("runIds") List<Long> runIds, @Param("userRefId") Long userRefId) {
    QTrainingRun trainingRun = QTrainingRun.trainingRun;
    return new JPAQueryFactory(entityManager)
        .selectFrom(trainingRun)
        .where(trainingRun.id.in(runIds).and(trainingRun.participantRef.userRefId.eq(userRefId)))
        .fetch();
  }

  /**
   * Keeps the runs whose primary key is one of the given ids and whose training instance has an
   * organizer with the given cross-service {@code userRefId}. Runs in a read-only transaction;
   * returns an empty list when none match.
   */
  @Override
  @Transactional(readOnly = true)
  public List<TrainingRun> findAllByIdInAndOrganizedByUser(
      @Param("runIds") List<Long> runIds, @Param("userRefId") Long userRefId) {
    QTrainingRun trainingRun = QTrainingRun.trainingRun;
    QTrainingInstance trainingInstance = QTrainingInstance.trainingInstance;
    QUserRef organizer = new QUserRef("organizer");

    return new JPAQueryFactory(entityManager)
        .selectFrom(trainingRun)
        .distinct()
        .innerJoin(trainingRun.trainingInstance, trainingInstance)
        .innerJoin(trainingInstance.organizers, organizer)
        .where(trainingRun.id.in(runIds).and(organizer.userRefId.eq(userRefId)))
        .fetch();
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
