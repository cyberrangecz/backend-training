package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Custom {@link TrainingDefinition} lookup, restricted to one user's own definitions. */
public interface TrainingDefinitionRepositoryCustom {

  /**
   * Finds the training definitions the given user authored, or that belong to a beta testing group
   * the user organizes, additionally narrowed by the given predicate. {@code loggedInUserId} is
   * matched against each candidate user's cross-service {@code userRefId}, not a local primary key.
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @param loggedInUserId the cross-service user identifier of the logged in user
   * @return the page of training definitions
   */
  Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable, Long loggedInUserId);
}
