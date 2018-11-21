package cz.muni.ics.kypo.training.service.impl;

import com.google.gson.JsonObject;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.TrainingDefinitionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
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
        return trainingRun.getParticipantRef().getParticipantRefLogin().equals(getSubOfLoggedInUser());
    }

    public boolean isOrganizeOfGivenTrainingInstance(Long instanceId) {
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(instanceId).orElseThrow(() -> new ServiceLayerException("The necessary permissions are required for a resource.", ErrorCode.SECURITY_RIGHTS));
        return trainingInstance.getOrganizers().stream().anyMatch(o -> o.getUserRefLogin().equals(getSubOfLoggedInUser()));
    }

    public boolean isDesignerOfGivenTrainingDefinition(Long definitionId) {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.findById(definitionId).orElseThrow(() -> new ServiceLayerException("The necessary permissions are required for a resource.", ErrorCode.SECURITY_RIGHTS));
        return trainingDefinition.getAuthorRef().stream().anyMatch(a -> a.getAuthorRefLogin().equals(getSubOfLoggedInUser()));
    }

    private String getSubOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("sub").getAsString();
    }
}
