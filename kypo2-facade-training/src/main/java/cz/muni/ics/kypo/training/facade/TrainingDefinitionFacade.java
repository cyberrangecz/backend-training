package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;

import java.util.Set;
import java.util.logging.Level;

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
  void update(TrainingDefinitionUpdateDTO trainingDefinition) throws FacadeLayerException;

  /**
   * Creates new training definition
   * @param trainingDefinition to be created
   * @return DTO of created definition
   */
  TrainingDefinitionDTO create(TrainingDefinitionCreateDTO trainingDefinition);

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
  Set<BasicLevelInfoDTO> swapLeft(Long definitionId, Long levelId) throws FacadeLayerException;


  /**
   * swaps level to the right
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   * @throws FacadeLayerException if training definition or level is not found
   */
  Set<BasicLevelInfoDTO> swapRight(Long definitionId, Long levelId) throws FacadeLayerException;


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
  Set<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId) throws FacadeLayerException;


  /**
   * updates game level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param gameLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
   void updateGameLevel(Long definitionId, GameLevelUpdateDTO gameLevel) throws FacadeLayerException;


  /**
   * updates info level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param infoLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
  void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) throws FacadeLayerException;


  /**
   * updates assessment level from training definition
   * @param definitionId - id of training definition containing level to be updated
   * @param assessmentLevel to be updated
   * @throws FacadeLayerException if training definition is not found
   */
  void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevel) throws FacadeLayerException;


  /**
   * creates new info level in training definition
   * @param definitionId - id of definition in which level will be created
   * @return DTO of new info level
   * @throws FacadeLayerException if training definition is not found
   */
   BasicLevelInfoDTO createInfoLevel(Long definitionId) throws FacadeLayerException;


  /**
   * creates new game level in training definition
   * @param definitionId - id of definition in which level will be created
   * @return DTO of new game level
   * @throws FacadeLayerException if training definition is not found
   */
  BasicLevelInfoDTO createGameLevel(Long definitionId) throws FacadeLayerException;


  /**
   * creates new assessment level in training definition
   * @param definitionId - id of definition in which level will be created
   * @return DTO of new assessment level
   * @throws FacadeLayerException if training definition is not found
   */
  BasicLevelInfoDTO createAssessmentLevel(Long definitionId) throws FacadeLayerException;

	/**
	 * Finds specific level by id
	 *
	 * @param levelId - id of wanted level
	 * @return wanted level
	 * @throws FacadeLayerException if level is not found
	 */
	AbstractLevelDTO findLevelById(Long levelId) throws FacadeLayerException;

}
