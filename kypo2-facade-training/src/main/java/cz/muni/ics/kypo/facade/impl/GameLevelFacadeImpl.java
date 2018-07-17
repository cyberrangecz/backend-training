package cz.muni.ics.kypo.facade.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.GameLevelDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.GameLevelFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.GameLevel;
import cz.muni.ics.kypo.service.GameLevelService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class GameLevelFacadeImpl implements GameLevelFacade {

  private static final Logger LOG = LoggerFactory.getLogger(GameLevelFacadeImpl.class);

  private GameLevelService gameLevelService;
  private BeanMapping beanMapping;

  @Autowired
  public GameLevelFacadeImpl(GameLevelService gameLevelService, BeanMapping beanMapping) {
    this.gameLevelService = gameLevelService;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)
  public GameLevelDTO findById(long id) {
    LOG.debug("findById({})", id);
    try {
      Optional<GameLevel> gameLevel = gameLevelService.findById(id);
      GameLevel game = gameLevel.orElseThrow(() -> new ServiceLayerException("GameLevel with this id is not found."));
      return beanMapping.mapTo(game, GameLevelDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given GameLevel ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<GameLevelDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(gameLevelService.findAll(predicate, pageable), GameLevelDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
