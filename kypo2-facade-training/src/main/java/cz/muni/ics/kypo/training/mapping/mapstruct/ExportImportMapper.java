package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import org.mapstruct.Mapper;

/**
 * @author Pavel Seda
 */
@Mapper(componentModel = "spring",
        uses = {UserRefMapper.class})
public interface ExportImportMapper extends ParentMapper {

    ExportTrainingDefinitionAndLevelsDTO mapToDTO(TrainingDefinition entity);
    TrainingDefinition mapToEntity(ImportTrainingDefinitionDTO dto);
}
