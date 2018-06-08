package cz.muni.ics.kypo.facade;

import cz.muni.ics.kypo.transfer.InfoDTO;
import cz.muni.ics.kypo.transfer.resource.LevelDTOResource;

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
  public LevelDTOResource<InfoDTO> findById(Long id);

}
