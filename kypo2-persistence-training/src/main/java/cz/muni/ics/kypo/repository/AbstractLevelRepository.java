package cz.muni.ics.kypo.repository;

import cz.muni.ics.kypo.model.AbstractLevel;
import cz.muni.ics.kypo.repository.custom.AbstractLevelRepositoryCustom;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AbstractLevelRepository extends AbstractLevelRepositoryCustom<AbstractLevel> {
}
