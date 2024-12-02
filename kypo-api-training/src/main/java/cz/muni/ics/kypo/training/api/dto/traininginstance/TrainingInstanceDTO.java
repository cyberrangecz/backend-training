package cz.muni.ics.kypo.training.api.dto.traininginstance;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Encapsulates information about Training Instance
 *
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingInstanceDTO", description = "A session of attending a concrete training, which involves a deployment of the training definition in one or more sandbox instances that are then assigned to participants. The instance comprises one or more training runs.")
public class TrainingInstanceDTO {

    @ApiModelProperty(value = "Main identifier of training instance.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Date when training instance starts.", example = "2016-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "Date when training instance ends.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime endTime;
    @ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
    private String title;
    @ApiModelProperty(value = "Reference to training definition from which is training instance created.")
    private TrainingDefinitionByIdDTO trainingDefinition;
    @ApiModelProperty(value = "Token used to access training run.", required = true, example = "hunter")
    private String accessToken;
    @ApiModelProperty(value = "Id of sandbox pool belonging to training instance", example = "1")
    private Long poolId;
    @ApiModelProperty(value = "Ids of sandboxes which are assigned to training run.", example = "[3,15]")
    private List<String> sandboxesWithTrainingRun = new ArrayList<>();
    @ApiModelProperty(value = "Time of last edit done to instance.", example = "2017-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime lastEdited;
    @ApiModelProperty(value = "Name of the user who has done the last edit in instance.", example = "John Doe")
    private String lastEditedBy;
    @ApiModelProperty(value = "Indicates if local sandboxes are used for training runs.", example = "true")
    private boolean localEnvironment;
    @ApiModelProperty(value = "Id of sandbox definition assigned to training instance", example = "1")
    private Long sandboxDefinitionId;
    @ApiModelProperty(value = "Indicates if trainee can during training run move to the previous already solved levels.", example = "true")
    private boolean backwardMode;
}
