package cz.muni.ics.kypo.training.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.model.InfoLevel;
import cz.muni.ics.kypo.training.repository.InfoLevelRepository;
import cz.muni.ics.kypo.training.service.InfoLevelService;
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
      return infoRepository.findById(id);
  }

  @Override
  public Page<InfoLevel> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
      return infoRepository.findAll(predicate, pageable);
  }
}
