package cz.cyberrange.platform.training.api.dto.traininginstance.lobby;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamDTO;
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
@ApiModel(value = "TrainingInstanceLobbyDTO", description = "Object managing players waiting for team and players in teams.")
public class TrainingInstanceLobbyDTO {

    @ApiModelProperty(value = "List of players waiting in queue.")
    private List<UserRefDTO> usersQueue;

    @ApiModelProperty(value = "List of created teams.")
    private List<TeamDTO> teams;

}
