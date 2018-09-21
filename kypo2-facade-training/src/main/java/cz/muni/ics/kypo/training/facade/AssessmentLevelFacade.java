package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;

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
  AssessmentLevelDTO findById(Long id) throws FacadeLayerException;

  /**
   * Find all Assessment Levels.
   * 
   * @return all a ssessment levels
   */
  PageResultResource<AssessmentLevelDTO> findAll(Predicate predicate, Pageable pageable) throws FacadeLayerException;

}
