package cz.muni.ics.kypo.training.api.dto.run;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "AccessTrainingRunDTO", description = ".")
public class AccessTrainingRunDTO {

    private Long trainingRunID;
    private boolean showStepperBar;
    private AbstractLevelDTO abstractLevelDTO;
    private List<BasicLevelInfoDTO> infoAboutLevels;

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    public Long getTrainingRunID() {
        return trainingRunID;
    }

    public void setTrainingRunID(Long trainingRunID) {
        this.trainingRunID = trainingRunID;
    }

    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    @ApiModelProperty(value = "First level in the current training run.")
    public AbstractLevelDTO getAbstractLevelDTO() {
        return abstractLevelDTO;
    }

    public void setAbstractLevelDTO(AbstractLevelDTO abstractLevelDTO) {
        this.abstractLevelDTO = abstractLevelDTO;
    }

    @ApiModelProperty(value = "Information about all levels in training instance.")
    public List<BasicLevelInfoDTO> getInfoAboutLevels() {
        return infoAboutLevels;
    }

    public void setInfoAboutLevels(List<BasicLevelInfoDTO> infoAboutLevels) {
        this.infoAboutLevels = infoAboutLevels;
    }

    @Override
    public String toString() {
        return "AccessTrainingRunDTO{" +
                "trainingRunID=" + trainingRunID +
                ", showStepperBar=" + showStepperBar +
                ", abstractLevelDTO=" + abstractLevelDTO +
                ", infoAboutLevels=" + infoAboutLevels +
                '}';
    }
}
