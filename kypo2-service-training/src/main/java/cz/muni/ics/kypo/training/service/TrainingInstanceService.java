package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
   *
   */
  public TrainingInstance findById(long id) throws ServiceLayerException;

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
  public TrainingInstance create(TrainingInstance trainingInstance);

  /**
   * updates training instance
   * @param trainingInstance to be updated
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
   *                                               RESOURCE_CONFLICT cannot be updated for some reason.
   */
  public void update(TrainingInstance trainingInstance) throws ServiceLayerException;

  /**
   * deletes training instance
   * @param id of training instance
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
   *                                               RESOURCE_CONFLICT cannot be deleted for some reason.
   */
  public void delete(Long id) throws ServiceLayerException;

  /**
   * Generates password for training instance
   * @return new password
   * @throws ServiceLayerException with ErrorCode: RESOURCE_CONFLICT given password already exists in DB.
   */
  public char[] generatePassword() throws ServiceLayerException;


}
