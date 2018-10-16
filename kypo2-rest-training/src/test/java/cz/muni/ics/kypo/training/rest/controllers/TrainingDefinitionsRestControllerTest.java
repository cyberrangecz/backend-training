package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Predicates;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AuthorRefDTO;
import cz.muni.ics.kypo.training.api.dto.SandboxDefinitionRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.rest.config.RestConfigTest;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import io.swagger.annotations.ApiParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TrainingDefinitionsRestController.class)
@Import(RestConfigTest.class)
public class TrainingDefinitionsRestControllerTest {

	@Autowired
	private TrainingDefinitionsRestController trainingDefinitionsRestController;

	@MockBean
	private TrainingDefinitionFacade trainingDefinitionFacade;

	private MockMvc mockMvc;

	@MockBean
	@Qualifier("objMapperRESTApi")
	private ObjectMapper objectMapper;

	private TrainingDefinition trainingDefinition1, trainingDefinition2;

	private TrainingDefinitionDTO trainingDefinition1DTO, trainingDefinition2DTO;
	private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
	private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO;

	private GameLevel gameLevel;
	private GameLevelUpdateDTO gameLevelUpdateDTO;

	private InfoLevel infoLevel;
	private InfoLevelUpdateDTO infoLevelUpdateDTO;

	private AssessmentLevel assessmentLevel;
	private AssessmentLevelUpdateDTO alUpdateDTO;

	private Page p;

	private PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTOPageResultResource;

	private Predicate predicate;

	@ApiParam(value = "Pagination support.", required = false)
	private Pageable pageable;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

		predicate = (Predicate) Predicates.alwaysTrue();

		gameLevel = new GameLevel();
		gameLevel.setId(1L);
		gameLevel.setTitle("GameTest");
		gameLevel.setContent("content");
		gameLevel.setSolution("solution");
		gameLevel.setFlag("FlagTest");
		gameLevel.setIncorrectFlagLimit(5);
		gameLevel.setMaxScore(50);
		gameLevel.setEstimatedDuration(30);

		gameLevelUpdateDTO = new GameLevelUpdateDTO();
		gameLevelUpdateDTO.setId(2L);
		gameLevelUpdateDTO.setTitle("title");
		gameLevelUpdateDTO.setAttachments(new String[3]);
		gameLevelUpdateDTO.setContent("Content");
		gameLevelUpdateDTO.setEstimatedDuration(1000);
		gameLevelUpdateDTO.setFlag("flag1");
		gameLevelUpdateDTO.setIncorrectFlagLimit(4);
		gameLevelUpdateDTO.setSolutionPenalized(true);
		gameLevelUpdateDTO.setMaxScore(20);
/*
		gameLevelCreateDTO = new GameLevelCreateDTO();
		gameLevelCreateDTO.setTitle("title");
		gameLevelCreateDTO.setAttachments(new String[3]);
		gameLevelCreateDTO.setContent("Content");
		gameLevelCreateDTO.setEstimatedDuration(1000);
		gameLevelCreateDTO.setFlag("flag1");
		gameLevelCreateDTO.setIncorrectFlagLimit(4);
		gameLevelCreateDTO.setNextLevel(2L);
*/
		infoLevelUpdateDTO = new InfoLevelUpdateDTO();
		infoLevelUpdateDTO.setId(3L);
		infoLevelUpdateDTO.setTitle("some title");
		infoLevelUpdateDTO.setContent("some content");
/*
		infoLevelCreateDTO = new InfoLevelCreateDTO();
		infoLevelCreateDTO.setMaxScore(40);
		infoLevelCreateDTO.setTitle("some title");
		infoLevelCreateDTO.setContent("some content");
		infoLevelCreateDTO.setNextLevel(gameLevel.getId());
*/
		infoLevel = new InfoLevel();
		infoLevel.setId(2L);
		infoLevel.setTitle("InfoTest");
		infoLevel.setContent("content");

		assessmentLevel = new AssessmentLevel();
		assessmentLevel.setId(3L);
		assessmentLevel.setTitle("AssTest");
		assessmentLevel.setAssessmentType(AssessmentType.TEST);
		assessmentLevel.setQuestions("questions");
/*
		alCreateDTO = new AssessmentLevelCreateDTO();
		alCreateDTO.setInstructions("instructions");
		alCreateDTO.setMaxScore(50);
		alCreateDTO.setNextLevel(1L);
		alCreateDTO.setQuestions("test");
		alCreateDTO.setTitle("Some title");
		alCreateDTO.setType(AssessmentType.QUESTIONNAIRE);
*/
		alUpdateDTO = new AssessmentLevelUpdateDTO();
		alUpdateDTO.setInstructions("instructions");
		alUpdateDTO.setMaxScore(50);
		alUpdateDTO.setQuestions("test");
		alUpdateDTO.setTitle("Some title");
		alUpdateDTO.setType(AssessmentType.QUESTIONNAIRE);

		AuthorRef authorRef = new AuthorRef();
		Set<AuthorRef> authorRefSet = new HashSet<>();
		authorRefSet.add(authorRef);

		AuthorRefDTO authorRefDTO = new AuthorRefDTO();
		Set<AuthorRefDTO> authorRefSetDTO = new HashSet<>();
		authorRefSetDTO.add(authorRefDTO);

		SandboxDefinitionRef sandboxDefinitionRef = new SandboxDefinitionRef();
		SandboxDefinitionRefDTO sandboxDefinitionRefDTO = new SandboxDefinitionRefDTO();

		trainingDefinition1 = new TrainingDefinition();
		trainingDefinition1.setId(1L);
		trainingDefinition1.setState(TDState.UNRELEASED);
		trainingDefinition1.setStartingLevel(1L);
		trainingDefinition1.setTitle("test");
		trainingDefinition1.setAuthorRef(authorRefSet);
		trainingDefinition1.setSandBoxDefinitionRef(sandboxDefinitionRef);

		trainingDefinition2 = new TrainingDefinition();
		trainingDefinition2.setId(2L);
		trainingDefinition2.setState(TDState.PRIVATED);
		trainingDefinition2.setTitle("test");
		trainingDefinition2.setAuthorRef(authorRefSet);
		trainingDefinition2.setSandBoxDefinitionRef(sandboxDefinitionRef);

		trainingDefinition1DTO = new TrainingDefinitionDTO();
		trainingDefinition1DTO.setId(1L);
		trainingDefinition1DTO.setState(TDState.UNRELEASED);
		trainingDefinition1DTO.setTitle("test");
		trainingDefinition1DTO.setAuthorRef(authorRefSetDTO);
		trainingDefinition1DTO.setSandBoxDefinitionRefDTO(sandboxDefinitionRefDTO);

		trainingDefinition2DTO = new TrainingDefinitionDTO();
		trainingDefinition2DTO.setId(2L);
		trainingDefinition2DTO.setState(TDState.PRIVATED);
		trainingDefinition2DTO.setTitle("test");
		trainingDefinition2DTO.setAuthorRef(authorRefSetDTO);
		trainingDefinition2DTO.setSandBoxDefinitionRefDTO(sandboxDefinitionRefDTO);

		trainingDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();
		trainingDefinitionUpdateDTO.setId(4L);
		trainingDefinitionUpdateDTO.setState(TDState.UNRELEASED);
		trainingDefinitionUpdateDTO.setTitle("training definition title");
		trainingDefinitionUpdateDTO.setAuthorRef(authorRefSetDTO);
		trainingDefinitionUpdateDTO.setSandBoxDefinitionRef(sandboxDefinitionRefDTO);
		trainingDefinitionUpdateDTO.setShowStepperBar(false);

		trainingDefinitionCreateDTO = new TrainingDefinitionCreateDTO();
		trainingDefinitionCreateDTO.setDescription("TD desc");
		trainingDefinitionCreateDTO.setOutcomes(new String[0]);
		trainingDefinitionCreateDTO.setPrerequisities(new String[0]);
		trainingDefinitionCreateDTO.setState(TDState.ARCHIVED);
		trainingDefinitionCreateDTO.setTitle("TD some title");

		List<TrainingDefinition> expected = new ArrayList<>();
		expected.add(trainingDefinition1);
		expected.add(trainingDefinition2);

		p = new PageImpl<>(expected);

		ObjectMapper obj = new ObjectMapper();
		obj.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		given(objectMapper.getSerializationConfig()).willReturn(obj.getSerializationConfig());

		BeanMapping bM = new BeanMappingImpl(new ModelMapper());
		trainingDefinitionDTOPageResultResource = bM.mapToPageResultDTO(p, TrainingDefinitionDTO.class);

	}

	@Test
	public void findTrainingDefinitionById() throws Exception {
		given(trainingDefinitionFacade.findById(any(Long.class))).willReturn(trainingDefinition1DTO);
		String valueTd = convertObjectToJsonBytes(trainingDefinition1DTO);
		given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
		MockHttpServletResponse result = mockMvc.perform(get("/training-definitions" + "/{id}", 1l)).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingDefinition1DTO)), result.getContentAsString());
	}

	@Test
	public void findTrainingDefinitionByIdWithFacadeException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).findById(any(Long.class));
		Exception exception =
				mockMvc.perform(get("/training-definitions" + "/{id}", 6l)).andExpect(status().isNotFound()).andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	@Test
	public void findAllTrainingDefinitions() throws Exception {
		PageResultResource<TrainingDefinitionDTO> expected = new PageResultResource<>(Arrays.asList(trainingDefinition1DTO, trainingDefinition2DTO));
		given(trainingDefinitionFacade.findAll(predicate, pageable)).willReturn(expected);
		String valueTd = convertObjectToJsonBytes(expected);
		given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
		MockHttpServletResponse result = mockMvc.perform(get("/training-definitions")).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(expected)), result.getContentAsString());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void findAllTrainingDefinitions_withFacadeException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).findAll((Predicate) Predicates.alwaysTrue(), PageRequest.of(0, 10));
		Exception exception =	mockMvc.perform(get("/training-definitions")).andExpect(status().isNotFound()).andReturn().getResolvedException();
		//assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	@Test
	public void findAllTrainingDefinitionsBySandboxDefinitionId() throws Exception {

	}

	@Test
	public void createTrainingDefinition() throws Exception {

	}

	@Test
	public void updateTrainingDefinition() throws Exception {
		mockMvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNoContent());
	}

	@Test
	public void updateTrainingDefinitionWithFacadeException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).update(any(TrainingDefinitionUpdateDTO.class));
		Exception exception = mockMvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();
		assertEquals(ConflictException.class, exception.getClass());
	}

	@Test
	public void cloneTrainingDefinitionWithFacadeException() throws Exception {
		Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).clone(any(Long.class));
		Exception exception = mockMvc.perform(post("/training-definitions/1").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict()).andReturn().getResolvedException();
		assertEquals(ConflictException.class, exception.getClass());
	}

	@Test
	public void swapLeft() throws Exception {
		mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-left", trainingDefinition1.getId(), gameLevel.getId()))
				.andExpect(status().isOk());
	}

	@Test
	public void swapLeftWithCannotBeUpdatedException() throws Exception {
		Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).swapLeft(any(Long.class), any(Long.class));
		Exception exception = mockMvc
				.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-left", trainingDefinition1.getId(), gameLevel.getId()))
				.andExpect(status().isConflict()).andReturn().getResolvedException();
		assertEquals(ConflictException.class, exception.getClass());
	}

	@Test
	public void swapLeftWithFacadeLayerException() throws Exception {
		Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).swapLeft(any(Long.class), any(Long.class));
		Exception exception = mockMvc
				.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-left", trainingDefinition1.getId(), gameLevel.getId()))
				.andExpect(status().isNotFound()).andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	@Test
	public void swapRight() throws Exception {
		mockMvc.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-right", trainingDefinition1.getId(), gameLevel.getId()))
				.andExpect(status().isOk());
	}

	@Test
	public void swapRightWithCannotBeUpdatedException() throws Exception {
		Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).swapRight(any(Long.class), any(Long.class));
		Exception exception = mockMvc
				.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-right", trainingDefinition1.getId(), gameLevel.getId()))
				.andExpect(status().isConflict()).andReturn().getResolvedException();
		assertEquals(ConflictException.class, exception.getClass());
	}

	@Test
	public void swapRightWithFacadeLayerException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).swapRight(any(Long.class), any(Long.class));
		Exception exception = mockMvc
				.perform(put("/training-definitions/{definitionId}/levels/{levelId}/swap-right", trainingDefinition1.getId(), gameLevel.getId()))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	@Test
	public void deleteTrainingDefinition() throws Exception {
		mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition1.getId())).andExpect(status().isOk());
	}

	@Test
	public void deleteTrainingDefinitionWithCannotBeDeletedException() throws Exception {
		Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).delete(any(Long.class));
		Exception exception = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition2.getId()))
				.andExpect(status().isConflict()).andReturn().getResolvedException();

		assertEquals(ConflictException.class, exception.getClass());
	}

	@Test
	public void deleteTrainingDefinitionWithFacadeLayerException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).delete(any(Long.class));
		Exception exception = mockMvc.perform(delete("/training-definitions/{id}", trainingDefinition2.getId()))
				.andExpect(status().isNotFound()).andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	@Test
	public void deleteLevel() throws Exception {
		mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition1.getId(), gameLevel.getId()))
				.andExpect(status().isOk());
	}

	@Test
	public void deleteLevelWithCannotBeUpdatedException() throws Exception {
		Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
		Exception exception =
				mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition2.getId(), gameLevel.getId()))
						.andExpect(status().isConflict()).andReturn().getResolvedException();
		assertEquals(ConflictException.class, exception.getClass());
	}

	@Test
	public void deleteLevelWithFacadeLayerException() throws Exception {
		Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).deleteOneLevel(any(Long.class), any(Long.class));
		Exception exception =
				mockMvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", trainingDefinition2.getId(), gameLevel.getId()))
						.andExpect(status().isNotFound()).andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	@Test
	public void updateGameLevel() throws Exception {
		mockMvc
				.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition1.getId())
						.content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNoContent());
	}

	@Test
	public void updateGameLevelWithFacadeLayerException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevelUpdateDTO.class));
		Exception exception = mockMvc
				.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition2.getId())
						.content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound()).andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	@Test
	public void updateGameLevelWithCannotBeUpdatedException() throws Exception {
		Exception exceptionToThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionToThrow)).given(trainingDefinitionFacade).updateGameLevel(any(Long.class), any(GameLevelUpdateDTO.class));
		Exception exception = mockMvc
				.perform(put("/training-definitions/{definitionId}/game-levels", trainingDefinition2.getId())
						.content(convertObjectToJsonBytes(gameLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict()).andReturn().getResolvedException();
		assertEquals(ConflictException.class, exception.getClass());
	}

	@Test
	public void updateInfoLevel() throws Exception {

		mockMvc.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition1.getId())
				.content(convertObjectToJsonBytes(infoLevel)).contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNoContent());
	}

	@Test
	public void updateInfoLevelWithFacadeLayerException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_NOT_FOUND);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevelUpdateDTO.class));
		Exception exception = mockMvc
				.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition2.getId())
						.content(convertObjectToJsonBytes(infoLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound()).andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, exception.getClass());
	}

	@Test
	public void updateInfoLevelWithCannotBeUpdatedException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).updateInfoLevel(any(Long.class), any(InfoLevelUpdateDTO .class));
		Exception exception = mockMvc
				.perform(put("/training-definitions/{definitionId}/info-levels", trainingDefinition2.getId())
						.content(convertObjectToJsonBytes(infoLevelUpdateDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict()).andReturn().getResolvedException();
		assertEquals(ConflictException.class, exception.getClass());
	}

	@Test
	public void updateAssessmentLevel() throws Exception {

	}

	@Test
	public void findLevelById() throws Exception {

	}

	@Test
	public void createLevel() throws Exception {

	}

/*
	@Test
	public void createTrainingDefinition() throws Exception {
		String valueTd = convertObjectToJsonBytes(trainingDefinitionCreateDTO);
		given(objectMapper.writeValueAsString(any(Object.class))).willReturn(valueTd);
		given(trainingDefinitionFacade.create(any(TrainingDefinitionCreateDTO.class))).willReturn(trainingDefinition1DTO);
		MockHttpServletResponse result = mockMvc
				.perform(post("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		assertEquals(convertObjectToJsonBytes(trainingDefinitionCreateDTO), result.getContentAsString());
	}

	@Test
	public void createTrainingDefinitionWithFacadeException() throws Exception {
		Exception exceptionThrow = new ServiceLayerException("message", ErrorCode.RESOURCE_CONFLICT);
		willThrow(new FacadeLayerException(exceptionThrow)).given(trainingDefinitionFacade).create(any(TrainingDefinitionCreateDTO.class));
		Exception exception = mockMvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isNotAcceptable()).andReturn().getResolvedException();
		assertEquals(ResourceNotCreatedException.class, exception.getClass());
	}
*/


	private static String convertObjectToJsonBytes(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}
}
