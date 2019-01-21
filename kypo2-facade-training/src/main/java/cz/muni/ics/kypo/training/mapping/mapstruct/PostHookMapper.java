package cz.muni.ics.kypo.training.mapping.mapstruct;


import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.posthook.PostHookDTO;
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
public interface PostHookMapper extends ParentMapper {

    PostHook mapToEntity(PostHookDTO dto);

    PostHookDTO mapToDTO(PostHook entity);

    List<PostHook> mapToList(Collection<PostHookDTO> dtos);

    List<PostHookDTO> mapToListDTO(Collection<PostHook> entities);

    Set<PostHook> mapToSet(Collection<PostHookDTO> dtos);

    Set<PostHookDTO> mapToSetDTO(Collection<PostHook> entities);

    default Optional<PostHook> mapToOptional(PostHookDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<PostHookDTO> mapToOptional(PostHook entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<PostHookDTO> mapToPageDTO(Page<PostHook> objects){
        List<PostHookDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<PostHook> mapToPage(Page<PostHookDTO> objects){
        List<PostHook> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<PostHookDTO> mapToPageResultResource(Page<PostHook> objects){
        List<PostHookDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
