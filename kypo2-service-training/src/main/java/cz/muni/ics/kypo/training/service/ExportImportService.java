package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;

import java.util.List;
import java.util.Set;

/**
 * @author Pavel Seda
 */
public interface ExportImportService {

    TrainingDefinition findById(Long trainingDefinitionId);

    List<AbstractLevel> findAllLevelsFromDefinition(Long levelId);

    Long createLevel(AbstractLevel level);

    TrainingInstance findInstanceById(Long trainingInstanceId);

    Set<TrainingRun> findRunsByInstanceId(Long trainingInstanceId);
}
