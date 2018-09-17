package cz.muni.ics.kypo.training.api.dto.run;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import io.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel(value = "AccessTrainingRunDTO", description = ".")
public class AccessTrainingRunDTO {
    private AbstractLevelDTO abstractLevelDTO;
    private List<BasicLevelInfoDTO> infoAboutLevels;

    public AbstractLevelDTO getAbstractLevelDTO() {
        return abstractLevelDTO;
    }

    public void setAbstractLevelDTO(AbstractLevelDTO abstractLevelDTO) {
        this.abstractLevelDTO = abstractLevelDTO;
    }

    public List<BasicLevelInfoDTO> getInfoAboutLevels() {
        return infoAboutLevels;
    }

    public void setInfoAboutLevels(List<BasicLevelInfoDTO> infoAboutLevels) {
        this.infoAboutLevels = infoAboutLevels;
    }
}
