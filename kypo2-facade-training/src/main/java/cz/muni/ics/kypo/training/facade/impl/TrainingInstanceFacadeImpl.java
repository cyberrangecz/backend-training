package cz.muni.ics.kypo.training.facade.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.TransactionalWO;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import cz.muni.ics.kypo.training.mapping.BeanMapping;
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
    private BeanMapping beanMapping;

    @Autowired
    public TrainingInstanceFacadeImpl(TrainingInstanceService trainingInstanceService, TrainingDefinitionService trainingDefinitionService,
                                      BeanMapping beanMapping) {
        this.trainingInstanceService = trainingInstanceService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.beanMapping = beanMapping;
    }

    @Override
    @TransactionalRO
    public TrainingInstanceDTO findById(long id) {
        LOG.debug("findById({})", id);
        try {
            Objects.requireNonNull(id);
            return beanMapping.mapTo(trainingInstanceService.findById(id), TrainingInstanceDTO.class);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAll({},{})", predicate, pageable);
        return beanMapping.mapToPageResultDTO(trainingInstanceService.findAll(predicate, pageable), TrainingInstanceDTO.class);
    }


    @Override
    @TransactionalWO
    public String update(TrainingInstanceUpdateDTO trainingInstance) {
        LOG.debug("update({})", trainingInstance);
        try {
            Objects.requireNonNull(trainingInstance);
            TrainingInstance tI = beanMapping.mapTo(trainingInstance, TrainingInstance.class);
            tI.setTrainingDefinition(trainingDefinitionService.findById(trainingInstance.getTrainingDefinitionId()));
            Set<UserRef> organizers = new HashSet<>();
            for (Long id : trainingInstance.getOrgIds()) {
                organizers.add(trainingInstanceService.findUserRefById(id));
            }
            tI.setOrganizers(organizers);
            return trainingInstanceService.update(tI);
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
            TrainingInstance tI = beanMapping.mapTo(trainingInstance, TrainingInstance.class);
            tI.setTrainingDefinition(trainingDefinitionService.findById(trainingInstance.getTrainingDefinitionId()));
            tI.setId(null);
            Set<UserRef> organizers = new HashSet<>();
            for (Long id : trainingInstance.getOrgIds()) {
                organizers.add(trainingInstanceService.findUserRefById(id));
            }
            tI.setOrganizers(organizers);
            return beanMapping.mapTo(trainingInstanceService.create(tI), TrainingInstanceDTO.class);
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
    public ResponseEntity<Void> allocateSandboxes(Long instanceId) {
        LOG.debug("allocateSandboxes({})", instanceId);
        try {
            return trainingInstanceService.allocateSandboxes(instanceId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex.getLocalizedMessage());
        }
    }


    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Pageable pageable) {
        LOG.debug("findAllTrainingRunsByTrainingInstance({})", trainingInstanceId);
        Page<TrainingRun> trainingRuns = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstanceId, pageable);
        return beanMapping.mapToPageResultDTO(trainingRuns, TrainingRunDTO.class);
    }
}
