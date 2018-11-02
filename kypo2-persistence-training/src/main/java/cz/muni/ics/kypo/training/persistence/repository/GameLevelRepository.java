package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import cz.muni.ics.kypo.training.persistence.repository.custom.AbstractLevelRepositoryCustom;

/**
 * @author Pavel Seda (441048)
 */
@Repository
public interface GameLevelRepository extends AbstractLevelRepositoryCustom<GameLevel>, QuerydslPredicateExecutor<GameLevel> {

}
