package cz.muni.ics.kypo.training.service.impl;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.repository.TrainingDefinitionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

/**
 * @author Dominik Pilar & Pavel Seda
 */
@Service
@TransactionalRO(propagation = Propagation.REQUIRES_NEW)
public class SecurityService {
    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    private TrainingRunRepository trainingRunRepository;
    private TrainingDefinitionRepository trainingDefinitionRepository;
    private TrainingInstanceRepository trainingInstanceRepository;

    @Autowired
    public SecurityService(TrainingInstanceRepository trainingInstanceRepository, TrainingDefinitionRepository trainingDefinitionRepository,
                           TrainingRunRepository trainingRunRepository) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
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
        return credentials.get("sub").getAsString();
    }

    public String getFullNameOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("name").getAsString();
    }
}
