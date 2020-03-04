package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.rest.ApiError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, TrainingInstanceMapperImpl.class, UserRefMapperImpl.class,
        TrainingDefinitionMapperImpl.class, UserRefMapperImpl.class, TrainingRunMapperImpl.class,
        BetaTestingGroupMapperImpl.class})
public class TrainingRunsRestControllerTest {

    private TrainingRunsRestController trainingRunsRestController;

    @Mock
    private TrainingRunFacade trainingRunFacade;

    private MockMvc mockMvc;

    @Autowired
    TrainingRunMapper trainingRunMapper;

    @MockBean
    private ObjectMapper objectMapper;
    private ObjectMapper testObjectMapper = new ObjectMapper();

    private TrainingRun trainingRun1, trainingRun2;
    private TrainingRunDTO trainingRun1DTO, trainingRun2DTO;
    private Page p, pAccessed;
    private PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource;
    private PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOPage;
    private AccessTrainingRunDTO accessTrainingRunDTO;
    private AssessmentLevelDTO assessmentLevelDTO;
    private InfoLevelDTO infoLevelDTO;
    private GameLevelDTO gameLevelDTO;
    private HintDTO hintDTO;
    private AccessedTrainingRunDTO accessedTrainingRunDTO;
    private IsCorrectFlagDTO isCorrectFlagDTO;
    private TrainingRunByIdDTO trainingRunByIdDTO;
    private UserRefDTO participantDTO1, participantDTO2;
    private UserRef participant1;


    @Before
    public void init() {
        participant1 = new UserRef();
        participant1.setId(1L);
        participant1.setUserRefId(10L);

        participantDTO1 = createUserRefDTO(10L, "Bc. Dominik Meškal", "Meškal", "Dominik", "445533@muni.cz", "https://oidc.muni.cz/oidc", null);
        participantDTO2 = createUserRefDTO(20L, "Bc. Boris Makal", "Makal", "Boris", "772211@muni.cz", "https://oidc.muni.cz/oidc", null);

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.FINISHED);
        trainingRun1.setParticipantRef(participant1);

        trainingRunByIdDTO = new TrainingRunByIdDTO();
        trainingRunByIdDTO.setId(1L);
        trainingRunByIdDTO.setState(cz.muni.ics.kypo.training.api.enums.TRState.FINISHED);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.RUNNING);

        trainingRun1DTO = new TrainingRunDTO();
        trainingRun1DTO.setId(1L);
        trainingRun1DTO.setState(cz.muni.ics.kypo.training.api.enums.TRState.FINISHED);

        trainingRun2DTO = new TrainingRunDTO();
        trainingRun2DTO.setId(2L);
        trainingRun2DTO.setState(cz.muni.ics.kypo.training.api.enums.TRState.RUNNING);

        accessTrainingRunDTO = new AccessTrainingRunDTO();

        gameLevelDTO = new GameLevelDTO();
        gameLevelDTO.setId(1L);
        gameLevelDTO.setSolution("solution");
        gameLevelDTO.setFlag("flag");

        infoLevelDTO = new InfoLevelDTO();
        infoLevelDTO.setId(2L);
        infoLevelDTO.setContent("content");

        assessmentLevelDTO = new AssessmentLevelDTO();
        assessmentLevelDTO.setId(3L);
        assessmentLevelDTO.setAssessmentType(cz.muni.ics.kypo.training.api.enums.AssessmentType.TEST);
        assessmentLevelDTO.setInstructions("instructions");
        assessmentLevelDTO.setQuestions("questions");

        hintDTO = new HintDTO();
        hintDTO.setId(1L);
        hintDTO.setContent("hint content");
        hintDTO.setTitle("hint title");

        isCorrectFlagDTO = new IsCorrectFlagDTO();
        isCorrectFlagDTO.setCorrect(true);
        isCorrectFlagDTO.setRemainingAttempts(2);

        accessedTrainingRunDTO = new AccessedTrainingRunDTO();
        accessedTrainingRunDTO.setId(1L);
        accessedTrainingRunDTO.setCurrentLevelOrder(1);
        accessedTrainingRunDTO.setNumberOfLevels(5);
        accessedTrainingRunDTO.setTitle("accessed training run");
        List<AccessedTrainingRunDTO> accessed = new ArrayList<>();
        accessed.add(accessedTrainingRunDTO);
        pAccessed = new PageImpl<>(accessed);

        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        p = new PageImpl<>(expected);

        ObjectMapper obj = new ObjectMapper();
        obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        obj.registerModule(new JavaTimeModule());
        given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

        trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(p);

        MockitoAnnotations.initMocks(this);
        trainingRunsRestController = new TrainingRunsRestController(trainingRunFacade, objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter())
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();


    }

    @Test
    public void findTrainingRunById() throws Exception {
        given(trainingRunFacade.findById(any(Long.class))).willReturn(trainingRunByIdDTO);
        String valueTr = convertObjectToJsonBytes(trainingRun1DTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs" + "/{runId}", 1l))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRun1DTO)), result.getContentAsString());
    }

    @Test
    public void findTrainingRunByIdWithFacadeException() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingRunFacade).findById(any(Long.class));
        MockHttpServletResponse response = mockMvc.perform(get("/training-runs" + "/{runId}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void findAllTrainingRuns() throws Exception {
        String valueTr = convertObjectToJsonBytes(trainingRunDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(trainingRunFacade.findAll(any(Predicate.class), any(Pageable.class))).willReturn(trainingRunDTOPageResultResource);

        MockHttpServletResponse result = mockMvc.perform(get("/training-runs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRunDTOPageResultResource)), result.getContentAsString());
    }

    @Test
    public void getAllAccessedTrainingRuns() throws Exception {
        accessedTrainingRunDTOPage = trainingRunMapper.mapToPageResultResourceAccessed(pAccessed);

        String valueTr = convertObjectToJsonBytes(trainingRunDTOPageResultResource);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(trainingRunFacade.findAllAccessedTrainingRuns(any(Pageable.class), anyString())).willReturn(accessedTrainingRunDTOPage);

        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/accessible")
                .param("sortByTitle", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRunDTOPageResultResource)), result.getContentAsString());
    }

    @Test
    public void accessTrainingRun() throws Exception {
        given(trainingRunFacade.accessTrainingRun("accessToken")).willReturn(accessTrainingRunDTO);
        MockHttpServletResponse result = mockMvc.perform(post("/training-runs").param("accessToken", "accessToken"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(accessTrainingRunDTO), result.getContentAsString());
    }

    @Test
    public void accessTrainingRunWithNoSandbox() throws Exception {
        willThrow(new ForbiddenException()).given(trainingRunFacade).accessTrainingRun("accessToken");
        MockHttpServletResponse response = mockMvc.perform(post("/training-runs").param("accessToken", "accessToken"))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    @Test
    public void accessTrainingRunWithResourceNotFound() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingRunFacade).accessTrainingRun("accessToken");
        MockHttpServletResponse response = mockMvc.perform(post("/training-runs").param("accessToken", "accessToken"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void accessTrainingRunWithUnexpectedError() throws Exception {
        willThrow(new InternalServerErrorException()).given(trainingRunFacade).accessTrainingRun("accessToken");
        MockHttpServletResponse response = mockMvc.perform(post("/training-runs").param("accessToken", "accessToken"))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatus());
    }

    @Test
    public void getNextLevelAssessment() throws Exception {
        String valueTr = convertObjectToJsonBytes(assessmentLevelDTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(trainingRunFacade.getNextLevel(assessmentLevelDTO.getId())).willReturn(assessmentLevelDTO);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{runId}/next-levels", 3L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(assessmentLevelDTO)), result.getContentAsString());
    }

    @Test
    public void getNextLevelGame() throws Exception {
        String valueTr = convertObjectToJsonBytes(gameLevelDTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(trainingRunFacade.getNextLevel(gameLevelDTO.getId())).willReturn(gameLevelDTO);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{runId}/next-levels", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(gameLevelDTO)), result.getContentAsString());
    }

    @Test
    public void getNextLevelInfo() throws Exception {
        String valueTr = convertObjectToJsonBytes(infoLevelDTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(trainingRunFacade.getNextLevel(infoLevelDTO.getId())).willReturn(infoLevelDTO);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{runId}/next-levels", 2L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(infoLevelDTO)), result.getContentAsString());
    }


    @Test
    public void getSolution() throws Exception {
        String solution = "Solution";
        given(trainingRunFacade.getSolution(assessmentLevelDTO.getId())).willReturn(solution);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{runId}/solutions", 3L))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals("Solution", result.getContentAsString().replace("\"", ""));
    }

    @Test
    public void getSolutionWithException() throws Exception {
        willThrow(new BadRequestException()).given(trainingRunFacade).getSolution(1L);
        MockHttpServletResponse response = mockMvc.perform(get("/training-runs/{runId}/solutions", 1L))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    public void isCorrectFlag() throws Exception {
        given(trainingRunFacade.isCorrectFlag(trainingRun1.getId(), "flag")).willReturn(isCorrectFlagDTO);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{runId}/is-correct-flag", trainingRun1.getId())
                .param("flag", "flag")
                .param("solutionTaken", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    @Test
    public void getHint() throws Exception {
        String valueTr = convertObjectToJsonBytes(hintDTO);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
        given(trainingRunFacade.getHint(trainingRun1.getId(), hintDTO.getId())).willReturn(hintDTO);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), hintDTO.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(hintDTO)), result.getContentAsString());
    }

    @Test
    public void getHintNotFound() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingRunFacade).getHint(anyLong(), anyLong());
        MockHttpServletResponse response = mockMvc.perform(get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), hintDTO.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void getHintWrongLevelType() throws Exception {
        willThrow(new BadRequestException()).given(trainingRunFacade).getHint(anyLong(), anyLong());
        MockHttpServletResponse response = mockMvc.perform(get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), hintDTO.getId()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    public void evaluateResponsesToAssessment() throws Exception {
        mockMvc.perform(put("/training-runs/{runId}/assessment-evaluations", trainingRun1.getId())
        .content("responses"))
                .andExpect(status().isNoContent());
        then(trainingRunFacade).should().evaluateResponsesToAssessment(trainingRun1.getId(), "responses");
    }

    @Test
    public void finishTrainingRun() throws Exception {
        mockMvc.perform(put("/training-runs/{runId}", trainingRun1.getId()))
                .andExpect(status().isOk());
        then(trainingRunFacade).should().finishTrainingRun(trainingRun1.getId());
    }

    @Test
    public void finishTrainingRunCannotFinish() throws Exception {
        willThrow(new EntityConflictException()).given(trainingRunFacade).finishTrainingRun(trainingRun1.getId());
        MockHttpServletResponse response = mockMvc.perform(put("/training-runs/{runId}", trainingRun1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void finishTrainingRunNotFound() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingRunFacade).finishTrainingRun(trainingRun1.getId());
        MockHttpServletResponse response = mockMvc.perform(put("/training-runs/{runId}", trainingRun1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }


    @Test
    public void resumeTrainingRun() throws Exception {
        given(trainingRunFacade.resumeTrainingRun(trainingRun1.getId())).willReturn(accessTrainingRunDTO);
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(convertObjectToJsonBytes(accessTrainingRunDTO), result.getContentAsString());
    }

    @Test
    public void resumeTrainingRunCannotFinish() throws Exception {
        willThrow(new EntityConflictException()).given(trainingRunFacade).resumeTrainingRun(trainingRun1.getId());
        MockHttpServletResponse response = mockMvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("The request could not be completed due to a conflict with the current state of the target resource.", error.getMessage());
    }

    @Test
    public void resumeTrainingRunNotFound() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingRunFacade).resumeTrainingRun(trainingRun1.getId());
        MockHttpServletResponse response = mockMvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void getParticipant() throws Exception {
        given(trainingRunFacade.getParticipant(trainingRun1.getId())).willReturn(participantDTO1);
        given(objectMapper.writeValueAsString(any(Object.class))).willReturn(convertObjectToJsonBytes(participantDTO1));
        MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{runId}/participant", trainingRun1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertEquals(participantDTO1, convertResultStringToDTO(result.getContentAsString(), UserRefDTO.class));
    }

    @Test
    public void getParticipantTrainingRunNotFound() throws Exception {
        willThrow(new EntityNotFoundException()).given(trainingRunFacade).getParticipant(trainingRun1.getId());
        MockHttpServletResponse response = mockMvc.perform(get("/training-runs/{runId}/participant", trainingRun1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("The requested entity could not be found", error.getMessage());
    }

    @Test
    public void getParticipantUserAndGroupServiceError() throws Exception {
        willThrow(new InternalServerErrorException()).given(trainingRunFacade).getParticipant(trainingRun1.getId());
        MockHttpServletResponse response = mockMvc.perform(get("/training-runs/{runId}/participant", trainingRun1.getId()))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse();
        ApiError error = convertJsonBytesToObject(response.getContentAsString(), ApiError.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatus());
    }


    private UserRefDTO createUserRefDTO(Long userRefId, String fullName, String familyName, String givenName, String login, String iss, byte[] picture) {
        UserRefDTO userRefDTO = new UserRefDTO();
        userRefDTO.setUserRefId(userRefId);
        userRefDTO.setUserRefFullName(fullName);
        userRefDTO.setUserRefFamilyName(familyName);
        userRefDTO.setUserRefGivenName(givenName);
        userRefDTO.setUserRefLogin(login);
        userRefDTO.setIss(iss);
        userRefDTO.setPicture(picture);
        return userRefDTO;
    }

    private <T> T convertResultStringToDTO(String resultAsString, Class<T> claas) throws Exception {
        System.out.println(resultAsString);
        return testObjectMapper.readValue(convertJsonBytesToString(resultAsString), claas);
    }

    private static String convertJsonBytesToString(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, String.class);
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }


}
