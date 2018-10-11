package cz.muni.ics.kypo.training.persistence.repository.custom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;

/**
 * Just for showing how custom repositories in Spring data could be done.
 *
 * @author Pavel Seda (441048)
 *
 */
@NoRepositoryBean
public interface AbstractLevelRepositoryCustom<T extends AbstractLevel> extends JpaRepository<T, Long>, QuerydslPredicateExecutor<T> {

}
