package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.prehook.PreHookDTO;
import cz.muni.ics.kypo.training.persistence.model.SnapshotHook;
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
public interface SnapshotHookMapper extends ParentMapper {

    SnapshotHook mapToEntity(PreHookDTO dto);

    PreHookDTO mapToDTO(SnapshotHook entity);

    List<SnapshotHook> mapToList(Collection<PreHookDTO> dtos);

    List<PreHookDTO> mapToListDTO(Collection<SnapshotHook> entities);

    Set<SnapshotHook> mapToSet(Collection<PreHookDTO> dtos);

    Set<PreHookDTO> mapToSetDTO(Collection<SnapshotHook> entities);

    default Optional<SnapshotHook> mapToOptional(PreHookDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<PreHookDTO> mapToOptional(SnapshotHook entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<PreHookDTO> mapToPageDTO(Page<SnapshotHook> objects){
        List<PreHookDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<SnapshotHook> mapToPage(Page<PreHookDTO> objects){
        List<SnapshotHook> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<PreHookDTO> mapToPageResultResource(Page<SnapshotHook> objects){
        List<PreHookDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }

}
