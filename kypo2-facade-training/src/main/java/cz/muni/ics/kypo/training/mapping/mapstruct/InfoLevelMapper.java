package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring",
        uses = {PreHookMapper.class, PostHookMapper.class, SandboxInstanceRefMapper.class},
        nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public interface InfoLevelMapper extends ParentMapper{
    InfoLevel mapToEntity(InfoLevelDTO dto);

    InfoLevel mapUpdateToEntity(InfoLevelUpdateDTO dto);

    InfoLevelDTO mapToDTO(InfoLevel entity);

    List<InfoLevel> mapToList(Collection<InfoLevelDTO> dtos);

    List<InfoLevelDTO> mapToListDTO(Collection<InfoLevel> entities);

    Set<InfoLevel> mapToSet(Collection<InfoLevelDTO> dtos);

    Set<InfoLevelDTO> mapToSetDTO(Collection<InfoLevel> entities);

    default Optional<InfoLevel> mapToOptional(InfoLevelDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<InfoLevelDTO> mapToOptional(InfoLevel entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<InfoLevelDTO> mapToPageDTO(Page<InfoLevel> objects){
        List<InfoLevelDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<InfoLevel> mapToPage(Page<InfoLevelDTO> objects){
        List<InfoLevel> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<InfoLevelDTO> mapToPageResultResource(Page<InfoLevel> objects){
        List<InfoLevelDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
