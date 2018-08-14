package cz.muni.ics.kypo.training.service.impl;

import java.util.Optional;

import com.mysema.commons.lang.Assert;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.repository.TrainingDefinitionRepository;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class TrainingDefinitionServiceImpl implements TrainingDefinitionService {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionServiceImpl.class);

  private TrainingDefinitionRepository trainingDefinitionRepository;

  @Autowired
  public TrainingDefinitionServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
  }

  @Override
  public Optional<TrainingDefinition> findById(long id) {
    LOG.debug("findById({})", id);
    try {
      return trainingDefinitionRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return trainingDefinitionRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Optional<TrainingDefinition> update(TrainingDefinition trainingDefinition) {
    LOG.debug("update({})", trainingDefinition);
    Assert.notNull(trainingDefinition, "Input training definition must not be null");
    TrainingDefinition tD = trainingDefinitionRepository.saveAndFlush(trainingDefinition);
    LOG.info("Training definition with id: " + trainingDefinition.getId() + " updated");
    return Optional.of(tD);
  }

  @Override
  public Optional<TrainingDefinition> clone(TrainingDefinition trainingDefinition) {
    LOG.debug("clone({})", trainingDefinition);
    Assert.notNull(trainingDefinition, "Input training definition must not be null");
    TrainingDefinition tD = new TrainingDefinition();
    BeanUtils.copyProperties(trainingDefinition, tD);
    tD.setId(null);
    tD = trainingDefinitionRepository.save(tD);
    LOG.info("Training definition with id: "+ trainingDefinition.getId() +" cloned.");
    return Optional.of(tD);
  }

}
