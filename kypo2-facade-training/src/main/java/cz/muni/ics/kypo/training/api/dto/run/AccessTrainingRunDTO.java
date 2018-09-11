package cz.muni.ics.kypo.training.api.dto.run;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicInfoLevelDTO;
import io.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel(value = "AccessTrainingRunDTO", description = ".")
public class AccessTrainingRunDTO {
    private AbstractLevelDTO abstractLevelDTO;
    private List<BasicInfoLevelDTO> infoAboutLevels;

    public AbstractLevelDTO getAbstractLevelDTO() {
        return abstractLevelDTO;
    }

    public void setAbstractLevelDTO(AbstractLevelDTO abstractLevelDTO) {
        this.abstractLevelDTO = abstractLevelDTO;
    }

    public List<BasicInfoLevelDTO> getInfoLevels() {
        return infoAboutLevels;
    }

    public void setInfoLevels(List<BasicInfoLevelDTO> infoLevels) {
        this.infoAboutLevels = infoLevels;
    }
}
