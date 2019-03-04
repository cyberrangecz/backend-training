package cz.muni.ics.kypo.training.service.impl;

import com.google.gson.JsonObject;
import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.AccessTokenRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import cz.muni.ics.kypo.training.utils.SandboxPoolInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author Pavel Seda (441048)
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

    @Autowired
    public TrainingInstanceServiceImpl(TrainingInstanceRepository trainingInstanceRepository, AccessTokenRepository accessTokenRepository,
                                       TrainingRunRepository trainingRunRepository, UserRefRepository organizerRefRepository,
                                       RestTemplate restTemplate) {
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.organizerRefRepository = organizerRefRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public TrainingInstance findById(Long instanceId) {
        LOG.debug("findById({})", instanceId);
        return trainingInstanceRepository.findById(instanceId).orElseThrow(() -> new ServiceLayerException("Training instance with id: " + instanceId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAllTrainingDefinitions({},{})", predicate, pageable);
        return trainingInstanceRepository.findAll(predicate, pageable);
    }

    @Override
    @IsOrganizerOrAdmin
    public TrainingInstance create(TrainingInstance trainingInstance) {
        LOG.debug("create({})", trainingInstance);
        Assert.notNull(trainingInstance, "Input training instance must not be null");
        trainingInstance.setAccessToken(generateAccessToken(trainingInstance.getAccessToken()));
        if (trainingInstance.getStartTime().isAfter(trainingInstance.getEndTime())) {
            throw new ServiceLayerException("End time must be latfindAllByParticipantRefLoginer than start time.", ErrorCode.RESOURCE_CONFLICT);
        }
        Optional<UserRef> authorOfTrainingInstance = organizerRefRepository.findUserByUserRefLogin(getSubOfLoggedInUser());
        if(authorOfTrainingInstance.isPresent()) {
            trainingInstance.addOrganizer(authorOfTrainingInstance.get());
        } else {
            UserRef u = new UserRef();
            u.setUserRefLogin(getSubOfLoggedInUser());
            u.setUserRefFullName(getFullNameOfLoggedInUser());
            trainingInstance.addOrganizer(organizerRefRepository.save(u));

        }
        TrainingInstance tI = trainingInstanceRepository.save(trainingInstance);
        LOG.info("Training instance with id: {} created.", trainingInstance.getId());
        return tI;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstance.id)")
    public String update(TrainingInstance trainingInstance) {
        LOG.debug("update({})", trainingInstance);
        Assert.notNull(trainingInstance, "Input training instance must not be null");
        TrainingInstance tI = trainingInstanceRepository.findById(trainingInstance.getId())
                .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + trainingInstance.getId() + ", not found.", ErrorCode.RESOURCE_NOT_FOUND));
        if (trainingInstance.getStartTime().isAfter(trainingInstance.getEndTime())) {
            throw new ServiceLayerException("End time must be later than start time.", ErrorCode.RESOURCE_CONFLICT);
        }
        String shortPass = tI.getAccessToken().substring(0, tI.getAccessToken().length() - 5);
        if (trainingInstance.getAccessToken().equals(shortPass) || trainingInstance.getAccessToken().equals(tI.getAccessToken())) {
            trainingInstance.setAccessToken(tI.getAccessToken());
        } else {
            trainingInstance.setAccessToken(generateAccessToken(trainingInstance.getAccessToken()));
        }
        trainingInstance.setTrainingDefinition(tI.getTrainingDefinition());
        trainingInstanceRepository.save(trainingInstance);
        LOG.info("Training instance with id: {} updated.", trainingInstance.getId());
        return trainingInstance.getAccessToken();
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public void delete(Long instanceId) {
        LOG.debug("delete({})", instanceId);
        Assert.notNull(instanceId, "Input training instance id must not be null");
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + instanceId + ", not found.", ErrorCode.RESOURCE_NOT_FOUND));
        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.isAfter(trainingInstance.getStartTime()) && currentDate.isBefore(trainingInstance.getEndTime()))
            throw new ServiceLayerException("The training instance which is running cannot be deleted.", ErrorCode.RESOURCE_CONFLICT);
        if (currentDate.isAfter(trainingInstance.getEndTime()) && trainingRunRepository.findAllByTrainingInstanceId(
                trainingInstance.getId(), PageRequest.of(0, 5)).getTotalElements() > 0)
            throw new ServiceLayerException("Finished training instance with already assigned training runs cannot be deleted.", ErrorCode.RESOURCE_CONFLICT);
        trainingInstanceRepository.delete(trainingInstance);
        LOG.info("Training instance with id: {} deleted.", instanceId);
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
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public Long createPoolForSandboxes(Long instanceId) {
        LOG.debug("createPoolForSandboxes({})", instanceId);
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
        ResponseEntity<SandboxPoolInfo> poolResponse = restTemplate.exchange(kypoOpenStackURI + "/pools/", HttpMethod.POST, new HttpEntity<>(requestJson, httpHeaders), SandboxPoolInfo.class);
        if (poolResponse.getStatusCode().isError() || poolResponse.getBody() == null) {
            throw new ServiceLayerException("Error from openstack while creating pool.", ErrorCode.UNEXPECTED_ERROR);
        }
        trainingInstance.setPoolId(poolResponse.getBody().getId());
        return poolResponse.getBody().getId();
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public void allocateSandboxes(Long instanceId) {
        LOG.debug("allocateSandboxes({})", instanceId);
        TrainingInstance trainingInstance = findById(instanceId);
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
        Set<Long> idsOfNewSandboxes = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            try {
                //Allocate sandboxes in pool
                ResponseEntity<List<SandboxInfo>> sandboxResponse = restTemplate.exchange(kypoOpenStackURI + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/", HttpMethod.POST, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>() {
                });
                if (sandboxResponse.getStatusCode().isError() || sandboxResponse.getBody() == null) {
                    throw new ServiceLayerException("Error from openstack while allocate sandboxes.", ErrorCode.UNEXPECTED_ERROR);
                }
                sandboxResponse.getBody().forEach(s -> {
                    if (s.getStatus().contains("CREATE")) {
                        idsOfNewSandboxes.add(s.getId());
                    }
                });
                try {
                    TimeUnit.SECONDS.sleep(20);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ex);
                }
                //Get state of sandboxes
                ResponseEntity<List<SandboxInfo>> allocatedSandboxes = restTemplate.exchange(kypoOpenStackURI + "/pools/" + trainingInstance.getPoolId() + "/sandboxes/", HttpMethod.GET, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>() {
                });
                if (allocatedSandboxes.getStatusCode().isError() || allocatedSandboxes.getBody() == null) {
                    throw new ServiceLayerException("Error from openstack while obtaining states of sandboxes.", ErrorCode.UNEXPECTED_ERROR);
                }

                //Remove failed sandboxes, also from open stack
                Set<Long> idsOfFailedSandboxes = allocatedSandboxes.getBody().stream().filter(f -> f.getStatus().contains("FAILED")).map(SandboxInfo::getId).collect(Collectors.toSet());
                if (idsOfFailedSandboxes.size() == 0) {
                    break;
                }
                for (Long idOfSandboxInstance : idsOfFailedSandboxes) {
                    ResponseEntity<String> responseOnDelete = restTemplate.exchange(kypoOpenStackURI + "/sandboxes/" + idOfSandboxInstance + "/", HttpMethod.DELETE, new HttpEntity<>(httpHeaders), String.class);
                    if (!responseOnDelete.getStatusCode().is2xxSuccessful()) {
                        throw new ServiceLayerException("Error from openstack while deleting sandboxes.", ErrorCode.UNEXPECTED_ERROR);
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ex);
                }

                idsOfNewSandboxes.removeAll(idsOfFailedSandboxes);
            } catch (HttpClientErrorException ex) {
                throw new ServiceLayerException("Client side error when calling OpenStack:" + ex.getMessage() + ". Probably wrong URL of service.", ErrorCode.UNEXPECTED_ERROR);
            }
        }
        for (Long sandboxInstanceRefId : idsOfNewSandboxes) {
            SandboxInstanceRef s = new SandboxInstanceRef();
            s.setSandboxInstanceRef(sandboxInstanceRefId);
            trainingInstance.addSandboxInstanceRef(s);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public Page<TrainingRun> findTrainingRunsByTrainingInstance(Long instanceId, Pageable pageable) {
        LOG.debug("findTrainingRunsByTrainingInstance({})", instanceId);
        org.springframework.util.Assert.notNull(instanceId, "Input training instance id must not be null.");
        trainingInstanceRepository.findById(instanceId)
                .orElseThrow( () -> new ServiceLayerException("Training instance with id: " + instanceId + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
        return trainingRunRepository.findAllByTrainingInstanceId(instanceId, pageable);
    }

    @Override
    @IsOrganizerOrAdmin
    public Set<UserRef> findUserRefsByLogins(Set<String> logins) {
        return organizerRefRepository.findUsers(logins);
    }

    private String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("sub").getAsString();
    }

    private String getFullNameOfLoggedInUser(){
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("name").getAsString();
    }
}
