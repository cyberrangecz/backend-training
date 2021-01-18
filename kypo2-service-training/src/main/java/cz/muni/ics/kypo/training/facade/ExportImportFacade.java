package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.events.trainings.LevelStarted;
import cz.muni.csirt.kypo.events.trainings.TrainingRunStarted;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.archive.AbstractLevelArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingDefinitionArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingRunArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.export.AbstractLevelExportDTO;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.api.responses.SandboxDefinitionInfo;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.exceptions.UnprocessableEntityException;
import cz.muni.ics.kypo.training.mapping.mapstruct.ExportImportMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.LevelMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingDefinitionMapper;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.utils.AbstractFileExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The type Export import facade.
 */
@Service
@Transactional
public class ExportImportFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ExportImportFacade.class);

    private ExportImportService exportImportService;
    private TrainingDefinitionService trainingDefinitionService;
    private ExportImportMapper exportImportMapper;
    private LevelMapper levelMapper;
    private TrainingDefinitionMapper trainingDefinitionMapper;
    private ObjectMapper objectMapper;
    private ElasticsearchApiService elasticsearchApiService;

    /**
     * Instantiates a new Export import facade.
     *
     * @param exportImportService       the export import service
     * @param exportImportMapper        the export import mapper
     * @param levelMapper               the level mapper
     * @param trainingDefinitionService the training definition service
     * @param trainingDefinitionMapper  the training definition mapper
     * @param objectMapper              the object mapper
     */
    @Autowired
    public ExportImportFacade(ExportImportService exportImportService,
                              TrainingDefinitionService trainingDefinitionService,
                              ElasticsearchApiService elasticsearchApiService,
                              ExportImportMapper exportImportMapper,
                              LevelMapper levelMapper,
                              TrainingDefinitionMapper trainingDefinitionMapper,
                              ObjectMapper objectMapper) {
        this.exportImportService = exportImportService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.exportImportMapper = exportImportMapper;
        this.levelMapper = levelMapper;
        this.trainingDefinitionMapper = trainingDefinitionMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Exports Training Definition to file
     *
     * @param trainingDefinitionId the id of the definition to be exported
     * @return the file containing definition, {@link FileToReturnDTO}
     */
    @IsDesignerOrOrganizerOrAdmin
    @TransactionalRO
    public FileToReturnDTO dbExport(Long trainingDefinitionId) {
        TrainingDefinition td = exportImportService.findById(trainingDefinitionId);
        ExportTrainingDefinitionAndLevelsDTO dbExport = exportImportMapper.mapToDTO(td);
        if (dbExport != null) {
            dbExport.setLevels(mapAbstractLevelToAbstractLevelDTO(trainingDefinitionId));
        }
        try {
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(objectMapper.writeValueAsBytes(dbExport));
            if(dbExport != null && dbExport.getTitle() != null){
                fileToReturnDTO.setTitle(dbExport.getTitle());
            } else {
                fileToReturnDTO.setTitle("");
            }
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    private List<AbstractLevelExportDTO> mapAbstractLevelToAbstractLevelDTO(Long trainingDefinitionId) {
        List<AbstractLevelExportDTO> abstractLevelExportDTOs = new ArrayList<>();
        List<AbstractLevel> abstractLevels = trainingDefinitionService.findAllLevelsFromDefinition(trainingDefinitionId);
        abstractLevels.forEach(level ->
                abstractLevelExportDTOs.add(levelMapper.mapToExportDTO(level)));
        return abstractLevelExportDTOs;
    }

    private List<AbstractLevelArchiveDTO> mapAbstractLevelsToArchiveDTO(Long trainingDefinitionId) {
        List<AbstractLevelArchiveDTO> abstractLevelArchiveDTOs = new ArrayList<>();
        List<AbstractLevel> abstractLevels = trainingDefinitionService.findAllLevelsFromDefinition(trainingDefinitionId);
        abstractLevels.forEach(level ->
                abstractLevelArchiveDTOs.add(levelMapper.mapToArchiveDTO(level)));
        return abstractLevelArchiveDTOs;
    }

    /**
     * Imports training definition.
     *
     * @param importTrainingDefinitionDTO the training definition to be imported
     * @return the {@link TrainingDefinitionByIdDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalWO
    public TrainingDefinitionByIdDTO dbImport(ImportTrainingDefinitionDTO importTrainingDefinitionDTO) {
        importTrainingDefinitionDTO.setState(TDState.UNRELEASED);
        if (importTrainingDefinitionDTO.getTitle() != null && !importTrainingDefinitionDTO.getTitle().startsWith("Uploaded")) {
            importTrainingDefinitionDTO.setTitle("Uploaded " + importTrainingDefinitionDTO.getTitle());
        }

        TrainingDefinition newDefinition = exportImportMapper.mapToEntity(importTrainingDefinitionDTO);
        TrainingDefinition newTrainingDefinition = trainingDefinitionService.create(newDefinition);
        List<AbstractLevelImportDTO> levels = importTrainingDefinitionDTO.getLevels();
        levels.forEach(level -> {
            AbstractLevel newLevel;
            if (level.getLevelType().equals(LevelType.GAME_LEVEL)) {
                newLevel = levelMapper.mapImportToEntity((GameLevelImportDTO) level);
                checkSumOfHintPenalties((GameLevel) newLevel);
            } else if (level.getLevelType().equals(LevelType.INFO_LEVEL)) {
                newLevel = levelMapper.mapImportToEntity((InfoLevelImportDTO) level);
            } else {
                newLevel = levelMapper.mapImportToEntity((AssessmentLevelImportDTO) level);
            }
            exportImportService.createLevel(newLevel, newTrainingDefinition);
        });
        return trainingDefinitionMapper.mapToDTOById(newTrainingDefinition);
    }

    /**
     * Exports Training Instance to file
     *
     * @param trainingInstanceId the id of the instance to be exported
     * @return the file containing instance, {@link FileToReturnDTO}
     */
    @IsOrganizerOrAdmin
    @TransactionalRO
    public FileToReturnDTO archiveTrainingInstance(Long trainingInstanceId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            TrainingInstance trainingInstance = exportImportService.findInstanceById(trainingInstanceId);

            TrainingInstanceArchiveDTO archivedInstance = exportImportMapper.mapToDTO(trainingInstance);
            archivedInstance.setDefinitionId(trainingInstance.getTrainingDefinition().getId());
            Set<Long> organizersRefIds = trainingInstance.getOrganizers().stream()
                    .map(UserRef::getUserRefId)
                    .collect(Collectors.toSet());
            archivedInstance.setOrganizersRefIds(new HashSet<>(organizersRefIds));

            writeTrainingInstanceGeneralInfo(zos, trainingInstance.getId(), archivedInstance);
            writeTrainingDefinitionInfo(zos, trainingInstance);
            writeTrainingRunsInfo(zos, trainingInstance);
            writeSandboxDefinitionInfo(zos, trainingInstance);

            zos.closeEntry();
            zos.close();
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(baos.toByteArray());
            fileToReturnDTO.setTitle(trainingInstance.getTitle());
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException("The .zip file was not created since there were some processing error.", ex);
        }
    }

    private void writeTrainingInstanceGeneralInfo(ZipOutputStream zos, Long trainingInstanceId, TrainingInstanceArchiveDTO archivedInstance) throws IOException {
        ZipEntry instanceEntry = new ZipEntry("training_instance-id" + trainingInstanceId + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(instanceEntry);
        zos.write(objectMapper.writeValueAsBytes(archivedInstance));
    }

    private void writeTrainingRunsInfo(ZipOutputStream zos, TrainingInstance trainingInstance) throws IOException {
        Set<TrainingRun> runs = exportImportService.findRunsByInstanceId(trainingInstance.getId());
        for (TrainingRun run : runs) {
            TrainingRunArchiveDTO archivedRun = exportImportMapper.mapToArchiveDTO(run);
            archivedRun.setInstanceId(trainingInstance.getId());
            archivedRun.setParticipantRefId(run.getParticipantRef().getUserRefId());
            ZipEntry runEntry = new ZipEntry("training_runs/training_run-id" + run.getId() + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(runEntry);
            zos.write(objectMapper.writeValueAsBytes(archivedRun));

            List<Map<String, Object>> events = elasticsearchApiService.findAllEventsFromTrainingRun(run);
            Map<Integer, Long> levelStartTimestampMapping = writeEventsAndGetLevelStartTimestampMapping(zos, run, events);
            writeEventsByLevels(zos, run, events);

            List<Map<String, Object>> consoleCommands = elasticsearchApiService.findAllConsoleCommandsFromSandbox(run.getSandboxInstanceRefId());
            Integer sandboxId = (Integer) events.get(0).get("sandbox_id");
            writeConsoleCommands(zos, sandboxId, consoleCommands);
            writeConsoleCommandsDetails(zos, sandboxId, levelStartTimestampMapping);
        }
    }

    private Map<Integer, Long> writeEventsAndGetLevelStartTimestampMapping(ZipOutputStream zos, TrainingRun run, List<Map<String, Object>> events) throws IOException {
        ZipEntry eventsEntry = new ZipEntry("training_events/training_run-id" + run.getId() + "-events" + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(eventsEntry);
        //Obtain start timestamp of each level, so it can be used later
        Map<Integer, Long> levelStartTimestampMapping = new LinkedHashMap<>();

        for (Map<String, Object> event : events) {
            zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(event));
            zos.write(System.lineSeparator().getBytes());
            if (event.get("type").equals(LevelStarted.class.getCanonicalName())) {
                levelStartTimestampMapping.put(((Integer) event.get("level")), (Long) event.get("timestamp"));
            }
        }
        return levelStartTimestampMapping;
    }

    private void writeEventsByLevels(ZipOutputStream zos, TrainingRun run, List<Map<String, Object>> events) throws IOException {
        Integer currentLevel = ((Integer) events.get(0).get("level"));
        ZipEntry eventsDetailEntry = new ZipEntry("training_events/training_run-id" + run.getId() + "-details" + "/level" + currentLevel + "-events" + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(eventsDetailEntry);
        for (Map<String, Object> event : events) {
            if (!event.get("level").equals(currentLevel)) {
                currentLevel = ((Integer) event.get("level"));
                eventsDetailEntry = new ZipEntry("training_events/training_run-id" + run.getId() + "-details" + "/level" + currentLevel + "-events" + AbstractFileExtensions.JSON_FILE_EXTENSION);
                zos.putNextEntry(eventsDetailEntry);
            }
            zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(event));
            zos.write(System.lineSeparator().getBytes());
        }
    }

    private void writeConsoleCommands(ZipOutputStream zos, Integer sandboxId, List<Map<String, Object>> consoleCommands) throws IOException {
        ZipEntry consoleCommandsEntry = new ZipEntry("command_histories/sandbox-" + sandboxId + "-useractions" + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(consoleCommandsEntry);
        for (Map<String, Object> command : consoleCommands) {
            zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(command));
            zos.write(System.lineSeparator().getBytes());
        }
    }

    private void writeConsoleCommandsDetails(ZipOutputStream zos, Integer sandboxId, Map<Integer, Long> levelStartTimestampMapping) throws IOException {
        List<Long> levelTimestampRanges = new ArrayList<>(levelStartTimestampMapping.values());
        List<Integer> levelIds = new ArrayList<>(levelStartTimestampMapping.keySet());
        levelTimestampRanges.add(Long.MAX_VALUE);

        for (int i = 0; i < levelIds.size(); i++) {
            List<Map<String, Object>> consoleCommandsByLevel = elasticsearchApiService.findAllConsoleCommandsFromSandboxAndTimeRange(sandboxId, levelTimestampRanges.get(i), levelTimestampRanges.get(i+1));
            ZipEntry consoleCommandsEntryDetails = new ZipEntry("command_histories/sandbox-" + sandboxId + "-details" + "/level" + levelIds.get(i)+ "-useractions" + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(consoleCommandsEntryDetails);
            for (Map<String, Object> command : consoleCommandsByLevel) {
                zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(command));
                zos.write(System.lineSeparator().getBytes());
            }
        }
    }

    private void writeTrainingDefinitionInfo(ZipOutputStream zos, TrainingInstance trainingInstance) throws IOException {
        TrainingDefinitionArchiveDTO tD = exportImportMapper.mapToArchiveDTO(exportImportService.findById(trainingInstance.getTrainingDefinition().getId()));
        if (tD != null) {
            tD.setLevels(mapAbstractLevelsToArchiveDTO(trainingInstance.getTrainingDefinition().getId()));
            ZipEntry definitionEntry = new ZipEntry("training_definition-id" + trainingInstance.getTrainingDefinition().getId() + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(definitionEntry);
            zos.write(objectMapper.writeValueAsBytes(tD));
        }
    }

    private void writeSandboxDefinitionInfo(ZipOutputStream zos, TrainingInstance trainingInstance) throws IOException {
        if (trainingInstance.getPoolId() != null) {
            SandboxDefinitionInfo sandboxDefinitionInfo = exportImportService.getSandboxDefinitionId(trainingInstance.getPoolId());
            ZipEntry sandboxDefinitionEntry = new ZipEntry("sandbox_definition-id" + sandboxDefinitionInfo.getId() + AbstractFileExtensions.JSON_FILE_EXTENSION);
            zos.putNextEntry(sandboxDefinitionEntry);
            zos.write(objectMapper.writeValueAsBytes(sandboxDefinitionInfo));
        }
    }

    private void checkSumOfHintPenalties(GameLevel gameLevel) {
        int sumHintPenalties = 0;
        for (Hint hint : gameLevel.getHints()) {
            sumHintPenalties += hint.getHintPenalty();
        }
        if(sumHintPenalties > gameLevel.getMaxScore()) {
            throw new UnprocessableEntityException(new EntityErrorDetail(GameLevel.class, "title", String.class, gameLevel.getTitle(),
                    "Sum of hints penalties cannot be greater than maximal score of the game level."));     }
    }


}
