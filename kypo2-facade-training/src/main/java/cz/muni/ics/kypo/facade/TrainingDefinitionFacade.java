package cz.muni.ics.kypo.facade;

import cz.muni.ics.kypo.api.dto.*;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.CannotBeClonedException;
import cz.muni.ics.kypo.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.model.*;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingDefinitionFacade {

  /**
   * Finds specific Training Definition by id
   * 
   * @param id of a Training Definition that would be returned
   * @return specific Training Definition by id
   */
  public TrainingDefinitionDTO findById(long id);

  /**
   * Find all Training Definitions.
   * 
   * @return all Training Definitions
   */
  public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable);

  /**
   * Updates training definition
   * @param trainingDefinition to be updated
   * @return DTO of updated definition
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void update(TrainingDefinition trainingDefinition) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * Clones Training Definition by id
   * @param id of definition to be cloned
   * @return DTO of cloned definition
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeClonedException if definition status is unreleased
   */
  public TrainingDefinitionDTO clone(Long id) throws FacadeLayerException, CannotBeClonedException;

  /**
   * swaps level to the left
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   * @throws FacadeLayerException if training definition or level is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void swapLeft(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * swaps level to the right
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   * @throws FacadeLayerException if training definition or level is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void swapRight(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * Deletes specific training instance based on id
   * @param id of definition to be deleted
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeDeletedException if definition status is RELEASED
   */
  public void delete(Long id) throws FacadeLayerException, CannotBeDeletedException;

  /**
   * deletes specific level by id
   * @param definitionId - id of definition containing level to be deleted
   * @param levelId - id of level to be deleted
   * @throws FacadeLayerException if training definition or level is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void deleteOneLevel(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * updates game level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param gameLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void updateGameLevel(Long definitionId, GameLevel gameLevel) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * updates info level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param infoLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void updateInfoLevel(Long definitionId, InfoLevel infoLevel) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * updates assessment level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param assessmentLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * creates new info level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param infoLevel to be created
   * @return DTO of new info level
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public InfoLevelDTO createInfoLevel(Long definitionId, InfoLevel infoLevel) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * creates new game level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param gameLevel to be created
   * @return DTO of new game level
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public GameLevelDTO createGameLevel(Long definitionId, GameLevel gameLevel) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * creates new assessment level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param assessmentLevel to be created
   * @return DTO of new assessment level
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public AssessmentLevelDTO createAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws FacadeLayerException, CannotBeUpdatedException;

}
