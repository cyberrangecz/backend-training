package cz.muni.ics.kypo.service.impl;

import java.util.Optional;

import org.hibernate.HibernateException;
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

  private TrainingInstanceRepository trainingInstanceRepository;

  @Autowired
  public TrainingInstanceServiceImpl(TrainingInstanceRepository trainingInstanceRepository) {
    this.trainingInstanceRepository = trainingInstanceRepository;
  }


  @Override
  public Optional<TrainingInstance> findById(long id) {
    try {
      return trainingInstanceRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
    try {
      return trainingInstanceRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

}
