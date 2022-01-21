package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.BDDMockito.*;


@SpringBootTest(classes = {TestDataFactory.class})
public class ExportImportServiceTest {

    private static ExportImportService exportImportService;
    @MockBean
    private static TrainingDefinitionRepository trainingDefinitionRepository;
    @MockBean
    private static AbstractLevelRepository abstractLevelRepository;
    @MockBean
    private static AssessmentLevelRepository assessmentLevelRepository;
    @MockBean
    private static QuestionAnswerRepository questionAnswerRepository;
    @MockBean
    private static InfoLevelRepository infoLevelRepository;
    @MockBean
    private static TrainingLevelRepository trainingLevelRepository;
    @MockBean
    private static TrainingInstanceRepository trainingInstanceRepository;
    @MockBean
    private static TrainingRunRepository trainingRunRepository;

    @Mock
    private static AssessmentLevel assessmentLevel;
    @Mock
    private static TrainingLevel trainingLevel;
    @Mock
    private static InfoLevel infoLevel;
    @Mock
    private static TrainingInstance trainingInstance;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        exportImportService = new ExportImportService(trainingDefinitionRepository, abstractLevelRepository, assessmentLevelRepository,
                questionAnswerRepository, infoLevelRepository, trainingLevelRepository, trainingInstanceRepository, trainingRunRepository);

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
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance));
        given(trainingInstance.getId()).willReturn(1L);
        TrainingInstance tI = exportImportService.findInstanceById(trainingInstance.getId());

        then(trainingInstanceRepository).should().findById(trainingInstance.getId());
    }

    @Test
    public void test(){
        String pass = "pass-0221";
        String shortPass = pass.substring(0, pass.length() - 5);
        String pin = pass.substring(pass.length() -4, pass.length() );
        System.out.println("short:"  +shortPass);
        System.out.println("pin: " + pin);
    }

}
