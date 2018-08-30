package cz.muni.ics.kypo.training.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.InfoLevelFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.model.InfoLevel;
import cz.muni.ics.kypo.training.service.InfoLevelService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class InfoLevelFacadeImpl implements InfoLevelFacade {

  private static final Logger LOG = LoggerFactory.getLogger(GameLevelFacadeImpl.class);

  private InfoLevelService infoService;
  private BeanMapping beanMapping;

  @Autowired
  public InfoLevelFacadeImpl(InfoLevelService infoService, BeanMapping beanMapping) {
    this.infoService = infoService;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)
  public InfoLevelDTO findById(Long id) {
    LOG.debug("findById({})", id);
    try {
      Objects.requireNonNull(id);
      Optional<InfoLevel> info = infoService.findById(id);
      InfoLevel inf = info.orElseThrow(() -> new ServiceLayerException("Info with this id is not found"));
      return beanMapping.mapTo(inf, InfoLevelDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given info ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<InfoLevelDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(infoService.findAll(predicate, pageable), InfoLevelDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }
  
  @Transactional
  @Override
  public InfoLevelDTO create(InfoLevel infoLevel) {
    LOG.debug("create({})", infoLevel);
    try{
      Objects.requireNonNull(infoLevel);
      InfoLevel iL = infoService.create(infoLevel).orElseThrow(() -> new ServiceLayerException());
      return beanMapping.mapTo(iL, InfoLevelDTO.class);
    } catch (NullPointerException | ServiceLayerException ex){
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
