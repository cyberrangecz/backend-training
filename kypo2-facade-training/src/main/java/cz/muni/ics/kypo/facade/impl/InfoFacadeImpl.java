package cz.muni.ics.kypo.facade.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.facade.InfoFacade;
import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.service.InfoService;
import cz.muni.ics.kypo.transfer.InfoDTO;
import cz.muni.ics.kypo.transfer.factory.InfoDTOFactory;
import cz.muni.ics.kypo.transfer.resource.LevelDTOResource;

/**
 * @author Pavel Šeda
 *
 */
@Service
@Transactional
public class InfoFacadeImpl implements InfoFacade {

  private InfoService infoService;
  private InfoDTOFactory infoDTOFactory;

  @Autowired
  public InfoFacadeImpl(InfoService infoService, InfoDTOFactory infoDTOFactory) {
    this.infoService = infoService;
    this.infoDTOFactory = infoDTOFactory;
  }

  @Override
  @Transactional(readOnly = true)
  public LevelDTOResource<InfoDTO> findById(Long id) {
    try {
      Objects.requireNonNull(id);
      Optional<InfoLevel> info = infoService.findById(id);
      InfoLevel inf = info.orElseThrow(() -> new ServiceLayerException("Info with this id is not found"));
      InfoDTO infoDTO = infoDTOFactory.createInfoDTO(inf);
      return infoDTOFactory.createInfoDTOResource(infoDTO);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given info id is null");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

}
