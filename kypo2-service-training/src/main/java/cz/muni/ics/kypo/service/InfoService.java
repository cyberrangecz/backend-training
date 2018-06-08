package cz.muni.ics.kypo.service;

import java.util.Optional;

import cz.muni.ics.kypo.model.InfoLevel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface InfoService {

  /**
   * finds specific Info level by id
   * 
   * @param id of a Info level that would be returned
   * @return specific info level by id
   */
  public Optional<InfoLevel> findById(Long id);

}
