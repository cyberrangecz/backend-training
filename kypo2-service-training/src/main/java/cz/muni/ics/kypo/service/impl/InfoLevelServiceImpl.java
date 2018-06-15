package cz.muni.ics.kypo.service.impl;


import java.util.Optional;

import org.hibernate.HibernateException;
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

  private InfoLevelRepository infoRepository;

  @Autowired
  public InfoLevelServiceImpl(InfoLevelRepository infoRepository) {
    this.infoRepository = infoRepository;
  }

  @Override
  public Optional<InfoLevel> findById(long id) {
    try {
      return infoRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public Page<InfoLevel> findAll(Predicate predicate, Pageable pageable) {
    try {
      return infoRepository.findAll(predicate, pageable);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

}
