package cz.cyberrange.platform.training.service.facade;

import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.services.TrainingInstanceService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import org.springframework.stereotype.Service;

@Service
public class TrainingTypeResolver {

  private final TrainingInstanceService trainingInstanceService;
  private final TrainingRunService trainingRunService;

  public TrainingTypeResolver(
      TrainingInstanceService trainingInstanceService, TrainingRunService trainingRunService) {
    this.trainingInstanceService = trainingInstanceService;
    this.trainingRunService = trainingRunService;
  }

  @TransactionalRO
  public TrainingType fromAccessToken(String accessToken) {
    return trainingInstanceService.findByEndTimeBeforeAndAccessToken(accessToken).getType();
  }

  @TransactionalRO
  public TrainingType fromInstanceId(Long instanceId) {
    return trainingInstanceService.findById(instanceId).getType();
  }

  @TransactionalRO
  public TrainingType fromTrainingRunId(Long trainingRunId) {
    return trainingRunService.findById(trainingRunId).getType();
  }
}
