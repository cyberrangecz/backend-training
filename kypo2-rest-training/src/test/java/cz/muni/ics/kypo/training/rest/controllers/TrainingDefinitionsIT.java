package cz.muni.ics.kypo.training.rest.controllers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.betatestinggroup.BetaTestingGroupCreateDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMapping;
import cz.muni.ics.kypo.training.mapping.modelmapper.BeanMappingImpl;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.rest.controllers.config.DBTestUtil;
import cz.muni.ics.kypo.training.rest.controllers.config.RestConfigTest;
import cz.muni.ics.kypo.training.rest.exceptions.ConflictException;
import cz.muni.ics.kypo.training.rest.exceptions.ResourceNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * @author Boris Jadus(445343)
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TrainingDefinitionsRestController.class)
@DataJpaTest
@Import(RestConfigTest.class)
public class TrainingDefinitionsIT {

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private MockMvc mvc;
    private BeanMapping beanMapping;
    private static final Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrainingDefinitionsRestController.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private TrainingDefinitionsRestController trainingDefinitionsRestController;

	@Autowired
	private TrainingDefinitionRepository trainingDefinitionRepository;

	@Autowired
	private UserRefRepository userRefRepository;

	@Autowired
	private GameLevelRepository gameLevelRepository;

	@Autowired
	private InfoLevelRepository infoLevelRepository;

	@Autowired
	private AssessmentLevelRepository assessmentLevelRepository;

	private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO, invalidDefinitionUpdateDTO, updateForNonexistingDefinition;
	private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
	private TrainingDefinitionByIdDTO invalidDefinitionDTO;
	private TrainingDefinition releasedTrainingDefinition, unreleasedDefinition;
	private GameLevel gameLevel1, gameLevel2;
	private GameLevelUpdateDTO gameLevelUpdateDTO, invalidGameLevelUpdateDTO;
	private InfoLevel infoLevel1;
	private InfoLevelUpdateDTO infoLevelUpdateDTO, invalidInfoLevelUpdateDTO;
	private AssessmentLevel assessmentLevel1;
	private AssessmentLevelUpdateDTO assessmentLevelUpdateDTO, invalidAssessmentLevelUpdateDTO;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		this.mvc = MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
						new QuerydslPredicateArgumentResolver(new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
				.setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

		beanMapping = new BeanMappingImpl(new ModelMapper());

		UserRef userRef = new UserRef();
		userRef.setUserRefLogin("testDesigner");
		UserRef uR = userRefRepository.save(userRef);

		BetaTestingGroup betaTestingGroup = new BetaTestingGroup();
		betaTestingGroup.setOrganizers(new HashSet<>(Arrays.asList(uR)));

		BetaTestingGroup betaTestingGroup2 = new BetaTestingGroup();
		betaTestingGroup2.setOrganizers(new HashSet<>(Arrays.asList(uR)));

		BetaTestingGroupCreateDTO betaTestingGroupCreateDTO = new BetaTestingGroupCreateDTO();
		betaTestingGroupCreateDTO.setOrganizers(Set.of());

		BetaTestingGroupUpdateDTO betaTestingGroupUpdateDTO = new BetaTestingGroupUpdateDTO();
		betaTestingGroupUpdateDTO.setOrganizers(Set.of());

		gameLevel1 = new GameLevel();
		gameLevel1.setTitle("testTitle");
		gameLevel1.setContent("testContent");
		gameLevel1.setFlag("testFlag");
		gameLevel1.setSolution("testSolution");
		gameLevel1.setSolutionPenalized(true);
		gameLevel1.setMaxScore(25);

		gameLevelUpdateDTO = new GameLevelUpdateDTO();
		gameLevelUpdateDTO.setTitle("newTitle");
		gameLevelUpdateDTO.setContent("newContent");
		gameLevelUpdateDTO.setFlag("newFlag");
		gameLevelUpdateDTO.setSolution("newSolution");
		gameLevelUpdateDTO.setSolutionPenalized(false);
		gameLevelUpdateDTO.setMaxScore(50);

		invalidGameLevelUpdateDTO = new GameLevelUpdateDTO();

		gameLevel2 = new GameLevel();
		gameLevel2.setTitle("testTitle2");
		gameLevel2.setContent("testContent2");
		gameLevel2.setFlag("testFlag2");
		gameLevel2.setSolution("testSolution2");
		gameLevel2.setSolutionPenalized(false);
		gameLevel2.setMaxScore(50);

		infoLevel1 = new InfoLevel();
		infoLevel1.setTitle("info1");
		infoLevel1.setContent("testContent");
		infoLevel1.setMaxScore(0);

		infoLevelUpdateDTO = new InfoLevelUpdateDTO();
		infoLevelUpdateDTO.setTitle("newTitle");
		infoLevelUpdateDTO.setContent("newContent");

		invalidInfoLevelUpdateDTO = new InfoLevelUpdateDTO();

		assessmentLevel1 = new AssessmentLevel();
		assessmentLevel1.setTitle("assessment1");
		assessmentLevel1.setAssessmentType(cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType.QUESTIONNAIRE);
		assessmentLevel1.setInstructions("testInstructions");
		assessmentLevel1.setQuestions("[]");
		assessmentLevel1.setMaxScore(20);

		assessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();
		assessmentLevelUpdateDTO.setTitle("newTitle");
		assessmentLevelUpdateDTO.setType(AssessmentType.TEST);
		assessmentLevelUpdateDTO.setInstructions("newInstructions");
		assessmentLevelUpdateDTO.setQuestions("[]");
		assessmentLevelUpdateDTO.setMaxScore(50);

		invalidAssessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();

		trainingDefinitionCreateDTO = new TrainingDefinitionCreateDTO();
		trainingDefinitionCreateDTO.setTitle("testTitle");
		trainingDefinitionCreateDTO.setDescription("testDescription");
		trainingDefinitionCreateDTO.setShowStepperBar(true);
		trainingDefinitionCreateDTO.setState(TDState.UNRELEASED);
		trainingDefinitionCreateDTO.setSandboxDefinitionRefId(1L);
		trainingDefinitionCreateDTO.setBetaTestingGroup(betaTestingGroupCreateDTO);
		trainingDefinitionCreateDTO.setAuthors(Set.of());

		releasedTrainingDefinition = new TrainingDefinition();
		releasedTrainingDefinition.setTitle("released");
		releasedTrainingDefinition.setDescription("released");
		releasedTrainingDefinition.setShowStepperBar(true);
		releasedTrainingDefinition.setState(cz.muni.ics.kypo.training.persistence.model.enums.TDState.RELEASED);
		releasedTrainingDefinition.setSandboxDefinitionRefId(2L);
		releasedTrainingDefinition.setBetaTestingGroup(betaTestingGroup);
		releasedTrainingDefinition.setAuthors(new HashSet<>(Arrays.asList(uR)));
		releasedTrainingDefinition.setLastEdited(LocalDateTime.now());

		invalidDefinitionDTO = new TrainingDefinitionByIdDTO();

		unreleasedDefinition = new TrainingDefinition();
		unreleasedDefinition.setTitle("testTitle");
		unreleasedDefinition.setDescription("testDescription");
		unreleasedDefinition.setShowStepperBar(false);
		unreleasedDefinition.setState(cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED);
		unreleasedDefinition.setSandboxDefinitionRefId(1L);
		unreleasedDefinition.setBetaTestingGroup(betaTestingGroup2);
		unreleasedDefinition.setAuthors(new HashSet<>(Arrays.asList(uR)));
		unreleasedDefinition.setLastEdited(LocalDateTime.now());

		trainingDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();
		trainingDefinitionUpdateDTO.setTitle("newTitle");
		trainingDefinitionUpdateDTO.setDescription("newDescription");
		trainingDefinitionUpdateDTO.setShowStepperBar(true);
		trainingDefinitionUpdateDTO.setState(TDState.UNRELEASED);
		trainingDefinitionUpdateDTO.setSandboxDefinitionRefId(1L);
		trainingDefinitionUpdateDTO.setBetaTestingGroup(betaTestingGroupUpdateDTO);
		trainingDefinitionUpdateDTO.setAuthors(Set.of());

		invalidDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();

		updateForNonexistingDefinition = new TrainingDefinitionUpdateDTO();
		updateForNonexistingDefinition.setId(100L);
		updateForNonexistingDefinition.setTitle("test");
		updateForNonexistingDefinition.setState(TDState.UNRELEASED);
		updateForNonexistingDefinition.setSandboxDefinitionRefId(1L);
		updateForNonexistingDefinition.setBetaTestingGroup(betaTestingGroupUpdateDTO);
		updateForNonexistingDefinition.setAuthors(Set.of());
	}

	@After
	public void reset() throws SQLException {
		DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_definition", "abstract_level");

	}

	@Test
	public void findTrainingDefinitionById() throws Exception {
		TrainingDefinition expected = trainingDefinitionRepository.save(releasedTrainingDefinition);
		GameLevel gL1 = gameLevelRepository.save(gameLevel1);
		gL1.setTrainingDefinition(expected);
		trainingDefinitionRepository.save(expected);

		MockHttpServletResponse result = mvc.perform(get("/training-definitions" + "/{id}", expected.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		TrainingDefinitionByIdDTO definitionDTO = beanMapping.mapTo(expected, TrainingDefinitionByIdDTO.class);
		GameLevelDTO gLDTO = beanMapping.mapTo(gameLevel1, GameLevelDTO.class);
		gLDTO.setLevelType(LevelType.GAME_LEVEL);
		definitionDTO.setLevels(new ArrayList<>(Arrays.asList(gLDTO)));
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(definitionDTO)), result.getContentAsString());
	}

    @Test
    public void findTrainingDefinitionByIdWithDefinitionNotFound() throws Exception {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Exception ex = mvc.perform(get("/training-definitions" + "/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResolvedException();

        assertEquals(ex.getClass(), ResourceNotFoundException.class);
        assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));

        assertEquals("Training definition with id {} not found.", listAppender.list.get(0).getMessage());
        assertEquals(Level.ERROR, listAppender.list.get(0).getLevel());
    }
/*TODO poriesit canBeArchived
	@Test
	public void findAllTrainingDefinitions() throws Exception {
		TrainingDefinition tD1 = trainingDefinitionRepository.saveTrainingRunEvent(releasedTrainingDefinition);
		TrainingDefinition tD2 = trainingDefinitionRepository.saveTrainingRunEvent(unreleasedDefinition);

		List<TrainingDefinition> expected = new ArrayList<>();
		expected.add(tD1);
		expected.add(tD2);
		System.out.println(beanMapping.mapTo(tD1, TrainingDefinitionByIdDTO.class));

		Page p = new PageImpl<TrainingDefinition>(expected);

		PageResultResource<TrainingDefinitionByIdDTO> trainingDefinitionDTOPageResultResource = beanMapping.mapToPageResultDTO(p, TrainingDefinitionByIdDTO.class);
		PageResultResource.Pagination pagination = trainingDefinitionDTOPageResultResource.getPagination();
		pagination.setSize(20);
		trainingDefinitionDTOPageResultResource.setPagination(pagination);

		MockHttpServletResponse result = mvc.perform(get("/training-definitions"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(trainingDefinitionDTOPageResultResource)), result.getContentAsString());
	}
*/
	@Test
	public void createTrainingDefinition() throws Exception {
		mockSpringSecurityContextForGet();
		MockHttpServletResponse result = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		Optional<TrainingDefinition> newDefinition = trainingDefinitionRepository.findById(1L);
		assertTrue(newDefinition.isPresent());
		TrainingDefinitionByIdDTO newDefinitionDTO = beanMapping.mapTo(newDefinition.get(), TrainingDefinitionByIdDTO.class);
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(newDefinitionDTO)), result.getContentAsString());
	}

	@Test
	public void deleteReleasedTrainingDefinition() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);

		Exception ex = mvc.perform(delete("/training-definitions/{Id}", tD.getId()))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();
		System.out.println(tD);
		assertEquals(ex.getClass(), ConflictException.class);
		assertTrue(ex.getMessage().contains("Cannot delete released training definition"));
	}

	@Test
	public void createTrainingDefinitionWithInvalidDefinition() throws Exception{
		Exception ex = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(invalidDefinitionDTO))
		.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andReturn().getResolvedException();
		assertTrue(ex.getMessage().contains("Validation failed for argument"));
		assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
	}

	@Test
	public void updateTrainingDefinition() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		trainingDefinitionUpdateDTO.setId(tD.getId());
		System.out.println(tD);
		mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
		.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNoContent());
		Optional<TrainingDefinition> optionalDefinition = trainingDefinitionRepository.findById(tD.getId());
		assertTrue(optionalDefinition.isPresent());
		TrainingDefinition updatedDefinition = optionalDefinition.get();
		assertEquals(updatedDefinition.getTitle(), trainingDefinitionUpdateDTO.getTitle());
		assertEquals(updatedDefinition.getDescription(), trainingDefinitionUpdateDTO.getDescription());
		assertEquals(updatedDefinition.getState().toString(), trainingDefinitionUpdateDTO.getState().toString());
		assertEquals(updatedDefinition.isShowStepperBar(), trainingDefinitionUpdateDTO.isShowStepperBar());
		assertEquals(updatedDefinition.getAuthors(), unreleasedDefinition.getAuthors());
		assertEquals(updatedDefinition.getSandboxDefinitionRefId(), unreleasedDefinition.getSandboxDefinitionRefId());
	}

	@Test
	public void updateTrainingDefinitionWithInvalidDefinition() throws Exception{
		Exception ex = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(invalidDefinitionUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andReturn().getResolvedException();
		assertTrue(ex.getMessage().contains("Validation failed for argument"));
		assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
	}

	@Test
	public void updateTrainingDefinitionWithNonexistentDefinition() throws Exception{
		Exception ex = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(updateForNonexistingDefinition))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
	}

	@Test
	public void updateTrainingDefinitionWithReleasedDefinition() throws Exception{
		TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
		trainingDefinitionUpdateDTO.setId(tD.getId());
		Exception ex = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ConflictException.class);
		assertTrue(ex.getMessage().contains("Cannot edit released or archived training definition"));
	}

	@Test
	public void cloneTrainingDefinition() throws Exception{
		TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
		GameLevel gL1 = gameLevelRepository.save(gameLevel1);
		trainingDefinitionRepository.save(tD);

		mvc.perform(post("/training-definitions" + "/{id}", tD.getId()).param("title", "title"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE));

		Optional<TrainingDefinition> opt = trainingDefinitionRepository.findById(2L);
		assertTrue(opt.isPresent());
		TrainingDefinition clonedTD = opt.get();
		assertEquals(clonedTD.getTitle(), "title");
		assertEquals(clonedTD.getState().toString(), TDState.UNRELEASED.toString());
		assertEquals(clonedTD.isShowStepperBar(), tD.isShowStepperBar());
	}

	@Test
	public void cloneNonexistentTrainingDefinition() throws Exception {
		Exception ex = mvc.perform(post("/training-definitions" + "/{id}", 100L).param("title", "title"))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
	}

	@Test
	public void deleteTrainingDefinition() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
		GameLevel gL = gameLevelRepository.save(gameLevel1);
		InfoLevel iL = infoLevelRepository.save(infoLevel1);
		aL.setTrainingDefinition(tD);
		gL.setTrainingDefinition(tD);
		iL.setTrainingDefinition(tD);

		mvc.perform(delete("/training-definitions" + "/{id}", tD.getId()))
				.andExpect(status().isOk());
		Optional<TrainingDefinition> optTD = trainingDefinitionRepository.findById(tD.getId());
		Optional<AssessmentLevel> optAL = assessmentLevelRepository.findById(aL.getId());
		Optional<GameLevel> optGL = gameLevelRepository.findById(gL.getId());
		Optional<InfoLevel> optIL = infoLevelRepository.findById(iL.getId());
		assertFalse(optTD.isPresent());
		assertFalse(optAL.isPresent());
		assertFalse(optGL.isPresent());
		assertFalse(optIL.isPresent());
	}

	@Test
	public void deleteNonexistentDefinition() throws Exception {
		Exception ex = mvc.perform(delete("/training-definitions/{id}", 100L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
	}

	@Test
	public void deleteOneLevel() throws Exception{
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		GameLevel gL = gameLevelRepository.save(gameLevel1);
		InfoLevel iL = infoLevelRepository.save(infoLevel1);
		gL.setTrainingDefinition(tD);
		iL.setTrainingDefinition(tD);

		mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), gL.getId()))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());

		Optional<TrainingDefinition> optTD = trainingDefinitionRepository.findById(tD.getId());
		Optional<GameLevel> optGL = gameLevelRepository.findById(gL.getId());
		Optional<InfoLevel> optIl = infoLevelRepository.findById(iL.getId());
		assertTrue(optTD.isPresent());
		assertFalse(optGL.isPresent());
		assertTrue(optIl.isPresent());
	}

	@Test
	public void deleteOneLevelWithNonexistentLevel() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		Exception ex = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), 100L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Level not found"));
	}

	@Test
	public void deleteOneLevelWithNonexistentDefinition() throws Exception {
		GameLevel gL = gameLevelRepository.save(gameLevel1);
		Exception ex = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", 100L, gL.getId()))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
	}

	@Test
	public void deleteOneLevelWithReleasedDefinition() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
		GameLevel gL = gameLevelRepository.save(gameLevel1);
		Exception ex = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", tD.getId(), gL.getId()))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ConflictException.class);
		assertTrue(ex.getMessage().contains("Cannot edit released or archived training definition"));
	}

	@Test
	public void updateGameLevel() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		GameLevel gL = gameLevelRepository.save(gameLevel1);
		gL.setTrainingDefinition(tD);
		gameLevelUpdateDTO.setId(gL.getId());

		mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNoContent());

		Optional<GameLevel> optGL = gameLevelRepository.findById(gL.getId());
		assertTrue(optGL.isPresent());
		GameLevel updatedGL = optGL.get();
		assertEquals(updatedGL.getTitle(), gameLevelUpdateDTO.getTitle());
		assertEquals(updatedGL.getContent(), gameLevelUpdateDTO.getContent());
		assertEquals(updatedGL.getFlag(), gameLevelUpdateDTO.getFlag());
		assertEquals(updatedGL.getSolution(), gameLevelUpdateDTO.getSolution());
		assertEquals(updatedGL.isSolutionPenalized(), gameLevelUpdateDTO.isSolutionPenalized());
		assertEquals(updatedGL.getMaxScore(), gameLevelUpdateDTO.getMaxScore());
	}

	@Test
	public void updateGameLevelWithInvalidLevel() throws Exception {
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", 100L).content(convertObjectToJsonBytes(invalidGameLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andReturn().getResolvedException();
		assertTrue(ex.getMessage().contains("Validation failed for argument"));
		assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
	}

	@Test
	public void updateGameLevelWithReleasedDefinition() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
		GameLevel gL = gameLevelRepository.save(gameLevel1);
		gameLevelUpdateDTO.setId(gL.getId());

		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ConflictException.class);
		assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
	}

	@Test
	public void updateGameLevelWithNonexistentDefinition() throws Exception {
		gameLevelUpdateDTO.setId(1L);
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", 100L).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
	}

	@Test
	public void updateGameLevelWithNonexistentLevel() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		gameLevelUpdateDTO.setId(100L);
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/game-levels", tD.getId()).content(convertObjectToJsonBytes(gameLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Level was not found"));
	}

	@Test
	public void updateInfoLevel() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		InfoLevel iL = infoLevelRepository.save(infoLevel1);
		iL.setTrainingDefinition(tD);
		infoLevelUpdateDTO.setId(iL.getId());

		mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNoContent());

		Optional<InfoLevel> optIL = infoLevelRepository.findById(iL.getId());
		assertTrue(optIL.isPresent());
		InfoLevel updatedIL = optIL.get();
		assertEquals(updatedIL.getTitle(), infoLevelUpdateDTO.getTitle());
		assertEquals(updatedIL.getContent(), infoLevelUpdateDTO.getContent());
	}

	@Test
	public void updateInfoLevelWithInvalidLevel() throws Exception {
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(invalidInfoLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andReturn().getResolvedException();
		assertTrue(ex.getMessage().contains("Validation failed for argument"));
		assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
	}

	@Test
	public void updateInfoLevelWithReleasedDefinition() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
		InfoLevel iL = infoLevelRepository.save(infoLevel1);
		infoLevelUpdateDTO.setId(iL.getId());

		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ConflictException.class);
		assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
	}

	@Test
	public void updateInfoLevelWithNonexistentDefinition() throws Exception {
		infoLevelUpdateDTO.setId(1L);
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
	}

	@Test
	public void updateInfoLevelWithNonexistentLevel() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		infoLevelUpdateDTO.setId(100L);
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", tD.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Level was not found"));
	}

	@Test
	public void updateAssessmentLevel() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
		aL.setTrainingDefinition(tD);
		assessmentLevelUpdateDTO.setId(aL.getId());

		mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNoContent());

		Optional<AssessmentLevel> optAL = assessmentLevelRepository.findById(aL.getId());
		assertTrue(optAL.isPresent());
		AssessmentLevel updatedAL = optAL.get();
		assertEquals(updatedAL.getTitle(), assessmentLevelUpdateDTO.getTitle());
		assertEquals(updatedAL.getAssessmentType().toString(), assessmentLevelUpdateDTO.getType().toString());
		assertEquals(updatedAL.getQuestions(), assessmentLevelUpdateDTO.getQuestions());
		assertEquals(updatedAL.getInstructions(), assessmentLevelUpdateDTO.getInstructions());
		assertEquals(updatedAL.getMaxScore(), assessmentLevelUpdateDTO.getMaxScore());
	}

	@Test
	public void updateAssessmentLevelWithInvalidLevel() throws Exception {
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(invalidAssessmentLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andReturn().getResolvedException();
		assertTrue(ex.getMessage().contains("Validation failed for argument"));
		assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
	}

	@Test
	public void updateAssessmentLevelWithReleasedDefinition() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(releasedTrainingDefinition);
		AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
		assessmentLevelUpdateDTO.setId(aL.getId());

		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ConflictException.class);
		assertTrue(ex.getMessage().contains("Cannot edit released or archived training"));
	}

	@Test
	public void updateAssessmentLevelWithNonexistentDefinition() throws Exception {
		assessmentLevelUpdateDTO.setId(1L);
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
	}

	@Test
	public void updateAssessmentLevelWithNonexistentLevel() throws Exception {
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		assessmentLevelUpdateDTO.setId(100L);
		Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", tD.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Level was not found"));
	}

	@Test
	public void findGameLevelById() throws Exception{
		TrainingDefinition td = trainingDefinitionRepository.save(unreleasedDefinition);
		GameLevel gL = gameLevelRepository.save(gameLevel1);
		gL.setTrainingDefinition(td);
		MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", gL.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		GameLevelDTO gLDTO = beanMapping.mapTo(gL, GameLevelDTO.class);
		gLDTO.setLevelType(LevelType.GAME_LEVEL);
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(gLDTO)), result.getContentAsString());
	}

	@Test
	public void findInfoLevelById() throws Exception{
		TrainingDefinition td = trainingDefinitionRepository.save(unreleasedDefinition);
		InfoLevel iL = infoLevelRepository.save(infoLevel1);
		iL.setTrainingDefinition(td);
		MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", iL.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		InfoLevelDTO iLDTO = beanMapping.mapTo(iL, InfoLevelDTO.class);
		iLDTO.setLevelType(LevelType.INFO_LEVEL);
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(iLDTO)), result.getContentAsString());
	}

	@Test
	public void findAssessmentLevelById() throws Exception{
		TrainingDefinition td = trainingDefinitionRepository.save(unreleasedDefinition);
		AssessmentLevel aL = assessmentLevelRepository.save(assessmentLevel1);
		aL.setTrainingDefinition(td);
		MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", aL.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		AssessmentLevelDTO aLDTO = beanMapping.mapTo(aL, AssessmentLevelDTO.class);
		aLDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
		assertEquals(convertObjectToJsonBytes(convertObjectToJsonBytes(aLDTO)), result.getContentAsString());
	}

	@Test
	public void findGameLevelByIdWithDefinitionNotFound() throws Exception {
		Exception ex = mvc.perform(get("/training-definitions/levels/{id}", 100L))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();

		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Level with id: 100, not found"));
	}

	@Test
	public void createGameLevel() throws Exception{
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}",tD.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.GAME))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isCreated());

		Optional<GameLevel> optGL = gameLevelRepository.findById(1L);
		assertTrue(optGL.isPresent());
		GameLevel gL = optGL.get();
		assertEquals(gL.getMaxScore(), 100);
		assertEquals(gL.getTitle(), "Title of game level");
		assertEquals(gL.getIncorrectFlagLimit(), 100);
		assertEquals(gL.getFlag(), "Secret flag");
		assertTrue(gL.isSolutionPenalized());
		assertEquals(gL.getSolution(), "Solution of the game should be here");
		assertEquals(gL.getContent(), "The test entry should be here");
	}

	@Test
	public void createInfoLevel() throws Exception{
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}",tD.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.INFO))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isCreated());

		Optional<InfoLevel> optIL = infoLevelRepository.findById(1L);
		assertTrue(optIL.isPresent());
		InfoLevel iL = optIL.get();
		assertEquals(iL.getMaxScore(), 0);
		assertEquals(iL.getTitle(), "Title of info level");
		assertEquals(iL.getContent(), "Content of info level should be here.");
	}

	@Test
	public void createAssessmentLevel() throws Exception{
		TrainingDefinition tD = trainingDefinitionRepository.save(unreleasedDefinition);
		mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}",tD.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isCreated());

		Optional<AssessmentLevel> optAL = assessmentLevelRepository.findById(1L);
		assertTrue(optAL.isPresent());
		AssessmentLevel aL = optAL.get();
		assertEquals(aL.getMaxScore(), 0);
		assertEquals(aL.getTitle(), "Title of assessment level");
		assertEquals(aL.getAssessmentType().toString(), AssessmentType.QUESTIONNAIRE.toString());
		assertEquals(aL.getInstructions(), "Instructions should be here");
		assertEquals(aL.getQuestions(), "[{\"answer_required\":false,\"order\":0,\"penalty\":0,\"points\":0,\"text\":\"Example Question\",\"question_type\":\"FFQ\",\"correct_choices\":[]}]");
	}

	@Test
	public void createLevelOnReleasedDefinition()throws Exception{
		TrainingDefinition tD  = trainingDefinitionRepository.save(releasedTrainingDefinition);
		Exception ex = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}",tD.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
				.andExpect(status().isConflict())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ConflictException.class);
		assertTrue(ex.getMessage().contains("Cannot create level in released or archived training definition"));
	}

	@Test
	public void createLevelOnNonexistingDefinition()throws Exception{
		Exception ex = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", 100L, cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
				.andExpect(status().isNotFound())
				.andReturn().getResolvedException();
		assertEquals(ex.getClass(), ResourceNotFoundException.class);
		assertTrue(ex.getMessage().contains("Training definition with id: 100 not found"));
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

