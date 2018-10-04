package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import cz.muni.ics.kypo.training.model.enums.TDState;
import io.swagger.annotations.ApiModel;
import cz.muni.ics.kypo.training.api.dto.AuthorRefDTO;
import cz.muni.ics.kypo.training.api.dto.SandboxDefinitionRefDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
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
	private String title;
	private String description;
	private String[] prerequisities;
	private String[] outcomes;
	private TDState state;
	private Set<AuthorRefDTO> authorRef = new HashSet<>();
	private SandboxDefinitionRefDTO sandBoxDefinitionRef;
	private Long startingLevel;
	private Set<BasicLevelInfoDTO> basicLevelInfoDTOs;
	private boolean showStepperBar;

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

	public Set<AuthorRefDTO> getAuthorRef() {
		return authorRef;
	}

	public void setAuthorRef(Set<AuthorRefDTO> authorRef) {
		this.authorRef = authorRef;
	}

	public SandboxDefinitionRefDTO getSandBoxDefinitionRef() {
		return sandBoxDefinitionRef;
	}

	public void setSandBoxDefinitionRefDTO(SandboxDefinitionRefDTO sandBoxDefinitionRefDTO) {
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

	public boolean isShowStepperBar() {
		return showStepperBar;
	}

	public void setShowStepperBar(boolean showStepperBar) {
		this.showStepperBar = showStepperBar;
	}

	@Override public String toString() {
		return "TrainingDefinitionDTO{" + "id=" + id + ", title='" + title + '\'' + ", description='" + description + '\'' + ", prerequisities="
				+ Arrays.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state + ", authorRefDTO="
				+ authorRef + ", sandBoxDefinitionRefDTO=" + sandBoxDefinitionRef + ", startingLevel=" + startingLevel
				+ ", basicLevelInfoDTOs=" + basicLevelInfoDTOs + ", showStepperBar=" + showStepperBar + '}';
	}
}
