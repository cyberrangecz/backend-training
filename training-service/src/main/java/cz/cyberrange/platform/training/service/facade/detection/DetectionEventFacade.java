package cz.cyberrange.platform.training.service.facade.detection;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.AbstractDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.AnswerSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectedForbiddenCommandDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectionEventParticipantDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.ForbiddenCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.LocationSimilarityDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.MinimalSolveTimeDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.NoCommandsDetectionEventDTO;
import cz.cyberrange.platform.training.api.dto.cheatingdetection.TimeProximityDetectionEventDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.AnswerSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.MinimalSolveTimeDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.NoCommandsDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.TimeProximityDetectionEvent;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.DetectedForbiddenCommandMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.DetectionEventMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.DetectionEventParticipantMapper;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.detection.AnswerSimilarityService;
import cz.cyberrange.platform.training.service.services.detection.DetectionEventService;
import cz.cyberrange.platform.training.service.services.detection.ForbiddenCommandsService;
import cz.cyberrange.platform.training.service.services.detection.LocationSimilarityService;
import cz.cyberrange.platform.training.service.services.detection.MinimalSolveTimeService;
import cz.cyberrange.platform.training.service.services.detection.NoCommandsService;
import cz.cyberrange.platform.training.service.services.detection.TimeProximityService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retrieves the detection events produced by a cheating detection, their participants and their
 * detected forbidden commands, both in bulk and by individual event kind (answer similarity,
 * location similarity, time proximity, minimal solve time, no commands, forbidden commands)
 */
@Service
@Transactional
public class DetectionEventFacade {
  private final AnswerSimilarityService answerSimilarityService;
  private final LocationSimilarityService locationSimilarityService;
  private final MinimalSolveTimeService minimalSolveTimeService;
  private final TimeProximityService timeProximityService;
  private final NoCommandsService noCommandsService;
  private final ForbiddenCommandsService forbiddenCommandsService;
  private final DetectionEventService detectionEventService;
  public final UserService userService;
  private final DetectionEventMapper detectionEventMapper;
  private final DetectionEventParticipantMapper detectionEventParticipantMapper;

  private final DetectedForbiddenCommandMapper detectedForbiddenCommandMapper;

  @Autowired
  public DetectionEventFacade(
      UserService userService,
      DetectionEventMapper detectionEventMapper,
      DetectionEventParticipantMapper detectionEventParticipantMapper,
      DetectedForbiddenCommandMapper forbiddenCommandMapper,
      DetectionEventService detectionEventService,
      AnswerSimilarityService answerSimilarityService,
      LocationSimilarityService locationSimilarityService,
      MinimalSolveTimeService minimalSolveTimeService,
      TimeProximityService timeProximityService,
      NoCommandsService noCommandsService,
      ForbiddenCommandsService forbiddenCommandsService) {
    this.userService = userService;
    this.detectionEventMapper = detectionEventMapper;
    this.detectionEventParticipantMapper = detectionEventParticipantMapper;
    this.detectedForbiddenCommandMapper = forbiddenCommandMapper;
    this.detectionEventService = detectionEventService;
    this.answerSimilarityService = answerSimilarityService;
    this.locationSimilarityService = locationSimilarityService;
    this.minimalSolveTimeService = minimalSolveTimeService;
    this.timeProximityService = timeProximityService;
    this.noCommandsService = noCommandsService;
    this.forbiddenCommandsService = forbiddenCommandsService;
  }

  /**
   * Finds all detection events of a cheating detection, filtered by {@code predicate}. The {@code
   * trainingInstanceId} parameter takes no part in the query or the authorization check.
   *
   * @param cheatingDetectionId the cheating detection ID
   * @param pageable the pageable
   * @return page of {@link AbstractDetectionEventDTO} matching the predicate
   * @throws EntityNotFoundException when the cheating detection with the given id does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenCheatingDetection(#cheatingDetectionId)")
  @TransactionalWO
  public PageResultResource<AbstractDetectionEventDTO> findAllDetectionEventsOfCheatingDetection(
      Long cheatingDetectionId, Pageable pageable, Predicate predicate, Long trainingInstanceId) {
    return detectionEventMapper.mapToPageResultResource(
        this.detectionEventService.findAllDetectionEventsOfCheatingDetection(
            cheatingDetectionId, pageable, predicate));
  }

  /**
   * Finds all participants of a detection event.
   *
   * @param eventId the detection event ID
   * @param pageable the pageable
   * @return page of {@link DetectionEventParticipantDTO} for the event
   * @throws EntityNotFoundException when the detection event with the given id does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public PageResultResource<DetectionEventParticipantDTO> findAllParticipantsOfDetectionEvent(
      Long eventId, Pageable pageable) {
    getDetectionEvent(eventId);
    return detectionEventParticipantMapper.mapToPageResultResource(
        this.detectionEventService.findAllParticipantsOfEvent(eventId, pageable));
  }

  /**
   * Finds all forbidden commands detected within a forbidden-commands detection event.
   *
   * @param eventId the detection event ID
   * @param pageable the pageable
   * @return page of {@link DetectedForbiddenCommandDTO} for the event
   * @throws EntityNotFoundException when the detection event with the given id does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public PageResultResource<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(
      Long eventId, Pageable pageable) {
    getDetectionEvent(eventId);
    return detectedForbiddenCommandMapper.mapToPageResultResource(
        this.detectionEventService.findAllForbiddenCommandsOfDetectionEvent(eventId, pageable));
  }

  /**
   * Finds all forbidden commands detected within a forbidden-commands detection event, unpaged.
   *
   * @param eventId the detection event ID
   * @return every {@link DetectedForbiddenCommandDTO} for the event
   * @throws EntityNotFoundException when the detection event with the given id does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public List<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(Long eventId) {
    getDetectionEvent(eventId);
    return detectedForbiddenCommandMapper.mapToListDTO(
        this.detectionEventService.findAllForbiddenCommandsOfDetectionEvent(eventId));
  }

  /**
   * Finds a detection event by its ID, regardless of its kind.
   *
   * @param eventId the detection event ID
   * @return the event as {@link AbstractDetectionEventDTO}
   * @throws EntityNotFoundException when the detection event with the given id does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public AbstractDetectionEventDTO findDetectionEventById(Long eventId) {
    return detectionEventMapper.mapToDTO(getDetectionEvent(eventId));
  }

  /**
   * Loads a detection event of any kind.
   *
   * @throws EntityNotFoundException when the detection event with the given id does not exist
   */
  private AbstractDetectionEvent getDetectionEvent(Long eventId) {
    AbstractDetectionEvent detectionEvent =
        this.detectionEventService.findDetectionEventById(eventId);
    if (detectionEvent == null) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(AbstractDetectionEvent.class, "id", eventId.getClass(), eventId));
    }
    return detectionEvent;
  }

  /**
   * Finds a detection event of type answer similarity by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link AnswerSimilarityDetectionEventDTO}
   * @throws EntityNotFoundException when the answer similarity detection event with the given id
   *     does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public AnswerSimilarityDetectionEventDTO findAnswerSimilarityEventById(Long eventId) {
    AnswerSimilarityDetectionEvent answerSimilarityDetectionEvent =
        this.answerSimilarityService.findAnswerSimilarityEventById(eventId);
    if (answerSimilarityDetectionEvent == null) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              AnswerSimilarityDetectionEvent.class, "id", eventId.getClass(), eventId));
    }
    return detectionEventMapper.mapToAnswerSimilarityDetectionEventDTO(
        answerSimilarityDetectionEvent);
  }

  /**
   * Finds a detection event of type location similarity by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link LocationSimilarityDetectionEventDTO}
   * @throws EntityNotFoundException when the location similarity detection event with the given id
   *     does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public LocationSimilarityDetectionEventDTO findLocationSimilarityEventById(Long eventId) {
    LocationSimilarityDetectionEvent locationSimilarityDetectionEvent =
        this.locationSimilarityService.findLocationSimilarityEventById(eventId);
    if (locationSimilarityDetectionEvent == null) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              LocationSimilarityDetectionEvent.class, "id", eventId.getClass(), eventId));
    }
    return detectionEventMapper.mapToLocationSimilarityDetectionEventDTO(
        locationSimilarityDetectionEvent);
  }

  /**
   * Finds a detection event of type time proximity by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link TimeProximityDetectionEventDTO}
   * @throws EntityNotFoundException when the time proximity detection event with the given id does
   *     not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public TimeProximityDetectionEventDTO findTimeProximityEventById(Long eventId) {
    TimeProximityDetectionEvent timeProximityDetectionEvent =
        this.timeProximityService.findTimeProximityEventById(eventId);
    if (timeProximityDetectionEvent == null) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              TimeProximityDetectionEvent.class, "id", eventId.getClass(), eventId));
    }
    return detectionEventMapper.mapToTimeProximityDetectionEventDTO(timeProximityDetectionEvent);
  }

  /**
   * Finds a detection event of type minimal solve time by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link MinimalSolveTimeDetectionEventDTO}
   * @throws EntityNotFoundException when the minimal solve time detection event with the given id
   *     does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public MinimalSolveTimeDetectionEventDTO findMinimalSolveTimeEventById(Long eventId) {
    MinimalSolveTimeDetectionEvent minimalSolveTimeDetectionEvent =
        this.minimalSolveTimeService.findMinimalSolveTimeEventById(eventId);
    if (minimalSolveTimeDetectionEvent == null) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              MinimalSolveTimeDetectionEvent.class, "id", eventId.getClass(), eventId));
    }
    return detectionEventMapper.mapToMinimalSolveTimeDetectionEventDTO(
        minimalSolveTimeDetectionEvent);
  }

  /**
   * Finds a detection event of type no commands by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link NoCommandsDetectionEventDTO}
   * @throws EntityNotFoundException when the no commands detection event with the given id does not
   *     exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public NoCommandsDetectionEventDTO findNoCommandsEventById(Long eventId) {
    NoCommandsDetectionEvent noCommandsDetectionEvent =
        this.noCommandsService.findNoCommandsEventById(eventId);
    if (noCommandsDetectionEvent == null) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(NoCommandsDetectionEvent.class, "id", eventId.getClass(), eventId));
    }
    return detectionEventMapper.mapToNoCommandsDetectionEventDTO(noCommandsDetectionEvent);
  }

  /**
   * Finds a detection event of type forbidden commands by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link ForbiddenCommandsDetectionEventDTO}
   * @throws EntityNotFoundException when the forbidden commands detection event with the given id
   *     does not exist
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public ForbiddenCommandsDetectionEventDTO findForbiddenCommandsEventById(Long eventId) {
    ForbiddenCommandsDetectionEvent forbiddenCommandsDetectionEvent =
        this.forbiddenCommandsService.findForbiddenCommandsEventById(eventId);
    if (forbiddenCommandsDetectionEvent == null) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              ForbiddenCommandsDetectionEvent.class, "id", eventId.getClass(), eventId));
    }
    return detectionEventMapper.mapToForbiddenCommandsDetectionEventDTO(
        forbiddenCommandsDetectionEvent);
  }
}
