package cz.muni.ics.kypo.facade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.dto.GameLevelDTO;

/**
 * @author Pavel Šeda
 *
 */
public interface GameLevelFacade {

  /**
   * Finds specific Game Level by id
   * 
   * @param id of a Game Level that would be returned
   * @return specific game level by id
   */
  public GameLevelDTO findById(long id);

  /**
   * Find all Game Levels.
   * 
   * @return all game levels
   */
  public Page<GameLevelDTO> findAll(Predicate predicate, Pageable pageable);
}
