package cz.muni.ics.kypo.service.impl;

import java.util.Optional;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.TrainingRun;
import cz.muni.ics.kypo.repository.TrainingRunRepository;
import cz.muni.ics.kypo.service.TrainingRunService;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class TrainingRunServiceImpl implements TrainingRunService {

  private TrainingRunRepository trainingRunRepository;

  @Autowired
  public TrainingRunServiceImpl(TrainingRunRepository trainingRunRepository) {
    this.trainingRunRepository = trainingRunRepository;
  }


  @Override
  public Optional<TrainingRun> findById(long id) {
    try {
      return trainingRunRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
    try {
      return trainingRunRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

}
