package cz.muni.ics.kypo.transfer.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsondoc.core.annotation.ApiObject;
import org.springframework.stereotype.Component;

import cz.muni.ics.kypo.model.InfoLevel;
import cz.muni.ics.kypo.transfer.InfoDTO;
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
public class InfoDTOFactory {

  public InfoDTO createInfoDTO(InfoLevel info) {
    return new InfoDTO(info.getId(), info.getTitle(), info.getMaxScore(), info.getPreHook(), info.getPostHook(), info.getNextLevel(),
        info.getTrainingDefinition(), info.getTrainingRun(), info.getContent());
  }

  public List<InfoDTO> createInfoDTOs(List<InfoLevel> infoLevels) {
    List<InfoDTO> infoDTOs = new ArrayList<>();
    infoLevels.forEach(infoLevel -> {
      infoDTOs.add(createInfoDTO(infoLevel));
    });
    return infoDTOs;
  }

  public InfoLevelsDTOResource<InfoDTO> createInfoDTOsResource(InfoDTO infoLevel) {
    return new InfoLevelsDTOResource<>(Arrays.asList(infoLevel));
  }

  public InfoLevelsDTOResource<InfoDTO> createInfoDTOsResource(List<InfoDTO> infoLevels) {
    return new InfoLevelsDTOResource<>(infoLevels);
  }

  public InfoLevelsDTOResource<InfoDTO> createInfoDTOsResource(List<InfoDTO> infoLevels, ResultInfoDTO resultInfo) {
    return new InfoLevelsDTOResource<>(resultInfo, infoLevels);
  }

}
