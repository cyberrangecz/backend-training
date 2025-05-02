package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamScoreDTO;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.repository.TeamRepository;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TeamMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/*
Required to be separate from ScoreboardService,
to avoid transactional self-invocation
*/
@Service
public class ScoreboardRefreshService {

  private final TeamMapper teamMapper;
  private final CoopTrainingRunService coopTrainingRunService;
  private final TeamRepository teamRepository;

  public ScoreboardRefreshService(
      TeamMapper teamMapper,
      CoopTrainingRunService coopTrainingRunService,
      TeamRepository teamRepository) {
    this.teamMapper = teamMapper;
    this.coopTrainingRunService = coopTrainingRunService;
    this.teamRepository = teamRepository;
  }

  @TransactionalRO
  public Map<Long, TeamScoreDTO> refreshScoreboardForInstance(TrainingInstance instance) {
    Map<Long, Team> teamsById =
        teamRepository.findAllByTrainingInstance_Id(instance.getId()).stream()
            .filter(Team::isLocked)
            .collect(Collectors.toMap(Team::getId, team -> team));

    SortedMap<Integer, List<Long>> teamsByScore = new TreeMap<>(Comparator.reverseOrder());
    for (Team team : teamsById.values()) {
      Integer score =
          coopTrainingRunService
              .findRelatedTrainingRun(team.getId())
              .map(TrainingRun::getTotalTrainingScore)
              .orElse(0);
      teamsByScore.computeIfAbsent(score, k -> new ArrayList<>()).add(team.getId());
    }

    Map<Long, TeamScoreDTO> instanceTeamScores = new HashMap<>();
    int position = 1;
    for (Map.Entry<Integer, List<Long>> entry : teamsByScore.entrySet()) {
      for (Long teamId : entry.getValue()) {
        Team team = teamsById.get(teamId);
        TeamScoreDTO teamScoreDTO =
            new TeamScoreDTO(teamMapper.mapToDTO(team), entry.getKey(), position);
        instanceTeamScores.put(teamId, teamScoreDTO);
      }
      position++;
    }
    return instanceTeamScores;
  }
}
