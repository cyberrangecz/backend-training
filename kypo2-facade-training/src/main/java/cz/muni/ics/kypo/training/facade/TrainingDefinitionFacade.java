package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.CannotBeClonedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.model.InfoLevel;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingDefinitionDTO;

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
   * Find all training definition with same sandbox definition
   * @param sandboxDefinitionId - id of sandbox definition
   * @return all training definition with same sandbox definition
   */
  public PageResultResource<TrainingDefinitionDTO> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable);
  /**
   * Updates training definition
   * @param trainingDefinition to be updated
   * @return DTO of updated definition
   * @throws FacadeLayerException if training definition is not found
   * @throws CannotBeUpdatedException if definition status is not UNRELEASED
   */
  public void update(TrainingDefinition trainingDefinition) throws FacadeLayerException, CannotBeUpdatedException;

  /**
   * Creates new training definition
   * @param trainingDefinition to be created
   * @return DTO of created definition
   * @throws FacadeLayerException
   */
  public TrainingDefinitionDTO create(TrainingDefinition trainingDefinition) throws FacadeLayerException;



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
