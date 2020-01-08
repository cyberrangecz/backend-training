package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.BetaTestingGroup;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


/**
 * The JPA repository interface to manage {@link BetaTestingGroup} instances.
 *
 */
public interface BetaTestingGroupRepository extends JpaRepository<BetaTestingGroup, Long>, QuerydslPredicateExecutor<InfoLevel> {
}
