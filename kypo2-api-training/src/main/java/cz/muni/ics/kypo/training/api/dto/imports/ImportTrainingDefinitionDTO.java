package cz.muni.ics.kypo.training.api.dto.imports;

import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates information about training definition and its levels.
 *
 */
@ApiModel(value = "HintDTO", description = "A basic information about hint.")
public class ImportTrainingDefinitionDTO {

	@ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
	@NotEmpty(message = "{trainingdefinitionimport.title.NotEmpty.message}")
	private String title;
	@ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Unreleased training definition")
	private String description;
	@ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
	private String[] prerequisities;
	@ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "")
	private String[] outcomes;
	@ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
	@NotNull(message = "{trainingdefinitionimport.state.NotNull.message}")
	private TDState state;
	@ApiModelProperty(value = "Sign if stepper bar should be displayed.", example = "false")
	@NotNull(message = "{trainingdefinitionimport.showStepperBar.NotNull.message}")
	private boolean showStepperBar;
	@ApiModelProperty(value = "Main identifier of sandbox definition associated with this training definition.", example = "1")
	@NotNull(message = "{trainingdefinitionimport.sandboxDefinitionRefId.NotNull.message}")
    private Long sandboxDefinitionRefId;
	@ApiModelProperty(value = "Information about all levels in training definition.")
	private List<AbstractLevelImportDTO> levels = new ArrayList<>();
	@ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
	private Integer estimatedDuration;

	/**
	 * Instantiates a new Import training definition dto.
	 */
	public ImportTrainingDefinitionDTO() {
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
	 * Gets if stepper bar is shown while in run.
	 *
	 * @return true if bar is shown
	 */
	public boolean isShowStepperBar() {
		return showStepperBar;
	}

	/**
	 * Gets if stepper bar is shown while in run.
	 *
	 * @param showStepperBar true if bar is shown
	 */
	public void setShowStepperBar(boolean showStepperBar) {
		this.showStepperBar = showStepperBar;
	}

	/**
	 * Gets levels.
	 *
	 * @return the list of {@link AbstractLevelImportDTO}
	 */
	public List<AbstractLevelImportDTO> getLevels() {
		return levels;
	}

	/**
	 * Sets levels.
	 *
	 * @param levels the list of {@link AbstractLevelImportDTO}
	 */
	public void setLevels(List<AbstractLevelImportDTO> levels) {
		this.levels = new ArrayList<>(levels);
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

	/**
	 * Gets estimated duration.
	 *
	 * @return the estimated duration
	 */
	public Integer getEstimatedDuration() {
		return estimatedDuration;
	}

	/**
	 * Sets estimated duration.
	 *
	 * @param estimatedDuration the estimated duration
	 */
	public void setEstimatedDuration(Integer estimatedDuration) {
		this.estimatedDuration = estimatedDuration;
	}

	@Override public String toString() {
		return "ImportTrainingDefinitionDTO{" + "title='" + title + '\'' + ", description='" + description + '\'' + ", prerequisities=" + Arrays
				.toString(prerequisities) + ", outcomes=" + Arrays.toString(outcomes) + ", state=" + state + ", showStepperBar=" + showStepperBar
				+ ", sandboxDefinitionRefId=" + sandboxDefinitionRefId + ", levels=" + levels + ", estimatedDuration=" + estimatedDuration + '}';
	}
}
