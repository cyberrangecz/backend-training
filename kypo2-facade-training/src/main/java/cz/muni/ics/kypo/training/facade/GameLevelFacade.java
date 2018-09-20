package cz.muni.ics.kypo.training.facade;

import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;

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
  GameLevelDTO findById(long id);

  /**
   * Find all Game Levels.
   * 
   * @return all game levels
   */
  PageResultResource<GameLevelDTO> findAll(Predicate predicate, Pageable pageable);
}
