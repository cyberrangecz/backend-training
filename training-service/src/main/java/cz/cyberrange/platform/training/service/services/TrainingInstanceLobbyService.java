package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TeamMessage;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingInstanceLobby;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.TeamMessageRepository;
import cz.cyberrange.platform.training.persistence.repository.TeamRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.elasticsearch.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrainingInstanceLobbyService {

  private final TeamRepository teamRepository;
  private final UserService userService;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final UserRefRepository userRefRepository;

  private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceLobbyService.class);
  private final TeamMessageRepository teamMessageRepository;

  @Autowired
  public TrainingInstanceLobbyService(
      TeamRepository teamRepository,
      UserService userService,
      TrainingInstanceRepository trainingInstanceRepository,
      UserRefRepository userRefRepository,
      TeamMessageRepository teamMessageRepository) {
    this.teamRepository = teamRepository;
    this.userService = userService;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.userRefRepository = userRefRepository;
    this.teamMessageRepository = teamMessageRepository;
  }

  public TrainingInstanceLobby updateTrainingInstanceLobby(
      TrainingInstanceLobby trainingInstanceLobby) {
    validateMaxTeamSize(trainingInstanceLobby);
    trainingInstanceLobby.getTeams().forEach(this::validateTeamSize);
    return trainingInstanceRepository
        .save(trainingInstanceLobby.getTrainingInstance())
        .getTrainingInstanceLobby();
  }

  public TrainingInstanceLobby getInstanceLobbyOrThrow(Long instanceId) {
    return this.trainingInstanceRepository
        .findById(instanceId)
        .orElseThrow(
            () -> {
              LOG.warn("Lobby with id " + instanceId + " not found");
              return new ResourceNotFoundException("Lobby with id " + instanceId + " not found");
            })
        .getTrainingInstanceLobby();
  }

  public Team getTeamOrThrow(Long teamId) {
    return this.teamRepository
        .findById(teamId)
        .orElseThrow(() -> new ResourceNotFoundException("Team with id " + teamId + " not found"));
  }

  /**
   * Ensure lobbie's max team size is not exceeding limit
   *
   * @param trainingInstanceLobby
   */
  private void validateMaxTeamSize(TrainingInstanceLobby trainingInstanceLobby) {
    if (trainingInstanceLobby.getTrainingInstance().getMaxTeamSize()
        > TrainingInstance.TEAM_SIZE_LIMIT) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstanceLobby.class,
              "id",
              trainingInstanceLobby.getTrainingInstance().getId().getClass(),
              trainingInstanceLobby.getTrainingInstance().getId(),
              String.format("Max allowed team size is %d", TrainingInstance.TEAM_SIZE_LIMIT)));
    }
    if (trainingInstanceLobby.getTrainingInstance().getMaxTeamSize() < 1) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstanceLobby.class,
              "id",
              trainingInstanceLobby.getTrainingInstance().getId().getClass(),
              trainingInstanceLobby.getTrainingInstance().getId(),
              String.format("Min allowed team size is %d", 1)));
    }
  }

  /**
   * Ensure team's size is not exceeding max team size
   *
   * @param team
   */
  private void validateTeamSize(Team team) {
    if (team.getMembers().size() > team.getTrainingInstance().getMaxTeamSize()) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              team.getTrainingInstance().getId().getClass(),
              team.getTrainingInstance().getId(),
              String.format(
                  "Max team size is %d, but team with id %d has %d members",
                  team.getTrainingInstance().getMaxTeamSize(),
                  team.getTrainingInstance().getId(),
                  team.getMembers().size())));
    }
  }

  /**
   * Create a new team with
   *
   * @param instanceId
   * @param name
   * @return
   */
  public Team createTeam(Long instanceId, String name) {
    TrainingInstance instance =
        this.trainingInstanceRepository
            .findById(instanceId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail("Instance with id " + instanceId + " not found")));
    validateTeamName(instanceId, name);
    Team team = new Team();
    team.setName(name);
    team.setTrainingInstance(instance);
    return teamRepository.save(team);
  }

  /**
   * Clear all user relationships and delete the team
   *
   * @param teamId deleted team id
   */
  public void deleteTeam(Long teamId) {
    Team team = getTeamOrThrow(teamId);
    Set<UserRef> members = team.getMembers();
    members.forEach(team::removeMember);
    teamMessageRepository.deleteTeamMessageByTeam(team);
    userRefRepository.saveAll(members);
    this.teamRepository.delete(team);
  }

  public Team updateTeam(Team team) {
    validateTeamSize(team);
    validateTeamNotLocked(team);
    return this.teamRepository.save(team);
  }

  public Team renameTeam(Team team, String newName) {
    validateTeamName(team.getTrainingInstance().getId(), newName.strip());
    team.setName(newName);
    return this.teamRepository.save(team);
  }

  /**
   * Validate and update team
   *
   * @param team update team
   * @return update entity
   */
  public Team lockTeam(Team team) {
    team.setLocked(true);
    return teamRepository.save(team);
  }

  /**
   * Only unlocked team should be edited
   *
   * @param team checked team
   */
  private void validateTeamNotLocked(Team team) {
    if (team.isLocked()) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              Team.class,
              "id",
              team.getId().getClass(),
              team.getId(),
              "Cannot edit team which has been locked"));
    }
  }

  /** Check name length and uniqueness */
  private void validateTeamName(Long instanceId, String teamName) {
    if (teamName.isBlank() || teamName.length() > 64) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              Team.class,
              "id",
              instanceId.getClass(),
              instanceId,
              String.format("Team name %s has invalid length", teamName)));
    }

    if (teamRepository.existsByNameAndTrainingInstance_Id(teamName, instanceId)) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              String.format(
                  "Team with name %s aleready exists in instance %s", teamName, instanceId)));
    }
  }

  /**
   * Delete all relationships between many-to-many entities
   *
   * @param trainingInstanceLobby
   */
  public void cleanupLobby(TrainingInstanceLobby trainingInstanceLobby) {
    trainingInstanceLobby.getTeams().forEach(team -> this.deleteTeam(team.getId()));
    Long instanceId = trainingInstanceLobby.getTrainingInstance().getId();
    List<Long> idsToRemove =
        trainingInstanceLobby.getUsersQueue().stream().map(UserRef::getUserRefId).toList();
    idsToRemove.forEach(id -> this.removeUserFromQueue(instanceId, id));
  }

  public void removeUserFromQueue(Long instanceId, Long userId) {
    TrainingInstanceLobby lobby = this.getInstanceLobbyOrThrow(instanceId);
    UserRef userRef = userService.getUserByUserRefId(userId);
    if (!lobby.getUsersQueue().contains(userRef)) {
      throw new EntityConflictException(new EntityErrorDetail("User not in the queue"));
    }
    lobby.removeWaitingUser(userRef);
    this.userRefRepository.save(userRef);
    this.updateTrainingInstanceLobby(lobby);
  }

  public void addUserToQueue(Long instanceId, Long userId) {
    TrainingInstanceLobby lobby = this.getInstanceLobbyOrThrow(instanceId);
    UserRef userRef = userService.getUserByUserRefId(userId);
    if (lobby.getUsersQueue().contains(userRef)) {
      throw new EntityConflictException(new EntityErrorDetail("User is already in the queue"));
    }
    lobby.addWaitingUser(userRef);
    this.userRefRepository.save(userRef);
    this.updateTrainingInstanceLobby(lobby);
  }

  /**
   * @param participantRefId participant id
   * @return Whether the participant is already in a team or queue
   */
  public boolean isWaitingForStart(
      Long instanceId, Long participantRefId, boolean instanceStarted) {
    TrainingInstanceLobby lobby = this.getInstanceLobbyOrThrow(instanceId);
    return lobby.getUsersQueue().contains(userService.createOrGetUserRef(participantRefId))
        || lobby.getTeams().stream()
            .anyMatch(
                team ->
                    (!team.isLocked() || !instanceStarted)
                        && team.getMembers().stream()
                            .anyMatch(
                                userRef ->
                                    Objects.equals(userRef.getUserRefId(), participantRefId)));
  }

  public Optional<TrainingInstanceLobby> getTrainingInstanceLobby(Long instanceId) {
    return trainingInstanceRepository
        .findById(instanceId)
        .map(TrainingInstance::getTrainingInstanceLobby);
  }

  public void addUserToTeam(TrainingInstance trainingInstance, UserRef userRef, Team team) {
    validateTeamNotLocked(team);
    if (team.getMembers().size() >= trainingInstance.getMaxTeamSize()) {
      throw new BadRequestException(String.format("Team %d is full", team.getId()));
    }
    if (!trainingInstance.getTrainingInstanceLobby().getUsersQueue().contains(userRef)) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              String.format(
                  "User %d is not in the instance lobby queue.", userRef.getUserRefId())));
    }
    if (!Objects.equals(team.getTrainingInstance().getId(), trainingInstance.getId())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              String.format(
                  "Team %d does not exist in instance %d",
                  team.getId(), trainingInstance.getId())));
    }
    trainingInstance.getTrainingInstanceLobby().removeWaitingUser(userRef);
    team.addMember(userRef);
  }

  public Team findTeam(Long instanceId, Long teamId) {
    Optional<Team> team = teamRepository.findByIdAndTrainingInstance_Id(teamId, instanceId);
    team.orElseThrow(
        () ->
            new EntityNotFoundException(
                new EntityErrorDetail(
                    Team.class,
                    "id",
                    Long.class,
                    teamId,
                    "Team not found in instance " + instanceId)));
    return team.get();
  }

  public Team findTeam(Long teamId) {
    Optional<Team> team = teamRepository.findById(teamId);
    team.orElseThrow(() -> new ResourceNotFoundException("Team with id " + teamId + " not found"));
    return team.get();
  }

  public void moveUserBetweenTeams(Team teamFrom, Team teamTo, UserRef user) {
    validateTeamNotLocked(teamFrom);
    validateTeamNotLocked(teamTo);
    if (!teamFrom.getMembers().contains(user)) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              String.format("User %d not found in team %d", user.getId(), teamFrom.getId())));
    }
    if (!teamTo.getTrainingInstance().getId().equals(teamFrom.getTrainingInstance().getId())) {
      throw new EntityConflictException(
          new EntityErrorDetail("Teams must be from the same Training Instance"));
    }
    teamFrom.removeMember(user);
    teamTo.addMember(user);
    validateTeamSize(teamTo);
  }

  public boolean isInLockedTeam(Long instanceId, Long participantRefId) {
    TrainingInstanceLobby instance = this.getInstanceLobbyOrThrow(instanceId);
    UserRef participantRef = this.userService.createOrGetUserRef(participantRefId);
    Optional<Team> assignedTeam =
        instance.getTeams().stream()
            .filter(team -> team.getMembers().contains(participantRef))
            .findFirst();
    return assignedTeam.isPresent() && assignedTeam.get().isLocked();
  }

  public void setTeamMembersData(TeamDTO dto, boolean withImages) {
    dto.setMembers(
        dto.getMembers().stream()
            .map(member -> userService.getUserRefDTOWithLimitedInformation(member.getUserRefId()))
            .peek(
                (member) -> {
                  if (!withImages) {
                    member.setPicture(null);
                  }
                })
            .toList());
  }

  public List<TeamMessage> getTeamMessages(Long teamId, Long since) {
    LocalDateTime utc0Time = LocalDateTime.ofEpochSecond(since, 0, ZoneOffset.UTC);
    return this.teamMessageRepository.findAllByTeam_IdAndTimeAfter(teamId, utc0Time);
  }

  public TeamMessage saveTeamMessage(Team team, UserRef sender, String message) {
    TeamMessage teamMessage = new TeamMessage();
    teamMessage.setTeam(team);
    teamMessage.setSender(sender);
    teamMessage.setTime(LocalDateTime.now(ZoneOffset.UTC).plus(100, ChronoUnit.MILLIS));
    teamMessage.setMessage(message);
    return teamMessageRepository.save(teamMessage);
  }
}
