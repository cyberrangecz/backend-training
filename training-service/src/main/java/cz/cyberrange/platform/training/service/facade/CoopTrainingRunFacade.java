package cz.cyberrange.platform.training.service.facade;

import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.LimitedScoreboardDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamMessageDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamScoreDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ResourceNotReadyException;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TeamMessage;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import cz.cyberrange.platform.training.service.annotations.security.IsOrganizerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.security.IsTraineeOrAdmin;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TeamMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TeamMessageMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingRunMapper;
import cz.cyberrange.platform.training.service.services.AuditEventsService;
import cz.cyberrange.platform.training.service.services.CoopTrainingRunService;
import cz.cyberrange.platform.training.service.services.ScoreboardService;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceLobbyService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.TrainingFeedbackApiService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** The type Training run facade. */
@Service
public class CoopTrainingRunFacade extends TrainingRunFacade {

  private static final Logger LOG = LoggerFactory.getLogger(CoopTrainingRunFacade.class);
  private final TrainingInstanceLobbyService trainingInstanceLobbyService;
  private final CoopTrainingRunService coopTrainingRunService;
  private final ScoreboardService scoreboardService;
  private final TeamMapper teamMapper;
  private final TeamMessageMapper teamMessageMapper;
  private final AuditEventsService auditEventsService;

  /**
   * Instantiates a new Training run facade.
   *
   * @param trainingRunService the training run service
   * @param securityService the security service
   * @param userService the user service
   * @param trainingRunMapper the training run mapper
   * @param levelMapper the level mapper
   * @param hintMapper the hint mapper
   */
  @Autowired
  public CoopTrainingRunFacade(
      TrainingRunService trainingRunService,
      TrainingDefinitionService trainingDefinitionService,
      AnswersStorageApiService answersStorageApiService,
      SecurityService securityService,
      UserService userService,
      TrainingFeedbackApiService trainingFeedbackApiService,
      TrainingRunMapper trainingRunMapper,
      LevelMapper levelMapper,
      HintMapper hintMapper,
      TrainingInstanceLobbyService trainingInstanceLobbyService,
      CoopTrainingRunService coopTrainingRunService,
      ScoreboardService scoreboardService,
      TeamMapper teamMapper,
      TeamMessageMapper teamMessageMapper,
      AuditEventsService auditEventsService) {
    super(
        trainingRunService,
        trainingDefinitionService,
        answersStorageApiService,
        securityService,
        userService,
        trainingFeedbackApiService,
        trainingRunMapper,
        levelMapper,
        hintMapper);
    this.trainingInstanceLobbyService = trainingInstanceLobbyService;
    this.coopTrainingRunService = coopTrainingRunService;
    this.scoreboardService = scoreboardService;
    this.teamMapper = teamMapper;
    this.teamMessageMapper = teamMessageMapper;
    this.auditEventsService = auditEventsService;
  }

  @PersistenceContext private EntityManager entityManager;

  /**
   * Check whether the user should wait for the start of his run
   *
   * @param accessToken instance access token
   * @return waiting state
   */
  @IsTraineeOrAdmin
  @Transactional
  public boolean isWaitingForStart(String accessToken) {
    TrainingInstance trainingInstance =
        trainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
    Long participantRefId = securityService.getUserRefIdFromUserAndGroup();

    if (trainingInstance.getType() == TrainingType.LINEAR) {
      return trainingInstance.notStarted();
    }

    if (trainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId).isPresent()
        || trainingInstanceLobbyService.isInLockedTeam(
            trainingInstance.getId(), participantRefId)) {
      return false;
    }

    if (!trainingInstanceLobbyService.isWaitingForStart(
        trainingInstance.getId(), participantRefId, !trainingInstance.notStarted())) {
      trainingInstanceLobbyService.addUserToQueue(
          trainingInstance.getTrainingInstanceLobby().getTrainingInstance().getId(),
          participantRefId);
      trainingInstanceLobbyService.updateTrainingInstanceLobby(
          trainingInstance.getTrainingInstanceLobby());
    }
    return true;
  }

  /**
   * Access Training Run by logged in user based on given accessToken.
   *
   * @param accessToken of one training instance
   * @return {@link AccessTrainingRunDTO} response
   */
  @IsTraineeOrAdmin
  @Transactional
  @Override
  public AccessTrainingRunDTO accessTrainingRun(String accessToken) {
    TrainingInstance trainingInstance =
        coopTrainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
    // checking if the user is not accessing to his existing training run (resume action)
    Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
    if (trainingInstance.notStarted()) {
      throw new ResourceNotReadyException(
          new EntityErrorDetail("The training instance has not started yet."));
    }

    Optional<TrainingRun> accessedTrainingRun =
        coopTrainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId);
    LOG.error("Run found: {}", accessedTrainingRun.isPresent());
    if (accessedTrainingRun.isPresent()) {
      TrainingRun trainingRun =
          coopTrainingRunService.resumeTrainingRun(accessedTrainingRun.get().getId());
      return convertToAccessTrainingRunDTO(trainingRun);
    }
    // Check if the user already clicked access training run, in that case, it returns an exception
    // (it prevents concurrent accesses).
    coopTrainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(
        participantRefId, trainingInstance.getId(), accessToken);
    TrainingRun trainingRun;
    try {
      TrainingRun newRun =
          coopTrainingRunService.createTrainingRun(trainingInstance, participantRefId);
      TrainingRun withLevel = trainingRunService.findByIdWithLevel(newRun.getId());
      if (!trainingInstance.isLocalEnvironment()) {
        coopTrainingRunService.assignSandbox(withLevel, trainingInstance.getPoolId());
      }
      coopTrainingRunService.auditTrainingRunStarted(withLevel);
      return convertToAccessTrainingRunDTO(withLevel);
    } catch (DataIntegrityViolationException | ConstraintViolationException exception) {
      // Allow only a signle team member to create the run
      // others must access existing
      return convertToAccessTrainingRunDTO(handleTeamRunExisting(accessToken, participantRefId));
    } catch (Exception e) {
      // delete/rollback acquisition lock when failed
      coopTrainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(
          participantRefId, trainingInstance.getId());
      throw e;
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public TrainingRun handleTeamRunExisting(String accessToken, Long participantRefId) {
    LOG.warn("Concurrent run creation detected — recovering existing team run.");
    entityManager.clear(); // Reset dirty session just in case

    TrainingInstance instance =
        this.coopTrainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
    UserRef userRef = userService.getUserByUserRefId(participantRefId);
    Optional<Team> teamOpt = userRef.getTeamByInstance(instance.getId());
    if (teamOpt.isEmpty()) {
      throw new EntityNotFoundException(
          new EntityErrorDetail("User is not a member of any team in this training instance"));
    }

    Team team = teamOpt.get();
    Long trainingRunId = coopTrainingRunService.getRelatedTrainingRun(team.getId()).getId();

    if (instance.isLocalEnvironment()) {
      return coopTrainingRunService.findByIdWithLevel(trainingRunId);
    }

    final int maxRetries = 20;
    final Duration retryDelay = Duration.ofMillis(200); // adjustable
    int attempt = 0;

    while (true) {
      TrainingRun fullRun = coopTrainingRunService.findById(trainingRunId);

      if (fullRun.getSandboxInstanceRefId() != null) {
        auditEventsService.auditTrainingRunResumedAction(fullRun);
        return coopTrainingRunService.findByIdWithLevel(trainingRunId);
      }

      if (++attempt >= maxRetries) {
        throw new IllegalStateException("Sandbox has not yet been assigned to team run");
      }

      try {
        Thread.sleep(retryDelay.toMillis());
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while waiting for sandbox assignment", ex);
      }
    }
  }

  /**
   * Delete selected training runs together with teams
   *
   * @param trainingRunIds training runs to delete
   * @param forceDelete indicates if this training run should be force deleted.
   */
  @IsOrganizerOrAdmin
  @Transactional
  @Override
  public void deleteTrainingRuns(List<Long> trainingRunIds, boolean forceDelete) {
    List<Long> teamsToDelete =
        trainingRunIds.stream()
            .map(coopTrainingRunService::findById)
            .filter(run -> run.getTrainingInstance().getType() == TrainingType.COOP)
            .map(run -> run.getCoopRunTeam().getId())
            .toList();
    super.deleteTrainingRuns(trainingRunIds, forceDelete);
    teamsToDelete.forEach(trainingInstanceLobbyService::deleteTeam);
  }

  /**
   * Delete selected training run together with team.
   *
   * @param trainingRunId training run to delete
   * @param forceDelete indicates if this training run should be force deleted.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingRun(#trainingRunId)")
  @Transactional
  public void deleteTrainingRun(Long trainingRunId, boolean forceDelete) {
    TrainingRun run = this.coopTrainingRunService.findById(trainingRunId);
    super.deleteTrainingRun(trainingRunId, forceDelete);
    if (run.getTrainingInstance().getType() == TrainingType.COOP) {
      this.trainingInstanceLobbyService.deleteTeam(run.getCoopRunTeam().getId());
    }
  }

  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalRO
  public LimitedScoreboardDTO getLimitedScoreboard(Long trainingRunId, List<Long> cachedTeams) {
    TrainingRun run = this.coopTrainingRunService.findById(trainingRunId);
    UserRef userRef =
        userService.getUserByUserRefId(securityService.getUserRefIdFromUserAndGroup());
    Long userTeamId =
        userRef
            .getTeamByInstance(run.getTrainingInstance().getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            "User is not a member of any team in this training instance")))
            .getId();

    Set<Long> cachedTeamIds = new java.util.HashSet<>(cachedTeams);

    LimitedScoreboardDTO scoreboardDTO =
        scoreboardService.getLimitedScoreboard(run.getTrainingInstance().getId(), userTeamId);
    scoreboardService.setFullUserRefDTOData(
        scoreboardDTO.getLimitedScoreboard().stream()
            .filter((team) -> !cachedTeamIds.contains(team.getTeam().getId()))
            .toList(),
        true);
    scoreboardDTO
        .getLimitedScoreboard()
        .forEach(
            (scoreDTO -> {
              if (cachedTeamIds.contains(scoreDTO.getTeam().getId())) {
                TeamDTO team = scoreDTO.getTeam();
                team.setMembers(new ArrayList<>());
                scoreDTO.setTeam(team);
              }
            }));
    return scoreboardDTO;
  }

  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
  @TransactionalRO
  public TeamDTO getTeam(Long runId) {
    TeamDTO dto = teamMapper.mapToDTO(this.coopTrainingRunService.findRelatedTeam(runId));
    dto.setMembers(
        dto.getMembers().stream()
            .map(member -> userService.getUserRefDTOWithLimitedInformation(member.getUserRefId()))
            .toList());
    return dto;
  }

  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)")
  @TransactionalRO
  public List<TeamScoreDTO> getFullScoreboard(Long instanceId) {
    List<TeamScoreDTO> teamScoreDTOs =
        new ArrayList<>(scoreboardService.getScoreboard(instanceId).values());
    scoreboardService.setFullUserRefDTOData(teamScoreDTOs, true);
    return teamScoreDTOs;
  }

  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTeam(#teamId)")
  @TransactionalRO
  public Map<Long, List<TeamMessageDTO>> getTeamMessagesByPlayer(Long teamId, Long since) {
    List<TeamMessage> messages = this.trainingInstanceLobbyService.getTeamMessages(teamId, since);
    return messages.stream()
        .collect(
            Collectors.groupingBy(
                message -> message.getSender().getUserRefId(),
                Collectors.mapping(teamMessageMapper::mapToDTO, Collectors.toList())));
  }

  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTeam(#teamId)"
          + "or @securityService.isTraineeOfGivenTeam(#teamId)")
  @Transactional
  public TeamMessageDTO saveTeamMessage(Long teamId, String message) {
    UserRef sender =
        this.userService.getUserByUserRefId(this.securityService.getUserRefIdFromUserAndGroup());
    Team team = this.trainingInstanceLobbyService.getTeamOrThrow(teamId);
    return teamMessageMapper.mapToDTO(
        this.trainingInstanceLobbyService.saveTeamMessage(team, sender, message.strip()));
  }

  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
  @TransactionalRO
  public ResponseEntity<AccessTrainingRunDTO> fetchUpdatedRunData(
      Long runId, Long currentLevelId, List<Long> hintIds, Boolean solutionShown) {
    TrainingRun trainingRun = coopTrainingRunService.findByIdWithLevelReadOnly(runId);

    if (trainingRun.getState() == TRState.FINISHED) {
      return ResponseEntity.status(HttpStatus.GONE).build();
    }

    if (!this.coopTrainingRunService.hasRunChanged(
        trainingRun,
        currentLevelId,
        hintIds == null ? new ArrayList<>() : hintIds,
        solutionShown != null && solutionShown)) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
    TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
    coopTrainingRunService.validateTrainingRunAccess(trainingInstance, trainingRun);
    return ResponseEntity.ok(createAccessRunDTO(trainingRun));
  }
}
