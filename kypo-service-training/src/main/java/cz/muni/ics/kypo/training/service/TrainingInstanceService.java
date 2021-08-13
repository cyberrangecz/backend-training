package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PoolInfoDTO;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.persistence.model.AccessToken;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.AccessTokenRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Training instance service.
 */
@Service
public class TrainingInstanceService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceService.class);

    private TrainingInstanceRepository trainingInstanceRepository;
    private TrainingRunRepository trainingRunRepository;
    private AccessTokenRepository accessTokenRepository;
    private UserRefRepository userRefRepository;
    private SecurityService securityService;
    private WebClient sandboxServiceWebClient;

    /**
     * Instantiates a new Training instance service.
     *
     * @param trainingInstanceRepository the training instance repository
     * @param accessTokenRepository      the access token repository
     * @param trainingRunRepository      the training run repository
     * @param userRefRepository     the organizer ref repository
     * @param sandboxServiceWebClient    the python rest template
     * @param securityService            the security service
     */
    @Autowired

    public TrainingInstanceService(TrainingInstanceRepository trainingInstanceRepository,
                                   AccessTokenRepository accessTokenRepository,
                                   TrainingRunRepository trainingRunRepository,
                                   UserRefRepository userRefRepository,
                                   SecurityService securityService,
                                   @Qualifier("sandboxServiceWebClient") WebClient sandboxServiceWebClient) {
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.userRefRepository = userRefRepository;
        this.securityService = securityService;
        this.sandboxServiceWebClient = sandboxServiceWebClient;
    }

    /**
     * Finds basic info about Training Instance by id
     *
     * @param instanceId of a Training Instance that would be returned
     * @return specific {@link TrainingInstance} by id
     * @throws EntityNotFoundException training instance is not found.
     */
    public TrainingInstance findById(Long instanceId) {
        return trainingInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", instanceId.getClass(), instanceId)));
    }

    /**
     * Find specific Training instance by id including its associated Training definition.
     *
     * @param instanceId the instance id
     * @return the {@link TrainingInstance}
     */
    public TrainingInstance findByIdIncludingDefinition(Long instanceId) {
        return trainingInstanceRepository.findByIdIncludingDefinition(instanceId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", instanceId.getClass(), instanceId)));
    }

    /**
     * Find all Training Instances.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return all {@link TrainingInstance}s
     */
    public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
        return trainingInstanceRepository.findAll(predicate, pageable);
    }

    /**
     * Find all training instances based on the logged in user.
     *
     * @param predicate      the predicate
     * @param pageable       the pageable
     * @param loggedInUserId the logged in user id
     * @return the page
     */
    public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable, Long loggedInUserId) {
        return trainingInstanceRepository.findAll(predicate, pageable, loggedInUserId);
    }

    /**
     * Creates new training instance
     *
     * @param trainingInstance to be created
     * @return created {@link TrainingInstance}
     */
    public TrainingInstance create(TrainingInstance trainingInstance) {
        trainingInstance.setAccessToken(generateAccessToken(trainingInstance.getAccessToken()));
        if (trainingInstance.getStartTime().isAfter(trainingInstance.getEndTime())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "End time must be later than start time."));
        }
        addLoggedInUserAsOrganizerToTrainingInstance(trainingInstance);
        return trainingInstanceRepository.save(trainingInstance);
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
        //add original organizers and poolId to update
        trainingInstanceToUpdate.setOrganizers(new HashSet<>(trainingInstance.getOrganizers()));
        addLoggedInUserAsOrganizerToTrainingInstance(trainingInstanceToUpdate);
        trainingInstanceToUpdate.setPoolId(trainingInstance.getPoolId());
        //check if TI is running, true - only title can be changed, false - any field can be changed
        if (LocalDateTime.now(Clock.systemUTC()).isAfter(trainingInstance.getStartTime())) {
            this.checkChangedFieldsOfRunningTrainingInstance(trainingInstanceToUpdate, trainingInstance);
        } else {
            //check if new access token should be generated, if not original is kept
            if (shouldGenerateNewToken(trainingInstance.getAccessToken(), trainingInstanceToUpdate.getAccessToken())) {
                trainingInstanceToUpdate.setAccessToken(generateAccessToken(trainingInstanceToUpdate.getAccessToken()));
            } else {
                trainingInstanceToUpdate.setAccessToken(trainingInstance.getAccessToken());
            }
        }
        trainingInstanceRepository.save(trainingInstanceToUpdate);
        return trainingInstanceToUpdate.getAccessToken();
    }

    private void validateStartAndEndTime(TrainingInstance trainingInstance) {
        if (trainingInstance.getStartTime().isAfter(trainingInstance.getEndTime())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id",
                    trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "End time must be later than start time."));
        }
    }

    private void checkChangedFieldsOfRunningTrainingInstance(TrainingInstance trainingInstanceToUpdate, TrainingInstance currentTrainingInstance) {
        if (!currentTrainingInstance.getStartTime().equals(trainingInstanceToUpdate.getStartTime())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstanceToUpdate.getId(),
                    "The start time of the running training instance cannot be changed. Only title can be updated."));
        } else if (!currentTrainingInstance.getEndTime().equals(trainingInstanceToUpdate.getEndTime())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstanceToUpdate.getId(),
                    "The end time of the running training instance cannot be changed. Only title can be updated."));
        } else if (!currentTrainingInstance.getAccessToken().equals(trainingInstanceToUpdate.getAccessToken())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstanceToUpdate.getId(),
                    "The access token of the running training instance cannot be changed. Only title can be updated."));
        }
    }

    private boolean shouldGenerateNewToken(String originalToken, String newToken) {
        //new token should not be generated if token in update equals original token or if token in update equals original token without PIN
        String tokenWithoutPin = originalToken.substring(0, originalToken.length() - 5);
        return !(newToken.equals(tokenWithoutPin) || originalToken.equals(newToken));
    }

    private String generateAccessToken(String accessToken) {
        Random rand = new Random();
        String newPass = "";
        boolean generated = false;
        while (!generated) {
            int firstNumber = rand.nextInt(5);
            String pin = firstNumber + RandomStringUtils.random(3, false, true);
            newPass = accessToken + "-" + pin;
            Optional<AccessToken> pW = accessTokenRepository.findOneByAccessToken(newPass);
            if (!pW.isPresent()) {
                generated = true;
            }
        }
        AccessToken newTokenInstance = new AccessToken();
        newTokenInstance.setAccessToken(newPass);
        accessTokenRepository.saveAndFlush(newTokenInstance);
        return newPass;
    }

    private void addLoggedInUserAsOrganizerToTrainingInstance(TrainingInstance trainingInstance) {
        Optional<UserRef> authorOfTrainingInstance = userRefRepository.findUserByUserRefId(securityService.getUserRefIdFromUserAndGroup());
        if (authorOfTrainingInstance.isPresent()) {
            trainingInstance.addOrganizer(authorOfTrainingInstance.get());
        } else {
            UserRef userRef = securityService.createUserRefEntityByInfoFromUserAndGroup();
            trainingInstance.addOrganizer(userRefRepository.save(userRef));
        }
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
     * Update training instance pool training instance.
     *
     * @param trainingInstance the training instance
     * @return the training instance
     */
    public TrainingInstance updateTrainingInstancePool(TrainingInstance trainingInstance) {
        return trainingInstanceRepository.saveAndFlush(trainingInstance);
    }

    /**
     * Lock pool locked pool info.
     *
     * @param poolId the pool id
     * @return the locked pool info
     */
    public LockedPoolInfo lockPool(Long poolId) {
        try {
            return sandboxServiceWebClient
                    .post()
                    .uri("/pools/{poolId}/locks", poolId)
                    .body(Mono.just("{}"), String.class)
                    .retrieve()
                    .bodyToMono(LockedPoolInfo.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Currently, it is not possible to lock and assign pool with (ID: " + poolId + ").", ex);
        }
    }

    /**
     * Unlock pool.
     *
     * @param poolId the pool id
     */
    public void unlockPool(Long poolId) {
        try {
            // get lock id from pool
            PoolInfoDTO poolInfoDto = sandboxServiceWebClient
                    .get()
                    .uri("/pools/{poolId}", poolId)
                    .retrieve()
                    .bodyToMono(PoolInfoDTO.class)
                    .block();
            // unlock pool
            if (poolInfoDto != null && poolInfoDto.getLockId() != null) {
                sandboxServiceWebClient
                        .delete()
                        .uri("/pools/{poolId}/locks/{lockId}", poolId, poolInfoDto.getLockId())
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            }
        } catch (CustomWebClientException ex) {
            if(ex.getStatusCode() != HttpStatus.NOT_FOUND){
                throw new MicroserviceApiException("Currently, it is not possible to unlock a pool with (ID: " + poolId + ").", ex);
            }
        }
    }

    /**
     * Finds all Training Runs of specific Training Instance.
     *
     * @param instanceId id of Training Instance whose Training Runs would be returned.
     * @param isActive   if isActive attribute is True, only active runs are returned
     * @param pageable   pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific {@link TrainingInstance}
     */
    public Page<TrainingRun> findTrainingRunsByTrainingInstance(Long instanceId, Boolean isActive, Pageable pageable) {
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
        return trainingInstanceRepository.isFinished(trainingInstanceId, LocalDateTime.now(Clock.systemUTC()));
    }

    /**
     * Find specific Training instance by its access token and with start time before current time and ending time after current time
     *
     * @param accessToken of Training instance
     * @return Training instance
     */
    public TrainingInstance findByStartTimeAfterAndEndTimeBeforeAndAccessToken(String accessToken) {
        return trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                        "There is no active training session matching access token.")));
    }


    /**
     * Find all IDs of the sandboxes that have been used in training instance.
     *
     * @param trainingInstanceId id of training instance.
     */
    public List<Long> findAllSandboxesUsedByTrainingInstanceId(Long trainingInstanceId) {
        return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId)
                .stream()
                .map(trainingRun -> trainingRun.getSandboxInstanceRefId() == null ? trainingRun.getPreviousSandboxInstanceRefId() : trainingRun.getSandboxInstanceRefId())
                .collect(Collectors.toList());
    }

}
