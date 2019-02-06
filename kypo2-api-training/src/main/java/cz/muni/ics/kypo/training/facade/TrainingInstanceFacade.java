package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import org.springframework.data.domain.Pageable;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;

/**
 * @author Pavel Seda (441048)
 */
public interface TrainingInstanceFacade {

    /**
     * Finds specific Training Instance by id
     *
     * @param id of a Training Instance that would be returned
     * @return specific Training Instance by id
     * @throws FacadeLayerException if training instance is not found
     */
    TrainingInstanceDTO findById(Long id) throws FacadeLayerException;

    /**
     * Find all Training Instances.
     *
     * @return all Training Instances
     */
    PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable);

    /**
     * Updates training instance
     *
     * @param trainingInstance to be updated
     * @throws FacadeLayerException if instance is not found
     */
    void update(TrainingInstanceUpdateDTO trainingInstance);

    /**
     * Creates new training instance
     *
     * @param trainingInstance to be created
     * @return DTO of created instance
     * @throws FacadeLayerException
     */
    TrainingInstanceDTO create(TrainingInstanceCreateDTO trainingInstance);

    /**
     * Deletes specific training instance based on id
     *
     * @param id of training instance to be deleted
     * @throws FacadeLayerException
     */
    void delete(Long id);

    /**
     * Allocates sandboxes for training instance
     *
     * @param instanceId
     * @return
     * @throws FacadeLayerException
     */
    void allocateSandboxes(Long instanceId);

    /**
     * Finds all Training Runs by specific Training Instance.
     *
     * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
     * @return Training Runs of specific Training Instance
     */
    PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Pageable pageable);

    /**
     * Create pool for sandboxes in open stack for given training instance
     *
     * @param instanceId id of training instance for which to create pool
     * @return id of created pool
     */
    Long createPoolForSandboxes(Long instanceId);

}
