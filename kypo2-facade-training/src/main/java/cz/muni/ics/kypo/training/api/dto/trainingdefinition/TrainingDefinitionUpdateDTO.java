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
    @NotNull(message = "{trainingdefinitionupdate.authorRef.NotNull.message}")
    private Set<AuthorRefDTO> authorRef = new HashSet<>();
    @NotNull(message = "{trainingdefinitionupdate.sandboxDefinitionRef.NotNull.message}")
    private SandboxDefinitionRefDTO sandBoxDefinitionRef;
    @NotNull(message = "{trainingdefinitionupdate.showStepperBar.NotNull.message}")
    private boolean showStepperBar;
    @NotNull
    private Long startingLevel;

    @ApiModelProperty(value = "Main identifier of training definition.", required = true)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", required = true)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Description of training definition that is visible to the participant.")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.")
    public String[] getPrerequisities() {
        return prerequisities;
    }

    public void setPrerequisities(String[] prerequisities) {
        this.prerequisities = prerequisities;
    }

    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ")
    public String[] getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }

    @ApiModelProperty(value = "Current state of training definition.", required = true)
    public TDState getState() {
        return state;
    }

    public void setState(TDState state) {
        this.state = state;
    }

    @ApiModelProperty(value = "References to the authors of the training definition.", required = true)
    public Set<AuthorRefDTO> getAuthorRef() {
        return authorRef;
    }

    public void setAuthorRef(Set<AuthorRefDTO> authorRef) {
        this.authorRef = authorRef;
    }

    @ApiModelProperty(value = "Reference to the sandbox definition.", required = true)
    public SandboxDefinitionRefDTO getSandBoxDefinitionRef() {
        return sandBoxDefinitionRef;
    }

    public void setSandBoxDefinitionRef(SandboxDefinitionRefDTO sandBoxDefinitionRef) {
        this.sandBoxDefinitionRef = sandBoxDefinitionRef;
    }

    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", required = true)
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    @ApiModelProperty(value = "Identifier of first level of training definition.", required = true)
    public Long getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(Long startingLevel) {
        this.startingLevel = startingLevel;
    }

    @Override
    public String toString() {
        return "TrainingDefinitionUpdateDTO{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\''
                + ", prerequisities=" + Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state
                + ", authorRef=" + authorRef + ", sandBoxDefinitionRef=" + sandBoxDefinitionRef + ", showStepperBar=" + showStepperBar + '}';
    }
}
