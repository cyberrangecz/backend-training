package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * Encapsulates information about Training Definition, intended for edit of the definition.
 *
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
    @NotNull(message = "{trainingdefinitioncreate.authors.NotNull.message}")
    private Set<Long> authorsRefIds = new HashSet<>();
    @Valid
    private BetaTestingGroupUpdateDTO betaTestingGroup;
    private Long sandboxDefinitionRefId;
    @NotNull(message = "{trainingdefinitionupdate.showStepperBar.NotNull.message}")
    private boolean showStepperBar;

    /**
     * Gets id.
     *
     * @return the id
     */
    @ApiModelProperty(value = "Main identifier of training definition.", required = true, example = "2")
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", required = true, example = "TrainingDefinition2")
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Unreleased training definition")
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get prerequisites.
     *
     * @return the prerequisites.
     */
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "[phishing]")
    public String[] getPrerequisities() {
        return prerequisities;
    }

    /**
     * Sets prerequisites.
     *
     * @param prerequisities the prerequisites.
     */
    public void setPrerequisities(String[] prerequisities) {
        this.prerequisities = prerequisities;
    }

    /**
     * Get outcomes.
     *
     * @return the outcomes
     */
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "")
    public String[] getOutcomes() {
        return outcomes;
    }

    /**
     * Sets outcomes.
     *
     * @param outcomes the outcomes
     */
    public void setOutcomes(String[] outcomes) {
        this.outcomes = outcomes;
    }

    /**
     * Gets state.
     *
     * @return the {@link TDState}
     */
    @ApiModelProperty(value = "Current state of training definition.", required = true, example = "UNRELEASED")
    public TDState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the {@link TDState}
     */
    public void setState(TDState state) {
        this.state = state;
    }

    /**
     * Gets authors ids.
     *
     * @return the authors ids
     */
    @ApiModelProperty(value = "References (IDs) to the authors of the training definition.", required = true, example = "[1]")
    public Set<Long> getAuthorsRefIds() {
        return authorsRefIds;
    }

    public void setAuthorsRefIds(Set<Long> authorsRefIds) {
        this.authorsRefIds = authorsRefIds;
    }

    /**
     * Gets beta testing group.
     *
     * @return the {@link BetaTestingGroupUpdateDTO}
     */
    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.", required = true)
    public BetaTestingGroupUpdateDTO getBetaTestingGroup() {
        return betaTestingGroup;
    }

    /**
     * Sets beta testing group.
     *
     * @param betaTestingGroup the {@link BetaTestingGroupUpdateDTO}
     */
    public void setBetaTestingGroup(BetaTestingGroupUpdateDTO betaTestingGroup) {
        this.betaTestingGroup = betaTestingGroup;
    }

    /**
     * Gets sandbox definition ref id.
     *
     * @return the sandbox definition ref id
     */
    @ApiModelProperty(value = "Reference to the sandbox definition.", required = true, example = "1")
    public Long getSandboxDefinitionRefId() {
        return sandboxDefinitionRefId;
    }

    /**
     * Sets sandbox definition ref id.
     *
     * @param sandBoxDefinitionRef the sand box definition ref
     */
    public void setSandboxDefinitionRefId(Long sandBoxDefinitionRef) {
        this.sandboxDefinitionRefId = sandBoxDefinitionRef;
    }

    /**
     * Gets if stepper bar is shown while in run.
     *
     * @return true if bar is shown
     */
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", required = true, example = "false")
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

    @Override
    public String toString() {
        return "TrainingDefinitionUpdateDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", prerequisities=" + Arrays.toString(prerequisities) +
                ", outcomes=" + Arrays.toString(outcomes) +
                ", state=" + state +
                ", authorsRefIds=" + authorsRefIds +
                ", sandboxDefinitionRefId=" + sandboxDefinitionRefId +
                ", showStepperBar=" + showStepperBar +
                '}';
    }
}
