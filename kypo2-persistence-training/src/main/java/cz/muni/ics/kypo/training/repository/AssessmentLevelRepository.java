package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.repository.custom.AbstractLevelRepositoryCustom;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
/*
@Repository
public interface AssessmentLevelRepository
    extends JpaRepository<AssessmentLevel, Long>, QuerydslPredicateExecutor<AssessmentLevel>{

}
*/
@Repository
public interface AssessmentLevelRepository extends AbstractLevelRepositoryCustom<AssessmentLevel>, QuerydslPredicateExecutor<AssessmentLevel> {

}
