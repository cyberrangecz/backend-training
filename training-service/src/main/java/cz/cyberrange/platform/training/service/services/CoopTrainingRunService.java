package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotModifiedException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.HintInfo;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TRAcquisitionLockRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.ElasticsearchApiService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** The type Training run service. */
@Service
public class CoopTrainingRunService extends TrainingRunService {

  private static final Logger LOG = Logger.getLogger(CoopTrainingRunService.class.getName());
  private final UserService userService;
  private final TrainingInstanceLobbyService trainingInstanceLobbyService;

  public CoopTrainingRunService(
      TrainingRunRepository trainingRunRepository,
      AbstractLevelRepository abstractLevelRepository,
      TrainingInstanceRepository trainingInstanceRepository,
      UserRefRepository participantRefRepository,
      HintRepository hintRepository,
      AuditEventsService auditEventsService,
      ElasticsearchApiService elasticsearchApiService,
      AnswersStorageApiService answersStorageApiService,
      SecurityService securityService,
      QuestionAnswerRepository questionAnswerRepository,
      SandboxApiService sandboxApiService,
      TRAcquisitionLockRepository trAcquisitionLockRepository,
      SubmissionRepository submissionRepository,
      UserService userService,
      TrainingInstanceLobbyService trainingInstanceLobbyService) {
    super(
        trainingRunRepository,
        abstractLevelRepository,
        trainingInstanceRepository,
        participantRefRepository,
        hintRepository,
        auditEventsService,
        elasticsearchApiService,
        answersStorageApiService,
        securityService,
        questionAnswerRepository,
        sandboxApiService,
        trAcquisitionLockRepository,
        submissionRepository);
    this.userService = userService;
    this.trainingInstanceLobbyService = trainingInstanceLobbyService;
  }

  /**
   * Find running training run of user optional.
   *
   * @param accessToken the access token
   * @param participantRefId the participant ref id
   * @return the optional
   */
  @Override
  public Optional<TrainingRun> findRunningTrainingRunOfUser(
      String accessToken, Long participantRefId) {
    TrainingInstance trainingInstance =
        this.getTrainingInstanceForParticularAccessToken(accessToken);
    UserRef userRef =
        userService.getUserByUserRefId(securityService.getUserRefIdFromUserAndGroup());
    Optional<Team> team = userRef.getTeamByInstance(trainingInstance.getId());
    LOG.severe("Team found: " + team.isPresent());
    if (team.isEmpty()) {
      return Optional.empty();
    }
    return trainingRunRepository.findByCoopRunTeam_IdAndStateLike(
        team.get().getId(), TRState.RUNNING);
  }

  /**
   * Access training run based on given accessToken.
   *
   * @param trainingInstance the training instance
   * @param participantRefId the participant ref id
   * @return accessed {@link TrainingRun}
   * @throws EntityNotFoundException no active training instance for given access token, no starting
   *     level in training definition.
   * @throws EntityConflictException pool of sandboxes is not created for training instance.
   */
  @Override
  protected TrainingRun getNewTrainingRun(
      AbstractLevel currentLevel,
      TrainingInstance trainingInstance,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Long participantRefId) {
    TrainingRun newTrainingRun =
        super.getNewTrainingRun(
            currentLevel, trainingInstance, startTime, endTime, participantRefId);
    newTrainingRun.setType(TrainingType.COOP);
    Optional<Team> team =
        userService
            .getUserByUserRefId(participantRefId)
            .getTeamByInstance(trainingInstance.getId());
    if (team.isEmpty()) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              String.format(
                  "User with id %d is not a member of any team in training instance with id %d",
                  participantRefId, trainingInstance.getId())));
    }
    newTrainingRun.setCoopRunTeam(team.get());
    return newTrainingRun;
  }

  public Team findRelatedTeam(Long trainingRunId) {
    TrainingRun run = findById(trainingRunId);
    if (run.getCoopRunTeam() == null) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              String.format("Training run with id %d does not have a team", trainingRunId)));
    }
    return run.getCoopRunTeam();
  }

  public TrainingRun findRelatedTrainingRun(Long teamId) {
    return this.trainingRunRepository
        .findByCoopRunTeam_Id(teamId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        String.format("Team with id %d has no training run", teamId))));
  }

  public void validateRunChanged(
      Long runId, Long currentLevelId, List<Long> hintIds, Boolean solutionShown) {
    TrainingRun trainingRun = findByIdWithLevel(runId);

    boolean levelIdChanged = !trainingRun.getCurrentLevel().getId().equals(currentLevelId);
    boolean hintsChanged =
        !new HashSet<>(hintIds)
            .containsAll(
                trainingRun.getHintInfoList().stream()
                    .filter(
                        (hintInfo -> Objects.equals(hintInfo.getTrainingLevelId(), currentLevelId)))
                    .map(HintInfo::getHintId)
                    .collect(Collectors.toSet()));
    boolean solutionChanged = !solutionShown.equals(trainingRun.isSolutionTaken());

    if (!(levelIdChanged || hintsChanged || solutionChanged)) {
      throw new EntityNotModifiedException(
          new EntityErrorDetail(TrainingRun.class, "id", Long.class, runId));
    }
  }
}
