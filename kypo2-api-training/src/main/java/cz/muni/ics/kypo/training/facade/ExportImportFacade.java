package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;

/**
 * The interface for export/import facade.
 *
 */
public interface ExportImportFacade {

    /**
     * Exports Training Definition to file
     *
     * @param trainingDefinitionId the id of the definition to be exported
     * @return the file containing definition, {@link FileToReturnDTO}
     */
    FileToReturnDTO dbExport(Long trainingDefinitionId);

    /**
     * Imports training definition.
     *
     * @param importTrainingDefinitionDTO the training definition to be imported
     * @return the {@link TrainingDefinitionByIdDTO}
     */
    TrainingDefinitionByIdDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO);

    /**
     * Exports Training Instance to file
     *
     * @param trainingInstanceId the id of the instance to be exported
     * @return the file containing instance, {@link FileToReturnDTO}
     */
    FileToReturnDTO archiveTrainingInstance(Long trainingInstanceId);
}
