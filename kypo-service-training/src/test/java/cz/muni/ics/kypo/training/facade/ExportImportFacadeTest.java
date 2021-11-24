package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.TrainingLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.imports.InfoLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.api.SandboxApiService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
@SpringBootTest(classes = {LevelMapperImpl.class, ExportImportMapperImpl.class, TrainingDefinitionMapperImpl.class,
        LevelMapperImpl.class, UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class, HintMapperImpl.class,
        QuestionMapperImpl.class, AttachmentMapperImpl.class, ReferenceSolutionNodeMapperImpl.class})
public class ExportImportFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ExportImportFacade exportImportFacade;

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private ExportImportMapperImpl exportImportMapper;
    @Autowired
    private LevelMapperImpl infoLevelMapper;
    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TrainingDefinitionService trainingDefinitionService;
    @Mock
    private SandboxApiService sandboxApiService;
    @Mock
    private ExportImportService exportImportService;
    @Mock
    private TrainingFeedbackApiService trainingFeedbackApiService;

    private TrainingDefinition trainingDefinition;
    private TrainingDefinition trainingDefinitionImported;
    private AssessmentLevel assessmentLevel;
    private TrainingLevel trainingLevel;
    private InfoLevel infoLevel;
    private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;
    private ElasticsearchApiService elasticsearchApiService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        exportImportFacade = new ExportImportFacade(exportImportService, trainingDefinitionService, elasticsearchApiService,
                trainingFeedbackApiService, sandboxApiService, exportImportMapper, infoLevelMapper, trainingDefinitionMapper, objectMapper);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(1L);

        trainingLevel = testDataFactory.getPenalizedLevel();
        trainingLevel.setId(2L);

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(3L);

        AssessmentLevelImportDTO importAssessmentLevelDTO = testDataFactory.getAssessmentLevelImportDTO();
        importAssessmentLevelDTO.setOrder(3);

        TrainingLevelImportDTO importGameLevelDTO = testDataFactory.getTrainingLevelImportDTO();
        importGameLevelDTO.setOrder(2);

        InfoLevelImportDTO importInfoLevelDTO = testDataFactory.getInfoLevelImportDTO();
        importInfoLevelDTO.setOrder(1);

        trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);

        trainingDefinitionImported = testDataFactory.getUnreleasedDefinition();
        trainingDefinitionImported.setId(1L);

        importTrainingDefinitionDTO = testDataFactory.getImportTrainingDefinitionDTO();
        importTrainingDefinitionDTO.setLevels(Arrays.asList(importInfoLevelDTO, importGameLevelDTO, importAssessmentLevelDTO));

        TrainingInstance trainingInstance = testDataFactory.getConcludedInstance();
        trainingInstance.setTrainingDefinition(trainingDefinition);

        TrainingRun trainingRun = testDataFactory.getFinishedRun();
        trainingRun.setTrainingInstance(trainingInstance);
    }

	@Test
    public void dbExport() throws Exception {
        given(exportImportService.findById(trainingDefinition.getId())).willReturn(trainingDefinition);
        given(trainingDefinitionService.findLevelById(infoLevel.getId())).willReturn(infoLevel);
        given(trainingDefinitionService.findLevelById(trainingLevel.getId())).willReturn(trainingLevel);
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
        given(trainingDefinitionService.create(any(TrainingDefinition.class), any(Boolean.class))).willReturn(trainingDefinitionImported);

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
