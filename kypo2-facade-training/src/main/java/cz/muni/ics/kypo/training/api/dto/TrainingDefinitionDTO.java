package cz.muni.ics.kypo.training.api.dto;

import cz.muni.ics.kypo.training.model.AuthorRef;
import cz.muni.ics.kypo.training.model.SandboxDefinitionRef;
import cz.muni.ics.kypo.training.model.enums.TDState;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@ApiModel(value = "TrainingDefinitionDTO", description = ".")
public class TrainingDefinitionDTO {

    private Long id;
    @NotEmpty(message = "Training definition title cannot be empty")
    //@Pattern(regexp = "/\s/g", message = "")
    private String title;
    private String description;
    private String[] prerequisities;
    private String[] outcomes;
    private TDState state;
    @NotEmpty(message = "Authors cannot be empty")
    private Set<AuthorRef> authorRef = new HashSet<>();
    @NotNull(message = "Sandbox definition cannot be empty")
    private SandboxDefinitionRef sandBoxDefinitionRef;
    private Long startingLevel;
    private Set<BasicLevelInfoDTO> basicLevelInfoDTOs;

    public TrainingDefinitionDTO() {}

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

    public Set<AuthorRef> getAuthorRef() {
        return authorRef;
    }

    public void setAuthorRef(Set<AuthorRef> authorRef) {
        this.authorRef = authorRef;
    }

    public SandboxDefinitionRef getSandBoxDefinitionRef() {
        return sandBoxDefinitionRef;
    }

    public void setSandBoxDefinitionRef(SandboxDefinitionRef sandBoxDefinitionRef) {
        this.sandBoxDefinitionRef = sandBoxDefinitionRef;
    }

    public Long getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(Long startingLevel) {
        this.startingLevel = startingLevel;
    }

    public Set<BasicLevelInfoDTO> getBasicLevelInfoDTOs() {
        return basicLevelInfoDTOs;
    }

    public void setBasicLevelInfoDTOs(Set<BasicLevelInfoDTO> basicLevelInfoDTOs) {
        this.basicLevelInfoDTOs = basicLevelInfoDTOs;
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
                ", authorRef=" + authorRef +
                ", sandBoxDefinitionRef=" + sandBoxDefinitionRef +
                ", startingLevel=" + startingLevel +
                ", basicLevelInfoDTOs=" + basicLevelInfoDTOs +
                '}';
    }
}
