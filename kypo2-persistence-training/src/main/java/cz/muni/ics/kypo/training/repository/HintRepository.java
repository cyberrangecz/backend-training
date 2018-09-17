package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.Hint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Repository
public interface HintRepository extends JpaRepository<Hint, Long>, QuerydslPredicateExecutor<Hint> {

}
