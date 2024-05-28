package cz.muni.ics.kypo.training.facade.detection;

import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.cheatingdetection.*;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.mapping.mapstruct.detection.CheatingDetectionMapper;
import cz.muni.ics.kypo.training.persistence.model.detection.*;
import cz.muni.ics.kypo.training.service.*;
import cz.muni.ics.kypo.training.service.detection.CheatingDetectionService;
import cz.muni.ics.kypo.training.service.detection.DetectionEventService;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Cheating Detection facade.
 */
@Service
@Transactional
public class CheatingDetectionFacade {

    private final CheatingDetectionService cheatingDetectionService;
    private final DetectionEventService detectionEventService;
    public final UserService userService;
    private final CheatingDetectionMapper cheatingDetectionMapper;
    private final SecurityService securityService;

    /**
     * Instantiates a new Cheating detection facade.
     *
     * @param cheatingDetectionService the cheating detection service
     * @param detectionEventService the detection event service
     * @param userService              the user service
     * @param cheatingDetectionMapper  the cheating detection mapper
     * @param securityService          the security service
     */
    @Autowired
    public CheatingDetectionFacade(CheatingDetectionService cheatingDetectionService,
                                   DetectionEventService detectionEventService,
                                   UserService userService,
                                   CheatingDetectionMapper cheatingDetectionMapper,
                                   SecurityService securityService) {
        this.cheatingDetectionService = cheatingDetectionService;
        this.detectionEventService = detectionEventService;
        this.userService = userService;
        this.cheatingDetectionMapper = cheatingDetectionMapper;
        this.securityService = securityService;
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
        this.detectionEventService.deleteDetectionEvents(cheatingDetectionId);
        this.cheatingDetectionService.reExecuteCheatingDetection(cheatingDetectionId);
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

