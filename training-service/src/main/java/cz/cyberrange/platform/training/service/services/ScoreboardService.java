package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.events.AbstractAuditPOJO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamScoreDTO;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TeamMapper;
import cz.cyberrange.platform.training.service.services.api.ElasticsearchApiService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ScoreboardService {


    private final TrainingInstanceRepository trainingInstanceRepository;
    private final TrainingInstanceLobbyService trainingInstanceLobbyService;
    private final CoopTrainingRunService coopTrainingRunService;

    public ScoreboardService(TrainingInstanceRepository trainingInstanceRepository, SubmissionRepository submissionRepository, TrainingInstanceLobbyService trainingInstanceLobbyService, TrainingRunRepository trainingRunRepository, CoopTrainingRunService coopTrainingRunService, ElasticsearchApiService elasticsearchApiService, TrainingLevelRepository trainingLevelRepository,
                             TeamMapper teamMapper) {
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingInstanceLobbyService = trainingInstanceLobbyService;
        this.coopTrainingRunService = coopTrainingRunService;
        this.teamMapper = teamMapper;
    }

    public Map<Long, TeamScoreDTO> getScoreboard(Long instanceId) {
        return Map.copyOf(scoreboards.get(instanceId));
    }

    public boolean hasUpdatedScoreboard(Long instanceId, Instant lastUpdated) {
        return (!scoreboardLastUpdatedByInstanceId.containsKey(instanceId)) ||
                scoreboardLastUpdatedByInstanceId.get(instanceId).isAfter(lastUpdated);
    }

    // InstanceId -> TeamId -> Score
    private final Map<Long, Map<Long, TeamScoreDTO>> scoreboards = new ConcurrentHashMap<>();
    private final Map<Long, Instant> scoreboardLastUpdatedByInstanceId = new ConcurrentHashMap<>();
    private final TeamMapper teamMapper;

    @Scheduled(fixedRate = 3_000)
    public void recalculateScores() {
        List<TrainingInstance> runningCoopInstances = trainingInstanceRepository.findAllByStartTimeBeforeAndEndTimeAfter(
                LocalDateTime.now(Clock.systemUTC()), LocalDateTime.now(Clock.systemUTC())
        ).stream().filter(instance -> instance.getType() == TrainingType.COOP).toList();

        Set<Long> activeInstanceIds = runningCoopInstances.stream()
                .map(TrainingInstance::getId)
                .collect(Collectors.toSet());

        scoreboards.keySet().retainAll(activeInstanceIds);

        for (TrainingInstance instance : runningCoopInstances) {
            Map<Long, Team> teamsById = instance.getTrainingInstanceLobby().getTeams()
                    .stream().filter(Team::isLocked)
                    .collect(Collectors.toMap(Team::getId, team -> team));

            SortedMap<Integer, List<Long>> teamsByScore = new TreeMap<>(Comparator.reverseOrder());
            for (Team team : teamsById.values()) {
                Integer score = getTeamScore(team);
                teamsByScore.computeIfAbsent(score, k -> new ArrayList<>()).add(team.getId());
            }

            Map<Long, TeamScoreDTO> instanceTeamScores = new HashMap<>();
            int position = 1;
            for (Map.Entry<Integer, List<Long>> entry : teamsByScore.entrySet()) {
                for (Long teamId : entry.getValue()) {
                    Team team = teamsById.get(teamId);
                    TeamScoreDTO teamScoreDTO = new TeamScoreDTO(teamMapper.mapToDTO(team), entry.getKey(), position);
                    trainingInstanceLobbyService.setTeamMembersData(teamScoreDTO.getTeam(), true);
                    instanceTeamScores.put(teamId, teamScoreDTO);
                }
                position++;
            }
            if (!scoreboards.get(instance.getId()).equals(instanceTeamScores)) {
                scoreboards.replace(instance.getId(), instanceTeamScores);
                scoreboardLastUpdatedByInstanceId.replace(instance.getId(), Instant.now(Clock.systemUTC()));
            }
        }
    }

    public Integer getTeamScore(Team team) {
        TrainingRun teamRun = coopTrainingRunService.findRelatedTrainingRun(team.getId());
        return teamRun.getTotalTrainingScore();
    }

    /**
     * Compare event timestamp with dateTime
     *
     * @param event    event pojo
     * @param dateTime dateTime to compare with
     * @return true if event timestamp is before dateTime
     */
    private boolean isEventBeforeDate(AbstractAuditPOJO event, LocalDateTime dateTime) {
        return Instant.ofEpochMilli(event.getTimestamp()).isBefore(dateTime.toInstant(ZoneOffset.UTC));
    }

}
