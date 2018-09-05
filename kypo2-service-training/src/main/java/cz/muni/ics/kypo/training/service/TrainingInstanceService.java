package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import cz.muni.ics.kypo.training.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
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

  /**
   * Creates new training instance
   * @param trainingInstance to be created
   * @return created instance
   */
  public Optional<TrainingInstance> create(TrainingInstance trainingInstance);

  /**
   * Updates training instance
   * @param trainingInstance to be updated
   * @return updated instance
   */
  public Optional<TrainingInstance> update(TrainingInstance trainingInstance);

  /**
   * Deletes instance
   * @param id of instance to be deleted
   */
  public void delete(Long id) throws CannotBeDeletedException, ServiceLayerException;

  /**
   * Generates keyword for training instance
   * @return new keyword
   * @throws ServiceLayerException if keyword already exists
   */
  public char[] generateKeyword() throws ServiceLayerException;


}
