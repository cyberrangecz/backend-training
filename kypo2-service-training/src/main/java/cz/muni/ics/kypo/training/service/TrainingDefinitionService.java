package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import cz.muni.ics.kypo.training.exceptions.CannotBeClonedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.model.InfoLevel;
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

  /**
   * Updates game level in training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param gameLevel to be updated
   * @throws ServiceLayerException if training definition or level in not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void updateGameLevel(Long definitionId, GameLevel gameLevel) throws ServiceLayerException, CannotBeUpdatedException;

  /**
   * Updates info level in training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param infoLevel to be updated
   * @throws ServiceLayerException if training definition or level in not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void updateInfoLevel(Long definitionId, InfoLevel infoLevel) throws ServiceLayerException, CannotBeUpdatedException;

  /**
   * Updates assessment level in training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param assessmentLevel to be updated
   * @throws ServiceLayerException if training definition or level in not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws ServiceLayerException, CannotBeUpdatedException;

  /**
   * Creates new game level
   * @param definitionId - id of definition in which level will be created
   * @param gameLevel to be created
   * @return new game level
   * @throws ServiceLayerException if definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public Optional<GameLevel> createGameLevel(Long definitionId, GameLevel gameLevel) throws ServiceLayerException, CannotBeUpdatedException;


  /**
   * Creates new info level
   * @param definitionId - id of definition in which level will be created
   * @param infoLevel to be created
   * @return new info level
   * @throws ServiceLayerException if definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public Optional<InfoLevel> createInfoLevel(Long definitionId, InfoLevel infoLevel) throws ServiceLayerException, CannotBeUpdatedException;

  /**
   * Creates new assessment level
   * @param definitionId - id of definition in which level will be created
   * @param assessmentLevel to be created
   * @return new assessment level
   * @throws ServiceLayerException if definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public Optional<AssessmentLevel> createAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws ServiceLayerException, CannotBeUpdatedException;


}
