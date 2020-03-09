package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import cz.muni.ics.kypo.training.exceptions.errors.JavaApiError;
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

import java.io.IOException;

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
        TrainingRun trainingRun = trainingRunRepository.findById(trainingRunId).orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(),
                trainingRunId, "The necessary permissions are required for a resource.")));
        return trainingRun.getParticipantRef().getUserRefId().equals(getUserRefIdFromUserAndGroup());
    }

    public boolean isOrganizerOfGivenTrainingInstance(Long instanceId) {
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(instanceId).orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", instanceId.getClass(),
                instanceId, "The necessary permissions are required for a resource.")));
        return trainingInstance.getOrganizers().stream().anyMatch(o -> o.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
    }

    public boolean isDesignerOfGivenTrainingDefinition(Long definitionId) {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.findById(definitionId).orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class,
                "id", definitionId.getClass(), definitionId, "The necessary permissions are required for a resource.")));
        return trainingDefinition.getAuthors().stream().anyMatch(a -> a.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
    }

    public boolean isInBetaTestingGroup(Long definitionId) {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.findById(definitionId).orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class,
                "id", definitionId.getClass(), definitionId, "The necessary permissions are required for a resource.")));
        if (trainingDefinition.getBetaTestingGroup() == null) {
            throw new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class,
                    "id", definitionId.getClass(), definitionId, "The necessary permissions are required for a resource."));
        }
        return trainingDefinition.getBetaTestingGroup().getOrganizers().stream().anyMatch(o -> o.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
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
            ResponseEntity<UserRefDTO> userInfoResponseEntity = restTemplate.exchange(userAndGroupURI + "/users/info", HttpMethod.GET, new HttpEntity<>(httpHeaders), UserRefDTO.class);
            UserRefDTO userRefDto = userInfoResponseEntity.getBody();
            return userRefDto.getUserRefId();
        } catch (HttpClientErrorException ex) {
            try {
                throw new MicroserviceApiException("Error when calling UserAndGroup API to get info about logged in user.", convertJsonBytesToObject(ex.getResponseBodyAsString(), JavaApiError.class));
            } catch (IOException ex1) {
                throw new InternalServerErrorException("Unable to parse ApiError when calling UserAndGroup API");
            }
        }
    }

    public UserRefDTO getUserRefDTOFromUserAndGroup() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<UserRefDTO> userInfoResponseEntity = restTemplate.exchange(userAndGroupURI + "/users/info", HttpMethod.GET, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<UserRefDTO>() {
            });
            return userInfoResponseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            try {
                throw new MicroserviceApiException("Error when calling UserAndGroup API to get info about logged in user.", convertJsonBytesToObject(ex.getResponseBodyAsString(), JavaApiError.class));
            } catch (IOException ex1) {
                throw new InternalServerErrorException("Unable to parse ApiError when calling UserAndGroup API");
            }
        }
    }

    public UserRef createUserRefEntityByInfoFromUserAndGroup() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<UserRefDTO> userInfoResponseEntity = restTemplate.exchange(userAndGroupURI + "/users/info", HttpMethod.GET, new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<UserRefDTO>() {
            });
            UserRefDTO userRefDto = userInfoResponseEntity.getBody();

            UserRef userRef = new UserRef();
            userRef.setUserRefId(userRefDto.getUserRefId());
            return userRef;
        } catch (HttpClientErrorException ex) {
            try {
                throw new MicroserviceApiException("Error when calling UserAndGroup API to get info about logged in user.", convertJsonBytesToObject(ex.getResponseBodyAsString(), JavaApiError.class));
            } catch (IOException ex1) {
                throw new InternalServerErrorException("Unable to parse ApiError when calling UserAndGroup API");
            }
        }
    }
    private static <T> T convertJsonBytesToObject(String object, Class<T> objectClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule( new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.readValue(object, objectClass);
    }


}
