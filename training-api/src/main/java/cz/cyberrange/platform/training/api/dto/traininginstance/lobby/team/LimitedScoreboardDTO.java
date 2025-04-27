package cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class LimitedScoreboardDTO {

    @ApiModelProperty(value = "Scoreboard data")
    private List<TeamScoreDTO> limitedScoreboard;

    @ApiModelProperty(value = "Used to render information on how many teams are placed before the " +
            "users team in the scoreboard and top 3 teams")
    private int teamCountBeforeRelative;

    @ApiModelProperty(value = "Used to render information on how many teams are placed after the " +
            "users team in the scoreboard")
    private int teamCountAfterRelative;


}
