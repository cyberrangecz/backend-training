package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.*;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import org.springframework.data.domain.Pageable;

import java.util.Set;

/**
 * The interface for Training instance facade.
 *
 */
public interface TrainingInstanceFacade {

    /**
     * Finds specific Training Instance by id
     *
     * @param id of a Training Instance that would be returned
     * @return specific {@link TrainingInstanceDTO} by id
     */
    TrainingInstanceDTO findById(Long id);

    /**
     * Find all Training Instances.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingInstanceFindAllResponseDTO}
     */
    PageResultResource<TrainingInstanceFindAllResponseDTO> findAll(Predicate predicate, Pageable pageable);

    /**
     * Updates training instance
     *
     * @param trainingInstance to be updated
     * @return new access token if it was changed
     */
    String update(TrainingInstanceUpdateDTO trainingInstance);

    /**
     * Creates new training instance
     *
     * @param trainingInstance to be created
     * @return created {@link TrainingInstanceDTO}
     */
    TrainingInstanceDTO create(TrainingInstanceCreateDTO trainingInstance);

    /**
     * Deletes specific training instance based on id
     *
     * @param id of training instance to be deleted
     */
    void delete(Long id);

    /**
     * Assign pool in training instance new training instance
     *
     * @param trainingInstance of training instance to be deleted
     */
    TrainingInstanceBasicInfoDTO assignPoolToTrainingInstance(TrainingInstanceAssignPoolIdDTO trainingInstance);

    /**
     * Reassign pool in training instance  or assignes new training instance
     *
     * @param trainingInstance of training instance to be deleted
     */
    TrainingInstanceBasicInfoDTO reassignPoolToTrainingInstance(TrainingInstanceAssignPoolIdDTO trainingInstance);

    /**
     * Finds all Training Runs by specific Training Instance.
     *
     * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
     * @param isActive           if isActive attribute is True, only active runs are returned
     * @param pageable           pageable parameter with information about pagination.
     * @return Page of {@link TrainingRunDTO} of specific Training Instance
     */
    PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Boolean isActive, Pageable pageable);

    /**
     * Check if instance can be deleted.
     *
     * @param trainingInstanceId the training instance id
     * @return true if instance can be deleted, false if not and message. {@link TrainingInstanceIsFinishedInfoDTO}
     */
    TrainingInstanceIsFinishedInfoDTO checkIfInstanceCanBeDeleted(Long trainingInstanceId);

    /**
     * Retrieve all organizers for given training instance .
     *
     * @param trainingInstanceId id of the training instance for which to get the organizers
     * @param pageable pageable parameter with information about pagination.
     * @param givenName optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return returns all organizers in given training instance.
     */
    PageResultResource<UserRefDTO> getOrganizersOfTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName);

    /**
     * Retrieve all organizers not in the given training instance.
     *
     * @param trainingInstanceId id of the training instance which users should be excluded from the result list.
     * @param pageable pageable parameter with information about pagination.
     * @param givenName optional parameter used for filtration
     * @param familyName optional parameter used for filtration
     * @return returns all organizers not in the given training instance.
     */
    PageResultResource<UserRefDTO> getOrganizersNotInGivenTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName);

    /**
     * Concurrently add organizers to the given training instance and remove authors from the training instance.
     *
     * @param trainingInstanceId if of the training instance to be updated
     * @param organizersAddition ids of the organizers to be added to the training instance
     * @param organizersRemoval ids of the organizers to be removed from the training instance.
     */
    void editOrganizers(Long trainingInstanceId, Set<Long> organizersAddition, Set<Long> organizersRemoval);

}
