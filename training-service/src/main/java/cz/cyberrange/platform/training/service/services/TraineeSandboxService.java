package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.dto.run.TraineeSandboxDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.exceptions.TooManyRequestsException;
import cz.cyberrange.platform.training.api.responses.AllocationUnitInfo;
import cz.cyberrange.platform.training.api.responses.SandboxRequestInfo;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;

/**
 * Manages the sandboxes trainees allocate for themselves. When trainee allocation is enabled for
 * the deployment, a trainee of an instance that is neither managed nor local gets a sandbox of
 * their own built when they enter the instance, instead of one the organizer allocated in advance.
 * A trainee holds at most one active sandbox at a time, across all instances; the sandbox service
 * enforces the same per pool.
 */
@Service
public class TraineeSandboxService {

  private static final Logger LOG = LoggerFactory.getLogger(TraineeSandboxService.class);
  private static final String STAGE_FINISHED = "FINISHED";
  private static final String STAGE_FAILED = "FAILED";

  private final SandboxApiService sandboxApiService;
  private final TrainingRunRepository trainingRunRepository;
  private final UserRefRepository userRefRepository;
  private final boolean traineeAllocationEnabled;

  @PersistenceContext private EntityManager entityManager;

  /** How far the sandbox of a training run is from being ready to work in */
  public enum SandboxReadiness {
    /** The run's sandbox is ready, and it was assigned to the run before */
    READY,
    /** The run's first sandbox has just become ready and is now assigned to the run */
    FIRST_READY,
    /** The run's sandbox is being built or removed, or it failed and must be removed */
    NOT_READY,
    /** The run has no sandbox */
    NONE
  }

  /**
   * Instantiates a new Trainee sandbox service.
   *
   * @param sandboxApiService the sandbox api service
   * @param trainingRunRepository the training run repository
   * @param userRefRepository the user ref repository
   * @param traineeAllocationEnabled whether trainees of instances that are not managed allocate
   *     their own sandboxes
   */
  @Autowired
  public TraineeSandboxService(
      SandboxApiService sandboxApiService,
      TrainingRunRepository trainingRunRepository,
      UserRefRepository userRefRepository,
      @Value("${single.sandbox.per.user.enabled:false}") boolean traineeAllocationEnabled) {
    this.sandboxApiService = sandboxApiService;
    this.trainingRunRepository = trainingRunRepository;
    this.userRefRepository = userRefRepository;
    this.traineeAllocationEnabled = traineeAllocationEnabled;
  }

  /**
   * Decides whether trainee allocation is enabled for the deployment.
   *
   * @return true when trainees of instances that are not managed allocate their own sandboxes
   */
  public boolean isEnabled() {
    return traineeAllocationEnabled;
  }

  /**
   * Decides whether trainees of the given instance allocate their own sandboxes: trainee allocation
   * is enabled for the deployment, and the instance is neither managed nor local.
   *
   * @param trainingInstance the training instance
   * @return true when trainees of the instance allocate their own sandboxes
   */
  public boolean isEnabledFor(TrainingInstance trainingInstance) {
    return traineeAllocationEnabled
        && !trainingInstance.isManaged()
        && !trainingInstance.isLocalEnvironment();
  }

  /**
   * Locks the given user until the current transaction ends, so that the user's requests that may
   * change the sandbox of their runs run one at a time. Lock the user before loading or locking the
   * training run. Does not wait for the lock: a request finding it held is turned away.
   *
   * @param userRefId the cross-service user reference id of the user
   * @throws TooManyRequestsException when another request of the user holds the lock
   */
  public void lockUser(Long userRefId) {
    userRefRepository.createOrGet(userRefId);
    try {
      userRefRepository.lockByUserRefId(userRefId);
    } catch (PessimisticLockingFailureException ex) {
      throw new TooManyRequestsException(
          new EntityErrorDetail(
              UserRef.class,
              "userRefId",
              Long.class,
              userRefId,
              "Another request of yours is preparing your sandbox. Try again in a moment."),
          ex);
    }
  }

  /**
   * Reloads the training run from the database, dropping state loaded before the caller took the
   * lock of {@link #lockUser}.
   *
   * @param trainingRun the training run
   */
  public void reload(TrainingRun trainingRun) {
    entityManager.refresh(trainingRun);
  }

  /**
   * Decides whether {@link #prepareSandbox} could give a run in the given pool a sandbox: the user
   * has no active sandbox, or has one in this pool that no training run uses.
   *
   * @param userSub the OIDC subject of the user
   * @param poolId the pool of the run's training instance
   * @return true when the run could get a sandbox
   */
  public boolean canProvideSandbox(String userSub, Long poolId) {
    List<AllocationUnitInfo> activeUnits = findActiveUnits(userSub);
    return activeUnits.isEmpty() || findUnusedUnit(activeUnits, poolId).isPresent();
  }

  /**
   * Brings the sandbox of the training run up to date with the sandbox service, without allocating
   * a new one. Assigns the sandbox to the run once it is ready, setting the run's start time when
   * it is the run's first sandbox. Clears the run's sandbox once its removal has started, and the
   * allocation too once the sandbox service no longer knows it. A sandbox the organizer allocated
   * is left as it is.
   *
   * @param trainingRun the training run
   * @return how far the run's sandbox is from being ready
   */
  public SandboxReadiness refreshSandbox(TrainingRun trainingRun) {
    Integer unitId = trainingRun.getSandboxInstanceAllocationId();
    if (unitId == null) {
      return SandboxReadiness.NONE;
    }
    Optional<AllocationUnitInfo> unit;
    try {
      unit = sandboxApiService.findAllocationUnit(unitId);
    } catch (MicroserviceApiException ex) {
      if (ex.getStatusCode() == HttpStatus.FORBIDDEN
          && trainingRun.getSandboxInstanceRefId() != null) {
        // The organizer allocated this sandbox, e.g. before the deployment enabled trainee
        // allocation; the run keeps using it as before.
        return SandboxReadiness.READY;
      }
      throw ex;
    }
    if (unit.isEmpty()) {
      releaseSandbox(trainingRun);
      trainingRun.setSandboxInstanceAllocationId(null);
      trainingRunRepository.save(trainingRun);
      return SandboxReadiness.NONE;
    }
    if (unit.get().getCleanupRequest() != null) {
      releaseSandbox(trainingRun);
      trainingRunRepository.save(trainingRun);
      return SandboxReadiness.NOT_READY;
    }
    if (!isReady(unit.get())) {
      return SandboxReadiness.NOT_READY;
    }
    String sandboxId = unit.get().getSandboxId();
    if (sandboxId.equals(trainingRun.getSandboxInstanceRefId())) {
      return SandboxReadiness.READY;
    }
    boolean firstSandbox =
        trainingRun.getSandboxInstanceRefId() == null
            && trainingRun.getPreviousSandboxInstanceRefId() == null;
    releaseSandbox(trainingRun);
    trainingRun.setSandboxInstanceRefId(sandboxId);
    if (firstSandbox) {
      trainingRun.setStartTime(LocalDateTime.now(Clock.systemUTC()));
    }
    trainingRunRepository.save(trainingRun);
    return firstSandbox ? SandboxReadiness.FIRST_READY : SandboxReadiness.READY;
  }

  /**
   * Brings the sandbox of the training run up to date like {@link #refreshSandbox}, and gives the
   * run a sandbox when it has none. It adopts the trainee's active sandbox in the run's pool that
   * no training run uses, such as one left behind when a request failed after allocating it, or
   * else allocates a new one when the trainee has no active sandbox. The caller must hold the lock
   * of {@link #lockUser} for the run's participant, and the run must belong to the logged in user,
   * who becomes the owner of a new sandbox.
   *
   * @param trainingRun the training run of the logged in user
   * @param userSub the OIDC subject of the logged in user
   * @return how far the run's sandbox is from being ready; never {@link SandboxReadiness#NONE}
   */
  public SandboxReadiness prepareSandbox(TrainingRun trainingRun, String userSub) {
    SandboxReadiness readiness = refreshSandbox(trainingRun);
    if (readiness != SandboxReadiness.NONE) {
      return readiness;
    }
    List<AllocationUnitInfo> activeUnits = findActiveUnits(userSub);
    Optional<AllocationUnitInfo> unusedUnit =
        findUnusedUnit(activeUnits, trainingRun.getTrainingInstance().getPoolId());
    if (unusedUnit.isPresent()) {
      trainingRun.setSandboxInstanceAllocationId(unusedUnit.get().getId());
      trainingRunRepository.save(trainingRun);
      readiness = refreshSandbox(trainingRun);
    } else if (activeUnits.isEmpty()) {
      allocateSandbox(trainingRun);
    }
    return readiness == SandboxReadiness.NONE ? SandboxReadiness.NOT_READY : readiness;
  }

  /**
   * Asks the sandbox service to remove the sandbox of a finished training run, so that it does not
   * keep the trainee from getting another one. A sandbox the organizer allocated, or one the call
   * fails to remove, is left to the cleanup job and the trainee.
   *
   * @param trainingRun the finished training run
   */
  public void removeSandboxOfFinishedRun(TrainingRun trainingRun) {
    Integer unitId = trainingRun.getSandboxInstanceAllocationId();
    if (unitId == null) {
      return;
    }
    try {
      sandboxApiService.requestCleanup(unitId);
    } catch (MicroserviceApiException | WebClientException ex) {
      LOG.warn(
          "Could not remove the sandbox of allocation unit {} of finished training run {}: {}",
          unitId,
          trainingRun.getId(),
          ex.getMessage());
    }
  }

  private void allocateSandbox(TrainingRun trainingRun) {
    TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
    sandboxApiService
        .allocateSandboxForTrainee(trainingInstance.getPoolId(), trainingInstance.getAccessToken())
        .ifPresent(
            unit -> {
              trainingRun.setSandboxInstanceAllocationId(unit.getId());
              trainingRunRepository.save(trainingRun);
            });
  }

  private List<AllocationUnitInfo> findActiveUnits(String userSub) {
    return sandboxApiService.findAllocationUnitsCreatedBy(userSub).stream()
        .filter(TraineeSandboxService::isActive)
        .toList();
  }

  /**
   * Finds a unit of the given pool among the given ones that no training run uses: none is
   * allocated with it, and none works or worked in its sandbox.
   */
  private Optional<AllocationUnitInfo> findUnusedUnit(List<AllocationUnitInfo> units, Long poolId) {
    List<AllocationUnitInfo> unitsInPool =
        units.stream().filter(unit -> poolId.equals(unit.getPoolId())).toList();
    if (unitsInPool.isEmpty()) {
      return Optional.empty();
    }
    Set<Integer> usedUnitIds =
        trainingRunRepository
            .findAllBySandboxInstanceAllocationIds(
                unitsInPool.stream().map(AllocationUnitInfo::getId).toList())
            .stream()
            .map(TrainingRun::getSandboxInstanceAllocationId)
            .collect(Collectors.toSet());
    return unitsInPool.stream()
        .filter(unit -> !usedUnitIds.contains(unit.getId()))
        .filter(
            unit ->
                unit.getSandboxId() == null
                    || !trainingRunRepository
                        .existsBySandboxInstanceRefIdOrPreviousSandboxInstanceRefId(
                            unit.getSandboxId(), unit.getSandboxId()))
        .findFirst();
  }

  /**
   * Lists the sandboxes the user allocated for themselves, in any state, each with the training
   * instance of the run it was allocated for when that run still exists.
   *
   * @param userSub the OIDC subject of the user
   * @return the user's sandboxes
   */
  public List<TraineeSandboxDTO> findOwnSandboxes(String userSub) {
    List<AllocationUnitInfo> units = sandboxApiService.findAllocationUnitsCreatedBy(userSub);
    if (units.isEmpty()) {
      return List.of();
    }
    Map<Integer, TrainingInstance> instancesByUnitId =
        trainingRunRepository
            .findAllBySandboxInstanceAllocationIds(
                units.stream().map(AllocationUnitInfo::getId).toList())
            .stream()
            .collect(
                Collectors.toMap(
                    TrainingRun::getSandboxInstanceAllocationId,
                    TrainingRun::getTrainingInstance,
                    (kept, ignored) -> kept));
    return units.stream()
        .map(unit -> toTraineeSandboxDTO(unit, instancesByUnitId.get(unit.getId())))
        .toList();
  }

  /**
   * Asks the sandbox service to remove a sandbox the user allocated for themselves. Nothing happens
   * when it is gone already.
   *
   * @param userSub the OIDC subject of the user
   * @param unitId the id of the sandbox's allocation unit
   * @throws ForbiddenException when the user did not allocate such a sandbox
   */
  public void removeOwnSandbox(String userSub, Integer unitId) {
    requestCleanupOfOwnUnit(userSub, unit -> unitId.equals(unit.getId()));
  }

  /**
   * Asks the sandbox service to remove a sandbox the user allocated for themselves.
   *
   * @param userSub the OIDC subject of the user
   * @param sandboxId the UUID of the sandbox
   * @throws ForbiddenException when the user did not allocate such a sandbox
   */
  public void removeOwnSandboxBySandboxId(String userSub, String sandboxId) {
    requestCleanupOfOwnUnit(userSub, unit -> sandboxId.equals(unit.getSandboxId()));
  }

  private void requestCleanupOfOwnUnit(String userSub, Predicate<AllocationUnitInfo> matches) {
    AllocationUnitInfo unit =
        sandboxApiService.findAllocationUnitsCreatedBy(userSub).stream()
            .filter(matches)
            .findFirst()
            .orElseThrow(
                () -> new ForbiddenException("Sandbox not found or you did not allocate it."));
    sandboxApiService.requestCleanup(unit.getId());
  }

  /** Moves the run's sandbox, if any, to its previous sandbox, keeping its allocation */
  private static void releaseSandbox(TrainingRun trainingRun) {
    if (trainingRun.getSandboxInstanceRefId() != null) {
      trainingRun.setPreviousSandboxInstanceRefId(trainingRun.getSandboxInstanceRefId());
      trainingRun.setSandboxInstanceRefId(null);
    }
  }

  private static TraineeSandboxDTO toTraineeSandboxDTO(
      AllocationUnitInfo unit, TrainingInstance trainingInstance) {
    TraineeSandboxDTO sandbox = new TraineeSandboxDTO();
    sandbox.setId(unit.getId());
    sandbox.setPoolId(unit.getPoolId());
    sandbox.setSandboxId(unit.getSandboxId());
    sandbox.setCreatedAt(unit.getCreatedAt());
    sandbox.setAllocationRequest(unit.getAllocationRequest());
    sandbox.setCleanupRequest(unit.getCleanupRequest());
    sandbox.setAllowRemove(isRemovable(unit));
    if (trainingInstance != null) {
      sandbox.setTrainingInstanceId(trainingInstance.getId());
      sandbox.setTrainingInstanceTitle(trainingInstance.getTitle());
    }
    return sandbox;
  }

  /**
   * The unit is built or being built, and not removed: its allocation has not failed, and it is not
   * being cleaned up or its cleanup is still running. Matches the sandbox service's own rule.
   */
  static boolean isActive(AllocationUnitInfo unit) {
    return !anyStage(unit.getAllocationRequest(), STAGE_FAILED)
        && (unit.getCleanupRequest() == null || isRunning(unit.getCleanupRequest()));
  }

  /** The unit's sandbox is built and its removal has not been requested */
  static boolean isReady(AllocationUnitInfo unit) {
    return unit.getSandboxId() != null
        && unit.getCleanupRequest() == null
        && allStages(unit.getAllocationRequest(), STAGE_FINISHED);
  }

  /**
   * The trainee may remove the unit's sandbox: it is built, or building or removing it failed. It
   * may not while the sandbox is being built or removed.
   */
  static boolean isRemovable(AllocationUnitInfo unit) {
    if (unit.getCleanupRequest() != null) {
      return !isRunning(unit.getCleanupRequest());
    }
    return isReady(unit) || anyStage(unit.getAllocationRequest(), STAGE_FAILED);
  }

  private static boolean isRunning(SandboxRequestInfo request) {
    return !request.getStages().stream()
        .allMatch(stage -> STAGE_FINISHED.equals(stage) || STAGE_FAILED.equals(stage));
  }

  private static boolean anyStage(SandboxRequestInfo request, String state) {
    return request != null && request.getStages().stream().anyMatch(state::equals);
  }

  private static boolean allStages(SandboxRequestInfo request, String state) {
    return request != null
        && !request.getStages().isEmpty()
        && request.getStages().stream().allMatch(state::equals);
  }
}
