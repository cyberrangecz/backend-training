package cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class TeamMessageDTO {

    @ApiModelProperty(value = "Message ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "Date when the message was sent", example = "2022-10-19 10:23:54+02")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime time;

    @ApiModelProperty(value = "Message", example = "Hi guys!")
    private String message;

}
