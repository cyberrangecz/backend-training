package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.*;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

/**
 * @author Pavel Seda
 * @author Boris Jadus
 */
@Service
@Transactional
public class ExportImportFacadeImpl implements ExportImportFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ExportImportFacadeImpl.class);
    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;

    private ExportImportService exportImportService;
    private ExportImportMapper exportImportMapper;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private TrainingDefinitionService trainingDefinitionService;
    private TrainingDefinitionMapper trainingDefinitionMapper;
    private ObjectMapper objectMapper;

    @Autowired
    public ExportImportFacadeImpl(ExportImportService exportImportService, ExportImportMapper exportImportMapper, GameLevelMapper gameLevelMapper,
                                  InfoLevelMapper infoLevelMapper, AssessmentLevelMapper assessmentLevelMapper, TrainingDefinitionService trainingDefinitionService,
                                  TrainingDefinitionMapper trainingDefinitionMapper, ObjectMapper objectMapper) {
        this.exportImportService = exportImportService;
        this.exportImportMapper = exportImportMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingDefinitionMapper = trainingDefinitionMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @TransactionalRO
    public FileToReturnDTO dbExport(Long trainingDefinitionId) {
        TrainingDefinition td = exportImportService.findById(trainingDefinitionId);
        ExportTrainingDefinitionAndLevelsDTO dbExport = exportImportMapper.mapToDTO(exportImportService.findById(trainingDefinitionId));
        if (dbExport != null) {
            dbExport.setLevels(mapAbstractLevelToAbstractLevelDTO(trainingDefinitionId));
            dbExport.setEstimatedDuration(calculateEstimatedDuration(dbExport.getLevels()));
        }
        try {
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(objectMapper.writeValueAsBytes(dbExport));
            fileToReturnDTO.setTitle(dbExport.getTitle());
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private int calculateEstimatedDuration(List<AbstractLevelExportDTO> levels) {
        int duration = 0;
        for (AbstractLevelExportDTO level : levels) duration += level.getEstimatedDuration();
        return duration;
    }

    private List<AbstractLevelExportDTO> mapAbstractLevelToAbstractLevelDTO(Long trainingDefinitionId) {
        List<AbstractLevelExportDTO> abstractLevelExportDTOs = new ArrayList<>();
        List<AbstractLevel> abstractLevels = trainingDefinitionService.findAllLevelsFromDefinition(trainingDefinitionId);
        abstractLevels.forEach(level -> {
            if (level instanceof GameLevel) {
                GameLevelExportDTO gameLevelExportDTO = gameLevelMapper.mapToGamelevelExportDTO((GameLevel) level);
                gameLevelExportDTO.setLevelType(LevelType.GAME_LEVEL);
                abstractLevelExportDTOs.add(gameLevelExportDTO);
            } else if (level instanceof InfoLevel) {
                InfoLevelExportDTO infoLevelExportDTO = infoLevelMapper.mapToInfoLevelExportDTO((InfoLevel) level);
                infoLevelExportDTO.setLevelType(LevelType.INFO_LEVEL);
                abstractLevelExportDTOs.add(infoLevelExportDTO);
            } else {
                AssessmentLevelExportDTO assessmentLevelExportDTO = assessmentLevelMapper.mapToAssessmentLevelExportDTO((AssessmentLevel) level);
                assessmentLevelExportDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
                abstractLevelExportDTOs.add(assessmentLevelExportDTO);
            }
        });
        return abstractLevelExportDTOs;
    }

    @Override
    @TransactionalWO
    public TrainingDefinitionByIdDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO) {
        Objects.requireNonNull(importTrainingDefinitionDTO, "In dbImport() method the input parameter for ImportTrainingDefinitionDTO must not be empty.");
        // by default set uploaded training definition to unrelease state
        importTrainingDefinitionDTO.setState(TDState.UNRELEASED);
        // uploaded training definitions have title started with 'Uploaded 'prefix
        if (importTrainingDefinitionDTO.getTitle() != null && !importTrainingDefinitionDTO.getTitle().startsWith("Uploaded")) {
            importTrainingDefinitionDTO.setTitle("Uploaded " + importTrainingDefinitionDTO.getTitle());
        }

        TrainingDefinition newDefinition = exportImportMapper.mapToEntity(importTrainingDefinitionDTO);
        TrainingDefinition newTrainingDefinition = trainingDefinitionService.create(newDefinition);
        List<AbstractLevelImportDTO> levels = importTrainingDefinitionDTO.getLevels();
        levels.forEach(level -> {
            AbstractLevel newLevel;
            if (level.getLevelType().equals(LevelType.GAME_LEVEL))
                newLevel = gameLevelMapper.mapImportToEntity((GameLevelImportDTO) level);
            else if (level.getLevelType().equals(LevelType.INFO_LEVEL))
                newLevel = infoLevelMapper.mapImportToEntity((InfoLevelImportDTO) level);
            else newLevel = assessmentLevelMapper.mapImportToEntity((AssessmentLevelImportDTO) level);
            exportImportService.createLevel(newLevel, newTrainingDefinition);
        });
        return trainingDefinitionMapper.mapToDTOById(newTrainingDefinition);
    }

    @Override
    @TransactionalRO
    public FileToReturnDTO archiveTrainingInstance(Long trainingInstanceId) {
        try {
            TrainingInstance trainingInstance = exportImportService.findInstanceById(trainingInstanceId);
            exportImportService.failIfInstanceIsNotFinished(trainingInstance.getEndTime());
            TrainingInstanceArchiveDTO archivedInstance = exportImportMapper.mapToDTO(trainingInstance);
            if (archivedInstance != null) {
                ExportTrainingDefinitionAndLevelsDTO tD = exportImportMapper.mapToDTO(exportImportService.findById(trainingInstance.getTrainingDefinition().getId()));
                if (tD != null) {
                    tD.setLevels(mapAbstractLevelToAbstractLevelDTO(trainingInstance.getTrainingDefinition().getId()));
                    tD.setEstimatedDuration(calculateEstimatedDuration(tD.getLevels()));
                }
                archivedInstance.setExportTrainingDefinitionAndLevelsDTO(tD);
                Set<TrainingRun> runs = exportImportService.findRunsByInstanceId(trainingInstanceId);
                for (TrainingRun run : runs) {
                    archivedInstance.getTrainingRuns().add(exportImportMapper.mapToDTO(run));
                }
            }
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(objectMapper.writeValueAsBytes(archivedInstance));
            fileToReturnDTO.setTitle(trainingInstance.getTitle());
            return fileToReturnDTO;

        } catch (ServiceLayerException | IOException ex) {
            throw new FacadeLayerException(ex);
        }
    }

}
