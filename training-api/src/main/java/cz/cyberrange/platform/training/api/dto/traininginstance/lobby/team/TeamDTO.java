package cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ApiModel(value = "TeamDTO", description = "Team information including name and members")
public class TeamDTO {

    @ApiModelProperty(value = "Main identifier.", example = "1")
    Long id;

    @ApiModelProperty(value = "Name of the team.", example = "Team Blue")
    String name;

    @ApiModelProperty(value = "Lock state.", example = "true")
    boolean locked;

    @ApiModelProperty(value = "List of players in the team.")
    List<UserRefDTO> members;

}
