package cz.cyberrange.platform.training.api.dto.export;

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
 * Encapsulates information about Training Run.
 */
@Getter
@Setter
@ToString
@ApiModel(value = "TrainingRunExportDTO", description = "An exported run of training instance of a particular participant.")
public class TrainingRunExportDTO {

	@ApiModelProperty(value = "Date when training run started.", example = "2016-10-19 10:23:54+02")
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime startTime;
	@ApiModelProperty(value = "Date when training run ends.", example = "2022-10-19 10:23:54+02")
	@JsonSerialize(using = LocalDateTimeUTCSerializer.class)
	private LocalDateTime endTime;
	private String eventLogReference;
	@ApiModelProperty(value = "Current state of training run.", example = "ALLOCATED")
	private TRState state;
	@ApiModelProperty(value = "Reference to participant of training run.")
	private UserRefExportDTO participantRef;
}


