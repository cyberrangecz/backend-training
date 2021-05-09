package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.repository.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.BDDMockito.*;


@RunWith(SpringRunner.class)
public class ExportImportServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static ExportImportService exportImportService;

    @Mock
    private static WebClient pythonWebClient;
    @Mock
    private static TrainingDefinitionRepository trainingDefinitionRepository;
    @Mock
    private static AbstractLevelRepository abstractLevelRepository;
    @Mock
    private static AssessmentLevelRepository assessmentLevelRepository;
    @Mock
    private static InfoLevelRepository infoLevelRepository;
    @Mock
    private static GameLevelRepository gameLevelRepository;
    @Mock
    private static TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    private static TrainingRunRepository trainingRunRepository;

    @Mock
    private static AssessmentLevel assessmentLevel;
    @Mock
    private static GameLevel gameLevel;
    @Mock
    private static InfoLevel infoLevel;
    @Mock
    private static TrainingInstance trainingInstance;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        exportImportService = new ExportImportService(trainingDefinitionRepository, abstractLevelRepository, assessmentLevelRepository,
                infoLevelRepository, gameLevelRepository, trainingInstanceRepository, trainingRunRepository, pythonWebClient);

        given(assessmentLevel.getId()).willReturn(1L);
        given(assessmentLevel.getQuestions()).willReturn(new ArrayList<>());
        given(gameLevel.getId()).willReturn(2L);
        given(infoLevel.getId()).willReturn(3L);

    }

    @Test
    public void createLevel() {
        TrainingDefinition trainingDefinition = new TrainingDefinition();

        exportImportService.createLevel(assessmentLevel, trainingDefinition);
        exportImportService.createLevel(gameLevel, trainingDefinition);
        exportImportService.createLevel(infoLevel, trainingDefinition);

        then(assessmentLevelRepository).should().save(assessmentLevel);
        then(gameLevelRepository).should().save(gameLevel);
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
