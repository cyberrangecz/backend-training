package cz.muni.ics.kypo.training.facade.impl;

import java.util.Objects;

import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private BeanMapping beanMapping;

    @Autowired
    public TrainingInstanceFacadeImpl(TrainingInstanceService trainingInstanceService, BeanMapping beanMapping) {
        this.trainingInstanceService = trainingInstanceService;
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
            TrainingInstance UpdatedTrainingInstance = beanMapping.mapTo(trainingInstance, TrainingInstance.class);
            trainingInstanceService.update(UpdatedTrainingInstance);
            if (!trainingInstance.getKeyword().isEmpty()) {
                String newKeyword = trainingInstanceService.generatePassword(UpdatedTrainingInstance, trainingInstance.getKeyword());
                return newKeyword;
            }
            return null;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public TrainingInstanceCreateResponseDTO create(TrainingInstanceCreateDTO trainingInstance) {
        LOG.debug("create({})", trainingInstance);
        try {
            Objects.requireNonNull(trainingInstance);
            TrainingInstance newTI = trainingInstanceService.create(beanMapping.mapTo(trainingInstance, TrainingInstance.class));
            TrainingInstanceCreateResponseDTO newTIDTO = beanMapping.mapTo(newTI, TrainingInstanceCreateResponseDTO.class);
            String newKeyword = trainingInstanceService.generatePassword(newTI, trainingInstance.getKeyword());
            newTIDTO.setKeyword(newKeyword);
            return newTIDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void delete(Long id) throws FacadeLayerException {
        try {
            Objects.requireNonNull(id);
            trainingInstanceService.delete(id);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public ResponseEntity<Void> allocateSandboxes(Long instanceId) throws FacadeLayerException {
        LOG.debug("allocateSandboxes({})", instanceId);
        try {
            return trainingInstanceService.allocateSandboxes(instanceId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex.getLocalizedMessage());
        }
    }
}
