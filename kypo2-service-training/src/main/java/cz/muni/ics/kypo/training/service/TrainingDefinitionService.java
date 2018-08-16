package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.model.TrainingDefinition;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingDefinitionService {

  /**
   * Finds specific Training Definition by id
   * 
   * @param id of a Training Definition that would be returned
   * @return specific Training Definition by id
   */
  public Optional<TrainingDefinition> findById(long id);

  /**
   * Find all Training Definitions.
   * 
   * @return all Training Definitions
   */
  public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable);

  /**
   * Updates Training Definition
   * @param trainingDefinition to be updated
   * @return updated definition
   */
  public Optional<TrainingDefinition> update(TrainingDefinition trainingDefinition);

  /**
   * Creates new training definition by cloning existing one
   * @param id of definition to be cloned
   * @return cloned definition
   */
  public Optional<TrainingDefinition> clone(Long id);

  /**
   * Swaps level to the left
   * @param definitionId - Id of definition containing level to be swapped
   * @param levelId - Id of level to be swapped
   */
  public void swapLeft(Long definitionId, Long levelId);

  /**
   * Swaps level to the right
   * @param definitionId - Id of definition containing level to be swaped
   * @param levelId - Id of level to be swaped
   */
  public void swapRight(Long definitionId, Long levelId);

}
