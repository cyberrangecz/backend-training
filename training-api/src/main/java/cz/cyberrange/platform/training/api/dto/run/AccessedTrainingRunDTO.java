package cz.cyberrange.platform.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.Actions;
import cz.cyberrange.platform.training.api.enums.TrainingType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Encapsulates information about already accessed training run.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "AccessedTrainingRunDTO", description = "Already accessed training run by some participant.")
public class AccessedTrainingRunDTO {

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
    private String title;
    @ApiModelProperty(value = "Start date of training instance for which the training run was created.", example = "2016-10-19T10:23:54")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime trainingInstanceStartDate;
    @ApiModelProperty(value = "End date of training instance for which the training run was created.", example = "2017-10-19T10:23:54")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime trainingInstanceEndDate;
    @ApiModelProperty(value = "Training type", example = "LINEAR")
    private TrainingType type;
    @ApiModelProperty(value = "Current level order of training run.", example = "1")
    private int currentLevelOrder;
    @ApiModelProperty(value = "The number of levels in the training instance.", example = "3")
    private int numberOfLevels;
    @ApiModelProperty(value = "Possible action which can be executed with training Run.", example = "RESULTS")
    private Actions possibleAction;
    @ApiModelProperty(value = "Id of associated training instance", example = "1")
    private Long instanceId;
}
