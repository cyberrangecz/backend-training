package cz.muni.ics.kypo.training.facade.impl;

import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.ExportTrainingDefinitionsAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.facade.ExportImportFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.LevelType;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel Seda
 */
@Service
@Transactional
public class ExportImportFacadeImpl implements ExportImportFacade {

    private ExportImportService exportImportService;
    private TrainingDefinitionService trainingDefinitionService;
    private ExportImportMapper exportImportMapper;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;

    @Autowired
    public ExportImportFacadeImpl(ExportImportService exportImportService, TrainingDefinitionService trainingDefinitionService, ExportImportMapper exportImportMapper, GameLevelMapper gameLevelMapper,
                                  InfoLevelMapper infoLevelMapper, AssessmentLevelMapper assessmentLevelMapper) {
        this.exportImportService = exportImportService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.exportImportMapper = exportImportMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
    }

    @Override
    @TransactionalRO
    public List<ExportTrainingDefinitionsAndLevelsDTO> dbExport() {
        List<ExportTrainingDefinitionsAndLevelsDTO> exportTrainingDefinitionsAndLevelsDTOS = new ArrayList<>();
        List<TrainingDefinition> trainingDefinitions = exportImportService.findAll();

        trainingDefinitions.forEach(td -> {
            ExportTrainingDefinitionsAndLevelsDTO trainingDefinitionWithLevels = exportImportMapper.mapToExportDTO(td);
            trainingDefinitionWithLevels.setLevels(gatherLevels(td.getId()));
            exportTrainingDefinitionsAndLevelsDTOS.add(trainingDefinitionWithLevels);
        });
        return exportTrainingDefinitionsAndLevelsDTOS;
    }

    private boolean isAdmin() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getUserAuthentication().getAuthorities()) {
            if (gA.getAuthority().equals("ADMINISTRATOR")) return true;
        }
        return false;
    }


    private Set<AbstractLevelDTO> gatherLevels(Long definitionId) {
        List<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(definitionId);
        Set<AbstractLevelDTO> levelDTOS = new HashSet<>();

        for (AbstractLevel l : levels) {
            if (l instanceof GameLevel) {
                GameLevelDTO lDTO = gameLevelMapper.mapToDTO((GameLevel) l);
                levelDTOS.add(lDTO);
            } else if (l instanceof InfoLevel) {
                InfoLevelDTO lDTO = infoLevelMapper.mapToDTO((InfoLevel) l);
                levelDTOS.add(lDTO);
            } else {
                AssessmentLevelDTO lDTO = assessmentLevelMapper.mapToDTO((AssessmentLevel) l);
                levelDTOS.add(lDTO);
            }
        }
        return levelDTOS;
    }

}
