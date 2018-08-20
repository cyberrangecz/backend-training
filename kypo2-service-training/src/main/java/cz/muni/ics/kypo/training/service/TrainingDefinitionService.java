package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import cz.muni.ics.kypo.exceptions.CannotBeClonedException;
import cz.muni.ics.kypo.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.model.TrainingDefinition;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingDefinitionService {

  /**
   * Finds specific Training Definition by id
   * 
   * @param id of a Training Definition that would be returned
   * @return specific Training Definition by id
   */
  public Optional<TrainingDefinition> findById(long id);

  /**
   * Find all Training Definitions.
   * 
   * @return all Training Definitions
   */
  public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable);

  /**
   * Updates Training Definition
   * @param trainingDefinition to be updated
   * @return updated definition
   * @throws ServiceLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void update(TrainingDefinition trainingDefinition) throws ServiceLayerException, CannotBeUpdatedException;

  /**
   * Creates new training definition by cloning existing one
   * @param id of definition to be cloned
   * @return cloned definition
   * @throws ServiceLayerException if training definition is not found
   * @throws CannotBeClonedException if definition status is unreleased
   */
  public Optional<TrainingDefinition> clone(Long id) throws ServiceLayerException, CannotBeClonedException;

  /**
   * Swaps level to the left
   * @param definitionId - Id of definition containing level to be swapped
   * @param levelId - Id of level to be swapped
   * @throws ServiceLayerException if training definition or level is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void swapLeft(Long definitionId, Long levelId) throws ServiceLayerException, CannotBeUpdatedException;

  /**
   * Swaps level to the right
   * @param definitionId - Id of definition containing level to be swaped
   * @param levelId - Id of level to be swaped
   * @throws ServiceLayerException if training definition or level is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void swapRight(Long definitionId, Long levelId) throws ServiceLayerException, CannotBeUpdatedException;

  /**
   * Deletes specific training definition based on id
   * @param id of definition to be deleted
   * @throws ServiceLayerException if training definition is not found
   * @throws CannotBeDeletedException if definition status is RELEASED
   */
  public void delete(Long id) throws ServiceLayerException, CannotBeDeletedException;

  /**
   * Deletes specific level based on id
   * @param definitionId - id of definition containing level to be deleted
   * @param levelId - id of level to be deleted
   * @throws ServiceLayerException if training definition or level is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void deleteOneLevel(Long definitionId, Long levelId) throws ServiceLayerException, CannotBeUpdatedException;

}
