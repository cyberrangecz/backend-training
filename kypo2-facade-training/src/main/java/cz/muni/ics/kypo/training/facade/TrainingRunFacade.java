package cz.muni.ics.kypo.training.facade;

import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingRunDTO;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingRunFacade {

  /**
   * Finds specific Training Run by id
   * 
   * @param id of a Training Run that would be returned
   * @return specific Training Run by id
   */
  public TrainingRunDTO findById(long id);

  /**
   * Find all Training Runs.
   * 
   * @return all Training Runs
   */
  public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable);

}
