package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import cz.muni.ics.kypo.training.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.model.TrainingInstance;
import org.springframework.http.ResponseEntity;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingInstanceService {

	/**
	 * Finds specific Training Instance by id
	 * 
	 * @param id of a Training Instance that would be returned
	 * @return specific Training Instance by id
	 */
	Optional<TrainingInstance> findById(long id);

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
	Optional<TrainingInstance> create(TrainingInstance trainingInstance);

	/**
	 * updates training instance
	 * 
	 * @param trainingInstance to be updated
	 * @throws CannotBeUpdatedException if starting date of instance is not in future
	 * @throws ServiceLayerException if instance is not found
	 */
	void update(TrainingInstance trainingInstance) throws CannotBeUpdatedException, ServiceLayerException;

	/**
	 * deletes training instance
	 * 
	 * @param id of training instance
	 * @throws CannotBeDeletedException if end date of instance is not in past
	 * @throws ServiceLayerException if instance is not found
	 */
	void delete(Long id) throws CannotBeDeletedException, ServiceLayerException;

	/**
	 * Generates password for training instance
	 * 
	 * @return new password
	 * @throws ServiceLayerException if password already exists
	 */
	char[] generatePassword() throws ServiceLayerException;

	/**
	 * Allocates sandboxes for training instance
	 *
	 * @param instanceId of training instance
	 * @throws ServiceLayerException if instance is not found
	 */
	ResponseEntity<Void> allocateSandboxes(Long instanceId) throws ServiceLayerException;

}
