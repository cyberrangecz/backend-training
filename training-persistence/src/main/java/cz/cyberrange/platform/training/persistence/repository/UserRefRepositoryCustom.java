package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.UserRef;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

/** Custom lookup and insert for {@link UserRef} that a derived or named query cannot express. */
public interface UserRefRepositoryCustom {

  /**
   * Inserts a {@link UserRef} row for the given cross-service user identifier if none exists yet,
   * then returns the row for that identifier, whether newly inserted or already present.
   *
   * @param userRefId the cross-service user identifier
   * @return the {@link UserRef} row matching the given identifier
   */
  @Modifying
  @Transactional
  UserRef createOrGet(@Param("userRefId") Long userRefId);
}
