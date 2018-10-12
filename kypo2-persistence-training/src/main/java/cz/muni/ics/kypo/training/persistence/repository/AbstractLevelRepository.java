package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.repository.custom.AbstractLevelRepositoryCustom;

@Repository
public interface AbstractLevelRepository extends AbstractLevelRepositoryCustom<AbstractLevel> {
}
