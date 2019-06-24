package cz.muni.ics.kypo.training.mapping.mapstruct;



import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.SandboxInstanceRefDTO;
import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * The SandboxInstanceRefMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type SandboxInstanceRefMapper and
 * DTOs classes. Code is generated during compile time.
 *
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring")
public interface SandboxInstanceRefMapper extends ParentMapper {
    SandboxInstanceRef mapToEntity(SandboxInstanceRefDTO dto);

    SandboxInstanceRefDTO mapToDTO(SandboxInstanceRef entity);

    List<SandboxInstanceRef> mapToList(Collection<SandboxInstanceRefDTO> dtos);

    List<SandboxInstanceRefDTO> mapToListDTO(Collection<SandboxInstanceRef> entities);

    Set<SandboxInstanceRef> mapToSet(Collection<SandboxInstanceRefDTO> dtos);

    Set<SandboxInstanceRefDTO> mapToSetDTO(Collection<SandboxInstanceRef> entities);

    default Optional<SandboxInstanceRef> mapToOptional(SandboxInstanceRefDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<SandboxInstanceRefDTO> mapToOptional(SandboxInstanceRef entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<SandboxInstanceRefDTO> mapToPageDTO(Page<SandboxInstanceRef> objects){
        List<SandboxInstanceRefDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<SandboxInstanceRef> mapToPage(Page<SandboxInstanceRefDTO> objects){
        List<SandboxInstanceRef> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<SandboxInstanceRefDTO> mapToPageResultResource(Page<SandboxInstanceRef> objects){
        List<SandboxInstanceRefDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
