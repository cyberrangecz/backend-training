package cz.muni.ics.kypo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.model.Hint;
import cz.muni.ics.kypo.model.AbstractLevel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Repository
public interface AbstractLevelRepository extends JpaRepository<AbstractLevel, Long>, QuerydslPredicateExecutor<AbstractLevel> {

}
