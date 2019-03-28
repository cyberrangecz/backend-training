package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;

import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Set;

/**
 * @author Pavel Seda (441048)
 */
public interface TrainingInstanceService {

    /**
     * Finds specific Training Instance by id
     *
     * @param id of a Training Instance that would be returned
     * @return specific Training Instance by id
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
     */
    TrainingInstance findById(Long id);

    /**
     * Find all Training Instances.
     *
     * @return all Training Instances
     */
    Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable);

    /**
     * Creates new training instance
     *
     * @param trainingInstance to be created
     * @return created instance
     */
    TrainingInstance create(TrainingInstance trainingInstance);

    /**
     * updates training instance
     *
     * @param trainingInstance to be updated
     * @return new access token if it was changed
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
     *                               RESOURCE_CONFLICT cannot be updated for some reason.
     */
    String update(TrainingInstance trainingInstance);

    /**
     * deletes training instance
     *
     * @param id of training instance
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
     *                               RESOURCE_CONFLICT cannot be deleted for some reason.
     */
    void delete(Long id);

    /**
     * Allocates sandboxes for training instance
     *
     * @param instanceId of training instance
     * @throws ServiceLayerException if instance is not found
     */
    void allocateSandboxes(Long instanceId);

    /**
     * Finds all Training Runs of specific Training Instance.
     *
     * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
     * @return Training Runs of specific Training Instance
     */
    Page<TrainingRun> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Pageable pageable);

    /**
     * Find UserRefs by logins
     *
     * @param logins of wanted UserRefs
     * @return UserRefs with corresponding logins
     */
    Set<UserRef> findUserRefsByLogins(Set<String> logins);

    /**
     * Create pool in openstack for sandboxes
     *
     * @param trainingInstanceId id of training instance for which to create pool
     * @return id of pool created in openstack
     */
    Long createPoolForSandboxes(Long trainingInstanceId);

    /**
     * Deletes sandboxes from training instance
     *
     * @param instanceId id of training instance for which sandboxes will be deleted
     * @param listOfSandBoxIds ids of sandboxes to be deleted
     * @throws ServiceLayerException
     */
    void deleteSandboxes(Long instanceId, Set<Long> listOfSandBoxIds);

    /**
     * Reallocate sandboxe in training instance
     *
     * @param instanceId id of training instance for which sandboxes will be reallocated
     * @param sandboxId id of sandbox that will be reallocated
     * @throws ServiceLayerException
     */
    void reallocateSandbox(Long instanceId, Long sandboxId);
}
