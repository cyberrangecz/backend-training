package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.UserRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * The JPA repository interface to manage {@link UserRef} instances.
 *
 * @author Pavel Seda
 */
@Repository
public interface UserRefRepository extends JpaRepository<UserRef, Long>,
        QuerydslPredicateExecutor<UserRef> {

    /**
     * Find all users by userRefIds.
     *
     * @param userRefId the user ref id
     * @return the set of {@link UserRef}
     */
    @Query("SELECT ur FROM UserRef ur WHERE ur.userRefId IN :userRefId")
    Set<UserRef> findUsers(@Param("userRefId") Set<Long> userRefId);

    /**
     * Find user by user ref id.
     *
     * @param userRefId the user id
     * @return the {@link UserRef}
     */
    @Query("SELECT ur FROM UserRef ur WHERE ur.userRefId = :userRefId")
    Optional<UserRef> findUserByUserRefId(@Param("userRefId") Long userRefId);

    /**
     * Find user by user ref login and issuer.
     *
     * @param userRefLogin the user login
     * @param iss the issuer which provides authentication of the user
     * @return the {@link UserRef}
     */
    Optional<UserRef> findUserByUserRefLoginAndIss(String userRefLogin, String iss);
}
