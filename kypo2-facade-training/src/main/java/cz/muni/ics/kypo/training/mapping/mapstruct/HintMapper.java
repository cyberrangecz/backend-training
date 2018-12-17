package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.persistence.model.Hint;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring")
public interface HintMapper extends ParentMapper {
    Hint mapToEntity(HintDTO dto);

    HintDTO mapToDTO(Hint entity);

    List<Hint> mapToList(Collection<HintDTO> dtos);

    List<HintDTO> mapToListDTO(Collection<Hint> entities);

    Set<Hint> mapToSet(Collection<HintDTO> dtos);

    Set<HintDTO> mapToSetDTO(Collection<Hint> entities);

    default Optional<Hint> mapToOptional(HintDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<HintDTO> mapToOptional(Hint entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<HintDTO> mapToPageDTO(Page<Hint> objects){
        List<HintDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<Hint> mapToPage(Page<HintDTO> objects){
        List<Hint> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<HintDTO> mapToPageResultResource(Page<Hint> objects){
        List<HintDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
