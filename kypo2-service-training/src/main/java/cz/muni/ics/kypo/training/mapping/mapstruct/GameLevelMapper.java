package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.archive.GameLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.GameLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelViewDTO;
import cz.muni.ics.kypo.training.api.dto.imports.GameLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.GameLevelVisualizationDTO;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * The GameLevelMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type GameLevelMapper and
 * DTOs classes. Code is generated during compile time.
 *
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring", uses = {HintMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameLevelMapper extends ParentMapper{
    GameLevel mapToEntity(GameLevelDTO dto);

    GameLevel mapUpdateToEntity(GameLevelUpdateDTO dto);

    GameLevel mapImportToEntity(GameLevelImportDTO dto);

    GameLevelDTO mapToDTO(GameLevel entity);

    GameLevelVisualizationDTO mapToVisualizationGameLevelDTO(GameLevel entity);

    GameLevelExportDTO mapToGamelevelExportDTO(GameLevel entity);

    GameLevelArchiveDTO mapToArchiveDTO(GameLevel entity);

    GameLevelViewDTO mapToViewDTO(GameLevel entity);

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
