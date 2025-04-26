package cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team;


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

    private TeamDTO team;
    private int score;
    private int position;
}
