package cz.muni.ics.kypo.service.impl;


import java.util.Optional;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.repository.InfoLevelRepository;
import cz.muni.ics.kypo.service.InfoService;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Service
public class InfoServiceImpl implements InfoService {

  private InfoLevelRepository infoRepository;

  @Autowired
  public InfoServiceImpl(InfoLevelRepository infoRepository) {
    this.infoRepository = infoRepository;
  }

  @Override
  public Optional<InfoLevel> findById(Long id) {
    try {
      return infoRepository.findById(id);
    } catch (HibernateException ex) {
      throw new ServiceLayerException(ex.getLocalizedMessage());
    }
  }

}
