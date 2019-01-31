package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.export.*;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.sandboxdefinition.SandboxDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.impl.TrainingInstanceServiceImpl;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Pavel Seda
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

    @Autowired
    private RestTemplate restTemplate;

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
        if (dbExport != null) {
            dbExport.setLevels(mapAbstractLevelToAbstractLevelDTO(dbExport.getStartingLevel()));
        }
        return dbExport;
    }

    private List<AbstractLevelExportDTO> mapAbstractLevelToAbstractLevelDTO(Long levelId) {
        List<AbstractLevelExportDTO> abstractLevelExportDTOs = new ArrayList<>();
        int count = 0;
        while (levelId != null) {
            AbstractLevel abstractLevel = trainingDefinitionService.findLevelById(levelId);
            if (abstractLevel instanceof GameLevel) {
                GameLevelExportDTO gameLevelExportDTO = gameLevelMapper.mapToGamelevelExportDTO((GameLevel) abstractLevel);
                gameLevelExportDTO.setLevelType(LevelType.GAME_LEVEL);
                gameLevelExportDTO.setOrder(count);
                abstractLevelExportDTOs.add(gameLevelExportDTO);
            } else if (abstractLevel instanceof InfoLevel) {
                InfoLevelExportDTO infoLevelExportDTO = infoLevelMapper.mapToInfoLevelExportDTO((InfoLevel) abstractLevel);
                infoLevelExportDTO.setLevelType(LevelType.INFO_LEVEL);
                infoLevelExportDTO.setOrder(count);
                abstractLevelExportDTOs.add(infoLevelExportDTO);
            } else {
                AssessmentLevelExportDTO assessmentLevelExportDTO = assessmentLevelMapper.mapToAssessmentLevelExportDTO((AssessmentLevel) abstractLevel);
                assessmentLevelExportDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
                assessmentLevelExportDTO.setOrder(count);
                abstractLevelExportDTOs.add(assessmentLevelExportDTO);
            }
            count++;
            levelId = abstractLevel.getNextLevel();
        }
        return abstractLevelExportDTOs;
    }

    @Override
    @TransactionalWO
    public TrainingDefinitionDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO) {
        int levelOrder = importTrainingDefinitionDTO.getLevels().size() - 1;
        Long newLevelId = null;
        AbstractLevel newLevel;
        while (levelOrder != -1) {
            for (AbstractLevelImportDTO level : importTrainingDefinitionDTO.getLevels()) {
                if (level.getOrder() == levelOrder) {
                    if (level.getLevelType().equals(LevelType.GAME_LEVEL))
                        newLevel = gameLevelMapper.mapImportToEntity((GameLevelImportDTO) level);
                    else if (level.getLevelType().equals(LevelType.INFO_LEVEL))
                        newLevel = infoLevelMapper.mapImportToEntity((InfoLevelImportDTO) level);
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

    @Override
    public ResponseEntity<SandboxDefinitionCreateDTO> createSandboxDefinitionInOpenStack(SandboxDefinitionCreateDTO sandboxDefinitionCreateDTO) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.set("Content-Type", "application/yaml");
//        return restTemplate.exchange(kypoOpenStackURI + "/definitions/",
//                HttpMethod.POST,
//                new HttpEntity<>(sandboxDefinitionCreateDTO, httpHeaders),
//                new ParameterizedTypeReference<SandboxDefinitionCreateDTO>() {
//                });

        sandboxDefinitionCreateDTO.toString();
        return null;
    }
}
