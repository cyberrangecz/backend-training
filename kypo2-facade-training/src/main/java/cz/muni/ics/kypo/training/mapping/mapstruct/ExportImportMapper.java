package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.ExportTrainingDefinitionsAndLevelsDTO;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;


/**
 * @author Pavel Seda
 */
@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ExportImportMapper extends ParentMapper {

    ExportTrainingDefinitionsAndLevelsDTO mapToExportDTO(TrainingDefinition entity);

}
