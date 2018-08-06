package cz.muni.ics.kypo.training.service.impl;

import java.util.Optional;

import com.mysema.commons.lang.Assert;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import cz.muni.ics.kypo.training.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class TrainingInstanceServiceImpl implements TrainingInstanceService {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceServiceImpl.class);

  private TrainingInstanceRepository trainingInstanceRepository;

  @Autowired
  public TrainingInstanceServiceImpl(TrainingInstanceRepository trainingInstanceRepository) {
    this.trainingInstanceRepository = trainingInstanceRepository;
  }


  @Override
  public Optional<TrainingInstance> findById(long id) {
    LOG.debug("findById({})", id);
    try {
      return trainingInstanceRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return trainingInstanceRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Optional<TrainingInstance> create(TrainingInstance trainingInstance) {
    LOG.debug("create({})", trainingInstance);
    Assert.notNull(trainingInstance, "Input training instance must not be null");
    TrainingInstance tI = trainingInstanceRepository.save(trainingInstance);
    LOG.info("Training instance with id: " + trainingInstance.getId() + "created.");
    return Optional.of(tI);
  }

  @Override
  public Optional<TrainingInstance> update(TrainingInstance trainingInstance) {
    LOG.debug("update({})", trainingInstance);
    Assert.notNull(trainingInstance, "Input training instance must not be null");
    TrainingInstance tI = trainingInstanceRepository.saveAndFlush(trainingInstance);
    LOG.info("Training instance with id: " + trainingInstance.getId() + "updated.");
    return Optional.of(tI);
  }

  @Override
  public void delete(TrainingInstance trainingInstance) {
    LOG.debug("delete({})",trainingInstance);
    Assert.notNull(trainingInstance, "Input training instance must not be null");
    trainingInstanceRepository.delete(trainingInstance);
    LOG.info("Training instance with id: " + trainingInstance.getId() + "created.");
  }
}
