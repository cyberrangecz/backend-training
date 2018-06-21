package cz.muni.ics.kypo.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.InfoLevelFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.service.InfoLevelService;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class InfoLevelFacadeImpl implements InfoLevelFacade {

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
    try {
      return beanMapping.mapToPageResultDTO(infoService.findAll(predicate, pageable), InfoLevelDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
