package cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@ApiModel(value = "TeamMessageDTO", description = "Message and its metadata")
public class TeamMessageDTO {

  @ApiModelProperty(value = "Message ID", example = "1")
  @JsonProperty("id")
  private Long messageId;

  @ApiModelProperty(value = "Date when the message was sent", example = "2022-10-19 10:23:54+02")
  @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
  private LocalDateTime time;

  @ApiModelProperty(value = "Message", example = "Hi guys!")
  private String message;
}
