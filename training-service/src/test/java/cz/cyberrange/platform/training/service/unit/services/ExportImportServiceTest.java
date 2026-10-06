package cz.cyberrange.platform.training.service.unit.services;

import static org.mockito.BDDMockito.*;

import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.repository.*;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.services.ExportImportService;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {TestDataFactory.class})
public class ExportImportServiceTest {

  private static ExportImportService exportImportService;
  @MockitoBean private TrainingDefinitionRepository trainingDefinitionRepository;
  @MockitoBean private AbstractLevelRepository abstractLevelRepository;
  @MockitoBean private AssessmentLevelRepository assessmentLevelRepository;
  @MockitoBean private QuestionAnswerRepository questionAnswerRepository;
  @MockitoBean private InfoLevelRepository infoLevelRepository;
  @MockitoBean private TrainingLevelRepository trainingLevelRepository;
  @MockitoBean private MitreTechniqueRepository mitreTechniqueRepository;
  @MockitoBean private AccessLevelRepository accessLevelRepository;
  @MockitoBean private TrainingInstanceRepository trainingInstanceRepository;
  @MockitoBean private TrainingRunRepository trainingRunRepository;

  @Mock private static AssessmentLevel assessmentLevel;
  @Mock private static TrainingLevel trainingLevel;
  @Mock private static InfoLevel infoLevel;
  @Mock private static TrainingInstance trainingInstance;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
    exportImportService =
        new ExportImportService(
            trainingDefinitionRepository,
            abstractLevelRepository,
            assessmentLevelRepository,
            questionAnswerRepository,
            infoLevelRepository,
            trainingLevelRepository,
            mitreTechniqueRepository,
            accessLevelRepository,
            trainingInstanceRepository,
            trainingRunRepository);

    given(assessmentLevel.getId()).willReturn(1L);
    given(assessmentLevel.getQuestions()).willReturn(new ArrayList<>());
    given(trainingLevel.getId()).willReturn(2L);
    given(infoLevel.getId()).willReturn(3L);
  }

  @Test
  public void createLevel() {
    TrainingDefinition trainingDefinition = new TrainingDefinition();

    exportImportService.createLevel(assessmentLevel, trainingDefinition);
    exportImportService.createLevel(trainingLevel, trainingDefinition);
    exportImportService.createLevel(infoLevel, trainingDefinition);

    then(assessmentLevelRepository).should().save(assessmentLevel);
    then(trainingLevelRepository).should().save(trainingLevel);
    then(infoLevelRepository).should().save(infoLevel);
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
