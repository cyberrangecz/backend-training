package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceIsFinishedInfoDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
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
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Pavel Šeda
 * @author Boris Jadus
 */
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
    @TransactionalRO
    public TrainingInstanceDTO findById(Long id) {
        try {
            Objects.requireNonNull(id);
            TrainingInstanceDTO trainingInstanceDTO = trainingInstanceMapper.mapToDTO(trainingInstanceService.findByIdIncludingDefinition(id));
            if (trainingInstanceDTO.getPoolId() != null)
                trainingInstanceDTO.setSandboxesWithTrainingRun(trainingInstanceService.findIdsOfAllOccupiedSandboxesByTrainingInstance(trainingInstanceDTO.getId()));
            return trainingInstanceDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable) {
        PageResultResource<TrainingInstanceDTO> trainingInstancePageResultResource = trainingInstanceMapper.mapToPageResultResource(trainingInstanceService.findAll(predicate, pageable));
        trainingInstancePageResultResource.getContent().forEach(trainingInstanceDTO -> {
            trainingInstanceDTO.setSandboxesWithTrainingRun(trainingInstanceService.findIdsOfAllOccupiedSandboxesByTrainingInstance(trainingInstanceDTO.getId()));
        });
        return trainingInstancePageResultResource;
    }

    @Override
    @TransactionalWO
    public String update(TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
        try {
            Objects.requireNonNull(trainingInstanceUpdateDTO);
            TrainingInstance trainingInstance = trainingInstanceMapper.mapUpdateToEntity(trainingInstanceUpdateDTO);
            trainingInstance.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceUpdateDTO.getTrainingDefinitionId()));
            return trainingInstanceService.update(trainingInstance);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public TrainingInstanceDTO create(TrainingInstanceCreateDTO trainingInstanceCreateDTO) {
        try {
            Objects.requireNonNull(trainingInstanceCreateDTO);
            TrainingInstance trainingInstance = trainingInstanceMapper.mapCreateToEntity(trainingInstanceCreateDTO);
            trainingInstance.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceCreateDTO.getTrainingDefinitionId()));
            trainingInstance.setId(null);
            return trainingInstanceMapper.mapToDTO(trainingInstanceService.create(trainingInstance));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addOrganizersToTrainingInstance(TrainingInstance trainingInstance, Set<Long> userRefIdsOfOrganizers) {
        if(userRefIdsOfOrganizers.isEmpty()) return;
        PageResultResource<UserRefDTO> organizers;
        int page = 0;
        do {
            organizers = userService.getUsersRefDTOByGivenUserIds(userRefIdsOfOrganizers, PageRequest.of(page,999), null, null);
            Set<Long> actualOrganizersIds = trainingInstance.getOrganizers().stream().map(UserRef::getUserRefId).collect(Collectors.toSet());
            page++;
            for (UserRefDTO organizer : organizers.getContent()) {
                if(actualOrganizersIds.contains(organizer.getUserRefId())) {
                    continue;
                }
                try {
                    trainingInstance.addOrganizer(userService.getUserByUserRefId(organizer.getUserRefId()));
                } catch (ServiceLayerException ex) {
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
    @TransactionalWO
    public void delete(Long id) {
        try {
            Objects.requireNonNull(id);
            TrainingInstance trainingInstance = trainingInstanceService.findById(id);
            trainingInstanceService.delete(trainingInstance);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void allocateSandboxes(Long instanceId, Integer count) {
        try{
            TrainingInstance trainingInstance = trainingInstanceService.findById(instanceId);
            trainingInstanceService.allocateSandboxes(trainingInstance, count);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void deleteSandboxes(Long instanceId, Set<Long> sandboxIds) {
        try {
            TrainingInstance trainingInstance = trainingInstanceService.findById(instanceId);
            for (Long idOfSandboxToDelete : sandboxIds) {
                trainingInstanceService.deleteSandbox(trainingInstance.getId(), idOfSandboxToDelete);
            }
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Boolean isActive, Pageable pageable) {
        try {
            Page<TrainingRun> trainingRuns = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstanceId, isActive, pageable);
            PageResultResource<TrainingRunDTO> trainingRunDTOsPageResult = trainingRunMapper.mapToPageResultResource(trainingRuns);
            addParticipantsToTrainingRunDTOs(trainingRunDTOsPageResult.getContent());
            return trainingRunDTOsPageResult;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addParticipantsToTrainingRunDTOs(List<TrainingRunDTO> trainingRunDTOS) {
        trainingRunDTOS.forEach(trainingRunDTO ->
            trainingRunDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunDTO.getParticipantRef().getUserRefId())));
    }

    @Override
    @TransactionalRO
    public TrainingInstanceIsFinishedInfoDTO checkIfInstanceCanBeDeleted(Long trainingInstanceId) {
        TrainingInstanceIsFinishedInfoDTO infoDTO = new TrainingInstanceIsFinishedInfoDTO();
        if (trainingInstanceService.checkIfInstanceIsFinished(trainingInstanceId)){
            infoDTO.setHasFinished(true);
            infoDTO.setMessage("Training instance has already finished and can be safely deleted.");
        }else{
            infoDTO.setHasFinished(false);
            infoDTO.setMessage("WARNING: Training instance is still running! Are you sure you want to delete it?");
        }
        return infoDTO;
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserRefDTO> getOrganizersOfTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
        try {
            TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
            return userService.getUsersRefDTOByGivenUserIds(trainingInstance.getOrganizers().stream().map(UserRef::getUserRefId).collect(Collectors.toSet()), pageable, givenName, familyName);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserRefDTO> getOrganizersNotInGivenTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
        try {
            TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
            Set<Long> excludedOrganizers = trainingInstance.getOrganizers().stream().map(UserRef::getUserRefId).collect(Collectors.toSet());
            return userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, excludedOrganizers, pageable, givenName, familyName);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void editOrganizers(Long trainingInstanceId, Set<Long> organizersAddition, Set<Long> organizersRemoval) throws FacadeLayerException {
        try {
            TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
            Long loggedInUserRefId = securityService.getUserRefIdFromUserAndGroup();
            if(organizersRemoval != null && !organizersRemoval.isEmpty()) {
                organizersRemoval.remove(loggedInUserRefId);
                trainingInstance.removeOrganizersByUserRefIds(organizersRemoval);
            }
            if(organizersAddition != null && !organizersAddition.isEmpty()) {
                addOrganizersToTrainingInstance(trainingInstance, organizersAddition);
            }
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }
}
