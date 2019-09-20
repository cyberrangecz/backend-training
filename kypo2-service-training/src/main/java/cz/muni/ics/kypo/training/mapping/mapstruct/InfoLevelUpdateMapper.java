package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.RestResponses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * The InfoLevelUpdateMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type InfoLevelUpdateMapper and
 * DTOs classes. Code is generated during compile time.
 *
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InfoLevelUpdateMapper extends ParentMapper{
    InfoLevel mapToEntity(InfoLevelUpdateDTO dto);

    InfoLevelUpdateDTO mapToDTO(InfoLevel entity);

    List<InfoLevel> mapToList(Collection<InfoLevelUpdateDTO> dtos);

    List<InfoLevelUpdateDTO> mapToListDTO(Collection<InfoLevel> entities);

    Set<InfoLevel> mapToSet(Collection<InfoLevelUpdateDTO> dtos);

    Set<InfoLevelUpdateDTO> mapToSetDTO(Collection<InfoLevel> entities);

    default Optional<InfoLevel> mapToOptional(InfoLevelUpdateDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<InfoLevelUpdateDTO> mapToOptional(InfoLevel entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<InfoLevelUpdateDTO> mapToPageDTO(Page<InfoLevel> objects){
        List<InfoLevelUpdateDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<InfoLevel> mapToPage(Page<InfoLevelUpdateDTO> objects){
        List<InfoLevel> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<InfoLevelUpdateDTO> mapToPageResultResource(Page<InfoLevel> objects){
        List<InfoLevelUpdateDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}