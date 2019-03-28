package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Šeda
 */
@Service
@Transactional
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
        LOG.debug("findById({})", id);
        try {
            Objects.requireNonNull(id);
            return trainingInstanceMapper.mapToDTO(trainingInstanceService.findById(id));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAllTrainingDefinitions({},{})", predicate, pageable);
        return trainingInstanceMapper.mapToPageResultResource(trainingInstanceService.findAll(predicate, pageable));
    }


    @Override
    @TransactionalWO
    public String update(TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
        LOG.debug("update({})", trainingInstanceUpdateDTO);
        try {
            Objects.requireNonNull(trainingInstanceUpdateDTO);
            TrainingInstance trainingInstance = trainingInstanceMapper.mapUpdateToEntity(trainingInstanceUpdateDTO);
            addOrganizersToTrainingInstance(trainingInstance, trainingInstanceUpdateDTO.getOrganizers());
            return trainingInstanceService.update(trainingInstance);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public TrainingInstanceDTO create(TrainingInstanceCreateDTO trainingInstanceCreateDTO) {
        LOG.debug("create({})", trainingInstanceCreateDTO);
        try {
            Objects.requireNonNull(trainingInstanceCreateDTO);
            TrainingInstance trainingInstance = trainingInstanceMapper.mapCreateToEntity(trainingInstanceCreateDTO);
            trainingInstance.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceCreateDTO.getTrainingDefinitionId()));
            trainingInstance.setId(null);
            addOrganizersToTrainingInstance(trainingInstance, trainingInstanceCreateDTO.getOrganizers());
            return trainingInstanceMapper.mapToDTO(trainingInstanceService.create(trainingInstance));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addOrganizersToTrainingInstance(TrainingInstance trainingInstance, Set<UserInfoDTO> organizers) {
        trainingInstance.setOrganizers(new HashSet<>());
        for (UserInfoDTO organizer : organizers) {
            try {
                trainingInstance.addOrganizer(trainingDefinitionService.findUserRefByLogin(organizer.getLogin()));
            } catch (ServiceLayerException ex) {
                UserRef userRef = new UserRef();
                userRef.setUserRefLogin(organizer.getLogin());
                userRef.setUserRefFullName(organizer.getFullName());
                trainingInstance.addOrganizer(trainingDefinitionService.createUserRef(userRef));
            }
        }
    }

    @Override
    @TransactionalWO
    public void delete(Long id) {
        try {
            Objects.requireNonNull(id);
            trainingInstanceService.delete(id);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public Long createPoolForSandboxes(Long instanceId) {
        LOG.debug("createPoolForSandboxes({})", instanceId);
        try {
            return trainingInstanceService.createPoolForSandboxes(instanceId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void allocateSandboxes(Long instanceId) {
        LOG.debug("allocateSandboxes({})", instanceId);
        try {
            trainingInstanceService.allocateSandboxes(instanceId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Pageable pageable) {
        LOG.debug("findAllTrainingRunsByTrainingInstance({})", trainingInstanceId);
        try{
            Page<TrainingRun> trainingRuns = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstanceId, pageable);
            return trainingRunMapper.mapToPageResultResource(trainingRuns);
        }catch(ServiceLayerException ex){
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void deleteSandboxes(Long instanceId, Set<Long> sandboxIds) {
        LOG.debug("deleteFailedSandboxes({}, {})", instanceId, sandboxIds);
        try{
            trainingInstanceService.deleteSandboxes(instanceId, sandboxIds);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void reallocateSandbox(Long instanceId, Long sandboxId){
        LOG.debug("reallocateSandboxes({}, {})", instanceId, sandboxId);
        try{
            trainingInstanceService.reallocateSandbox(instanceId, sandboxId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }
}
