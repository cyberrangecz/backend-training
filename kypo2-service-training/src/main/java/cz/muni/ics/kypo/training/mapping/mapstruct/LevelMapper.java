package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.archive.AbstractLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.AssessmentLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.GameLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.export.AssessmentLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.export.GameLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelViewDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.GameLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.AbstractLevelVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.AssessmentLevelVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.GameLevelVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.archive.InfoLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.InfoLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.InfoLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.InfoLevelVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.LevelDefinitionProgressDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * The InfoLevelMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type InfoLevelMapper and
 * DTOs classes. Code is generated during compile time.
 *
 */
@Mapper(componentModel = "spring", uses = {HintMapper.class, AttachmentMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LevelMapper extends ParentMapper {
    // INFO LEVEL
    InfoLevel mapToEntity(InfoLevelDTO dto);

    InfoLevel mapToEntity(InfoLevelUpdateDTO dto);

    BasicLevelInfoDTO mapTo(InfoLevel infoLevel);

    InfoLevel mapImportToEntity(InfoLevelImportDTO dto);

    InfoLevel mapUpdateToEntity(InfoLevelUpdateDTO dto);

    InfoLevelDTO mapToInfoLevelDTO(InfoLevel entity);

    InfoLevelVisualizationDTO mapToVisualizationInfoLevelDTO(InfoLevel entity);

    InfoLevelExportDTO mapToExportInfoLevelDTO(InfoLevel entity);

    InfoLevelArchiveDTO mapToArchiveInfoLevelDTO(InfoLevel entity);

    // ASSESSMENT LEVEL
    AssessmentLevel mapToEntity(AssessmentLevelDTO dto);

    @Mapping(source = "type", target = "assessmentType")
    AssessmentLevel mapUpdateToEntity(AssessmentLevelUpdateDTO dto);

    AssessmentLevel mapImportToEntity(AssessmentLevelImportDTO dto);

    BasicLevelInfoDTO mapTo(AssessmentLevel assessmentLevel);

    AssessmentLevelDTO mapToAssessmentLevelDTO(AssessmentLevel entity);

    AssessmentLevelArchiveDTO mapToArchiveAssessmentLevelDTO(AssessmentLevel entity);

    AssessmentLevelVisualizationDTO mapToVisualizationAssessmentLevelDTO(AssessmentLevel entity);

    AssessmentLevelExportDTO mapToExportAssessmentLevelDTO(AssessmentLevel entity);

    // GAME LEVEL
    GameLevel mapToEntity(GameLevelDTO dto);

    GameLevel mapUpdateToEntity(GameLevelUpdateDTO dto);

    GameLevel mapImportToEntity(GameLevelImportDTO dto);

    BasicLevelInfoDTO mapTo(GameLevel gameLevel);

    GameLevelDTO mapToGameLevelDTO(GameLevel entity);

    GameLevelVisualizationDTO mapToVisualizationGameLevelDTO(GameLevel entity);

    GameLevelExportDTO mapToExportGameLevelDTO(GameLevel entity);

    GameLevelArchiveDTO mapToArchiveGameLevelDTO(GameLevel entity);

    GameLevelViewDTO mapToViewDTO(GameLevel entity);

    // ABSTRACT
    default AbstractLevelDTO mapToDTO(AbstractLevel entity) {
        AbstractLevelDTO abstractLevelDTO;
        if (entity instanceof GameLevel) {
            abstractLevelDTO = mapToGameLevelDTO((GameLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelDTO = mapToInfoLevelDTO((InfoLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelDTO = mapToAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, game or info level.");
        }

        return abstractLevelDTO;
    }

    default AbstractLevelVisualizationDTO mapToVisualizationDTO(AbstractLevel entity) {
        AbstractLevelVisualizationDTO abstractLevelVisualizationDTO;
        if (entity instanceof GameLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationGameLevelDTO((GameLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.GAME_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationInfoLevelDTO((InfoLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, game or info level.");
        }
        return abstractLevelVisualizationDTO;
    }

    default AbstractLevelArchiveDTO mapToArchiveDTO(AbstractLevel entity) {
        AbstractLevelArchiveDTO abstractLevelArchiveDTO;
        if (entity instanceof GameLevel) {
            abstractLevelArchiveDTO = mapToArchiveGameLevelDTO((GameLevel) entity);
            abstractLevelArchiveDTO.setLevelType(LevelType.GAME_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelArchiveDTO = mapToArchiveInfoLevelDTO((InfoLevel) entity);
            abstractLevelArchiveDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelArchiveDTO = mapToArchiveAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelArchiveDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, game or info level.");
        }
        return abstractLevelArchiveDTO;
    }

    default AbstractLevelExportDTO mapToExportDTO(AbstractLevel entity) {
        AbstractLevelExportDTO abstractLevelExportDTO;
        if (entity instanceof GameLevel) {
            abstractLevelExportDTO = mapToExportGameLevelDTO((GameLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.GAME_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelExportDTO = mapToExportInfoLevelDTO((InfoLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelExportDTO = mapToExportAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, game or info level.");
        }
        return abstractLevelExportDTO;
    }

    @Mapping(target = "levelType", constant = "GAME_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(GameLevel entity);
    @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(AssessmentLevel entity);
    @Mapping(target = "levelType", constant = "INFO_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(InfoLevel entity);

    default LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(AbstractLevel entity) {
        if (entity instanceof GameLevel) {
            return mapToLevelDefinitionProgressDTO((GameLevel) entity);
        } else if (entity instanceof InfoLevel) {
            return mapToLevelDefinitionProgressDTO((InfoLevel) entity);
        } else if (entity instanceof AssessmentLevel) {
            return mapToLevelDefinitionProgressDTO((AssessmentLevel) entity);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, game or info level.");
        }
    }
}
