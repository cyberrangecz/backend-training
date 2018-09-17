package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.model.InfoLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface InfoLevelService {

  /**
   * Finds specific Info level by id
   * 
   * @param id of a Info level that would be returned
   * @return specific info level by id
   */
  public Optional<InfoLevel> findById(long id);

  /**
   * Find all Info Levels.
   * 
   * @return all info levels
   */
  public Page<InfoLevel> findAll(Predicate predicate, Pageable pageable);
}
