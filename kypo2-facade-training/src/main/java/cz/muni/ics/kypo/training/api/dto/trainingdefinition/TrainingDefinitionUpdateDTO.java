package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import java.util.Arrays;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import io.swagger.annotations.ApiModel;

/**
 * 
 * @author Pavel Šeda (441048)
 *
 */
@ApiModel(value = "TrainingDefinitionUpdateDTO", description = "Training Definition to update.")
public class TrainingDefinitionUpdateDTO {

	@NotNull(message = "")
	private Long id;
	@NotEmpty(message = "")
	private String title;
	private String description;
	private String[] prerequisities;
	private String[] outcomes;
	@NotNull(message = "")
	private TDState state;
	@NotNull(message = "")
	private Long startingLevel;

	public TrainingDefinitionUpdateDTO() {}

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

	public Long getStartingLevel() {
		return startingLevel;
	}

	public void setStartingLevel(Long startingLevel) {
		this.startingLevel = startingLevel;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrainingDefinitionUpdateDTO [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", description=");
		builder.append(description);
		builder.append(", prerequisities=");
		builder.append(Arrays.toString(prerequisities));
		builder.append(", outcomes=");
		builder.append(Arrays.toString(outcomes));
		builder.append(", state=");
		builder.append(state);
		builder.append(", startingLevel=");
		builder.append(startingLevel);
		builder.append("]");
		return builder.toString();
	}

}
