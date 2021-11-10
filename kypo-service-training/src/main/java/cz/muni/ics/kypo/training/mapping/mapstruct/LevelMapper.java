package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.archive.AbstractLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.AssessmentLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.export.AssessmentLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.export.TrainingLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelViewDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.TrainingLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.AbstractLevelVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.AssessmentLevelVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.TrainingLevelVisualizationDTO;
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
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * The InfoLevelMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type InfoLevelMapper and
 * DTOs classes. Code is generated during compile time.
 *
 */
@Mapper(componentModel = "spring", uses = {
        HintMapper.class, AttachmentMapper.class,
        QuestionMapper.class, ReferenceSolutionNodeMapper.class
        },
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LevelMapper extends ParentMapper {
    // INFO LEVEL
    InfoLevel mapToEntity(InfoLevelDTO dto);

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

    @Mapping(source = "questions", target = "questions", qualifiedByName = "questionWithoutId")
    AssessmentLevelExportDTO mapToExportAssessmentLevelDTO(AssessmentLevel entity);

    // TRAINING LEVEL
    TrainingLevel mapToEntity(TrainingLevelDTO dto);

    @Mapping(target = "answer", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(dto.getAnswer()) ? null : dto.getAnswer())")
    @Mapping(target = "answerVariableName", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(dto.getAnswerVariableName()) ? null : dto.getAnswerVariableName())")
    TrainingLevel mapUpdateToEntity(TrainingLevelUpdateDTO dto);

    TrainingLevel mapImportToEntity(TrainingLevelImportDTO dto);

    BasicLevelInfoDTO mapTo(TrainingLevel trainingLevel);

    TrainingLevelDTO mapToTrainingLevelDTO(TrainingLevel entity);

    TrainingLevelVisualizationDTO mapToVisualizationTrainingLevelDTO(TrainingLevel entity);

    TrainingLevelExportDTO mapToExportTrainingLevelDTO(TrainingLevel entity);

    TrainingLevelArchiveDTO mapToArchiveTrainingLevelDTO(TrainingLevel entity);

    TrainingLevelViewDTO mapToViewDTO(TrainingLevel entity);

    // ABSTRACT

    List<AbstractLevel> mapToLevels(List<AbstractLevelUpdateDTO> dtos);

    default AbstractLevel mapToAbstractLevel(AbstractLevelUpdateDTO dto) {
        if (dto.getLevelType() == LevelType.TRAINING_LEVEL) {
            return mapUpdateToEntity((TrainingLevelUpdateDTO) dto);
        } else if (dto.getLevelType() == LevelType.INFO_LEVEL) {
            return mapUpdateToEntity((InfoLevelUpdateDTO) dto);
        } else if (dto.getLevelType() == LevelType.ASSESSMENT_LEVEL) {
            return mapUpdateToEntity((AssessmentLevelUpdateDTO) dto);
        } else {
            throw new InternalServerErrorException("Level with id: " + dto.getId() + " and with title: " + dto.getTitle() +
                    " is not instance of assessment, training or info level.");
        }
    }

    default AbstractLevelDTO mapToDTO(AbstractLevel entity) {
        AbstractLevelDTO abstractLevelDTO;
        if (entity instanceof TrainingLevel) {
            abstractLevelDTO = mapToTrainingLevelDTO((TrainingLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.TRAINING_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelDTO = mapToInfoLevelDTO((InfoLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelDTO = mapToAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }

        return abstractLevelDTO;
    }

    default AbstractLevelVisualizationDTO mapToVisualizationDTO(AbstractLevel entity) {
        AbstractLevelVisualizationDTO abstractLevelVisualizationDTO;
        if (entity instanceof TrainingLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationTrainingLevelDTO((TrainingLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.TRAINING_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationInfoLevelDTO((InfoLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }
        return abstractLevelVisualizationDTO;
    }

    default AbstractLevelArchiveDTO mapToArchiveDTO(AbstractLevel entity) {
        AbstractLevelArchiveDTO abstractLevelArchiveDTO;
        if (entity instanceof TrainingLevel) {
            abstractLevelArchiveDTO = mapToArchiveTrainingLevelDTO((TrainingLevel) entity);
            abstractLevelArchiveDTO.setLevelType(LevelType.TRAINING_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelArchiveDTO = mapToArchiveInfoLevelDTO((InfoLevel) entity);
            abstractLevelArchiveDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelArchiveDTO = mapToArchiveAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelArchiveDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }
        return abstractLevelArchiveDTO;
    }

    default AbstractLevelExportDTO mapToExportDTO(AbstractLevel entity) {
        AbstractLevelExportDTO abstractLevelExportDTO;
        if (entity instanceof TrainingLevel) {
            abstractLevelExportDTO = mapToExportTrainingLevelDTO((TrainingLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.TRAINING_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelExportDTO = mapToExportInfoLevelDTO((InfoLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelExportDTO = mapToExportAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }
        return abstractLevelExportDTO;
    }

    @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(TrainingLevel entity);
    @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(AssessmentLevel entity);
    @Mapping(target = "levelType", constant = "INFO_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(InfoLevel entity);

    default LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(AbstractLevel entity) {
        if (entity instanceof TrainingLevel) {
            return mapToLevelDefinitionProgressDTO((TrainingLevel) entity);
        } else if (entity instanceof InfoLevel) {
            return mapToLevelDefinitionProgressDTO((InfoLevel) entity);
        } else if (entity instanceof AssessmentLevel) {
            return mapToLevelDefinitionProgressDTO((AssessmentLevel) entity);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }
    }
}
