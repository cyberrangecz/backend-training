package cz.cyberrange.platform.training.service.unit.services;

import static org.mockito.BDDMockito.*;

import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.service.services.ExportImportService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExportImportServiceTest {

  private ExportImportService exportImportService;
  @Mock private TrainingDefinitionRepository trainingDefinitionRepository;
  @Mock private QuestionAnswerRepository questionAnswerRepository;
  @Mock private TrainingInstanceRepository trainingInstanceRepository;
  @Mock private TrainingRunRepository trainingRunRepository;

  @Mock private TrainingInstance trainingInstance;

  @BeforeEach
  public void init() {
    exportImportService =
        new ExportImportService(
            trainingDefinitionRepository,
            questionAnswerRepository,
            trainingInstanceRepository,
            trainingRunRepository);
  }

  @Test
  public void getTrainingInstanceById() {
    given(trainingInstanceRepository.findById(any(Long.class)))
        .willReturn(Optional.of(trainingInstance));
    given(trainingInstance.getId()).willReturn(1L);
    exportImportService.findInstanceById(trainingInstance.getId());

    then(trainingInstanceRepository).should().findById(trainingInstance.getId());
  }
}
