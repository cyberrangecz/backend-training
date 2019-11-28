package cz.muni.ics.kypo.training.api.dto.archive;

import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information about Training Definition.
 * Used for archiving
 */
@ApiModel(value = "TrainingDefinitionArchiveDTO", description = "Archived detailed information about training definition which also include individual levels.")
public class TrainingDefinitionArchiveDTO {

    @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
    private Long id;
    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
    private String title;
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Unreleased training definition")
    private String description;
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
    private String[] prerequisities;
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "")
    private String[] outcomes;
    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    private TDState state;
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    private boolean showStepperBar;
    @ApiModelProperty(value = "Information about all levels in training definition.")
    private List<AbstractLevelArchiveDTO> levels = new ArrayList<>();
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
    private int estimatedDuration;
    @ApiModelProperty(value = "Main identifier of sandbox definition associated with this training definition.", example = "1")
    private Long sandboxDefinitionRefId;

    /**
     * Gets id.
     *
     * @return the id
     */
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
     * Get prerequisities string [ ].
     *
     * @return the string [ ]
     */
    public String[] getPrerequisities() {
        return prerequisities;
    }

    /**
     * Sets prerequisities.
     *
     * @param prerequisities the prerequisities
     */
    public void setPrerequisities(String[] prerequisities) {
        this.prerequisities = prerequisities;
    }

    /**
     * Get outcomes string [ ].
     *
     * @return the string [ ]
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
     * @return the state
     */
    public TDState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the state
     */
    public void setState(TDState state) {
        this.state = state;
    }

    /**
     * Is show stepper bar boolean.
     *
     * @return the boolean
     */
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    /**
     * Sets show stepper bar.
     *
     * @param showStepperBar the show stepper bar
     */
    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    /**
     * Gets levels.
     *
     * @return the levels
     */
    public List<AbstractLevelArchiveDTO> getLevels() {
        return levels;
    }

    /**
     * Sets levels.
     *
     * @param levels the levels
     */
    public void setLevels(List<AbstractLevelArchiveDTO> levels) {
        this.levels = levels;
    }

    /**
     * Gets estimated duration.
     *
     * @return the estimated duration
     */
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated duration.
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets sandbox definition ref id.
     *
     * @return the sandbox definition ref id
     */
    public Long getSandboxDefinitionRefId() {
        return sandboxDefinitionRefId;
    }

    /**
     * Sets sandbox definition ref id.
     *
     * @param sandboxDefinitionRefId the sandbox definition ref id
     */
    public void setSandboxDefinitionRefId(Long sandboxDefinitionRefId) {
        this.sandboxDefinitionRefId = sandboxDefinitionRefId;
    }
}
