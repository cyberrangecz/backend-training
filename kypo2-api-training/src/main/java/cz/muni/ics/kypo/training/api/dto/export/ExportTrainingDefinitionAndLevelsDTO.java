package cz.muni.ics.kypo.training.api.dto.export;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Pavel Seda
 */
public class ExportTrainingDefinitionAndLevelsDTO {

    private String title;
    private String description;
    private String[] prerequisities;
    private String[] outcomes;
    private TDState state;
    private boolean showStepperBar;
    private List<AbstractLevelExportDTO> levels = new ArrayList<>();
    private int estimatedDuration;
    private Long sandboxDefinitionRefId;

    public ExportTrainingDefinitionAndLevelsDTO() {
    }

    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Unreleased training definition")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
    public String[] getPrerequisities() {
        return prerequisities;
    }

    public void setPrerequisities(String[] prerequisities) {
        this.prerequisities = prerequisities;
    }

    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "")
    public String[] getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }

    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    public TDState getState() {
        return state;
    }

    public void setState(TDState state) {
        this.state = state;
    }

    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    public List<AbstractLevelExportDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<AbstractLevelExportDTO> levels) {
        this.levels = levels;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Long getSandboxDefinitionRefId() {
        return sandboxDefinitionRefId;
    }

    public void setSandboxDefinitionRefId(Long sandboxDefinitionRefId) {
        this.sandboxDefinitionRefId = sandboxDefinitionRefId;
    }

    @Override public String toString() {
        return "ExportTrainingDefinitionAndLevelsDTO{" + "title='" + title + '\'' + ", description='" + description + '\''
            + ", prerequisities=" + Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state
            + ", showStepperBar=" + showStepperBar + ", levels=" + levels + ", estimatedDuration=" + estimatedDuration
            + ", sandboxDefinitionRefId=" + sandboxDefinitionRefId + '}';
    }
}
