package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.InfoLevel;

/**
 * @author Pavel Seda (441048)
 */
@Repository
public interface InfoLevelRepository extends JpaRepository<InfoLevel, Long>, QuerydslPredicateExecutor<InfoLevel> {

}
