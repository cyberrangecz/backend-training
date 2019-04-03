package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;

import java.util.List;

/**
 * @author Pavel Seda
 */
public interface ExportImportService {

    TrainingDefinition findById(Long trainingDefinitionId);

    List<AbstractLevel> findAllLevelsFromDefinition(Long levelId);

    Long createLevel(AbstractLevel level);

    TrainingInstance findInstanceById(Long trainingInstanceId);

}
