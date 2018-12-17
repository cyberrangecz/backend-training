package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.persistence.model.SandboxDefinitionRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring",
        uses = {SandboxDefinitionRefMapper.class, AuthorRefMapper.class},
        nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public interface TrainingDefinitionMapper extends ParentMapper{

    @Mapping(source = "sandboxDefinitionRef", target = "sandBoxDefinitionRef")
    TrainingDefinition mapToEntity(TrainingDefinitionDTO dto);

    @Mapping(source = "sandBoxDefinitionRef", target = "sandboxDefinitionRef")
    TrainingDefinitionDTO mapToDTO(TrainingDefinition entity);

    @Mapping(source = "sandboxDefinitionRef", target = "sandBoxDefinitionRef")
    TrainingDefinition mapCreateToEntity(TrainingDefinitionCreateDTO dto);

    @Mapping(source = "sandboxDefinitionRef", target = "sandBoxDefinitionRef")
    TrainingDefinition mapUpdateToEntity(TrainingDefinitionUpdateDTO dto);

    default SandboxDefinitionRef mapLongToSandboxDefRef(Long ref){
        SandboxDefinitionRef dr = new SandboxDefinitionRef();
        dr.setSandboxDefinitionRef(ref);
        return dr;
    }

    List<TrainingDefinition> mapToList(Collection<TrainingDefinitionDTO> dtos);

    List<TrainingDefinitionDTO> mapToListDTO(Collection<TrainingDefinition> entities);

    Set<TrainingDefinition> mapToSet(Collection<TrainingDefinitionDTO> dtos);

    Set<TrainingDefinitionDTO> mapToSetDTO(Collection<TrainingDefinition> entities);

    default Optional<TrainingDefinition> mapToOptional(TrainingDefinitionDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<TrainingDefinitionDTO> mapToOptional(TrainingDefinition entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<TrainingDefinitionDTO> mapToPageDTO(Page<TrainingDefinition> objects){
        List<TrainingDefinitionDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<TrainingDefinition> mapToPage(Page<TrainingDefinitionDTO> objects){
        List<TrainingDefinition> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<TrainingDefinitionDTO> mapToPageResultResource(Page<TrainingDefinition> objects){
        List<TrainingDefinitionDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
