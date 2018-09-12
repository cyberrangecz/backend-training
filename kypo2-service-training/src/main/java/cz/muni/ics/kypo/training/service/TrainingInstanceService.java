package cz.muni.ics.kypo.training.service;

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
   *                                               CANNOT_BE_UPDATED cannot be updated for some reason.
   */
  public void update(TrainingInstance trainingInstance) throws ServiceLayerException;

  /**
   * deletes training instance
   * @param id of training instance
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
   *                                               CANNOT_BE_DELETED cannot be deleted for some reason.
   */
  public void delete(Long id) throws ServiceLayerException;

  /**
   * Generates password for training instance
   * @return new password
   * @throws ServiceLayerException with ErrorCode: PASSWORD_ALREADY_EXISTS given password already exists in DB.
   */
  public char[] generatePassword() throws ServiceLayerException;


}
