package cz.muni.ics.kypo.facade;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.transfer.InfoDTO;
import cz.muni.ics.kypo.transfer.resource.InfoLevelsDTOResource;

/**
 * @author Pavel Šeda
 *
 */
public interface InfoFacade {

  /**
   * finds specific Info level by id
   * 
   * @param id of a Info level that would be returned
   * @return specific info level by id
   */
  public InfoLevelsDTOResource<InfoDTO> findById(Long id);

  /**
   * Find all Info Levels.
   * 
   * @return all info levels
   */
  public InfoLevelsDTOResource<InfoDTO> findAll(Predicate predicate, Pageable pageable);


}
