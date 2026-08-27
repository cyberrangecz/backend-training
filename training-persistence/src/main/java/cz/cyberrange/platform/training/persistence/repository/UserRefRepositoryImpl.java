package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.UserRef;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/** Implements the {@link UserRef} lookup and insert declared by {@link UserRefRepositoryCustom} */
@Repository
public class UserRefRepositoryImpl implements UserRefRepositoryCustom {

  private static final Logger LOG = LoggerFactory.getLogger(UserRefRepositoryImpl.class);

  @PersistenceContext private EntityManager entityManager;

  /**
   * Runs a native {@code INSERT ... ON CONFLICT DO NOTHING} against the {@code user_ref} table's
   * {@code user_ref_id} column, then fetches and returns the row for that value, whether the insert
   * created it or it already existed. The unique constraint on {@code user_ref_id} is what makes
   * the conflict path silent rather than an error.
   */
  @Override
  public UserRef createOrGet(Long userRefId) {
    int rowsAffected =
        entityManager
            .createNativeQuery(
                "INSERT INTO user_ref (user_ref_id) VALUES (:userRefId)" + "ON CONFLICT DO NOTHING")
            .setParameter("userRefId", userRefId)
            .executeUpdate();

    if (rowsAffected != 0) {
      LOG.info("User ref with user_ref_id: {} created.", userRefId);
    }

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<UserRef> criteriaQuery = criteriaBuilder.createQuery(UserRef.class);
    Root<UserRef> root = criteriaQuery.from(UserRef.class);
    criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("userRefId"), userRefId));
    return entityManager.createQuery(criteriaQuery).getSingleResult();
  }
}
