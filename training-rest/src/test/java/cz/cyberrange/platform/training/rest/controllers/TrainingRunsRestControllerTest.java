package cz.cyberrange.platform.training.rest.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.IsCorrectAnswerDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.AccessedTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.run.TraineeSandboxDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunByIdDTO;
import cz.cyberrange.platform.training.api.dto.run.TrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.ValidateAnswerDTO;
import cz.cyberrange.platform.training.api.enums.TRState;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ForbiddenException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.api.responses.SandboxRequestInfo;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.rest.controllers.util.ObjectConverter;
import cz.cyberrange.platform.training.rest.utils.error.ApiEntityError;
import cz.cyberrange.platform.training.rest.utils.error.ApiError;
import cz.cyberrange.platform.training.rest.utils.error.CustomRestExceptionHandlerTraining;
import cz.cyberrange.platform.training.service.facade.TrainingRunFacade;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingInstanceMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingRunMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingRunMapperImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    classes = {
      TestDataFactory.class,
      EnumMapperImpl.class,
      TrainingInstanceMapperImpl.class,
      TrainingDefinitionMapperImpl.class,
      TrainingRunMapperImpl.class
    })
public class TrainingRunsRestControllerTest {

  private TrainingRunsRestController trainingRunsRestController;

  @MockitoBean private TrainingRunFacade trainingRunFacade;

  private MockMvc mockMvc;
  private AutoCloseable closeable;

  @Autowired private TestDataFactory testDataFactory;
  @Autowired TrainingRunMapper trainingRunMapper;

  private TrainingRun trainingRun1;
  private TrainingRunDTO trainingRun1DTO;
  private Page pageAccessed;
  private PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOPage;
  private AccessTrainingRunDTO accessTrainingRunDTO;
  private AssessmentLevelDTO assessmentLevelDTO;
  private InfoLevelDTO infoLevelDTO;
  private TrainingLevelDTO trainingLevelDTO;
  private HintDTO hintDTO;
  private AccessedTrainingRunDTO accessedTrainingRunDTO;
  private IsCorrectAnswerDTO isCorrectAnswerDTO;
  private TrainingRunByIdDTO trainingRunByIdDTO;
  private UserRefDTO participantDTO1;
  private UserRef participant1;
  private ValidateAnswerDTO validAnswerDTO;

  @BeforeEach
  public void init() {
    participant1 = new UserRef();
    participant1.setId(1L);
    participant1.setUserRefId(10L);

    participantDTO1 = testDataFactory.getUserRefDTO1();

    trainingRun1 = testDataFactory.getFinishedRun();
    trainingRun1.setId(1L);
    trainingRun1.setParticipantRef(participant1);

    trainingRunByIdDTO = testDataFactory.getTrainingRunByIdDTO();
    trainingRunByIdDTO.setId(1L);

    trainingRun1DTO = testDataFactory.getTrainingRunDTO();
    trainingRun1DTO.setId(1L);
    trainingRun1DTO.setState(TRState.FINISHED);

    accessTrainingRunDTO = new AccessTrainingRunDTO();

    trainingLevelDTO = testDataFactory.getTrainingLevelDTO();
    trainingLevelDTO.setId(1L);

    infoLevelDTO = testDataFactory.getInfoLevelDTO();
    infoLevelDTO.setId(2L);

    assessmentLevelDTO = testDataFactory.getAssessmentLevelDTO();
    assessmentLevelDTO.setId(3L);

    hintDTO = testDataFactory.getHintDTO();
    hintDTO.setId(1L);

    isCorrectAnswerDTO = new IsCorrectAnswerDTO();
    isCorrectAnswerDTO.setCorrect(true);
    isCorrectAnswerDTO.setRemainingAttempts(2);

    accessedTrainingRunDTO = testDataFactory.getAccessedTrainingRunDTO();
    accessedTrainingRunDTO.setId(1L);

    validAnswerDTO = new ValidateAnswerDTO();
    validAnswerDTO.setAnswer("answer");

    pageAccessed = new PageImpl<>(List.of(accessedTrainingRunDTO));

    JsonMapper snakeCaseMapper = ObjectConverter.restResponseMapper();

    closeable = MockitoAnnotations.openMocks(this);
    trainingRunsRestController = new TrainingRunsRestController(trainingRunFacade);
    this.mockMvc =
        MockMvcBuilders.standaloneSetup(trainingRunsRestController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new QuerydslPredicateArgumentResolver(
                    new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE),
                    Optional.empty()))
            .setMessageConverters(
                new JacksonJsonHttpMessageConverter(snakeCaseMapper),
                new StringHttpMessageConverter())
            .setControllerAdvice(new CustomRestExceptionHandlerTraining())
            .build();
  }

  @AfterEach
  void closeService() throws Exception {
    closeable.close();
  }

  @Test
  public void findTrainingRunById() throws Exception {
    given(trainingRunFacade.findById(trainingRunByIdDTO.getId())).willReturn(trainingRunByIdDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-runs" + "/{runId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        ObjectConverter.convertObjectToJsonBytes(trainingRunByIdDTO), result.getContentAsString());
  }

  @Test
  public void findTrainingRunById_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException()).given(trainingRunFacade).findById(any(Long.class));
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/training-runs" + "/{runId}", 6l))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void accessTrainingRun() throws Exception {
    given(trainingRunFacade.accessTrainingRun("accessToken")).willReturn(accessTrainingRunDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(post("/training-runs").param("accessToken", "accessToken"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        ObjectConverter.convertObjectToJsonBytes(accessTrainingRunDTO),
        result.getContentAsString());
  }

  @Test
  public void accessTrainingRun_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingRunFacade)
        .accessTrainingRun("accessToken");
    MockHttpServletResponse response =
        mockMvc
            .perform(post("/training-runs").param("accessToken", "accessToken"))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void getUserActiveSandboxes() throws Exception {
    SandboxRequestInfo allocationRequest = new SandboxRequestInfo();
    allocationRequest.setId(3);
    allocationRequest.setAllocationUnitId(7);
    allocationRequest.setStages(List.of("FINISHED", "FINISHED", "FINISHED"));
    TraineeSandboxDTO sandbox = new TraineeSandboxDTO();
    sandbox.setId(7);
    sandbox.setAllocationRequest(allocationRequest);
    sandbox.setAllowRemove(true);
    sandbox.setTrainingInstanceTitle("Linux basics");
    given(trainingRunFacade.findOwnSandboxes()).willReturn(List.of(sandbox));

    mockMvc
        .perform(get("/training-runs/user-active-sandboxes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(7))
        .andExpect(jsonPath("$[0].allow_remove").value(true))
        .andExpect(jsonPath("$[0].training_instance_title").value("Linux basics"))
        .andExpect(jsonPath("$[0].allocation_request.allocation_unit_id").value(7));
  }

  @Test
  public void requestTraineeSandboxCleanup() throws Exception {
    mockMvc
        .perform(post("/training-runs/user-sandboxes/{sandboxId}/cleanup", "sandbox-uuid"))
        .andExpect(status().isNoContent());

    then(trainingRunFacade).should().removeOwnSandboxBySandboxId("sandbox-uuid");
  }

  @Test
  public void requestTraineeSandboxCleanup_NotOwnSandbox() throws Exception {
    willThrow(new ForbiddenException("Sandbox not found or you did not allocate it."))
        .given(trainingRunFacade)
        .removeOwnSandboxBySandboxId("sandbox-uuid");

    mockMvc
        .perform(post("/training-runs/user-sandboxes/{sandboxId}/cleanup", "sandbox-uuid"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void requestTraineeSandboxCleanupByAllocationId() throws Exception {
    mockMvc
        .perform(post("/training-runs/user-sandboxes/by-allocation/{allocationUnitId}/cleanup", 7))
        .andExpect(status().isNoContent());

    then(trainingRunFacade).should().removeOwnSandbox(7);
  }

  @Test
  public void getAllAccessedTrainingRuns() throws Exception {
    accessedTrainingRunDTOPage = trainingRunMapper.mapToPageResultResourceAccessed(pageAccessed);

    given(
            trainingRunFacade.findAllAccessedTrainingRuns(
                any(Predicate.class), any(Pageable.class), anyString()))
        .willReturn(accessedTrainingRunDTOPage);

    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-runs/accessible").param("sortByTitle", ""))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        ObjectConverter.convertObjectToJsonBytes(accessedTrainingRunDTOPage),
        result.getContentAsString());
  }

  @Test
  public void getNextLevel_Assessment() throws Exception {
    given(trainingRunFacade.getNextLevel(assessmentLevelDTO.getId()))
        .willReturn(assessmentLevelDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-runs/{runId}/next-levels", 3L))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        ObjectConverter.convertObjectToJsonBytes(assessmentLevelDTO), result.getContentAsString());
  }

  @Test
  public void getNextLevel_Game() throws Exception {
    given(trainingRunFacade.getNextLevel(trainingLevelDTO.getId())).willReturn(trainingLevelDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-runs/{runId}/next-levels", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        ObjectConverter.convertObjectToJsonBytes(trainingLevelDTO), result.getContentAsString());
  }

  @Test
  public void getNextLevel_Info() throws Exception {
    given(trainingRunFacade.getNextLevel(infoLevelDTO.getId())).willReturn(infoLevelDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-runs/{runId}/next-levels", 2L))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        ObjectConverter.convertObjectToJsonBytes(infoLevelDTO), result.getContentAsString());
  }

  @Test
  public void getNextLevel_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException()).given(trainingRunFacade).getNextLevel(1L);
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/training-runs/{runId}/next-levels", 1L))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(
            response.getContentAsString(), ApiEntityError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
  }

  @Test
  public void getSolution() throws Exception {
    String solution = "Solution";
    given(trainingRunFacade.getSolution(assessmentLevelDTO.getId())).willReturn(solution);
    mockMvc
        .perform(get("/training-runs/{runId}/solutions", 3L))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.solution").value(solution));
  }

  @Test
  public void getSolution_FacadeException() throws Exception {
    willThrow(new BadRequestException()).given(trainingRunFacade).getSolution(1L);
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/training-runs/{runId}/solutions", 1L))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
  }

  @Test
  public void getHint() throws Exception {
    given(trainingRunFacade.getHint(trainingRun1.getId(), hintDTO.getId())).willReturn(hintDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(
                get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), hintDTO.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        ObjectConverter.convertObjectToJsonBytes(hintDTO), result.getContentAsString());
  }

  @Test
  public void getHint_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException()).given(trainingRunFacade).getHint(anyLong(), anyLong());
    MockHttpServletResponse response =
        mockMvc
            .perform(
                get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), hintDTO.getId()))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void isCorrectAnswer() throws Exception {
    given(trainingRunFacade.isCorrectAnswer(trainingRun1.getId(), "answer"))
        .willReturn(isCorrectAnswerDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(
                post("/training-runs/{runId}/is-correct-answer", trainingRun1.getId())
                    .param("solutionTaken", "true")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(ObjectConverter.convertObjectToJsonBytes(validAnswerDTO)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
    assertEquals(
        isCorrectAnswerDTO,
        ObjectConverter.convertJsonBytesToObject(
            result.getContentAsString(), IsCorrectAnswerDTO.class));
  }

  @Test
  public void isCorrectAnswer_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingRunFacade)
        .isCorrectAnswer(anyLong(), anyString());
    MockHttpServletResponse result =
        mockMvc
            .perform(
                post("/training-runs/{runId}/is-correct-answer", trainingRun1.getId())
                    .param("solutionTaken", "true")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(ObjectConverter.convertObjectToJsonBytes(validAnswerDTO)))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(result.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  //    @Test
  //    public void evaluateResponsesToAssessment() throws Exception {
  //        mockMvc.perform(put("/training-runs/{runId}/assessment-evaluations",
  // trainingRun1.getId())
  //                .content("responses"))
  //                .andExpect(status().isNoContent());
  //        then(trainingRunFacade).should().evaluateResponsesToAssessment(trainingRun1.getId(),
  // "responses");
  //    }

  @Test
  public void finishTrainingRun() throws Exception {
    mockMvc.perform(put("/training-runs/{runId}", trainingRun1.getId())).andExpect(status().isOk());
    then(trainingRunFacade).should().finishTrainingRun(trainingRun1.getId());
  }

  @Test
  public void finishTrainingRun_FacadeException() throws Exception {
    willThrow(new EntityConflictException())
        .given(trainingRunFacade)
        .finishTrainingRun(trainingRun1.getId());
    MockHttpServletResponse response =
        mockMvc
            .perform(put("/training-runs/{runId}", trainingRun1.getId()))
            .andExpect(status().isConflict())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals(
        "The request could not be completed due to a conflict with the current state of the target resource.",
        error.getMessage());
  }

  @Test
  public void finishTrainingRunNotFound() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingRunFacade)
        .finishTrainingRun(trainingRun1.getId());
    MockHttpServletResponse response =
        mockMvc
            .perform(put("/training-runs/{runId}", trainingRun1.getId()))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }

  @Test
  public void resumeTrainingRun() throws Exception {
    given(trainingRunFacade.resumeTrainingRun(trainingRun1.getId()))
        .willReturn(accessTrainingRunDTO);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        ObjectConverter.convertObjectToJsonBytes(accessTrainingRunDTO),
        result.getContentAsString());
  }

  @Test
  public void resumeTrainingRun_FacadeException() throws Exception {
    willThrow(new EntityConflictException())
        .given(trainingRunFacade)
        .resumeTrainingRun(trainingRun1.getId());
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
            .andExpect(status().isConflict())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals(
        "The request could not be completed due to a conflict with the current state of the target resource.",
        error.getMessage());
  }

  @Test
  public void getParticipant() throws Exception {
    given(trainingRunFacade.getParticipant(trainingRun1.getId())).willReturn(participantDTO1);
    MockHttpServletResponse result =
        mockMvc
            .perform(get("/training-runs/{runId}/participant", trainingRun1.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    Assertions.assertEquals(
        participantDTO1,
        ObjectConverter.convertJsonBytesToObject(result.getContentAsString(), UserRefDTO.class));
  }

  @Test
  public void getParticipant_FacadeException() throws Exception {
    willThrow(new EntityNotFoundException())
        .given(trainingRunFacade)
        .getParticipant(trainingRun1.getId());
    MockHttpServletResponse response =
        mockMvc
            .perform(get("/training-runs/{runId}/participant", trainingRun1.getId()))
            .andExpect(status().isNotFound())
            .andReturn()
            .getResponse();
    ApiError error =
        ObjectConverter.convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("The requested entity could not be found", error.getMessage());
  }
}
