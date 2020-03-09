package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.GameLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.imports.InfoLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.UserService;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, ExportImportMapperImpl.class, TrainingDefinitionMapperImpl.class,
        GameLevelMapperImpl.class, InfoLevelMapperImpl.class, AssessmentLevelMapperImpl.class,
        UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class, HintMapperImpl.class})
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
    @Autowired
    private UserRefMapper userRefMapper;


    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TrainingDefinitionService trainingDefinitionService;
    @Mock
    private ExportImportService exportImportService;
    @Mock
    private UserService userService;

    private TrainingDefinition trainingDefinition;
    private TrainingDefinition trainingDefinitionImported;
    private AssessmentLevel assessmentLevel;
    private GameLevel gameLevel;
    private InfoLevel infoLevel;
    private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;
    private InfoLevelImportDTO importInfoLevelDTO;
    private AssessmentLevelImportDTO importAssessmentLevelDTO;
    private GameLevelImportDTO importGameLevelDTO;
    private TrainingInstance trainingInstance;
    private TrainingRun trainingRun;
    private TrainingEventsService trainingEventsService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        exportImportFacade = new ExportImportFacadeImpl(exportImportService, exportImportMapper, gameLevelMapper,
                infoLevelMapper, assessmentLevelMapper, trainingDefinitionService, trainingDefinitionMapper, objectMapper, trainingEventsService, userService, userRefMapper);
        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(1L);
        assessmentLevel.setTitle("Assessment title");

        gameLevel = new GameLevel();
        gameLevel.setId(2L);
        gameLevel.setSolution("solution");

        infoLevel = new InfoLevel();
        infoLevel.setId(3L);

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

        trainingDefinitionImported = new TrainingDefinition();
        trainingDefinitionImported.setId(1L);
        trainingDefinitionImported.setTitle("Uploaded " + "Training definition");
        trainingDefinitionImported.setDescription("description");
        trainingDefinitionImported.setState(TDState.UNRELEASED);

        importTrainingDefinitionDTO = new ImportTrainingDefinitionDTO();
        importTrainingDefinitionDTO.setTitle("Training definition");
        importTrainingDefinitionDTO.setDescription("description");
        importTrainingDefinitionDTO.setState(cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        importTrainingDefinitionDTO.setLevels(Arrays.asList(importInfoLevelDTO, importGameLevelDTO, importAssessmentLevelDTO));

        trainingInstance = new TrainingInstance();
        trainingInstance.setAccessToken("pass-1234");
        trainingInstance.setTrainingDefinition(trainingDefinition);
        trainingInstance.setPoolSize(10);
        trainingInstance.setTitle("title");
        LocalDateTime time = LocalDateTime.now();
        trainingInstance.setEndTime(time.minusHours(10));
        trainingInstance.setStartTime(time.minusHours(20));

        trainingRun = new TrainingRun();
        trainingRun.setTrainingInstance(trainingInstance);
        trainingRun.setEndTime(time.minusHours(10));
        trainingRun.setStartTime(time.minusHours(20));
        trainingRun.setState(TRState.RUNNING);
    }

	@Test
    public void dbExport() throws Exception {
        given(exportImportService.findById(trainingDefinition.getId())).willReturn(trainingDefinition);
        given(trainingDefinitionService.findLevelById(infoLevel.getId())).willReturn(infoLevel);
        given(trainingDefinitionService.findLevelById(gameLevel.getId())).willReturn(gameLevel);
        given(trainingDefinitionService.findLevelById(assessmentLevel.getId())).willReturn(assessmentLevel);
        ExportTrainingDefinitionAndLevelsDTO exportedTrainingDefinition = exportImportMapper.mapToDTO(trainingDefinition);
        given(objectMapper.writeValueAsBytes(any(ExportTrainingDefinitionAndLevelsDTO.class))).willReturn(convertObjectToJsonBytes(exportedTrainingDefinition));
        FileToReturnDTO export = exportImportFacade.dbExport(trainingDefinition.getId());

        assertEquals(exportedTrainingDefinition.toString(), convertJsonBytesToString(export.getContent()).toString());
        assertEquals(trainingDefinition.getTitle(), export.getTitle());
    }

    private static ExportTrainingDefinitionAndLevelsDTO convertJsonBytesToString(byte[] object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, ExportTrainingDefinitionAndLevelsDTO.class);
    }

    private static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(object);
    }

    @Test
    public void dbImport() {
        given(trainingDefinitionService.create(any(TrainingDefinition.class))).willReturn(trainingDefinitionImported);

        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = exportImportFacade.dbImport(importTrainingDefinitionDTO);
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTOImported = trainingDefinitionMapper.mapToDTOById(trainingDefinitionImported);

        deepEqualsTrainingDefinitionDTO(trainingDefinitionByIdDTOImported, trainingDefinitionByIdDTO);
    }

    private void deepEqualsTrainingDefinitionDTO(TrainingDefinitionByIdDTO t1, TrainingDefinitionByIdDTO t2) {
        assertEquals(t1.getId(), t2.getId());
        assertEquals(t1.getState(), t2.getState());
        assertEquals(t1.getDescription(), t2.getDescription());
        assertEquals(t1.getTitle(), t2.getTitle());
        assertEquals(t1.getBetaTestingGroupId(), t2.getBetaTestingGroupId());
        assertEquals(t1.getLevels(), t2.getLevels());
    }

}
