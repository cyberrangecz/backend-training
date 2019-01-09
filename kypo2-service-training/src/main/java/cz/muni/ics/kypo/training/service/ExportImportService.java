package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;

import java.util.List;

/**
 * @author Pavel Seda
 */
public interface ExportImportService {

    List<TrainingDefinition> findAllTrainingDefinitions();

    List<AbstractLevel> findAllLevels();
}