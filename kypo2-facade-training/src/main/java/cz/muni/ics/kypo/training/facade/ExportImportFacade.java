package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;

/**
 * @author Pavel Seda
 */
public interface ExportImportFacade {

    ExportTrainingDefinitionAndLevelsDTO dbExport(Long trainingDefinitionId);

    TrainingDefinitionDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO);
}
