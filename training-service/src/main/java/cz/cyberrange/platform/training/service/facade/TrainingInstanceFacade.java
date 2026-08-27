package cz.cyberrange.platform.training.service.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.event.AbstractEventDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceAssignPoolIdDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicInfoDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceFindAllResponseDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceIsFinishedInfoDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.service.annotations.security.IsOrganizerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EventMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingInstanceMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingRunMapper;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.TrainingEventAccessService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import cz.cyberrange.platform.training.service.services.detection.CheatingDetectionService;
import cz.cyberrange.platform.training.service.utils.SandboxIdHasher;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Orchestrates training instance lifecycle operations between the REST layer and the underlying
 * services: creation, update and deletion of instances, pool assignment, organizer management, and
 * retrieval of the instance's training runs and audited events.
 */
@Service
public class TrainingInstanceFacade {

  /** Event type discriminator selecting console commands, stored per pool rather than per run. */
  private static final String COMMAND_EVENT_TYPE = "COMMAND";

  private final TrainingInstanceService trainingInstanceService;
  private final TrainingDefinitionService trainingDefinitionService;
  private final TrainingRunService trainingRunService;
  private final CheatingDetectionService cheatingDetectionService;
  private final UserService userService;
  private final SecurityService securityService;
  private final TrainingInstanceMapper trainingInstanceMapper;
  private final TrainingRunMapper trainingRunMapper;
  private final SandboxApiService sandboxApiService;
  private final CommandEventsService commandEventsService;
  private final TrainingEventsService trainingEventsService;
  private final EventMapper eventMapper;
  private final TrainingEventAccessService trainingEventAccessService;

  @Autowired
  public TrainingInstanceFacade(
      TrainingInstanceService trainingInstanceService,
      TrainingDefinitionService trainingDefinitionService,
      TrainingRunService trainingRunService,
      CheatingDetectionService cheatingDetectionService,
      UserService userService,
      SecurityService securityService,
      SandboxApiService sandboxApiService,
      TrainingInstanceMapper trainingInstanceMapper,
      TrainingRunMapper trainingRunMapper,
      CommandEventsService commandEventsService,
      TrainingEventsService trainingEventsService,
      EventMapper eventMapper,
      TrainingEventAccessService trainingEventAccessService) {
    this.trainingInstanceService = trainingInstanceService;
    this.trainingDefinitionService = trainingDefinitionService;
    this.trainingRunService = trainingRunService;
    this.cheatingDetectionService = cheatingDetectionService;
    this.userService = userService;
    this.securityService = securityService;
    this.sandboxApiService = sandboxApiService;
    this.trainingInstanceMapper = trainingInstanceMapper;
    this.trainingRunMapper = trainingRunMapper;
    this.commandEventsService = commandEventsService;
    this.trainingEventsService = trainingEventsService;
    this.eventMapper = eventMapper;
    this.trainingEventAccessService = trainingEventAccessService;
  }

  /**
   * Finds specific Training Instance by id.
   *
   * @param id of a Training Instance that would be returned
   * @return specific {@link TrainingInstanceDTO} by id
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#id)")
  @TransactionalRO
  public TrainingInstanceDTO findById(Long id) {
    return trainingInstanceMapper.mapToDTO(trainingInstanceService.findByIdIncludingDefinition(id));
  }

  /**
   * Get Training instance access token by pool id.
   *
   * @param poolId id of the assigned pool.
   * @return Requested access token by pool id if it exists.
   */
  @IsOrganizerOrAdmin
  @TransactionalRO
  public String findInstanceAccessTokenByPoolId(Long poolId) {
    return trainingInstanceService.findInstanceAccessTokenByPoolId(poolId);
  }

  /**
   * Find all Training Instances.
   *
   * @param predicate represents a predicate (boolean-valued function) of one argument.
   * @param pageable pageable parameter with information about pagination.
   * @return page of all {@link TrainingInstanceFindAllResponseDTO}
   */
  @IsOrganizerOrAdmin
  @TransactionalRO
  public PageResultResource<TrainingInstanceFindAllResponseDTO> findAll(
      Predicate predicate, Pageable pageable) {
    if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
      return trainingInstanceMapper.mapToPageResultResourceBasicView(
          trainingInstanceService.findAll(predicate, pageable));
    }
    return trainingInstanceMapper.mapToPageResultResourceBasicView(
        trainingInstanceService.findAll(
            predicate, pageable, securityService.getUserRefIdFromUserAndGroup()));
  }

  /**
   * Updates a training instance. Refuses the update if the instance has already started and the
   * assigned training definition would change. Validates that the instance's local environment and
   * pool configuration are consistent, and that the sandbox definition or pool exposes every
   * variable name referenced by the training definition's levels. Locks the new pool when a pool is
   * assigned for the first time; unlocks and deletes a previously assigned pool's recorded console
   * commands whenever that pool is replaced or removed.
   *
   * @param trainingInstanceUpdateDTO to be updated
   * @return the access token in effect after the update, whether it changed or was kept
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceUpdateDTO.getId())")
  @TransactionalWO
  public String update(TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
    TrainingInstance updatedTrainingInstance =
        trainingInstanceMapper.mapUpdateToEntity(trainingInstanceUpdateDTO);
    TrainingInstance trainingInstance =
        trainingInstanceService.findById(trainingInstanceUpdateDTO.getId());

    if (LocalDateTime.now(Clock.systemUTC()).isAfter(trainingInstance.getStartTime())
        && !trainingInstance
            .getTrainingDefinition()
            .getId()
            .equals(trainingInstanceUpdateDTO.getTrainingDefinitionId())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              Long.class,
              trainingInstance.getId(),
              "The training definition assigned to running training instance cannot be changed."));
    }
    checkLocalEnvironmentConfiguration(updatedTrainingInstance);
    updatedTrainingInstance.setTrainingDefinition(
        trainingDefinitionService.findById(trainingInstanceUpdateDTO.getTrainingDefinitionId()));
    validateVariableNames(updatedTrainingInstance);
    Long oldPoolId = trainingInstance.getPoolId();
    String accessToken = trainingInstanceService.update(updatedTrainingInstance);
    if (isPoolIdChanged(oldPoolId, updatedTrainingInstance.getPoolId())) {
      handlePoolIdModification(
          oldPoolId, updatedTrainingInstance.getPoolId(), trainingInstance.getAccessToken());
    }
    return accessToken;
  }

  private void validateVariableNames(TrainingInstance trainingInstance) {
    if (trainingInstance.getPoolId() == null && trainingInstance.getSandboxDefinitionId() == null) {
      return;
    }
    Set<String> trainingDefinitionVariables =
        trainingDefinitionService
            .getAllTrainingLevels(trainingInstance.getTrainingDefinition().getId())
            .stream()
            .map(TrainingLevel::getAnswerVariableName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    if (!trainingDefinitionVariables.isEmpty()) {
      Set<String> sandboxDefinitionVariables =
          trainingInstance.getPoolId() != null
              ? sandboxApiService.getVariablesByPoolId(trainingInstance.getPoolId()).getVariables()
              : sandboxApiService.getVariablesBySandboxDefinitionId(
                  trainingInstance.getSandboxDefinitionId());
      if (!sandboxDefinitionVariables.containsAll(trainingDefinitionVariables)) {
        trainingDefinitionVariables.removeAll(sandboxDefinitionVariables);
        throw new EntityConflictException(
            new EntityErrorDetail(
                "Variable names ["
                    + StringUtils.collectionToCommaDelimitedString(trainingDefinitionVariables)
                    + "] defined in the training definition (ID: "
                    + trainingInstance.getTrainingDefinition().getId()
                    + ") aren't present in the sandbox definition."));
      }
    }
  }

  private void handlePoolIdModification(Long currentPoolId, Long newPoolId, String accessToken) {
    if (currentPoolId == null) {
      sandboxApiService.lockPool(newPoolId, accessToken);
    } else if (newPoolId == null) {
      sandboxApiService.unlockPool(currentPoolId);
      deleteBashCommandsByPool(currentPoolId);
    } else {
      sandboxApiService.unlockPool(currentPoolId);
      deleteBashCommandsByPool(currentPoolId);
      sandboxApiService.lockPool(newPoolId, accessToken);
    }
  }

  private boolean isPoolIdChanged(Long currentPoolId, Long updatedPoolId) {
    return (currentPoolId == null && updatedPoolId != null)
        || (currentPoolId != null && (!currentPoolId.equals(updatedPoolId)));
  }

  /**
   * Creates new training instance.
   *
   * @param trainingInstanceCreateDTO to be created
   * @return created {@link TrainingInstanceDTO}
   */
  @IsOrganizerOrAdmin
  @TransactionalWO
  public TrainingInstanceDTO create(TrainingInstanceCreateDTO trainingInstanceCreateDTO) {
    TrainingInstance trainingInstance =
        trainingInstanceMapper.mapCreateToEntity(trainingInstanceCreateDTO);
    checkLocalEnvironmentConfiguration(trainingInstance);
    trainingInstance.setTrainingDefinition(
        trainingDefinitionService.findById(trainingInstanceCreateDTO.getTrainingDefinitionId()));
    validateVariableNames(trainingInstance);
    trainingInstance.setId(null);
    TrainingInstance createdTrainingInstance = trainingInstanceService.create(trainingInstance);
    if (trainingInstance.getPoolId() != null) {
      handlePoolIdModification(
          null, trainingInstance.getPoolId(), trainingInstance.getAccessToken());
    }
    return trainingInstanceMapper.mapToDTO(createdTrainingInstance);
  }

  private void addOrganizersToTrainingInstance(
      TrainingInstance trainingInstance, Set<Long> userRefIdsOfOrganizers) {
    if (userRefIdsOfOrganizers.isEmpty()) return;
    List<UserRefDTO> organizers =
        getAllUsersRefsByGivenUsersIds(new ArrayList<>(userRefIdsOfOrganizers));
    Set<Long> actualOrganizersIds =
        trainingInstance.getOrganizers().stream()
            .map(UserRef::getUserRefId)
            .collect(Collectors.toSet());
    for (UserRefDTO organizer : organizers) {
      if (actualOrganizersIds.contains(organizer.getUserRefId())) {
        continue;
      }
      UserRef userRef = userService.createOrGetUserRef(organizer.getUserRefId());
      trainingInstance.addOrganizer(userRef);
    }
  }

  private List<UserRefDTO> getAllUsersRefsByGivenUsersIds(List<Long> participantsRefIds) {
    List<UserRefDTO> users = new ArrayList<>();
    PageResultResource<UserRefDTO> usersPageResultResource;
    int page = 0;
    do {
      usersPageResultResource =
          userService.getUsersRefDTOByGivenUserIds(
              participantsRefIds, PageRequest.of(page, 999), null, null);
      users.addAll(usersPageResultResource.getContent());
      page++;
    } while (page < usersPageResultResource.getPagination().getTotalPages());
    return users;
  }

  /**
   * Deletes a training instance together with its training runs, cheating detections and audited
   * events. Unless {@code forceDelete} is set, refuses to delete an instance that has not finished
   * and still has training runs, and refuses to delete an instance with a pool still assigned. When
   * {@code forceDelete} is set on a non-local instance with an assigned pool, unlocks the pool and
   * deletes its recorded console commands instead of raising either refusal.
   *
   * @param trainingInstanceId of training instance to be deleted
   * @param forceDelete indicates if the instance should be deleted regardless of these checks.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalWO
  public void delete(Long trainingInstanceId, boolean forceDelete) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    if (forceDelete) {
      if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() != null) {
        sandboxApiService.unlockPool(trainingInstance.getPoolId());
        deleteBashCommandsByPool(trainingInstance.getPoolId());
      }
    } else if (!trainingInstanceService.checkIfInstanceIsFinished(trainingInstanceId)
        && trainingRunService.existsAnyForTrainingInstance(trainingInstanceId)) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              Long.class,
              trainingInstanceId,
              "Active training instance with already assigned training runs cannot be deleted. Please delete training runs assigned to training instance and try again."));
      // not possible to delete active training instances with associated training runs
    } else if (trainingInstance.getPoolId() != null) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              Long.class,
              trainingInstanceId,
              "First, you must unassign pool id from training instance then try it again."));
      // not possible to delete training instance with associated pool
    }
    Set<TrainingRun> trainingRunsInTrainingInstance =
        trainingRunService.findAllByTrainingInstanceId(trainingInstanceId);
    trainingRunsInTrainingInstance.forEach(
        tr -> trainingRunService.deleteTrainingRun(tr.getId(), true, false));
    trainingInstanceService.delete(trainingInstance);
    cheatingDetectionService.deleteAllCheatingDetectionsOfTrainingInstance(trainingInstanceId);
    trainingEventsService.deleteEventsByTrainingInstanceId(trainingInstance.getId());
  }

  private void deleteBashCommandsByPool(Long poolId) {
    try {
      commandEventsService.deleteCommandsByPool(poolId);
    } catch (MicroserviceApiException ignored) {
    }
  }

  /**
   * Assigns a sandbox pool to a training instance that currently has none. Refuses instances
   * running in a local environment and instances that already have a pool assigned.
   *
   * @param trainingInstanceId the training instance id
   * @param trainingInstanceAssignPoolIdDTO carries the id of the pool to assign
   * @return the updated training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalWO
  public TrainingInstanceBasicInfoDTO assignPoolToTrainingInstance(
      Long trainingInstanceId, TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    if (trainingInstance.isLocalEnvironment()) {
      throw new BadRequestException(
          "The pool cannot be assigned to training instance if the local environment is enabled.");
    }
    if (trainingInstance.getPoolId() != null) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              trainingInstance.getId().getClass(),
              trainingInstance.getId(),
              "Training instance already contains pool Id. Please first unassign pool id and then assign another pool again."));
    }
    // lock pool and update pool
    sandboxApiService.lockPool(
        trainingInstanceAssignPoolIdDTO.getPoolId(), trainingInstance.getAccessToken());
    trainingInstance.setPoolId(trainingInstanceAssignPoolIdDTO.getPoolId());
    TrainingInstance updatedTrainingInstance =
        trainingInstanceService.auditAndSave(trainingInstance);
    return trainingInstanceMapper.mapToBasicDto(updatedTrainingInstance);
  }

  /**
   * Unassigns the sandbox pool currently assigned to a training instance, unlocking the pool and
   * deleting its recorded console commands. Refuses an instance with no pool assigned.
   *
   * @param trainingInstanceId of training instance to be updated
   * @return the updated training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalWO
  public TrainingInstanceBasicInfoDTO unassignPoolInTrainingInstance(Long trainingInstanceId) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    if (trainingInstance.getPoolId() == null) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              trainingInstance.getId().getClass(),
              trainingInstance.getId(),
              "The training instance does not contain any assigned pool already."));
    }
    // unlock previously assigned pool
    sandboxApiService.unlockPool(trainingInstance.getPoolId());
    deleteBashCommandsByPool(trainingInstance.getPoolId());

    trainingInstance.setPoolId(null);
    TrainingInstance updatedTrainingInstance =
        trainingInstanceService.auditAndSave(trainingInstance);
    return trainingInstanceMapper.mapToBasicDto(updatedTrainingInstance);
  }

  /**
   * Finds all Training Runs by specific Training Instance.
   *
   * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
   * @param isActive if isActive attribute is True, only active runs are returned
   * @param pageable pageable parameter with information about pagination.
   * @return Page of {@link TrainingRunDTO} of specific Training Instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(
      Long trainingInstanceId, Boolean isActive, Pageable pageable) {
    Page<TrainingRun> trainingRuns =
        trainingInstanceService.findTrainingRunsByTrainingInstance(
            trainingInstanceId, isActive, pageable);
    Set<Long> runIdsWithWorkingEventLogging =
        checkLogging(trainingRuns, trainingRunService::checkRunEventLogging);
    Set<Long> runIdsWithWorkingCommandLogging =
        checkLogging(trainingRuns, trainingRunService::checkRunCommandLogging);
    PageResultResource<TrainingRunDTO> trainingRunDTOsPageResult =
        trainingRunMapper.mapToPageResultResourceLogging(
            trainingRuns, runIdsWithWorkingEventLogging, runIdsWithWorkingCommandLogging);
    addParticipantsToTrainingRunDTOs(trainingRunDTOsPageResult.getContent());
    return trainingRunDTOsPageResult;
  }

  private Set<Long> checkLogging(Page<TrainingRun> runs, Function<TrainingRun, Boolean> checker) {
    Set<Long> runIdsWithWorkingLogging = new HashSet<>();
    runs.forEach(
        trainingRun -> {
          if (checker.apply(trainingRun)) {
            runIdsWithWorkingLogging.add(trainingRun.getId());
          }
        });

    return runIdsWithWorkingLogging;
  }

  private void addParticipantsToTrainingRunDTOs(List<TrainingRunDTO> trainingRunDTOS) {
    trainingRunDTOS.forEach(
        trainingRunDTO ->
            trainingRunDTO.setParticipantRef(
                userService.getUserRefDTOByUserRefId(
                    trainingRunDTO.getParticipantRef().getUserRefId())));
  }

  /**
   * Reports whether the given training instance has finished, alongside a message describing
   * whether it is safe to delete.
   *
   * @param trainingInstanceId id of the training instance to check
   * @return a DTO carrying the finished flag and its accompanying message
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public TrainingInstanceIsFinishedInfoDTO checkIfInstanceCanBeDeleted(Long trainingInstanceId) {
    TrainingInstanceIsFinishedInfoDTO infoDTO = new TrainingInstanceIsFinishedInfoDTO();
    if (trainingInstanceService.checkIfInstanceIsFinished(trainingInstanceId)) {
      infoDTO.setHasFinished(true);
      infoDTO.setMessage("Training instance has already finished and can be safely deleted.");
    } else {
      infoDTO.setHasFinished(false);
      infoDTO.setMessage(
          "WARNING: Training instance is still running! Are you sure you want to delete it?");
    }
    return infoDTO;
  }

  /**
   * Retrieve all organizers for given training instance .
   *
   * @param trainingInstanceId id of the training instance for which to get the organizers
   * @param pageable pageable parameter with information about pagination.
   * @param givenName optional parameter used for filtration
   * @param familyName optional parameter used for filtration
   * @return returns all organizers in given training instance.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public PageResultResource<UserRefDTO> getOrganizersOfTrainingInstance(
      Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    return userService.getUsersRefDTOByGivenUserIds(
        trainingInstance.getOrganizers().stream().map(UserRef::getUserRefId).toList(),
        pageable,
        givenName,
        familyName);
  }

  /**
   * Retrieve all organizers not in the given training instance.
   *
   * @param trainingInstanceId id of the training instance which users should be excluded from the
   *     result list.
   * @param pageable pageable parameter with information about pagination.
   * @param givenName optional parameter used for filtration
   * @param familyName optional parameter used for filtration
   * @return returns all organizers not in the given training instance.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalRO
  public PageResultResource<UserRefDTO> getOrganizersNotInGivenTrainingInstance(
      Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    Set<Long> excludedOrganizers =
        trainingInstance.getOrganizers().stream()
            .map(UserRef::getUserRefId)
            .collect(Collectors.toSet());
    return userService.getUsersByGivenRoleAndNotWithGivenIds(
        RoleType.ROLE_TRAINING_ORGANIZER, excludedOrganizers, pageable, givenName, familyName);
  }

  /**
   * Adds and removes organizers of the given training instance in one call. The caller's own user
   * reference id is dropped from {@code organizersRemoval} before it is applied, so a caller cannot
   * remove itself as organizer through this method.
   *
   * @param trainingInstanceId if of the training instance to be updated
   * @param organizersAddition ids of the organizers to be added to the training instance
   * @param organizersRemoval ids of the organizers to be removed from the training instance.
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
  @TransactionalWO
  public void editOrganizers(
      Long trainingInstanceId, Set<Long> organizersAddition, Set<Long> organizersRemoval) {
    TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
    Long loggedInUserRefId = securityService.getUserRefIdFromUserAndGroup();
    if (organizersRemoval != null && !organizersRemoval.isEmpty()) {
      organizersRemoval.remove(loggedInUserRefId);
      trainingInstance.removeOrganizersByUserRefIds(organizersRemoval);
    }
    if (organizersAddition != null && !organizersAddition.isEmpty()) {
      addOrganizersToTrainingInstance(trainingInstance, organizersAddition);
    }
    trainingInstanceService.auditAndSave(trainingInstance);
  }

  /**
   * Finds Training Instances by their ids.
   *
   * @param ids the ids of Training Instances to return.
   * @return List of requested {@link TrainingInstanceBasicDTO}.
   */
  @TransactionalRO
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) or "
          + "@securityService.isOrganizerOfGivenTrainingInstances(#ids) or "
          + "@securityService.isParticipantOfGivenTrainingInstances(#ids)")
  public List<TrainingInstanceBasicDTO> findTrainingInstancesByIds(List<Long> ids) {
    return trainingInstanceMapper.mapToBasicDtoList(trainingInstanceService.findAllByIds(ids));
  }

  /**
   * Returns training events for the given instance, filtered by event type and timestamp.
   *
   * <p>Access rules applied per caller role:
   *
   * <ul>
   *   <li><b>Administrator / organizer of the instance:</b> all events returned with full field
   *       set.
   *   <li><b>Every other caller:</b> answer events ({@code CorrectAnswerSubmitted}, {@code
   *       WrongAnswerSubmitted}, {@code AssessmentAnswers}) restricted to their own submissions;
   *       console commands restricted to their own sandbox index; {@code sandbox_id} field replaced
   *       with its SHA-256 hash on every event whose sandbox is not the caller's own run (the
   *       caller's own sandbox identifier stays plain).
   * </ul>
   *
   * <p>All restrictions are enforced at OpenSearch query level — no post-fetch filtering.
   *
   * <p>Console commands live in a pool-scoped index; the pool is resolved from the instance itself,
   * and an instance holding no pool yields no commands.
   *
   * @param instanceId training instance id
   * @param eventType OpenSearch type discriminator string (e.g. {@code "level_started"}); pass
   *     {@code "COMMAND"} for console commands
   * @param sinceTimestampMs epoch milliseconds lower bound (exclusive)
   * @return list of events mapped to {@link AbstractEventDTO}, never null; the {@code sandbox_id}
   *     field holds the plain sandbox UUID for administrators and organizers of the instance; for
   *     other callers only their own run's sandbox UUID is plain and all other sandbox identifiers
   *     are replaced by their SHA-256 hash
   * @throws EntityNotFoundException if {@code instanceId} does not resolve to a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + " or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)"
          + " or @securityService.isParticipantOfGivenTrainingInstances({#instanceId})")
  @TransactionalRO
  public List<AbstractEventDTO> getTrainingInstanceEvents(
      Long instanceId, String eventType, long sinceTimestampMs) {

    Long restrictToUserRefId =
        hasUnrestrictedEventAccess(instanceId)
            ? null
            : securityService.getUserRefIdFromUserAndGroup();

    if (COMMAND_EVENT_TYPE.equals(eventType)) {
      List<TrainingCommand> commands =
          trainingEventAccessService.fetchCommandEventsWithRestrictions(
              instanceId, sinceTimestampMs, restrictToUserRefId);
      return new ArrayList<>(eventMapper.mapToListDTO(commands));
    }

    List<AbstractAuditPOJO> trainingEvents =
        trainingEventAccessService.fetchTrainingEventsWithRestrictions(
            instanceId, eventType, sinceTimestampMs, restrictToUserRefId);
    List<AbstractEventDTO> mappedEvents =
        new ArrayList<>(eventMapper.mapToEventListDTO(trainingEvents));

    if (restrictToUserRefId != null) {
      maskForeignSandboxIds(
          mappedEvents,
          trainingEventAccessService.resolveTraineeSandboxId(instanceId, restrictToUserRefId));
    }
    return mappedEvents;
  }

  /**
   * Replaces the sandbox identifier of every event not belonging to the given sandbox with its
   * SHA-256 hash, leaving that sandbox's own identifier in plain text.
   *
   * @param events events to mask in place
   * @param ownSandboxId sandbox identifier to leave plain; null masks every identifier present
   */
  private void maskForeignSandboxIds(List<AbstractEventDTO> events, String ownSandboxId) {
    events.forEach(
        event -> {
          String eventSandboxId = event.getSandboxId();
          if (eventSandboxId != null && !eventSandboxId.equals(ownSandboxId)) {
            event.setSandboxId(SandboxIdHasher.hash(eventSandboxId));
          }
        });
  }

  private boolean hasUnrestrictedEventAccess(Long instanceId) {
    return securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)
        || securityService.isOrganizerOfGivenTrainingInstance(instanceId);
  }

  private void checkLocalEnvironmentConfiguration(TrainingInstance trainingInstance) {
    if (trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() != null) {
      throw new BadRequestException(
          "The pool cannot be assigned to training instance if the local environment is enabled.");
    }

    if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() == null) {
      throw new BadRequestException("The pool must be set if local environment is disabled.");
    }

    if (!trainingInstance.isLocalEnvironment()
        && trainingInstance.getSandboxDefinitionId() != null) {
      throw new BadRequestException(
          "The sandbox definition cannot be set in the training instance if the local environment is disabled.");
    }
  }
}
