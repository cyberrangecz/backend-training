package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.export.TrainingDefinitionExportDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec & Pavel Seda
 */
@Mapper(componentModel = "spring",
        uses = {UserRefMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TrainingDefinitionMapper extends ParentMapper {

    TrainingDefinition mapToEntity(TrainingDefinitionDTO dto);

    TrainingDefinitionDTO mapToDTO(TrainingDefinition entity);

    TrainingDefinitionExportDTO mapToTrainingDefinitionExportDTO(TrainingDefinition entity);

    TrainingDefinition mapCreateToEntity(TrainingDefinitionCreateDTO dto);

    TrainingDefinition mapUpdateToEntity(TrainingDefinitionUpdateDTO dto);

    List<TrainingDefinition> mapToList(Collection<TrainingDefinitionDTO> dtos);

    List<TrainingDefinitionDTO> mapToListDTO(Collection<TrainingDefinition> entities);

    List<TrainingDefinitionExportDTO> mapToTrainingDefinitionExportDTOList(Collection<TrainingDefinition> entities);

    Set<TrainingDefinition> mapToSet(Collection<TrainingDefinitionDTO> dtos);

    Set<TrainingDefinitionDTO> mapToSetDTO(Collection<TrainingDefinition> entities);

    default Optional<TrainingDefinition> mapToOptional(TrainingDefinitionDTO dto) {
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<TrainingDefinitionDTO> mapToOptional(TrainingDefinition entity) {
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<TrainingDefinitionDTO> mapToPageDTO(Page<TrainingDefinition> objects) {
        List<TrainingDefinitionDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<TrainingDefinition> mapToPage(Page<TrainingDefinitionDTO> objects) {
        List<TrainingDefinition> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<TrainingDefinitionDTO> mapToPageResultResource(Page<TrainingDefinition> objects) {
        List<TrainingDefinitionDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
