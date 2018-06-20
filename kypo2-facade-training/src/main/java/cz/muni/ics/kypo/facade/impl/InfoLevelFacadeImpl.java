package cz.muni.ics.kypo.facade.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.InfoLevelFacade;
import cz.muni.ics.kypo.mapping.BeanMapping;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.service.InfoLevelService;
import cz.muni.ics.kypo.transfer.InfoLevelDTO;
import cz.muni.ics.kypo.transfer.ResultInfoDTO;
import cz.muni.ics.kypo.transfer.factory.InfoLevelDTOFactory;
import cz.muni.ics.kypo.transfer.resource.InfoLevelsDTOResource;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class InfoLevelFacadeImpl implements InfoLevelFacade {

  private InfoLevelService infoService;
  private InfoLevelDTOFactory infoDTOFactory;
  private BeanMapping beanMapping;

  @Autowired
  public InfoLevelFacadeImpl(InfoLevelService infoService, InfoLevelDTOFactory infoDTOFactory, BeanMapping beanMapping) {
    this.infoService = infoService;
    this.infoDTOFactory = infoDTOFactory;
    this.beanMapping = beanMapping;
  }

  @Override
  @Transactional(readOnly = true)
  public InfoLevelsDTOResource<InfoLevelDTO> findById(Long id) {
    try {
      Objects.requireNonNull(id);
      Optional<InfoLevel> info = infoService.findById(id);
      InfoLevel inf = info.orElseThrow(() -> new ServiceLayerException("Info with this id is not found"));
      InfoLevelDTO infoDTO = beanMapping.mapTo(inf, InfoLevelDTO.class);
      return infoDTOFactory.createInfoDTOsResource(infoDTO);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given info ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public InfoLevelsDTOResource<InfoLevelDTO> findAll(Predicate predicate, Pageable pageable) {
    try {
      Page<InfoLevel> infoLevels = infoService.findAll(predicate, pageable);
      List<InfoLevelDTO> infoDTOs = beanMapping.mapTo(infoLevels.getContent(), InfoLevelDTO.class);
      return infoDTOFactory.createInfoDTOsResource(infoDTOs, new ResultInfoDTO(infoLevels.getNumber(), infoLevels.getNumberOfElements(), infoLevels.getSize(),
          infoLevels.getTotalElements(), infoLevels.getTotalPages()));
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
