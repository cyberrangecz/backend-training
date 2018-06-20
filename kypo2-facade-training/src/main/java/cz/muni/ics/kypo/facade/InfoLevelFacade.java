package cz.muni.ics.kypo.facade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.dto.InfoLevelDTO;

/**
 * @author Pavel Šeda
 *
 */
public interface InfoLevelFacade {

  /**
   * finds specific Info level by id
   * 
   * @param id of a Info level that would be returned
   * @return specific info level by id
   */
  public InfoLevelDTO findById(Long id);

  /**
   * Find all Info Levels.
   * 
   * @return all info levels
   */
  public Page<InfoLevelDTO> findAll(Predicate predicate, Pageable pageable);


}
