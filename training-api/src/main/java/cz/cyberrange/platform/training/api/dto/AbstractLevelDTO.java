package cz.cyberrange.platform.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelDTO;
import cz.cyberrange.platform.training.api.dto.jeopardylevel.JeopardyLevelDTO;
import cz.cyberrange.platform.training.api.dto.jeopardylevel.sublevel.JeopardySublevelDTO;
import cz.cyberrange.platform.training.api.dto.snapshothook.SnapshotHookDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelDTO}, {@link TrainingLevelDTO}, {@link AccessLevelDTO} and {@link InfoLevelDTO}
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "AbstractLevelDTO", subTypes = {TrainingLevelDTO.class, AccessLevelDTO.class, InfoLevelDTO.class, AssessmentLevelDTO.class},
        description = "Superclass for classes TrainingLevelDTO, AccessLevelDTO, AssessmentLevelDTO and InfoLevelDTO")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TrainingLevelDTO.class, name = "TrainingLevelDTO"),
        @JsonSubTypes.Type(value = AccessLevelDTO.class, name = "AccessLevelDTO"),
        @JsonSubTypes.Type(value = AssessmentLevelDTO.class, name = "AssessmentLevelDTO"),
        @JsonSubTypes.Type(value = InfoLevelDTO.class, name = "InfoLevelDTO"),
        @JsonSubTypes.Type(value = JeopardyLevelDTO.class, name = "JeopardyLevelDTO"),
        @JsonSubTypes.Type(value = JeopardySublevelDTO.class, name = "JeopardySublevelDTO"),
})
public class AbstractLevelDTO {

    @ApiModelProperty(value = "Main identifier of level.", example = "1")
    protected Long id;
    @ApiModelProperty(value = "Short textual description of the level.", example = "Training Level1")
    protected String title;
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    protected int maxScore;
    protected SnapshotHookDTO snapshotHook;
    @ApiModelProperty(value = "Type of the level.", example = "TRAINING")
    protected LevelType levelType;
    @ApiModelProperty(value = "Estimated time taken by the player to resolve the level.", example = "5")
    protected int estimatedDuration;
    @ApiModelProperty(value = "Training definition to which is this level assigned.", example = "2")
    protected TrainingDefinitionDTO trainingDefinition;
    @ApiModelProperty(value = "Order of level, starts with 0", example = "2")
    protected int order;
    @ApiModelProperty(value = "Minimal possible solve time (minutes) that must be taken by the player to solve the level.", example = "5")
    protected Integer minimalPossibleSolveTime;
}

