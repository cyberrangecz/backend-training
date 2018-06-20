package cz.muni.ics.kypo.facade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.dto.AssessmentLevelDTO;

/**
 * @author Pavel Šeda
 *
 */
public interface AssessmentLevelFacade {

  /**
   * Finds specific Assessment Level by id
   * 
   * @param id of a Assessment Level that would be returned
   * @return specific assessment level by id
   */
  public AssessmentLevelDTO findById(long id);

  /**
   * Find all Assessment Levels.
   * 
   * @return all a ssessment levels
   */
  public Page<AssessmentLevelDTO> findAll(Predicate predicate, Pageable pageable);

}
