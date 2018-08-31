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

}
