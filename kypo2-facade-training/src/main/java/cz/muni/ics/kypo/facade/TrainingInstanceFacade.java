package cz.muni.ics.kypo.facade;

import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.TrainingInstanceDTO;

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
  public TrainingInstanceDTO findById(long id);

  /**
   * Find all Training Instances.
   * 
   * @return all Training Instances
   */
  public PageResultResource<TrainingInstanceDTO> findAll(Predicate predicate, Pageable pageable);

}
