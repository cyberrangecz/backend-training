package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.model.TrainingRun;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface TrainingRunService {

  /**
   * Finds specific Training Run by id.
   * 
   * @param id of a Training Run that would be returned
   * @return specific Training Run by id
   */
  public Optional<TrainingRun> findById(long id);

  /**
   * Find all Training Runs.
   * 
   * @return all Training Runs
   */
  public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable);

}
