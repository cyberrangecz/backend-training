package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.impl.ExportImportServiceImpl;
import cz.muni.ics.kypo.training.utils.AssessmentUtil;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;

/**
 * @author Boris Jadus(445343)
 */

@RunWith(SpringRunner.class)
public class ExportImportServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static ExportImportService exportImportService;

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
        exportImportService = new ExportImportServiceImpl(trainingDefinitionRepository, abstractLevelRepository, assessmentLevelRepository,
                infoLevelRepository, gameLevelRepository, trainingInstanceRepository, trainingRunRepository);

        given(assessmentLevel.getId()).willReturn(1L);
        given(assessmentLevel.getQuestions()).willReturn("[]");
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
    public void failIfInstanceNotFinished() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("The training instance is not finished.");
        exportImportService.failIfInstanceIsNotFinished(LocalDateTime.now().plusHours(25));
    }

}
