package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionsAndLevelsDTO;

/**
 * @author Pavel Seda
 */
public interface ExportImportFacade {

    ExportTrainingDefinitionsAndLevelsDTO dbExport();

}
