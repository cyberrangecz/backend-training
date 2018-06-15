package cz.muni.ics.kypo.facade;

import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.transfer.InfoLevelDTO;
import cz.muni.ics.kypo.transfer.resource.InfoLevelsDTOResource;

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
  public InfoLevelsDTOResource<InfoLevelDTO> findById(Long id);

  /**
   * Find all Info Levels.
   * 
   * @return all info levels
   */
  public InfoLevelsDTOResource<InfoLevelDTO> findAll(Predicate predicate, Pageable pageable);


}
