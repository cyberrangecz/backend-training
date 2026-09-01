package cz.cyberrange.platform.training.service.unit.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.dto.imports.AbstractLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.imports.InfoLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.TrainingLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.facade.ExportImportFacade;
import cz.cyberrange.platform.training.service.mapping.mapstruct.*;
import cz.cyberrange.platform.training.service.services.ExportImportService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import cz.cyberrange.platform.training.service.services.score.ScoreReportService;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    classes = {
      TestDataFactory.class,
      EnumMapperImpl.class,
      ExportImportMapperImpl.class,
      TrainingDefinitionMapperImpl.class,
      LevelMapperImpl.class,
      UserRefMapperImpl.class,
      BetaTestingGroupMapperImpl.class,
      HintMapperImpl.class,
      QuestionMapperImpl.class,
      AttachmentMapperImpl.class,
      MitreTechniqueMapperImpl.class
    })
public class ExportImportFacadeTest {

  private ExportImportFacade exportImportFacade;

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private ExportImportMapperImpl exportImportMapper;
  @Autowired private LevelMapperImpl infoLevelMapper;
  @Autowired private TrainingDefinitionMapperImpl trainingDefinitionMapper;

  @MockBean private ObjectMapper objectMapper;
  @MockBean private TrainingDefinitionService trainingDefinitionService;
  @MockBean private SandboxApiService sandboxApiService;
  @MockBean private ExportImportService exportImportService;
  @MockBean private UserService userService;
  @MockBean private CommandEventsService commandEventsService;
  @MockBean private TrainingEventsService trainingEventsService;
  @MockBean private EventMapper eventMapper;
  @MockBean private ScoreReportService scoreReportService;

  private TrainingDefinition trainingDefinition;
  private TrainingDefinition trainingDefinitionImported;
  private AssessmentLevel assessmentLevel;
  private TrainingLevel trainingLevel;
  private InfoLevel infoLevel;
  private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
    exportImportFacade =
        new ExportImportFacade(
            exportImportService,
            trainingDefinitionService,
            sandboxApiService,
            userService,
            exportImportMapper,
            infoLevelMapper,
            trainingDefinitionMapper,
            objectMapper,
            commandEventsService,
            trainingEventsService,
            eventMapper,
            scoreReportService);

    assessmentLevel = testDataFactory.getTest();
    assessmentLevel.setId(1L);

    trainingLevel = testDataFactory.getPenalizedLevel();
    trainingLevel.setId(2L);

    infoLevel = testDataFactory.getInfoLevel1();
    infoLevel.setId(3L);

    AssessmentLevelImportDTO importAssessmentLevelDTO =
        testDataFactory.getAssessmentLevelImportDTO();

    TrainingLevelImportDTO importGameLevelDTO = testDataFactory.getTrainingLevelImportDTO();

    InfoLevelImportDTO importInfoLevelDTO = testDataFactory.getInfoLevelImportDTO();

    trainingDefinition = testDataFactory.getReleasedDefinition();
    trainingDefinition.setId(1L);

    trainingDefinitionImported = testDataFactory.getUnreleasedDefinition();
    trainingDefinitionImported.setId(1L);

    importTrainingDefinitionDTO = testDataFactory.getImportTrainingDefinitionDTO();
    importTrainingDefinitionDTO.setLevels(
        Arrays.asList(importInfoLevelDTO, importGameLevelDTO, importAssessmentLevelDTO));

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
    given(trainingDefinitionService.findLevelById(assessmentLevel.getId()))
        .willReturn(assessmentLevel);
    ExportTrainingDefinitionAndLevelsDTO exportedTrainingDefinition =
        exportImportMapper.mapToDTO(trainingDefinition);
    given(objectMapper.writeValueAsBytes(any(ExportTrainingDefinitionAndLevelsDTO.class)))
        .willReturn(convertObjectToJsonBytes(exportedTrainingDefinition));
    FileToReturnDTO export = exportImportFacade.dbExport(trainingDefinition.getId());

    assertEquals(
        exportedTrainingDefinition.toString(),
        convertJsonBytesToString(export.getContent()).toString());
    assertEquals(trainingDefinition.getTitle(), export.getTitle());
  }

  private static ExportTrainingDefinitionAndLevelsDTO convertJsonBytesToString(byte[] object)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(object, ExportTrainingDefinitionAndLevelsDTO.class);
  }

  private static byte[] convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsBytes(object);
  }

  @Test
  public void dbImport() {
    given(trainingDefinitionService.create(any(TrainingDefinition.class), any(Boolean.class)))
        .willReturn(trainingDefinitionImported);

    TrainingDefinitionWithLevelsDTO trainingDefinitionWithLevelsDTO =
        exportImportFacade.dbImport(importTrainingDefinitionDTO);
    TrainingDefinitionWithLevelsDTO trainingDefinitionWithLevelsDTOImported =
        trainingDefinitionMapper.mapToDTOWithLevels(
            trainingDefinitionImported, trainingDefinitionWithLevelsDTO.getLevels());

    deepEqualsTrainingDefinitionDTO(
        trainingDefinitionWithLevelsDTOImported, trainingDefinitionWithLevelsDTO);
    assertEquals(
        importTrainingDefinitionDTO.getLevels().stream()
            .map(AbstractLevelImportDTO::getLevelType)
            .collect(Collectors.toList()),
        trainingDefinitionWithLevelsDTO.getLevels().stream()
            .map(AbstractLevelBasicDTO::getLevelType)
            .collect(Collectors.toList()));
  }

  private void deepEqualsTrainingDefinitionDTO(
      TrainingDefinitionWithLevelsDTO t1, TrainingDefinitionWithLevelsDTO t2) {
    assertEquals(t1.getId(), t2.getId());
    assertEquals(t1.getState(), t2.getState());
    assertEquals(t1.getDescription(), t2.getDescription());
    assertEquals(t1.getTitle(), t2.getTitle());
    assertEquals(t1.getBetaTestingGroupId(), t2.getBetaTestingGroupId());
    assertEquals(t1.getLevels(), t2.getLevels());
  }
}
