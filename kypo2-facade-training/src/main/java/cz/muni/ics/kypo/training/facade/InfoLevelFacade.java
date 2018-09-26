package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import org.springframework.data.domain.Pageable;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;

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
  InfoLevelDTO findById(Long id);

  /**
   * Find all Info Levels.
   * 
   * @return all info levels
   */
  PageResultResource<InfoLevelDTO> findAll(Predicate predicate, Pageable pageable);
}
