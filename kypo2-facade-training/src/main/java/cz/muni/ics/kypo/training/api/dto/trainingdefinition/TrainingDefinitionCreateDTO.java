package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import java.util.Arrays;
import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import cz.muni.ics.kypo.training.api.dto.AuthorRefDTO;
import cz.muni.ics.kypo.training.api.dto.SandboxDefinitionRefDTO;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "TrainingDefinitionCreateDTO", description = "Training definition to create.")
public class TrainingDefinitionCreateDTO {
    @NotEmpty(message = "{trainingdefinitioncreate.title.NotEmpty.message}")
    private String title;
    private String description;
    private String[] prerequisities;
    private String[] outcomes;
    @NotNull(message = "{trainingdefinitioncreate.state.NotNull.message}")
    private TDState state;
    @NotNull(message = "{trainingdefinitioncreate.autIds.NotNull.message}")
    private Set<Long> autIds;
    @NotNull(message = "{trainingdefinitioncreate.showStepperBar.NotNull.message}")
    private boolean showStepperBar;
    @NotNull(message = "{trainingdefinitioncreate.sandboxDefinitionRef.NotNull.message}")
    private Long sandboxDefinitionRef;


    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", required = true, example = "Photo Hunter")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Description of Photo Hunter")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "[HTML, http protocol]")
    public String[] getPrerequisities() {
        return prerequisities;
    }

    public void setPrerequisities(String[] prerequisities) {
        this.prerequisities = prerequisities;
    }
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "[outcomes]")
    public String[] getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }

    @ApiModelProperty(value = "Current state of training definition.", required = true, example = "UNRELEASED")
    public TDState getState() {
        return state;
    }

    public void setState(TDState state) {
        this.state = state;
    }

    @ApiModelProperty(value = "References to the authors of the training definition.", required = true, example = "[1]")
    public Set<Long> getAutIds() {
        return autIds;
    }

    public void setAutIds(Set<Long> autIds) {
        this.autIds = autIds;
    }

    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", required = true, example = "true")
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    @ApiModelProperty(value = "Reference to the sandbox definition.", required = true, example = "1")
    public Long getSandboxDefinitionRef() {
        return sandboxDefinitionRef;
    }

    public void setSandboxDefinitionRef(Long sandboxDefinitionRef) {
        this.sandboxDefinitionRef = sandboxDefinitionRef;
    }

    @Override public String toString() {
        return "TrainingDefinitionCreateDTO{" + "title='" + title + '\'' + ", description='" + description + '\'' + ", prerequisities="
            + Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state + ", autIds=" + autIds
            + ", showStepperBar=" + showStepperBar + ", sandboxDefinitionRef=" + sandboxDefinitionRef + '}';
    }
}
