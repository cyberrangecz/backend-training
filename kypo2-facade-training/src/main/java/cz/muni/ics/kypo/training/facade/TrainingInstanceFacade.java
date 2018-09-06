package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingInstanceDTO;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingInstanceFacade {

  /**
   * Finds specific Training Instance by id
   * 
   * @param id of a Training Instance that would be returned
   * @return specific Training Instance by id
   */
  public TrainingInstanceDTO findById(long id);

  /**
   * Find all Training Instances.
   * 
   * @return all Training Instances
   */
  public PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable);

  /**
   * Updates training instance
   * @param trainingInstance to be updated
   * @throws FacadeLayerException if instance is not found
   * @throws CannotBeUpdatedException if starting date of instance is not in future
   */
  public void update(TrainingInstance trainingInstance) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * Creates new training instance
   * @param trainingInstance to be created
   * @return DTO of created instance
   * @throws FacadeLayerException
   */
  public TrainingInstanceDTO create(TrainingInstance trainingInstance) throws FacadeLayerException;

  /**
   * Deletes specific training instance based on id
   * @param id of training instance to be deleted
   * @throws FacadeLayerException
   */
  public void delete(Long id) throws FacadeLayerException;

  /**
   * Generates new password
   * @return generated password
   * @throws FacadeLayerException if password already exists
   */
  public char[] generatePassword() throws FacadeLayerException;

}
