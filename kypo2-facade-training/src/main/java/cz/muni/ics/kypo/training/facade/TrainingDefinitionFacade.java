package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
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
   * Updates training definition
   * @param trainingDefinition to be updated
   * @return DTO of updated definition
   * @throws FacadeLayerException if training definition is not found
   */
  public void update(TrainingDefinition trainingDefinition) throws FacadeLayerException;

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
   */
  public TrainingDefinitionDTO clone(Long id) throws FacadeLayerException;

  /**
   * swaps level to the left
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   * @throws FacadeLayerException if training definition or level is not found
   */
  public void swapLeft(Long definitionId, Long levelId) throws FacadeLayerException;

  /**
   * swaps level to the right
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   * @throws FacadeLayerException if training definition or level is not found
   */
  public void swapRight(Long definitionId, Long levelId) throws FacadeLayerException;

  /**
   * Deletes specific training instance based on id
   * @param id of definition to be deleted
   * @throws FacadeLayerException if training definition is not found
   */
  public void delete(Long id) throws FacadeLayerException;

  /**
   * deletes specific level by id
   * @param definitionId - id of definition containing level to be deleted
   * @param levelId - id of level to be deleted
   * @throws FacadeLayerException if training definition or level is not found
   */
  public void deleteOneLevel(Long definitionId, Long levelId) throws FacadeLayerException;

  /**
   * updates game level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param gameLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
  public void updateGameLevel(Long definitionId, GameLevel gameLevel) throws FacadeLayerException;

  /**
   * updates info level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param infoLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
  public void updateInfoLevel(Long definitionId, InfoLevel infoLevel) throws FacadeLayerException;

  /**
   * updates assessment level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param assessmentLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
  public void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws FacadeLayerException;

  /**
   * creates new info level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param infoLevel to be created
   * @return DTO of new info level
   * @throws FacadeLayerException if training definition is not found
   */
  public InfoLevelDTO createInfoLevel(Long definitionId, InfoLevel infoLevel) throws FacadeLayerException;

  /**
   * creates new game level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param gameLevel to be created
   * @return DTO of new game level
   * @throws FacadeLayerException if training definition is not found
   */
  public GameLevelDTO createGameLevel(Long definitionId, GameLevel gameLevel) throws FacadeLayerException;

  /**
   * creates new assessment level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param assessmentLevel to be created
   * @return DTO of new assessment level
   * @throws FacadeLayerException if training definition is not found
   */
  public AssessmentLevelDTO createAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws FacadeLayerException;

}
