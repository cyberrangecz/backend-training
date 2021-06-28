package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.*;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapper;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The type Training instance facade.
 */
@Service
public class TrainingInstanceFacade {

    private TrainingInstanceService trainingInstanceService;
    private TrainingDefinitionService trainingDefinitionService;
    private TrainingRunService trainingRunService;
    private UserService userService;
    private SecurityService securityService;
    private TrainingInstanceMapper trainingInstanceMapper;
    private TrainingRunMapper trainingRunMapper;
    private ElasticsearchApiService elasticsearchApiService;


    /**
     * Instantiates a new Training instance facade.
     *
     * @param trainingInstanceService   the training instance service
     * @param trainingDefinitionService the training definition service
     * @param trainingRunService        the training run service
     * @param trainingInstanceMapper    the training instance mapper
     * @param trainingRunMapper         the training run mapper
     * @param userService               the user service
     * @param elasticsearchApiService   the elasticsearch api service
     * @param securityService           the security service
     */
    @Autowired
    public TrainingInstanceFacade(TrainingInstanceService trainingInstanceService,
                                  TrainingDefinitionService trainingDefinitionService,
                                  TrainingRunService trainingRunService,
                                  UserService userService,
                                  ElasticsearchApiService elasticsearchApiService,
                                  SecurityService securityService,
                                  TrainingInstanceMapper trainingInstanceMapper,
                                  TrainingRunMapper trainingRunMapper) {
        this.trainingInstanceService = trainingInstanceService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingRunService = trainingRunService;
        this.userService = userService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.securityService = securityService;
        this.trainingInstanceMapper = trainingInstanceMapper;
        this.trainingRunMapper = trainingRunMapper;
    }

    /**
     * Finds specific Training Instance by id
     *
     * @param id of a Training Instance that would be returned
     * @return specific {@link TrainingInstanceDTO} by id
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#id)")
    @TransactionalRO
    public TrainingInstanceDTO findById(Long id) {
        return trainingInstanceMapper.mapToDTO(trainingInstanceService.findByIdIncludingDefinition(id));
    }

    /**
     * Find all Training Instances.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingInstanceFindAllResponseDTO}
     */
    @IsOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingInstanceFindAllResponseDTO> findAll(Predicate predicate, Pageable pageable) {
        if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
            return trainingInstanceMapper.mapToPageResultResourceBasicView(trainingInstanceService.findAll(predicate, pageable));
        }
        return trainingInstanceMapper.mapToPageResultResourceBasicView(trainingInstanceService.findAll(predicate, pageable, securityService.getUserRefIdFromUserAndGroup()));
    }

    /**
     * Updates training instance
     *
     * @param trainingInstanceUpdateDTO to be updated
     * @return new access token if it was changed
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public String update(TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
        TrainingInstance trainingInstanceToUpdate = trainingInstanceMapper.mapUpdateToEntity(trainingInstanceUpdateDTO);
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceUpdateDTO.getId());

        if (LocalDateTime.now(Clock.systemUTC()).isAfter(trainingInstance.getStartTime()) &&
                !trainingInstance.getTrainingDefinition().getId().equals(trainingInstanceUpdateDTO.getTrainingDefinitionId())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstance.getId(),
                    "The training definition assigned to running training instance cannot be changed."));
        }
        trainingInstanceToUpdate.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceUpdateDTO.getTrainingDefinitionId()));
        return trainingInstanceService.update(trainingInstanceToUpdate);
    }

    /**
     * Creates new training instance
     *
     * @param trainingInstanceCreateDTO to be created
     * @return created {@link TrainingInstanceDTO}
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public TrainingInstanceDTO create(TrainingInstanceCreateDTO trainingInstanceCreateDTO) {
        TrainingInstance trainingInstance = trainingInstanceMapper.mapCreateToEntity(trainingInstanceCreateDTO);
        trainingInstance.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceCreateDTO.getTrainingDefinitionId()));
        trainingInstance.setId(null);
        return trainingInstanceMapper.mapToDTO(trainingInstanceService.create(trainingInstance));
    }

    private void addOrganizersToTrainingInstance(TrainingInstance trainingInstance, Set<Long> userRefIdsOfOrganizers) {
        if (userRefIdsOfOrganizers.isEmpty()) return;
        PageResultResource<UserRefDTO> organizers;
        int page = 0;
        do {
            organizers = userService.getUsersRefDTOByGivenUserIds(userRefIdsOfOrganizers, PageRequest.of(page, 999), null, null);
            Set<Long> actualOrganizersIds = trainingInstance.getOrganizers().stream()
                                                    .map(UserRef::getUserRefId)
                                                    .collect(Collectors.toSet());
            page++;
            for (UserRefDTO organizer : organizers.getContent()) {
                if (actualOrganizersIds.contains(organizer.getUserRefId())) {
                    continue;
                }
                try {
                    trainingInstance.addOrganizer(userService.getUserByUserRefId(organizer.getUserRefId()));
                } catch (EntityNotFoundException ex) {
                    trainingInstance.addOrganizer(userService.createUserRef(createUserRefFromDTO(organizer)));
                }
            }
        } while (organizers.getPagination().getTotalPages() != page);
    }

    private UserRef createUserRefFromDTO(UserRefDTO userToBeCreated) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userToBeCreated.getUserRefId());
        return userRef;
    }

    /**
     * Deletes specific training instance based on id
     *
     * @param trainingInstanceId of training instance to be deleted
     * @param forceDelete        indicates if this training run should be force deleted.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public void delete(Long trainingInstanceId, boolean forceDelete) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        if (forceDelete) {
            Set<TrainingRun> trainingRunsInTrainingInstance = trainingRunService.findAllByTrainingInstanceId(trainingInstanceId);
            trainingRunsInTrainingInstance.forEach(tr -> trainingRunService.deleteTrainingRun(tr.getId(), true));
            if (trainingInstance.getPoolId() != null) {
                trainingInstanceService.unlockPool(trainingInstance.getPoolId());
                deleteBashCommandsfromPool(trainingInstance.getPoolId());
            }
        } else if (!trainingInstanceService.checkIfInstanceIsFinished(trainingInstanceId) && trainingRunService.existsAnyForTrainingInstance(trainingInstanceId)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstanceId,
                    "Active training instance with already assigned training runs cannot be deleted. Please delete training runs assigned to training instance and try again."));
            // not possible to delete active training instances with associated training runs
        } else if (trainingInstance.getPoolId() != null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstanceId,
                    "First, you must unassign pool id from training instance then try it again."));
            // not possible to delete training instance with associated pool
        }
        trainingInstanceService.delete(trainingInstance);
        elasticsearchApiService.deleteEventsByTrainingInstanceId(trainingInstance.getId());
    }

    private void deleteBashCommandsfromPool(Long poolId){
        try {
            elasticsearchApiService.deleteBashCommandsFromPool(poolId);
        } catch (MicroserviceApiException ignored){ }
    }

    /**
     * Assign pool in training instance new training instance
     *
     * @param trainingInstanceId              the training instance id
     * @param trainingInstanceAssignPoolIdDTO of training instance to be deleted
     * @return the training instance basic info dto
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public TrainingInstanceBasicInfoDTO assignPoolToTrainingInstance(Long trainingInstanceId, TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        if (trainingInstance.getPoolId() != null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "Training instance already contains pool Id. Please first unassign pool id and then assign another pool again."));
        }
        // lock pool and update pool
        trainingInstanceService.lockPool(trainingInstanceAssignPoolIdDTO.getPoolId());
        trainingInstance.setPoolId(trainingInstanceAssignPoolIdDTO.getPoolId());
        TrainingInstance updatedTrainingInstance = trainingInstanceService.updateTrainingInstancePool(trainingInstance);
        return trainingInstanceMapper.mapEntityToTIBasicInfo(updatedTrainingInstance);
    }

    /**
     * Reassign pool in training instance  or assignes new training instance
     *
     * @param trainingInstanceId of training instance to be deleted
     * @return the training instance basic info dto
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public TrainingInstanceBasicInfoDTO unassignPoolInTrainingInstance(Long trainingInstanceId) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        if (trainingInstance.getPoolId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "The training instance does not contain any assigned pool already."));
        }
        // unlock previously assigned pool
        trainingInstanceService.unlockPool(trainingInstance.getPoolId());
        deleteBashCommandsfromPool(trainingInstance.getPoolId());

        trainingInstance.setPoolId(null);
        TrainingInstance updatedTrainingInstance = trainingInstanceService.updateTrainingInstancePool(trainingInstance);
        return trainingInstanceMapper.mapEntityToTIBasicInfo(updatedTrainingInstance);
    }

    /**
     * Finds all Training Runs by specific Training Instance.
     *
     * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
     * @param isActive           if isActive attribute is True, only active runs are returned
     * @param pageable           pageable parameter with information about pagination.
     * @return Page of {@link TrainingRunDTO} of specific Training Instance
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Boolean isActive, Pageable pageable) {
        Page<TrainingRun> trainingRuns = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstanceId, isActive, pageable);
        PageResultResource<TrainingRunDTO> trainingRunDTOsPageResult = trainingRunMapper.mapToPageResultResource(trainingRuns);
        addParticipantsToTrainingRunDTOs(trainingRunDTOsPageResult.getContent());
        return trainingRunDTOsPageResult;
    }

    private void addParticipantsToTrainingRunDTOs(List<TrainingRunDTO> trainingRunDTOS) {
        trainingRunDTOS.forEach(trainingRunDTO ->
                trainingRunDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunDTO.getParticipantRef().getUserRefId())));
    }

    /**
     * Check if instance can be deleted.
     *
     * @param trainingInstanceId the training instance id
     * @return true if instance can be deleted, false if not and message. {@link TrainingInstanceIsFinishedInfoDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public TrainingInstanceIsFinishedInfoDTO checkIfInstanceCanBeDeleted(Long trainingInstanceId) {
        TrainingInstanceIsFinishedInfoDTO infoDTO = new TrainingInstanceIsFinishedInfoDTO();
        if (trainingInstanceService.checkIfInstanceIsFinished(trainingInstanceId)) {
            infoDTO.setHasFinished(true);
            infoDTO.setMessage("Training instance has already finished and can be safely deleted.");
        } else {
            infoDTO.setHasFinished(false);
            infoDTO.setMessage("WARNING: Training instance is still running! Are you sure you want to delete it?");
        }
        return infoDTO;
    }

    /**
     * Retrieve all organizers for given training instance .
     *
     * @param trainingInstanceId id of the training instance for which to get the organizers
     * @param pageable           pageable parameter with information about pagination.
     * @param givenName          optional parameter used for filtration
     * @param familyName         optional parameter used for filtration
     * @return returns all organizers in given training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public PageResultResource<UserRefDTO> getOrganizersOfTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        return userService.getUsersRefDTOByGivenUserIds(trainingInstance.getOrganizers().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet()), pageable, givenName, familyName);
    }

    /**
     * Retrieve all organizers not in the given training instance.
     *
     * @param trainingInstanceId id of the training instance which users should be excluded from the result list.
     * @param pageable           pageable parameter with information about pagination.
     * @param givenName          optional parameter used for filtration
     * @param familyName         optional parameter used for filtration
     * @return returns all organizers not in the given training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public PageResultResource<UserRefDTO> getOrganizersNotInGivenTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        Set<Long> excludedOrganizers = trainingInstance.getOrganizers().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        return userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, excludedOrganizers, pageable, givenName, familyName);
    }

    /**
     * Concurrently add organizers to the given training instance and remove authors from the training instance.
     *
     * @param trainingInstanceId if of the training instance to be updated
     * @param organizersAddition ids of the organizers to be added to the training instance
     * @param organizersRemoval  ids of the organizers to be removed from the training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public void editOrganizers(Long trainingInstanceId, Set<Long> organizersAddition, Set<Long> organizersRemoval) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        Long loggedInUserRefId = securityService.getUserRefIdFromUserAndGroup();
        if (organizersRemoval != null && !organizersRemoval.isEmpty()) {
            organizersRemoval.remove(loggedInUserRefId);
            trainingInstance.removeOrganizersByUserRefIds(organizersRemoval);
        }
        if (organizersAddition != null && !organizersAddition.isEmpty()) {
            addOrganizersToTrainingInstance(trainingInstance, organizersAddition);
        }
    }

}
