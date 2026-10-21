package cz.cyberrange.platform.training.rest.controllers;

import static cz.cyberrange.platform.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static cz.cyberrange.platform.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.cyberrange.platform.training.api.enums.LevelType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.rest.controllers.util.ObjectConverter;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.error.CustomRestExceptionHandlerTraining;
import cz.cyberrange.platform.training.service.facade.TrainingDefinitionFacade;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.cyberrange.platform.training.service.services.UserNameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {TestDataFactory.class, EnumMapperImpl.class, TrainingDefinitionMapperImpl.class})
public class TrainingDefinitionsRestControllerTest {

  private static final long DEFINITION_ID = 4L;
  private static final UserNameFilter JANE_DOE_FILTER = new UserNameFilter(null, null, "Jane Doe");

  private TrainingDefinitionsRestController trainingDefinitionsRestController;

  @Autowired private TestDataFactory testDataFactory;
  @Autowired private TrainingDefinitionMapper trainingDefinitionMapper;

  @Mock private TrainingDefinitionFacade trainingDefinitionFacade;

  private MockMvc mockMvc;
  private AutoCloseable closeable;

  private TrainingDefinition trainingDefinition1, trainingDefinition2;
  private TrainingDefinitionWithLevelsDTO trainingDefinitionDTO1, trainingDefinitionDTO2;
  private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
  private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO;

  private AbstractLevelDTO abstractLevelDTO;
  private UserRefDTO designerDTO1, designerDTO2, organizerDTO;

  private PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionInfoDTOPageResultResource;
  private PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTOPageResultResource;

  private Page page;
  private Pageable pageable;

  @BeforeEach
  public void init() {
    JsonMapper snakeCaseMapper = ObjectConverter.restResponseMapper();

    closeable = MockitoAnnotations.openMocks(this);
    trainingDefinitionsRestController =
        new TrainingDefinitionsRestController(trainingDefinitionFacade);
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new QuerydslPredicateArgumentResolver(
                    new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE),
                    Optional.empty()))
            .setMessageConverters(new JacksonJsonHttpMessageConverter(snakeCaseMapper))
            .setControllerAdvice(new CustomRestExceptionHandlerTraining())
            .build();

    designerDTO1 = testDataFactory.getUserRefDTO1();
    designerDTO2 = testDataFactory.getUserRefDTO2();
    organizerDTO = testDataFactory.getUserRefDTO3();

    trainingDefinitionDTO1 = testDataFactory.getTrainingDefinitionWithLevelsDTO();
    trainingDefinitionDTO1.setId(1L);

    trainingDefinitionDTO2 = testDataFactory.getTrainingDefinitionWithLevelsDTO();
    trainingDefinitionDTO2.setId(2L);
    trainingDefinitionDTO2.setTitle("TDbyId2");

    InfoLevelUpdateDTO infoLevelUpdateDTO = testDataFactory.getInfoLevelUpdateDTO();
    infoLevelUpdateDTO.setLevelType(LevelType.INFO_LEVEL);
    trainingDefinitionUpdateDTO = testDataFactory.getTrainingDefinitionUpdateDTO();
    trainingDefinitionUpdateDTO.setVersion(3L);
    trainingDefinitionUpdateDTO.setLevels(List.of(infoLevelUpdateDTO));

    trainingDefinitionCreateDTO = testDataFactory.getTrainingDefinitionCreateDTO();

    abstractLevelDTO = testDataFactory.getAbstractLevelDTO();

    abstractLevelDTO.setId(1L);

    trainingDefinition1 = testDataFactory.getArchivedDefinition();
    trainingDefinition2 = testDataFactory.getReleasedDefinition();

    page = new PageImpl<>(List.of(trainingDefinition1, trainingDefinition2));

    trainingDefinitionInfoDTOPageResultResource =
        trainingDefinitionMapper.mapToPageResultResourceInfoDTO(page);
    trainingDefinitionDTOPageResultResource =
        trainingDefinitionMapper.mapToPageResultResource(page);
  }

  @AfterEach
  void closeService() throws Exception {
    closeable.close();
  }

  private static Stream<Named<Consumer<TrainingDefinitionUpdateDTO>>> requiredFieldRemovals() {
    return Stream.of(
        Named.of("version", update -> update.setVersion(null)),
        Named.of("levels", update -> update.setLevels(null)));
  }

  private MockHttpServletRequestBuilder putUpdate() throws IOException {
    return put("/training-definitions/{definitionId}", DEFINITION_ID)
        .content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  public void findTrainingDefinitionById() throws Exception {
    given(trainingDefinitionFacade.findById(any(Long.class))).willReturn(trainingDefinitionDTO1);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-definitions" + "/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        trainingDefinitionDTO1,
        convertJsonBytesToObject(
            result.getContentAsString(), TrainingDefinitionWithLevelsDTO.class));
  }

  @Test
  public void findTrainingDefinitionById_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingDefinitionFacade)
        .findById(any(Long.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/training-definitions" + "/{id}", 6L))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
    then(trainingDefinitionFacade).should().findById(6L);
  }

  @Test
  public void findAllTrainingDefinitions() throws Exception {
    given(trainingDefinitionFacade.findAll(any(Predicate.class), any(Pageable.class)))
        .willReturn(trainingDefinitionDTOPageResultResource);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-definitions"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        trainingDefinitionDTOPageResultResource.getContent(),
        convertJsonBytesToObject(
                result.getContentAsString(),
                new TypeReference<PageResultResource<TrainingDefinitionDTO>>() {})
            .getContent());
  }

  @Test
  public void findAllTrainingDefinitionsForOrganizers() throws Exception {
    given(trainingDefinitionFacade.findAllForOrganizers(eq(TDState.RELEASED), any(Pageable.class)))
        .willReturn(trainingDefinitionInfoDTOPageResultResource);
    MockHttpServletResponse result =
        mockMvc
            .perform(
                get("/training-definitions/for-organizers")
                    .queryParam("state", TDState.RELEASED.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        trainingDefinitionInfoDTOPageResultResource.getContent(),
        convertJsonBytesToObject(
                result.getContentAsString(),
                new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {})
            .getContent());
  }

  @Test
  public void createTrainingDefinition() throws Exception {
    given(trainingDefinitionFacade.create(any(TrainingDefinitionCreateDTO.class)))
        .willReturn(trainingDefinitionDTO1);
    MockHttpServletResponse result =
        mockMvc
            .perform(
                post("/training-definitions")
                    .content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        trainingDefinitionDTO1,
        convertJsonBytesToObject(
            result.getContentAsString(), TrainingDefinitionWithLevelsDTO.class));
    then(trainingDefinitionFacade).should().create(any(TrainingDefinitionCreateDTO.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"id\": 5, \"estimated_duration\": 1", "\"estimated_duration\": -1"})
  public void createTrainingDefinition_InvalidLevel(String invalidLevelFields) throws Exception {
    String body =
        "{\"title\": \"T\", \"levels\": [{\"level_type\": \"INFO_LEVEL\","
            + " \"title\": \"L\", \"content\": \"C\", "
            + invalidLevelFields
            + "}]}";

    mockMvc
        .perform(
            post("/training-definitions")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest());

    then(trainingDefinitionFacade).shouldHaveNoInteractions();
  }

  @Test
  public void createTrainingDefinition_FacadeException() throws Exception {
    willThrow(new UnprocessableEntityException())
        .given(trainingDefinitionFacade)
        .create(any(TrainingDefinitionCreateDTO.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                post("/training-definitions")
                    .content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isUnprocessableContent())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, error.getStatus());
  }

  @Test
  public void updateTrainingDefinition() throws Exception {
    given(
            trainingDefinitionFacade.update(
                eq(DEFINITION_ID), any(TrainingDefinitionUpdateDTO.class)))
        .willReturn(trainingDefinitionDTO1);
    MockHttpServletResponse result =
        mockMvc
            .perform(putUpdate())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        trainingDefinitionDTO1,
        convertJsonBytesToObject(
            result.getContentAsString(), TrainingDefinitionWithLevelsDTO.class));
    ArgumentCaptor<TrainingDefinitionUpdateDTO> updateCaptor =
        ArgumentCaptor.forClass(TrainingDefinitionUpdateDTO.class);
    then(trainingDefinitionFacade).should().update(eq(DEFINITION_ID), updateCaptor.capture());
    assertEquals(1, updateCaptor.getValue().getLevels().size());
    assertInstanceOf(InfoLevelUpdateDTO.class, updateCaptor.getValue().getLevels().get(0));
  }

  @ParameterizedTest(name = "without {0}")
  @MethodSource("requiredFieldRemovals")
  public void updateTrainingDefinition_RequiredFieldMissing(
      Consumer<TrainingDefinitionUpdateDTO> removeRequiredField) throws Exception {
    removeRequiredField.accept(trainingDefinitionUpdateDTO);
    mockMvc.perform(putUpdate()).andExpect(status().isBadRequest());
    then(trainingDefinitionFacade).shouldHaveNoInteractions();
  }

  @Test
  public void updateTrainingDefinition_FacadeException() throws Exception {
    willThrow(new EntityConflictException())
        .given(trainingDefinitionFacade)
        .update(eq(DEFINITION_ID), any(TrainingDefinitionUpdateDTO.class));
    MockHttpServletResponse response =
        mockMvc.perform(putUpdate()).andExpect(status().isConflict()).andReturn().getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals(
        "The request could not be completed due to a conflict with the current state of the target resource.",
        error.getMessage());
  }

  @Test
  public void updateTrainingDefinition_ConcurrentWrite() throws Exception {
    willThrow(new OptimisticLockingFailureException("Row was updated by another transaction"))
        .given(trainingDefinitionFacade)
        .update(eq(DEFINITION_ID), any(TrainingDefinitionUpdateDTO.class));
    MockHttpServletResponse response =
        mockMvc.perform(putUpdate()).andExpect(status().isConflict()).andReturn().getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals(
        "Write refused: another request changed the same data first. Reload it and repeat the"
            + " change.",
        error.getMessage());
    assertEquals(List.of("The stored data changed after this request read it."), error.getErrors());
  }

  @Test
  public void cloneTrainingDefinition() throws Exception {
    given(trainingDefinitionFacade.clone(any(Long.class), anyString()))
        .willReturn(trainingDefinitionDTO1);
    MockHttpServletResponse result =
        mockMvc
            .perform(
                post("/training-definitions/{id}", trainingDefinitionDTO1.getId())
                    .param("title", "title")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertEquals(
        trainingDefinitionDTO1,
        convertJsonBytesToObject(
            result.getContentAsString(), TrainingDefinitionWithLevelsDTO.class));
  }

  @Test
  public void cloneTrainingDefinition_FacadeException() throws Exception {
    willThrow(new EntityConflictException())
        .given(trainingDefinitionFacade)
        .clone(any(Long.class), anyString());
    MockHttpServletResponse response =
        mockMvc
            .perform(
                post("/training-definitions/1")
                    .param("title", "title")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isConflict())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals(
        "The request could not be completed due to a conflict with the current state of the target resource.",
        error.getMessage());
  }

  @Test
  public void deleteTrainingDefinition() throws Exception {
    mockMvc
        .perform(delete("/training-definitions/{id}", trainingDefinitionDTO1.getId()))
        .andExpect(status().isOk());
    then(trainingDefinitionFacade).should().delete(trainingDefinitionDTO1.getId());
  }

  @Test
  public void deleteTrainingDefinition_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingDefinitionFacade)
        .delete(any(Long.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(delete("/training-definitions/{id}", trainingDefinitionDTO2.getId()))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
    then(trainingDefinitionFacade).should().delete(trainingDefinitionDTO2.getId());
  }

  @Test
  public void findLevelById() throws Exception {
    given(trainingDefinitionFacade.findLevelById(any(Long.class))).willReturn(abstractLevelDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-definitions/levels" + "/{levelId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        abstractLevelDTO,
        convertJsonBytesToObject(result.getContentAsString(), TrainingLevelDTO.class));
  }

  @Test
  public void findLevelById_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException()).given(trainingDefinitionFacade).findLevelById(1L);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-definitions/levels" + "/{levelId}", 1L))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(result.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void getAuthors() throws Exception {
    List<UserRefDTO> authors = List.of(organizerDTO, designerDTO1);
    given(
            trainingDefinitionFacade.getAuthors(
                eq(trainingDefinitionDTO1.getId()), any(Pageable.class), eq(JANE_DOE_FILTER)))
        .willReturn(new PageResultResource<>(authors));
    MockHttpServletResponse result =
        mockMvc
            .perform(
                get("/training-definitions/{definitionId}/authors", trainingDefinitionDTO1.getId())
                    .param("name", "Jane Doe")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertEquals(
        authors,
        convertJsonBytesToObject(
                result.getContentAsString(), new TypeReference<PageResultResource<UserRefDTO>>() {})
            .getContent());
  }

  @Test
  public void getAuthors_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingDefinitionFacade)
        .getAuthors(
            eq(trainingDefinitionDTO1.getId()), any(Pageable.class), eq(UserNameFilter.NONE));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                get("/training-definitions/{definitionId}/authors", trainingDefinitionDTO1.getId())
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
  }

  @Test
  public void getDesignersNotInGivenTrainingDefinition() throws Exception {
    List<UserRefDTO> designers = List.of(designerDTO1, designerDTO2);
    given(
            trainingDefinitionFacade.getDesignersNotInGivenTrainingDefinition(
                eq(trainingDefinitionDTO1.getId()), any(Pageable.class), eq(JANE_DOE_FILTER)))
        .willReturn(new PageResultResource<>(designers));
    MockHttpServletResponse result =
        mockMvc
            .perform(
                get(
                        "/training-definitions/{definitionId}/designers-not-in-training-definition",
                        trainingDefinitionDTO1.getId())
                    .param("name", "Jane Doe"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertEquals(
        designers,
        convertJsonBytesToObject(
                result.getContentAsString(), new TypeReference<PageResultResource<UserRefDTO>>() {})
            .getContent());
  }

  @Test
  public void editAuthors() throws Exception {
    mockMvc
        .perform(
            put("/training-definitions/{definitionId}/authors", trainingDefinitionDTO1.getId())
                .queryParam(
                    "authorsAddition",
                    List.of(designerDTO1.getUserRefId(), designerDTO2.getUserRefId())
                        .toString()
                        .replace("[", "")
                        .replace("]", ""))
                .queryParam(
                    "authorsRemoval",
                    List.of(organizerDTO.getUserRefId())
                        .toString()
                        .replace("[", "")
                        .replace("]", ""))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isNoContent())
        .andReturn()
        .getResponse();
    then(trainingDefinitionFacade)
        .should()
        .editAuthors(
            trainingDefinitionDTO1.getId(),
            Set.of(designerDTO1.getUserRefId(), designerDTO2.getUserRefId()),
            Set.of(organizerDTO.getUserRefId()));
  }

  @Test
  public void editAuthors_FacadeException() throws Exception {
    willThrow(new EntityConflictException())
        .given(trainingDefinitionFacade)
        .editAuthors(
            trainingDefinitionDTO1.getId(),
            Set.of(designerDTO1.getUserRefId(), designerDTO2.getUserRefId()),
            Set.of(organizerDTO.getUserRefId()));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                put("/training-definitions/{definitionId}/authors", trainingDefinitionDTO1.getId())
                    .queryParam(
                        "authorsAddition",
                        List.of(designerDTO1.getUserRefId(), designerDTO2.getUserRefId())
                            .toString()
                            .replace("[", "")
                            .replace("]", ""))
                    .queryParam(
                        "authorsRemoval",
                        List.of(organizerDTO.getUserRefId())
                            .toString()
                            .replace("[", "")
                            .replace("]", ""))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isConflict())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.CONFLICT, error.getStatus());
  }

  @Test
  public void switchState() throws Exception {
    mockMvc
        .perform(
            put(
                "/training-definitions/{definitionId}/states/{state}",
                trainingDefinitionDTO1.getId(),
                TDState.ARCHIVED))
        .andExpect(status().isNoContent());
    then(trainingDefinitionFacade)
        .should()
        .switchState(trainingDefinitionDTO1.getId(), TDState.ARCHIVED);
  }

  @Test
  public void switchStateWithConflict() throws Exception {
    willThrow(new EntityConflictException())
        .given(trainingDefinitionFacade)
        .switchState(any(Long.class), any(TDState.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(
                put(
                    "/training-definitions/{definitionId}/states/{state}",
                    trainingDefinitionDTO2.getId(),
                    TDState.ARCHIVED))
            .andExpect(status().isConflict())
            .andReturn()
            .getResponse();
    ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals(
        "The request could not be completed due to a conflict with the current state of the target resource.",
        error.getMessage());
  }
}
