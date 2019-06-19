package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.UserRef;
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
     * Find all users.
     *
     * @param userRefLogin the user ref login
     * @return the set of {@link UserRef}
     */
    @Query("SELECT DISTINCT ur FROM UserRef ur WHERE ur.userRefLogin IN :userRefLogin")
    Set<UserRef> findUsers(@Param("userRefLogin") Set<String> userRefLogin);

    /**
     * Find user by user ref login.
     *
     * @param userLogin the user login
     * @return the {@link UserRef}
     */
    @Query("SELECT ur FROM UserRef ur WHERE userRefLogin = :userLogin")
    Optional<UserRef> findUserByUserRefLogin(@Param("userLogin") String
                                                     userLogin);

}
