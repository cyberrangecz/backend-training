package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.model.GameLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

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
}
