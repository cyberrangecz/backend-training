package cz.muni.ics.kypo.facade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.dto.TrainingRunDTO;

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
  public Page<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable);

}
