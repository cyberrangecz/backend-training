package cz.muni.ics.kypo.service.impl;


import java.util.Optional;

import com.mysema.commons.lang.Assert;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.repository.InfoLevelRepository;
import cz.muni.ics.kypo.service.InfoLevelService;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class InfoLevelServiceImpl implements InfoLevelService {

  private static final Logger LOG = LoggerFactory.getLogger(InfoLevelServiceImpl.class);

  private InfoLevelRepository infoRepository;

  @Autowired
  public InfoLevelServiceImpl(InfoLevelRepository infoRepository) {
    this.infoRepository = infoRepository;
  }

  @Override
  public Optional<InfoLevel> findById(long id) {
    LOG.debug("findById({})", id);
    try {
      return infoRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<InfoLevel> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return infoRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      System.out.println("Error message is: " + ex);
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public void update(InfoLevel infoLevel) {
    LOG.debug("update({})", infoLevel);
    Assert.notNull(infoLevel, "Info level must not be null");
    infoRepository.saveAndFlush(infoLevel);
    LOG.info("Info Level with id: "+ infoLevel.getId() + " updated");
  }

  @Override
  public Optional<InfoLevel> create(InfoLevel infoLevel) {
    LOG.debug("create ({})", infoLevel);
    Assert.notNull(infoLevel, "Info level must not be null");
    InfoLevel iL = infoRepository.save(infoLevel);
    LOG.info("Info level with id: "+ iL.getId() +" created");
    return Optional.of(iL);
  }
}
