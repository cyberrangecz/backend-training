package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.OrganizerRef;
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
public interface OrganizerRefRepository extends JpaRepository<OrganizerRef, Long>, QuerydslPredicateExecutor<OrganizerRef> {

    @Query("SELECT DISTINCT orgRef FROM OrganizerRef orgRef WHERE orgRef.id IN :organizersIds")
    Set<OrganizerRef> findUsers(@Param("organizersIds") Set<Long> organizersIds);
}
