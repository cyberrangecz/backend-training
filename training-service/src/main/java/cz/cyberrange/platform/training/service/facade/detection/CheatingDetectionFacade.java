package cz.cyberrange.platform.training.service.facade.detection;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.CheatingDetectionDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.CheatingDetectionMapper;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.detection.CheatingDetectionService;
import cz.cyberrange.platform.training.service.services.detection.DetectionEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the lifecycle of cheating detections for a training instance: creating and running
 * one, rerunning an existing one, deleting one together with its detection events, and listing the
 * detections of a training instance
 */
@Service
@Transactional
public class CheatingDetectionFacade {

  private final CheatingDetectionService cheatingDetectionService;
  private final DetectionEventService detectionEventService;
  public final UserService userService;
  private final CheatingDetectionMapper cheatingDetectionMapper;

  @Autowired
  public CheatingDetectionFacade(
      CheatingDetectionService cheatingDetectionService,
      DetectionEventService detectionEventService,
      UserService userService,
      CheatingDetectionMapper cheatingDetectionMapper) {
    this.cheatingDetectionService = cheatingDetectionService;
    this.detectionEventService = detectionEventService;
    this.userService = userService;
    this.cheatingDetectionMapper = cheatingDetectionMapper;
  }

  /**
   * Creates a cheating detection from the given configuration and immediately executes it.
   *
   * @param cheatingDetectionDTO the cheating detection to create and execute
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#cheatingDetectionDTO.getTrainingInstanceId())")
  @TransactionalWO
  public void createAndExecute(CheatingDetectionDTO cheatingDetectionDTO) {
    CheatingDetection cd = this.cheatingDetectionMapper.mapToEntity(cheatingDetectionDTO);
    this.cheatingDetectionService.createCheatingDetection(cd);
    this.cheatingDetectionService.executeCheatingDetection(cd);
  }

  /**
   * Deletes the detection events of a cheating detection and re-executes it. The {@code
   * trainingInstanceId} parameter takes no part in the deletion or re-execution; only the
   * authorization check is scoped by {@code cheatingDetectionId}.
   *
   * @param cheatingDetectionId id of cheating detection for rerun.
   * @param trainingInstanceId id of training instance.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenCheatingDetection(#cheatingDetectionId)")
  @TransactionalWO
  public void rerunCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
    this.detectionEventService.deleteDetectionEvents(cheatingDetectionId);
    this.cheatingDetectionService.reExecuteCheatingDetection(cheatingDetectionId);
  }

  /**
   * Deletes a cheating detection together with its detection events, their participants and their
   * forbidden commands, and clears the detection-event flag on every training run of {@code
   * trainingInstanceId} — not only the runs the deleted detection covered.
   *
   * @param cheatingDetectionId id of cheating detection.
   * @param trainingInstanceId id of the training instance whose runs have their detection-event
   *     flag cleared.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or (@securityService.isOrganizerOfGivenCheatingDetection(#cheatingDetectionId)"
          + " and @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId))")
  @TransactionalWO
  public void deleteCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
    this.cheatingDetectionService.deleteCheatingDetection(cheatingDetectionId, trainingInstanceId);
  }

  /**
   * Finds all cheating detections of a training instance, ordered by their execution time.
   *
   * @param trainingInstanceId id of Training instance for cheating detection.
   * @param pageable pageable parameter with information about pagination.
   * @return page of {@link CheatingDetectionDTO} for the training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalWO
  public PageResultResource<CheatingDetectionDTO> findAllCheatingDetectionsOfTrainingInstance(
      Long trainingInstanceId, Pageable pageable) {

    return cheatingDetectionMapper.mapToPageResultResource(
        this.cheatingDetectionService.findAllCheatingDetectionsOfTrainingInstance(
            trainingInstanceId, pageable));
  }
}
