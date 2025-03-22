package cz.cyberrange.platform.training.api.dto.imports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.enums.TrainingType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates information about training definition and its levels.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "ImportTrainingDefinitionDTO", description = "A basic information about hint.")
@JsonIgnoreProperties({"show_stepper_bar"})
// show_stepper_bar is allowed for backwards compatibility with old training definitions
public class ImportTrainingDefinitionDTO {

    @ApiModelProperty(value = "A name of the training/game (e.g., Photo Hunter) .", example = "TrainingDefinition2")
    @NotEmpty(message = "{trainingDefinition.title.NotEmpty.message}")
    private String title;
    @ApiModelProperty(value = "Description of training definition that is visible to the participant.", example = "Unreleased training definition")
    private String description;
    @ApiModelProperty(value = "List of knowledge and skills necessary to complete the training.", example = "")
    private String[] prerequisites;
    @ApiModelProperty(value = "A list of knowledge and skills that the participant should learn by attending the training (if it is used for educational purposes) ", example = "")
    private String[] outcomes;
    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    @NotNull(message = "{trainingDefinition.state.NotNull.message}")
    private TDState state;
    @ApiModelProperty(value = "Type of training instance.", example = "COOP")
    private TrainingType type;
    @Valid
    @ApiModelProperty(value = "Information about all levels in training definition.")
    private List<AbstractLevelImportDTO> levels = new ArrayList<>();
    @ApiModelProperty(value = "Estimated time it takes to finish runs created from this definition.", example = "5")
    private Integer estimatedDuration;
    @ApiModelProperty(value = "Marking if levels flags/answers are randomly generated and are different for each trainee. Default is false.", example = "false")
    private boolean variantSandboxes;

    /**
     * Sets levels.
     *
     * @param levels the list of {@link AbstractLevelImportDTO}
     */
    public void setLevels(List<AbstractLevelImportDTO> levels) {
        this.levels = new ArrayList<>(levels);
    }
}
