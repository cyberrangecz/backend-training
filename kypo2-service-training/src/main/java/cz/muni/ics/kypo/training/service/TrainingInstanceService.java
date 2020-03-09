package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.csirt.kypo.elasticsearch.service.exceptions.ElasticsearchTrainingServiceLayerException;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PoolInfoDto;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.exceptions.errors.PythonApiError;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TrainingInstanceService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceService.class);
    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;

    private TrainingInstanceRepository trainingInstanceRepository;
    private TrainingRunRepository trainingRunRepository;
    private AccessTokenRepository accessTokenRepository;
    private UserRefRepository organizerRefRepository;
    private SecurityService securityService;
    private TrainingEventsService trainingEventsService;
    private TRAcquisitionLockRepository trAcquisitionLockRepository;
    @Qualifier("pythonRestTemplate")
    private RestTemplate pythonRestTemplate;
    private static final int PYTHON_RESULT_PAGE_SIZE = 1000;

    @Autowired
    public TrainingInstanceService(TrainingInstanceRepository trainingInstanceRepository, AccessTokenRepository accessTokenRepository,
                                   TrainingRunRepository trainingRunRepository, UserRefRepository organizerRefRepository,
                                   RestTemplate pythonRestTemplate, SecurityService securityService, TrainingEventsService trainingEventsService,
                                   TRAcquisitionLockRepository trAcquisitionLockRepository) {
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.organizerRefRepository = organizerRefRepository;
        this.securityService = securityService;
        this.trainingEventsService = trainingEventsService;
        this.pythonRestTemplate = pythonRestTemplate;
        this.trAcquisitionLockRepository = trAcquisitionLockRepository;
    }

    /**
     * Finds specific Training Instance by id
     *
     * @param instanceId of a Training Instance that would be returned
     * @return specific {@link TrainingInstance} by id
     * @throws EntityNotFoundException training instance is not found.
     */
    public TrainingInstance findById(Long instanceId) {
        return trainingInstanceRepository.findById(instanceId).orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(
                TrainingInstance.class, "id", instanceId.getClass(), instanceId, "Training instance not found.")));
    }

    /**
     * Find specific Training instance by id including its associated Training definition.
     *
     * @param instanceId the instance id
     * @return the {@link TrainingInstance}
     */
    public TrainingInstance findByIdIncludingDefinition(Long instanceId) {
        return trainingInstanceRepository.findByIdIncludingDefinition(instanceId).orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(
                TrainingInstance.class, "id", instanceId.getClass(), instanceId, "Training instance not found.")));
    }

    /**
     * Find all Training Instances.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable pageable parameter with information about pagination.
     * @return all {@link TrainingInstance}s
     */
    public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
        if (securityService.isAdmin()) {
            return trainingInstanceRepository.findAll(predicate, pageable);
        }
        Predicate loggedInUser = QTrainingInstance.trainingInstance.organizers.any().userRefId.eq(securityService.getUserRefIdFromUserAndGroup()).and(predicate);
        return trainingInstanceRepository.findAll(loggedInUser, pageable);
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
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(trainingInstanceToUpdate.getId())
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id",
                        trainingInstanceToUpdate.getId().getClass(), trainingInstanceToUpdate.getId(), "Training instance not found.")));
        if (trainingInstanceToUpdate.getStartTime().isAfter(trainingInstanceToUpdate.getEndTime())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id",
                    trainingInstanceToUpdate.getId().getClass(), trainingInstanceToUpdate.getId(), "End time must be later than start time."));
        }
        String shortPass = trainingInstance.getAccessToken().substring(0, trainingInstance.getAccessToken().length() - 5);
        if (trainingInstanceToUpdate.getAccessToken().equals(shortPass) || trainingInstanceToUpdate.getAccessToken().equals(trainingInstance.getAccessToken())) {
            trainingInstanceToUpdate.setAccessToken(trainingInstance.getAccessToken());
        } else {
            trainingInstanceToUpdate.setAccessToken(generateAccessToken(trainingInstanceToUpdate.getAccessToken()));
        }
        trainingInstanceToUpdate.setOrganizers(new HashSet<>(trainingInstance.getOrganizers()));
        addLoggedInUserAsOrganizerToTrainingInstance(trainingInstanceToUpdate);
        trainingInstanceToUpdate.setPoolId(trainingInstance.getPoolId());
        trainingInstanceRepository.save(trainingInstanceToUpdate);
        return trainingInstanceToUpdate.getAccessToken();
    }

    private void addLoggedInUserAsOrganizerToTrainingInstance(TrainingInstance trainingInstance) {
        Optional<UserRef> authorOfTrainingInstance = organizerRefRepository.findUserByUserRefId(securityService.getUserRefIdFromUserAndGroup());
        if (authorOfTrainingInstance.isPresent()) {
            trainingInstance.addOrganizer(authorOfTrainingInstance.get());
        } else {
            UserRef userRef = securityService.createUserRefEntityByInfoFromUserAndGroup();
            trainingInstance.addOrganizer(organizerRefRepository.save(userRef));
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
        if (trainingRunRepository.existsAnyForTrainingInstance(trainingInstance.getId())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "Training instance with already assigned training runs cannot be deleted. Please delete training runs assigned to training instance" +
                    " and try again or contact administrator."));
        }
        trainingInstanceRepository.delete(trainingInstance);
        try {
            trainingEventsService.deleteEventsByTrainingInstanceId(trainingInstance.getId());
        } catch (ElasticsearchTrainingServiceLayerException io) {
            LOG.error("Could not delete documents of this training instance from Elasticsearch. Please contact administrator to check if Elasticsearch is running.");
        }
        LOG.debug("Training instance with id: {} deleted.", trainingInstance.getId());
    }

    public TrainingInstance assignPoolToTrainingInstance(TrainingInstance trainingInstance) {
        return trainingInstanceRepository.saveAndFlush(trainingInstance);
    }

    public LockedPoolInfo lockPool(Long poolId){
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            String url = kypoOpenStackURI + "/pools/" + poolId + "/locks/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            return pythonRestTemplate.postForObject(builder.toUriString(), new HttpEntity<>("{}", httpHeaders), LockedPoolInfo.class);
        } catch (RestTemplateException ex) {
            throw new MicroserviceApiException("Currently, it is not possible to lock and assign pool with (ID: " + poolId + ").", new PythonApiError(ex.getMessage()));
        }
    }

    public void unlockPool(Long poolId) {
        try {
            // get lock id from pool
            String urlGetLockId = kypoOpenStackURI + "/pools/" + poolId;
            PoolInfoDto poolInfoDto = pythonRestTemplate.getForEntity(UriComponentsBuilder.fromUriString(urlGetLockId).toUriString(), PoolInfoDto.class).getBody();
            // unlock pool
            String urlUnlockPool = kypoOpenStackURI + "/pools/" + poolId + "/locks/" + poolInfoDto.getLock();
            pythonRestTemplate.delete(UriComponentsBuilder.fromUriString(urlUnlockPool).toString());
        } catch (RestTemplateException ex) {
            throw new MicroserviceApiException("Currently, it is not possible to unlock a pool with (ID: " + poolId + ").", new PythonApiError(ex.getMessage()));
        }
    }

    private String generateAccessToken(String accessToken) {
        String newPass = "";
        boolean generated = false;
        while (!generated) {
            String numPart = RandomStringUtils.random(4, false, true);
            newPass = accessToken + "-" + numPart;
            Optional<AccessToken> pW = accessTokenRepository.findOneByAccessToken(newPass);
            if (!pW.isPresent()) generated = true;
        }
        AccessToken newTokenInstance = new AccessToken();
        newTokenInstance.setAccessToken(newPass);
        accessTokenRepository.saveAndFlush(newTokenInstance);
        return newPass;
    }

    private void removeSandboxFromTrainingRun(Long sandboxId) {
        Optional<TrainingRun> trainingRun = trainingRunRepository.findBySandboxInstanceRefId(sandboxId);
        if (trainingRun.isPresent()) {
            trainingRun.get().setState(TRState.ARCHIVED);
            trainingRun.get().setSandboxInstanceRefId(null);
            trainingRun.get().setPreviousSandboxInstanceRefId(sandboxId);
            trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.get().getParticipantRef().getUserRefId(), trainingRun.get().getTrainingInstance().getId());
            trainingRunRepository.save(trainingRun.get());
        }
    }

    /**
     * Finds all Training Runs of specific Training Instance.
     *
     * @param instanceId id of Training Instance whose Training Runs would be returned.
     * @param isActive           if isActive attribute is True, only active runs are returned
     * @param pageable           pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific {@link TrainingInstance}
     */
    public Page<TrainingRun> findTrainingRunsByTrainingInstance(Long instanceId, Boolean isActive, Pageable
            pageable) {
        Assert.notNull(instanceId, "Input training instance id must not be null.");
        trainingInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(
                        TrainingInstance.class, "id", instanceId.getClass(), instanceId, "Training instance not found.")));
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
        return organizerRefRepository.findUsers(usersRefId);
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
        Assert.hasLength(accessToken, "AccessToken cannot be null or empty.");
        return trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(
                        TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken, "There is no active game session matching access token.")));
    }
}
