package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.sandboxdefinition.SandboxDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import org.springframework.http.ResponseEntity;

/**
 * @author Pavel Seda
 */
public interface ExportImportFacade {

    ExportTrainingDefinitionAndLevelsDTO dbExport(Long trainingDefinitionId);

    TrainingDefinitionDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO);

    TrainingInstanceArchiveDTO archiveTrainingInstance(Long trainingInstanceId);
}
