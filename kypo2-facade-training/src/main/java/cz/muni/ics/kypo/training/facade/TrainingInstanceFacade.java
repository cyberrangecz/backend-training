package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateResponseDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import org.springframework.data.domain.Pageable;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import org.springframework.http.ResponseEntity;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingInstanceFacade {

	/**
	 * Finds specific Training Instance by id
	 * 
	 * @param id of a Training Instance that would be returned
	 * @return specific Training Instance by id
	 */
	TrainingInstanceDTO findById(long id);

	/**
	 * Find all Training Instances.
	 * 
	 * @return all Training Instances
	 */
	PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable);

	/**
	 * Updates training instance
	 * @param trainingInstance to be updated
	 * @return newly generated password
	 * @throws FacadeLayerException if instance is not found
	 */
  String update(TrainingInstanceUpdateDTO trainingInstance) throws FacadeLayerException;

	/**
	 * Creates new training instance
	 * 
	 * @param trainingInstance to be created
	 * @return DTO of created instance
	 * @throws FacadeLayerException
	 */
	TrainingInstanceCreateResponseDTO create(TrainingInstanceCreateDTO trainingInstance) throws FacadeLayerException;

	/**
	 * Deletes specific training instance based on id
	 * 
	 * @param id of training instance to be deleted
	 * @throws FacadeLayerException
	 */
	void delete(Long id) throws FacadeLayerException;

	/**
	 * Allocates sandboxes for training instance
	 *
	 * @param instanceId
	 * @return
	 * @throws FacadeLayerException
	 */
	ResponseEntity<Void> allocateSandboxes(Long instanceId) throws FacadeLayerException;

	/**
	 * Finds all Training Runs by specific Training Instance.
	 *
	 * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
	 * @return Training Runs of specific Training Instance
	 */
	PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Pageable pageable);

}
