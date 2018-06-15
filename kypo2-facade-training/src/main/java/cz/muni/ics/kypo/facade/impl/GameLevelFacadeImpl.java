package cz.muni.ics.kypo.facade.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.GameLevelFacade;
import cz.muni.ics.kypo.model.GameLevel;
import cz.muni.ics.kypo.service.GameLevelService;
import cz.muni.ics.kypo.transfer.GameLevelDTO;
import cz.muni.ics.kypo.transfer.factory.GameLevelDTOFactory;
import cz.muni.ics.kypo.transfer.resource.GameLevelsDTOResource;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class GameLevelFacadeImpl implements GameLevelFacade {

  private GameLevelService gameLevelService;
  private GameLevelDTOFactory gameLevelDTOFactory;

  @Autowired
  public GameLevelFacadeImpl(GameLevelService gameLevelService, GameLevelDTOFactory gameLevelDTOFactory) {
    this.gameLevelService = gameLevelService;
    this.gameLevelDTOFactory = gameLevelDTOFactory;
  }

  @Override
  @Transactional(readOnly = true)
  public GameLevelsDTOResource<GameLevelDTO> findById(long id) {
    try {
      Optional<GameLevel> gameLevel = gameLevelService.findById(id);
      GameLevel game = gameLevel.orElseThrow(() -> new ServiceLayerException("Info with this id is not found."));

      return null;
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public GameLevelsDTOResource<List<GameLevelDTO>> findAll(Predicate predicate, Pageable pageable) {
    try {
      Page<GameLevel> gameLevels = gameLevelService.findAll(predicate, pageable);
      // List<GameLevelDTO> gameLevelDTOs =
      return null;
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
