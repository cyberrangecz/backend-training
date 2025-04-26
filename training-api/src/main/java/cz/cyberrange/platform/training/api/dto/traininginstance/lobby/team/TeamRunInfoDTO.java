package cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;


@Setter
@Getter
@NoArgsConstructor
public class TeamRunInfoDTO {

    private Map<Long, Long> currentLevels;

    private Set<Long> usedHints;

    private Set<Long> shownSolutions;

}
