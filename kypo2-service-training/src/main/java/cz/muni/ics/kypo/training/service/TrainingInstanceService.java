package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;

import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * The interface for Training instance service.
 *
 * @author Pavel Seda (441048)
 */
public interface TrainingInstanceService {

    /**
     * Finds specific Training Instance by id
     *
     * @param id of a Training Instance that would be returned
     * @return specific {@link TrainingInstance} by id
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
     */
    TrainingInstance findById(Long id);

    /**
     * Find all Training Instances.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable
     * @return all {@link TrainingInstance}s
     */
    Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable);

    /**
     * Creates new training instance
     *
     * @param trainingInstance to be created
     * @return created {@link TrainingInstance}
     */
    TrainingInstance create(TrainingInstance trainingInstance);

    /**
     * updates training instance
     *
     * @param trainingInstance to be updated
     * @return new access token if it was changed
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.                                               RESOURCE_CONFLICT cannot be updated for some reason.
     */
    String update(TrainingInstance trainingInstance);

    /**
     * deletes training instance
     *
     * @param trainingInstance the training instance to be deleted.
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.                                               RESOURCE_CONFLICT cannot be deleted for some reason.
     */
    void delete(TrainingInstance trainingInstance);

    /**
     * Allocates sandboxes for training instance
     * This method is annotated with @Transactional
     *
     * @param trainingInstance for which allocate sandboxes
     * @param count            number of sandboxes to allocate
     * @throws ServiceLayerException if instance is not found
     */
    void allocateSandboxes(TrainingInstance trainingInstance, Integer count);

    /**
     * Delete sandbox from training instance
     * This method is annotated with @Transactional and is asynchronous
     *
     * @param trainingInstanceId             id of Training Instnace from which to delete sandbox instance.
     * @param idOfSandboxInstanceRefToDelete id of sandbox to be removed from training instance and deleted from open stack
     */
    void deleteSandbox(Long trainingInstanceId, Long idOfSandboxInstanceRefToDelete);

    /**
     * Finds all Training Runs of specific Training Instance.
     *
     * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
     * @param isActive           if isActive attribute is True, only active runs are returned
     * @param pageable           pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific {@link TrainingInstance}
     */
    Page<TrainingRun> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Boolean isActive, Pageable pageable);

    /**
     * Find UserRefs by userRefId
     *
     * @param usersRefId of wanted UserRefs
     * @return {@link UserRef}s with corresponding userRefIds
     */
    Set<UserRef> findUserRefsByUserRefIds(Set<Long> usersRefId);

    /**
     * Create pool in openstack for sandboxes
     *
     * @param trainingInstanceId id of training instance for which to create pool
     * @return id of pool created in openstack
     */
    Long createPoolForSandboxes(Long trainingInstanceId);

    /**
     * Find specific Training instance by id including its associated Training definition.
     *
     * @param instanceId the instance id
     * @return the {@link TrainingInstance}
     */
    TrainingInstance findByIdIncludingDefinition(Long instanceId);

    /**
     * Find ids of all sandboxes occupied by specific training instance.
     *
     * @param trainingInstanceId the id of specific training instance
     * @return the list of ids of all occupied sandboxes
     */
    List<Long> findIdsOfAllOccupiedSandboxesByTrainingInstance(Long trainingInstanceId);


    /**
     * Check if instance is finished.
     *
     * @param trainingInstanceId the training instance id
     * @return true if instance is finished, false if not
     */
    boolean checkIfInstanceIsFinished(Long trainingInstanceId);

    TrainingInstance findByStartTimeAfterAndEndTimeBeforeAndAccessToken(String accessToken);
}
