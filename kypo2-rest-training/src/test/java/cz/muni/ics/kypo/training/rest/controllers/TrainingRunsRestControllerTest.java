package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.rest.exceptions.*;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Boris Jadus(445343)
 */
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


    @Before
    public void init() {
        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.FINISHED);

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
                        new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(), new StringHttpMessageConverter()).build();


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
        Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionThrow)).given(trainingRunFacade).findById(any(Long.class));
        Exception exception = mockMvc.perform(get("/training-runs" + "/{runId}", 6l))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
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
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.NO_AVAILABLE_SANDBOX);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).accessTrainingRun("accessToken");
        Exception exception = mockMvc.perform(post("/training-runs").param("accessToken", "accessToken"))
                .andExpect(status().isServiceUnavailable())
                .andReturn().getResolvedException();
        assertEquals(ServiceUnavailableException.class, exception.getClass());
    }

    @Test
    public void accessTrainingRunWithResourceNotFound() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).accessTrainingRun("accessToken");
        Exception exception = mockMvc.perform(post("/training-runs").param("accessToken", "accessToken"))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void accessTrainingRunWithUnexpectedError() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.UNEXPECTED_ERROR);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).accessTrainingRun("accessToken");
        Exception exception = mockMvc.perform(post("/training-runs").param("accessToken", "accessToken"))
                .andExpect(status().isInternalServerError())
                .andReturn().getResolvedException();
        assertEquals(InternalServerErrorException.class, exception.getClass());
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
                //.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals("Solution", result.getContentAsString().replace("\"", ""));
    }

    @Test
    public void getSolutionWithException() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.WRONG_LEVEL_TYPE);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).getSolution(1L);
        Exception ex = mockMvc.perform(get("/training-runs/{runId}/solutions", 1L))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertEquals(BadRequestException.class, ex.getClass());
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
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).getHint(anyLong(), anyLong());
        Exception exception = mockMvc.perform(get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), hintDTO.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, exception.getClass());
    }

    @Test
    public void getHintWrongLevelType() throws Exception {
        Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.WRONG_LEVEL_TYPE);
        willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).getHint(anyLong(), anyLong());
        Exception exception = mockMvc.perform(get("/training-runs/{runId}/hints/{hintId}", trainingRun1.getId(), hintDTO.getId()))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertEquals(BadRequestException.class, exception.getClass());
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
        willThrow(new FacadeLayerException(new ServiceLayerException("Cannot finish given training run.",
                ErrorCode.RESOURCE_CONFLICT))).given(trainingRunFacade).finishTrainingRun(trainingRun1.getId());
        Exception ex = mockMvc.perform(put("/training-runs/{runId}", trainingRun1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, ex.getClass());
        assertEquals("Cannot finish given training run.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void finishTrainingRunNotFound() throws Exception {
        willThrow(new FacadeLayerException(new ServiceLayerException("Training run not found.",
                ErrorCode.RESOURCE_NOT_FOUND))).given(trainingRunFacade).finishTrainingRun(trainingRun1.getId());
        Exception ex = mockMvc.perform(put("/training-runs/{runId}", trainingRun1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, ex.getClass());
        assertEquals("Training run not found.", ex.getCause().getCause().getLocalizedMessage());
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
        willThrow(new FacadeLayerException(new ServiceLayerException("Cannot finish given training run.",
                ErrorCode.RESOURCE_CONFLICT))).given(trainingRunFacade).resumeTrainingRun(trainingRun1.getId());
        Exception ex = mockMvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResolvedException();
        assertEquals(ConflictException.class, ex.getClass());
        assertEquals("Cannot finish given training run.", ex.getCause().getCause().getLocalizedMessage());
    }

    @Test
    public void resumeTrainingRunNotFound() throws Exception {
        willThrow(new FacadeLayerException(new ServiceLayerException("Training run not found.",
                ErrorCode.RESOURCE_NOT_FOUND))).given(trainingRunFacade).resumeTrainingRun(trainingRun1.getId());
        Exception ex = mockMvc.perform(get("/training-runs/{runId}/resumption", trainingRun1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();
        assertEquals(ResourceNotFoundException.class, ex.getClass());
        assertEquals("Training run not found.", ex.getCause().getCause().getLocalizedMessage());
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }


}
