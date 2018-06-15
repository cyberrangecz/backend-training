package cz.muni.ics.kypo.facade;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.model.GameLevel;
import cz.muni.ics.kypo.transfer.GameLevelDTO;
import cz.muni.ics.kypo.transfer.resource.GameLevelsDTOResource;

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
  public GameLevelsDTOResource<GameLevelDTO> findById(long id);

  /**
   * Find all Game Levels.
   * 
   * @return all game levels
   */
  public GameLevelsDTOResource<List<GameLevelDTO>> findAll(Predicate predicate, Pageable pageable);
}
