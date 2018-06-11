package cz.muni.ics.kypo.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.model.InfoLevel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface InfoService {

  /**
   * Finds specific Info level by id
   * 
   * @param id of a Info level that would be returned
   * @return specific info level by id
   */
  public Optional<InfoLevel> findById(Long id);

  /**
   * Find all Info Levels.
   * 
   * @return all info levels
   */
  public Page<InfoLevel> findAll(Predicate predicate, Pageable pageable);


}
