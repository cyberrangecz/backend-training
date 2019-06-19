package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.BetaTestingGroup;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


/**
 * The JPA repository interface to manage {@link BetaTestingGroup} instances.
 *
 * @author Dominik Pilar (445537)
 */
public interface BetaTestingGroupRepository extends JpaRepository<BetaTestingGroup, Long>, QuerydslPredicateExecutor<InfoLevel> {
}
