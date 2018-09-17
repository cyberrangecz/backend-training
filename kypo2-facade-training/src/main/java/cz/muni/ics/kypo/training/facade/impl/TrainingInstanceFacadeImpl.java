package cz.muni.ics.kypo.training.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingInstanceFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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
      return beanMapping.mapTo(trainingInstanceService.findById(id), TrainingInstanceDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(trainingInstanceService.findAll(predicate, pageable), TrainingInstanceDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
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
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public TrainingInstanceDTO create(TrainingInstance trainingInstance) {
    LOG.debug("create({})", trainingInstance);
    try{
      Objects.requireNonNull(trainingInstance);
      return beanMapping.mapTo(trainingInstanceService.create(trainingInstance), TrainingInstanceDTO.class);
    } catch(ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public void delete(Long id) throws FacadeLayerException{
    try {
      Objects.requireNonNull(id);
      trainingInstanceService.delete(id);
    } catch(ServiceLayerException ex) {
      throw new FacadeLayerException(ex);
    }
  }

  @Override
  @Transactional
  public char[] generatePassword() throws FacadeLayerException {
    try {
      char[] newPassword = trainingInstanceService.generatePassword();
      return newPassword;
    } catch (ServiceLayerException ex){
      throw new FacadeLayerException(ex);
    }
  }
}
