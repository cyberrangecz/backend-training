package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.SandboxDefinitionRefDTO;
import cz.muni.ics.kypo.training.persistence.model.SandboxDefinitionRef;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring",
        nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public interface SandboxDefinitionRefMapper extends ParentMapper{
    SandboxDefinitionRef mapToEntity(SandboxDefinitionRefDTO dto);

    SandboxDefinitionRefDTO mapToDTO(SandboxDefinitionRef entity);

    List<SandboxDefinitionRef> mapToList(Collection<SandboxDefinitionRefDTO> dtos);

    List<SandboxDefinitionRefDTO> mapToListDTO(Collection<SandboxDefinitionRef> entities);

    Set<SandboxDefinitionRef> mapToSet(Collection<SandboxDefinitionRefDTO> dtos);

    Set<SandboxDefinitionRefDTO> mapToSetDTO(Collection<SandboxDefinitionRef> entities);

    default Optional<SandboxDefinitionRef> mapToOptional(SandboxDefinitionRefDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<SandboxDefinitionRefDTO> mapToOptional(SandboxDefinitionRef entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<SandboxDefinitionRefDTO> mapToPageDTO(Page<SandboxDefinitionRef> objects){
        List<SandboxDefinitionRefDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<SandboxDefinitionRef> mapToPage(Page<SandboxDefinitionRefDTO> objects){
        List<SandboxDefinitionRef> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<SandboxDefinitionRefDTO> mapToPageResultResource(Page<SandboxDefinitionRef> objects){
        List<SandboxDefinitionRefDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
