package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.LimitedScoreboardDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamScoreDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScoreboardService {

  private static final Logger LOG = Logger.getLogger(ScoreboardService.class.getName());

  private final TrainingInstanceLobbyService trainingInstanceLobbyService;
  // InstanceId -> TeamId -> Score
  private static final AtomicReference<Map<Long, Map<Long, TeamScoreDTO>>> scoreboardsRef =
      new AtomicReference<>(new HashMap<>());
  private final ScoreboardRefreshService scoreboardRefreshService;
  private final TrainingInstanceService trainingInstanceService;

  public ScoreboardService(
      TrainingInstanceLobbyService trainingInstanceLobbyService,
      ScoreboardRefreshService scoreboardRefreshService,
      TrainingInstanceService trainingInstanceService) {
    this.trainingInstanceLobbyService = trainingInstanceLobbyService;
    this.scoreboardRefreshService = scoreboardRefreshService;
    this.trainingInstanceService = trainingInstanceService;
  }

  /**
   * Fills UserRefs of TeamScoreDTOs to contain actual data
   *
   * @param scoreDTO
   * @param withImages if false, identicons will be excluded
   */
  public void setFullUserRefDTOData(TeamScoreDTO scoreDTO, boolean withImages) {
    trainingInstanceLobbyService.setTeamMembersData(scoreDTO.getTeam(), withImages);
  }

  /**
   * Fills UserRefs of TeamScoreDTO to contain actual data
   *
   * @param scoreDTO
   * @param withImages if false, identicons will be excluded
   */
  public void setFullUserRefDTOData(List<TeamScoreDTO> scoreDTO, boolean withImages) {
    scoreDTO.forEach(
        dto -> trainingInstanceLobbyService.setTeamMembersData(dto.getTeam(), withImages));
  }

  /**
   * Retrieves scoreboard including only top 3 positions and positions right after and right before
   * specified team
   *
   * @param instanceId instance of the team
   * @param teamId anchor team, relative to which to limit
   * @return processed scoreboard
   */
  public LimitedScoreboardDTO getLimitedScoreboard(Long instanceId, Long teamId) {
    Map<Long, TeamScoreDTO> scoresByTeamId = getScoreboard(instanceId);
    if (!scoresByTeamId.containsKey(teamId)) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              "Team with id " + teamId + " not found in scoreboard for instance " + instanceId));
    }
    int userPosition = scoresByTeamId.get(teamId).getPosition();

    Set<Integer> requiredPositions =
        new HashSet<>(Arrays.asList(1, 2, 3, userPosition - 1, userPosition, userPosition + 1));

    LimitedScoreboardDTO limitedScoreboardDTO = new LimitedScoreboardDTO();
    limitedScoreboardDTO.setLimitedScoreboard(
        scoresByTeamId.values().stream()
            .filter(team -> requiredPositions.contains(team.getPosition()))
            .sorted(Comparator.comparingInt(TeamScoreDTO::getPosition))
            .toList());

    limitedScoreboardDTO.setTeamCountBeforeRelative(
        (int)
            scoresByTeamId.values().stream()
                .filter(
                    team ->
                        (!requiredPositions.contains(team.getPosition())
                            && team.getPosition() < userPosition))
                .count());
    limitedScoreboardDTO.setTeamCountAfterRelative(
        (int)
            scoresByTeamId.values().stream()
                .filter(
                    team ->
                        (!requiredPositions.contains(team.getPosition())
                            && team.getPosition() > userPosition))
                .count());

    return limitedScoreboardDTO;
  }

  public Map<Long, TeamScoreDTO> getScoreboard(Long instanceId) {
    return scoreboardsRef.get().getOrDefault(instanceId, Collections.emptyMap());
  }

  @Scheduled(fixedRate = 5_000)
  public void recalculateScores() {
    List<TrainingInstance> runningCoopInstances =
        trainingInstanceService.findAllRunningInstances().stream()
            .filter(instance -> instance.getType() == TrainingType.COOP)
            .toList();

    Set<Long> activeInstanceIds =
        runningCoopInstances.stream().map(TrainingInstance::getId).collect(Collectors.toSet());
    Map<Long, Map<Long, TeamScoreDTO>> newScoreboards = new HashMap<>();

    scoreboardsRef
        .get()
        .forEach(
            (instanceId, scores) -> {
              if (activeInstanceIds.contains(instanceId)) {
                newScoreboards.put(instanceId, scores);
              }
            });

    for (TrainingInstance instance : runningCoopInstances) {
      Map<Long, TeamScoreDTO> instanceTeamScores =
          scoreboardRefreshService.refreshScoreboardForInstance(instance);
      newScoreboards.put(instance.getId(), instanceTeamScores);
    }
    scoreboardsRef.set(newScoreboards);
  }
}
