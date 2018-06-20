package cz.muni.ics.kypo.service.impl;

import java.util.Optional;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.TrainingDefinition;
import cz.muni.ics.kypo.repository.TrainingDefinitionRepository;
import cz.muni.ics.kypo.service.TrainingDefinitionService;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class TrainingDefinitionServiceImpl implements TrainingDefinitionService {

  private TrainingDefinitionRepository trainingDefinitionRepository;

  @Autowired
  public TrainingDefinitionServiceImpl(TrainingDefinitionRepository trainingDefinitionRepository) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
  }

  @Override
  public Optional<TrainingDefinition> findById(long id) {
    try {
      return trainingDefinitionRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable) {
    try {
      return trainingDefinitionRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

}
