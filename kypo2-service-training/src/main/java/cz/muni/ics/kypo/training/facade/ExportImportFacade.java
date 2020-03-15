package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.archive.*;
import cz.muni.ics.kypo.training.api.dto.export.*;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class ExportImportFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ExportImportFacade.class);
    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;

    private ExportImportService exportImportService;
    private UserService userService;
    private ExportImportMapper exportImportMapper;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private TrainingDefinitionService trainingDefinitionService;
    private TrainingDefinitionMapper trainingDefinitionMapper;
    private ObjectMapper objectMapper;
    private TrainingEventsService trainingEventsService;
    private UserRefMapper userRefMapper;

    @Autowired
    public ExportImportFacade(ExportImportService exportImportService, ExportImportMapper exportImportMapper, GameLevelMapper gameLevelMapper,
                              InfoLevelMapper infoLevelMapper, AssessmentLevelMapper assessmentLevelMapper, TrainingDefinitionService trainingDefinitionService,
                              TrainingDefinitionMapper trainingDefinitionMapper, ObjectMapper objectMapper, TrainingEventsService trainingEventsService,
                              UserService userService, UserRefMapper userRefMapper) {
        this.exportImportService = exportImportService;
        this.exportImportMapper = exportImportMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingDefinitionMapper = trainingDefinitionMapper;
        this.objectMapper = objectMapper;
        this.trainingEventsService = trainingEventsService;
        this.userService = userService;
        this.userRefMapper = userRefMapper;
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
            fileToReturnDTO.setTitle(dbExport.getTitle());
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex);
        }
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

    private List<AbstractLevelArchiveDTO> mapAbstractLevelsToArchiveDTO(Long trainingDefinitionId) {
        List<AbstractLevelArchiveDTO> abstractLevelArchiveDTOs = new ArrayList<>();
        List<AbstractLevel> abstractLevels = trainingDefinitionService.findAllLevelsFromDefinition(trainingDefinitionId);
        abstractLevels.forEach(level -> {
            if (level instanceof GameLevel) {
                GameLevelArchiveDTO gameLevelArchiveDTO = gameLevelMapper.mapToArchiveDTO((GameLevel) level);
                gameLevelArchiveDTO.setLevelType(LevelType.GAME_LEVEL);
                abstractLevelArchiveDTOs.add(gameLevelArchiveDTO);
            } else if (level instanceof InfoLevel) {
                InfoLevelArchiveDTO infoLevelArchiveDTO = infoLevelMapper.mapToArchiveDTO((InfoLevel) level);
                infoLevelArchiveDTO.setLevelType(LevelType.INFO_LEVEL);
                abstractLevelArchiveDTOs.add(infoLevelArchiveDTO);
            } else {
                AssessmentLevelArchiveDTO assessmentLevelArchiveDTO = assessmentLevelMapper.mapToArchiveDTO((AssessmentLevel) level);
                assessmentLevelArchiveDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
                abstractLevelArchiveDTOs.add(assessmentLevelArchiveDTO);
            }
        });
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
        Objects.requireNonNull(importTrainingDefinitionDTO, "In dbImport() method the input parameter for ImportTrainingDefinitionDTO must not be empty.");
        importTrainingDefinitionDTO.setState(TDState.UNRELEASED);
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
            this.checkIfInstanceIsNotFinished(trainingInstance);

            TrainingInstanceArchiveDTO archivedInstance = exportImportMapper.mapToDTO(trainingInstance);
            archivedInstance.setDefinitionId(trainingInstance.getTrainingDefinition().getId());
            Set<Long> organizersRefIds = trainingInstance.getOrganizers().stream()
                    .map(UserRef::getUserRefId)
                    .collect(Collectors.toSet());
            archivedInstance.setOrganizersRefIds(new HashSet<>(organizersRefIds));

            writeTrainingInstanceGeneralInfo(zos, trainingInstance.getId(), archivedInstance);
            writeTrainingInstanceOrganizersInfo(zos, trainingInstance.getId(), organizersRefIds);
            writeTrainingDefinitionInfo(zos, trainingInstance);

            Set<Long> participantRefIds = new HashSet<>();
            writeTrainingRunsInfo(zos, trainingInstance, participantRefIds);
            writeTrainingInstanceParticipantRefIdsInfo(zos, trainingInstance, participantRefIds);

            zos.closeEntry();
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(baos.toByteArray());
            fileToReturnDTO.setTitle(trainingInstance.getTitle());
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex);
        }
    }

    private void writeTrainingInstanceGeneralInfo(ZipOutputStream zos, Long trainingInstanceId, TrainingInstanceArchiveDTO archivedInstance) throws IOException {
        ZipEntry instanceEntry = new ZipEntry("training_instance-id" + trainingInstanceId + ".json");
        zos.putNextEntry(instanceEntry);
        zos.write(objectMapper.writeValueAsBytes(archivedInstance));
    }

    private void writeTrainingRunsInfo(ZipOutputStream zos, TrainingInstance trainingInstance, Set<Long> participantRefIds) throws IOException {
        Set<TrainingRun> runs = exportImportService.findRunsByInstanceId(trainingInstance.getId());
        for (TrainingRun run : runs) {
            TrainingRunArchiveDTO archivedRun = exportImportMapper.mapToArchiveDTO(run);
            archivedRun.setInstanceId(trainingInstance.getId());
            archivedRun.setParticipantRefId(run.getParticipantRef().getUserRefId());
            participantRefIds.add(run.getParticipantRef().getUserRefId());
            ZipEntry runEntry = new ZipEntry("training_run-id" + run.getId() + ".json");
            zos.putNextEntry(runEntry);
            zos.write(objectMapper.writeValueAsBytes(archivedRun));

            List<Map<String, Object>> events = trainingEventsService.findAllEventsFromTrainingRun(trainingInstance.getTrainingDefinition().getId(), trainingInstance.getId(), run.getId());
            ZipEntry eventsEntry = new ZipEntry("training_run-id" + run.getId() + "-events.json");
            zos.putNextEntry(eventsEntry);
            for (Map<String, Object> event : events) {
                zos.write(objectMapper.writer(new MinimalPrettyPrinter()).writeValueAsBytes(event));
                zos.write(System.lineSeparator().getBytes());
            }
        }
    }

    private void writeTrainingDefinitionInfo(ZipOutputStream zos, TrainingInstance trainingInstance) throws IOException {
        TrainingDefinitionArchiveDTO tD = exportImportMapper.mapToArchiveDTO(exportImportService.findById(trainingInstance.getTrainingDefinition().getId()));
        if (tD != null) {
            tD.setLevels(mapAbstractLevelsToArchiveDTO(trainingInstance.getTrainingDefinition().getId()));
            ZipEntry definitionEntry = new ZipEntry("training_definition-id" + trainingInstance.getTrainingDefinition().getId() + ".json");
            zos.putNextEntry(definitionEntry);
            zos.write(objectMapper.writeValueAsBytes(tD));
        }
    }

    private void writeTrainingInstanceOrganizersInfo(ZipOutputStream zos, Long trainingInstanceId, Set<Long> organizersRefIds) throws IOException {
        ZipEntry organizersEntry = new ZipEntry("training_instance-id" + trainingInstanceId + "-organizers" + ".json");
        zos.putNextEntry(organizersEntry);
        zos.write(objectMapper.writeValueAsBytes(getUsersRefExportDTO(organizersRefIds)));
    }

    private void writeTrainingInstanceParticipantRefIdsInfo(ZipOutputStream zos, TrainingInstance trainingInstance, Set<Long> participantRefIds) throws IOException {
        ZipEntry participantsEntry = new ZipEntry("training_instance-id" + trainingInstance.getId() + "-participants" + ".json");
        zos.putNextEntry(participantsEntry);
        zos.write(objectMapper.writeValueAsBytes(getUsersRefExportDTO(participantRefIds)));
    }

    private List<UserRefExportDTO> getUsersRefExportDTO(Set<Long> usersRefIds) {
        PageResultResource<UserRefDTO> usersResponse;
        List<UserRefExportDTO> users = new ArrayList<>();
        int page = 0;
        do {
            usersResponse = userService.getUsersRefDTOByGivenUserIds(usersRefIds, PageRequest.of(page, 999), null, null);
            users.addAll(userRefMapper.mapUserRefExportDTOToUserRefDTO(usersResponse.getContent()));
            page++;

        } while (usersResponse.getPagination().getTotalPages() != usersResponse.getPagination().getNumber());
        return users;
    }

    private void checkIfInstanceIsNotFinished(TrainingInstance trainingInstance) {
        LocalDateTime currentTime = LocalDateTime.now(Clock.systemUTC());
        if (currentTime.isBefore(trainingInstance.getEndTime()))
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(),
                    trainingInstance.getId(), "The training instance is not finished."));
    }

}
