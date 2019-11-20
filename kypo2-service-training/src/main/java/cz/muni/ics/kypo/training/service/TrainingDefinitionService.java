package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * The interface for training definition service.
 *
 * @author Pavel Seda (441048)
 */
public interface TrainingDefinitionService {

    /**
     * Finds specific Training Definition by id
     *
     * @param id of a Training Definition that would be returned
     * @return specific {@link TrainingDefinition} by id
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND training definition cannot be found
     */
    TrainingDefinition findById(Long id);

    /**
     * Find all Training Definitions by author if user is designer or all Training Definitions if user is admin.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return all {@link TrainingDefinition}s
     */
    Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find all training definition with same sandbox definition
     *
     * @param sandboxDefinitionId - id of sandbox definition
     * @param pageable            pageable parameter with information about pagination.
     * @return all {@link TrainingDefinition}s with same sandbox definition
     */
    Page<TrainingDefinition> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable);

    /**
     * Updates given Training Definition
     *
     * @param trainingDefinition to be updated
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
     *                                               RESOURCE_CONFLICT released or archived training definition cannot be modified.
     */
    void update(TrainingDefinition trainingDefinition);

    /**
     * Creates new training definition by cloning existing one
     *
     * @param id    of definition to be cloned
     * @param title the title of the new cloned definition
     * @return cloned {@link TrainingDefinition}
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition not found.
     *                                               RESOURCE_CONFLICT cannot clone unreleased training definition.
     */
    TrainingDefinition clone(Long id, String title);

    /**
     * Swaps between levels. Swap basically means swapping the order attribute between these two levels.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param swapLevelFrom - Id of a first level to be swapped.
     * @param swapLevelTo   - Id of a second level to be swapped.
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
     *                                               RESOURCE_CONFLICT released or archived training definition cannot be modified.
     */
    void swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo);

    /**
     * Move level to the different position and modify orders of levels between moved level and new position.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param levelIdToBeMoved - id of the level to be moved to the new position
     * @param newPosition   - position where level will be moved
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
     *                                              RESOURCE_CONFLICT released or archived training definition cannot be modified.
     */
    void moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition);

    /**
     * Deletes specific training definition based on id
     *
     * @param id of definition to be deleted
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or level is not found.
     *                                               RESOURCE_CONFLICT released training definition cannot be deleted.
     */
    void delete(Long id);

    /**
     * Deletes specific level based on id
     *
     * @param definitionId - id of definition containing level to be deleted
     * @param levelId      - id of level to be deleted
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or level is not found.
     *                                               RESOURCE_CONFLICT level cannot be deleted in released or archived training definition.
     */
    void deleteOneLevel(Long definitionId, Long levelId);

    /**
     * Updates game level in training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param gameLevel    to be updated
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                               RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
     */
    void updateGameLevel(Long definitionId, GameLevel gameLevel);

    /**
     * Updates info level in training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param infoLevel    to be updated
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                               RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
     */
    void updateInfoLevel(Long definitionId, InfoLevel infoLevel);

    /**
     * Updates assessment level in training definition
     *
     * @param definitionId    - id of training definition containing level to be updated
     * @param assessmentLevel to be updated
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                               RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
     */
    void updateAssessmentLevel(Long definitionId, AssessmentLevel assessmentLevel);

    /**
     * Creates new game level
     *
     * @param definitionId - id of definition in which level will be created
     * @return new {@link GameLevel}
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                               RESOURCE_CONFLICT level cannot be created in released or archived training definition.
     */
    GameLevel createGameLevel(Long definitionId);

    /**
     * Creates new info level
     *
     * @param definitionId - id of definition in which level will be created
     * @return new {@link InfoLevel}
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                               RESOURCE_CONFLICT level cannot be created in released or archived training definition.
     */
    InfoLevel createInfoLevel(Long definitionId);

    /**
     * Creates new assessment level
     *
     * @param definitionId - id of definition in which level will be created
     * @return new {@link AssessmentLevel}
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                               RESOURCE_CONFLICT level cannot be created in released or archived training definition.
     */
    AssessmentLevel createAssessmentLevel(Long definitionId);

    /**
     * Finds all levels from single definition
     *
     * @param id of definition
     * @return list of {@link AbstractLevel} associated with training definition
     */
    List<AbstractLevel> findAllLevelsFromDefinition(Long id);

    /**
     * creates new training definition
     *
     * @param trainingDefinition to be created
     * @return new {@link TrainingDefinition}
     */
    TrainingDefinition create(TrainingDefinition trainingDefinition);

    /**
     * Finds specific level by id
     *
     * @param levelId - id of wanted level
     * @return wanted {@link AbstractLevel}
     * @throws ServiceLayerException if level is not found
     */
    AbstractLevel findLevelById(Long levelId);

    /**
     * Find all training instances associated with training definition by id.
     *
     * @param id the id of training definition
     * @return the list of all {@link TrainingInstance}s associated with wanted {@link TrainingDefinition}
     */
    List<TrainingInstance> findAllTrainingInstancesByTrainingDefinitionId(Long id);

    /**
     * Switch development state of definition from unreleased to released, or from released to archived or back to unreleased.
     *
     * @param definitionId - id of training definition
     * @param state        - new state of training definition
     */
    void switchState(Long definitionId, cz.muni.ics.kypo.training.api.enums.TDState state);

    /**
     * Finds all Training Definitions accessible to users with the role of organizer.
     *
     * @param state represents a state of training definition if it is released or unreleased.
     * @param pageable  pageable parameter with information about pagination.
     * @return all Training Definitions for organizers
     */
    Page<TrainingDefinition> findAllForOrganizers(String state, Pageable pageable);
}
