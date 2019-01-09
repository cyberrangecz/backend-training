package cz.muni.ics.kypo.training.facade.impl;

import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.export.*;
import cz.muni.ics.kypo.training.facade.ExportImportFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.LevelType;
import cz.muni.ics.kypo.training.service.ExportImportService;
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
    private TrainingDefinitionMapper trainingDefinitionMapper;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;

    @Autowired
    public ExportImportFacadeImpl(ExportImportService exportImportService, TrainingDefinitionMapper trainingDefinitionMapper, GameLevelMapper gameLevelMapper,
                                  InfoLevelMapper infoLevelMapper, AssessmentLevelMapper assessmentLevelMapper) {
        this.exportImportService = exportImportService;
        this.trainingDefinitionMapper = trainingDefinitionMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
    }

    @Override
    @TransactionalRO
    public ExportTrainingDefinitionsAndLevelsDTO dbExport() {
        ExportTrainingDefinitionsAndLevelsDTO dbExport = new ExportTrainingDefinitionsAndLevelsDTO();
        dbExport.setLevels(mapAbstractLevelToAbstractLevelDTO(exportImportService.findAllLevels()));
        dbExport.setTrainingDefinitions(trainingDefinitionMapper.mapToTrainingDefinitionExportDTOList(exportImportService.findAllTrainingDefinitions()));
        return dbExport;
    }

    private List<AbstractLevelExportDTO> mapAbstractLevelToAbstractLevelDTO(List<AbstractLevel> levels) {
        List<AbstractLevelExportDTO> abstractLevelExportDTOs = new ArrayList<>();

        for (AbstractLevel abstractLevel : levels) {
            if (abstractLevel instanceof GameLevel) {
                GameLevelExportDTO gameLevelExportDTO = gameLevelMapper.mapToGamelevelExportDTO((GameLevel) abstractLevel);
                gameLevelExportDTO.setLevelType(LevelType.GAME);
                abstractLevelExportDTOs.add(gameLevelExportDTO);
            } else if (abstractLevel instanceof InfoLevel) {
                InfoLevelExportDTO infoLevelExportDTO = infoLevelMapper.mapToInfoLevelExportDTO((InfoLevel) abstractLevel);
                infoLevelExportDTO.setLevelType(LevelType.INFO);
                abstractLevelExportDTOs.add(infoLevelExportDTO);
            } else {
                AssessmentLevelExportDTO assessmentLevelExportDTO = assessmentLevelMapper.mapToAssessmentLevelExportDTO((AssessmentLevel) abstractLevel);
                assessmentLevelExportDTO.setLevelType(LevelType.ASSESSMENT);
                abstractLevelExportDTOs.add(assessmentLevelExportDTO);
            }
        }
        return abstractLevelExportDTOs;
    }

}
