package cz.muni.ics.kypo.training.service.impl;

import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.aop.TrackTime;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import cz.muni.ics.kypo.training.utils.SandboxPoolInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


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
    private RestTemplate restTemplate;
    private SecurityService securityService;
    private SandboxInstanceRefRepository sandboxInstanceRefRepository;

    @Autowired
    public TrainingInstanceServiceImpl(TrainingInstanceRepository trainingInstanceRepository, AccessTokenRepository accessTokenRepository,
                                       TrainingRunRepository trainingRunRepository, UserRefRepository organizerRefRepository,
                                       RestTemplate restTemplate, SecurityService securityService, SandboxInstanceRefRepository sandboxInstanceRefRepository) {
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.organizerRefRepository = organizerRefRepository;
        this.restTemplate = restTemplate;
        this.securityService = securityService;
        this.sandboxInstanceRefRepository = sandboxInstanceRefRepository;
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
        Predicate loggedInUser = QTrainingInstance.trainingInstance.organizers.any().userRefLogin.eq(securityService.getSubOfLoggedInUser());
        return trainingInstanceRepository.findAll(loggedInUser, pageable);
    }

    @Override
    @IsOrganizerOrAdmin
    public List<Long> findIdsOfAllOccupiedSandboxesByTrainingInstance(Long trainingInstanceId) {
        return trainingRunRepository.findIdsOfAllOccupiedSandboxesByTrainingInstance(trainingInstanceId);
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
        return trainingInstanceRepository.save(trainingInstance);
    }

//TODO during update automatically add author as organizer of training instance, add login of logged in user in facade when calling user and group ;)
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
        trainingInstanceRepository.save(trainingInstanceToUpdate);
        addLoggedInUserAsOrganizerToTrainingInstance(trainingInstance);
        return trainingInstanceToUpdate.getAccessToken();
    }

    private void addLoggedInUserAsOrganizerToTrainingInstance(TrainingInstance trainingInstance) {
        Optional<UserRef> authorOfTrainingInstance = organizerRefRepository.findUserByUserRefLogin(securityService.getSubOfLoggedInUser());
        if (authorOfTrainingInstance.isPresent()) {
            trainingInstance.addOrganizer(authorOfTrainingInstance.get());
        } else {
            UserRef userRef = new UserRef();
            userRef.setUserRefLogin(securityService.getSubOfLoggedInUser());
            userRef.setUserRefFullName(securityService.getFullNameOfLoggedInUser());
            userRef.setUserRefGivenName(securityService.getGivenNameOfLoggedInUser());
            userRef.setUserRefFamilyName(securityService.getFamilyNameOfLoggedInUser());
            trainingInstance.addOrganizer(organizerRefRepository.save(userRef));
        }
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TrackTime
    public void delete(Long instanceId) {
        Assert.notNull(instanceId, "Input training instance id must not be null");
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + instanceId + ", not found.", ErrorCode.RESOURCE_NOT_FOUND));
        LocalDateTime currentDate = LocalDateTime.now(Clock.systemUTC());
        if (currentDate.isAfter(trainingInstance.getEndTime()) && trainingRunRepository.findAllByTrainingInstanceId(
                trainingInstance.getId(), PageRequest.of(0, 5)).getTotalElements() > 0)
            throw new ServiceLayerException("Finished training instance with already assigned training runs cannot be deleted.", ErrorCode.RESOURCE_CONFLICT);
        if (trainingInstance.getPoolId() != null && trainingInstance.getSandboxInstanceRefs().isEmpty()) {
            removePoolOfSandboxesFromOpenStack(trainingInstance.getPoolId());
        } else if (trainingInstance.getPoolId() != null) {
            throw new ServiceLayerException("Cannot delete training instance because it contains some sandboxes. Please delete sandboxes and try again.", ErrorCode.RESOURCE_CONFLICT);
        }
        trainingInstanceRepository.delete(trainingInstance);
        LOG.debug("Training instance with id: {} deleted.", instanceId);
    }

    private void removePoolOfSandboxesFromOpenStack(Long poolId) {
        //Delete pool
        String url = kypoOpenStackURI + "/pools/" + poolId + "/";
        try {
            restTemplate.delete(UriComponentsBuilder.fromUriString(url).toUriString());
        } catch (HttpClientErrorException ex) {
            LOG.error("Client side error when calling OpenStack: {}.", ex.getMessage() + " - " + ex.getResponseBodyAsString());
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

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    @TrackTime
    public Long createPoolForSandboxes(Long instanceId) {
        TrainingInstance trainingInstance = findById(instanceId);
        //Check if pool can be created
        if (trainingInstance.getPoolId() != null) {
            return trainingInstance.getPoolId();
        }

        //Create pool with given size
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String requestJson = "{\"definition\": " + trainingInstance.getTrainingDefinition().getSandboxDefinitionRefId() +
                ", \"max_size\": " + trainingInstance.getPoolSize() + "}";
        try {
            ResponseEntity<SandboxPoolInfo> poolResponse = restTemplate.exchange(kypoOpenStackURI + "/pools/", HttpMethod.POST, new HttpEntity<>(requestJson, httpHeaders), SandboxPoolInfo.class);
            trainingInstance.setPoolId(Objects.requireNonNull(poolResponse.getBody()).getId());
            return poolResponse.getBody().getId();
        } catch (HttpClientErrorException ex) {
            throw new ServiceLayerException("Error from OpenStack while creating pool: " + ex.getMessage()  +  " - " + ex.getResponseBodyAsString(), ErrorCode.UNEXPECTED_ERROR);
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
        if (count != null && count + trainingInstance.getSandboxInstanceRefs().size() > trainingInstance.getPoolSize()) {
            count = trainingInstance.getPoolSize() - trainingInstance.getSandboxInstanceRefs().size();
        }
        //Check if pool exist
        if (trainingInstance.getPoolId() == null) {
            throw new ServiceLayerException("Pool for sandboxes is not created yet. Please create pool before allocating sandboxes.", ErrorCode.RESOURCE_CONFLICT);
        }
        //Check if sandbox can be allocated
        if (trainingInstance.getSandboxInstanceRefs().size() >= trainingInstance.getPoolSize()) {
            throw new ServiceLayerException("Pool of sandboxes of training instance with id: " + trainingInstance.getId() + " is full.", ErrorCode.RESOURCE_CONFLICT);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            //Allocate sandboxes in pool
            String url = kypoOpenStackURI + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            if (count != null) {
                builder.queryParam("count", count);
            }
            // allocate sandboxes with appropriate ansible scripts (set up an environment etc.)
            builder.queryParam("full", true);
            ResponseEntity<List<SandboxInfo>> sandboxResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.POST,
                    new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>() {
                    });
            Objects.requireNonNull(sandboxResponse.getBody()).forEach(s -> {
                SandboxInstanceRef sIR = new SandboxInstanceRef();
                sIR.setSandboxInstanceRef(s.getId());
                trainingInstance.addSandboxInstanceRef(sIR);
            });
            trainingInstanceRepository.save(trainingInstance);
        } catch (HttpClientErrorException ex) {
            LOG.error("Client side error when calling OpenStack: {}.", ex.getMessage() + " - " + ex.getResponseBodyAsString());
        }
    }
    @Override
    @TransactionalWO
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @Async
    @TrackTime
    public void deleteSandbox(Long trainingInstanceId, Long idOfSandboxRefToDelete) {
        Optional<TrainingInstance> trainingInstanceOptional = trainingInstanceRepository.findById(trainingInstanceId);
        TrainingInstance trainingInstance;
        if(trainingInstanceOptional.isPresent()) {
            trainingInstance = trainingInstanceOptional.get();
        } else {
            LOG.error("Training Instance with id: " + trainingInstanceId + " not found.");
            return;
        }
        trainingInstanceRepository.save(trainingInstance);
        Optional<SandboxInstanceRef> optionalSandboxInstanceRefToDelete = sandboxInstanceRefRepository.findBySandboxInstanceRefId(idOfSandboxRefToDelete);
        if(optionalSandboxInstanceRefToDelete.isPresent()) {
            if(trainingInstance.getSandboxInstanceRefs().contains(optionalSandboxInstanceRefToDelete.get())) {
                Optional<TrainingRun> trainingRun = trainingRunRepository.findBySandboxInstanceRef(optionalSandboxInstanceRefToDelete.get());
                if(trainingRun.isPresent()) {
                    trainingRun.get().setState(TRState.ARCHIVED);
                    trainingRun.get().setSandboxInstanceRef(null);
                    trainingRun.get().setPreviousSandboxInstanceRefId(optionalSandboxInstanceRefToDelete.get().getSandboxInstanceRef());
                    trainingRunRepository.save(trainingRun.get());
                }
                trainingInstance.removeSandboxInstanceRef(optionalSandboxInstanceRefToDelete.get());
            } else {
                return;
            }
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.exchange(kypoOpenStackURI + "/sandboxes/" + idOfSandboxRefToDelete + "/",
                    HttpMethod.DELETE, new HttpEntity<>(httpHeaders), String.class);
        } catch (HttpClientErrorException ex) {
            if (!ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                LOG.error("Client side error when calling OpenStack: {}. Probably wrong URL of service.", ex.getMessage() + " - " + ex.getResponseBodyAsString());
            }
        }
        trainingInstanceRepository.save(trainingInstance);
    }

    private void synchronizeSandboxesWithPythonApi(TrainingInstance trainingInstance) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            //Get sandboxes
            String url = kypoOpenStackURI + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/";
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            ResponseEntity<List<SandboxInfo>> sandboxResponse = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>() {
                    });
            Objects.requireNonNull(sandboxResponse.getBody()).forEach(s -> {
                if (trainingInstance.getSandboxInstanceRefs().stream().noneMatch((sandboxInstanceRef -> sandboxInstanceRef.getSandboxInstanceRef().equals(s.getId())))) {
                    SandboxInstanceRef sIR = new SandboxInstanceRef();
                    sIR.setSandboxInstanceRef(s.getId());
                    trainingInstance.addSandboxInstanceRef(sIR);
                }
            });
            trainingInstanceRepository.save(trainingInstance);
        } catch (HttpClientErrorException ex) {
            LOG.error("Client side error when calling OpenStack: {}.", new JSONObject(ex.getResponseBodyAsString()).get("detail"));
        }

    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public Page<TrainingRun> findTrainingRunsByTrainingInstance(Long instanceId, Boolean isActive,  Pageable pageable) {
        org.springframework.util.Assert.notNull(instanceId, "Input training instance id must not be null.");
        trainingInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + instanceId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
        if(isActive == null) {
            return trainingRunRepository.findAllByTrainingInstanceId(instanceId, pageable);

        } else if( isActive) {
            return trainingRunRepository.findAllActiveByTrainingInstanceId(instanceId, pageable);
        } else {
            return trainingRunRepository.findAllInactiveByTrainingInstanceId(instanceId, pageable);
        }
    }

    @Override
    @IsOrganizerOrAdmin
    public Set<UserRef> findUserRefsByLogins(Set<String> logins) {
        return organizerRefRepository.findUsers(logins);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public boolean checkIfInstanceIsFinished(Long trainingInstanceId) {
        return trainingInstanceRepository.isFinished(trainingInstanceId, LocalDateTime.now(Clock.systemUTC()));
    }
}
