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

/**
 * Encapsulates information about Training Run, intended as a response to run accessing.
 */
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

    /**
     * Gets training run id.
     *
     * @return the training run id
     */
    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    public Long getTrainingRunID() {
        return trainingRunID;
    }

    /**
     * Sets training run id.
     *
     * @param trainingRunID the training run id
     */
    public void setTrainingRunID(Long trainingRunID) {
        this.trainingRunID = trainingRunID;
    }

    /**
     * Gets if stepper bar is shown while in run.
     *
     * @return true if bar is shown
     */
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    /**
     * Sets if stepper bar is shown while in run.
     *
     * @param showStepperBar true if bar is shown
     */
    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    /**
     * Gets sandbox instance id.
     *
     * @return the sandbox instance id
     */
    @ApiModelProperty(value = "Main identifier of sandbox which is assigned to training run.", example = "2")
    public Long getSandboxInstanceId() {
        return sandboxInstanceId;
    }

    /**
     * Sets sandbox instance id.
     *
     * @param sandboxInstanceId the sandbox instance id
     */
    public void setSandboxInstanceId(Long sandboxInstanceId) {
        this.sandboxInstanceId = sandboxInstanceId;
    }

    /**
     * Gets current level.
     *
     * @return the {@link AbstractLevelDTO}
     */
    @ApiModelProperty(value = "First level in the current training run.")
    public AbstractLevelDTO getAbstractLevelDTO() {
        return abstractLevelDTO;
    }

    /**
     * Sets current level.
     *
     * @param abstractLevelDTO the {@link AbstractLevelDTO}
     */
    public void setAbstractLevelDTO(AbstractLevelDTO abstractLevelDTO) {
        this.abstractLevelDTO = abstractLevelDTO;
    }

    /**
     * Gets basic info about all levels.
     *
     * @return the list of {@link BasicLevelInfoDTO}
     */
    @ApiModelProperty(value = "Information about all levels in training instance.")
    public List<BasicLevelInfoDTO> getInfoAboutLevels() {
        return infoAboutLevels;
    }

    /**
     * Sets basic info about all levels.
     *
     * @param infoAboutLevels the list of {@link BasicLevelInfoDTO}
     */
    public void setInfoAboutLevels(List<BasicLevelInfoDTO> infoAboutLevels) {
        this.infoAboutLevels = infoAboutLevels;
    }

    /**
     * Gets instance id.
     *
     * @return the instance id
     */
    @ApiModelProperty(value = "Id of associated training instance", example = "1")
    public Long getInstanceId() {
        return instanceId;
    }

    /**
     * Sets instance id.
     *
     * @param instanceId the instance id
     */
    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Gets start time.
     *
     * @return the start time
     */
    @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets taken solution.
     *
     * @return the taken solution
     */
    public String getTakenSolution() {
        return takenSolution;
    }

    /**
     * Sets taken solution.
     *
     * @param takenSolution the taken solution
     */
    public void setTakenSolution(String takenSolution) {
        this.takenSolution = takenSolution;
    }

    /**
     * Gets taken hints.
     *
     * @return the list of {@link TakenHintDTO}
     */
    public List<TakenHintDTO> getTakenHints() {
        return takenHints;
    }

    /**
     * Sets taken hints.
     *
     * @param takenHints the list of {@link TakenHintDTO}
     */
    public void setTakenHints(List<TakenHintDTO> takenHints) {
        this.takenHints = takenHints;
    }

    /**
     * Add taken hint to list of taken hints.
     *
     * @param takenHintDTO the {@link TakenHintDTO}
     */
    public void addTakenHint(TakenHintDTO takenHintDTO) {
        this.takenHints.add(takenHintDTO);
    }


    @Override public String toString() {
        return "AccessTrainingRunDTO{" + "trainingRunID=" + trainingRunID + ", showStepperBar=" + showStepperBar + ", sandboxInstanceId="
            + sandboxInstanceId + ", abstractLevelDTO=" + abstractLevelDTO + ", infoAboutLevels=" + infoAboutLevels + ", instanceId="
            + instanceId + ", startTime=" + startTime + '}';
    }
}
