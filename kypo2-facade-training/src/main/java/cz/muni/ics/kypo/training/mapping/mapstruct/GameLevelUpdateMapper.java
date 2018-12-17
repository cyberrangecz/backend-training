package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring", uses = {PostHookMapper.class, PreHookMapper.class, HintMapper.class})
public interface GameLevelUpdateMapper extends ParentMapper{
    GameLevel mapToEntity(GameLevelUpdateDTO dto);

    GameLevelUpdateDTO mapToDTO(GameLevel entity);

    List<GameLevel> mapToList(Collection<GameLevelUpdateDTO> dtos);

    List<GameLevelUpdateDTO> mapToListDTO(Collection<GameLevel> entities);

    Set<GameLevel> mapToSet(Collection<GameLevelUpdateDTO> dtos);

    Set<GameLevelUpdateDTO> mapToSetDTO(Collection<GameLevel> entities);

    default Optional<GameLevel> mapToOptional(GameLevelUpdateDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<GameLevelUpdateDTO> mapToOptional(GameLevel entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<GameLevelUpdateDTO> mapToPageDTO(Page<GameLevel> objects){
        List<GameLevelUpdateDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<GameLevel> mapToPage(Page<GameLevelUpdateDTO> objects){
        List<GameLevel> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<GameLevelUpdateDTO> mapToPageResultResource(Page<GameLevel> objects){
        List<GameLevelUpdateDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}