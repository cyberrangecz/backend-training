package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.snapshothook.SnapshotHookDTO;
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

    SnapshotHook mapToEntity(SnapshotHookDTO dto);

    SnapshotHookDTO mapToDTO(SnapshotHook entity);

    List<SnapshotHook> mapToList(Collection<SnapshotHookDTO> dtos);

    List<SnapshotHookDTO> mapToListDTO(Collection<SnapshotHook> entities);

    Set<SnapshotHook> mapToSet(Collection<SnapshotHookDTO> dtos);

    Set<SnapshotHookDTO> mapToSetDTO(Collection<SnapshotHook> entities);

    default Optional<SnapshotHook> mapToOptional(SnapshotHookDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<SnapshotHookDTO> mapToOptional(SnapshotHook entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<SnapshotHookDTO> mapToPageDTO(Page<SnapshotHook> objects){
        List<SnapshotHookDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<SnapshotHook> mapToPage(Page<SnapshotHookDTO> objects){
        List<SnapshotHook> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<SnapshotHookDTO> mapToPageResultResource(Page<SnapshotHook> objects){
        List<SnapshotHookDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }

}
