package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.model.GameLevel;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.GameLevelDTO;

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
  public PageResultResource<GameLevelDTO> findAll(Predicate predicate, Pageable pageable);

  /**
   * creates new game level
   * @param gameLevel to be created
   * @return DTO of new game level
   */
  public GameLevelDTO create(GameLevel gameLevel);
}
