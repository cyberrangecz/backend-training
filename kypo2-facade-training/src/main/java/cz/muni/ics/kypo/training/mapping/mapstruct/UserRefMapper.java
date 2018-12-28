package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.OrganizerRefDTO;
import cz.muni.ics.kypo.training.persistence.model.OrganizerRef;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring", uses = {TrainingInstanceMapper.class})
public interface UserRefMapper extends ParentMapper{
    OrganizerRef mapToEntity(OrganizerRefDTO dto);

    OrganizerRefDTO mapToDTO(OrganizerRef entity);

    List<OrganizerRef> mapToList(Collection<OrganizerRefDTO> dtos);

    List<OrganizerRefDTO> mapToListDTO(Collection<OrganizerRef> entities);

    Set<OrganizerRef> mapToSet(Collection<OrganizerRefDTO> dtos);

    Set<OrganizerRefDTO> mapToSetDTO(Collection<OrganizerRef> entities);

    default Optional<OrganizerRef> mapToOptional(OrganizerRefDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<OrganizerRefDTO> mapToOptional(OrganizerRef entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<OrganizerRefDTO> mapToPageDTO(Page<OrganizerRef> objects){
        List<OrganizerRefDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<OrganizerRef> mapToPage(Page<OrganizerRefDTO> objects){
        List<OrganizerRef> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<OrganizerRefDTO> mapToPageResultResource(Page<OrganizerRef> objects){
        List<OrganizerRefDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
