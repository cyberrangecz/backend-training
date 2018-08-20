package cz.muni.ics.kypo.facade.impl;

import java.util.Objects;
import java.util.Optional;

import cz.muni.ics.kypo.exceptions.CannotBeClonedException;
import cz.muni.ics.kypo.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.exceptions.CannotBeUpdatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.TrainingDefinition;
import cz.muni.ics.kypo.service.TrainingDefinitionService;


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
  public void update(TrainingDefinition trainingDefinition) throws FacadeLayerException, CannotBeUpdatedException {
    LOG.debug("update({})", trainingDefinition);
    try {
      Objects.requireNonNull(trainingDefinition);
      trainingDefinitionService.update(trainingDefinition);
    } catch (NullPointerException | ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public TrainingDefinitionDTO clone(Long id) throws FacadeLayerException, CannotBeClonedException {
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

  @Override
  @Transactional
  public void swapLeft(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException {
    LOG.debug("swapLeft({},{})", definitionId, levelId);
    try{
      Objects.requireNonNull(definitionId);
      Objects.requireNonNull(levelId);
      trainingDefinitionService.swapLeft(definitionId,levelId);
    } catch (NullPointerException | ServiceLayerException ex){
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public void swapRight(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException {
    LOG.debug("swapRight({},{})", definitionId, levelId);
    try{
      Objects.requireNonNull(definitionId);
      Objects.requireNonNull(levelId);
      trainingDefinitionService.swapRight(definitionId,levelId);
    } catch (NullPointerException | ServiceLayerException ex){
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public void delete(Long id) throws FacadeLayerException, CannotBeDeletedException {
    LOG.debug("delete({})", id);
    try{
      Objects.requireNonNull(id);
      trainingDefinitionService.delete(id);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public void deleteOneLevel(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException {
    LOG.debug("deleteOneLevel({}, {})", definitionId, levelId);
    try {
      Objects.requireNonNull(definitionId);
      Objects.requireNonNull(levelId);
      trainingDefinitionService.deleteOneLevel(definitionId, levelId);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }
}
