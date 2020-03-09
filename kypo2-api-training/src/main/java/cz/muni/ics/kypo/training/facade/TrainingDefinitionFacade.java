package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * The interface for training definition facade.
 *
 */
public interface TrainingDefinitionFacade {

    /**
     * Finds specific Training Definition by id
     *
     * @param id of a Training Definition that would be returned
     * @return specific {@link TrainingDefinitionByIdDTO}
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
     * Updates training definition
     *
     * @param trainingDefinition to be updated
     */
    void update(TrainingDefinitionUpdateDTO trainingDefinition);

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
     */
    TrainingDefinitionByIdDTO clone(Long id, String title);

    /**
     * Swaps between levels. Swap basically means swapping the order attribute between these two levels.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param swapLevelFrom - Id of a first level to be swapped.
     * @param swapLevelTo   - Id of a second level to be swapped.
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    List<BasicLevelInfoDTO> swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo);

    /**
     * Move level to the different position and modify orders of levels between moved level and new position.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param levelIdToBeMoved - id of the level to be moved to the new position
     * @param newPosition   - position where level will be moved
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    List<BasicLevelInfoDTO> moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition);


    /**
     * Deletes specific training instance based on id
     *
     * @param id of definition to be deleted
     */
    void delete(Long id);


    /**
     * deletes specific level by id
     *
     * @param definitionId - id of definition containing level to be deleted
     * @param levelId      - id of level to be deleted
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    List<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId);

    /**
     * updates game level from training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param gameLevel    to be updated
     */
    void updateGameLevel(Long definitionId, GameLevelUpdateDTO gameLevel);


    /**
     * updates info level from training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param infoLevel    to be updated
     */
    void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel);


    /**
     * updates assessment level from training definition
     *
     * @param definitionId    - id of training definition containing level to be updated
     * @param assessmentLevel to be updated
     */
    void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevel);


    /**
     * creates new info level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new info level
     */
    BasicLevelInfoDTO createInfoLevel(Long definitionId);


    /**
     * creates new game level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new game level
     */
    BasicLevelInfoDTO createGameLevel(Long definitionId);


    /**
     * creates new assessment level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new assessment level
     */
    BasicLevelInfoDTO createAssessmentLevel(Long definitionId);

    /**
     * Finds specific level by id
     *
     * @param levelId - id of wanted level
     * @return wanted {@link AbstractLevelDTO}
     */
    AbstractLevelDTO findLevelById(Long levelId);


    /**
     * Get users with given role
     *
     * @param roleType the wanted role type
     * @param pageable pageable parameter with information about pagination.
     * @return list of users {@link UserRefDTO}
     */
    PageResultResource<UserRefDTO> getUsersWithGivenRole(RoleType roleType, Pageable pageable, String givenName, String familyName);

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
     * @param state represents a string if the training definitions should be relased or not.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingDefinitionInfoDTO} accessible for organizers
     */
    PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizers(String state, Pageable pageable);

    /**
     * Retrieve all authors for given training definition.
     *
     * @param trainingDefinitionId id of the training definition for which to get the authors
     * @param pageable pageable parameter with information about pagination.
     * @param givenName optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return returns all authors in given training definition.
     */
    PageResultResource<UserRefDTO> getAuthors(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName);

    /**
     * Retrieve all beta testers for given training definition.
     *
     * @param trainingDefinitionId id of the training definition for which to get the beta testers
     * @param pageable pageable parameter with information about pagination.
     * @return returns all beta testers in given training definition.
     */
    PageResultResource<UserRefDTO> getBetaTesters(Long trainingDefinitionId, Pageable pageable);

    /**
     * Retrieve all designers not in the given training definition.
     *
     * @param trainingDefinitionId id of the training definition which users should be excluded from the result list.
     * @param pageable pageable parameter with information about pagination.
     * @param givenName optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return returns all designers not in the given training definition.
     */
    PageResultResource<UserRefDTO> getDesignersNotInGivenTrainingDefinition(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName);

    /**
     * Concurrently add authors to the given training definition and remove authors from the training definition.
     *
     * @param trainingDefinitionId if of the training definition to be updated
     * @param authorsAddition ids of the authors to be added to the training definition
     * @param authorsRemoval ids of the authors to be removed from the training definition.
     */
    void editAuthors(Long trainingDefinitionId, Set<Long> authorsAddition, Set<Long> authorsRemoval);

}
