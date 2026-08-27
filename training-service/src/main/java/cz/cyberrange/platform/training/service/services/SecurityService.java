package cz.cyberrange.platform.training.service.services;

import com.google.common.collect.Sets;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.exceptions.CustomWebClientException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.detection.AbstractDetectionEvent;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.cyberrange.platform.training.persistence.repository.detection.CheatingDetectionRepository;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Resolves the identity of the logged in user and evaluates the authorization checks referenced
 * from {@code @PreAuthorize} expressions across the facades, comparing the cross-service {@code
 * userRefId} rather than any local primary key. Every method runs in its own new read-only
 * transaction, independent of the caller's transaction.
 */
@Service
@TransactionalRO(propagation = Propagation.REQUIRES_NEW)
public class SecurityService {

  private final TrainingRunRepository trainingRunRepository;
  private final TrainingDefinitionRepository trainingDefinitionRepository;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final WebClient userManagementWebClient;
  private final UserService userService;
  private final AbstractLevelRepository abstractLevelRepository;
  private final TrainingLevelRepository trainingLevelRepository;
  private final HintRepository hintRepository;
  private final AbstractDetectionEventRepository abstractDetectionEventRepository;
  private final CheatingDetectionRepository cheatingDetectionRepository;

  @Autowired
  public SecurityService(
      TrainingInstanceRepository trainingInstanceRepository,
      TrainingDefinitionRepository trainingDefinitionRepository,
      TrainingRunRepository trainingRunRepository,
      @Qualifier("userManagementServiceWebClient") WebClient userManagementWebClient,
      UserService userService,
      AbstractLevelRepository abstractLevelRepository,
      TrainingLevelRepository trainingLevelRepository,
      HintRepository hintRepository,
      AbstractDetectionEventRepository abstractDetectionEventRepository,
      CheatingDetectionRepository cheatingDetectionRepository) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.userManagementWebClient = userManagementWebClient;
    this.userService = userService;
    this.abstractLevelRepository = abstractLevelRepository;
    this.trainingLevelRepository = trainingLevelRepository;
    this.hintRepository = hintRepository;
    this.abstractDetectionEventRepository = abstractDetectionEventRepository;
    this.cheatingDetectionRepository = cheatingDetectionRepository;
  }

  /**
   * Decides whether the logged in user is the participant of the given training run, comparing
   * cross-service {@code userRefId}s rather than local primary keys.
   *
   * @param trainingRunId the training run id
   * @return true when the logged in user is the run's participant
   * @throws EntityNotFoundException when no training run with the given id exists
   */
  public boolean isTraineeOfGivenTrainingRun(Long trainingRunId) {
    TrainingRun trainingRun =
        trainingRunRepository
            .findById(trainingRunId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            TrainingRun.class,
                            "id",
                            trainingRunId.getClass(),
                            trainingRunId,
                            "The necessary permissions are required for a resource.")));
    return trainingRun.getParticipantRef().getUserRefId().equals(getUserRefIdFromUserAndGroup());
  }

  /**
   * Decides whether the logged in user is one of the organizers of the given training instance.
   *
   * @param instanceId the instance id
   * @return true when the logged in user is among its organizers
   * @throws EntityNotFoundException when no training instance with the given id exists
   */
  public boolean isOrganizerOfGivenTrainingInstance(Long instanceId) {
    TrainingInstance trainingInstance =
        trainingInstanceRepository
            .findById(instanceId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            TrainingInstance.class,
                            "id",
                            instanceId.getClass(),
                            instanceId,
                            "The necessary permissions are required for a resource.")));
    return isOrganizerOfGivenInstance(trainingInstance);
  }

  /**
   * Decides whether the logged in user is one of the organizers of the training instance the
   * given training run belongs to.
   *
   * @param trainingRunId the run id
   * @return true when the logged in user is among the instance's organizers
   * @throws EntityNotFoundException when no training run with the given id exists
   */
  public boolean isOrganizerOfGivenTrainingRun(Long trainingRunId) {
    TrainingRun trainingRun =
        trainingRunRepository
            .findById(trainingRunId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            TrainingRun.class,
                            "id",
                            trainingRunId.getClass(),
                            trainingRunId,
                            "The necessary permissions are required for a resource.")));
    return trainingRun.getTrainingInstance().getOrganizers().stream()
        .anyMatch(o -> o.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
  }

  /**
   * Decides whether the logged in user organizes the training instance the given detection event
   * was raised in.
   *
   * @param eventId the detection event id
   * @return true when the logged in user is among that instance's organizers
   * @throws EntityNotFoundException when the detection event with the given id does not exist
   */
  public boolean isOrganizerOfGivenDetectionEvent(Long eventId) {
    AbstractDetectionEvent detectionEvent =
        abstractDetectionEventRepository
            .findById(eventId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            AbstractDetectionEvent.class,
                            "id",
                            eventId.getClass(),
                            eventId,
                            "The necessary permissions are required for a resource.")));
    return isOrganizerOfGivenTrainingInstance(detectionEvent.getTrainingInstanceId());
  }

  /**
   * Decides whether the logged in user organizes the training instance the given cheating
   * detection was raised in.
   *
   * @param cheatingDetectionId the cheating detection id
   * @return true when the logged in user is among that instance's organizers
   * @throws EntityNotFoundException when the cheating detection with the given id does not exist
   */
  public boolean isOrganizerOfGivenCheatingDetection(Long cheatingDetectionId) {
    CheatingDetection cheatingDetection =
        cheatingDetectionRepository
            .findById(cheatingDetectionId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            CheatingDetection.class,
                            "id",
                            cheatingDetectionId.getClass(),
                            cheatingDetectionId,
                            "The necessary permissions are required for a resource.")));
    return isOrganizerOfGivenTrainingInstance(cheatingDetection.getTrainingInstanceId());
  }

  /**
   * Decides whether the logged in user authors the given training definition.
   *
   * @param definitionId id of the training definition whose authors are examined
   * @return true when the logged in user is one of the definition's authors
   * @throws EntityNotFoundException when no training definition with the given id exists
   */
  public boolean isDesignerOfGivenTrainingDefinition(Long definitionId) {
    TrainingDefinition trainingDefinition =
        trainingDefinitionRepository
            .findById(definitionId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        new EntityErrorDetail(
                            TrainingDefinition.class,
                            "id",
                            definitionId.getClass(),
                            definitionId,
                            "The necessary permissions are required for a resource.")));
    return trainingDefinition.getAuthors().stream()
        .anyMatch(a -> a.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
  }

  /**
   * Decides whether the logged in user organizes any training instance created from the given
   * training definition.
   *
   * @param definitionId id of the training definition whose instances are examined
   * @return true when the logged in user organizes at least one of those instances, false also when
   *     the definition has no instance at all
   */
  public boolean isOrganizerForGivenTrainingDefinition(Long definitionId) {
    List<TrainingInstance> instances =
        trainingInstanceRepository.findAllByTrainingDefinitionId(definitionId);
    for (TrainingInstance trainingInstance : instances) {
      if (isOrganizerOfGivenInstance(trainingInstance)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Decides whether the logged in user is one of the organizers of the given training instance.
   *
   * @param trainingInstance the training instance whose organizers are examined
   * @return true when the logged in user is among them
   */
  private boolean isOrganizerOfGivenInstance(TrainingInstance trainingInstance) {
    return trainingInstance.getOrganizers().stream()
        .anyMatch(o -> o.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
  }

  /**
   * Decides whether the logged in user organizes every one of the given training instances.
   *
   * @param instanceIds the instance ids
   * @return true when every id resolves to an instance and the logged in user organizes each of
   *     them; false if any id does not resolve to an instance; true also for an empty list
   */
  public boolean isOrganizerOfGivenTrainingInstances(List<Long> instanceIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();
    List<TrainingInstance> instances = trainingInstanceRepository.findAllByIdIn(instanceIds);
    if (instances.size() != instanceIds.size()) {
      return false;
    }
    return instances.stream()
        .allMatch(
            instance ->
                instance.getOrganizers().stream()
                    .anyMatch(o -> o.getUserRefId().equals(userRefId)));
  }

  /**
   * Decides whether the logged in user has a training run in every one of the given training
   * instances.
   *
   * @param instanceIds the instance ids
   * @return true when the logged in user has a run in each of them, true also for an empty list
   */
  public boolean isParticipantOfGivenTrainingInstances(List<Long> instanceIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();
    List<TrainingRun> runs = trainingRunRepository.findAllByParticipantRefId(userRefId);
    Set<Long> participatedInstanceIds =
        runs.stream().map(run -> run.getTrainingInstance().getId()).collect(Collectors.toSet());
    return participatedInstanceIds.containsAll(instanceIds);
  }

  /**
   * Decides whether the logged in user is the participant of every one of the given training
   * runs.
   *
   * @param trainingRunIds the training run ids
   * @return true when every id resolves to a run the logged in user participates in; true also
   *     for an empty list
   */
  public boolean isTraineeOfGivenTrainingRuns(List<Long> trainingRunIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();
    List<TrainingRun> runs =
        trainingRunRepository.findAllByIdInAndParticipantRefId(trainingRunIds, userRefId);
    return runs.size() == trainingRunIds.size();
  }

  /**
   * Decides whether the logged in user organizes the training instance of every one of the given
   * training runs.
   *
   * @param trainingRunIds the training run ids
   * @return true when every id resolves to a run whose instance the logged in user organizes;
   *     true also for an empty list
   */
  public boolean isOrganizerOfGivenTrainingRuns(List<Long> trainingRunIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();
    List<TrainingRun> runs =
        trainingRunRepository.findAllByIdInAndOrganizedByUser(trainingRunIds, userRefId);
    return runs.size() == trainingRunIds.size();
  }

  /**
   * Decides whether the authentication token of the current request grants the given role.
   *
   * @param roleTypeSecurity the role to look for among the granted authorities
   * @return true when the token carries that role
   */
  public boolean hasRole(RoleTypeSecurity roleTypeSecurity) {
    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    for (GrantedAuthority gA : authentication.getAuthorities()) {
      if (gA.getAuthority().equals(roleTypeSecurity.name())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Asks the user-and-group service who the logged in user is and reports the identifier that user
   * carries across service boundaries, which is not the primary key of the local {@link
   * cz.cyberrange.platform.training.persistence.model.UserRef} row.
   *
   * @return the cross-service user reference id of the logged in user
   * @throws MicroserviceApiException when the call to the user-and-group service fails
   */
  public Long getUserRefIdFromUserAndGroup() {
    try {
      UserRefDTO userRefDTO =
          userManagementWebClient
              .get()
              .uri("/users/info")
              .retrieve()
              .bodyToMono(UserRefDTO.class)
              .block();
      return userRefDTO.getUserRefId();
    } catch (CustomWebClientException ex) {
      throw new MicroserviceApiException(
          "Error when calling user management service API to get info about logged in user.", ex);
    }
  }

  /**
   * Reads the raw bearer token carried by the authentication token of the current request.
   *
   * @return the token value
   */
  public String getBearerToken() {
    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    return authentication.getToken().getTokenValue();
  }

  /**
   * Decides whether every one of the given users has a training run in an instance the logged in
   * user either has a training run in or organizes. A given user's own instances are collected
   * from their training runs alone; organizing is checked only for the logged in user, not for
   * the given users.
   *
   * @param userIds cross-service user reference ids of the users to check against the logged in
   *     user
   * @return true when every given user has such an overlapping instance; true also for an empty
   *     list
   */
  public boolean sharesCommonTrainingInstance(List<Long> userIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();
    UserRef userRef = userService.getUserByUserRefId(userRefId);
    Set<Long> participatesIn =
        trainingRunRepository.findAllByParticipantRefId(userRefId).stream()
            .map(run -> run.getTrainingInstance().getId())
            .collect(Collectors.toSet());

    Set<Long> organizes =
        trainingInstanceRepository.findAllByOrganizersContains(userRef).stream()
            .map(TrainingInstance::getId)
            .collect(Collectors.toSet());

    Set<Long> sourceUserInstanceIds = Sets.union(participatesIn, organizes);

    List<Long> participantRefIds =
        userIds.stream()
            .map(id -> userService.getUserByUserRefId(id).getId())
            .collect(Collectors.toList());
    List<TrainingRun> runsByUserIds =
        trainingRunRepository.findAllByParticipantRefIdIn(participantRefIds);
    List<Set<Long>> userInstanceIds =
        userIds.stream()
            .map(
                id ->
                    runsByUserIds.stream()
                        .filter(run -> run.getParticipantRef().getUserRefId().equals(id))
                        .map(run -> run.getTrainingInstance().getId())
                        .collect(Collectors.toSet()))
            .collect(Collectors.toList());

    return userInstanceIds.stream()
        .allMatch(instanceIds -> !Sets.intersection(sourceUserInstanceIds, instanceIds).isEmpty());
  }

  /**
   * Decides whether the logged in user is involved in every one of the given training definitions,
   * either by having a training run in one of their instances or by organizing one of their
   * instances.
   *
   * @param trainingDefinitionIds ids of the training definitions that all have to be covered
   * @return true when the user is involved in all of them, true as well for an empty list
   */
  public boolean participatesInTrainingDefinitions(List<Long> trainingDefinitionIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();

    return getParticipatedDefinitions(userRefId).containsAll(trainingDefinitionIds);
  }

  /**
   * Decides whether the logged in user is involved in the training definitions the given levels
   * belong to, involvement meaning a training run in one of their instances or organizing one of
   * their instances.
   *
   * @param levelIds ids of the levels whose definitions all have to be covered
   * @return true when the user is involved in every definition those levels belong to, a level id
   *     matching no level imposing no requirement
   */
  public boolean participatesInLevels(List<Long> levelIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();

    Set<Long> requestedDefinitionIds =
        abstractLevelRepository.findAllByIdIn(levelIds).stream()
            .map(level -> level.getTrainingDefinition().getId())
            .collect(Collectors.toSet());

    return getParticipatedDefinitions(userRefId).containsAll(requestedDefinitionIds);
  }

  /**
   * Decides whether every one of the given hints belongs to a training level of a training
   * definition the logged in user is involved in, involvement meaning a training run in one of its
   * instances or organizing one of its instances.
   *
   * @param hintIds ids of the hints that all have to be covered
   * @return true when all of them are reachable that way
   */
  public boolean participatesInLevelsWithHints(List<Long> hintIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();

    Set<Long> participatedTrainingDefinitions = getParticipatedDefinitions(userRefId);

    Set<Long> availableHintIds =
        trainingLevelRepository
            .findAllByTrainingDefinitionIdIn(participatedTrainingDefinitions)
            .stream()
            .flatMap(level -> level.getHints().stream().map(Hint::getId))
            .collect(Collectors.toSet());

    return availableHintIds.containsAll(hintIds);
  }

  /**
   * Collects the training definitions the given user is involved in, both those whose instances the
   * user has a training run in and those whose instances the user organizes.
   *
   * @param userRefId the cross-service user reference id of the user in question
   * @return ids of the training definitions reached either way, empty when there are none
   */
  private Set<Long> getParticipatedDefinitions(Long userRefId) {
    UserRef userRef = userService.getUserByUserRefId(userRefId);
    Set<Long> participatedDefinitions =
        trainingRunRepository.findAllByParticipantRefId(userRefId).stream()
            .map(run -> run.getTrainingInstance().getTrainingDefinition().getId())
            .collect(Collectors.toSet());

    participatedDefinitions.addAll(
        trainingInstanceRepository.findAllByOrganizersContains(userRef).stream()
            .map(instance -> instance.getTrainingDefinition().getId())
            .collect(Collectors.toSet()));
    return participatedDefinitions;
  }
}
