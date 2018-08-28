package cz.muni.ics.kypo.training.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.model.GameLevel;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public interface GameLevelService {

  /**
   * Finds specific Game Level by id
   * 
   * @param id of a Game Level that would be returned
   * @return specific game level by id
   */
  public Optional<GameLevel> findById(long id);

  /**
   * Find all Game Levels.
   * 
   * @return all game levels
   */
  public Page<GameLevel> findAll(Predicate predicate, Pageable pageable);

  /**
   * updates game level
   * @param gameLevel to be updated
   */
  //public void update(GameLevel gameLevel);

  /**
   * creates game level
   * @param gameLevel to be created
   * @return created game level
   */
  public Optional<GameLevel> create(GameLevel gameLevel);

}
