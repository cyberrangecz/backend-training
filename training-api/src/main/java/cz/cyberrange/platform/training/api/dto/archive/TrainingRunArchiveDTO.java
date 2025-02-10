package cz.cyberrange.platform.training.api.dto.archive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.enums.TRState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Encapsulates information about Training run.
 * Used for archiving
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingRunArchiveDTO", description = "An archived run of training instance of a particular participant.")
public class TrainingRunArchiveDTO {

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Main identifier of training instance associated with this run.", example = "1")
    private Long instanceId;
    @ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime startTime;
    @ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime endTime;
    private String eventLogReference;
    @ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
    private TRState state;
    @ApiModelProperty(value = "Reference to participant of training run.", example = "5")
    private Long participantRefId;
}
