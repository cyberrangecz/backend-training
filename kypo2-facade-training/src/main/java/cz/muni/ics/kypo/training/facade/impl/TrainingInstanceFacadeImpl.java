package cz.muni.ics.kypo.training.facade.impl;

import java.util.Objects;

import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.TransactionalWO;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapper;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;

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
    public void update(TrainingInstanceUpdateDTO trainingInstance) {
        LOG.debug("update({})", trainingInstance);
        try {
            Objects.requireNonNull(trainingInstance);
            TrainingInstance tI = trainingInstanceMapper.mapUpdateToEntity(trainingInstance);
            //TODO why is allowed change training definition ??
            tI.setTrainingDefinition(trainingDefinitionService.findById(trainingInstance.getTrainingDefinitionId()));
            tI.setOrganizers(trainingInstanceService.findUserRefsByLogins(trainingInstance.getOrganizerLogins()));
            trainingInstanceService.update(tI);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public TrainingInstanceDTO create(TrainingInstanceCreateDTO trainingInstance) {
        LOG.debug("create({})", trainingInstance);
        try {
            Objects.requireNonNull(trainingInstance);
            TrainingInstance tI = trainingInstanceMapper.mapCreateToEntity(trainingInstance);
            tI.setTrainingDefinition(trainingDefinitionService.findById(trainingInstance.getTrainingDefinitionId()));
            tI.setId(null);
            tI.setOrganizers(trainingInstanceService.findUserRefsByLogins(trainingInstance.getOrganizerLogins()));
            return trainingInstanceMapper.mapToDTO(trainingInstanceService.create(tI));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
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
    public void allocateSandboxes(Long instanceId) {
        LOG.debug("allocateSandboxes({})", instanceId);
        try {
            trainingInstanceService.allocateSandboxes(instanceId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex.getLocalizedMessage());
        }
    }


    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Pageable pageable) {
        LOG.debug("findAllTrainingRunsByTrainingInstance({})", trainingInstanceId);
        Page<TrainingRun> trainingRuns = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstanceId, pageable);
        return trainingRunMapper.mapToPageResultResource(trainingRuns);
    }
}
