package cz.muni.ics.kypo.training.facade.impl;

import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.export.*;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.facade.ExportImportFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.LevelType;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Seda
 */
@Service
@Transactional
public class ExportImportFacadeImpl implements ExportImportFacade {

    private ExportImportService exportImportService;
    private ExportImportMapper exportImportMapper;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private TrainingDefinitionService trainingDefinitionService;
    private TrainingDefinitionMapper trainingDefinitionMapper;

    @Autowired
    public ExportImportFacadeImpl(ExportImportService exportImportService, ExportImportMapper exportImportMapper, GameLevelMapper gameLevelMapper,
                                  InfoLevelMapper infoLevelMapper, AssessmentLevelMapper assessmentLevelMapper, TrainingDefinitionService trainingDefinitionService,
                                  TrainingDefinitionMapper trainingDefinitionMapper) {
        this.exportImportService = exportImportService;
        this.exportImportMapper = exportImportMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingDefinitionMapper = trainingDefinitionMapper;
    }

    @Override
    @TransactionalRO
    public ExportTrainingDefinitionAndLevelsDTO dbExport(Long trainingDefinitionId) {
        ExportTrainingDefinitionAndLevelsDTO dbExport = exportImportMapper.mapToDTO(exportImportService.findById(trainingDefinitionId));
        if(dbExport != null){
            dbExport.setLevels(mapAbstractLevelToAbstractLevelDTO(dbExport.getStartingLevel()));
        }
        return dbExport;
    }

    private List<AbstractLevelExportDTO> mapAbstractLevelToAbstractLevelDTO(Long levelId) {
        List<AbstractLevelExportDTO> abstractLevelExportDTOs = new ArrayList<>();
        int count = 0;
        while (levelId != null){
            AbstractLevel abstractLevel = trainingDefinitionService.findLevelById(levelId);
            if (abstractLevel instanceof GameLevel) {
                GameLevelExportDTO gameLevelExportDTO = gameLevelMapper.mapToGamelevelExportDTO((GameLevel) abstractLevel);
                gameLevelExportDTO.setLevelType(LevelType.GAME);
                gameLevelExportDTO.setOrder(count);
                abstractLevelExportDTOs.add(gameLevelExportDTO);
            } else if (abstractLevel instanceof InfoLevel) {
                InfoLevelExportDTO infoLevelExportDTO = infoLevelMapper.mapToInfoLevelExportDTO((InfoLevel) abstractLevel);
                infoLevelExportDTO.setLevelType(LevelType.INFO);
                infoLevelExportDTO.setOrder(count);
                abstractLevelExportDTOs.add(infoLevelExportDTO);
            } else {
                AssessmentLevelExportDTO assessmentLevelExportDTO = assessmentLevelMapper.mapToAssessmentLevelExportDTO((AssessmentLevel) abstractLevel);
                assessmentLevelExportDTO.setLevelType(LevelType.ASSESSMENT);
                assessmentLevelExportDTO.setOrder(count);
                abstractLevelExportDTOs.add(assessmentLevelExportDTO);
            }
            count++;
            levelId = abstractLevel.getNextLevel();
        }
        return abstractLevelExportDTOs;
    }

    @Transactional
    @Override
    public TrainingDefinitionDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO) {
        int levelOrder = importTrainingDefinitionDTO.getLevels().size() -1;
        Long newLevelId = null;
        AbstractLevel newLevel;
        while (levelOrder != -1){
            for (AbstractLevelImportDTO level : importTrainingDefinitionDTO.getLevels()){
                if (level.getOrder() == levelOrder) {
                    if (level.getLevelType().equals(LevelType.GAME)) newLevel = gameLevelMapper.mapImportToEntity((GameLevelImportDTO) level);
                    else if (level.getLevelType().equals(LevelType.INFO)) newLevel = infoLevelMapper.mapImportToEntity((InfoLevelImportDTO) level);
                    else newLevel = assessmentLevelMapper.mapImportToEntity((AssessmentLevelImportDTO) level);

                    newLevel.setNextLevel(newLevelId);
                    newLevelId = exportImportService.createLevel(newLevel);
                }
            }
            levelOrder--;
        }
        TrainingDefinition newDefinition = exportImportMapper.mapToEntity(importTrainingDefinitionDTO);
        newDefinition.setStartingLevel(newLevelId);
        return trainingDefinitionMapper.mapToDTO(trainingDefinitionService.create(newDefinition));
    }
}
