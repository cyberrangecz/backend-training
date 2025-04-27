package cz.cyberrange.platform.training.service.services;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** The type Training instance service. */
@Service
public class TrainingInstanceService {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceService.class);

  private TrainingInstanceRepository trainingInstanceRepository;
  private TrainingRunRepository trainingRunRepository;
  private UserRefRepository userRefRepository;
  private SecurityService securityService;
  private UserService userService;

  /**
   * Instantiates a new Training instance service.
   *
   * @param trainingInstanceRepository the training instance repository
   * @param trainingRunRepository the training run repository
   * @param userRefRepository the organizer ref repository
   * @param securityService the security service
   */
  @Autowired
  public TrainingInstanceService(
      TrainingInstanceRepository trainingInstanceRepository,
      TrainingRunRepository trainingRunRepository,
      UserRefRepository userRefRepository,
      SecurityService securityService,
      UserService userService) {
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.trainingRunRepository = trainingRunRepository;
    this.userRefRepository = userRefRepository;
    this.securityService = securityService;
    this.userService = userService;
  }

  /**
   * Finds basic info about Training Instance by id
   *
   * @param instanceId of a Training Instance that would be returned
   * @return specific {@link TrainingInstance} by id
   * @throws EntityNotFoundException training instance is not found.
   */
  public TrainingInstance findById(Long instanceId) {
    return trainingInstanceRepository
        .findById(instanceId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        TrainingInstance.class, "id", instanceId.getClass(), instanceId)));
  }

  /**
   * Find specific Training instance by id including its associated Training definition.
   *
   * @param instanceId the instance id
   * @return the {@link TrainingInstance}
   */
  public TrainingInstance findByIdIncludingDefinition(Long instanceId) {
    return trainingInstanceRepository
        .findByIdIncludingDefinition(instanceId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        TrainingInstance.class, "id", instanceId.getClass(), instanceId)));
  }

  /**
   * Find Training instance access token by pool id if exists.
   *
   * @param poolId the pool id
   * @return the access token
   */
  public String findInstanceAccessTokenByPoolId(Long poolId) {
    Optional<TrainingInstance> instance = trainingInstanceRepository.findByPoolId(poolId);
    return instance.map(TrainingInstance::getAccessToken).orElse(null);
  }

  /**
   * Find all Training Instances.
   *
   * @param predicate represents a predicate (boolean-valued function) of one argument.
   * @param pageable pageable parameter with information about pagination.
   * @return all {@link TrainingInstance}s
   */
  public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
    return trainingInstanceRepository.findAll(predicate, pageable);
  }

  /**
   * Find all training instances based on the logged in user.
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @param loggedInUserId the logged in user id
   * @return the page
   */
  public Page<TrainingInstance> findAll(
      Predicate predicate, Pageable pageable, Long loggedInUserId) {
    return trainingInstanceRepository.findAll(predicate, pageable, loggedInUserId);
  }

  /**
   * Find all training instances based on the list of IDs.
   *
   * @param ids the list of IDs
   * @return the page
   */
  public List<TrainingInstance> findAllByIds(List<Long> ids) {
    return trainingInstanceRepository.findAllById(ids);
  }

  /**
   * Creates new training instance
   *
   * @param trainingInstance to be created
   * @return created {@link TrainingInstance}
   */
  public TrainingInstance create(TrainingInstance trainingInstance) {
    trainingInstance.setAccessToken(
        generateAccessToken(trainingInstance.getAccessToken().trim(), trainingInstance.getType()));
    if (trainingInstance.getStartTime().isAfter(trainingInstance.getEndTime())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              trainingInstance.getId().getClass(),
              trainingInstance.getId(),
              "End time must be later than start time."));
    }
    addLoggedInUserAsOrganizerToTrainingInstance(trainingInstance);
    return auditAndSave(trainingInstance);
  }

  /**
   * updates training instance
   *
   * @param trainingInstanceToUpdate to be updated
   * @return new access token if it was changed
   * @throws EntityNotFoundException training instance is not found.
   * @throws EntityConflictException cannot be updated for some reason.
   */
  public String update(TrainingInstance trainingInstanceToUpdate) {
    validateStartAndEndTime(trainingInstanceToUpdate);
    TrainingInstance trainingInstance = findById(trainingInstanceToUpdate.getId());
    checkNotRevivingAnExpiredInstance(trainingInstanceToUpdate, trainingInstance);
    // add original organizers to update
    trainingInstanceToUpdate.setOrganizers(new HashSet<>(trainingInstance.getOrganizers()));
    addLoggedInUserAsOrganizerToTrainingInstance(trainingInstanceToUpdate);
    trainingInstanceToUpdate.setTrainingInstanceLobby(trainingInstance.getTrainingInstanceLobby());
    trainingInstanceToUpdate.setMaxTeamSize(trainingInstance.getMaxTeamSize());
    trainingInstanceToUpdate.setType(trainingInstance.getType());
    // check if TI is running, true - only title can be changed, false - any field can be changed
    if (trainingInstance.notStarted()) {
      // check if access token has changed and new should be generated, if not original is kept
      if (isAccessTokenChanged(
          trainingInstance.getAccessToken(), trainingInstanceToUpdate.getAccessToken())) {
        trainingInstanceToUpdate.setAccessToken(
            generateAccessToken(
                trainingInstanceToUpdate.getAccessToken(), trainingInstance.getType()));
      } else {
        trainingInstanceToUpdate.setAccessToken(trainingInstance.getAccessToken());
      }
    } else {
      this.checkChangedFieldsOfTrainingInstance(trainingInstanceToUpdate, trainingInstance);
      trainingInstanceToUpdate.setAccessToken(trainingInstance.getAccessToken());
    }
    return auditAndSave(trainingInstanceToUpdate).getAccessToken();
  }

  private void validateStartAndEndTime(TrainingInstance trainingInstance) {
    if (trainingInstance.getStartTime().isAfter(trainingInstance.getEndTime())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              trainingInstance.getId().getClass(),
              trainingInstance.getId(),
              "End time must be later than start time."));
    }
  }

  private void checkNotRevivingAnExpiredInstance(
      TrainingInstance trainingInstanceToUpdate, TrainingInstance currentTrainingInstance) {
    if (currentTrainingInstance.finished() && !trainingInstanceToUpdate.finished()) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              trainingInstanceToUpdate.getId().getClass(),
              trainingInstanceToUpdate.getId(),
              "End time of an expired instance cannot be set to the future."));
    }
  }

  private void checkChangedFieldsOfTrainingInstance(
      TrainingInstance trainingInstanceToUpdate, TrainingInstance currentTrainingInstance) {
    if (!currentTrainingInstance.getStartTime().equals(trainingInstanceToUpdate.getStartTime())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              Long.class,
              trainingInstanceToUpdate.getId(),
              "The start time of the running or finished training instance cannot be changed. Only title and end time can be updated."));
    } else if (isAccessTokenChanged(
        currentTrainingInstance.getAccessToken(), trainingInstanceToUpdate.getAccessToken())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              Long.class,
              trainingInstanceToUpdate.getId(),
              "The access token of the running or finished training instance cannot be changed. Only title and end time can be updated."));
    } else if (!Objects.equals(
        currentTrainingInstance.getPoolId(), trainingInstanceToUpdate.getPoolId())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingInstance.class,
              "id",
              Long.class,
              currentTrainingInstance.getId(),
              "The pool in the running or finished instance cannot be changed. Only title and end time can be updated."));
    }
  }

  private boolean isAccessTokenChanged(String originalToken, String newToken) {
    if (newToken == null || originalToken == null || newToken.isBlank()) {
      return false;
    }
    // new token should not be generated if token in update equals original token without PIN
    String originalTokenWithoutPin = originalToken.split("-")[0];
    return !newToken.equals(originalTokenWithoutPin);
  }

  private String generateAccessToken(String accessToken, TrainingType trainingType) {
    Random rand = new Random();
    String newPass;
    do {
      int firstNumber = rand.nextInt(5);
      String pin = firstNumber + RandomStringUtils.random(2, false, true);
      newPass = accessToken + "-" + pin + (trainingType == TrainingType.COOP ? "C" : "L");
    } while (trainingInstanceRepository.existsForToken(newPass));
    return newPass;
  }

  private void addLoggedInUserAsOrganizerToTrainingInstance(TrainingInstance trainingInstance) {
    UserRef userRef = userRefRepository.createOrGet(securityService.getUserRefIdFromUserAndGroup());
    trainingInstance.addOrganizer(userRef);
  }

  /**
   * deletes training instance
   *
   * @param trainingInstance the training instance to be deleted.
   * @throws EntityNotFoundException training instance is not found.
   * @throws EntityConflictException cannot be deleted for some reason.
   */
  public void delete(TrainingInstance trainingInstance) {
    trainingInstanceRepository.delete(trainingInstance);
    LOG.debug("Training instance with id: {} deleted.", trainingInstance.getId());
  }

  /**
   * deletes training instance
   *
   * @param id the training instance to be deleted.
   * @throws EntityNotFoundException training instance is not found.
   * @throws EntityConflictException cannot be deleted for some reason.
   */
  public void deleteById(Long id) {
    trainingInstanceRepository.deleteById(id);
    LOG.debug("Training instance with id: {} deleted.", id);
  }

  /**
   * Finds all Training Runs of specific Training Instance.
   *
   * @param instanceId id of Training Instance whose Training Runs would be returned.
   * @param isActive if isActive attribute is True, only active runs are returned
   * @param pageable pageable parameter with information about pagination.
   * @return {@link TrainingRun}s of specific {@link TrainingInstance}
   */
  public Page<TrainingRun> findTrainingRunsByTrainingInstance(
      Long instanceId, Boolean isActive, Pageable pageable) {
    // check if instance exists
    this.findById(instanceId);
    if (isActive == null) {
      return trainingRunRepository.findAllByTrainingInstanceId(instanceId, pageable);
    } else if (isActive) {
      return trainingRunRepository.findAllActiveByTrainingInstanceId(instanceId, pageable);
    } else {
      return trainingRunRepository.findAllInactiveByTrainingInstanceId(instanceId, pageable);
    }
  }

  /**
   * Finds all finished Training Runs of specific Training Instance.
   *
   * @param instanceId id of Training Instance whose Training Runs would be returned.
   * @param pageable pageable parameter with information about pagination.
   * @return {@link TrainingRun}s of specific {@link TrainingInstance}
   */
  public Page<TrainingRun> findFinishedTrainingRunsByTrainingInstance(
      Long instanceId, Pageable pageable) {
    // check if instance exists
    this.findById(instanceId);
    return trainingRunRepository.findAllFinishedByTrainingInstanceId(instanceId, pageable);
  }

  /**
   * Find UserRefs by userRefId
   *
   * @param usersRefId of wanted UserRefs
   * @return {@link UserRef}s with corresponding userRefIds
   */
  public Set<UserRef> findUserRefsByUserRefIds(Set<Long> usersRefId) {
    return userRefRepository.findUsers(usersRefId);
  }

  /**
   * Check if instance is finished.
   *
   * @param trainingInstanceId the training instance id
   * @return true if instance is finished, false if not
   */
  public boolean checkIfInstanceIsFinished(Long trainingInstanceId) {
    return trainingInstanceRepository.isFinished(
        trainingInstanceId, LocalDateTime.now(Clock.systemUTC()));
  }

  /**
   * Find specific Training instance by its access token and with start time before current time and
   * ending time after current time
   *
   * @param accessToken of Training instance
   * @return Training instance
   */
  public TrainingInstance findByEndTimeBeforeAndAccessToken(String accessToken) {
    return trainingInstanceRepository
        .findByEndTimeAfterAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        TrainingInstance.class,
                        "accessToken",
                        accessToken.getClass(),
                        accessToken,
                        "There is no active training session matching access token.")));
  }

  /**
   * Find all IDs of the sandboxes that have been used in training instance.
   *
   * @param trainingInstanceId id of training instance.
   */
  public List<String> findAllSandboxesUsedByTrainingInstanceId(Long trainingInstanceId) {
    return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId).stream()
        .map(
            trainingRun ->
                trainingRun.getSandboxInstanceRefId() == null
                    ? trainingRun.getPreviousSandboxInstanceRefId()
                    : trainingRun.getSandboxInstanceRefId())
        .collect(Collectors.toList());
  }

  /**
   * Find all IDs of the trainees that have been participated in training instance.
   *
   * @param trainingInstanceId id of training instance.
   */
  public List<Long> findAllTraineesByTrainingInstance(Long trainingInstanceId) {
    return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId).stream()
        .map(trainingRun -> trainingRun.getParticipantRef().getUserRefId())
        .collect(Collectors.toList());
  }

  /**
   * Sets audit attributes to training instance and save.
   *
   * @param trainingInstance the training instance to be saved.
   */
  public TrainingInstance auditAndSave(TrainingInstance trainingInstance) {
    trainingInstance.setLastEdited(getCurrentTimeInUTC());
    trainingInstance.setLastEditedBy(userService.getUserRefFromUserAndGroup().getUserRefFullName());
    return trainingInstanceRepository.save(trainingInstance);
  }

  private LocalDateTime getCurrentTimeInUTC() {
    return LocalDateTime.now(Clock.systemUTC());
  }

  public List<TrainingInstance> findAllRunningInstances() {
    return trainingInstanceRepository.findAllByStartTimeBeforeAndEndTimeAfter(
        LocalDateTime.now(Clock.systemUTC()), LocalDateTime.now(Clock.systemUTC()));
  }
}
