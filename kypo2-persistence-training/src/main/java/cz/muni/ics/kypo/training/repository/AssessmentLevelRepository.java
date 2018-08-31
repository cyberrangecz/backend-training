package cz.muni.ics.kypo.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.model.AssessmentLevel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Repository
public interface AssessmentLevelRepository
    extends JpaRepository<AssessmentLevel, Long>, QuerydslPredicateExecutor<AssessmentLevel>{

}
