package cz.muni.ics.kypo.training.repository.custom;

import cz.muni.ics.kypo.training.model.AbstractLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Just for showing how custom repositories in Spring data could be done.
 *
 * @author Pavel Seda (441048)
 *
 */
@NoRepositoryBean
public interface AbstractLevelRepositoryCustom<T extends AbstractLevel>  extends JpaRepository<T, Long>, QuerydslPredicateExecutor<T> {


}
