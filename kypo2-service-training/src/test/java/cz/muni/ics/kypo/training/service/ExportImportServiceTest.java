package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;

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
        given(assessmentLevelRepository.save(assessmentLevel)).willReturn(assessmentLevel);
        given(gameLevelRepository.save(gameLevel)).willReturn(gameLevel);
        given(infoLevelRepository.save(infoLevel)).willReturn(infoLevel);

        Long assessmentLevelId = exportImportService.createLevel(assessmentLevel);
        Long gameLevelId = exportImportService.createLevel(gameLevel);
        Long infoLevelId = exportImportService.createLevel(infoLevel);

        assertEquals(1L, (long) assessmentLevelId);
        assertEquals(2L, (long) gameLevelId);
        assertEquals(3L, (long) infoLevelId);

        then(assessmentLevelRepository).should().save(assessmentLevel);
        then(gameLevelRepository).should().save(gameLevel);
        then(infoLevelRepository).should().save(infoLevel);
    }

}
