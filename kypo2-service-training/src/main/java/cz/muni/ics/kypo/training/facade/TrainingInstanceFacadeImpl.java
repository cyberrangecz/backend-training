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
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapper;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.impl.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrainingInstanceFacadeImpl implements TrainingInstanceFacade {

    private TrainingInstanceService trainingInstanceService;
    private TrainingDefinitionService trainingDefinitionService;
    private TrainingInstanceMapper trainingInstanceMapper;
    private TrainingRunMapper trainingRunMapper;
    private UserService userService;
    private SecurityService securityService;

    @Autowired
    public TrainingInstanceFacadeImpl(TrainingInstanceService trainingInstanceService, TrainingDefinitionService trainingDefinitionService,
                                      TrainingInstanceMapper trainingInstanceMapper, TrainingRunMapper trainingRunMapper, UserService userService,
                                      SecurityService securityService) {
        this.trainingInstanceService = trainingInstanceService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingInstanceMapper = trainingInstanceMapper;
        this.trainingRunMapper = trainingRunMapper;
        this.userService = userService;
        this.securityService = securityService;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TransactionalRO
    public TrainingInstanceDTO findById(Long id) {
        return trainingInstanceMapper.mapToDTO(trainingInstanceService.findByIdIncludingDefinition(id));
    }

    @Override
    @IsOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingInstanceFindAllResponseDTO> findAll(Predicate predicate, Pageable pageable) {
        return trainingInstanceMapper.mapToPageResultResourceBasicView(trainingInstanceService.findAll(predicate, pageable));
    }

    @Override
    @IsOrganizerOrAdmin
    @TransactionalWO
    public String update(TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
        TrainingInstance trainingInstance = trainingInstanceMapper.mapUpdateToEntity(trainingInstanceUpdateDTO);
        trainingInstance.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceUpdateDTO.getTrainingDefinitionId()));
        return trainingInstanceService.update(trainingInstance);
    }

    @Override
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
            Set<Long> actualOrganizersIds = trainingInstance.getOrganizers().stream().map(UserRef::getUserRefId).collect(Collectors.toSet());
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

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TransactionalWO
    public void delete(Long id) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(id);
        trainingInstanceService.delete(trainingInstance);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TransactionalWO
    public TrainingInstanceBasicInfoDTO assignPoolToTrainingInstance(TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceAssignPoolIdDTO.getId());
        // lock pool and update pool
        LockedPoolInfo lockedPoolInfo = trainingInstanceService.lockPool(trainingInstanceAssignPoolIdDTO.getId());
        trainingInstance.setId(lockedPoolInfo.getPool());
        TrainingInstance updatedTrainingInstance = trainingInstanceService.assignPoolToTrainingInstance(trainingInstance);
        return trainingInstanceMapper.mapEntityToTIBasicInfo(updatedTrainingInstance);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TransactionalWO
    public TrainingInstanceBasicInfoDTO reassignPoolToTrainingInstance(TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceAssignPoolIdDTO.getId());
        // lock newly assigned pool
        trainingInstanceService.lockPool(trainingInstanceAssignPoolIdDTO.getPoolId());
        // unlock previously assigned pool
        trainingInstanceService.unlockPool(trainingInstance.getId());

        trainingInstance.setPoolId(trainingInstanceAssignPoolIdDTO.getPoolId());
        TrainingInstance updatedTrainingInstance = trainingInstanceService.assignPoolToTrainingInstance(trainingInstance);
        return trainingInstanceMapper.mapEntityToTIBasicInfo(updatedTrainingInstance);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
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

    @Override
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

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TransactionalRO
    public PageResultResource<UserRefDTO> getOrganizersOfTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        return userService.getUsersRefDTOByGivenUserIds(trainingInstance.getOrganizers().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet()), pageable, givenName, familyName);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TransactionalRO
    public PageResultResource<UserRefDTO> getOrganizersNotInGivenTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        Set<Long> excludedOrganizers = trainingInstance.getOrganizers().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        return userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, excludedOrganizers, pageable, givenName, familyName);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
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
