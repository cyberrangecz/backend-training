package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.hint.TakenHintDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "AccessTrainingRunDTO", description = ".")
public class AccessTrainingRunDTO {

    private Long trainingRunID;
    private boolean showStepperBar;
    private Long sandboxInstanceId;
    private AbstractLevelDTO abstractLevelDTO;
    private List<BasicLevelInfoDTO> infoAboutLevels;
    private Long instanceId;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    private String takenSolution;
    private List<TakenHintDTO> takenHints = new ArrayList<>();

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

    @ApiModelProperty(value = "Main identifier of sandbox which is assigned to training run.", example = "2")
    public Long getSandboxInstanceId() {
        return sandboxInstanceId;
    }

    public void setSandboxInstanceId(Long sandboxInstanceId) {
        this.sandboxInstanceId = sandboxInstanceId;
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

    @ApiModelProperty(value = "Id of associated training instance", example = "1")
    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getTakenSolution() {
        return takenSolution;
    }

    public void setTakenSolution(String takenSolution) {
        this.takenSolution = takenSolution;
    }

    public List<TakenHintDTO> getTakenHints() {
        return takenHints;
    }

    public void setTakenHints(List<TakenHintDTO> takenHints) {
        this.takenHints = takenHints;
    }

    public void addTakenHint(TakenHintDTO takenHintDTO) {
        this.takenHints.add(takenHintDTO);
    }


    @Override public String toString() {
        return "AccessTrainingRunDTO{" + "trainingRunID=" + trainingRunID + ", showStepperBar=" + showStepperBar + ", sandboxInstanceId="
            + sandboxInstanceId + ", abstractLevelDTO=" + abstractLevelDTO + ", infoAboutLevels=" + infoAboutLevels + ", instanceId="
            + instanceId + ", startTime=" + startTime + '}';
    }
}
