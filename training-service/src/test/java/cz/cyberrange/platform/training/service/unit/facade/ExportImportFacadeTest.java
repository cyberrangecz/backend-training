package cz.cyberrange.platform.training.service.unit.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.attachment.TrainingMaterialDTO;
import cz.cyberrange.platform.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.dto.imports.AbstractLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.imports.InfoLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.TrainingLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.export.JsonExportFormat;
import cz.cyberrange.platform.training.service.facade.ExportImportFacade;
import cz.cyberrange.platform.training.service.mapping.mapstruct.*;
import cz.cyberrange.platform.training.service.services.ExportImportService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import cz.cyberrange.platform.training.service.services.attachment.AttachmentHashes;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionArchive;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveReader;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionArchiveWriter;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionAttachmentReader;
import cz.cyberrange.platform.training.service.services.attachment.DefinitionAttachmentService;
import cz.cyberrange.platform.training.service.services.attachment.ExportedAttachment;
import cz.cyberrange.platform.training.service.services.attachment.SuppliedAttachment;
import cz.cyberrange.platform.training.service.services.score.ScoreReportService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {
      TestDataFactory.class,
      EnumMapperImpl.class,
      ExportImportMapperImpl.class,
      TrainingDefinitionMapperImpl.class,
      LevelMapperImpl.class,
      HintMapperImpl.class,
      QuestionMapperImpl.class,
      MitreTechniqueMapperImpl.class
    })
public class ExportImportFacadeTest {

  private ExportImportFacade exportImportFacade;

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private ExportImportMapperImpl exportImportMapper;
  @Autowired private LevelMapperImpl infoLevelMapper;
  @Autowired private TrainingDefinitionMapperImpl trainingDefinitionMapper;

  @MockitoBean private JsonMapper objectMapper;
  @MockitoBean private TrainingDefinitionService trainingDefinitionService;
  @MockitoBean private SandboxApiService sandboxApiService;
  @MockitoBean private ExportImportService exportImportService;
  @MockitoBean private UserService userService;
  @MockitoBean private CommandEventsService commandEventsService;
  @MockitoBean private TrainingEventsService trainingEventsService;
  @MockitoBean private EventMapper eventMapper;
  @MockitoBean private ScoreReportService scoreReportService;
  @MockitoBean private DefinitionAttachmentReader definitionAttachmentReader;
  @MockitoBean private DefinitionAttachmentService definitionAttachmentService;
  @MockitoBean private DefinitionArchiveWriter definitionArchiveWriter;
  @MockitoBean private DefinitionArchiveReader definitionArchiveReader;

  private TrainingDefinition trainingDefinition;
  private TrainingDefinition trainingDefinitionImported;
  private AssessmentLevel assessmentLevel;
  private TrainingLevel trainingLevel;
  private InfoLevel infoLevel;
  private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;

  @Captor private ArgumentCaptor<TrainingDefinition> definitionCaptor;
  @Captor private ArgumentCaptor<List<AbstractLevel>> levelsCaptor;
  @Captor private ArgumentCaptor<Collection<TrainingMaterialDTO>> materialsCaptor;
  @Captor private ArgumentCaptor<Collection<SuppliedAttachment>> suppliedFilesCaptor;
  private AutoCloseable mocks;

  @BeforeEach
  public void init() {
    mocks = MockitoAnnotations.openMocks(this);
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
            scoreReportService,
            definitionAttachmentReader,
            definitionAttachmentService,
            definitionArchiveWriter,
            definitionArchiveReader);

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

  @AfterEach
  public void closeMocks() throws Exception {
    mocks.close();
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
    FileToReturnDTO export =
        exportImportFacade.dbExport(trainingDefinition.getId(), new JsonExportFormat(objectMapper));

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
    given(trainingDefinitionService.create(any(TrainingDefinition.class), anyList()))
        .willReturn(trainingDefinitionImported);

    TrainingDefinitionWithLevelsDTO trainingDefinitionWithLevelsDTO =
        exportImportFacade.dbImport(importTrainingDefinitionDTO, false);
    assertEquals(0L, trainingDefinitionWithLevelsDTO.getInstanceCount());
    assertTrue(trainingDefinitionWithLevelsDTO.isCanBeArchived());
    then(trainingDefinitionService).should(never()).completeInstanceCounts(any());
    TrainingDefinitionWithLevelsDTO trainingDefinitionWithLevelsDTOImported =
        trainingDefinitionMapper.mapToDTOWithLevels(
            trainingDefinitionImported, trainingDefinitionWithLevelsDTO.getLevels());

    then(trainingDefinitionService)
        .should()
        .create(definitionCaptor.capture(), levelsCaptor.capture());
    assertEquals(TDState.UNRELEASED, definitionCaptor.getValue().getState());
    assertEquals(
        List.of(InfoLevel.class, TrainingLevel.class, AssessmentLevel.class),
        levelsCaptor.getValue().stream().map(Object::getClass).collect(Collectors.toList()));
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

  @Test
  public void dbExportOfADefinitionWithoutFilesIsASingleDocument() throws Exception {
    given(exportImportService.findById(trainingDefinition.getId())).willReturn(trainingDefinition);
    given(objectMapper.writeValueAsBytes(any(ExportTrainingDefinitionAndLevelsDTO.class)))
        .willReturn(new byte[] {1});

    FileToReturnDTO export =
        exportImportFacade.dbExport(trainingDefinition.getId(), new JsonExportFormat(objectMapper));

    assertFalse(export.isArchive());
    assertEquals(trainingDefinition.getTitle(), export.getTitle());
    then(definitionArchiveWriter).shouldHaveNoInteractions();
  }

  @Test
  public void dbExportOfADefinitionWithFilesIsAnArchiveNamedAfterTheCleanedTitle()
      throws Exception {
    ExportedAttachment file =
        new ExportedAttachment(
            "a".repeat(64), "plan.txt", "text/plain", new byte[] {1}, null, null, null, null);
    byte[] archive = {9, 9};
    JsonExportFormat format = new JsonExportFormat(objectMapper);
    given(exportImportService.findById(trainingDefinition.getId())).willReturn(trainingDefinition);
    given(definitionAttachmentReader.readAllForExport(trainingDefinition.getId()))
        .willReturn(List.of(file));
    given(objectMapper.writeValueAsBytes(any(ExportTrainingDefinitionAndLevelsDTO.class)))
        .willReturn(new byte[] {1});
    given(definitionArchiveWriter.archiveNameOf(trainingDefinition.getTitle())).willReturn("clean");
    given(definitionArchiveWriter.write(eq("clean"), eq(format), any(), eq(List.of(file))))
        .willReturn(archive);

    FileToReturnDTO export = exportImportFacade.dbExport(trainingDefinition.getId(), format);

    assertTrue(export.isArchive());
    assertEquals("clean", export.getTitle());
    assertArrayEquals(archive, export.getContent());
  }

  @Test
  public void dbImportOfADocumentRequiresEveryReferenceToBeSuppliedAndSuppliesNone() {
    given(trainingDefinitionService.create(any(TrainingDefinition.class), anyList()))
        .willReturn(trainingDefinitionImported);

    exportImportFacade.dbImport(importTrainingDefinitionDTO, false);

    then(definitionAttachmentService).should().requireReferencesSupplied(anyList(), eq(List.of()), eq(false));
  }

  @Test
  public void dbImportOfADocumentPassesTheKeepFlagToTheReferenceCheckAndTheLinkSynchronization() {
    given(trainingDefinitionService.create(any(TrainingDefinition.class), anyList()))
        .willReturn(trainingDefinitionImported);

    exportImportFacade.dbImport(importTrainingDefinitionDTO, true);

    then(definitionAttachmentService)
        .should()
        .requireReferencesSupplied(anyList(), eq(List.of()), eq(true));
    then(definitionAttachmentService)
        .should()
        .synchronizeLinks(
            eq(trainingDefinitionImported), anyCollection(), eq(List.of()), eq(true));
  }

  @Test
  public void dbImportArchivePassesTheKeepFlagToTheReferenceCheck() {
    byte[] archive = {1, 2, 3};
    given(definitionArchiveReader.read(archive))
        .willReturn(new DefinitionArchive(importTrainingDefinitionDTO, List.of()));
    given(trainingDefinitionService.create(any(TrainingDefinition.class), anyList()))
        .willReturn(trainingDefinitionImported);

    exportImportFacade.dbImportArchive(archive, true);

    then(definitionAttachmentService)
        .should()
        .requireReferencesSupplied(anyList(), anyCollection(), eq(true));
  }

  @Test
  public void dbImportOfADocumentWithAReferenceToAMissingFileIsRefusedBeforeCreating() {
    doThrow(new BadRequestException("missing"))
        .when(definitionAttachmentService)
        .requireReferencesSupplied(anyList(), anyCollection(), eq(false));

    assertThrows(
        BadRequestException.class, () -> exportImportFacade.dbImport(importTrainingDefinitionDTO, false));

    then(trainingDefinitionService).should(never()).create(any(), anyList());
  }

  @Test
  public void dbImportArchiveLinksTheArchiveFilesToTheCreatedDefinition() {
    byte[] archive = {1, 2, 3};
    byte[] content = "plan".getBytes(StandardCharsets.UTF_8);
    SuppliedAttachment file = SuppliedAttachment.of("plan.txt", content);
    given(definitionArchiveReader.read(archive))
        .willReturn(new DefinitionArchive(importTrainingDefinitionDTO, List.of(file)));
    given(trainingDefinitionService.create(any(TrainingDefinition.class), anyList()))
        .willReturn(trainingDefinitionImported);

    exportImportFacade.dbImportArchive(archive, false);

    then(definitionAttachmentService)
        .should()
        .synchronizeLinks(
            eq(trainingDefinitionImported),
            materialsCaptor.capture(),
            suppliedFilesCaptor.capture(),
            eq(false));
    assertEquals(
        List.of(new TrainingMaterialDTO(AttachmentHashes.sha256Hex(content), "plan.txt")),
        List.copyOf(materialsCaptor.getValue()));
    assertEquals(List.of(file), List.copyOf(suppliedFilesCaptor.getValue()));
  }

  @Test
  public void dbImportArchiveRefusedByTheReaderCreatesNothing() {
    byte[] archive = {1};
    given(definitionArchiveReader.read(archive)).willThrow(new BadRequestException("broken"));

    assertThrows(BadRequestException.class, () -> exportImportFacade.dbImportArchive(archive, false));

    then(trainingDefinitionService).should(never()).create(any(), anyList());
  }

  private void deepEqualsTrainingDefinitionDTO(
      TrainingDefinitionWithLevelsDTO t1, TrainingDefinitionWithLevelsDTO t2) {
    assertEquals(t1.getId(), t2.getId());
    assertEquals(t1.getState(), t2.getState());
    assertEquals(t1.getDescription(), t2.getDescription());
    assertEquals(t1.getTitle(), t2.getTitle());
    assertEquals(t1.getLevels(), t2.getLevels());
  }
}
