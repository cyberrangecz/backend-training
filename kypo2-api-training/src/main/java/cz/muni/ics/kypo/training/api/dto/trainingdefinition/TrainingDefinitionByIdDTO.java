package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupDTO;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Encapsulates information about Training Definition.
 *
 * @author Pavel Seda (441048)
 */
@ApiModel(value = "TrainingDefinitionByIdDTO", description = "A blueprint of abstract levels.")
public class TrainingDefinitionByIdDTO {

    private Long id;
    private String title;
    private String description;
    private String[] prerequisities;
    private String[] outcomes;
    private TDState state;
    private Set<UserRefDTO> authors = new HashSet<>();
    private BetaTestingGroupDTO betaTestingGroup;
    private Long sandboxDefinitionRefId;
    private List<AbstractLevelDTO> levels = new ArrayList<>();
    private boolean showStepperBar;
    private boolean canBeArchived;
    private long estimatedDuration;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime lastEdited;

    /**
     * Gets id.
     *
     * @return the id
     */
    @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
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
    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
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
     * @return the prerequisites
     */
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
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
    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
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
     * Gets authors.
     *
     * @return the set of {@link UserRefDTO}
     */
    @ApiModelProperty(value = "References to the authors of the training definition.")
    public Set<UserRefDTO> getAuthors() {
        return authors;
    }

    /**
     * Sets authors.
     *
     * @param authors the set of {@link UserRefDTO}
     */
    public void setAuthors(Set<UserRefDTO> authors) {
        this.authors = authors;
    }

    /**
     * Gets beta testing group.
     *
     * @return the {@link BetaTestingGroupDTO}
     */
    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.")
    public BetaTestingGroupDTO getBetaTestingGroup() {
        return betaTestingGroup;
    }

    /**
     * Sets beta testing group.
     *
     * @param betaTestingGroup the {@link BetaTestingGroupDTO}
     */
    public void setBetaTestingGroup(BetaTestingGroupDTO betaTestingGroup) {
        this.betaTestingGroup = betaTestingGroup;
    }

    /**
     * Gets sandbox definition ref id.
     *
     * @return the sandbox definition ref id
     */
    @ApiModelProperty(value = "Reference to the sandbox definition.")
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
     * Gets levels.
     *
     * @return the list of {@link AbstractLevelDTO}
     */
    @ApiModelProperty(value = "Information about all levels in training definition.")
    public List<AbstractLevelDTO> getLevels() {
        return levels;
    }

    /**
     * Sets levels.
     *
     * @param levels the list of {@link AbstractLevelDTO}
     */
    public void setLevels(List<AbstractLevelDTO> levels) {
        this.levels = levels;
    }

    /**
     * Gets if stepper bar is shown while in run.
     *
     * @return true if bar is shown
     */
    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
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
    @ApiModelProperty(value = "Time of last edit done to definition.", example = "2017-10-19 10:23:54+02")
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

    @Override public String toString() {
        return "TrainingDefinitionByIdDTO{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\''
            + ", prerequisities=" + Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state
            + ", authors=" + authors + ", betaTestingGroup=" + betaTestingGroup + ", sandboxDefinitionRefId=" + sandboxDefinitionRefId
            + ", levels=" + levels + ", showStepperBar=" + showStepperBar + ", canBeArchived=" + canBeArchived + ", estimatedDuration="
            + estimatedDuration + ", lastEdited=" + lastEdited + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TrainingDefinitionByIdDTO)) return false;
        TrainingDefinitionByIdDTO that = (TrainingDefinitionByIdDTO) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getSandboxDefinitionRefId(), that.getSandboxDefinitionRefId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getState(), getSandboxDefinitionRefId());
    }
}
