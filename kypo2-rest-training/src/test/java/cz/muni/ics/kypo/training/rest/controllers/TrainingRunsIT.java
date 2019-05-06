package cz.muni.ics.kypo.training.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;

import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;

import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import cz.muni.ics.kypo.training.rest.exceptions.BadRequestException;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingRunsRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
@TestPropertySource(properties = {"openstack-server.uri=http://localhost:8080"})
public class TrainingRunsIT {

	private MockMvc mvc;
	private BeanMapping beanMapping;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private TrainingRunsRestController trainingRunsRestController;

	@Autowired
	private TrainingRunRepository trainingRunRepository;

	@Autowired
	private UserRefRepository userRefRepository;

	@Autowired
	private TrainingDefinitionRepository trainingDefinitionRepository;

	@Autowired
	private TrainingInstanceRepository trainingInstanceRepository;

	@Autowired
	private InfoLevelRepository infoLevelRepository;

	@Autowired
	private GameLevelRepository gameLevelRepository;

	@Autowired
	private AssessmentLevelRepository assessmentLevelRepository;

	@Autowired
	private HintRepository hintRepository;

	private TrainingRun trainingRun1, trainingRun2;
	private AssessmentLevel assessmentLevel1;
	private GameLevel gameLevel1;
	private InfoLevel infoLevel1;
	private IsCorrectFlagDTO isCorrectFlagDTO;
	private HintDTO hintDTO;
	private Hint hint;
	private SandboxInfo sandboxInfo;
	private Long unexistingTrId;


	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.mvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

		beanMapping = new BeanMappingImpl(new ModelMapper());

		SandboxInstanceRef sIR1 = new SandboxInstanceRef();
		sIR1.setSandboxInstanceRef(1L);
		SandboxInstanceRef sIR2 = new SandboxInstanceRef();
		sIR2.setSandboxInstanceRef(2L);

		InfoLevel iL = new InfoLevel();
		iL.setContent("content");
		iL.setTitle("title");
		iL.setMaxScore(50);
		InfoLevel infoLevel = infoLevelRepository.save(iL);

		GameLevel gL = new GameLevel();
		gL.setContent("gameContent");
		gL.setTitle("gameTitle");
		gL.setSolution("gameSolution");
		gL.setFlag("gameFlag");
		gL.setSolutionPenalized(true);
		GameLevel gameLevel = gameLevelRepository.save(gL);

		AssessmentLevel aL = new AssessmentLevel();
		aL.setAssessmentType(AssessmentType.TEST);
		aL.setInstructions("instruction");
		aL.setQuestions("[]");
		aL.setTitle("assessmentTitle");
		assessmentLevelRepository.save(aL);

		UserRef userRef = new UserRef();
		userRef.setUserRefLogin("testDesigner");
		UserRef uR = userRefRepository.save(userRef);

		BetaTestingGroup betaTestingGroup = new BetaTestingGroup();
		betaTestingGroup.setOrganizers(new HashSet<>(Arrays.asList(uR)));

		TrainingDefinition definition = new TrainingDefinition();
		definition.setTitle("definition");
		definition.setState(TDState.RELEASED);
		definition.setShowStepperBar(true);
		definition.setBetaTestingGroup(betaTestingGroup);
		definition.setSandboxDefinitionRefId(1L);
		//definition.setStartingLevel(1L);
		definition.setLastEdited(LocalDateTime.now());
		TrainingDefinition tD = trainingDefinitionRepository.save(definition);

		TrainingInstance trainingInstance = new TrainingInstance();
		trainingInstance.setStartTime(LocalDateTime.now().minusHours(24));
		trainingInstance.setEndTime(LocalDateTime.now().plusHours(80));
		trainingInstance.setTitle("futureInstance");
		trainingInstance.setPoolSize(20);
		trainingInstance.setPoolId(1L);
		trainingInstance.setAccessToken("pass-1234");
		trainingInstance.setTrainingDefinition(tD);
		trainingInstance.setOrganizers(new HashSet<>(Arrays.asList(uR)));
		TrainingInstance tI = trainingInstanceRepository.save(trainingInstance);
		tI.addSandboxInstanceRef(sIR1);
		tI.addSandboxInstanceRef(sIR2);
		tI = trainingInstanceRepository.save(tI);

		List<SandboxInstanceRef> sandboxInstanceRefs = new ArrayList<>();
		sandboxInstanceRefs.addAll(tI.getSandboxInstanceRefs());

		unexistingTrId = 100L;

		hint = new Hint();
		hint.setTitle("hintTitle");
		hint.setContent("hintContent");
		hint.setHintPenalty(10);

		hintDTO = new HintDTO();
		hintDTO.setContent("hint content");
		hintDTO.setTitle("hint title");

		isCorrectFlagDTO = new IsCorrectFlagDTO();
		isCorrectFlagDTO.setCorrect(true);
		isCorrectFlagDTO.setRemainingAttempts(-10);

		infoLevel1 = new InfoLevel();
		infoLevel1.setTitle("info1");
		infoLevel1.setContent("testContent");
		infoLevel1.setMaxScore(0);

		assessmentLevel1 = new AssessmentLevel();
		assessmentLevel1.setTitle("assessment1");
		assessmentLevel1.setAssessmentType(cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType.QUESTIONNAIRE);
		assessmentLevel1.setInstructions("testInstructions");
		assessmentLevel1.setQuestions("[]");
		assessmentLevel1.setMaxScore(20);

		gameLevel1 = new GameLevel();
		gameLevel1.setTitle("testTitle");
		gameLevel1.setContent("testContent");
		gameLevel1.setFlag("testFlag");
		gameLevel1.setSolution("testSolution");
		gameLevel1.setSolutionPenalized(true);
		gameLevel1.setMaxScore(30);
		gameLevel1.setHints(Set.of(hint));

		trainingRun1 = new TrainingRun();
		trainingRun1.setStartTime(LocalDateTime.now().minusHours(24));
		trainingRun1.setEndTime(LocalDateTime.now().plusHours(48));
		trainingRun1.setState(TRState.READY);
		trainingRun1.setIncorrectFlagCount(5);
		trainingRun1.setSolutionTaken(false);
		trainingRun1.setCurrentLevel(infoLevel);
		trainingRun1.setLevelAnswered(true);
		trainingRun1.setTrainingInstance(tI);
		trainingRun1.setSandboxInstanceRef(sandboxInstanceRefs.get(0));
		trainingRun1.setParticipantRef(uR);

		trainingRun2 = new TrainingRun();
		trainingRun2.setStartTime(LocalDateTime.now().plusHours(2));
		trainingRun2.setEndTime(LocalDateTime.now().plusHours(4));
		trainingRun2.setState(TRState.ARCHIVED);
		trainingRun2.setIncorrectFlagCount(10);
		trainingRun2.setSolutionTaken(true);
		trainingRun2.setCurrentLevel(gameLevel);
		trainingRun2.setLevelAnswered(false);
		trainingRun2.setTrainingInstance(tI);
		trainingRun2.setSandboxInstanceRef(sandboxInstanceRefs.get(1));
		trainingRun2.setParticipantRef(uR);

		sandboxInfo = new SandboxInfo();
		sandboxInfo.setId(1L);
		sandboxInfo.setStatus("CREATE_COMPLETE");

	}

	@After
	public void reset() throws SQLException {
		DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_run", "abstract_level");
	}

	@Test
	public void findTrainingRunById() throws Exception {
		TrainingRun tR = trainingRunRepository.save(trainingRun1);

		MockHttpServletResponse result = mvc.perform(get("/training-runs/{id}", tR.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		TrainingRunDTO runDTO = beanMapping.mapTo(tR, TrainingRunDTO.class);
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(runDTO)), result.getContentAsString());
	}

	@Test
	public void findTrainingRunByIdWithInstanceNotFound() throws Exception {
		Exception ex = mvc.perform(get("/training-runs/{id}", 100L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training Run with runId: 100 not found."));
	}

	@Test
	public void findAllTrainingRuns() throws Exception {
		TrainingRun tR1 = trainingRunRepository.save(trainingRun1);
		TrainingRun tR2 = trainingRunRepository.save(trainingRun2);

		List<TrainingRun> expected = new ArrayList<>();
		expected.add(tR1);
		expected.add(tR2);
		Page p = new PageImpl<>(expected);

		PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource = beanMapping.mapToPageResultDTO(p, TrainingRunDTO.class);
		PageResultResource.Pagination pagination = trainingRunDTOPageResultResource.getPagination();
		pagination.setSize(20);
		trainingRunDTOPageResultResource.setPagination(pagination);

		MockHttpServletResponse result = mvc.perform(get("/training-runs"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingRunDTOPageResultResource)), result.getContentAsString());
	}

	@Test
	public void accessTrainingRunAccessTokenNotFound() throws Exception {
		trainingRunRepository.save(trainingRun2);

		Exception ex = mvc.perform(post("/training-runs")
				.param("accessToken", "notFoundToken"))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, ex.getClass());
	}

	@Test
	public void getAllAccessedTrainingRuns() throws Exception {
		TrainingRun tR1 = trainingRunRepository.save(trainingRun1);
		TrainingRun tR2 = trainingRunRepository.save(trainingRun2);

		List<TrainingRun> expected = Arrays.asList(tR1, tR2);
		Page p = new PageImpl<>(expected);

		PageResultResource<AccessedTrainingRunDTO> accessedTrainingRunDTOPageResultResource = beanMapping.mapToPageResultDTO(p, AccessedTrainingRunDTO.class);
		PageResultResource.Pagination pagination = accessedTrainingRunDTOPageResultResource.getPagination();
		pagination.setSize(20);
		accessedTrainingRunDTOPageResultResource.setPagination(pagination);

		mockSpringSecurityContextForGet();

		mvc.perform(get("/training-runs/accessible"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
	}

	@Test
	public void getNextLevelNoLevelAnswered() throws Exception {
		TrainingRun tR2 = trainingRunRepository.save(trainingRun2);

		mockSpringSecurityContextForGet();

		Exception ex = mvc.perform(get("/training-runs/{runId}/next-levels", tR2.getCurrentLevel().getId()))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, ex.getClass());
	}


	@Test
	public void getSolutionNotGameLevel() throws Exception {
		TrainingRun tR1 = trainingRunRepository.save(trainingRun1);

		Exception ex = mvc.perform(get("/training-runs/{runId}/solutions", tR1.getId()))
				.andExpect(status().isBadRequest())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), BadRequestException.class);
		assertTrue(ex.getMessage().contains("Current level is not game level and does not have solution."));
	}

	@Test
	public void getHintNotFound() throws Exception {
		TrainingRun tR2 = trainingRunRepository.save(trainingRun2);
		Exception ex = mvc.perform(get("/training-runs/{runId}/hints/{hintId}", tR2.getId(), 10L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, ex.getClass());
	}

	@Test
	public void getHintWrongLevelType() throws Exception {
		TrainingRun tR1 = trainingRunRepository.save(trainingRun1);
		Hint hint1 = hintRepository.save(hint);

		Exception exception = mvc.perform(get("/training-runs/{runId}/hints/{hintId}", tR1.getId(), hint1.getId()))
				.andExpect(status().isBadRequest())
				.andReturn().getResolvedException();
		assertEquals(BadRequestException.class, exception.getClass());
	}

	@Test
	public void isCorrectFlagNoGameLevel() throws Exception {
		TrainingRun tR = trainingRunRepository.save(trainingRun1);

		mockSpringSecurityContextForGet();

		Exception ex = mvc.perform(get("/training-runs/{runId}/is-correct-flag", tR.getId())
				.param("flag", "gameFlag")
				.param("solutionTaken", "true"))
				.andExpect(status().isBadRequest()).andReturn().getResolvedException();

		assertEquals(BadRequestException.class, ex.getClass());
	}

	@Test
	public void resumeTrainingRunCannotBeArchived() throws Exception {
		TrainingRun tR2 = trainingRunRepository.save(trainingRun2);

		Exception ex = mvc.perform(get("/training-runs/{runId}/resumption", tR2.getId()))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();

		assertEquals(ConflictException.class, ex.getClass());
		assertEquals("ServiceLayerException : Cannot resume archived training run.", ex.getLocalizedMessage());
	}

	@Test
	public void resumeTrainingRunNotFound() throws Exception {
		Long unexistingTrId = 10L;
		Exception ex = mvc.perform(get("/training-runs/{runId}/resumption", unexistingTrId))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ResourceNotFoundException.class, ex.getClass());
		assertEquals("ServiceLayerException : Training Run with id: " + unexistingTrId + " not found.", ex.getLocalizedMessage());
	}

	@Test
	public void finishTrainingRunNotFound() throws Exception {
		Exception ex = mvc.perform(put("/training-runs/{runId}", unexistingTrId))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ResourceNotFoundException.class, ex.getClass());
		assertEquals("ServiceLayerException : Training Run with runId: " + unexistingTrId + " not found.", ex.getLocalizedMessage());
	}

	private static String convertObjectToJsonBytes(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}

	private void mockSpringSecurityContextForGet() {
		JsonObject sub = new JsonObject();
		sub.addProperty("sub", "testDesigner");
		Authentication authentication = Mockito.mock(Authentication.class);
		OAuth2Authentication auth = Mockito.mock(OAuth2Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		given(securityContext.getAuthentication()).willReturn(auth);
		given(auth.getUserAuthentication()).willReturn(auth);
		given(auth.getCredentials()).willReturn(sub);
		given(auth.getAuthorities()).willReturn(Arrays.asList(new SimpleGrantedAuthority("ADMINISTRATOR")));
		given(authentication.getDetails()).willReturn(auth);
	}
}
