package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceIsFinishedInfoDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapper;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
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
        trainingInstancePageResultResource.getContent().forEach(trainingInstanceDTO ->{
            if (trainingInstanceDTO.getPoolId() != null)
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
            addOrganizersToTrainingInstance(trainingInstance, trainingInstanceUpdateDTO.getOrganizersRefIds());
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
            addOrganizersToTrainingInstance(trainingInstance, trainingInstanceCreateDTO.getOrganizersRefIds());
            return trainingInstanceMapper.mapToDTO(trainingInstanceService.create(trainingInstance));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addOrganizersToTrainingInstance(TrainingInstance trainingInstance, Set<Long> userRefIdsOfOrganizers) {
        trainingInstance.setOrganizers(new HashSet<>());
        Set<UserInfoDTO> organizers = trainingDefinitionService.getUsersWithGivenUserRefIds(userRefIdsOfOrganizers);
        for (UserInfoDTO organizer : organizers) {
            try {
                trainingInstance.addOrganizer(trainingDefinitionService.findUserByRefId(organizer.getUserRefId()));
            } catch (ServiceLayerException ex) {
                trainingInstance.addOrganizer(trainingDefinitionService.createUserRef(createUserRef(organizer)));
            }
        }
    }

    private UserRef createUserRef(UserInfoDTO userToBeCreated) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userToBeCreated.getUserRefId());
        userRef.setIss(userToBeCreated.getIss());
        userRef.setUserRefFamilyName(userToBeCreated.getFamilyName());
        userRef.setUserRefGivenName(userToBeCreated.getGivenName());
        userRef.setUserRefFullName(userToBeCreated.getFullName());
        userRef.setUserRefLogin(userToBeCreated.getLogin());
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
            return trainingRunMapper.mapToPageResultResource(trainingRuns);
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
