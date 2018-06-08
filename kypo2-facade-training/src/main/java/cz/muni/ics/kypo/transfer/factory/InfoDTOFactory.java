package cz.muni.ics.kypo.transfer.factory;

import java.util.Arrays;
import java.util.List;

import org.jsondoc.core.annotation.ApiObject;
import org.springframework.stereotype.Component;

import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.transfer.InfoDTO;
import cz.muni.ics.kypo.transfer.resource.LevelDTOResource;


/**
 * Factory class for info DTO and Resource classes
 * 
 * @author Pavel Šeda (441048)
 *
 */
@Component
@ApiObject(name = "Info DTO Factory", description = "Class for creating Info DTO and Resource classes.")
public class InfoDTOFactory {

  public InfoDTO createInfoDTO(InfoLevel info) {
    return new InfoDTO(info.getId(), info.getDescription(), info.getAuthor());
  }

  public LevelDTOResource<InfoDTO> createInfoDTOsResource(List<InfoDTO> infos) {
    return new LevelDTOResource<>(infos);
  }

  public LevelDTOResource<InfoDTO> createInfoDTOResource(InfoDTO info) {
    return new LevelDTOResource<>(Arrays.asList(info));
  }

}

