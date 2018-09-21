package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import org.springframework.data.domain.Pageable;

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
  TrainingInstanceDTO findById(long id);

  /**
   * Find all Training Instances.
   * 
   * @return all Training Instances
   */
  PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable);

  /**
   * Updates training instance
   * @param trainingInstance to be updated
   * @throws FacadeLayerException if instance is not found
   */
  void update(TrainingInstance trainingInstance) throws FacadeLayerException;

  /**
   * Creates new training instance
   * @param trainingInstance to be created
   * @return DTO of created instance
   * @throws FacadeLayerException
   */
  TrainingInstanceDTO create(TrainingInstance trainingInstance) throws FacadeLayerException;

  /**
   * Deletes specific training instance based on id
   * @param id of training instance to be deleted
   * @throws FacadeLayerException
   */
  void delete(Long id) throws FacadeLayerException;

  /**
   * Generates new password
   * @return generated password
   * @throws FacadeLayerException if password already exists
   */
  char[] generatePassword() throws FacadeLayerException;

}
