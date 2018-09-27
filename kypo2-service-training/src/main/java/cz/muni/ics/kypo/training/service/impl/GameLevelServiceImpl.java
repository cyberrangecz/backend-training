package cz.muni.ics.kypo.training.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.repository.GameLevelRepository;
import cz.muni.ics.kypo.training.service.GameLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class GameLevelServiceImpl implements GameLevelService {

  private static final Logger LOG = LoggerFactory.getLogger(GameLevelServiceImpl.class);

  private GameLevelRepository gameLevelRepository;

  @Autowired
  public GameLevelServiceImpl(GameLevelRepository gameLevelRepository) {
    this.gameLevelRepository = gameLevelRepository;
  }

  @Override
  public Optional<GameLevel> findById(long id) {
    LOG.debug("findById({})", id);
    return gameLevelRepository.findById(id);
  }

  @Override
  public Page<GameLevel> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    return gameLevelRepository.findAll(predicate, pageable);
  }
}
