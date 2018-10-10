package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.rest.exceptions.BadRequestException;
import cz.muni.ics.kypo.training.rest.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.training.rest.exceptions.ServiceUnavailableException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrainingRunsRestController.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo")
public class TrainingRunsRestControllerTest {

		@Autowired
		private TrainingRunsRestController trainingRunsRestController;

		@MockBean
		private TrainingRunFacade trainingRunFacade;

		private MockMvc mockMvc;

		@MockBean
		@Qualifier("objMapperRESTApi")
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


		@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

			trainingRun1 = new TrainingRun();
			trainingRun1.setId(1L);
			trainingRun1.setState(TRState.ARCHIVED);

			trainingRun2 = new TrainingRun();
			trainingRun2.setId(2L);
			trainingRun2.setState(TRState.READY);

			trainingRun1DTO = new TrainingRunDTO();
			trainingRun1DTO.setId(1L);
			trainingRun1DTO.setState(TRState.ARCHIVED);

			trainingRun2DTO = new TrainingRunDTO();
			trainingRun2DTO.setId(2L);
			trainingRun2DTO.setState(TRState.READY);

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
			assessmentLevelDTO.setAssessmentType(AssessmentType.TEST);
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
			pAccessed = new PageImpl<AccessedTrainingRunDTO>(accessed);

			List<TrainingRun> expected = new ArrayList<>();
			expected.add(trainingRun1);
			expected.add(trainingRun2);

			p = new PageImpl<TrainingRun>(expected);

			ObjectMapper obj = new ObjectMapper();
			obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
			given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

			BeanMapping bM = new BeanMappingImpl(new ModelMapper());
			trainingRunDTOPageResultResource = bM.mapToPageResultDTO(p, TrainingRunDTO.class);


	}

		@Test
		public void findTrainingRunById() throws Exception {
				given(trainingRunFacade.findById(any(Long.class))).willReturn(trainingRun1DTO);
				String valueTr = convertObjectToJsonBytes(trainingRun1DTO);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
				MockHttpServletResponse result = mockMvc.perform(get("/training-runs" + "/{id}", 1l))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRun1DTO)), result.getContentAsString());
		}

		@Test
		public void findTrainingRunByIdWithFacadeException() throws Exception {
				Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
				willThrow(new FacadeLayerException(exceptionThrow)).given(trainingRunFacade).findById(any(Long.class));
				Exception exception = mockMvc.perform(get("/training-runs" + "/{id}", 6l))
						.andExpect(status().isNotFound())
						.andReturn().getResolvedException();
				assertEquals(ResourceNotFoundException.class, exception.getClass());
		}

		@Test
		public void findAllTrainingRuns() throws Exception {
				String valueTr = convertObjectToJsonBytes(trainingRunDTOPageResultResource);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
				given(trainingRunFacade.findAll(any(Predicate.class),any(Pageable.class))).willReturn(trainingRunDTOPageResultResource);

				MockHttpServletResponse result = mockMvc.perform(get("/training-runs"))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRunDTOPageResultResource)), result.getContentAsString());
		}

		@Test
		public void getAllAccessedTrainingRuns() throws Exception {
				BeanMapping bM = new BeanMappingImpl(new ModelMapper());
				accessedTrainingRunDTOPage = bM.mapToPageResultDTO(pAccessed, AccessedTrainingRunDTO.class);

				String valueTr = convertObjectToJsonBytes(trainingRunDTOPageResultResource);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
				given(trainingRunFacade.findAllAccessedTrainingRuns(any(Pageable.class))).willReturn(accessedTrainingRunDTOPage);

				MockHttpServletResponse result = mockMvc.perform(get("/training-runs/accessed"))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRunDTOPageResultResource)), result.getContentAsString());
		}

		@Test
		public void accessTrainingRun() throws Exception {
				given(trainingRunFacade.accessTrainingRun("password")).willReturn(accessTrainingRunDTO);
				MockHttpServletResponse result = mockMvc.perform(get("/training-runs/access").param("password", "password"))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
		}

		@Test
		public void accessTrainingRunWithNoSandbox() throws Exception {
				Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.NO_AVAILABLE_SANDBOX);
				willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).accessTrainingRun("password");
				Exception exception = mockMvc.perform(get("/training-runs/access").param("password", "password"))
						.andExpect(status().isServiceUnavailable())
						.andReturn().getResolvedException();
				assertEquals(ServiceUnavailableException.class, exception.getClass());
		}

		@Test
		public void accessTrainingRunWithResourceNotFound() throws Exception {
				Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
				willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).accessTrainingRun("password");
				Exception exception = mockMvc.perform(get("/training-runs/access").param("password", "password"))
						.andExpect(status().isNotFound())
						.andReturn().getResolvedException();
				assertEquals(ResourceNotFoundException.class, exception.getClass());
		}

		@Test
		public void accessTrainingRunWithUnexpectedError() throws Exception {
				Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.UNEXPECTED_ERROR);
				willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).accessTrainingRun("password");
				Exception exception = mockMvc.perform(get("/training-runs/access").param("password", "password"))
						.andExpect(status().isInternalServerError())
						.andReturn().getResolvedException();
				assertEquals(InternalServerErrorException.class, exception.getClass());
		}
		@Test
		public void getNextLevelAssessment() throws Exception{
				String valueTr = convertObjectToJsonBytes(assessmentLevelDTO);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
				given(trainingRunFacade.getNextLevel(assessmentLevelDTO.getId())).willReturn(assessmentLevelDTO);
				MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{id}/get-next-level", 3L))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(assessmentLevelDTO)), result.getContentAsString());
		}
		@Test
		public void getNextLevelGame() throws Exception{
				String valueTr = convertObjectToJsonBytes(gameLevelDTO);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
				given(trainingRunFacade.getNextLevel(gameLevelDTO.getId())).willReturn(gameLevelDTO);
				MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{id}/get-next-level", 1L))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(gameLevelDTO)), result.getContentAsString());
		}

		@Test
		public void getNextLevelInfo() throws Exception{
				String valueTr = convertObjectToJsonBytes(infoLevelDTO);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
				given(trainingRunFacade.getNextLevel(infoLevelDTO.getId())).willReturn(infoLevelDTO);
				MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{id}/get-next-level", 2L))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(infoLevelDTO)), result.getContentAsString());
		}


		@Test
		public void getSolution() throws Exception{
				given(trainingRunFacade.getSolution(assessmentLevelDTO.getId())).willReturn("Solution");
				MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{id}/get-solution", 3L))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals("Solution", result.getContentAsString().replace("\"", ""));
		}

		@Test
		public void isCorrectFlag() throws Exception {
				given(trainingRunFacade.isCorrectFlag(trainingRun1.getId(), "flag", true)).willReturn(isCorrectFlagDTO);
				MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{id}/is-correct-flag", trainingRun1.getId())
						.param("flag", "flag")
						.param("solutionTaken", "true"))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
		}

		@Test
		public void getHint() throws Exception{
				String valueTr = convertObjectToJsonBytes(hintDTO);
				given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTr);
				given(trainingRunFacade.getHint(trainingRun1.getId(), hintDTO.getId())).willReturn(hintDTO);
				MockHttpServletResponse result = mockMvc.perform(get("/training-runs/{id}/get-hint/{hintId}", trainingRun1.getId(), hintDTO.getId()))
						.andExpect(status().isOk())
						.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
						.andReturn().getResponse();
				assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(hintDTO)), result.getContentAsString());
		}

		@Test
		public void getHintNotFound() throws Exception{
				Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
				willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).getHint(anyLong(), anyLong());
				Exception exception = mockMvc.perform(get("/training-runs/{id}/get-hint/{hintId}", trainingRun1.getId(), hintDTO.getId()))
						.andExpect(status().isNotFound())
						.andReturn().getResolvedException();
			System.out.println(exception);
				assertEquals(ResourceNotFoundException.class, exception.getClass());
		}

		@Test
		public void getHintWrongLevelType() throws Exception{
				Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.WRONG_LEVEL_TYPE);
				willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingRunFacade).getHint(anyLong(), anyLong());
				Exception exception = mockMvc.perform(get("/training-runs/{id}/get-hint/{hintId}", trainingRun1.getId(), hintDTO.getId()))
						.andExpect(status().isBadRequest())
						.andReturn().getResolvedException();
				assertEquals(BadRequestException.class, exception.getClass());
		}

		private static String convertObjectToJsonBytes(Object object) throws IOException {
				ObjectMapper mapper = new ObjectMapper();
				return mapper.writeValueAsString(object);
		}


}
