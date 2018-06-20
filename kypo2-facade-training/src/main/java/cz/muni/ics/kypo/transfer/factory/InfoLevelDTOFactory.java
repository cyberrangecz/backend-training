package cz.muni.ics.kypo.transfer.factory;

import java.util.Arrays;
import java.util.List;

import org.jsondoc.core.annotation.ApiObject;
import org.springframework.stereotype.Component;

import cz.muni.ics.kypo.transfer.InfoLevelDTO;
import cz.muni.ics.kypo.transfer.ResultInfoDTO;
import cz.muni.ics.kypo.transfer.resource.InfoLevelsDTOResource;

/**
 * Factory class for info DTO and Resource classes
 * 
 * @author Pavel Šeda (441048)
 *
 */
@Component
@ApiObject(name = "Info DTO Factory", description = "Class for creating Info DTO and Resource classes.")
public class InfoLevelDTOFactory {

  public InfoLevelsDTOResource<InfoLevelDTO> createInfoDTOsResource(InfoLevelDTO infoLevel) {
    return new InfoLevelsDTOResource<>(Arrays.asList(infoLevel));
  }

  public InfoLevelsDTOResource<InfoLevelDTO> createInfoDTOsResource(List<InfoLevelDTO> infoLevels) {
    return new InfoLevelsDTOResource<>(infoLevels);
  }

  public InfoLevelsDTOResource<InfoLevelDTO> createInfoDTOsResource(List<InfoLevelDTO> infoLevels, ResultInfoDTO resultInfo) {
    return new InfoLevelsDTOResource<>(resultInfo, infoLevels);
  }

}
