package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.GameLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.imports.InfoLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, ExportImportMapperImpl.class, TrainingDefinitionMapperImpl.class,
        GameLevelMapperImpl.class, InfoLevelMapperImpl.class, AssessmentLevelMapperImpl.class, SnapshotHookMapperImpl.class,
        UserRefMapperImpl.class, TDViewGroupMapperImpl.class, HintMapperImpl.class})
public class ExportImportFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ExportImportFacade exportImportFacade;

    @Autowired
    private ExportImportMapperImpl exportImportMapper;
    @Autowired
    private GameLevelMapperImpl gameLevelMapper;
    @Autowired
    private InfoLevelMapperImpl infoLevelMapper;
    @Autowired
    private AssessmentLevelMapperImpl assessmentLevelMapper;
    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;

    @Mock
    private TrainingDefinitionService trainingDefinitionService;
    @Mock
    private ExportImportService exportImportService;

    private TrainingDefinition trainingDefinition;
    private AssessmentLevel assessmentLevel;
    private GameLevel gameLevel;
    private InfoLevel infoLevel;
    private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;
    private InfoLevelImportDTO importInfoLevelDTO;
    private AssessmentLevelImportDTO importAssessmentLevelDTO;
    private GameLevelImportDTO importGameLevelDTO;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        exportImportFacade = new ExportImportFacadeImpl(exportImportService, exportImportMapper, gameLevelMapper,
                infoLevelMapper, assessmentLevelMapper, trainingDefinitionService, trainingDefinitionMapper);

        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(1L);
        assessmentLevel.setNextLevel(null);
        assessmentLevel.setTitle("Assessment title");

        gameLevel = new GameLevel();
        gameLevel.setId(2L);
        gameLevel.setNextLevel(null);
        gameLevel.setSolution("solution");
        gameLevel.setNextLevel(1L);

        infoLevel = new InfoLevel();
        infoLevel.setId(3L);
        infoLevel.setNextLevel(gameLevel.getId());

        importAssessmentLevelDTO = new AssessmentLevelImportDTO();
        importAssessmentLevelDTO.setTitle("Assessment title");
        importAssessmentLevelDTO.setInstructions("intructions");
        importAssessmentLevelDTO.setQuestions("questions");
        importAssessmentLevelDTO.setAssessmentType(AssessmentType.TEST);
        importAssessmentLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        importAssessmentLevelDTO.setMaxScore(100);
        importAssessmentLevelDTO.setOrder(3);

        importGameLevelDTO = new GameLevelImportDTO();
        importGameLevelDTO.setTitle("Game title");
        importGameLevelDTO.setFlag("flag");
        importGameLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        importGameLevelDTO.setContent("game level content here");
        importGameLevelDTO.setSolution("port 5050");
        importGameLevelDTO.setIncorrectFlagLimit(5);
        importGameLevelDTO.setOrder(2);

        importInfoLevelDTO = new InfoLevelImportDTO();
        importInfoLevelDTO.setTitle("Info level title");
        importInfoLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        importInfoLevelDTO.setOrder(1);
        importInfoLevelDTO.setMaxScore(0);

        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setId(1L);
        trainingDefinition.setTitle("Training definition");
        trainingDefinition.setDescription("description");
        trainingDefinition.setState(TDState.RELEASED);
        trainingDefinition.setStartingLevel(infoLevel.getId());

        importTrainingDefinitionDTO = new ImportTrainingDefinitionDTO();
        importTrainingDefinitionDTO.setTitle("Training definition");
        importTrainingDefinitionDTO.setDescription("description");
        importTrainingDefinitionDTO.setState(cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        importTrainingDefinitionDTO.setLevels(Arrays.asList(importInfoLevelDTO, importGameLevelDTO, importAssessmentLevelDTO));
        importTrainingDefinitionDTO.setStartingLevel(infoLevel.getId());
    }
    //TODO write better tests to check all attributes
    @Test
    public void dbExport() {
        given(exportImportService.findById(trainingDefinition.getId())).willReturn(trainingDefinition);
        given(trainingDefinitionService.findLevelById(infoLevel.getId())).willReturn(infoLevel);
        given(trainingDefinitionService.findLevelById(gameLevel.getId())).willReturn(gameLevel);
        given(trainingDefinitionService.findLevelById(assessmentLevel.getId())).willReturn(assessmentLevel);
        ExportTrainingDefinitionAndLevelsDTO export = exportImportFacade.dbExport(trainingDefinition.getId());

        assertEquals(trainingDefinition.getStartingLevel(), export.getStartingLevel());
        assertEquals(trainingDefinition.getTitle(), export.getTitle());
        assertEquals(trainingDefinition.getState().toString(), export.getState().toString());

        assertEquals(LevelType.INFO_LEVEL, export.getLevels().get(0).getLevelType());
        assertEquals(LevelType.GAME_LEVEL, export.getLevels().get(1).getLevelType());
        assertEquals(LevelType.ASSESSMENT_LEVEL, export.getLevels().get(2).getLevelType());

    }

    @Test
    public void dbImport() {
        given(exportImportService.createLevel(infoLevelMapper.mapImportToEntity(importInfoLevelDTO))).willReturn(3L);
        given(exportImportService.createLevel(gameLevelMapper.mapImportToEntity(importGameLevelDTO))).willReturn(2L);
        given(exportImportService.createLevel(assessmentLevelMapper.mapImportToEntity(importAssessmentLevelDTO))).willReturn(1L);
        given(trainingDefinitionService.create(exportImportMapper.mapToEntity(importTrainingDefinitionDTO))).willReturn(trainingDefinition);

        TrainingDefinitionDTO trainingDefinitionDTO = exportImportFacade.dbImport(importTrainingDefinitionDTO);

        deepEqualsTrainingDefinitionDTO(trainingDefinitionMapper.mapToDTO(trainingDefinition), trainingDefinitionDTO);


    }

    private void deepEqualsTrainingDefinitionDTO(TrainingDefinitionDTO t1, TrainingDefinitionDTO t2) {
        assertEquals(t1.getId(), t2.getId());
        assertEquals(t1.getSandboxDefinitionRefId(), t2.getSandboxDefinitionRefId());
        assertEquals(t1.getAuthors(), t2.getAuthors());
        assertEquals(t1.getState(), t2.getState());
        assertEquals(t1.getDescription(), t2.getDescription());
        assertEquals(t1.getStartingLevel(), t2.getStartingLevel());
        assertEquals(t1.getTitle(), t2.getTitle());
        assertEquals(t1.getTdViewGroup(), t2.getTdViewGroup());
        assertEquals(t1.getLevels(), t2.getLevels());


    }

}
