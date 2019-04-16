package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.*;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
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
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    public FileToReturnDTO dbExport(Long trainingDefinitionId) {
        ExportTrainingDefinitionAndLevelsDTO dbExport = exportImportMapper.mapToDTO(exportImportService.findById(trainingDefinitionId));
        if (dbExport != null) {
            dbExport.setLevels(mapAbstractLevelToAbstractLevelDTO(dbExport.getStartingLevel()));
        }
        try {
            File fileToReturn = File.createTempFile(dbExport.getTitle() + System.currentTimeMillis(), ".json");
            Files.write(Paths.get(fileToReturn.getName()), convertObjectToJsonBytes(dbExport).getBytes());
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(Files.readAllBytes(Paths.get(fileToReturn.getName())));
            fileToReturnDTO.setTitle(dbExport.getTitle());
            fileToReturn.deleteOnExit();
            return fileToReturnDTO;
        } catch (IOException ex){
            throw new FacadeLayerException(ex);
        }
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
        Objects.requireNonNull(importTrainingDefinitionDTO, "In dbImport() method the input parameter for ImportTrainingDefinitionDTO must not be empty.");
        // by default set uploaded training definition to unrelease state
        importTrainingDefinitionDTO.setState(TDState.UNRELEASED);
        // uploaded training definitions have title started with 'Uploaded 'prefix
        if (importTrainingDefinitionDTO.getTitle() != null && !importTrainingDefinitionDTO.getTitle().startsWith("Uploaded")) {
            importTrainingDefinitionDTO.setTitle("Uploaded " + importTrainingDefinitionDTO.getTitle());
        }
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
    @TransactionalRO
    public FileToReturnDTO archiveTrainingInstance(Long trainingInstanceId) {
        try {
            TrainingInstance trainingInstance = exportImportService.findInstanceById(trainingInstanceId);
            exportImportService.failIfInstanceIsNotFinished(trainingInstance.getEndTime());
            TrainingInstanceArchiveDTO archivedInstance = exportImportMapper.mapToDTO(trainingInstance);
            if (archivedInstance != null) {
                ExportTrainingDefinitionAndLevelsDTO tD = exportImportMapper.mapToDTO(exportImportService.findById(trainingInstance.getTrainingDefinition().getId()));
                if (tD != null) {
                    tD.setLevels(mapAbstractLevelToAbstractLevelDTO(tD.getStartingLevel()));
                }
                archivedInstance.setExportTrainingDefinitionAndLevelsDTO(tD);
                Set<TrainingRun> runs = exportImportService.findRunsByInstanceId(trainingInstanceId);
                for (TrainingRun run : runs) {
                    archivedInstance.getTrainingRuns().add(exportImportMapper.mapToDTO(run));
                }
            }

            File fileToReturn = File.createTempFile(archivedInstance.getTitle() + System.currentTimeMillis() , ".json");
            Files.write(Paths.get(fileToReturn.getName()), convertObjectToJsonBytes(archivedInstance).getBytes());
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(Files.readAllBytes(Paths.get(fileToReturn.getName())));
            fileToReturnDTO.setTitle(trainingInstance.getTitle());
            fileToReturn.deleteOnExit();
            return fileToReturnDTO;

        } catch (ServiceLayerException | IOException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return writer.writeValueAsString(object);
    }
}
