package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.TrainingDefinitionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * The type Security service.
 */
@Service
@TransactionalRO(propagation = Propagation.REQUIRES_NEW)
public class SecurityService {

    private TrainingRunRepository trainingRunRepository;
    private TrainingDefinitionRepository trainingDefinitionRepository;
    private TrainingInstanceRepository trainingInstanceRepository;
    private WebClient userManagementWebClient;

    /**
     * Instantiates a new Security service.
     *
     * @param trainingInstanceRepository   the training instance repository
     * @param trainingDefinitionRepository the training definition repository
     * @param trainingRunRepository        the training run repository
     * @param userManagementWebClient      the java rest template
     */
    @Autowired
    public SecurityService(TrainingInstanceRepository trainingInstanceRepository,
                           TrainingDefinitionRepository trainingDefinitionRepository,
                           TrainingRunRepository trainingRunRepository,
                           @Qualifier("userManagementServiceWebClient") WebClient userManagementWebClient) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.userManagementWebClient = userManagementWebClient;
    }

    /**
     * Is trainee of given training run boolean.
     *
     * @param trainingRunId the training run id
     * @return the boolean
     */
    public boolean isTraineeOfGivenTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunRepository.findById(trainingRunId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(),
                        trainingRunId, "The necessary permissions are required for a resource.")));
        return trainingRun.getParticipantRef().getUserRefId().equals(getUserRefIdFromUserAndGroup());
    }

    /**
     * Is organizer of given training instance boolean.
     *
     * @param instanceId the instance id
     * @return the boolean
     */
    public boolean isOrganizerOfGivenTrainingInstance(Long instanceId) {
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", instanceId.getClass(),
                        instanceId, "The necessary permissions are required for a resource.")));
        return trainingInstance.getOrganizers().stream()
                .anyMatch(o -> o.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
    }

    /**
     * Is designer of given training definition boolean.
     *
     * @param definitionId the definition id
     * @return the boolean
     */
    public boolean isDesignerOfGivenTrainingDefinition(Long definitionId) {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.findById(definitionId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class,
                        "id", definitionId.getClass(), definitionId, "The necessary permissions are required for a resource.")));
        return trainingDefinition.getAuthors().stream()
                .anyMatch(a -> a.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
    }

    /**
     * Has role boolean.
     *
     * @param roleTypeSecurity the role type security
     * @return the boolean
     */
    public boolean hasRole(RoleTypeSecurity roleTypeSecurity) {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getUserAuthentication().getAuthorities()) {
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
            UserRefDTO userRefDTO = userManagementWebClient
                    .get()
                    .uri("/users/info")
                    .retrieve()
                    .bodyToMono(UserRefDTO.class)
                    .block();
            checkNonNull(userRefDTO, "Returned null response when calling user management service API to get info about logged in user.");
            return userRefDTO.getUserRefId();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling user management service API to get info about logged in user.", ex.getApiSubError());
        }
    }

    /**
     * Create user ref entity by info from user and group user ref.
     *
     * @return the user ref
     */
    public UserRef createUserRefEntityByInfoFromUserAndGroup() {
        try {
            UserRefDTO userRefDTO = userManagementWebClient
                    .get()
                    .uri("/users/info")
                    .retrieve()
                    .bodyToMono(UserRefDTO.class)
                    .block();
            checkNonNull(userRefDTO, "Returned null response when calling user management service API to get info about logged in user.");
            UserRef userRef = new UserRef();
            userRef.setUserRefId(userRefDTO.getUserRefId());
            return userRef;
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling user management service API to get info about logged in user.", ex.getApiSubError());
        }
    }

    /**
     * Check if response from external API is not null.
     *
     * @param object object to check
     * @param message exception message if response is null
     * @throws MicroserviceApiException if response is null
     */
    private void checkNonNull(Object object, String message) {
        if (object == null) {
            throw new MicroserviceApiException(message);
        }
    }

}
