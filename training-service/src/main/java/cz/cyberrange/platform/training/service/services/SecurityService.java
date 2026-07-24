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
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
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

/** The type Security service. */
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

  /**
   * Instantiates a new Security service.
   *
   * @param trainingInstanceRepository the training instance repository
   * @param trainingDefinitionRepository the training definition repository
   * @param trainingRunRepository the training run repository
   * @param userManagementWebClient the java rest template
   */
  @Autowired
  public SecurityService(
      TrainingInstanceRepository trainingInstanceRepository,
      TrainingDefinitionRepository trainingDefinitionRepository,
      TrainingRunRepository trainingRunRepository,
      @Qualifier("userManagementServiceWebClient") WebClient userManagementWebClient,
      UserService userService,
      AbstractLevelRepository abstractLevelRepository,
      TrainingLevelRepository trainingLevelRepository,
      HintRepository hintRepository) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.userManagementWebClient = userManagementWebClient;
    this.userService = userService;
    this.abstractLevelRepository = abstractLevelRepository;
    this.trainingLevelRepository = trainingLevelRepository;
    this.hintRepository = hintRepository;
  }

  /**
   * Is trainee of given training run boolean.
   *
   * @param trainingRunId the training run id
   * @return the boolean
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
   * Is organizer of given training instance boolean.
   *
   * @param instanceId the instance id
   * @return the boolean
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
   * Is organizer of given training run.
   *
   * @param trainingRunId the run id
   * @return the boolean
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
   * Is designer of given training definition boolean.
   *
   * @param definitionId the definition id
   * @return the boolean
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
   * Is organizer of one of the training instances from the given training definition
   *
   * @param definitionId the definition id
   * @return the boolean
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

  private boolean isOrganizerOfGivenInstance(TrainingInstance trainingInstance) {
    return trainingInstance.getOrganizers().stream()
        .anyMatch(o -> o.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
  }

  /**
   * Is organizer of all given training instances.
   *
   * @param instanceIds the instance ids
   * @return true if the current user is an organizer of all given training instances
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
   * Is participant of all given training instances.
   *
   * @param instanceIds the instance ids
   * @return true if the current user is a participant of all given training instances
   */
  public boolean isParticipantOfGivenTrainingInstances(List<Long> instanceIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();
    List<TrainingRun> runs = trainingRunRepository.findAllByParticipantRefId(userRefId);
    Set<Long> participatedInstanceIds =
        runs.stream().map(run -> run.getTrainingInstance().getId()).collect(Collectors.toSet());
    return participatedInstanceIds.containsAll(instanceIds);
  }

  /**
   * Is trainee of all given training runs.
   *
   * @param trainingRunIds the training run ids
   * @return true if the current user is the participant of all given training runs
   */
  public boolean isTraineeOfGivenTrainingRuns(List<Long> trainingRunIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();
    List<TrainingRun> runs =
        trainingRunRepository.findAllByIdInAndParticipantRefId(trainingRunIds, userRefId);
    return runs.size() == trainingRunIds.size();
  }

  /**
   * Is organizer of all given training runs.
   *
   * @param trainingRunIds the training run ids
   * @return true if the current user is an organizer of the instance for all given training runs
   */
  public boolean isOrganizerOfGivenTrainingRuns(List<Long> trainingRunIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();
    List<TrainingRun> runs =
        trainingRunRepository.findAllByIdInAndOrganizedByUser(trainingRunIds, userRefId);
    return runs.size() == trainingRunIds.size();
  }

  /**
   * Has role boolean.
   *
   * @param roleTypeSecurity the role type security
   * @return the boolean
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
   * Gets user ref id from user and group.
   *
   * @return the user ref id from user and group
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

  public String getBearerToken() {
    JwtAuthenticationToken authentication =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    return authentication.getToken().getTokenValue();
  }

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

    List<TrainingRun> runsByUserIds = trainingRunRepository.findAllByParticipantRefIdIn(userIds);
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

  public boolean participatesInTrainingDefinitions(List<Long> trainingDefinitionIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();

    return getParticipatedDefinitions(userRefId).containsAll(trainingDefinitionIds);
  }

  public boolean participatesInLevels(List<Long> levelIds) {
    Long userRefId = getUserRefIdFromUserAndGroup();

    Set<Long> requestedDefinitionIds =
        abstractLevelRepository.findAllByIdIn(levelIds).stream()
            .map(level -> level.getTrainingDefinition().getId())
            .collect(Collectors.toSet());

    return getParticipatedDefinitions(userRefId).containsAll(requestedDefinitionIds);
  }

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
