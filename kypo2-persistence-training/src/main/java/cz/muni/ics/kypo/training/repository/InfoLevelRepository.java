package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.InfoLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Repository
public interface InfoLevelRepository extends JpaRepository<InfoLevel, Long>, QuerydslPredicateExecutor<InfoLevel> {

}
