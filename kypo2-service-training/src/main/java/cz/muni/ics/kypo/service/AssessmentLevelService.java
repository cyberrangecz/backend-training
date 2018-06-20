package cz.muni.ics.kypo.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.model.AssessmentLevel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface AssessmentLevelService {

  /**
   * Finds specific Assessment Level by id
   * 
   * @param id of a Assessment Level that would be returned
   * @return specific assessment level by id
   */
  public Optional<AssessmentLevel> findById(long id);

  /**
   * Find all Assessment Levels.
   * 
   * @return all a ssessment levels
   */
  public Page<AssessmentLevel> findAll(Predicate predicate, Pageable pageable);

}
