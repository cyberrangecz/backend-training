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
import cz.cyberrange.platform.training.api.responses.PageResultResource;
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
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public PageResultResource<DetectionEventParticipantDTO> findAllParticipantsOfDetectionEvent(
      Long eventId, Pageable pageable) {
    return detectionEventParticipantMapper.mapToPageResultResource(
        this.detectionEventService.findAllParticipantsOfEvent(eventId, pageable));
  }

  /**
   * Finds all forbidden commands detected within a forbidden-commands detection event.
   *
   * @param eventId the detection event ID
   * @param pageable the pageable
   * @return page of {@link DetectedForbiddenCommandDTO} for the event
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public PageResultResource<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(
      Long eventId, Pageable pageable) {
    return detectedForbiddenCommandMapper.mapToPageResultResource(
        this.detectionEventService.findAllForbiddenCommandsOfDetectionEvent(eventId, pageable));
  }

  /**
   * Finds all forbidden commands detected within a forbidden-commands detection event, unpaged.
   *
   * @param eventId the detection event ID
   * @return every {@link DetectedForbiddenCommandDTO} for the event
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public List<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(Long eventId) {
    return detectedForbiddenCommandMapper.mapToListDTO(
        this.detectionEventService.findAllForbiddenCommandsOfDetectionEvent(eventId));
  }

  /**
   * Finds a detection event by its ID, regardless of its kind.
   *
   * @param eventId the detection event ID
   * @return the event as {@link AbstractDetectionEventDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public AbstractDetectionEventDTO findDetectionEventById(Long eventId) {
    return detectionEventMapper.mapToDTO(
        this.detectionEventService.findDetectionEventById(eventId));
  }

  /**
   * Finds a detection event of type answer similarity by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link AnswerSimilarityDetectionEventDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public AnswerSimilarityDetectionEventDTO findAnswerSimilarityEventById(Long eventId) {
    return detectionEventMapper.mapToAnswerSimilarityDetectionEventDTO(
        this.answerSimilarityService.findAnswerSimilarityEventById(eventId));
  }

  /**
   * Finds a detection event of type location similarity by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link LocationSimilarityDetectionEventDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public LocationSimilarityDetectionEventDTO findLocationSimilarityEventById(Long eventId) {
    return detectionEventMapper.mapToLocationSimilarityDetectionEventDTO(
        this.locationSimilarityService.findLocationSimilarityEventById(eventId));
  }

  /**
   * Finds a detection event of type time proximity by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link TimeProximityDetectionEventDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public TimeProximityDetectionEventDTO findTimeProximityEventById(Long eventId) {
    return detectionEventMapper.mapToTimeProximityDetectionEventDTO(
        this.timeProximityService.findTimeProximityEventById(eventId));
  }

  /**
   * Finds a detection event of type minimal solve time by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link MinimalSolveTimeDetectionEventDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public MinimalSolveTimeDetectionEventDTO findMinimalSolveTimeEventById(Long eventId) {
    return detectionEventMapper.mapToMinimalSolveTimeDetectionEventDTO(
        this.minimalSolveTimeService.findMinimalSolveTimeEventById(eventId));
  }

  /**
   * Finds a detection event of type no commands by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link NoCommandsDetectionEventDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public NoCommandsDetectionEventDTO findNoCommandsEventById(Long eventId) {
    return detectionEventMapper.mapToNoCommandsDetectionEventDTO(
        this.noCommandsService.findNoCommandsEventById(eventId));
  }

  /**
   * Finds a detection event of type forbidden commands by its ID.
   *
   * @param eventId the detection event ID
   * @return the event as {@link ForbiddenCommandsDetectionEventDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenDetectionEvent(#eventId)")
  @TransactionalWO
  public ForbiddenCommandsDetectionEventDTO findForbiddenCommandsEventById(Long eventId) {
    return detectionEventMapper.mapToForbiddenCommandsDetectionEventDTO(
        this.forbiddenCommandsService.findForbiddenCommandsEventById(eventId));
  }
}
