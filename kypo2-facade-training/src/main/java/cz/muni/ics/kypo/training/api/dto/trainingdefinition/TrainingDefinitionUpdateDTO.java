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
@ApiModel(value = "TrainingDefinitionUpdateDTO", description = "Training Definition to update.")
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
    @ApiModelProperty(required = true)
    private Long startingLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getPrerequisities() {
        return prerequisities;
    }

    public void setPrerequisities(String[] prerequisities) {
        this.prerequisities = prerequisities;
    }

    public String[] getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }

    public TDState getState() {
        return state;
    }

    public void setState(TDState state) {
        this.state = state;
    }

    public Set<AuthorRefDTO> getAuthorRef() {
        return authorRef;
    }

    public void setAuthorRef(Set<AuthorRefDTO> authorRef) {
        this.authorRef = authorRef;
    }

    public SandboxDefinitionRefDTO getSandBoxDefinitionRef() {
        return sandBoxDefinitionRef;
    }

    public void setSandBoxDefinitionRef(SandboxDefinitionRefDTO sandBoxDefinitionRef) {
        this.sandBoxDefinitionRef = sandBoxDefinitionRef;
    }

    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

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
