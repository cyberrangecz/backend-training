package cz.muni.ics.kypo.training.service.impl;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.TrainingDefinitionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Dominik Pilar
 * @author Pavel Seda
 */
@Service
@TransactionalRO(propagation = Propagation.REQUIRES_NEW)
public class SecurityService {

    @Value("${openstack-server.uri}")
    private String kypoOpenStackURI;
    @Value("${user-and-group-server.uri}")
    private String userAndGroupURI;

    private TrainingRunRepository trainingRunRepository;
    private TrainingDefinitionRepository trainingDefinitionRepository;
    private TrainingInstanceRepository trainingInstanceRepository;
    private RestTemplate restTemplate;

    @Autowired
    public SecurityService(TrainingInstanceRepository trainingInstanceRepository, TrainingDefinitionRepository trainingDefinitionRepository,
                           TrainingRunRepository trainingRunRepository, RestTemplate restTemplate) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.restTemplate = restTemplate;
    }

    public boolean isTraineeOfGivenTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunRepository.findById(trainingRunId).orElseThrow(() -> new ServiceLayerException("The necessary permissions are required for a resource.", ErrorCode.SECURITY_RIGHTS));
        return trainingRun.getParticipantRef().getUserRefLogin().equals(getSubOfLoggedInUser());
    }

    public boolean isOrganizerOfGivenTrainingInstance(Long instanceId) {
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(instanceId).orElseThrow(() -> new ServiceLayerException("The necessary permissions are required for a resource.", ErrorCode.SECURITY_RIGHTS));
        return trainingInstance.getOrganizers().stream().anyMatch(o -> o.getUserRefLogin().equals(getSubOfLoggedInUser()));
    }

    public boolean isDesignerOfGivenTrainingDefinition(Long definitionId) {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.findById(definitionId).orElseThrow(() -> new ServiceLayerException("The necessary permissions are required for a resource.", ErrorCode.SECURITY_RIGHTS));
        return trainingDefinition.getAuthors().stream().anyMatch(a -> a.getUserRefLogin().equals(getSubOfLoggedInUser()));
    }

    public boolean isInBetaTestingGroup(Long definitionId) {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.findById(definitionId).orElseThrow(() -> new ServiceLayerException("The necessary permissions are required for a resource.", ErrorCode.SECURITY_RIGHTS));
        if (trainingDefinition.getBetaTestingGroup() == null) {
            throw new ServiceLayerException("The necessary permissions are required for a resource.", ErrorCode.SECURITY_RIGHTS);
        }
        return trainingDefinition.getBetaTestingGroup().getOrganizers().stream().anyMatch(a -> a.getUserRefLogin().equals(getSubOfLoggedInUser()));
    }

    public boolean isAdmin() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getUserAuthentication().getAuthorities()) {
            if (gA.getAuthority().equals(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name())) return true;
        }
        return false;
    }

    public boolean isDesigner() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getUserAuthentication().getAuthorities()) {
            if (gA.getAuthority().equals(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name())) return true;
        }
        return false;
    }

    public boolean isOrganizer() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getUserAuthentication().getAuthorities()) {
            if (gA.getAuthority().equals(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name())) return true;
        }
        return false;
    }

    public String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get(AuthenticatedUserOIDCItems.SUB.getName()).getAsString();
    }

    public String getFullNameOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get(AuthenticatedUserOIDCItems.NAME.getName()).getAsString();
    }

    public String getGivenNameOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get(AuthenticatedUserOIDCItems.GIVEN_NAME.getName()).getAsString();
    }

    public String getFamilyNameOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get(AuthenticatedUserOIDCItems.FAMILY_NAME.getName()).getAsString();
    }

    public String getIssOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get(AuthenticatedUserOIDCItems.ISS.getName()).getAsString();
    }

    public Long getUserRefIdFromUserAndGroup() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<UserInfoDTO> userInfoResponseEntity = restTemplate.exchange(userAndGroupURI + "/users/info", HttpMethod.GET, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<UserInfoDTO>() {
            });
            UserInfoDTO userRefDto = userInfoResponseEntity.getBody();
            return userRefDto.getUserRefId();
        } catch (HttpClientErrorException ex) {
            throw new ServiceLayerException("Error from retrieving information from user and group service: " + ex.getMessage() + " - " + ex.getResponseBodyAsString(), ErrorCode.UNEXPECTED_ERROR);
        }
    }

    public UserInfoDTO getUserRefDTOFromUserAndGroup() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<UserInfoDTO> userInfoResponseEntity = restTemplate.exchange(userAndGroupURI + "/users/info", HttpMethod.GET, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<UserInfoDTO>() {
            });
            return userInfoResponseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            throw new ServiceLayerException("Error from retrieving information from user and group service: " + ex.getMessage() + " - " + ex.getResponseBodyAsString(), ErrorCode.UNEXPECTED_ERROR);
        }
    }

    public UserRef createUserRefEntityByInfoFromUserAndGroup() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<UserInfoDTO> userInfoResponseEntity = restTemplate.exchange(userAndGroupURI + "/users/info", HttpMethod.GET, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<UserInfoDTO>() {
            });
            UserInfoDTO userRefDto = userInfoResponseEntity.getBody();

            UserRef userRef = new UserRef();
            userRef.setUserRefId(userRefDto.getUserRefId());
            userRef.setIss(userRefDto.getIss());
            userRef.setUserRefGivenName(userRefDto.getGivenName());
            userRef.setUserRefFamilyName(userRefDto.getFamilyName());
            userRef.setUserRefLogin(userRefDto.getLogin());
            userRef.setUserRefFullName(userRefDto.getFullName());
            return userRef;
        } catch (HttpClientErrorException ex) {
            throw new ServiceLayerException("Error from retrieving information from user management service: " + ex.getMessage() + " - " + ex.getResponseBodyAsString(), ErrorCode.UNEXPECTED_ERROR);
        }
    }

}
