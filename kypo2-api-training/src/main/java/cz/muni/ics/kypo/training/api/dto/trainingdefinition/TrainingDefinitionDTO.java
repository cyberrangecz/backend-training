package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * Encapsulates information about Training Definition
 *
 */
@ApiModel(value = "TrainingDefinitionDTO", description = "A blueprint of abstract levels.")
public class TrainingDefinitionDTO {

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
    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.", example = "14")
    private Long betaTestingGroupId;
    @ApiModelProperty(value = "Reference to the sandbox definition.", example = "1")
    private Long sandboxDefinitionRefId;
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    private boolean showStepperBar;
    @ApiModelProperty(value = "Sign if training definition can be archived or not.", example = "false")
    private boolean canBeArchived;
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
    private long estimatedDuration;
    @ApiModelProperty(value = "Time of last edit done to definition.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime lastEdited;


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
     * Gets beta testing group ID.
     *
     * @return the ID of the beta testing group
     */
    public Long getBetaTestingGroupId() {
        return betaTestingGroupId;
    }

    /**
     * Sets beta testing group ID.
     *
     * @param betaTestingGroupId the ID of the beta testing group.
     */
    public void setBetaTestingGroupId(Long betaTestingGroupId) {
        this.betaTestingGroupId = betaTestingGroupId;
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
     * @param sandBoxDefinitionRefId the sand box definition ref id
     */
    public void setSandboxDefinitionRefId(Long sandBoxDefinitionRefId) {
        this.sandboxDefinitionRefId = sandBoxDefinitionRefId;
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

    /**
     * Definition can be archived if no associated instances are active.
     *
     * @return true if definition can be archived
     */
    public boolean isCanBeArchived() {
        return canBeArchived;
    }

    /**
     * Definition can be archived if no associated instances are active.
     *
     * @param canBeArchived true if definition can be archived
     */
    public void setCanBeArchived(boolean canBeArchived) {
        this.canBeArchived = canBeArchived;
    }

    /**
     * Gets estimated duration.
     *
     * @return the estimated duration
     */
    public long getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated duration.
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets time of last edit.
     *
     * @return the last edited
     */
    public LocalDateTime getLastEdited() {
        return lastEdited;
    }

    /**
     * Sets time of last edit.
     *
     * @param lastEdited the last edited
     */
    public void setLastEdited(LocalDateTime lastEdited) {
        this.lastEdited = lastEdited;
    }

    @Override
    public String toString() {
        return "TrainingDefinitionDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", prerequisities=" + Arrays.toString(prerequisities) +
                ", outcomes=" + Arrays.toString(outcomes) +
                ", state=" + state +
                ", betaTestingGroupId=" + betaTestingGroupId +
                ", sandboxDefinitionRefId=" + sandboxDefinitionRefId +
                ", showStepperBar=" + showStepperBar +
                ", canBeArchived=" + canBeArchived +
                ", estimatedDuration=" + estimatedDuration +
                ", lastEdited=" + lastEdited +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TrainingDefinitionDTO)) return false;
        TrainingDefinitionDTO that = (TrainingDefinitionDTO) object;
        return isCanBeArchived() == that.isCanBeArchived() &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getSandboxDefinitionRefId(), that.getSandboxDefinitionRefId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getDescription(), getState(), getSandboxDefinitionRefId(), isCanBeArchived());
    }
}
