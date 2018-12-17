package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.prehook.PreHookDTO;
import cz.muni.ics.kypo.training.persistence.model.PreHook;
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
public interface PreHookMapper extends ParentMapper {

    PreHook mapToEntity(PreHookDTO dto);

    PreHookDTO mapToDTO(PreHook entity);

    List<PreHook> mapToList(Collection<PreHookDTO> dtos);

    List<PreHookDTO> mapToListDTO(Collection<PreHook> entities);

    Set<PreHook> mapToSet(Collection<PreHookDTO> dtos);

    Set<PreHookDTO> mapToSetDTO(Collection<PreHook> entities);

    default Optional<PreHook> mapToOptional(PreHookDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<PreHookDTO> mapToOptional(PreHook entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<PreHookDTO> mapToPageDTO(Page<PreHook> objects){
        List<PreHookDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<PreHook> mapToPage(Page<PreHookDTO> objects){
        List<PreHook> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<PreHookDTO> mapToPageResultResource(Page<PreHook> objects){
        List<PreHookDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }

}
