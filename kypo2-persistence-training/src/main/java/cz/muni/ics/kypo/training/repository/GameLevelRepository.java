package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.repository.custom.AbstractLevelRepositoryCustom;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Repository
public interface GameLevelRepository extends AbstractLevelRepositoryCustom<GameLevel>, QuerydslPredicateExecutor<GameLevel> {

}
