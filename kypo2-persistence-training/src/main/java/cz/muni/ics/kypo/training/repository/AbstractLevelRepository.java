package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.AbstractLevel;
import cz.muni.ics.kypo.training.repository.custom.AbstractLevelRepositoryCustom;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractLevelRepository extends AbstractLevelRepositoryCustom<AbstractLevel> {
}
