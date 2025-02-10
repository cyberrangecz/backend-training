package cz.cyberrange.platform.training.api.dto.visualization.compact;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "CompactLevelViewUserDTO", description = "Information about a specific user for compact level visualization.")
public class CompactLevelViewUserDTO {

    @ApiModelProperty(value = "Information about the user.")
    private UserRefDTO user;
    @ApiModelProperty(value = "Events from the training run this user was in.")
    private List<CompactLevelViewEventDTO> events;
}
