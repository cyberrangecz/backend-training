package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Custom {@link TrainingInstance} lookup, restricted to instances one user organizes. */
public interface TrainingInstanceRepositoryCustom {

  /**
   * Finds the training instances the given user organizes, additionally narrowed by the given
   * predicate. {@code loggedInUserId} is matched against each candidate organizer's cross-service
   * {@code userRefId}, not a local primary key.
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @param loggedInUserId the cross-service user identifier of the logged in user
   * @return the page of training instances
   */
  Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable, Long loggedInUserId);
}
