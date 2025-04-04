package cz.cyberrange.platform.training.service.facade;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.TrainingInstanceLobbyDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.UserTeamDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.ResourceNotFoundException;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TrainingInstanceLobby;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TeamMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingInstanceLobbyMapper;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceLobbyService;
import cz.cyberrange.platform.training.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class LobbyManagementFacade {

    private final SecurityService securityService;
    private final TrainingInstanceLobbyService teamsManagementService;
    private final TeamMapper teamMapper;
    private final TrainingInstanceLobbyMapper trainingInstanceLobbyMapper;
    private final UserService userService;

    @Autowired
    public LobbyManagementFacade(
            SecurityService securityService,
            TrainingInstanceLobbyService trainingInstanceLobbyService,
            UserService userService,
            TeamMapper teamMapper,
            TrainingInstanceLobbyMapper trainingInstanceLobbyMapper
    ) {
        this.teamsManagementService = trainingInstanceLobbyService;
        this.securityService = securityService;
        this.userService = userService;
        this.teamMapper = teamMapper;
        this.trainingInstanceLobbyMapper = trainingInstanceLobbyMapper;
    }

    @PreAuthorize("@securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public TrainingInstanceLobbyDTO getTrainingInstanceLobby(Long instanceId) {
        return trainingInstanceLobbyMapper.mapToDTO(
                teamsManagementService.getTrainingInstanceLobby(instanceId).orElseThrow(
                        () -> new ResourceNotFoundException("Lobby with of instance " + instanceId + " not found")
                ));
    }

    @PreAuthorize("@securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public void unassignFromTeams(Long instanceId, Set<UserTeamDTO> relations) {
        TrainingInstanceLobby lobby = teamsManagementService.getInstanceLobbyOrThrow(instanceId);
        relations.forEach(
                relation -> {
                    Optional<Team> team = lobby.getTeams().stream().filter(searchedTeam -> searchedTeam.getId().equals(relation.getTeamId())).findFirst();
                    if (team.isEmpty()) {
                        return; // Team not existing implies player not assigned
                    }
                    UserRef userRef = userService.createOrGetUserRef(relation.getUserId());
                    team.get().removeMember(userRef);
                    lobby.addWaitingUser(userRef);
                }
        );
        teamsManagementService.updateTrainingInstanceLobby(lobby);
    }

    @PreAuthorize("@securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public TeamDTO createTeam(Long instanceId, String name) {
        TrainingInstanceLobby lobby = teamsManagementService.getInstanceLobbyOrThrow(instanceId);
        Team team = teamsManagementService.createTeam(name);
        lobby.addTeam(team);
        teamsManagementService.updateTrainingInstanceLobby(lobby);
        return teamMapper.mapToDTO(team);
    }

    @PreAuthorize("@securityService.isOrganizerOfGivenTeamTrainingInstance(#teamId)")
    public void disbandTeam(Long teamId) {
        Team team = teamsManagementService.getTeamOrThrow(teamId);
        unassignFromTeams(teamId,
                team.getMembers().stream().map(userRef ->
                        new UserTeamDTO(userRef.getId(), teamId)
                ).collect(Collectors.toSet()));
        team.getTrainingInstance().getTrainingInstanceLobby().removeTeam(team);
        teamsManagementService.deleteTeam(teamId);
        teamsManagementService.updateTrainingInstanceLobby(team.getTrainingInstance().getTrainingInstanceLobby());
    }

    @PreAuthorize(
            "@securityService.isOrganizerOfGivenTeamTrainingInstance(#teamFrom) or" +
                    "@securityService.isOrganizerOfGivenTeamTrainingInstance(#teamTo)"
    )
    public void moveUserBetweenTeams(Long teamFrom, Long teamTo, Set<Long> playerIds) {
        Team fromTeamEntity = teamsManagementService.getTeamOrThrow(teamFrom);
        Team toTeamEntity = teamsManagementService.getTeamOrThrow(teamTo);
        if (!fromTeamEntity.getTrainingInstance().getId().equals(
                toTeamEntity.getTrainingInstance().getId())) {
            throw new EntityConflictException(new EntityErrorDetail(
                    "Teams must be from the same Training Instance"
            ));
        }
        playerIds.stream().map(userService::getUserByUserRefId)
                .forEach(user -> {
                    fromTeamEntity.removeMember(user);
                    toTeamEntity.addMember(user);
                });
        this.teamsManagementService.updateTeam(fromTeamEntity);
        this.teamsManagementService.updateTeam(fromTeamEntity);
    }

    @PreAuthorize("@securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public void assignToTeams(Long instanceId, Set<UserTeamDTO> relations) {
        TrainingInstanceLobby lobby = teamsManagementService.getInstanceLobbyOrThrow(instanceId);
        relations.forEach(
                relation -> {
                    Optional<Team> team = lobby.getTeams().stream().filter(searchedTeam -> searchedTeam.getId().equals(relation.getTeamId())).findFirst();
                    if (team.isEmpty()) {
                        throw new ResourceNotFoundException("Team with id " + relation.getTeamId() + " not found");
                    }
                    UserRef userRef = userService.createOrGetUserRef(relation.getUserId());
                    team.get().addMember(userRef);
                    lobby.removeWaitingUser(userRef);
                }
        );
        teamsManagementService.updateTrainingInstanceLobby(lobby);
    }

    @PreAuthorize("@securityService.isOrganizerOfGivenTeamTrainingInstance(#teamId)")
    public void lockTeam(Long teamId) {
        Team team = teamsManagementService.getTeamOrThrow(teamId);
        if (team.getMembers().isEmpty()) {
            throw new BadRequestException("Empty team cannot be locked");
        }
        team.setLocked(true);
        teamsManagementService.updateTeam(team);
    }

    @PreAuthorize("@securityService.isOrganizerOfGivenTeamTrainingInstance(#teamId)")
    public void renameTeam(Long teamId, String name) {
        Team team = teamsManagementService.getTeamOrThrow(teamId);
        team.setName(name);
        teamsManagementService.updateTeam(team);
    }
}
