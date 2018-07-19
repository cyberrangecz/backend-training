package cz.muni.ics.kypo.facade;

import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.model.AssessmentLevel;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.AssessmentLevelDTO;

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
  public AssessmentLevelDTO findById(Long id) throws FacadeLayerException;

  /**
   * Find all Assessment Levels.
   * 
   * @return all a ssessment levels
   */
  public PageResultResource<AssessmentLevelDTO> findAll(Predicate predicate, Pageable pageable) throws FacadeLayerException;

  public AssessmentLevelDTO create(AssessmentLevel assessmentLevel) throws FacadeLayerException;

  public AssessmentLevelDTO update(AssessmentLevel assessmentLevel) throws FacadeLayerException;

  public void delete(Long id) throws FacadeLayerException;


}
