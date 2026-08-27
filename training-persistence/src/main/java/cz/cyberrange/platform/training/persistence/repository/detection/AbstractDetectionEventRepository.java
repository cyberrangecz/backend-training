package cz.cyberrange.platform.training.persistence.repository.detection;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.QAbstractDetectionEvent;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Manages the {@link AbstractDetectionEvent} rows shared by every kind of cheating-detection
 * finding, the root of a JOINED-inheritance hierarchy each concrete finding extends.
 */
@Repository
public interface AbstractDetectionEventRepository
    extends JpaRepository<AbstractDetectionEvent, Long>,
        AbstractDetectionEventRepositoryCustom,
        QuerydslPredicateExecutor<AbstractDetectionEvent>,
        QuerydslBinderCustomizer<QAbstractDetectionEvent> {

  /**
   * Binds every {@code String} property so that, when queried through a Querydsl web binding, it
   * matches case-insensitively and by substring, ANDing together every value supplied for it.
   */
  @Override
  default void customize(
      QuerydslBindings querydslBindings, QAbstractDetectionEvent qAbstractDetectionEvent) {
    querydslBindings
        .bind(String.class)
        .all(
            (StringPath path, Collection<? extends String> values) -> {
              BooleanBuilder predicate = new BooleanBuilder();
              values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
              return Optional.ofNullable(predicate);
            });
  }

  /**
   * Deletes every {@link AbstractDetectionEvent} row of one cheating detection sweep in a single
   * bulk statement, which bypasses the persistence context: it does not detach or evict any
   * already-loaded instance. Because the base table is the root of a JOINED-inheritance
   * hierarchy, this deletes only from {@code abstract_detection_event}, leaving behind the
   * counterpart row in whichever concrete finding table the deleted event belonged to.
   *
   * @param cheatingDetectionId the cheating detection whose events are deleted
   */
  @Modifying
  void deleteDetectionEventsOfCheatingDetection(
      @Param("cheatingDetectionId") Long cheatingDetectionId);

  /**
   * Returns, as one page of distinct rows, the detection events of one cheating detection sweep
   * that also satisfy the given predicate. Delegates to the Querydsl query in
   * {@code AbstractDetectionEventRepositoryImpl}.
   *
   * @param cheatingDetectionId the cheating detection the returned events belong to
   * @param pageable the page to return; a null value defaults to the first page of 20 rows
   * @param predicate an extra condition ANDed onto the cheating detection filter, or null
   */
  Page<AbstractDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId,
      @Param("pageable") Pageable pageable,
      Predicate predicate);

  /**
   * Returns every detection event of one cheating detection sweep, across every finding kind, in
   * no defined order. Each returned instance is the concrete finding subtype the row belongs to.
   *
   * @param cheatingDetectionId the cheating detection the returned events belong to
   */
  List<AbstractDetectionEvent> findAllByCheatingDetectionId(
      @Param("cheatingDetectionId") Long cheatingDetectionId);

  /**
   * Delete all cheats by training instance.
   *
   * @param trainingInstanceId the training instance id
   */
  @Modifying
  void deleteDetectionEventsOfTrainingInstance(
      @Param("trainingInstanceId") Long trainingInstanceId);

  /**
   * Counts the detection events of one cheating detection sweep, across every finding kind.
   *
   * @param cheatingDetectionId the cheating detection whose events are counted
   */
  Long getNumberOfDetections(@Param("cheatingDetectionId") Long cheatingDetectionId);

  /**
   * Returns the detection event with the given primary key, as its concrete finding subtype.
   *
   * @param eventId the primary key of the detection event
   */
  AbstractDetectionEvent findDetectionEventById(@Param("eventId") Long eventId);
}
