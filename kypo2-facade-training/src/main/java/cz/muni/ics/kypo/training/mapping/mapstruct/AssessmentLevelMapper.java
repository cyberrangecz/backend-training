package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring", uses = {PostHookMapper.class, PreHookMapper.class})
public interface AssessmentLevelMapper extends ParentMapper {

    AssessmentLevel mapToEntity(AssessmentLevelDTO dto);

    @Mapping(source = "type", target = "assessmentType")
    AssessmentLevel mapUpdateToEntity(AssessmentLevelUpdateDTO dto);

    AssessmentLevelDTO mapToDTO(AssessmentLevel entity);

    List<AssessmentLevel> mapToList(Collection<AssessmentLevelDTO> dtos);

    List<AssessmentLevelDTO> mapToListDTO(Collection<AssessmentLevel> entities);

    Set<AssessmentLevel> mapToSet(Collection<AssessmentLevelDTO> dtos);

    Set<AssessmentLevelDTO> mapToSetDTO(Collection<AssessmentLevel> entities);

    default Optional<AssessmentLevel> mapToOptional(AssessmentLevelDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<AssessmentLevelDTO> mapToOptional(AssessmentLevel entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<AssessmentLevelDTO> mapToPageDTO(Page<AssessmentLevel> objects){
        List<AssessmentLevelDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<AssessmentLevel> mapToPage(Page<AssessmentLevelDTO> objects){
        List<AssessmentLevel> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<AssessmentLevelDTO> mapToPageResultResource(Page<AssessmentLevel> objects){
        List<AssessmentLevelDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
