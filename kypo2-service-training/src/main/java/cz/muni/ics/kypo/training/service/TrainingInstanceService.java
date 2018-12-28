package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.OrganizerRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;

import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
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
     * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND given training instance is not found.
     *                               RESOURCE_CONFLICT cannot be updated for some reason.
     */
    void update(TrainingInstance trainingInstance);

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
    ResponseEntity<Void> allocateSandboxes(Long instanceId);

    /**
     * Finds all Training Runs of specific Training Instance.
     *
     * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
     * @return Training Runs of specific Training Instance
     */
    Page<TrainingRun> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Pageable pageable);

    /**
     * Find UserRefs by ids
     *
     * @param ids of wanted UserRefs
     * @return UserRefs with corresponding ids
     */
    Set<OrganizerRef> findUserRefsByIds(Set<Long> ids);

}
