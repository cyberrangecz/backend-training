package cz.muni.ics.kypo.training.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class TrainingDefinitionFacadeImpl implements TrainingDefinitionFacade {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionFacadeImpl.class);

  private TrainingDefinitionService trainingDefinitionService;
  private BeanMapping beanMapping;

  @Autowired
  public TrainingDefinitionFacadeImpl(TrainingDefinitionService trainingDefinitionService, BeanMapping beanMapping) {
    this.trainingDefinitionService = trainingDefinitionService;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)
  public TrainingDefinitionDTO findById(long id) {
    LOG.debug("findById({})", id);
    try {
      Objects.requireNonNull(id);
      Optional<TrainingDefinition> trainingDef = trainingDefinitionService.findById(id);
      TrainingDefinition td = trainingDef.orElseThrow(() -> new ServiceLayerException("TrainingDefinition with this id is not found"));
      return beanMapping.mapTo(td, TrainingDefinitionDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given TrainingDefinition ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(trainingDefinitionService.findAll(predicate, pageable), TrainingDefinitionDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public TrainingDefinitionDTO update(TrainingDefinition trainingDefinition) {
    LOG.debug("update({})", trainingDefinition);
    try {
      Objects.requireNonNull(trainingDefinition);
      Optional<TrainingDefinition> tD = trainingDefinitionService.update(trainingDefinition);
      TrainingDefinition updatedTD = tD.orElseThrow(() -> new ServiceLayerException());
      return beanMapping.mapTo(updatedTD, TrainingDefinitionDTO.class);
    } catch (NullPointerException | ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public TrainingDefinitionDTO clone(Long id) throws FacadeLayerException {
    LOG.debug("clone({})", id);
    try {
      Objects.requireNonNull(id);
      Optional<TrainingDefinition> tD = trainingDefinitionService.clone(id);
      TrainingDefinition clonedTD = tD.orElseThrow(() -> new ServiceLayerException());
      return beanMapping.mapTo(clonedTD, TrainingDefinitionDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
