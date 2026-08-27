package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.Hint;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link Hint} instances. */
@Repository
public interface HintRepository extends JpaRepository<Hint, Long>, QuerydslPredicateExecutor<Hint> {

  /**
   * Deletes every hint whose {@code training_level_id} column equals the given training level
   * primary key. Established by the {@code Hint.deleteHintsByLevelId} named query declared on
   * {@link Hint}. Runs as a bulk delete against the database, bypassing the persistence context; it
   * does not remove the deleted hints from an already loaded training level's hint collection, and
   * triggers no cascade since {@link Hint} owns no further associations.
   *
   * @param levelId the training level id
   */
  @Modifying
  void deleteHintsByLevelId(@Param("levelId") Long levelId);

  /**
   * Finds the hints whose primary key is one of the given ids. Derived from the method name, with
   * no named query of this name on {@link Hint}; imposes no ordering. Returns an empty list when
   * none of the ids match.
   *
   * @param ids the hint ids
   * @return the matching {@link Hint}s, associations left lazy
   */
  List<Hint> findAllByIdIn(Collection<Long> ids);
}
