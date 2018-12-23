package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.UserRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author Pavel Seda
 */
@Repository
public interface UserRefRepository extends JpaRepository<UserRef, Long>, QuerydslPredicateExecutor<UserRef> {


    @Query("SELECT DISTINCT ur FROM UserRef ur WHERE ur.id IN :usersIds")
    Set<UserRef> findUsers(@Param("usersIds") Set<Long> usersIds);
}
