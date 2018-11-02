package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;

import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

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
    TrainingInstance findById(long id);

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
     * Generates password for training instance
     *
     * @param trainingInstance - Instance that wants to have new password
     * @param password         - original string that is to be changed
     * @return new password
     */
    String generatePassword(TrainingInstance trainingInstance, String password);


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

}
