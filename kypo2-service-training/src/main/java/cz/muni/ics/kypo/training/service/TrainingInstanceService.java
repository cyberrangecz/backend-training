package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;

import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.concurrent.Future;

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
     * This method is annotated with @Transactional
     *
     * @param trainingInstance for which allocate sandboxes
     * @param count            number of sandboxes to allocate
     * @throws ServiceLayerException if instance is not found
     */
    void allocateSandboxes(TrainingInstance trainingInstance, Integer count);

    /**
     * Delete sandbox from training instance
     * This method is annotated with @Transactional
     *
     * @param trainingInstance   which sandbox should be deleted
     * @param sandboxInstanceRef sandbox to be removed from training instance and deleted from open stack
     * @throws ServiceLayerException if instance is not found
     */
    void deleteSandbox(TrainingInstance trainingInstance, SandboxInstanceRef sandboxInstanceRef);

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

    TrainingInstance findByIdIncludingDefinition(Long instanceId);

}
