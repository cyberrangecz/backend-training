package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
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
public interface GameLevelMapper extends ParentMapper{
    GameLevel mapToEntity(GameLevelDTO dto);

    GameLevel mapUpdateToEntity(GameLevelUpdateDTO dto);

    GameLevelDTO mapToDTO(GameLevel entity);

    List<GameLevel> mapToList(Collection<GameLevelDTO> dtos);

    List<GameLevelDTO> mapToListDTO(Collection<GameLevel> entities);

    Set<GameLevel> mapToSet(Collection<GameLevelDTO> dtos);

    Set<GameLevelDTO> mapToSetDTO(Collection<GameLevel> entities);

    default Optional<GameLevel> mapToOptional(GameLevelDTO dto){
        return Optional.ofNullable(mapToEntity(dto));
    }

    default Optional<GameLevelDTO> mapToOptional(GameLevel entity){
        return Optional.ofNullable(mapToDTO(entity));
    }

    default Page<GameLevelDTO> mapToPageDTO(Page<GameLevel> objects){
        List<GameLevelDTO> mapped = mapToListDTO(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default Page<GameLevel> mapToPage(Page<GameLevelDTO> objects){
        List<GameLevel> mapped = mapToList(objects.getContent());
        return new PageImpl<>(mapped, objects.getPageable(), mapped.size());
    }

    default PageResultResource<GameLevelDTO> mapToPageResultResource(Page<GameLevel> objects){
        List<GameLevelDTO> mapped = new ArrayList<>();
        objects.forEach(object -> mapped.add(mapToDTO(object)));
        return new PageResultResource<>(mapped, createPagination(objects));
    }
}
