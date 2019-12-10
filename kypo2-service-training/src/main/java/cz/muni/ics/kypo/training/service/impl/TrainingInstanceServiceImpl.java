package cz.muni.ics.kypo.training.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.csirt.kypo.elasticsearch.service.exceptions.ElasticsearchTrainingServiceLayerException;
import cz.muni.ics.kypo.training.annotations.aop.TrackTime;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.requests.PoolCreationRequest;
import cz.muni.ics.kypo.training.api.responses.PageResultResourcePython;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.api.responses.SandboxPoolInfo;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.RestTemplateException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;


/**
 * @author Pavel Seda
 * @author Boris Jadus
 */
@Service
public class TrainingInstanceServiceImpl implements TrainingInstanceService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingInstanceServiceImpl.class);
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
    public TrainingInstanceServiceImpl(TrainingInstanceRepository trainingInstanceRepository, AccessTokenRepository accessTokenRepository,
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

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public TrainingInstance findById(Long instanceId) {
        return trainingInstanceRepository.findById(instanceId).orElseThrow(() -> new ServiceLayerException("Training instance with id: " + instanceId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public TrainingInstance findByIdIncludingDefinition(Long instanceId) {
        return trainingInstanceRepository.findByIdIncludingDefinition(instanceId).orElseThrow(() -> new ServiceLayerException("Training instance with id: " + instanceId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @IsOrganizerOrAdmin
    public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
        if (securityService.isAdmin()) {
            return trainingInstanceRepository.findAll(predicate, pageable);
        }
        Predicate loggedInUser = QTrainingInstance.trainingInstance.organizers.any().userRefId.eq(securityService.getUserRefIdFromUserAndGroup()).and(predicate);
        return trainingInstanceRepository.findAll(loggedInUser, pageable);
    }

    @Override
    @IsOrganizerOrAdmin
    public List<Long> findIdsOfAllOccupiedSandboxesByTrainingInstance(Long trainingInstanceId) {
        TrainingInstance trainingInstance =  trainingInstanceRepository.findById(trainingInstanceId)
                .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + trainingInstanceId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));

        List<SandboxInfo> sandboxes = findSandboxesFromSandboxPool(trainingInstance.getPoolId());
        List<Long> occupiedSandboxes = new ArrayList<>();
        sandboxes.forEach(s -> {
            if (s.isLocked()) occupiedSandboxes.add(s.getId());
        });
        return occupiedSandboxes;
    }

    private List<SandboxInfo> findSandboxesFromSandboxPool(Long poolId){
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            String url = kypoOpenStackURI + "/pools/" + poolId + "/sandboxes/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            builder.queryParam("page", 1);
            builder.queryParam("page_size", PYTHON_RESULT_PAGE_SIZE);
            ResponseEntity<PageResultResourcePython<SandboxInfo>> response = pythonRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    new ParameterizedTypeReference<PageResultResourcePython<SandboxInfo>>() {
                    });
            PageResultResourcePython<SandboxInfo> sandboxInfoPageResult = Objects.requireNonNull(response.getBody());
            return Objects.requireNonNull(sandboxInfoPageResult.getResults());
        } catch (RestTemplateException ex) {
            throw new ServiceLayerException("Error from Python API when checking a state of sandboxes in pool (ID: " + poolId + "): "
                    + ex.getStatusCode()+ ": " + ex.getMessage() + " Please contact administrator", ErrorCode.PYTHON_API_ERROR);
        }
    }

    @Override
    @IsOrganizerOrAdmin
    public TrainingInstance create(TrainingInstance trainingInstance) {
        Assert.notNull(trainingInstance, "Input training instance must not be null");
        trainingInstance.setAccessToken(generateAccessToken(trainingInstance.getAccessToken()));
        if (trainingInstance.getStartTime().isAfter(trainingInstance.getEndTime())) {
            throw new ServiceLayerException("End time must be later than start time.", ErrorCode.RESOURCE_CONFLICT);
        }
        addLoggedInUserAsOrganizerToTrainingInstance(trainingInstance);
        trainingInstance.setPoolId(createPoolForSandboxes(trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId(), trainingInstance.getPoolSize()));
        return trainingInstanceRepository.save(trainingInstance);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceToUpdate.id)")
    public String update(TrainingInstance trainingInstanceToUpdate) {
        Assert.notNull(trainingInstanceToUpdate, "Input training instance must not be null");
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(trainingInstanceToUpdate.getId())
                .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + trainingInstanceToUpdate.getId() + ", not found.", ErrorCode.RESOURCE_NOT_FOUND));
        if (trainingInstanceToUpdate.getStartTime().isAfter(trainingInstanceToUpdate.getEndTime())) {
            throw new ServiceLayerException("End time must be later than start time.", ErrorCode.RESOURCE_CONFLICT);
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

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstance.id)")
    @TrackTime
    public void delete(TrainingInstance trainingInstance) {
        Assert.notNull(trainingInstance, "Input training instance must not be null");
        if (trainingRunRepository.existsAnyForTrainingInstance(trainingInstance.getId())) {
            throw new ServiceLayerException("Training instance with already assigned training runs cannot be deleted. Please delete training runs assigned to training instance" +
                    " and try again or contact administrator.", ErrorCode.RESOURCE_CONFLICT);
        }
        trainingInstanceRepository.delete(trainingInstance);
        if (trainingInstance.getPoolId() != null) {
            try {
                List<SandboxInfo> sandboxes = findSandboxesFromSandboxPool(trainingInstance.getPoolId());
                if (sandboxes.isEmpty()){
                    removePoolOfSandboxesFromOpenStack(trainingInstance.getPoolId());
                }else {
                    LOG.error("Pool (ID: {}) assigned to training instance contains some sandboxes. Training instance will has been " +
                            "deleted and these sandboxes remains in the pool and must be deleted through Python API.");
                }
            } catch (ServiceLayerException ex) {
                LOG.error(ex.getLocalizedMessage());
            }
        }
        try {
            trainingEventsService.deleteEventsByTrainingInstanceId(trainingInstance.getId());
        } catch (ElasticsearchTrainingServiceLayerException io) {
            LOG.error("Could not delete documents of this training instance from Elasticsearch. Please contact administrator to check if Elasticsearch is running.");
        }
        LOG.debug("Training instance with id: {} deleted.", trainingInstance.getId());
    }

    private void removePoolOfSandboxesFromOpenStack(Long poolId) {
        //Delete pool
        String url = kypoOpenStackURI + "/pools/" + poolId + "/";
        try {
            pythonRestTemplate.delete(UriComponentsBuilder.fromUriString(url).toUriString());
        } catch (RestTemplateException ex) {
            throw new ServiceLayerException("Error when calling Python API to remove pool (ID: " + poolId + "):"
                    + ex.getStatusCode() + ": "+  ex.getMessage(), ErrorCode.PYTHON_API_ERROR);
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

    @TrackTime
    private Long createPoolForSandboxes(Long sandboxDefinitionId, int poolSize) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        PoolCreationRequest poolCreationRequest = new PoolCreationRequest();
        poolCreationRequest.setSandboxDefinitionId(sandboxDefinitionId);
        poolCreationRequest.setPoolSize(poolSize);

        try {
            ResponseEntity<SandboxPoolInfo> poolResponse = pythonRestTemplate.exchange(kypoOpenStackURI + "/pools/", HttpMethod.POST, new HttpEntity<>(poolCreationRequest, httpHeaders), SandboxPoolInfo.class);
            return Objects.requireNonNull(poolResponse.getBody()).getId();
        } catch (RestTemplateException ex) {
            throw new ServiceLayerException("Error when calling Python API to create pool: "  + ex.getStatusCode() + ": " + ex.getMessage(), ErrorCode.PYTHON_API_ERROR);
        }
    }

    @Override
    @TransactionalWO
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstance.id)")
    @Async
    @TrackTime
    public void allocateSandboxes(TrainingInstance trainingInstance, Integer count) {
        if (count != null && count > trainingInstance.getPoolSize()) {
            count = null;
        }
        if (trainingInstance.getPoolId() == null) {
            throw new ServiceLayerException("Pool for sandboxes is not created yet. Please create pool before allocating sandboxes.", ErrorCode.RESOURCE_CONFLICT);
        }
        List<SandboxInfo> sandboxes = findSandboxesFromSandboxPool(trainingInstance.getPoolId());
        if (count != null && count + sandboxes.size() > trainingInstance.getPoolSize()) {
            count = trainingInstance.getPoolSize() - sandboxes.size();
        }
        if (sandboxes.size() >= trainingInstance.getPoolSize()) {
            throw new ServiceLayerException("Pool of sandboxes of training instance with id: " + trainingInstance.getId() + " is full.", ErrorCode.RESOURCE_CONFLICT);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            String url = kypoOpenStackURI + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            if (count != null) {
                builder.queryParam("count", count);
            }
            builder.queryParam("full", true);
            pythonRestTemplate.exchange(builder.toUriString(), HttpMethod.POST,
                    new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>() {
                    });
        } catch (RestTemplateException ex) {
            LOG.error("Error when calling Python API to allocate sandboxes: {}.", ex.getStatusCode() + " - " + ex.getMessage());
        }
    }

    @Override
    @TransactionalWO
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @Async
    @TrackTime
    public void deleteSandbox(Long trainingInstanceId, Long idOfSandboxRefToDelete) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            pythonRestTemplate.exchange(kypoOpenStackURI + "/sandboxes/" + idOfSandboxRefToDelete + "/lock/",
                    HttpMethod.DELETE, new HttpEntity<>(httpHeaders), String.class);
        } catch (RestTemplateException ex) {
            if (!ex.getStatusCode().equals(HttpStatus.BAD_REQUEST.toString()) &&
                !ex.getStatusCode().equals(HttpStatus.NOT_FOUND.toString())) {
                LOG.error("Error when calling Python API to get lock status of sandbox (ID: {}): {}.", idOfSandboxRefToDelete, ex.getStatusCode() + " - " + ex.getMessage());
            }
        }
        try {
            pythonRestTemplate.exchange(kypoOpenStackURI + "/sandboxes/" + idOfSandboxRefToDelete + "/",
                    HttpMethod.DELETE, new HttpEntity<>(httpHeaders), String.class);
        } catch (RestTemplateException ex) {
            if (!ex.getStatusCode().equals(HttpStatus.NOT_FOUND.toString())){
                LOG.error("Error when calling Python API to delete sandbox (ID: {}): {}.", idOfSandboxRefToDelete, ex.getStatusCode() + " - " + ex.getMessage());
            }
        }
        removeSandboxFromTrainingRun(idOfSandboxRefToDelete);
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

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public Page<TrainingRun> findTrainingRunsByTrainingInstance(Long instanceId, Boolean isActive, Pageable
            pageable) {
        Assert.notNull(instanceId, "Input training instance id must not be null.");
        trainingInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + instanceId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
        if (isActive == null) {
            return trainingRunRepository.findAllByTrainingInstanceId(instanceId, pageable);

        } else if (isActive) {
            return trainingRunRepository.findAllActiveByTrainingInstanceId(instanceId, pageable);
        } else {
            return trainingRunRepository.findAllInactiveByTrainingInstanceId(instanceId, pageable);
        }
    }

    @Override
    @IsOrganizerOrAdmin
    public Set<UserRef> findUserRefsByUserRefIds(Set<Long> usersRefId) {
        return organizerRefRepository.findUsers(usersRefId);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    public boolean checkIfInstanceIsFinished(Long trainingInstanceId) {
        return trainingInstanceRepository.isFinished(trainingInstanceId, LocalDateTime.now(Clock.systemUTC()));
    }

    @Override
    @TransactionalRO
    public TrainingInstance findByStartTimeAfterAndEndTimeBeforeAndAccessToken(String accessToken) {
        Assert.hasLength(accessToken, "AccessToken cannot be null or empty.");
        return trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
                .orElseThrow(() -> new ServiceLayerException("There is no active game session matching your keyword: " + accessToken + ".", ErrorCode.RESOURCE_NOT_FOUND));
    }
}
