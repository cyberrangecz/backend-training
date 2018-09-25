package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.CannotBeClonedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeUpdatedException;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelCreateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;

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
	TrainingDefinitionDTO findById(Long id);

	/**
	 * Find all Training Definitions.
	 * 
	 * @return all Training Definitions
	 */
	PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable);

	/**
	 * Find all training definition with same sandbox definition
	 * 
	 * @param sandboxDefinitionId - id of sandbox definition
	 * @return all training definition with same sandbox definition
	 */
	public PageResultResource<TrainingDefinitionDTO> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable);

	/**
	 * Updates training definition
	 * 
	 * @param trainingDefinitionUpdateDTO to be updated
	 * @return DTO of updated definition
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	void update(TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * Creates new training definition
	 * 
	 * @param trainingDefinition to be created
	 * @return DTO of created definition
	 * @throws FacadeLayerException
	 */
	TrainingDefinitionCreateDTO create(TrainingDefinitionCreateDTO trainingDefinition) throws FacadeLayerException;

	/**
	 * Clones Training Definition by id
	 * 
	 * @param id of definition to be cloned
	 * @return DTO of cloned definition
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeClonedException if definition status is unreleased
	 */
	TrainingDefinitionDTO clone(Long id) throws FacadeLayerException, CannotBeClonedException;

	/**
	 * swaps level to the left
	 * 
	 * @param definitionId - id of definition containing level to be swapped
	 * @param levelId - id of level to be swapped
	 * @throws FacadeLayerException if training definition or level is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	void swapLeft(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * swaps level to the right
	 * 
	 * @param definitionId - id of definition containing level to be swapped
	 * @param levelId - id of level to be swapped
	 * @throws FacadeLayerException if training definition or level is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	void swapRight(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * Deletes specific training instance based on id
	 * 
	 * @param id of definition to be deleted
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeDeletedException if definition status is RELEASED
	 */
	void delete(Long id) throws FacadeLayerException, CannotBeDeletedException;

	/**
	 * deletes specific level by id
	 * 
	 * @param definitionId - id of definition containing level to be deleted
	 * @param levelId - id of level to be deleted
	 * @throws FacadeLayerException if training definition or level is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	void deleteOneLevel(Long definitionId, Long levelId) throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * updates game level from training definition
	 * 
	 * @param definitionId - id of training definition containing level to be updated
	 * @param gameLevel to be updated
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	void updateGameLevel(Long definitionId, GameLevelUpdateDTO gameLevel) throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * updates info level from training definition
	 * 
	 * @param definitionId - id of training definition containing level to be updated
	 * @param infoLevel to be updated
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * updates assessment level from training definition
	 * 
	 * @param definitionId - id of training definition containing level to be updated
	 * @param assessmentLevel to be updated
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevel)
			throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * creates new info level in training definition
	 * 
	 * @param definitionId - id of definition in which level will be created
	 * @param infoLevel to be created
	 * @return DTO of new info level
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	InfoLevelCreateDTO createInfoLevel(Long definitionId, InfoLevelCreateDTO infoLevel) throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * creates new game level in training definition
	 * 
	 * @param definitionId - id of definition in which level will be created
	 * @param gameLevel to be created
	 * @return DTO of new game level
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	GameLevelCreateDTO createGameLevel(Long definitionId, GameLevelCreateDTO gameLevel) throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * creates new assessment level in training definition
	 * 
	 * @param definitionId - id of definition in which level will be created
	 * @param assessmentLevel to be created
	 * @return DTO of new assessment level
	 * @throws FacadeLayerException if training definition is not found
	 * @throws CannotBeUpdatedException if definition status is not UNRELEASED
	 */
	AssessmentLevelCreateDTO createAssessmentLevel(Long definitionId, AssessmentLevelCreateDTO assessmentLevel)
			throws FacadeLayerException, CannotBeUpdatedException;

	/**
	 * Finds specific level by id
	 *
	 * @param levelId - id of wanted level
	 * @return wanted level
	 * @throws FacadeLayerException if level is not found
	 */
	AbstractLevelDTO findLevelById(Long levelId) throws FacadeLayerException;

}
