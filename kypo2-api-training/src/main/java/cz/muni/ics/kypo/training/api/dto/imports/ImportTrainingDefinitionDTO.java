package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.enums.TDState;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * @author Boris Jadus(445343)
 */
public class ImportTrainingDefinitionDTO {

	@NotEmpty(message = "{trainingdefinitionimport.title.NotEmpty.message}")
	private String title;
	private String description;
	private String[] prerequisities;
	private String[] outcomes;
	@NotNull(message = "{trainingdefinitionimport.state.NotNull.message}")
	private TDState state;
	@NotNull(message = "{trainingdefinitionimport.showStepperBar.NotNull.message}")
	private boolean showStepperBar;
	@NotNull(message = "{trainingdefinitionimport.sandboxDefinitionRefId.NotNull.message}")
  private Long sandboxDefinitionRefId;
	private List<AbstractLevelImportDTO> levels = new ArrayList<>();
	private Integer estimatedDuration;

	public ImportTrainingDefinitionDTO() {
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

	public boolean isShowStepperBar() {
		return showStepperBar;
	}

	public void setShowStepperBar(boolean showStepperBar) {
		this.showStepperBar = showStepperBar;
	}

	public List<AbstractLevelImportDTO> getLevels() {
		return levels;
	}

	public void setLevels(List<AbstractLevelImportDTO> levels) {
		this.levels = new ArrayList<>(levels);
	}

	public Long getSandboxDefinitionRefId() {
		return sandboxDefinitionRefId;
	}

	public void setSandboxDefinitionRefId(Long sandboxDefinitionRefId) {
		this.sandboxDefinitionRefId = sandboxDefinitionRefId;
	}

	public Integer getEstimatedDuration() {
		return estimatedDuration;
	}

	public void setEstimatedDuration(Integer estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	@Override public String toString() {
		return "ImportTrainingDefinitionDTO{" + "title='" + title + '\'' + ", description='" + description + '\'' + ", prerequisities=" + Arrays
				.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state + ", showStepperBar=" + showStepperBar
				+ ", sandboxDefinitionRefId=" + sandboxDefinitionRefId + ", levels=" + levels + ", estimatedDuration=" + estimatedDuration + '}';
	}
}
