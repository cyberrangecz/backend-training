package cz.muni.ics.kypo.service.impl;

import java.util.Optional;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.TrainingInstance;
import cz.muni.ics.kypo.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.service.TrainingInstanceService;

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

}
