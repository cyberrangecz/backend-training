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
 */
@ApiModel(value = "TrainingDefinitionCreateDTO", description = "Training definition to create.")
public class TrainingDefinitionCreateDTO {

    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", required = true, example = "Photo Hunter")
    @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
    private String title;
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Description of Photo Hunter")
    private String description;
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "[HTML, http protocol]")
    private String[] prerequisities;
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "[outcomes]")
    private String[] outcomes;
    @ApiModelProperty(value = "Current state of training definition.", required = true, example = "UNRELEASED")
    @NotNull(message = "{trainingDefinition.state.NotNull.message}")
    private TDState state;
    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.", required = true)
    @Valid
    private BetaTestingGroupCreateDTO betaTestingGroup;
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", required = true, example = "true")
    @NotNull(message = "{trainingDefinition.showStepperBar.NotNull.message}")
    private boolean showStepperBar;

    /**
     * Gets title.
     *
     * @return the title
     */
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
     * Gets beta testing group.
     *
     * @return the {@link BetaTestingGroupCreateDTO}
     */
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
        return "TrainingDefinitionCreateDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state +
                ", showStepperBar=" + showStepperBar +
                '}';
    }
}
