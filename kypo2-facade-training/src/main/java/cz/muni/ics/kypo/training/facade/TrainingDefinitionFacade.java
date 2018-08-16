package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.model.TrainingDefinition;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingDefinitionDTO;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingDefinitionFacade {

  /**
   * Finds specific Training Definition by id
   * 
   * @param id of a Training Definition that would be returned
   * @return specific Training Definition by id
   */
  public TrainingDefinitionDTO findById(long id);

  /**
   * Find all Training Definitions.
   * 
   * @return all Training Definitions
   */
  public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable);

  /**
   * Updates training definition
   * @param trainingDefinition to be updated
   * @return DTO of updated definition
   * @throws FacadeLayerException
   */
  public TrainingDefinitionDTO update(TrainingDefinition trainingDefinition) throws FacadeLayerException;

  /**
   * Clones Training Definition by id
   * @param id of definition to be cloned
   * @return DTO of cloned definition
   * @throws FacadeLayerException
   */
  public TrainingDefinitionDTO clone(Long id) throws FacadeLayerException;

  /**
   * swaps level to the left
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   */
  public void swapLeft(Long definitionId, Long levelId);

  /**
   * swaps level to the right
   * @param definitionId - id of definition containing level to be swapped
   * @param levelId - id of level to be swapped
   */
  public void swapRight(Long definitionId, Long levelId);

}
