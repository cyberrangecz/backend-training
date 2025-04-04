package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.ResourceNotFoundException;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingInstanceLobby;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.TeamRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class TrainingInstanceLobbyService {

    private final TeamRepository teamRepository;
    private final UserService userService;
    private final TrainingInstanceRepository trainingInstanceRepository;

    @Autowired
    public TrainingInstanceLobbyService(
            TeamRepository teamRepository,
            UserService userService, TrainingInstanceRepository trainingInstanceRepository) {
        this.teamRepository = teamRepository;
        this.userService = userService;
        this.trainingInstanceRepository = trainingInstanceRepository;
    }

    public TrainingInstanceLobby updateTrainingInstanceLobby(TrainingInstanceLobby trainingInstanceLobby) {
        validateNotInQueueAndTeam(trainingInstanceLobby);
        validateMaxTeamSize(trainingInstanceLobby);
        trainingInstanceLobby.getTeams().forEach(this::validateTeamSize);
        return trainingInstanceRepository.save(trainingInstanceLobby.getTrainingInstance()).getTrainingInstanceLobby();
    }

    private void validateNotInQueueAndTeam(TrainingInstanceLobby trainingInstanceLobby) {
        if (trainingInstanceLobby.getUsersQueue().stream().anyMatch((user) ->
                trainingInstanceLobby.getTeams().stream().anyMatch(team -> team.getMembers().contains(user))
        )) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstanceLobby.class, "id",
                    trainingInstanceLobby.getTrainingInstance().getId().getClass(),
                    trainingInstanceLobby.getTrainingInstance().getId(),
                    "A user cannot be assigned both to queue and to a team at the same time."));
        }
    }

    public TrainingInstanceLobby getInstanceLobbyOrThrow(Long instanceId) {
        return this.trainingInstanceRepository.findById(instanceId).orElseThrow(
                () -> new ResourceNotFoundException("Lobby with id " + instanceId + " not found")
        ).getTrainingInstanceLobby();
    }

    public Team getTeamOrThrow(Long teamId) {
        return this.teamRepository.findById(teamId).orElseThrow(
                () -> new ResourceNotFoundException("Team with id " + teamId + " not found")
        );
    }

    private void validateMaxTeamSize(TrainingInstanceLobby trainingInstanceLobby) {
        if (trainingInstanceLobby.getTrainingInstance().getMaxTeamSize() > TrainingInstanceLobby.TEAM_SIZE_LIMIT) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstanceLobby.class, "id",
                    trainingInstanceLobby.getTrainingInstance().getId().getClass(),
                    trainingInstanceLobby.getTrainingInstance().getId(),
                    String.format("Max allowed team size is %d",
                            TrainingInstanceLobby.TEAM_SIZE_LIMIT)));
        }
    }

    private void validateTeamSize(Team team) {
        if (team.getMembers().size() > team.getTrainingInstance().getMaxTeamSize()
        ) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id",
                    team.getTrainingInstance().getId().getClass(),
                    team.getTrainingInstance().getId(),
                    String.format("Max team size is %d, but team with id %d has %d members",
                            team.getTrainingInstance().getMaxTeamSize(),
                            team.getTrainingInstance().getId(),
                            team.getMembers().size())
            ));
        }
    }


    public Team createTeam(String name) {
        Team team = new Team();
        team.setName(name);
        validateTeamName(team);
        return teamRepository.save(team);
    }

    public void deleteTeam(Long teamId) {
        Optional<Team> team = this.teamRepository.findById(teamId);
        if (team.isEmpty()) {
            return;
        }
        if (!team.get().getMembers().isEmpty()) {
            throw new EntityConflictException(new EntityErrorDetail(
                    Team.class, "Cannot delete team with players still assigned")
            );
        }
        if (team.get().isLocked()) {
            throw new EntityConflictException(new EntityErrorDetail(
                    Team.class, "Cannot delete team which has already started"
            ));
        }
        this.teamRepository.delete(team.get());
    }

    public Team updateTeam(Team team) {
        validateTeamSize(team);
        validateTeamName(team);
        return teamRepository.save(team);
    }

    private void validateTeamName(Team team) {
        if (teamRepository.existsByNameAndTrainingInstance_Id(team.getName(), team.getTrainingInstance().getId())) {
            throw new EntityConflictException(new EntityErrorDetail(
                    Team.class, "id", team.getId().getClass(), team.getId(),
                    String.format("Team with name %s aleready exists in instance %s", team.getName(), team.getTrainingInstance().getId())
            ));
        }
    }

    public void cleanupLobby(TrainingInstanceLobby trainingInstanceLobby) {
        trainingInstanceLobby.getTeams().forEach(
                team -> this.deleteTeam(team.getId())
        );
        trainingInstanceLobby.getUsersQueue().forEach(
                trainingInstanceLobby::removeWaitingUser
        );
    }

    public void addUserToQueue(Long instanceId, Long participantRefId) {
        TrainingInstanceLobby lobby = this.getInstanceLobbyOrThrow(instanceId);
        UserRef userRef = userService.getUserByUserRefId(participantRefId);
        if (lobby.getUsersQueue().contains(userRef)) {
            throw new EntityConflictException(new EntityErrorDetail("User is already in the queue"));
        }
        lobby.addWaitingUser(userRef);
        this.updateTrainingInstanceLobby(lobby);
    }

    public boolean isWaiting(Long participantRefId) {
        TrainingInstanceLobby lobby = this.getInstanceLobbyOrThrow(participantRefId);
        return lobby.getUsersQueue().contains(userService.getUserByUserRefId(participantRefId)) ||
                lobby.getTeams().stream().anyMatch(team -> team.getMembers().stream().anyMatch(userRef -> Objects.equals(userRef.getUserRefId(), participantRefId)));
    }

    public Optional<TrainingInstanceLobby> getTrainingInstanceLobby(Long instanceId) {
        return trainingInstanceRepository.findById(instanceId).map(TrainingInstance::getTrainingInstanceLobby);
    }
}
