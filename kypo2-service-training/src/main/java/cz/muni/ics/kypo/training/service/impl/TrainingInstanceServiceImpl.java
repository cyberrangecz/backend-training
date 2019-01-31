package cz.muni.ics.kypo.training.service.impl;

import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;


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
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public TrainingInstance findById(Long id) {
        LOG.debug("findById({})", id);
        return trainingInstanceRepository.findById(id).orElseThrow(() -> new ServiceLayerException("Training instance with id: " + id + " not found.", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAllTrainingDefinitions({},{})", predicate, pageable);
        return trainingInstanceRepository.findAll(predicate, pageable);
    }

    @Override
    @PreAuthorize("hasAuthority({'ADMINISTRATOR'}) or hasAuthority({T(cz.muni.ics.kypo.training.persistence.model.enums.RoleType).ORGANIZER})")
    public TrainingInstance create(TrainingInstance trainingInstance) {
        LOG.debug("create({})", trainingInstance);
        Assert.notNull(trainingInstance, "Input training instance must not be null");
        trainingInstance.setAccessToken(generateAccessToken(trainingInstance.getAccessToken()));
        if (trainingInstance.getStartTime().isAfter(trainingInstance.getEndTime())) {
            throw new ServiceLayerException("End time must be latfindAllByParticipantRefLoginer than start time.", ErrorCode.RESOURCE_CONFLICT);
        }
        TrainingInstance tI = trainingInstanceRepository.save(trainingInstance);
        LOG.info("Training instance with id: {} created.", trainingInstance.getId());
        return tI;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizeOfGivenTrainingInstance(#trainingInstance.id)")
    public void update(TrainingInstance trainingInstance) {
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
        trainingInstanceRepository.save(trainingInstance);
        LOG.info("Training instance with id: {} updated.", trainingInstance.getId());
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizeOfGivenTrainingInstance(#id)")
    public void delete(Long id) {
        LOG.debug("delete({})", id);
        Assert.notNull(id, "Input training instance id must not be null");
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(id)
                .orElseThrow(() -> new ServiceLayerException("Training instance with id: " + id + ", not found.", ErrorCode.RESOURCE_NOT_FOUND));
        LocalDateTime currentDate = LocalDateTime.now();
        if (!currentDate.isAfter(trainingInstance.getEndTime()))
            throw new ServiceLayerException("Only finished instances can be deleted.", ErrorCode.RESOURCE_CONFLICT);
        trainingRunRepository.deleteTrainingRunsByTrainingInstance(trainingInstance.getId());
        trainingInstanceRepository.delete(trainingInstance);
        LOG.info("Training instance with id: {} deleted.", id);
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
            "or @securityService.isOrganizeOfGivenTrainingInstance(#instanceId)")
    public void allocateSandboxes(Long instanceId) {
        LOG.debug("allocateSandboxes({})", instanceId);
        TrainingInstance trainingInstance = findById(instanceId);

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

        //Allocate sandboxes in pool
        ResponseEntity<List<SandboxInfo>> sandboxResponse = restTemplate.exchange(kypoOpenStackURI + "/pools/" + poolResponse.getBody().getId() + "/", HttpMethod.POST, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<SandboxInfo>>() {});
        if(sandboxResponse.getStatusCode().isError() || sandboxResponse.getBody() == null) {
            throw new ServiceLayerException("Error from openstack while allocate sandboxes.", ErrorCode.UNEXPECTED_ERROR);
        }
        for (SandboxInfo sandboxInfo : sandboxResponse.getBody()) {
            SandboxInstanceRef s = new SandboxInstanceRef();
            s.setSandboxInstanceRef(sandboxInfo.getId());
            trainingInstance.addSandboxInstanceRef(s);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('ADMINISTRATOR')" +
            "or @securityService.isOrganizeOfGivenTrainingInstance(#trainingInstanceId)")
    public Page<TrainingRun> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Pageable pageable) {
        LOG.debug("findTrainingRunsByTrainingInstance({})", trainingInstanceId);
        org.springframework.util.Assert.notNull(trainingInstanceId, "Input training instance id must not be null.");
        return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId, pageable);
    }

    @Override
    public Set<UserRef> findUserRefsByLogins(Set<String> logins) {
        return organizerRefRepository.findUsers(logins);
    }
}
