package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceIsFinishedInfoDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapper;
import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Šeda
 * @author Boris Jadus
 */
@Service
public class TrainingInstanceFacadeImpl implements TrainingInstanceFacade {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceFacadeImpl.class);

    private TrainingInstanceService trainingInstanceService;
    private TrainingDefinitionService trainingDefinitionService;
    private TrainingInstanceMapper trainingInstanceMapper;
    private TrainingRunMapper trainingRunMapper;

    @Autowired
    public TrainingInstanceFacadeImpl(TrainingInstanceService trainingInstanceService, TrainingDefinitionService trainingDefinitionService,
                                      TrainingInstanceMapper trainingInstanceMapper, TrainingRunMapper trainingRunMapper) {
        this.trainingInstanceService = trainingInstanceService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingInstanceMapper = trainingInstanceMapper;
        this.trainingRunMapper = trainingRunMapper;
    }

    @Override
    @TransactionalRO
    public TrainingInstanceDTO findById(Long id) {
        try {
            Objects.requireNonNull(id);
            TrainingInstanceDTO trainingInstanceDTO = trainingInstanceMapper.mapToDTO(trainingInstanceService.findByIdIncludingDefinition(id));
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
        trainingInstancePageResultResource.getContent().forEach(trainingInstanceDTO -> trainingInstanceDTO.setSandboxesWithTrainingRun(trainingInstanceService.findIdsOfAllOccupiedSandboxesByTrainingInstance(trainingInstanceDTO.getId())));
        return trainingInstancePageResultResource;
    }

    @Override
    @TransactionalWO
    public String update(TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
        try {
            Objects.requireNonNull(trainingInstanceUpdateDTO);
            TrainingInstance trainingInstance = trainingInstanceMapper.mapUpdateToEntity(trainingInstanceUpdateDTO);
            trainingInstance.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceUpdateDTO.getTrainingDefinitionId()));
            addOrganizersToTrainingInstance(trainingInstance, trainingInstanceUpdateDTO.getOrganizersLogin());
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
            addOrganizersToTrainingInstance(trainingInstance, trainingInstanceCreateDTO.getOrganizersLogin());
            return trainingInstanceMapper.mapToDTO(trainingInstanceService.create(trainingInstance));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addOrganizersToTrainingInstance(TrainingInstance trainingInstance, Set<String> loginsOfOrganizers) {
        trainingInstance.setOrganizers(new HashSet<>());
        Set<UserInfoDTO> organizers = trainingDefinitionService.getUsersWithGivenLogins(loginsOfOrganizers);
        for (UserInfoDTO organizer : organizers) {
            try {
                trainingInstance.addOrganizer(trainingDefinitionService.findUserRefByLogin(organizer.getLogin()));
            } catch (ServiceLayerException ex) {
                UserRef userRef = new UserRef();
                userRef.setUserRefLogin(organizer.getLogin());
                userRef.setUserRefFullName(organizer.getFullName());
                userRef.setUserRefFamilyName(organizer.getFamilyName());
                userRef.setUserRefGivenName(organizer.getGivenName());
                trainingInstance.addOrganizer(trainingDefinitionService.createUserRef(userRef));
            }
        }
    }

    @Override
    @TransactionalWO
    public void delete(Long id) {
        try {
            Objects.requireNonNull(id);
            TrainingInstance trainingInstance = trainingInstanceService.findById(id);
            trainingInstanceService.synchronizeSandboxesWithPythonApi(trainingInstance);
            trainingInstanceService.delete(trainingInstance);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public Long createPoolForSandboxes(Long instanceId) {
        try {
            return trainingInstanceService.createPoolForSandboxes(instanceId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void allocateSandboxes(Long instanceId, Integer count) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(instanceId);
        //Check if pool exist
        if (trainingInstance.getPoolId() == null) {
            throw new FacadeLayerException(new ServiceLayerException("Pool for sandboxes is not created yet. Please create pool before allocating sandboxes.", ErrorCode.RESOURCE_CONFLICT));
        }
        trainingInstanceService.synchronizeSandboxesWithPythonApi(trainingInstance);
        //Check if sandbox can be allocated
        if (trainingInstance.getSandboxInstanceRefs().size() >= trainingInstance.getPoolSize()) {
            throw new FacadeLayerException(new ServiceLayerException("Pool of sandboxes of training instance with id: " + trainingInstance.getId() + " is full. " +
                    "Some sandboxes may be in the state DELETE_IN_PROGRESS right now, please wait a minute and try again or contact the administrator if you are sure that the pool is not full and you still get this error.", ErrorCode.RESOURCE_CONFLICT));
        }
        trainingInstanceService.allocateSandboxes(trainingInstance, count);
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
            return trainingRunMapper.mapToPageResultResource(trainingRuns);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    public void reallocateSandbox(Long instanceId, Long sandboxId) {
        try {
            TrainingInstance trainingInstance = trainingInstanceService.findById(instanceId);
            SandboxInstanceRef sandboxRefToDelete = trainingInstance.getSandboxInstanceRefs().stream().filter(sIR ->
                    sIR.getSandboxInstanceRef().equals(sandboxId)).findFirst().orElseThrow(() -> new FacadeLayerException(new ServiceLayerException("Given sandbox with id: " + sandboxId
                    + " is not in DB or is not assigned to given training instance.", ErrorCode.RESOURCE_NOT_FOUND)));
            trainingInstanceService.deleteSandbox(trainingInstance.getId(), sandboxRefToDelete.getSandboxInstanceRef());
            trainingInstanceService.allocateSandboxes(trainingInstance, 1);
            //Check if sandbox can be allocated
            if (trainingInstance.getSandboxInstanceRefs().size() >= trainingInstance.getPoolSize()) {
                throw new FacadeLayerException(new ServiceLayerException("Sandbox cannot be reallocated because pool of training instance with id: " + trainingInstance.getId() + " is full. " +
                        "Given sandbox with id: " + sandboxId + " is probably in the process of removing right now. Please wait and try allocate new sandbox later or contact administrator.", ErrorCode.RESOURCE_CONFLICT));
            }
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
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
}
