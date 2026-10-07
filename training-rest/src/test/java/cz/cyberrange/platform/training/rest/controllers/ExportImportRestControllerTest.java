package cz.cyberrange.platform.training.rest.controllers;

import static cz.cyberrange.platform.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.cyberrange.platform.training.api.converters.LocalDateTimeUTCSerializer;
import cz.cyberrange.platform.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.cyberrange.platform.training.api.dto.export.FileToReturnDTO;
import cz.cyberrange.platform.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.imports.InfoLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.TrainingLevelImportDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.rest.utils.ZipMediaType;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.error.ApiErrorResponder;
import cz.cyberrange.platform.training.rest.utils.error.CustomRestExceptionHandlerTraining;
import cz.cyberrange.platform.training.rest.utils.error.ImportedFileErrorAdvice;
import cz.cyberrange.platform.training.rest.utils.error.ImportedFileErrorDescriber;
import cz.cyberrange.platform.training.service.export.ExportFormats;
import cz.cyberrange.platform.training.service.export.JsonExportFormat;
import cz.cyberrange.platform.training.service.export.YamlExportFormat;
import cz.cyberrange.platform.training.service.facade.ExportImportFacade;
import cz.cyberrange.platform.training.service.facade.TrainingDefinitionFacade;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

@SpringBootTest(classes = TestDataFactory.class)
public class ExportImportRestControllerTest {

  private ExportImportRestController exportImportRestController;
  @Autowired private TestDataFactory testDataFactory;
  @MockitoBean private ObjectMapper objectMapper;
  @MockitoBean private ExportImportFacade exportImportFacade;

  private MockMvc mockMvc;
  private AutoCloseable closeable;

  private TrainingInstanceArchiveDTO trainingInstanceArchiveDTO;
  private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;

  @BeforeEach
  public void init() {
    JsonMapper objectMapper =
        JsonMapper.builderWithJackson2Defaults()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
            .addModule(new SimpleModule().addSerializer(new LocalDateTimeUTCSerializer()))
            .build();

    closeable = MockitoAnnotations.openMocks(this);
    exportImportRestController =
        new ExportImportRestController(
            exportImportFacade,
            new ExportFormats(
                new JsonExportFormat(objectMapper),
                new YamlExportFormat(YAMLMapper.builder().build())));
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(exportImportRestController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new QuerydslPredicateArgumentResolver(
                    new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE),
                    Optional.empty()))
            .setMessageConverters(
                new JacksonJsonHttpMessageConverter(objectMapper),
                new ByteArrayHttpMessageConverter())
            .setControllerAdvice(
                new ImportedFileErrorAdvice(
                    new ImportedFileErrorDescriber(objectMapper), new ApiErrorResponder()),
                new CustomRestExceptionHandlerTraining())
            .build();

    trainingInstanceArchiveDTO = testDataFactory.getTrainingInstanceArchiveDTO();

    InfoLevelImportDTO infoLevelImportDTO = testDataFactory.getInfoLevelImportDTO();

    AssessmentLevelImportDTO assessmentLevelDTO = testDataFactory.getAssessmentLevelImportDTO();

    TrainingLevelImportDTO trainingLevelImportDTO = testDataFactory.getTrainingLevelImportDTO();

    importTrainingDefinitionDTO = testDataFactory.getImportTrainingDefinitionDTO();
    importTrainingDefinitionDTO.setLevels(
        Arrays.asList(infoLevelImportDTO, assessmentLevelDTO, trainingLevelImportDTO));
  }

  @AfterEach
  void closeService() throws Exception {
    closeable.close();
  }

  @Test
  public void archiveTrainingInstance() throws Exception {
    FileToReturnDTO file = new FileToReturnDTO();
    file.setContent(convertObjectToJsonBytes(trainingInstanceArchiveDTO).getBytes());
    file.setTitle(trainingInstanceArchiveDTO.getTitle());
    String valueTi = convertObjectToJsonBytes(trainingInstanceArchiveDTO);
    given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTi);
    given(exportImportFacade.archiveTrainingInstance(any(Long.class), any())).willReturn(file);
    mockMvc
        .perform(get("/exports/training-instances" + "/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ZipMediaType.APPLICATION_ZIP));
  }

  @Test
  public void exportTrainingDefinitionServesJsonByDefault() throws Exception {
    givenExportedDefinition();
    mockMvc
        .perform(get("/exports/training-definitions/{id}", 1L).accept(MediaType.ALL))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(header().string("Content-Disposition", endsWith(".json")));
  }

  @Test
  public void exportTrainingDefinitionServesYamlWhenAccepted() throws Exception {
    givenExportedDefinition();
    mockMvc
        .perform(
            get("/exports/training-definitions/{id}", 1L)
                .accept(MediaType.valueOf(YamlExportFormat.TEXT_YAML_VALUE)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_YAML))
        .andExpect(header().string("Content-Disposition", endsWith(".yaml")));
  }

  @Test
  public void exportTrainingDefinitionRejectsOctetStreamOnly() throws Exception {
    givenExportedDefinition();
    mockMvc
        .perform(
            get("/exports/training-definitions/{id}", 1L)
                .accept(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(status().isNotAcceptable());
  }

  private void givenExportedDefinition() {
    FileToReturnDTO file = new FileToReturnDTO();
    file.setContent(new byte[] {1});
    file.setTitle("definition");
    given(exportImportFacade.dbExport(any(Long.class), any())).willReturn(file);
  }

  @Test
  public void archiveTrainingInstanceWithFacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(exportImportFacade)
        .archiveTrainingInstance(any(Long.class), any());
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/exports/training-instances" + "/{id}", 600l))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void importTrainingDefinition() throws Exception {
    mockMvc
        .perform(
            post("/imports/training-definitions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(importTrainingDefinitionDTO)))
        .andExpect(status().isOk());
  }

  @Test
  public void importTrainingDefinitionAcceptsLevelsWithoutMinimalPossibleSolveTime()
      throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    ArrayNode levels = (ArrayNode) root.get("levels");
    ((ObjectNode) levels.get(0)).remove("minimal_possible_solve_time");
    ((ObjectNode) levels.get(1)).putNull("minimal_possible_solve_time");

    mockMvc
        .perform(
            post("/imports/training-definitions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(treeMapper.writeValueAsString(root)))
        .andExpect(status().isOk());
  }

  @Test
  public void importTrainingDefinitionAcceptsNullDescription() throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    root.putNull("description");

    mockMvc
        .perform(
            post("/imports/training-definitions")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(treeMapper.writeValueAsString(root)))
        .andExpect(status().isOk());
  }

  @Test
  public void importTrainingDefinitionRejectsNullQuestions() throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    ArrayNode levels = (ArrayNode) root.get("levels");
    ((ObjectNode) levels.get(1)).putNull("questions");

    ApiError error = performImport(treeMapper.writeValueAsString(root));

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertTrue(error.getMessage().contains("The training definition could not be imported"));
    assertTrue(error.getMessage().contains("levels[1].questions —"));
  }

  @Test
  public void importTrainingDefinitionRejectsUnrecognizedField() throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    root.put("bogus_field", "unexpected value");

    ApiError error = performImport(treeMapper.writeValueAsString(root));

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertTrue(error.getMessage().contains("The training definition could not be imported"));
    assertTrue(error.getMessage().contains("\"bogus_field\""));
    assertTrue(error.getMessage().contains("is not a recognized field"));
    assertEquals(
        Set.of("title", "description", "prerequisites", "outcomes", "levels"),
        permittedFieldsIn(error.getMessage(), "the top level of the file"),
        "the permitted-fields listing should name every field of the file and nothing else");
  }

  @Test
  public void importTrainingDefinitionRejectsMissingLevelType() throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    ((ObjectNode) root.withArray("levels").get(0)).remove("level_type");

    ApiError error = performImport(treeMapper.writeValueAsString(root));

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertTrue(error.getMessage().contains("The training definition could not be imported"));
    assertTrue(error.getMessage().contains("\"level_type\""));
    assertTrue(error.getMessage().contains("is required"));
  }

  @Test
  public void importTrainingDefinitionRejectsUnsupportedLevelType() throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    ((ObjectNode) root.withArray("levels").get(0)).put("level_type", "BOGUS_LEVEL");

    ApiError error = performImport(treeMapper.writeValueAsString(root));

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertTrue(error.getMessage().contains("The training definition could not be imported"));
    assertTrue(error.getMessage().contains("\"level_type\""));
    assertTrue(error.getMessage().contains("\"BOGUS_LEVEL\""));
    assertTrue(error.getMessage().contains("unsupported value"));
    assertTrue(
        error
            .getMessage()
            .contains("TRAINING_LEVEL, GAME_LEVEL, ACCESS_LEVEL, ASSESSMENT_LEVEL, INFO_LEVEL"));
  }

  @Test
  public void importTrainingDefinitionRejectsValueOfWrongType() throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    ((ObjectNode) root.withArray("levels").get(0)).put("estimated_duration", "not-a-number");

    ApiError error = performImport(treeMapper.writeValueAsString(root));

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertTrue(error.getMessage().contains("The training definition could not be imported"));
    assertTrue(error.getMessage().contains("\"levels[0].estimated_duration\""));
    assertTrue(error.getMessage().contains("\"not-a-number\""));
    assertTrue(error.getMessage().contains("unsupported value"));
  }

  @Test
  public void importTrainingDefinitionRejectsValueOfWrongShape() throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    root.putArray("title").add("a").add("b");

    ApiError error = performImport(treeMapper.writeValueAsString(root));

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertTrue(error.getMessage().contains("The training definition could not be imported"));
    assertTrue(error.getMessage().contains("\"title\""));
    assertTrue(error.getMessage().contains("must contain text"));
  }

  @Test
  public void importTrainingDefinitionRejectsUnparsableJson() throws Exception {
    ApiError error = performImport("this is not { json at all");

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertTrue(error.getMessage().contains("The training definition could not be imported"));
    assertTrue(error.getMessage().contains("The file is not well-formed JSON"));
    assertTrue(error.getMessage().contains("Syntax error"));
  }

  @Test
  public void importTrainingDefinitionRejectsConstraintViolation() throws Exception {
    ObjectMapper treeMapper = new ObjectMapper();
    ObjectNode root =
        (ObjectNode) treeMapper.readTree(convertObjectToJsonBytes(importTrainingDefinitionDTO));
    root.put("title", "");

    ApiError error = performImport(treeMapper.writeValueAsString(root));

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertTrue(error.getMessage().contains("The training definition could not be imported"));
    assertTrue(error.getMessage().contains("title —"));
  }

  @Test
  public void anotherControllerValidationFailureKeepsGlobalWording() throws Exception {
    TrainingDefinitionFacade otherFacade = mock(TrainingDefinitionFacade.class);
    JsonMapper localMapper =
        JsonMapper.builderWithJackson2Defaults()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .addModule(new SimpleModule().addSerializer(new LocalDateTimeUTCSerializer()))
            .build();
    TrainingDefinitionsRestController otherController =
        new TrainingDefinitionsRestController(otherFacade);
    MockMvc otherMockMvc =
        MockMvcBuilders.standaloneSetup(otherController)
            .setMessageConverters(new JacksonJsonHttpMessageConverter(localMapper))
            .setControllerAdvice(
                new ImportedFileErrorAdvice(
                    new ImportedFileErrorDescriber(localMapper), new ApiErrorResponder()),
                new CustomRestExceptionHandlerTraining())
            .build();

    MockHttpServletResponse response =
        otherMockMvc
            .perform(
                post("/training-definitions")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\"title\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse();
    ApiError error =
        convertJsonBytesToObject(
            response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8), ApiError.class);

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertFalse(
        error.getMessage().contains("The training definition could not be imported"),
        "a controller other than the import endpoint must keep the global handler's wording");
  }

  private ApiError performImport(String requestBody) throws Exception {
    MockHttpServletResponse response =
        mockMvc
            .perform(
                post("/imports/training-definitions")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(requestBody))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse();
    return convertJsonBytesToObject(
        response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8), ApiError.class);
  }

  private static Set<String> permittedFieldsIn(String message, String location) {
    String prefix = "Permitted fields in " + location + ": ";
    return message
        .lines()
        .filter(line -> line.startsWith(prefix))
        .map(line -> line.substring(prefix.length()))
        .flatMap(listing -> Arrays.stream(listing.split(", ")))
        .collect(Collectors.toSet());
  }

  private static String convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper =
        JsonMapper.builderWithJackson2Defaults()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();
    return mapper.writeValueAsString(object);
  }
}
