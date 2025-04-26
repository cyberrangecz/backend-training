package cz.cyberrange.platform.training.service.facade;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.TrainingInstanceLobbyDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.UserTeamDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamMessageDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ResourceNotReadyException;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TeamMessage;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingInstanceLobby;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TeamMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TeamMessageMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingInstanceLobbyMapper;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceLobbyService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class LobbyManagementFacade {

    private final TrainingInstanceLobbyService teamsManagementService;
    private final TeamMapper teamMapper;
    private final TrainingInstanceLobbyMapper trainingInstanceLobbyMapper;
    private final UserService userService;

    private static final Logger LOG = LoggerFactory.getLogger(LobbyManagementFacade.class);
    private final TrainingInstanceService trainingInstanceService;
    private final SecurityService securityService;
    private final TeamMessageMapper teamMessageMapper;

    @Autowired
    public LobbyManagementFacade(
            TrainingInstanceLobbyService trainingInstanceLobbyService,
            UserService userService,
            TeamMapper teamMapper,
            TrainingInstanceLobbyMapper trainingInstanceLobbyMapper,
            TrainingInstanceService trainingInstanceService, SecurityService securityService, TeamMessageMapper teamMessageMapper) {
        this.teamsManagementService = trainingInstanceLobbyService;
        this.userService = userService;
        this.teamMapper = teamMapper;
        this.trainingInstanceLobbyMapper = trainingInstanceLobbyMapper;
        this.trainingInstanceService = trainingInstanceService;
        this.securityService = securityService;
        this.teamMessageMapper = teamMessageMapper;
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TransactionalRO
    public TrainingInstanceLobbyDTO getTrainingInstanceLobby(Long instanceId) {
        LOG.error("Getting Lobby Instance Lobby DTO for id: {}", instanceId);
        TrainingInstanceLobbyDTO dto = trainingInstanceLobbyMapper.mapToDTO(
                teamsManagementService.getInstanceLobbyOrThrow(instanceId)
        );
        addParticipantsToLobbyInstance(dto);
        return dto;
    }

    private void addParticipantsToLobbyInstance(TrainingInstanceLobbyDTO instanceLobbyDTO) {
        instanceLobbyDTO.setUsersQueue(
                instanceLobbyDTO.getUsersQueue().stream().map(userRef ->
                        userService.getUserRefDTOByUserRefId(userRef.getUserRefId())
                ).toList()
        );
        instanceLobbyDTO.getTeams().forEach(teamDTO ->
                teamDTO.setMembers(
                        teamDTO.getMembers().stream().map(member ->
                                userService.getUserRefDTOByUserRefId(member.getUserRefId())
                        ).toList()
                )
        );
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @Transactional
    public void returnToQueue(Long instanceId, Set<UserTeamDTO> relations) {
        TrainingInstanceLobby lobby = teamsManagementService.getInstanceLobbyOrThrow(instanceId);
        relations.stream().map(
                relation -> userService.getUserByUserRefId(relation.getUserId())
        ).forEach(lobby::addWaitingUser);

        relations.forEach(
                relation -> {
                    Team team = teamsManagementService.getTeamOrThrow(relation.getTeamId());
                    UserRef user = userService.getUserByUserRefId(relation.getUserId());
                    team.removeMember(user);
                    teamsManagementService.updateTeam(team);
                }
        );
        teamsManagementService.updateTrainingInstanceLobby(lobby);
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @Transactional
    public TeamDTO createTeam(Long instanceId, String name) {
        TrainingInstanceLobby lobby = teamsManagementService.getInstanceLobbyOrThrow(instanceId);
        Team team = teamsManagementService.createTeam(instanceId, name);
        lobby.addTeam(team);
        teamsManagementService.updateTrainingInstanceLobby(lobby);
        return teamMapper.mapToDTO(team);
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTeamTrainingInstance(#teamId)")
    public void disbandTeam(Long teamId) {
        Team team = teamsManagementService.getTeamOrThrow(teamId);
        if (team.isLocked()) {
            throw new BadRequestException("Cannot delete a locked team");
        }

        TrainingInstanceLobby lobby = teamsManagementService.getInstanceLobbyOrThrow(team.getTrainingInstance().getId());
        team.getMembers().forEach(lobby::addWaitingUser);
        new HashSet<>(team.getMembers()).forEach(team::removeMember);
        lobby.removeTeam(team);
        teamsManagementService.deleteTeam(teamId);
        teamsManagementService.updateTrainingInstanceLobby(lobby);
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) or" +
            "(@securityService.isOrganizerOfGivenTeamTrainingInstance(#teamFrom) and" +
            "@securityService.isOrganizerOfGivenTeamTrainingInstance(#teamTo))"
    )
    @Transactional
    public void moveUserBetweenTeams(Long teamFrom, Long teamTo, Set<Long> playerIds) {
        Team fromTeamEntity = teamsManagementService.getTeamOrThrow(teamFrom);
        Team toTeamEntity = teamsManagementService.getTeamOrThrow(teamTo);

        playerIds.stream()
                .map(userService::getUserByUserRefId)
                .forEach(user ->
                        this.teamsManagementService.moveUserBetweenTeams(fromTeamEntity, toTeamEntity, user)
                );
        this.teamsManagementService.updateTeam(fromTeamEntity);
        this.teamsManagementService.updateTeam(toTeamEntity);
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) or" +
            "@securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @Transactional
    public void assignToTeams(Long instanceId, Set<UserTeamDTO> relations) {
        TrainingInstanceLobby lobby = teamsManagementService.getInstanceLobbyOrThrow(instanceId);
        relations.forEach(
                relation -> {
                    Team team = teamsManagementService.findTeam(instanceId, relation.getTeamId());
                    UserRef userRef = userService.getUserByUserRefId(relation.getUserId());
                    teamsManagementService.addUserToTeam(lobby.getTrainingInstance(), userRef, team);
                }
        );

        teamsManagementService.updateTrainingInstanceLobby(lobby);
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTeamTrainingInstance(#teamId)")
    @Transactional
    public void lockTeam(Long teamId) {
        Team team = teamsManagementService.getTeamOrThrow(teamId);
        if (team.getMembers().isEmpty()) {
            throw new BadRequestException("Empty team cannot be locked");
        }
        team.setLocked(true);
        teamsManagementService.lockTeam(team);
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTeamTrainingInstance(#teamId)")
    @Transactional
    public String renameTeam(Long teamId, String name) {
        Team team = teamsManagementService.getTeamOrThrow(teamId);
        return teamsManagementService.renameTeam(team, name).getName();
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_TRAINEE) or " +
            "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) or " +
            "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_DESIGNER)")
    @TransactionalRO
    public Integer getTrainingInstanceLobbyWaitingCount(String token, Boolean unassignedOnly) {
        TrainingInstance instance = trainingInstanceService.findByEndTimeBeforeAndAccessToken(token);
        return instance.getTrainingInstanceLobby().getUsersQueue().size() +
                (unassignedOnly ? 0 :
                        instance.getTrainingInstanceLobby()
                                .getTeams().stream().filter(team -> !team.isLocked())
                                .mapToInt(team -> team.getMembers().size()).sum());
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_TRAINEE) or " +
            "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) or " +
            "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_DESIGNER)")
    @TransactionalRO
    public String getTrainingInstanceStartDate(String accessToken) {
        return this.trainingInstanceService.findByEndTimeBeforeAndAccessToken(accessToken)
                .getStartTime().toInstant(ZoneOffset.UTC).toString();
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_TRAINEE) or " +
            "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) or " +
            "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_DESIGNER)")
    @TransactionalRO
    public TeamDTO getTeamInfo(String accessToken) {
        UserRef user = this.userService.getUserByUserRefId(this.securityService.getUserRefIdFromUserAndGroup());

        Optional<Team> teamAssigned = user.getTeams().stream()
                .filter(team -> team.getMembers().contains(user))
                .filter(team -> team.getTrainingInstance().getAccessToken().equals(accessToken))
                .findFirst();

        boolean isInQueue = user.getJoinedQueues().stream()
                .anyMatch(instance -> instance.getAccessToken().equals(accessToken));

        if (isInQueue || (teamAssigned.isPresent() && (!teamAssigned.get().isLocked()))) {
            throw new ResourceNotReadyException(new EntityErrorDetail("Not yet assigned to a team"));
        }
        if (teamAssigned.isEmpty()) {
            throw new EntityNotFoundException(new EntityErrorDetail("This instance has not been accessed"));
        }

        TeamDTO teamDTO = teamMapper.mapToDTO(teamAssigned.get());
        teamDTO.setMembers(
                teamDTO.getMembers().stream().map(member ->
                        userService.getUserRefDTOWithLimitedInformation(member.getUserRefId())
                ).toList()
        );
        return teamDTO;
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTeam(#teamId)")
    @TransactionalRO
    public Map<Long, TeamMessageDTO> getTeamMessagesByPlayer(Long teamId, Long since) {
        List<TeamMessage> messages = this.teamsManagementService.getTeamMessages(teamId, since);
        return messages.stream().collect(Collectors.toMap(
                message -> message.getSender().getUserRefId(),
                teamMessageMapper::mapToDTO
        ));
    }
}
