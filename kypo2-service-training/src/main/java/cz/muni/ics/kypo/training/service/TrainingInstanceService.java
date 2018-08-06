package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.model.TrainingInstance;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingInstanceService {

  /**
   * Finds specific Training Instance by id
   * 
   * @param id of a Training Instance that would be returned
   * @return specific Training Instance by id
   */
  public Optional<TrainingInstance> findById(long id);

  /**
   * Find all Training Instances.
   * 
   * @return all Training Instances
   */
  public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable);

  public Optional<TrainingInstance> create(TrainingInstance trainingInstance);

  public Optional<TrainingInstance> update(TrainingInstance trainingInstance);

  public void delete(TrainingInstance trainingInstance);


}
