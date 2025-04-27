package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.LimitedScoreboardDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamScoreDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TeamMapper;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScoreboardService {

  private static final Logger LOG = Logger.getLogger(ScoreboardService.class.getName());

  private final TrainingInstanceRepository trainingInstanceRepository;
  private final TrainingInstanceLobbyService trainingInstanceLobbyService;
  // InstanceId -> TeamId -> Score
  private final Map<Long, Map<Long, TeamScoreDTO>> scoreboards = new ConcurrentHashMap<>();
  private final ScoreboardRefreshService scoreboardRefreshService;
  private final TrainingInstanceService trainingInstanceService;
  private final TrainingRunService trainingRunService;

  public ScoreboardService(
      TrainingInstanceRepository trainingInstanceRepository,
      TrainingInstanceLobbyService trainingInstanceLobbyService,
      CoopTrainingRunService coopTrainingRunService,
      TeamMapper teamMapper,
      ScoreboardRefreshService scoreboardRefreshService,
      TrainingInstanceService trainingInstanceService,
      TrainingRunService trainingRunService) {
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.trainingInstanceLobbyService = trainingInstanceLobbyService;
    this.scoreboardRefreshService = scoreboardRefreshService;
    this.trainingInstanceService = trainingInstanceService;
    this.trainingRunService = trainingRunService;
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
        Set.of(1, 2, 3, userPosition - 1, userPosition, userPosition + 1);

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
    return Map.copyOf(scoreboards.getOrDefault(instanceId, new HashMap<>()));
  }

  @Scheduled(fixedRate = 3_000)
  public void recalculateScores() {

    List<TrainingInstance> runningCoopInstances =
        trainingInstanceService.findAllRunningInstances().stream()
            .filter(instance -> instance.getType() == TrainingType.COOP)
            .toList();

    Set<Long> activeInstanceIds =
        runningCoopInstances.stream().map(TrainingInstance::getId).collect(Collectors.toSet());

    scoreboards.keySet().retainAll(activeInstanceIds);

    for (TrainingInstance instance : runningCoopInstances) {
      Map<Long, TeamScoreDTO> instanceTeamScores =
          scoreboardRefreshService.refreshScoreboardForInstance(instance);
      scoreboards.replace(instance.getId(), instanceTeamScores);
    }
  }
}
