package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.model.InfoLevel;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.InfoLevelDTO;

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
  public PageResultResource<InfoLevelDTO> findAll(Predicate predicate, Pageable pageable);

  /**
   * creates new info level
   * @param infoLevel to be created
   * @return DTO of new info level
   */
  public InfoLevelDTO create(InfoLevel infoLevel);
}
