package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.viewgroup.TDViewGroupUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.viewgroup.TDViewGroupDTO;
import cz.muni.ics.kypo.training.persistence.model.TDViewGroup;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

@Mapper(componentModel = "spring", uses = {UserRefMapper.class})
public interface TDViewGroupMapper extends ParentMapper {
    TDViewGroup mapToEntity(TDViewGroupDTO dto);

    TDViewGroupDTO mapToDTO(TDViewGroup entity);

    TDViewGroup mapCreateToEntity(TDViewGroupUpdateDTO dto);

    //TDViewGroup mapUpdateToEntity(TDViewGroupUpdateNotDTO dto);

    List<TDViewGroup> mapToList(Collection<TDViewGroupDTO> dtos);

    List<TDViewGroupDTO> mapToListDTO(Collection<TDViewGroup> entities);

    Set<TDViewGroup> mapToSet(Collection<TDViewGroupDTO> dtos);

    Set<TDViewGroupDTO> mapToSetDTO(Collection<TDViewGroup> entities);

    default Optional<TDViewGroup> mapToOptional(TDViewGroupDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<TDViewGroupDTO> mapToOptional(TDViewGroup entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<TDViewGroupDTO> mapToPageDTO(Page<TDViewGroup> objects){
        List<TDViewGroupDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<TDViewGroup> mapToPage(Page<TDViewGroupDTO> objects){
        List<TDViewGroup> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<TDViewGroupDTO> mapToPageResultResource(Page<TDViewGroup> objects){
        List<TDViewGroupDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
