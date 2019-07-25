package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Set;


/**
 * Encapsulates information about Training definition, intended for creation of new definition.
 *
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
    @NotNull(message = "{trainingdefinitioncreate.authors.NotNull.message}")
    private Set<Long> authorsRefIds;
    @Valid
    private BetaTestingGroupCreateDTO betaTestingGroup;
    @NotNull(message = "{trainingdefinitioncreate.showStepperBar.NotNull.message}")
    private boolean showStepperBar;
    private Long sandboxDefinitionRefId;

    /**
     * Gets title.
     *
     * @return the title
     */
    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", required = true, example = "Photo Hunter")
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
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Description of Photo Hunter")
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
     * @return the prerequisites
     */
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "[HTML, http protocol]")
    public String[] getPrerequisities() {
        return prerequisities;
    }

    /**
     * Sets prerequisites.
     *
     * @param prerequisities the prerequisites
     */
    public void setPrerequisities(String[] prerequisities) {
        this.prerequisities = prerequisities;
    }

    /**
     * Get outcomes.
     *
     * @return the outcomes
     */
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "[outcomes]")
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
     * Gets authors user ref ids.
     *
     * @return the authors user ref ids
     */
    @ApiModelProperty(value = "References to the authors of the training definition.", required = true)
    public Set<Long> getAuthorsRefIds() {
        return authorsRefIds;
    }

    /**
     * Sets authors user ref ids.
     *
     * @param authorsRefIds the authors user ref ids
     */
    public void setAuthorsRefIds(Set<Long> authorsRefIds) {
        this.authorsRefIds = authorsRefIds;
    }

    /**
     * Gets beta testing group.
     *
     * @return the {@link BetaTestingGroupCreateDTO}
     */
    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.", required = true)
    public BetaTestingGroupCreateDTO getBetaTestingGroup() {
        return betaTestingGroup;
    }

    /**
     * Sets beta testing group.
     *
     * @param betaTestingGroup the {@link BetaTestingGroupCreateDTO}
     */
    public void setBetaTestingGroup(BetaTestingGroupCreateDTO betaTestingGroup) {
        this.betaTestingGroup = betaTestingGroup;
    }

    /**
     * Gets if stepper bar is shown while in run.
     *
     * @return true if bar is shown
     */
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", required = true, example = "true")
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
     * @param sandboxDefinitionRef the sandbox definition ref
     */
    public void setSandboxDefinitionRefId(Long sandboxDefinitionRef) {
        this.sandboxDefinitionRefId = sandboxDefinitionRef;
    }

    @Override
    public String toString() {
        return "TrainingDefinitionCreateDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", prerequisities=" + Arrays.toString(prerequisities) +
                ", outcomes=" + Arrays.toString(outcomes) +
                ", state=" + state +
                ", authorsRefIds=" + authorsRefIds +
                ", showStepperBar=" + showStepperBar +
                ", sandboxDefinitionRefId=" + sandboxDefinitionRefId +
                '}';
    }
}
