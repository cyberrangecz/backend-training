package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.RestResponses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * The interface for training definition facade.
 *
 * @author Pavel Seda (441048)
 */
public interface TrainingDefinitionFacade {

    /**
     * Finds specific Training Definition by id
     *
     * @param id of a Training Definition that would be returned
     * @return specific {@link TrainingDefinitionByIdDTO}
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND training definition cannot be found
     */
    TrainingDefinitionByIdDTO findById(Long id);

    /**
     * Find all Training Definitions.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingDefinitionDTO}
     */
    PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find all info(id, title, canEdit) about training definition with given sandbox definition
     *
     * @param sandboxDefinitionId - id of sandbox definition
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingDefinitionInfoDTO} with given sandbox definition
     */
    PageResultResource<TrainingDefinitionInfoDTO> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable);

    /**
     * Updates training definition
     *
     * @param trainingDefinition to be updated
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
     *                                              RESOURCE_CONFLICT released or archived training definition cannot be modified.
     */
    void update(TrainingDefinitionUpdateDTO trainingDefinition) throws FacadeLayerException;

    /**
     * Creates new training definition
     *
     * @param trainingDefinition to be created
     * @return DTO of created definition, {@link TrainingDefinitionCreateDTO}
     */
    TrainingDefinitionByIdDTO create(TrainingDefinitionCreateDTO trainingDefinition);

    /**
     * Clones Training Definition by id
     *
     * @param id    of definition to be cloned
     * @param title the title of cloned definition
     * @return DTO of cloned definition, {@link TrainingDefinitionByIdDTO}
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition not found.
     *                                              RESOURCE_CONFLICT cannot clone unreleased training definition.
     */
    TrainingDefinitionByIdDTO clone(Long id, String title) throws FacadeLayerException;

    /**
     * Swaps between levels. Swap basically means swapping the order attribute between these two levels.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param swapLevelFrom - Id of a first level to be swapped.
     * @param swapLevelTo   - Id of a second level to be swapped.
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
     *                                              RESOURCE_CONFLICT released or archived training definition cannot be modified.
     */
    List<BasicLevelInfoDTO> swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo);

    /**
     * Move level to the different position and modify orders of levels between moved level and new position.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param levelIdToBeMoved - id of the level to be moved to the new position
     * @param newPosition   - position where level will be moved
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or one of the levels is not found.
     *                                              RESOURCE_CONFLICT released or archived training definition cannot be modified.
     */
    List<BasicLevelInfoDTO> moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition);


    /**
     * Deletes specific training instance based on id
     *
     * @param id of definition to be deleted
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition or level is not found.
     *                                              RESOURCE_CONFLICT level cannot be deleted in released or archived training definition.
     */
    void delete(Long id) throws FacadeLayerException;


    /**
     * deletes specific level by id
     *
     * @param definitionId - id of definition containing level to be deleted
     * @param levelId      - id of level to be deleted
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                              RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
     */
    List<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId) throws FacadeLayerException;

    /**
     * updates game level from training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param gameLevel    to be updated
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                              RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
     */
    void updateGameLevel(Long definitionId, GameLevelUpdateDTO gameLevel) throws FacadeLayerException;


    /**
     * updates info level from training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param infoLevel    to be updated
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                              RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
     */
    void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) throws FacadeLayerException;


    /**
     * updates assessment level from training definition
     *
     * @param definitionId    - id of training definition containing level to be updated
     * @param assessmentLevel to be updated
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                              RESOURCE_CONFLICT level cannot be updated in released or archived training definition.
     */
    void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevel) throws FacadeLayerException;


    /**
     * creates new info level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new info level
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                              RESOURCE_CONFLICT level cannot be created in released or archived training definition.
     */
    BasicLevelInfoDTO createInfoLevel(Long definitionId) throws FacadeLayerException;


    /**
     * creates new game level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new game level
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                              RESOURCE_CONFLICT level cannot be created in released or archived training definition.
     */
    BasicLevelInfoDTO createGameLevel(Long definitionId) throws FacadeLayerException;


    /**
     * creates new assessment level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new assessment level
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training definition is not found.
     *                                              RESOURCE_CONFLICT level cannot be created in released or archived training definition.
     */
    BasicLevelInfoDTO createAssessmentLevel(Long definitionId) throws FacadeLayerException;

    /**
     * Finds specific level by id
     *
     * @param levelId - id of wanted level
     * @return wanted {@link AbstractLevelDTO}
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given level is not found.
     */
    AbstractLevelDTO findLevelById(Long levelId) throws FacadeLayerException;


    /**
     * Get users with given role
     *
     * @param roleType the wanted role type
     * @param pageable pageable parameter with information about pagination.
     * @return list of users {@link UserRefDTO}
     * @throws FacadeLayerException some error encountered when obtaining info about users
     */
    List<UserInfoDTO> getUsersWithGivenRole(RoleType roleType, Pageable pageable);

    /**
     * Switch state of definition to unreleased
     *
     * @param definitionId - id of training definition
     * @param state        - new state of TD
     */
    void switchState(Long definitionId, TDState state);

    /**
     * Find all Training Definitions.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingDefinitionInfoDTO} accessible for organizers
     */
    PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizers(Predicate predicate, Pageable pageable);
}
