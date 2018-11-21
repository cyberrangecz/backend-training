package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import java.util.Arrays;
import java.util.HashSet;
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
@ApiModel(value = "TrainingDefinitionUpdateDTO", description = "Training definition to update.")
public class TrainingDefinitionUpdateDTO {

    @NotNull(message = "{trainingdefinitionupdate.id.NotNull.message}")
    private Long id;
    @NotEmpty(message = "{trainingdefinitionupdate.title.NotEmpty.message}")
    private String title;
    private String description;
    private String[] prerequisities;
    private String[] outcomes;
    @NotNull(message = "{trainingdefinitionupdate.state.NotNull.message}")
    private TDState state;
    @NotNull(message = "{trainingdefinitionupdate.autIds.NotNull.message}")
    private Set<Long> autIds = new HashSet<>();
    @NotNull(message = "{trainingdefinitionupdate.sandboxDefinitionRef.NotNull.message}")
    private Long sandboxDefinitionRef;
    @NotNull(message = "{trainingdefinitionupdate.showStepperBar.NotNull.message}")
    private boolean showStepperBar;

    @ApiModelProperty(value = "Main identifier of training definition.", required = true, example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", required = true, example = "TrainingDefinition2")
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

    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "[phishing]")
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

    @ApiModelProperty(value = "Reference to the sandbox definition.", required = true, example = "1")
    public Long getSandboxDefinitionRef() {
        return sandboxDefinitionRef;
    }

    public void setSandboxDefinitionRef(Long sandBoxDefinitionRef) {
        this.sandboxDefinitionRef = sandBoxDefinitionRef;
    }

    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", required = true, example = "false")
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    @Override
    public String toString() {
        return "TrainingDefinitionUpdateDTO{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\''
                + ", prerequisities=" + Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state
                + ", autIds=" + autIds + ", sandBoxDefinitionRef=" + sandboxDefinitionRef + ", showStepperBar=" + showStepperBar + '}';
    }
}
