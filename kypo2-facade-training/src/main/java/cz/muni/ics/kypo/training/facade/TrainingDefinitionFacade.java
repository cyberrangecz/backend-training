package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.model.InfoLevel;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import org.springframework.data.domain.Pageable;

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
  TrainingDefinitionDTO findById(long id);

  /**
   * Find all Training Definitions.
   * 
   * @return all Training Definitions
   */
  PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable);

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
   */
  void update(TrainingDefinition trainingDefinition) throws FacadeLayerException;

  /**
   * Creates new training definition
   * @param trainingDefinition to be created
   * @return DTO of created definition
   * @throws FacadeLayerException
   */
  TrainingDefinitionDTO create(TrainingDefinition trainingDefinition) throws FacadeLayerException;

  /**
   * Clones Training Definition by id
   * @param id of definition to be cloned
   * @return DTO of cloned definition
   * @throws FacadeLayerException if training definition is not found
   */
  TrainingDefinitionDTO clone(Long id) throws FacadeLayerException;


  /**
   * swaps level to the left
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   * @throws FacadeLayerException if training definition or level is not found
   */
  void swapLeft(Long definitionId, Long levelId) throws FacadeLayerException;


  /**
   * swaps level to the right
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   * @throws FacadeLayerException if training definition or level is not found
   */
  void swapRight(Long definitionId, Long levelId) throws FacadeLayerException;


  /**
   * Deletes specific training instance based on id
   * @param id of definition to be deleted
   * @throws FacadeLayerException if training definition is not found
   */
  void delete(Long id) throws FacadeLayerException;


  /**
   * deletes specific level by id
   * @param definitionId - id of definition containing level to be deleted
   * @param levelId - id of level to be deleted
   * @throws FacadeLayerException if training definition or level is not found
   */
  void deleteOneLevel(Long definitionId, Long levelId) throws FacadeLayerException;


  /**
   * updates game level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param gameLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
   void updateGameLevel(Long definitionId, GameLevel gameLevel) throws FacadeLayerException;


  /**
   * updates info level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param infoLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
  void updateInfoLevel(Long definitionId, InfoLevel infoLevel) throws FacadeLayerException;


  /**
   * updates assessment level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param assessmentLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
  void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws FacadeLayerException;


  /**
   * creates new info level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param infoLevel to be created
   * @return DTO of new info level
   * @throws FacadeLayerException if training definition is not found
   */
   InfoLevelDTO createInfoLevel(Long definitionId, InfoLevel infoLevel) throws FacadeLayerException;


  /**
   * creates new game level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param gameLevel to be created
   * @return DTO of new game level
   * @throws FacadeLayerException if training definition is not found
   */
  GameLevelDTO createGameLevel(Long definitionId, GameLevel gameLevel) throws FacadeLayerException;


  /**
   * creates new assessment level in training definition
   * @param definitionId - id of definition in which level will be created
   * @param assessmentLevel to be created
   * @return DTO of new assessment level
   * @throws FacadeLayerException if training definition is not found
   */
  AssessmentLevelDTO createAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel) throws FacadeLayerException;


}
