package cz.muni.ics.kypo.service.impl;

import java.util.Optional;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.GameLevel;
import cz.muni.ics.kypo.repository.GameLevelRepository;
import cz.muni.ics.kypo.service.GameLevelService;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class GameLevelServiceImpl implements GameLevelService {

  private GameLevelRepository gameLevelRepository;

  @Autowired
  public GameLevelServiceImpl(GameLevelRepository gameLevelRepository) {
    this.gameLevelRepository = gameLevelRepository;
  }

  @Override
  public Optional<GameLevel> findById(long id) {
    try {
      return gameLevelRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<GameLevel> findAll(Predicate predicate, Pageable pageable) {
    try {
      return gameLevelRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

}
