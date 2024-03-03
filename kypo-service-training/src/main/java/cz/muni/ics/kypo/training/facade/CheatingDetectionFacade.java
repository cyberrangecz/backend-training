package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.cheatingdetection.*;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.detection.*;
import cz.muni.ics.kypo.training.service.CheatingDetectionService;
import cz.muni.ics.kypo.training.service.SecurityService;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.utils.AbstractFileExtensions;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The type Cheating Detection facade.
 */
@Service
@Transactional
public class CheatingDetectionFacade {

    private static final String DETECTION_EVENTS_FOLDER = "detection_events";
    private static final String ANSWER_SIMILARITY_FOLDER = "answer_similarity";
    private static final String LOCATION_SIMILARITY_FOLDER = "location_similarity";
    private static final String TIME_PROXIMITY_FOLDER = "time_proximity";
    private static final String MINIMAL_SOLVE_TIME_FOLDER = "minimal_solve_time";
    private static final String NO_COMMANDS_FOLDER = "no_commands";
    private static final String FORBIDDEN_COMMANDS_FOLDER = "forbidden_commands";
    private static final String PARTICIPANT_RESPONSE_FOLDER = "participant_responses";
    private final CheatingDetectionService cheatingDetectionService;
    public final UserService userService;
    private final TrainingInstanceService trainingInstanceService;
    private final DetectionEventMapper detectionEventMapper;
    private final CheatingDetectionMapper cheatingDetectionMapper;
    private final DetectionEventParticipantMapper detectionEventParticipantMapper;

    private final DetectedForbiddenCommandMapper detectedForbiddenCommandMapper;
    private final SecurityService securityService;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new Cheating detection facade.
     *
     * @param cheatingDetectionService the cheating detection service
     * @param userService              the user service
     * @param trainingInstanceService  the training instance service
     * @param detectionEventMapper     the cheating detection mapper
     * @param cheatingDetectionMapper  the cheating detection mapper
     * @param forbiddenCommandMapper   the forbidden command mapper
     * @param securityService          the security service
     */
    @Autowired
    public CheatingDetectionFacade(CheatingDetectionService cheatingDetectionService,
                                   UserService userService,
                                   TrainingInstanceService trainingInstanceService,
                                   DetectionEventMapper detectionEventMapper,
                                   CheatingDetectionMapper cheatingDetectionMapper,
                                   DetectionEventParticipantMapper detectionEventParticipantMapper,
                                   DetectedForbiddenCommandMapper forbiddenCommandMapper,
                                   SecurityService securityService,
                                   ObjectMapper objectMapper) {
        this.cheatingDetectionService = cheatingDetectionService;
        this.userService = userService;
        this.trainingInstanceService = trainingInstanceService;
        this.detectionEventMapper = detectionEventMapper;
        this.cheatingDetectionMapper = cheatingDetectionMapper;
        this.detectionEventParticipantMapper = detectionEventParticipantMapper;
        this.detectedForbiddenCommandMapper = forbiddenCommandMapper;
        this.securityService = securityService;
        this.objectMapper = objectMapper;
    }


    /**
     * Create a new cheating detection and execute it.
     *
     * @param cheatingDetectionDTO object with constructor information
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#cheatingDetectionDTO.getTrainingInstanceId())")
    @TransactionalWO
    public void createAndExecute(CheatingDetectionDTO cheatingDetectionDTO) {
        CheatingDetection cd = this.cheatingDetectionMapper.mapToEntity(cheatingDetectionDTO);
        this.cheatingDetectionService.createCheatingDetection(cd);
        this.cheatingDetectionService.executeCheatingDetection(cd);
    }

    /**
     * Rerun cheating detection
     *
     * @param cheatingDetectionId id of cheating detection for rerun.
     * @param trainingInstanceId  id of training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public void rerunCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
        this.cheatingDetectionService.deleteDetectionEvents(cheatingDetectionId);
        this.cheatingDetectionService.rerunCheatingDetection(cheatingDetectionId);
    }

    /**
     * Deletes cheating detection and all its associated events.
     *
     * @param cheatingDetectionId id of cheating detection.
     * @param trainingInstanceId  id of training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public void deleteCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
        this.cheatingDetectionService.deleteCheatingDetection(cheatingDetectionId, trainingInstanceId);
    }

    /**
     * Finds all detection events of a cheating detection.
     *
     * @param cheatingDetectionId the cheating detection ID
     * @param trainingInstanceId  id of training instance.
     * @param pageable            the pageable
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public PageResultResource<AbstractDetectionEventDTO> findAllDetectionEventsOfCheatingDetection(Long cheatingDetectionId,
                                                                                                   Long trainingInstanceId,
                                                                                                   Pageable pageable) {
        return detectionEventMapper.mapToPageResultResource(
                this.cheatingDetectionService.findAllDetectionEventsOfCheatingDetection(cheatingDetectionId, pageable));
    }

    /**
     * Finds all participants of detection event.
     *
     * @param eventId  the detection event ID
     * @param pageable the pageable
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public PageResultResource<DetectionEventParticipantDTO> findAllParticipantsOfDetectionEvent(Long eventId,
                                                                                                Pageable pageable) {
        return detectionEventParticipantMapper.mapToPageResultResource(
                this.cheatingDetectionService.findAllParticipantsOfEvent(eventId, pageable));
    }

    /**
     * Finds all forbidden commands of detection event.
     *
     * @param eventId  the detection event ID
     * @param pageable the pageable
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public PageResultResource<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(Long eventId,
                                                                                                    Pageable pageable) {
        return detectedForbiddenCommandMapper.mapToPageResultResource(
                this.cheatingDetectionService.findAllForbiddenCommandsOfDetectionEvent(eventId, pageable));
    }

    /**
     * Find detection event by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public AbstractDetectionEventDTO findDetectionEventById(Long eventId) {
        return detectionEventMapper.mapToDTO(this.cheatingDetectionService.findDetectionEventById(eventId));
    }

    /**
     * Find detection event of type answer similarity by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public AnswerSimilarityDetectionEventDTO findAnswerSimilarityEventById(Long eventId) {
        return detectionEventMapper.mapToAnswerSimilarityDetectionEventDTO(this.cheatingDetectionService.findAnswerSimilarityEventById(eventId));
    }

    /**
     * Find detection event of type location similarity by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public LocationSimilarityDetectionEventDTO findLocationSimilarityEventById(Long eventId) {
        return detectionEventMapper.mapToLocationSimilarityDetectionEventDTO(this.cheatingDetectionService.findLocationSimilarityEventById(eventId));
    }

    /**
     * Find detection event of type time proximity by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public TimeProximityDetectionEventDTO findTimeProximityEventById(Long eventId) {
        return detectionEventMapper.mapToTimeProximityDetectionEventDTO(this.cheatingDetectionService.findTimeProximityEventById(eventId));
    }

    /**
     * Find detection event of type minimal solve time by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public MinimalSolveTimeDetectionEventDTO findMinimalSolveTimeEventById(Long eventId) {
        return detectionEventMapper.mapToMinimalSolveTimeDetectionEventDTO(this.cheatingDetectionService.findMinimalSolveTimeEventById(eventId));
    }

    /**
     * Find detection event of type no commands by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public NoCommandsDetectionEventDTO findNoCommandsEventById(Long eventId) {
        return detectionEventMapper.mapToNoCommandsDetectionEventDTO(this.cheatingDetectionService.findNoCommandsEventById(eventId));
    }

    /**
     * Find detection event of type forbidden commands by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public ForbiddenCommandsDetectionEventDTO findForbiddenCommandsEventById(Long eventId) {
        return detectionEventMapper.mapToForbiddenCommandsDetectionEventDTO(this.cheatingDetectionService.findForbiddenCommandsEventById(eventId));
    }

    /**
     * Exports Cheating Detection to file
     *
     * @param cheatingDetectionId the id of the cheating detection to be exported
     * @return the file containing cheating detection, {@link FileToReturnDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalRO
    public FileToReturnDTO archiveCheatingDetectionResults(Long cheatingDetectionId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            CheatingDetection cheatingDetection = cheatingDetectionService.findCheatingDetectionById(cheatingDetectionId);
            CheatingDetectionDTO cheatingDetectionDTO = cheatingDetectionMapper.mapToDTO(cheatingDetection);

            writeCheatingDetection(zos, cheatingDetectionId, cheatingDetectionDTO);
            writeAnswerSimilarityDetectionEvents(zos, cheatingDetectionId);
            writeLocationSimilarityDetectionEvents(zos, cheatingDetectionId);
            writeTimeProximityDetectionEvents(zos, cheatingDetectionId);
            writeMinimalSolveTimeDetectionEvents(zos, cheatingDetectionId);
            writeNoCommandsDetectionEvents(zos, cheatingDetectionId);
            writeForbiddenCommandsDetectionEvents(zos, cheatingDetectionId);
            writeTraineeParticipantGroups(zos, cheatingDetectionId);

            zos.closeEntry();
            zos.close();
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(baos.toByteArray());
            fileToReturnDTO.setTitle("cheating-detection-" + cheatingDetection.getId().toString());
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException("The .zip file was not created since there were some processing error.", ex);
        }
    }

    private void writeTraineeParticipantGroups(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        ParticipantGroups groups = populateParticipantGroups(cheatingDetectionId);
        List<List<Long>> userIdGroups = groups.getUserIdGroups();
        List<List<Long>> eventIdGroups = groups.getEventIdGroups();

        for (int i = 0; i < userIdGroups.size(); i++) {
            var currentUserGroup = userIdGroups.get(i);
            var currentEventGroup = eventIdGroups.get(i);
            var usersString = new StringBuilder();
            for (var userId : currentUserGroup) {
                usersString.append(userId).append('_');
            }
            ZipEntry participantResponseEntry = new ZipEntry(PARTICIPANT_RESPONSE_FOLDER + "/" + usersString + AbstractFileExtensions.CSV_FILE_EXTENSION);
            zos.putNextEntry(participantResponseEntry);
            auditParticipantGroup(currentEventGroup, zos);
        }
    }

    private ParticipantGroups populateParticipantGroups(Long cheatingDetectionId) {
        List<DetectionEventParticipant> participants = cheatingDetectionService.findAllParticipantsOfCheatingDetection(cheatingDetectionId);
        List<List<Long>> userIdGroups = new ArrayList<>();
        List<List<Long>> eventIdGroups = new ArrayList<>();

        for (var participant : participants) {
            var wasAdded = false;
            Long userId = participant.getUserId();
            Long eventId = participant.getDetectionEventId();
            for (int i = 0; i < userIdGroups.size(); i++) {
                var currentUserGroup = userIdGroups.get(i);
                var currentEventGroup = eventIdGroups.get(i);
                if (currentUserGroup.contains(userId)) {
                    if (!currentEventGroup.contains(eventId)) {
                        currentEventGroup.add(eventId);
                        wasAdded = true;
                    }
                }
                if (currentEventGroup.contains(eventId)) {
                    if (!currentUserGroup.contains(userId)) {
                        currentUserGroup.add(userId);
                        wasAdded = true;
                    }
                }
            }
            if (!wasAdded) {
                userIdGroups.add(new ArrayList<>(Collections.singletonList(userId)));
                eventIdGroups.add(new ArrayList<>(Collections.singletonList(eventId)));
            }
        }
        return new ParticipantGroups(userIdGroups, eventIdGroups);
    }

    private void auditParticipantGroup(List<Long> eventIds, ZipOutputStream zos) throws IOException {
        for (var eventId : eventIds) {
            AbstractDetectionEvent event = cheatingDetectionService.findDetectionEventById(eventId);
            var eventType = event.getDetectionEventType();
            List<DetectionEventParticipant> participants = cheatingDetectionService.findAllParticipantsOfEvent(eventId);
            switch (eventType) {
                case ANSWER_SIMILARITY -> auditAnswerSimilarityGroup(participants, cheatingDetectionService.findAnswerSimilarityEventById(eventId), zos);
                case LOCATION_SIMILARITY -> auditLocationSimilarityGroup(participants, cheatingDetectionService.findLocationSimilarityEventById(eventId), zos);
                case MINIMAL_SOLVE_TIME -> auditMinimalSolveTimeGroup(participants, cheatingDetectionService.findMinimalSolveTimeEventById(eventId), zos);
                case TIME_PROXIMITY -> auditTimeProximityGroup(participants, cheatingDetectionService.findTimeProximityEventById(eventId), zos);
                case NO_COMMANDS -> auditNoCommandsGroup(participants, cheatingDetectionService.findNoCommandsEventById(eventId), zos);
                case FORBIDDEN_COMMANDS -> auditForbiddenCommandsGroup(participants, cheatingDetectionService.findForbiddenCommandsEventById(eventId), zos);
            }
        }
    }

    private void auditAnswerSimilarityGroup(List<DetectionEventParticipant> participants, AnswerSimilarityDetectionEvent event, ZipOutputStream zos) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(zos);
        writer.write("participant,time\n");
        for (var participant : participants) {
            writer.write(String.format("%s,%s\n", participant.getParticipantName(), participant.getOccurredAt()));
        }
        writer.write("\n");
        writer.write("detection type,detected at,level title,answer,answer owner\n");
        writer.write(String.format("%s,%s,%s,%s,%s\n", event.getDetectionEventType(), event.getDetectedAt(), event.getLevelTitle(), event.getAnswer(), event.getAnswerOwner()));
        writer.write("\n\n");
        writer.close();
    }

    private void auditLocationSimilarityGroup(List<DetectionEventParticipant> participants, LocationSimilarityDetectionEvent event, ZipOutputStream zos) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(zos);
        writer.write("participant,time,ip address\n");
        for (var participant : participants) {
            writer.write(String.format("%s,%s,%s\n", participant.getParticipantName(), participant.getOccurredAt(), participant.getIpAddress()));
        }
        writer.write("\n");
        writer.write("detection type,detected at,level title,dns,ip address\n");
        writer.write(String.format("%s,%s,%s,%s,%s\n", event.getDetectionEventType(), event.getDetectedAt(), event.getLevelTitle(), event.getDns(), event.getIpAddress()));
        writer.write("\n\n");
        writer.close();
    }

    private void auditMinimalSolveTimeGroup(List<DetectionEventParticipant> participants, MinimalSolveTimeDetectionEvent event, ZipOutputStream zos) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(zos);
        writer.write("participant,time,solved in(seconds)\n");
        for (var participant : participants) {
            writer.write(String.format("%s,%s,%s\n", participant.getParticipantName(), participant.getOccurredAt(), participant.getSolvedInTime()));
        }
        writer.write("\n");
        writer.write("detection type,detected at,level title,minimal solve time\n");
        writer.write(String.format("%s,%s,%s,%s\n", event.getDetectionEventType(), event.getDetectedAt(), event.getLevelTitle(), event.getMinimalSolveTime()));
        writer.write("\n\n");
        writer.close();
    }

    private void auditTimeProximityGroup(List<DetectionEventParticipant> participants, TimeProximityDetectionEvent event, ZipOutputStream zos) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(zos);
        writer.write("participant,time\n");
        for (var participant : participants) {
            writer.write(String.format("%s,%s\n", participant.getParticipantName(), participant.getOccurredAt()));
        }
        writer.write("\n");
        writer.write("detection type,detected at,level title,proximity\n");
        writer.write(String.format("%s,%s,%s,%s\n", event.getDetectionEventType(), event.getDetectedAt(), event.getLevelTitle(), event.getThreshold()));
        writer.write("\n\n");
        writer.close();
    }

    private void auditNoCommandsGroup(List<DetectionEventParticipant> participants, NoCommandsDetectionEvent event, ZipOutputStream zos) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(zos);
        writer.write("participant,time\n");
        for (var participant : participants) {
            writer.write(String.format("%s,%s\n", participant.getParticipantName(), participant.getOccurredAt()));
        }
        writer.write("\n");
        writer.write("detection type,detected at,level title\n");
        writer.write(String.format("%s,%s,%s\n", event.getDetectionEventType(), event.getDetectedAt(), event.getLevelTitle()));
        writer.write("\n\n");
        writer.close();
    }

    private void auditForbiddenCommandsGroup(List<DetectionEventParticipant> participants, ForbiddenCommandsDetectionEvent event, ZipOutputStream zos) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(zos);
        writer.write("participant,time\n");
        for (var participant : participants) {
            writer.write(String.format("%s,%s\n", participant.getParticipantName(), participant.getOccurredAt()));
        }
        writer.write("\n");
        writer.write("detection type,detected at,level title,proximity\n");
        writer.write(String.format("%s,%s,%s\n", event.getDetectionEventType(), event.getDetectedAt(), event.getLevelTitle()));
        writer.write("\n\n");
        writer.close();
    }

    private void writeCheatingDetection(ZipOutputStream zos, Long cheatingDetectionId, CheatingDetectionDTO cheatingDetectionDTO) throws IOException {
        ZipEntry cheatingDetectionEntry = new ZipEntry("cheating-detection-id" + cheatingDetectionId + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(cheatingDetectionEntry);
        zos.write(objectMapper.writeValueAsBytes(cheatingDetectionDTO));
    }

    private void writeDetectionEventToFile(ZipOutputStream zos, AbstractDetectionEvent event, String dirName) throws IOException {
        ZipEntry detectionEventEntry = new ZipEntry(DETECTION_EVENTS_FOLDER + "/" + dirName + "/detection-event-id" + event.getId() + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(detectionEventEntry);
        List<DetectionEventParticipant> participants = cheatingDetectionService.findAllParticipantsOfEvent(event.getId());
        zos.write(objectMapper.writeValueAsBytes("{ Event: "));
        zos.write(objectMapper.writeValueAsBytes(event));
        zos.write(objectMapper.writeValueAsBytes(" Participants: "));
        for (var participant : participants) {
            zos.write(objectMapper.writeValueAsBytes(participant));
        }
        zos.write(objectMapper.writeValueAsBytes(" }"));
    }

    private void writeAnswerSimilarityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<AnswerSimilarityDetectionEvent> detectionEventsOfAS = cheatingDetectionService.findAllAnswerSimilarityEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfAS) {
            writeDetectionEventToFile(zos, event, ANSWER_SIMILARITY_FOLDER);
        }
    }

    private void writeLocationSimilarityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<LocationSimilarityDetectionEvent> detectionEventsOfLS = cheatingDetectionService.findAllLocationSimilarityEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfLS) {
            writeDetectionEventToFile(zos, event, LOCATION_SIMILARITY_FOLDER);
        }
    }

    private void writeTimeProximityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<TimeProximityDetectionEvent> detectionEventsOfTP = cheatingDetectionService.findAllTimeProximityEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfTP) {
            writeDetectionEventToFile(zos, event, TIME_PROXIMITY_FOLDER);
        }
    }

    private void writeMinimalSolveTimeDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<MinimalSolveTimeDetectionEvent> detectionEventsOfMST = cheatingDetectionService.findAllMinimalSolveTimeEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfMST) {
            writeDetectionEventToFile(zos, event, MINIMAL_SOLVE_TIME_FOLDER);
        }
    }

    private void writeNoCommandsDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<NoCommandsDetectionEvent> detectionEventsOfNC = cheatingDetectionService.findAllNoCommandsEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfNC) {
            writeDetectionEventToFile(zos, event, NO_COMMANDS_FOLDER);
        }
    }

    private void writeForbiddenCommandsDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<ForbiddenCommandsDetectionEvent> detectionEventsOfFC = cheatingDetectionService.findAllForbiddenCommandsEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfFC) {
            writeDetectionEventToFile(zos, event, FORBIDDEN_COMMANDS_FOLDER);
        }
    }

    /**
     * Find all cheating detections of a training instance
     *
     * @param trainingInstanceId id of Training instance for cheating detection.
     * @param pageable           pageable parameter with information about pagination.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public PageResultResource<CheatingDetectionDTO> findAllCheatingDetectionsOfTrainingInstance(Long trainingInstanceId, Pageable pageable) {

        return cheatingDetectionMapper.mapToPageResultResource(
                this.cheatingDetectionService.findAllCheatingDetectionsOfTrainingInstance(trainingInstanceId, pageable));
    }
}

