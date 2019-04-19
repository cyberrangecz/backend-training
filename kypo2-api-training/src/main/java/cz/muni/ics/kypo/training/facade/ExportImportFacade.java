package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;

/**
 * @author Pavel Seda
 */
public interface ExportImportFacade {

    FileToReturnDTO dbExport(Long trainingDefinitionId);

    TrainingDefinitionByIdDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO);

    FileToReturnDTO archiveTrainingInstance(Long trainingInstanceId);
}
