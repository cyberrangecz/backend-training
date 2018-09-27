package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;

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
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND training definition cannot be found
   */
  public TrainingDefinition findById(long id) throws ServiceLayerException;

  /**
   * Find all Training Definitions.
   * 
   * @return all Training Definitions
   */
  public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable);

  /**
   * Find all training definition with same sandbox definition
   *
   * @param sandboxDefinitionId - id of sandbox definition
   * @return all training definition with same sandbox definition
   */
  public Page<TrainingDefinition> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable);

  /**
   * Updates Training Definition
   * @param trainingDefinition to be updated
   * @return updated definition
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
   *                                               RESOURCE_CONFLICT released or archived training definition cannot be modified.
   */
  public void update(TrainingDefinition trainingDefinition) throws ServiceLayerException;

  /**
   * Creates new training definition by cloning existing one
   * @param id of definition to be cloned
   * @return cloned definition
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition not found.
   *                                               RESOURCE_CONFLICT cannot clone unreleased training definition.
   */
  public TrainingDefinition clone(Long id) throws ServiceLayerException;

  /**
   * Swaps level to the left
   * @param definitionId - Id of definition containing level to be swapped
   * @param levelId - Id of level to be swapped
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
   *                                               RESOURCE_CONFLICT released or archived training definition cannot be modified.
   */
  public void swapLeft(Long definitionId, Long levelId) throws ServiceLayerException;

  /**
   * Swaps level to the right
   * @param definitionId - Id of definition containing level to be swapped
   * @param levelId - Id of level to be swapped
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
   *                                               RESOURCE_CONFLICT released or archived training definition cannot be modified.
   */
  public void swapRight(Long definitionId, Long levelId) throws ServiceLayerException;

  /**
   * Deletes specific training definition based on id
   * @param id of definition to be deleted
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or level is not found.
   *                                               RESOURCE_CONFLICT released training definition cannot be deleted.
   */
  public void delete(Long id) throws ServiceLayerException;

  /**
   * Deletes specific level based on id
   * @param definitionId - id of definition containing level to be deleted
   * @param levelId - id of level to be deleted
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or level is not found.
   *                                               RESOURCE_CONFLICT level cannot be deleted in released or archived training definition.
   */
  public void deleteOneLevel(Long definitionId, Long levelId) throws ServiceLayerException;

  /**
   * Updates game level in training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param gameLevel to be updated
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
   *                                               RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
   */
  public void updateGameLevel(Long definitionId, GameLevel gameLevel) throws ServiceLayerException;

  /**
   * Updates info level in training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param infoLevel to be updated
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
   *                                               RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
   */
  public void updateInfoLevel(Long definitionId, InfoLevel infoLevel) throws ServiceLayerException;

  /**
   * Updates assessment level in training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param assessmentLevel to be updated
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
   *                                               RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
   */
  public void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws ServiceLayerException;

  /**
   * Creates new game level
   * @param definitionId - id of definition in which level will be created
   * @return new game level
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
   *                                               RESOURCE_CONFLICT level cannot be created in released or archived training definition.
   */
  public GameLevel createGameLevel(Long definitionId) throws ServiceLayerException;

  /**
   * Creates new info level
   * @param definitionId - id of definition in which level will be created
   * @return new info level
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
   *                                               RESOURCE_CONFLICT level cannot be created in released or archived training definition.
   */
  public InfoLevel createInfoLevel(Long definitionId) throws ServiceLayerException;

  /**
   * Creates new assessment level
   * @param definitionId - id of definition in which level will be created
   * @return new assessment level
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
   *                                               RESOURCE_CONFLICT level cannot be created in released or archived training definition.
   */
  public AssessmentLevel createAssessmentLevel(Long definitionId) throws ServiceLayerException;

  /**
   * Finds all levels from single definition
   * @param id of definition
   * @return set of levels
   */
  public ArrayList<AbstractLevel> findAllLevelsFromDefinition(Long id);

  /**
   * creates new training definition
   * @param trainingDefinition to be created
   * @return new training definition
   */
  public TrainingDefinition create(TrainingDefinition trainingDefinition);


	/**
	 * Finds specific level by id
	 * @param levelId - id of wanted level
	 * @return wanted level
	 * @throws ServiceLayerException if level is not found
	 */
	AbstractLevel findLevelById(Long levelId) throws ServiceLayerException;

}
