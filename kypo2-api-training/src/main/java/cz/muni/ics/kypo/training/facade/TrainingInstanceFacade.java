package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceIsFinishedInfoDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import org.springframework.data.domain.Pageable;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.RestResponses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;

import java.util.Set;

/**
 * The interface for Training instance facade.
 *
 * @author Pavel Seda (441048)
 */
public interface TrainingInstanceFacade {

    /**
     * Finds specific Training Instance by id
     *
     * @param id of a Training Instance that would be returned
     * @return specific {@link TrainingInstanceDTO} by id
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
     */
    TrainingInstanceDTO findById(Long id) throws FacadeLayerException;

    /**
     * Find all Training Instances.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingInstanceDTO}
     */
    PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable);

    /**
     * Updates training instance
     *
     * @param trainingInstance to be updated
     * @return new access token if it was changed
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.                                              RESOURCE_CONFLICT cannot be updated for some reason.
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
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.                                               RESOURCE_CONFLICT cannot be deleted for some reason.
     */
    void delete(Long id);

    /**
     * Allocates sandboxes for training instance
     *
     * @param instanceId the instance id
     * @param count      number of sandboxes that will be allocated
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
     */
    void allocateSandboxes(Long instanceId, Integer count);

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
     * Create pool for sandboxes in open stack for given training instance
     *
     * @param instanceId id of training instance for which to create pool
     * @return id of created pool
     */
    Long createPoolForSandboxes(Long instanceId);

    /**
     * Deletes all failed sandboxes from training instance
     *
     * @param instanceId id of training instance for which failed sandboxes will be deleted and reallocated
     * @param sandboxIds ids of sandboxes that will be deleted
     * @throws FacadeLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
     */
    void deleteSandboxes(Long instanceId, Set<Long> sandboxIds);

    /**
     * Check if instance can be deleted.
     *
     * @param trainingInstanceId the training instance id
     * @return true if instance can be deleted, false if not and message. {@link TrainingInstanceIsFinishedInfoDTO}
     */
    TrainingInstanceIsFinishedInfoDTO checkIfInstanceCanBeDeleted(Long trainingInstanceId);
}
