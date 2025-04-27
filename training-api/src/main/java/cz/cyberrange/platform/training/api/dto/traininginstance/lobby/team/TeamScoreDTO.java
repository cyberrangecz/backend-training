package cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class TeamScoreDTO {

    @ApiModelProperty(value = "Team information.")
    private TeamDTO team;
    @ApiModelProperty(value = "Score of the team.")
    private int score;
    @ApiModelProperty(value = "Position of the team in the scoreboard. Places are can be shared.")
    private int position;
}
