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
    private Long startingLevel;
    private List<AbstractLevelDTO> levels = new ArrayList<>();
    private boolean showStepperBar;
    private boolean canBeArchived;
    private long estimatedDuration;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime lastEdited;

    @ApiModelProperty(value = "Main identifier of training definition.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
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

    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
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

    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    public TDState getState() {
        return state;
    }

    public void setState(TDState state) {
        this.state = state;
    }

    @ApiModelProperty(value = "References to the authors of the training definition.")
    public Set<UserRefDTO> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<UserRefDTO> authors) {
        this.authors = authors;
    }

    @ApiModelProperty(value = "Group of organizers who is allowed to see the training definition.")
    public BetaTestingGroupDTO getBetaTestingGroup() {
        return betaTestingGroup;
    }

    public void setBetaTestingGroup(BetaTestingGroupDTO betaTestingGroup) {
        this.betaTestingGroup = betaTestingGroup;
    }

    @ApiModelProperty(value = "Reference to the sandbox definition.")
    public Long getSandboxDefinitionRefId() {
        return sandboxDefinitionRefId;
    }

    public void setSandboxDefinitionRefId(Long sandBoxDefinitionRefId) {
        this.sandboxDefinitionRefId = sandBoxDefinitionRefId;
    }

    @ApiModelProperty(value = "Identifier of first level of training definition.", example = "4")
    public Long getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(Long startingLevel) {
        this.startingLevel = startingLevel;
    }

    @ApiModelProperty(value = "Information about all levels in training definition.")
    public List<AbstractLevelDTO> getLevels() {
        return levels;
    }

    public void setLevels(List<AbstractLevelDTO> levels) {
        this.levels = levels;
    }

    @ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
    public boolean isShowStepperBar() {
        return showStepperBar;
    }

    public void setShowStepperBar(boolean showStepperBar) {
        this.showStepperBar = showStepperBar;
    }

    public boolean isCanBeArchived() {
        return canBeArchived;
    }

    public void setCanBeArchived(boolean canBeArchived) {
        this.canBeArchived = canBeArchived;
    }

    public long getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @ApiModelProperty(value = "Time of last edit done to definition.", example = "2017-10-19 10:23:54+02")
    public LocalDateTime getLastEdited() {
        return lastEdited;
    }

    @Override
    public String toString() {
        return "TrainingDefinitionByIdDTO{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\''
                + ", prerequisities=" + Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state
                + ", authors=" + authors + ", betaTestingGroup=" + betaTestingGroup + ", sandboxDefinitionRefId=" + sandboxDefinitionRefId
                + ", startingLevel=" + startingLevel + ", levels=" + levels + ", showStepperBar=" + showStepperBar + ", canBeArchived="
                + canBeArchived + ", estimatedDuration=" + estimatedDuration + ", lastEdited=" + lastEdited + '}';
    }
}
