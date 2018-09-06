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
import cz.muni.ics.kypo.training.api.dto.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;

/**
 * @author Pavel Šeda
 *
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
  @Transactional(readOnly = true)
  public TrainingInstanceDTO findById(long id) {
    LOG.debug("findById({})", id);
    try {
      Objects.requireNonNull(id);
      Optional<TrainingInstance> trainingInstance = trainingInstanceService.findById(id);
      TrainingInstance ti = trainingInstance.orElseThrow(() -> new ServiceLayerException("TrainingInstance with this id is not found"));
      return beanMapping.mapTo(ti, TrainingInstanceDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given TrainingInstance ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(trainingInstanceService.findAll(predicate, pageable), TrainingInstanceDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public void update(TrainingInstance trainingInstance) {
    LOG.debug("update({})",trainingInstance);
    try{
      Objects.requireNonNull(trainingInstance);
      trainingInstanceService.update(trainingInstance);
    } catch (ServiceLayerException ex){
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public TrainingInstanceDTO create(TrainingInstance trainingInstance) {
    LOG.debug("create({})", trainingInstance);
    try{
      Objects.requireNonNull(trainingInstance);
      Optional<TrainingInstance> tI = trainingInstanceService.create(trainingInstance);
      TrainingInstance newTI = tI.orElseThrow(() -> new ServiceLayerException("Training definition not created"));
      return beanMapping.mapTo(newTI, TrainingInstanceDTO.class);
    } catch(NullPointerException | ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public void delete(Long id) throws FacadeLayerException{
    try {
      Objects.requireNonNull(id);
      trainingInstanceService.delete(id);
    } catch(ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional
  public char[] generateKeyword() throws FacadeLayerException {
    try {
      char[] newKeyword = trainingInstanceService.generateKeyword();
      return newKeyword;
    } catch (ServiceLayerException ex){
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }
}
