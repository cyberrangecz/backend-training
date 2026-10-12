package cz.cyberrange.platform.training.service.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.BasicLevelInfoDTO;
import cz.cyberrange.platform.training.api.dto.CorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.IsCorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelViewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.AssessmentLevelPreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessedTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.TraineeSandboxDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunBasicDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunByIdDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelPreviewDTO;
import cz.cyberrange.platform.training.api.enums.Actions;
import cz.cyberrange.platform.training.api.enums.LevelType;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.api.responses.VariantAnswer;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.SolutionInfo;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.question.QuestionAnswer;
import cz.cyberrange.platform.training.service.annotations.security.IsOrganizerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.security.IsTrainee;
import cz.cyberrange.platform.training.service.annotations.security.IsTraineeOrAdmin;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingRunMapper;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TraineeSandboxService;
import cz.cyberrange.platform.training.service.services.TraineeSandboxService.SandboxReadiness;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.utils.SandboxIdHasher;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientException;

/**
 * Orchestrates a trainee's progress through a training run between the REST layer and the
 * underlying services: starting, resuming, answering and finishing a run, retrieving its levels and
 * hints, and reporting run and participant data to organizers and administrators
 */
@Service
public class TrainingRunFacade {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingRunFacade.class);
  private static final String RUN_INSTANCE_TITLE_PROPERTY = "trainingInstance.title";

  @Value("${central.syslog.ip:127.0.0.1}")
  private String centralSyslogIp;

  private final TrainingRunService trainingRunService;
  private final AnswersStorageApiService answersStorageApiService;
  private final SecurityService securityService;
  private final UserService userService;
  private final TrainingRunMapper trainingRunMapper;
  private final LevelMapper levelMapper;
  private final HintMapper hintMapper;
  private final TraineeSandboxService traineeSandboxService;

  @Autowired
  public TrainingRunFacade(
      TrainingRunService trainingRunService,
      AnswersStorageApiService answersStorageApiService,
      SecurityService securityService,
      UserService userService,
      TrainingRunMapper trainingRunMapper,
      LevelMapper levelMapper,
      HintMapper hintMapper,
      TraineeSandboxService traineeSandboxService) {
    this.trainingRunService = trainingRunService;
    this.answersStorageApiService = answersStorageApiService;
    this.securityService = securityService;
    this.userService = userService;
    this.trainingRunMapper = trainingRunMapper;
    this.levelMapper = levelMapper;
    this.hintMapper = hintMapper;
    this.traineeSandboxService = traineeSandboxService;
  }

  /**
   * Finds specific Training Run by id.
   *
   * @param id of a Training Run that would be returned
   * @return specific {@link TrainingRunByIdDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#id)")
  @TransactionalRO
  public TrainingRunByIdDTO findById(Long id) {
    TrainingRun trainingRun = trainingRunService.findById(id);
    TrainingRunByIdDTO trainingRunByIdDTO = trainingRunMapper.mapToFindByIdDTO(trainingRun);
    trainingRunByIdDTO.setDefinitionId(
        trainingRun.getTrainingInstance().getTrainingDefinition().getId());
    trainingRunByIdDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
    trainingRunByIdDTO.setParticipantRef(
        userService.getUserRefDTOByUserRefId(
            trainingRunByIdDTO.getParticipantRef().getUserRefId()));
    return trainingRunByIdDTO;
  }

  /**
   * Delete selected training runs.
   *
   * @param trainingRunIds training runs to delete
   * @param forceDelete indicates if this training run should be force deleted.
   * @throws ForbiddenException if a non-administrator caller is not an organizer of one of the
   *     given training runs
   */
  @IsOrganizerOrAdmin
  @TransactionalWO
  public void deleteTrainingRuns(List<Long> trainingRunIds, boolean forceDelete) {
    if (trainingRunIds.isEmpty()) {
      return;
    }

    if (!securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
      for (Long trainingRunId : trainingRunIds) {
        if (!securityService.isOrganizerOfGivenTrainingRun(trainingRunId)) {
          throw new ForbiddenException("Cannot delete training runs from different instance.");
        }
      }
    }

    for (Long trainingRunId : trainingRunIds) {
      trainingRunService.deleteTrainingRun(trainingRunId, forceDelete, true);
    }
  }

  /**
   * Delete selected training run.
   *
   * @param trainingRunId training run to delete
   * @param forceDelete indicates if this training run should be force deleted.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public void deleteTrainingRun(Long trainingRunId, boolean forceDelete) {
    trainingRunService.deleteTrainingRun(trainingRunId, forceDelete, true);
  }

  /**
   * Finds all Training Runs of logged in user.
   *
   * @param predicate represents a predicate (boolean-valued function) of one argument.
   * @param pageable pageable parameter with information about pagination.
   * @param sortByTitle optional parameter. "asc" for ascending sort, "desc" for descending and null
   *     if sort is not wanted
   * @return Page of all {@link AccessedTrainingRunDTO} of logged in user.
   */
  @IsTraineeOrAdmin
  @TransactionalRO
  public PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(
      Predicate predicate, Pageable pageable, String sortByTitle) {
    Page<TrainingRun> trainingRuns =
        trainingRunService.findAllByParticipantRefUserRefId(
            predicate, orderedByInstanceTitle(pageable, sortByTitle));
    return convertToAccessedRunDTO(trainingRuns);
  }

  /**
   * Resumes a training run. When the current level is a training level, adds the solution the
   * participant has already taken for it, if any, and every hint the participant has already taken
   * for that same level.
   *
   * <p>When trainees of the run's instance allocate their own sandboxes, first brings the run's
   * sandbox up to date with the sandbox service. Until it is ready, the response carries no level
   * and keeps the trainee on the run overview; a run left with no sandbox gets one only through
   * {@link #accessTrainingRun}.
   *
   * @param trainingRunId id of Training Run to be resumed.
   * @return {@link AccessTrainingRunDTO} describing the run's current level
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public AccessTrainingRunDTO resumeTrainingRun(Long trainingRunId) {
    if (traineeSandboxService.isEnabled()) {
      TrainingRun runToResume = trainingRunService.findById(trainingRunId);
      if (traineeSandboxService.isEnabledFor(runToResume.getTrainingInstance())) {
        traineeSandboxService.lockUser(runToResume.getParticipantRef().getUserRefId());
        traineeSandboxService.reload(runToResume);
        trainingRunService.checkTrainingRunResumable(runToResume);
        SandboxReadiness readiness = traineeSandboxService.refreshSandbox(runToResume);
        if (readiness == SandboxReadiness.FIRST_READY) {
          trainingRunService.auditTrainingRunStarted(runToResume);
          return convertToAccessTrainingRunDTO(runToResume);
        }
        if (readiness != SandboxReadiness.READY) {
          return convertToRunOverviewDTO(
              runToResume.getTrainingInstance(), trainingRunId, readiness == SandboxReadiness.NONE);
        }
      }
    }
    TrainingRun trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
    AccessTrainingRunDTO accessTrainingRunDTO = convertToAccessTrainingRunDTO(trainingRun);
    if (trainingRun.getCurrentLevel() instanceof TrainingLevel) {
      if (trainingRun.isSolutionTaken()) {
        accessTrainingRunDTO.setTakenSolution(
            ((TrainingLevel) trainingRun.getCurrentLevel()).getSolution());
      }
      trainingRun
          .getHintInfoList()
          .forEach(
              hintInfo -> {
                if (hintInfo.getTrainingLevelId().equals(trainingRun.getCurrentLevel().getId())) {
                  accessTrainingRunDTO.getTakenHints().add(hintMapper.mapToDTO(hintInfo));
                }
              });
    }
    return accessTrainingRunDTO;
  }

  /**
   * Accesses a training instance identified by its access token on behalf of the logged in user.
   * Resumes the user's own already-running training run for that instance if one exists. Otherwise
   * acquires a per-user lock preventing concurrent accesses from the same user, creates a new
   * training run, assigns it a sandbox unless the instance runs in a local environment, and audits
   * the run as started. If any step inside the try block fails — creating the run, assigning a
   * sandbox, or auditing the run as started — the acquisition lock is released before the exception
   * is rethrown.
   *
   * <p>When trainees of the instance allocate their own sandboxes, the run is entered only once its
   * sandbox is ready; until then the response carries no level and keeps the user on the run
   * overview, see {@link #accessTrainingRunWithOwnSandbox}.
   *
   * @param accessToken of one training instance
   * @return {@link AccessTrainingRunDTO} describing the resumed or newly created run
   */
  @IsTraineeOrAdmin
  @Transactional
  public AccessTrainingRunDTO accessTrainingRun(String accessToken) {
    TrainingInstance trainingInstance =
        trainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
    // checking if the user is not accessing to his existing training run (resume action)
    Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
    if (traineeSandboxService.isEnabledFor(trainingInstance)) {
      return accessTrainingRunWithOwnSandbox(trainingInstance, accessToken, participantRefId);
    }
    Optional<TrainingRun> accessedTrainingRun =
        trainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId);
    if (accessedTrainingRun.isPresent()) {
      TrainingRun trainingRun =
          trainingRunService.resumeTrainingRun(accessedTrainingRun.get().getId());
      return convertToAccessTrainingRunDTO(trainingRun);
    }
    // Check if the user already clicked access training run, in that case, it returns an exception
    // (it prevents concurrent accesses).
    trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(
        participantRefId, trainingInstance.getId(), accessToken);
    try {
      // During this action we create a new TrainingRun and lock and get sandbox from OpenStack
      // Sandbox API
      TrainingRun trainingRun =
          trainingRunService.createTrainingRun(trainingInstance, participantRefId);
      if (!trainingInstance.isLocalEnvironment()) {
        trainingRunService.assignSandbox(trainingRun, trainingInstance.getPoolId());
      }
      trainingRunService.auditTrainingRunStarted(trainingRun);
      return convertToAccessTrainingRunDTO(trainingRun);
    } catch (Exception e) {
      // delete/rollback acquisition lock when no training run either sandbox is assigned
      trainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(
          participantRefId, trainingInstance.getId());
      throw e;
    }
  }

  /**
   * Accesses a training instance whose trainees allocate their own sandboxes. Enters the user's
   * running run of the instance once its sandbox is ready, starting the run when the sandbox is its
   * first. Otherwise keeps the user on the run overview: creates the run when there is none, gives
   * it a sandbox unless the user already has an active sandbox elsewhere, and lists the user's
   * sandboxes. A user whose active sandbox keeps the instance from giving them one gets no run.
   *
   * @throws EntityConflictException when the instance has ended, which its access token is still
   *     accepted for a short while
   */
  private AccessTrainingRunDTO accessTrainingRunWithOwnSandbox(
      TrainingInstance trainingInstance, String accessToken, Long participantRefId) {
    if (trainingInstance.getEndTime().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              Long.class,
              trainingInstance.getId(),
              "The training instance has ended, so no sandbox can be allocated for it."));
    }
    String userSub = securityService.getOidcSub();
    traineeSandboxService.lockUser(participantRefId);
    Optional<TrainingRun> existingRun =
        trainingRunService.findUnfinishedTrainingRunOfUser(accessToken, participantRefId);
    if (existingRun.isPresent()) {
      return enterTrainingRunWithOwnSandbox(existingRun.get(), userSub);
    }
    if (!traineeSandboxService.canProvideSandbox(userSub, trainingInstance.getPoolId())) {
      return convertToRunOverviewDTO(trainingInstance, null, false);
    }
    trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(
        participantRefId, trainingInstance.getId(), accessToken);
    try {
      TrainingRun trainingRun =
          trainingRunService.createTrainingRun(trainingInstance, participantRefId);
      return enterTrainingRunWithOwnSandbox(trainingRun, userSub);
    } catch (Exception e) {
      trainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(
          participantRefId, trainingInstance.getId());
      throw e;
    }
  }

  /**
   * Enters a run of the logged in user once its sandbox is ready, giving the run a sandbox when it
   * has none. Starts the run when the sandbox is its first, and keeps the user on the run overview
   * while the sandbox is not ready.
   */
  private AccessTrainingRunDTO enterTrainingRunWithOwnSandbox(
      TrainingRun trainingRun, String userSub) {
    SandboxReadiness readiness = traineeSandboxService.prepareSandbox(trainingRun, userSub);
    if (readiness == SandboxReadiness.FIRST_READY) {
      trainingRunService.auditTrainingRunStarted(trainingRun);
      return convertToAccessTrainingRunDTO(trainingRun);
    }
    if (readiness == SandboxReadiness.READY) {
      return convertToAccessTrainingRunDTO(
          trainingRunService.resumeTrainingRun(trainingRun.getId()));
    }
    return convertToRunOverviewDTO(trainingRun.getTrainingInstance(), trainingRun.getId(), false);
  }

  /**
   * Builds a response that keeps the trainee on the run overview: no level, and the sandboxes the
   * logged in user allocated for themselves.
   */
  private AccessTrainingRunDTO convertToRunOverviewDTO(
      TrainingInstance trainingInstance, Long trainingRunId, boolean allowAllocate) {
    AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
    accessTrainingRunDTO.setTrainingRunID(trainingRunId);
    accessTrainingRunDTO.setInstanceId(trainingInstance.getId());
    accessTrainingRunDTO.setTrainingInstanceTitle(trainingInstance.getTitle());
    accessTrainingRunDTO.setShowStepperBar(trainingInstance.isShowStepperBar());
    accessTrainingRunDTO.setBackwardMode(trainingInstance.isBackwardMode());
    accessTrainingRunDTO.setSandboxDefinitionId(trainingInstance.getSandboxDefinitionId());
    accessTrainingRunDTO.setAllowAllocate(allowAllocate);
    accessTrainingRunDTO.setActiveSandboxes(findOwnSandboxesForOverview());
    return accessTrainingRunDTO;
  }

  /**
   * Lists the logged in user's sandboxes for the run overview, or none when the sandbox service
   * fails to list them. The list only informs the user; failing to build it must not roll back a
   * sandbox the same request has just allocated.
   */
  private List<TraineeSandboxDTO> findOwnSandboxesForOverview() {
    try {
      return traineeSandboxService.findOwnSandboxes(securityService.getOidcSub());
    } catch (MicroserviceApiException | WebClientException ex) {
      LOG.warn("Could not list the sandboxes of the logged in user: {}", ex.getMessage());
      return List.of();
    }
  }

  private AccessTrainingRunDTO convertToAccessTrainingRunDTO(TrainingRun trainingRun) {
    AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
    accessTrainingRunDTO.setTrainingRunID(trainingRun.getId());
    accessTrainingRunDTO.setAbstractLevelDTO(getAbstractLevelDTO(trainingRun.getCurrentLevel()));
    accessTrainingRunDTO.setShowStepperBar(trainingRun.getTrainingInstance().isShowStepperBar());
    accessTrainingRunDTO.setInfoAboutLevels(
        getInfoAboutLevels(trainingRun.getCurrentLevel().getTrainingDefinition().getId()));
    accessTrainingRunDTO.setSandboxInstanceRefId(trainingRun.getSandboxInstanceRefId());
    accessTrainingRunDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
    accessTrainingRunDTO.setTrainingInstanceTitle(trainingRun.getTrainingInstance().getTitle());
    accessTrainingRunDTO.setManaged(trainingRun.getTrainingInstance().isManaged());
    accessTrainingRunDTO.setStartTime(trainingRun.getStartTime());
    accessTrainingRunDTO.setLocalEnvironment(
        trainingRun.getTrainingInstance().isLocalEnvironment());
    accessTrainingRunDTO.setBackwardMode(trainingRun.getTrainingInstance().isBackwardMode());
    accessTrainingRunDTO.setSandboxDefinitionId(
        trainingRun.getTrainingInstance().getSandboxDefinitionId());
    accessTrainingRunDTO.setLevelAnswered(trainingRun.isLevelAnswered());
    if (trainingRun.getCurrentLevel().getClass() == AccessLevel.class) {
      replacePlaceholders(
          (AccessLevelViewDTO) accessTrainingRunDTO.getAbstractLevelDTO(),
          trainingRun.getTrainingInstance().getAccessToken(),
          securityService.getBearerToken(),
          trainingRun.getParticipantRef().getUserRefId(),
          trainingRun.getTrainingInstance().getSandboxDefinitionId());
    }
    return accessTrainingRunDTO;
  }

  private List<BasicLevelInfoDTO> getInfoAboutLevels(Long definitionId) {
    List<BasicLevelInfoDTO> infoAboutLevels = new ArrayList<>();
    List<AbstractLevel> levels = trainingRunService.getLevels(definitionId);
    for (AbstractLevel abstractLevel : levels) {
      if (abstractLevel instanceof AssessmentLevel) {
        infoAboutLevels.add(
            new BasicLevelInfoDTO(
                abstractLevel.getId(),
                abstractLevel.getTitle(),
                LevelType.ASSESSMENT_LEVEL,
                abstractLevel.getOrder()));
      } else if (abstractLevel instanceof TrainingLevel) {
        infoAboutLevels.add(
            new BasicLevelInfoDTO(
                abstractLevel.getId(),
                abstractLevel.getTitle(),
                LevelType.TRAINING_LEVEL,
                abstractLevel.getOrder()));
      } else if (abstractLevel instanceof AccessLevel) {
        infoAboutLevels.add(
            new BasicLevelInfoDTO(
                abstractLevel.getId(),
                abstractLevel.getTitle(),
                LevelType.ACCESS_LEVEL,
                abstractLevel.getOrder()));
      } else {
        infoAboutLevels.add(
            new BasicLevelInfoDTO(
                abstractLevel.getId(),
                abstractLevel.getTitle(),
                LevelType.INFO_LEVEL,
                abstractLevel.getOrder()));
      }
    }
    return infoAboutLevels;
  }

  /**
   * Lists the sandboxes the logged in user allocated for themselves, in any state. Empty when the
   * deployment does not let trainees allocate sandboxes.
   *
   * @return the sandboxes of the logged in user
   */
  @IsTraineeOrAdmin
  @TransactionalRO
  public List<TraineeSandboxDTO> findOwnSandboxes() {
    if (!traineeSandboxService.isEnabled()) {
      return List.of();
    }
    return traineeSandboxService.findOwnSandboxes(securityService.getOidcSub());
  }

  /**
   * Asks the sandbox service to remove a sandbox the logged in user allocated for themselves.
   *
   * @param allocationUnitId the id of the sandbox's allocation unit
   * @throws ForbiddenException when the user did not allocate such a sandbox
   */
  @IsTraineeOrAdmin
  public void removeOwnSandbox(Integer allocationUnitId) {
    traineeSandboxService.removeOwnSandbox(securityService.getOidcSub(), allocationUnitId);
  }

  /**
   * Asks the sandbox service to remove a sandbox the logged in user allocated for themselves.
   *
   * @param sandboxId the UUID of the sandbox
   * @throws ForbiddenException when the user did not allocate such a sandbox
   */
  @IsTraineeOrAdmin
  public void removeOwnSandboxBySandboxId(String sandboxId) {
    traineeSandboxService.removeOwnSandboxBySandboxId(securityService.getOidcSub(), sandboxId);
  }

  /**
   * Finds all Training Runs by specific Training Definition and logged in user.
   *
   * @param trainingDefinitionId id of Training Definition
   * @param pageable pageable parameter with information about pagination.
   * @return Page of all {@link AccessedTrainingRunDTO} of logged in user and given definition.
   */
  @IsTrainee
  @TransactionalRO
  public PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(
      Long trainingDefinitionId, Pageable pageable) {
    Page<TrainingRun> trainingRuns =
        trainingRunService.findAllByTrainingDefinitionAndParticipant(
            trainingDefinitionId, pageable);
    PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource =
        trainingRunMapper.mapToPageResultResource(trainingRuns);
    addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
    return trainingRunDTOPageResultResource;
  }

  /**
   * Gets next level of given Training Run and set new current level.
   *
   * @param trainingRunId id of Training Run whose next level should be returned.
   * @return {@link AbstractLevelDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public AbstractLevelDTO getNextLevel(Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.moveToNextLevel(trainingRunId);
    AbstractLevelDTO abstractLevelDTO = getAbstractLevelDTO(trainingRun.getCurrentLevel());
    if (abstractLevelDTO.getClass() == AccessLevelViewDTO.class) {
      replacePlaceholders(
          (AccessLevelViewDTO) abstractLevelDTO,
          trainingRun.getTrainingInstance().getAccessToken(),
          securityService.getBearerToken(),
          trainingRun.getParticipantRef().getUserRefId(),
          trainingRun.getTrainingInstance().getSandboxDefinitionId());
    }
    return abstractLevelDTO;
  }

  /**
   * Gets solution of current level of given Training Run.
   *
   * @param trainingRunId id of Training Run which current level gets solution for.
   * @return solution of current level.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public String getSolution(Long trainingRunId) {
    return trainingRunService.getSolution(trainingRunId);
  }

  /**
   * Gets hint of given current level of given Training Run.
   *
   * @param trainingRunId id of Training Run which current level gets hint for.
   * @param hintId id of hint to be returned.
   * @return {@link HintDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public HintDTO getHint(Long trainingRunId, Long hintId) {
    return hintMapper.mapToDTO(trainingRunService.getHint(trainingRunId, hintId));
  }

  /**
   * Checks a submitted answer against the current level of the given training run. When no attempts
   * remain after this submission, the current level's solution is included in the result.
   *
   * @param trainingRunId id of Training Run to check answer.
   * @param answer string which player submit.
   * @return whether the answer was correct, the attempts remaining, and the solution once attempts
   *     are exhausted
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public IsCorrectAnswerDTO isCorrectAnswer(Long trainingRunId, String answer) {
    IsCorrectAnswerDTO correctAnswerDTO = new IsCorrectAnswerDTO();
    correctAnswerDTO.setCorrect(trainingRunService.isCorrectAnswer(trainingRunId, answer));
    correctAnswerDTO.setRemainingAttempts(trainingRunService.getRemainingAttempts(trainingRunId));
    if (correctAnswerDTO.getRemainingAttempts() == 0) {
      correctAnswerDTO.setSolution(getSolution(trainingRunId));
    }
    return correctAnswerDTO;
  }

  /**
   * Check given passkey of given Training Run.
   *
   * @param trainingRunId id of Training Run to check passkey.
   * @param passkey string which player submit.
   * @return true if passkey is correct, false if passkey is wrong.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public Boolean isCorrectPasskey(Long trainingRunId, String passkey) {
    return trainingRunService.isCorrectPassKey(trainingRunId, passkey);
  }

  /**
   * Finishes a training run. When the trainee allocated the run's sandbox, asks for the sandbox to
   * be removed.
   *
   * @param trainingRunId id of Training Run to be finished.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public void finishTrainingRun(Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.finishTrainingRun(trainingRunId);
    if (traineeSandboxService.isEnabledFor(trainingRun.getTrainingInstance())) {
      traineeSandboxService.removeSandboxOfFinishedRun(trainingRun);
    }
  }

  /**
   * Archive training run.
   *
   * @param trainingRunId id of Training Run to be archived.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public void archiveTrainingRun(Long trainingRunId) {
    trainingRunService.archiveTrainingRun(trainingRunId);
  }

  /**
   * Evaluate and store responses to assessment.
   *
   * @param trainingRunId id of Training Run to be finish.
   * @param responsesToQuestions responses to assessment
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public void evaluateResponsesToAssessment(
      Long trainingRunId, List<QuestionAnswerDTO> responsesToQuestions) {
    trainingRunService.evaluateResponsesToAssessment(
        trainingRunId,
        responsesToQuestions.stream()
            .collect(Collectors.toMap(QuestionAnswerDTO::getQuestionId, Function.identity())));
  }

  /**
   * Retrieve info about the participant of the given training run.
   *
   * @param trainingRunId id of the training run whose participant you want to get.
   * @return returns participant of the given training run.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalRO
  public UserRefDTO getParticipant(Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
    return userService.getUserRefDTOByUserRefId(trainingRun.getParticipantRef().getUserRefId());
  }

  /**
   * Finds Training Runs by their ids.
   *
   * <p>Only an administrator, the organizer of every listed run, or the participant of every listed
   * run is admitted, so a trainee receives only their own runs, each with its plain sandbox UUID.
   * For a caller who is neither administrator nor organizer, any run that is not the caller's own
   * would carry the SHA-256 hash of its sandbox UUID instead.
   *
   * @param ids the ids of Training Runs to return.
   * @return List of requested {@link TrainingRunBasicDTO}.
   */
  @TransactionalRO
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) or "
          + "@securityService.isTraineeOfGivenTrainingRuns(#ids) or "
          + "@securityService.isOrganizerOfGivenTrainingRuns(#ids)")
  public List<TrainingRunBasicDTO> findTrainingRunsByIds(List<Long> ids) {
    List<TrainingRunBasicDTO> runs =
        trainingRunMapper.mapToBasicDtoList(trainingRunService.findAllByIds(ids));
    boolean callerIsPrivileged =
        securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
            || securityService.isOrganizerOfGivenTrainingRuns(ids);
    if (!callerIsPrivileged) {
      Long callerUserRefId = securityService.getUserRefIdFromUserAndGroup();
      runs.forEach(
          run -> {
            if (run.getSandboxInstanceRefId() != null
                && (run.getParticipantRef() == null
                    || !callerUserRefId.equals(run.getParticipantRef().getUserRefId()))) {
              run.setSandboxInstanceRefId(SandboxIdHasher.hash(run.getSandboxInstanceRefId()));
            }
          });
    }
    resolveParticipantRefs(runs);
    return runs;
  }

  /**
   * Replaces the participant reference of each given training run with a reference resolved from
   * the user management service. A reference mapped from the database carries only the user
   * reference id; the descriptive fields are held by the user management service. References are
   * resolved in a single request, and one whose user the service does not return is left unchanged.
   *
   * @param runs the training runs whose participant references are replaced in place
   */
  private void resolveParticipantRefs(List<? extends TrainingRunBasicDTO> runs) {
    List<Long> participantRefIds =
        runs.stream()
            .map(TrainingRunBasicDTO::getParticipantRef)
            .filter(Objects::nonNull)
            .map(UserRefDTO::getUserRefId)
            .distinct()
            .toList();
    if (participantRefIds.isEmpty()) {
      return;
    }
    Map<Long, UserRefDTO> resolvedByUserRefId =
        userService.getUsersRefDTOByGivenUserIds(participantRefIds).stream()
            .collect(
                Collectors.toMap(
                    UserRefDTO::getUserRefId, Function.identity(), (kept, ignored) -> kept));
    runs.forEach(
        run -> {
          UserRefDTO participantRef = run.getParticipantRef();
          if (participantRef != null) {
            run.setParticipantRef(
                resolvedByUserRefId.getOrDefault(participantRef.getUserRefId(), participantRef));
          }
        });
  }

  /**
   * Finds Users by their ids.
   *
   * @param ids the ids of Users to return.
   * @return List of requested {@link UserRefDTO}.
   */
  @TransactionalRO
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) or "
          + "@securityService.sharesCommonTrainingInstance(#ids)")
  public List<UserRefDTO> findUsersByIds(List<Long> ids) {
    return userService.getUsersRefDTOByGivenUserIds(ids);
  }

  /**
   * Gets visited level of given Training Run.
   *
   * @param trainingRunId id of Training Run whose visited level should be returned.
   * @param levelId ID of the visited level.
   * @return {@link AbstractLevelDTO}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
  @TransactionalWO
  public AbstractLevelDTO getVisitedLevel(Long trainingRunId, Long levelId) {
    AbstractLevel abstractLevel = trainingRunService.getVisitedLevel(trainingRunId, levelId);
    TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
    return getAbstractLevelPreviewDTO(abstractLevel, trainingRun);
  }

  /**
   * Gets the correct answer of every training level in the definition backing the given training
   * run. For a level using variant answers, the correct answer is looked up per participant from
   * the answer storage service, either by the instance's access token (local environment) or by the
   * run's sandbox instance reference id.
   *
   * @param trainingRunId id of the Training Run whose definition's correct answers are returned.
   * @return the correct answer of every training level, ordered by level order.
   */
  @IsOrganizerOrAdmin
  @TransactionalWO
  public List<CorrectAnswerDTO> getCorrectAnswers(Long trainingRunId) {
    TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
    List<TrainingLevel> trainingLevels =
        trainingRunService
            .getLevels(trainingRun.getTrainingInstance().getTrainingDefinition().getId())
            .stream()
            .filter(abstractLevel -> abstractLevel.getClass() == TrainingLevel.class)
            .map(abstractLevel -> (TrainingLevel) abstractLevel)
            .toList();
    boolean isVariantAnswers = trainingLevels.stream().anyMatch(TrainingLevel::isVariantAnswers);
    Map<String, String> variantAnswers =
        isVariantAnswers ? getVariantAnswers(trainingRun) : new HashMap<>();
    return trainingLevels.stream()
        .map(trainingLevel -> mapToCorrectAnswerDTO(trainingLevel, variantAnswers))
        .collect(Collectors.toList());
  }

  private Map<String, String> getVariantAnswers(TrainingRun trainingRun) {
    TrainingInstance instance = trainingRun.getTrainingInstance();
    return instance.isLocalEnvironment()
        ? answersStorageApiService
            .getAnswersByAccessTokenAndUserId(
                instance.getAccessToken(), trainingRun.getParticipantRef().getUserRefId())
            .getVariantAnswers()
            .stream()
            .collect(
                Collectors.toMap(
                    VariantAnswer::getAnswerVariableName, VariantAnswer::getAnswerContent))
        : answersStorageApiService
            .getAnswersBySandboxId(trainingRun.getSandboxInstanceRefId())
            .getVariantAnswers()
            .stream()
            .collect(
                Collectors.toMap(
                    VariantAnswer::getAnswerVariableName, VariantAnswer::getAnswerContent));
  }

  private CorrectAnswerDTO mapToCorrectAnswerDTO(
      TrainingLevel trainingLevel, Map<String, String> variantAnswers) {
    CorrectAnswerDTO correctAnswerDTO = new CorrectAnswerDTO();
    correctAnswerDTO.setLevelId(trainingLevel.getId());
    correctAnswerDTO.setLevelTitle(trainingLevel.getTitle());
    correctAnswerDTO.setLevelOrder(trainingLevel.getOrder());
    if (trainingLevel.isVariantAnswers()) {
      correctAnswerDTO.setCorrectAnswer(variantAnswers.get(trainingLevel.getAnswerVariableName()));
      correctAnswerDTO.setVariableName(trainingLevel.getAnswerVariableName());
    } else {
      correctAnswerDTO.setCorrectAnswer(trainingLevel.getAnswer());
    }
    return correctAnswerDTO;
  }

  private void addParticipantsToTrainingRunDTOs(List<TrainingRunDTO> trainingRunDTOS) {
    trainingRunDTOS.forEach(
        trainingRunDTO ->
            trainingRunDTO.setParticipantRef(
                userService.getUserRefDTOByUserRefId(
                    trainingRunDTO.getParticipantRef().getUserRefId())));
  }

  private PageResultResource<AccessedTrainingRunDTO> convertToAccessedRunDTO(
      Page<TrainingRun> trainingRuns) {
    List<AccessedTrainingRunDTO> accessedTrainingRunDTOS = new ArrayList<>();
    for (TrainingRun trainingRun : trainingRuns) {
      AccessedTrainingRunDTO accessedTrainingRunDTO = generateAccessedTrainingRunDTO(trainingRun);
      accessedTrainingRunDTOS.add(accessedTrainingRunDTO);
    }
    return new PageResultResource<>(
        accessedTrainingRunDTOS, trainingRunMapper.createPagination(trainingRuns));
  }

  private Pageable orderedByInstanceTitle(Pageable pageable, String sortByTitle) {
    return Sort.Direction.fromOptionalString(sortByTitle)
        .<Pageable>map(
            direction ->
                PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(direction, RUN_INSTANCE_TITLE_PROPERTY).and(pageable.getSort())))
        .orElse(pageable);
  }

  private AccessedTrainingRunDTO generateAccessedTrainingRunDTO(TrainingRun trainingRun) {
    AccessedTrainingRunDTO accessedTrainingRunDTO = new AccessedTrainingRunDTO();
    accessedTrainingRunDTO.setId(trainingRun.getId());
    accessedTrainingRunDTO.setTitle(trainingRun.getTrainingInstance().getTitle());
    accessedTrainingRunDTO.setTrainingInstanceStartDate(
        trainingRun.getTrainingInstance().getStartTime());
    accessedTrainingRunDTO.setTrainingInstanceEndDate(
        trainingRun.getTrainingInstance().getEndTime());
    accessedTrainingRunDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
    accessedTrainingRunDTO.setNumberOfLevels(
        trainingRunService.getMaxLevelOrder(
                trainingRun.getTrainingInstance().getTrainingDefinition().getId())
            + 1);
    accessedTrainingRunDTO.setCurrentLevelOrder(trainingRun.getCurrentLevel().getOrder() + 1);
    accessedTrainingRunDTO.setPossibleAction(
        resolvePossibleActions(accessedTrainingRunDTO, trainingRun.getState()));
    return accessedTrainingRunDTO;
  }

  private Actions resolvePossibleActions(
      AccessedTrainingRunDTO trainingRunDTO, TRState trainingRunState) {
    boolean isTrainingInstanceRunning =
        LocalDateTime.now(Clock.systemUTC()).isBefore(trainingRunDTO.getTrainingInstanceEndDate());
    if (trainingRunState == TRState.FINISHED || !isTrainingInstanceRunning) {
      return Actions.RESULTS;
    } else {
      return Actions.RESUME;
    }
  }

  private AbstractLevelDTO getAbstractLevelDTO(AbstractLevel abstractLevel) {
    AbstractLevelDTO abstractLevelDTO;
    if (abstractLevel instanceof AssessmentLevel assessmentLevel) {
      abstractLevelDTO = levelMapper.mapToAssessmentLevelDTO(assessmentLevel);
      deleteInfoAboutCorrectnessFromQuestions((AssessmentLevelDTO) abstractLevelDTO);
    } else if (abstractLevel instanceof TrainingLevel trainingLevel) {
      abstractLevelDTO = levelMapper.mapToViewDTO(trainingLevel);
    } else if (abstractLevel instanceof AccessLevel accessLevel) {
      abstractLevelDTO = levelMapper.mapToViewDTO(accessLevel);
    } else {
      InfoLevel infoLevel = (InfoLevel) abstractLevel;
      abstractLevelDTO = levelMapper.mapToInfoLevelDTO(infoLevel);
    }
    return abstractLevelDTO;
  }

  private AbstractLevelDTO getAbstractLevelPreviewDTO(
      AbstractLevel abstractLevel, TrainingRun trainingRun) {
    if (abstractLevel instanceof InfoLevel infoLevel) {
      return levelMapper.mapToInfoLevelDTO(infoLevel);
    } else if (abstractLevel instanceof TrainingLevel trainingLevel) {
      return mapToTrainingLevelPreviewDTO(trainingLevel, trainingRun);
    } else if (abstractLevel instanceof AccessLevel accessLevel) {
      return mapToAccessLevelViewDTO(accessLevel, trainingRun);
    } else {
      return mapToAssessmentLevelPreviewDTO((AssessmentLevel) abstractLevel, trainingRun);
    }
  }

  private TrainingLevelPreviewDTO mapToTrainingLevelPreviewDTO(
      TrainingLevel trainingLevel, TrainingRun trainingRun) {
    TrainingLevelPreviewDTO trainingLevelPreviewDTO = levelMapper.mapToPreviewDTO(trainingLevel);
    trainingLevelPreviewDTO.setHints(
        hintMapper.mapToSetInfoDTO(
            trainingRun.getHintInfoList().stream()
                .filter(hintInfo -> hintInfo.getTrainingLevelId().equals(trainingLevel.getId()))
                .toList()));
    String takenSolution =
        trainingRun.getSolutionInfoList().stream()
            .filter(solutionInfo -> trainingLevel.getId().equals(solutionInfo.getTrainingLevelId()))
            .map(SolutionInfo::getSolutionContent)
            .findFirst()
            .orElse(null);
    if (takenSolution != null && takenSolution.contains("${ANSWER}")) {
      takenSolution =
          takenSolution.replaceAll(
              "\\$\\{ANSWER\\}",
              trainingRunService.getTrainingLevelCorrectAnswer(trainingLevel, trainingRun));
    }
    trainingLevelPreviewDTO.setSolution(takenSolution);
    return trainingLevelPreviewDTO;
  }

  private AccessLevelViewDTO mapToAccessLevelViewDTO(
      AccessLevel accessLevel, TrainingRun trainingRun) {
    AccessLevelViewDTO accessLevelViewDTO = levelMapper.mapToViewDTO(accessLevel);
    replacePlaceholders(
        accessLevelViewDTO,
        trainingRun.getTrainingInstance().getAccessToken(),
        securityService.getBearerToken(),
        trainingRun.getParticipantRef().getUserRefId(),
        trainingRun.getTrainingInstance().getSandboxDefinitionId());
    return accessLevelViewDTO;
  }

  private AssessmentLevelPreviewDTO mapToAssessmentLevelPreviewDTO(
      AssessmentLevel assessmentLevel, TrainingRun trainingRun) {
    Map<Long, Set<String>> userAnswersByQuestionId =
        trainingRunService.getQuestionAnswersByTrainingRunId(trainingRun.getId()).stream()
            .collect(Collectors.toMap(q -> q.getQuestion().getId(), QuestionAnswer::getAnswers));
    AssessmentLevelPreviewDTO assessmentPreviewDTO =
        levelMapper.mapToAssessmentLevelPreviewDTO(assessmentLevel);
    assessmentPreviewDTO
        .getQuestions()
        .forEach(
            previewQuestionDTO -> {
              if (previewQuestionDTO.getQuestionType() == QuestionType.EMI) {
                Set<String> emiAnswers =
                    userAnswersByQuestionId.getOrDefault(
                        previewQuestionDTO.getId(), new HashSet<>());
                emiAnswers.stream()
                    .map(this::convertEmiAnswerToMap)
                    .forEach(
                        emiAnswerMap ->
                            previewQuestionDTO
                                .getExtendedMatchingStatements()
                                .get(emiAnswerMap.get("statementOrder"))
                                .setUserOptionOrder(emiAnswerMap.get("optionOrder")));
              } else {
                if (previewQuestionDTO.getQuestionType() == QuestionType.FFQ) {
                  previewQuestionDTO.setChoices(new ArrayList<>());
                }
                previewQuestionDTO.setUserAnswers(
                    userAnswersByQuestionId.get(previewQuestionDTO.getId()));
              }
            });
    return assessmentPreviewDTO;
  }

  private Map<String, Integer> convertEmiAnswerToMap(String emiAnswer) {
    emiAnswer = emiAnswer.replaceAll("\"", "");
    emiAnswer = emiAnswer.substring(1, emiAnswer.length() - 1);
    return Arrays.stream(emiAnswer.split(","))
        .map(s -> s.split(":"))
        .collect(Collectors.toMap(s -> s[0].trim(), s -> Integer.parseInt(s[1].trim())));
  }

  private void replacePlaceholders(
      AccessLevelViewDTO accessLevelViewDTO,
      String accessToken,
      String bearerToken,
      Long userId,
      Long sandboxDefinitionId) {
    String localContent = accessLevelViewDTO.getLocalContent();
    localContent = localContent.replaceAll("\\$\\{ACCESS_TOKEN\\}", accessToken);
    localContent = localContent.replaceAll("\\$\\{BEARER_TOKEN\\}", bearerToken);
    localContent = localContent.replaceAll("\\$\\{USER_ID\\}", userId.toString());
    localContent =
        localContent.replaceAll(
            "\\$\\{SANDBOX_DEFINITION_ID\\}",
            sandboxDefinitionId == null ? "" : sandboxDefinitionId.toString());
    localContent = localContent.replaceAll("\\$\\{CENTRAL_SYSLOG_IP\\}", centralSyslogIp);
    accessLevelViewDTO.setLocalContent(localContent);
  }

  private void deleteInfoAboutCorrectnessFromQuestions(AssessmentLevelDTO assessmentLevelDTO) {
    assessmentLevelDTO
        .getQuestions()
        .forEach(
            questionDTO -> {
              questionDTO
                  .getChoices()
                  .forEach(questionChoiceDTO -> questionChoiceDTO.setCorrect(null));
              questionDTO
                  .getExtendedMatchingStatements()
                  .forEach(statementDTO -> statementDTO.setCorrectOptionOrder(null));
            });
  }
}
