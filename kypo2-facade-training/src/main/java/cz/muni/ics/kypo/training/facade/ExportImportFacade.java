package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.ExportTrainingDefinitionsAndLevelsDTO;

import java.util.List;

/**
 * @author Pavel Seda
 */
public interface ExportImportFacade {

    List<ExportTrainingDefinitionsAndLevelsDTO> dbExport();

}
