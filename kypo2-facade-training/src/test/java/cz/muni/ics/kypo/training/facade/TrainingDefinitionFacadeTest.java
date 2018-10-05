package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.config.FacadeConfigTest;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.model.InfoLevel;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.model.enums.TDState;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(FacadeConfigTest.class)
public class TrainingDefinitionFacadeTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private TrainingDefinitionFacade trainingDefinitionFacade;

	@MockBean
	private TrainingDefinitionService trainingDefinitionService;

	private BeanMapping beanMapping;

	private TrainingDefinition trainingDefinition1, trainingDefinition2, releasedDefinition;
	private TrainingDefinitionUpdateDTO trainingDefinitionUpdate;
	private TrainingDefinitionCreateDTO trainingDefinitionCreate;

	private AssessmentLevel level1;
	private AssessmentLevelUpdateDTO alUpdate;

	private GameLevel gameLevel;
	private GameLevelUpdateDTO gameLevelUpdate;

	private InfoLevel infoLevel;
	private InfoLevelUpdateDTO infoLevelUpdate;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		beanMapping = new BeanMappingImpl(new ModelMapper());
		level1 = new AssessmentLevel();
		level1.setId(1L);

		alUpdate = new AssessmentLevelUpdateDTO();
		alUpdate.setId(2L);

		gameLevel = new GameLevel();
		gameLevel.setId(2L);
		gameLevel.setNextLevel(null);

		gameLevelUpdate = new GameLevelUpdateDTO();
		gameLevelUpdate.setId(2L);
		gameLevelUpdate.setTitle("title");
		gameLevelUpdate.setAttachments(new String[3]);
		gameLevelUpdate.setContent("Content");
		gameLevelUpdate.setEstimatedDuration(1000);
		gameLevelUpdate.setFlag("flag1");
		gameLevelUpdate.setIncorrectFlagLimit(4);
		gameLevelUpdate.setSolutionPenalized(true);
/*
		gameLevelCreate = new GameLevelCreateDTO();
		gameLevelCreate.setTitle("title");
		gameLevelCreate.setAttachments(new String[3]);
		gameLevelCreate.setContent("Content");
		gameLevelCreate.setEstimatedDuration(1000);
		gameLevelCreate.setFlag("flag1");
		gameLevelCreate.setIncorrectFlagLimit(4);
		gameLevelCreate.setNextLevel(2L);
*/
		infoLevel = new InfoLevel();
		infoLevel.setId(3L);
		infoLevel.setNextLevel(gameLevel.getId());

		infoLevelUpdate = new InfoLevelUpdateDTO();
		infoLevelUpdate.setId(3L);
		infoLevelUpdate.setTitle("some title");
		infoLevelUpdate.setContent("some content");
/*
		infoLevelCreate = new InfoLevelCreateDTO();
		infoLevelCreate.setMaxScore(40);
		infoLevelCreate.setTitle("some title");
		infoLevelCreate.setContent("some content");
		infoLevelCreate.setNextLevel(gameLevel.getId());

		alCreate = new AssessmentLevelCreateDTO();
		alCreate.setInstructions("instructions");
		alCreate.setMaxScore(50);
		alCreate.setNextLevel(1L);
		alCreate.setQuestions("test");
		alCreate.setTitle("Some title");
		alCreate.setType(AssessmentType.QUESTIONNAIRE);
*/
		alUpdate = new AssessmentLevelUpdateDTO();
		alUpdate.setInstructions("instructions");
		alUpdate.setMaxScore(50);
		alUpdate.setQuestions("test");
		alUpdate.setTitle("Some title");
		alUpdate.setType(AssessmentType.QUESTIONNAIRE);

		trainingDefinition1 = new TrainingDefinition();
		trainingDefinition1.setId(1L);
		trainingDefinition1.setState(TDState.RELEASED);

		trainingDefinition2 = new TrainingDefinition();
		trainingDefinition2.setId(2L);
		trainingDefinition2.setState(TDState.UNRELEASED);
		trainingDefinition2.setStartingLevel(infoLevel.getId());

		releasedDefinition = new TrainingDefinition();
		releasedDefinition.setState(TDState.RELEASED);
		releasedDefinition.setId(5L);

		trainingDefinition2 = new TrainingDefinition();
		trainingDefinition2.setId(2L);
		trainingDefinition2.setState(TDState.UNRELEASED);
		trainingDefinition2.setStartingLevel(infoLevel.getId());

		trainingDefinitionUpdate = new TrainingDefinitionUpdateDTO();
		trainingDefinitionUpdate.setId(4L);
		trainingDefinitionUpdate.setState(TDState.UNRELEASED);
		trainingDefinitionUpdate.setStartingLevel(level1.getId());

		trainingDefinitionCreate = new TrainingDefinitionCreateDTO();
		trainingDefinitionCreate.setDescription("TD desc");
		trainingDefinitionCreate.setOutcomes(new String[0]);
		trainingDefinitionCreate.setPrerequisities(new String[0]);
		trainingDefinitionCreate.setState(TDState.ARCHIVED);
		trainingDefinitionCreate.setTitle("TD some title");
	}

	@Test
	public void findTrainingDefinitionById() {
		given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(trainingDefinition1);

		TrainingDefinitionDTO trainingDefinitionDTO = trainingDefinitionFacade.findById(trainingDefinition1.getId());
		deepEquals(trainingDefinition1, trainingDefinitionDTO);

		then(trainingDefinitionService).should().findById(trainingDefinition1.getId());
	}

	@Test
	public void findNonexistentTrainingDefinitionById() {
		Long id = 6L;
		willThrow(ServiceLayerException.class).given(trainingDefinitionService).findById(id);
		thrown.expect(FacadeLayerException.class);
		trainingDefinitionFacade.findById(id);
	}




	@Test
	public void cloneTrainingDefinitionWithNull() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.clone(null);
	}

	@Test
	public void createTrainingDefinition() {
		given(trainingDefinitionService.create(beanMapping.mapTo(trainingDefinitionCreate, TrainingDefinition.class)))
				.willReturn(beanMapping.mapTo(trainingDefinitionCreate, TrainingDefinition.class));
		trainingDefinitionFacade.create(trainingDefinitionCreate);
		then(trainingDefinitionService).should().create(beanMapping.mapTo(trainingDefinitionCreate, TrainingDefinition.class));
	}

	private void deepEquals(TrainingDefinition expected, TrainingDefinitionDTO actual) {
		assertEquals(expected.getId(), actual.getId());
		assertEquals(expected.getState(), actual.getState());
	}

	@Test
	public void findAllTrainingDefinitions() {
		List<TrainingDefinition> expected = new ArrayList<>();
		expected.add(trainingDefinition1);
		expected.add(trainingDefinition2);

		Page<TrainingDefinition> p = new PageImpl<TrainingDefinition>(expected);
		PathBuilder<TrainingDefinition> tD = new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingDefinition");
		Predicate predicate = tD.isNotNull();

		given(trainingDefinitionService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

		PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTO = trainingDefinitionFacade.findAll(predicate, PageRequest.of(0, 2));
		deepEquals(trainingDefinition1, trainingDefinitionDTO.getContent().get(0));
		deepEquals(trainingDefinition2, trainingDefinitionDTO.getContent().get(1));

		then(trainingDefinitionService).should().findAll(predicate, PageRequest.of(0, 2));
	}

	@Test
	public void updateTrainingDefinition() {
		trainingDefinitionFacade.update(trainingDefinitionUpdate);
		then(trainingDefinitionService).should().update(beanMapping.mapTo(trainingDefinitionUpdate, TrainingDefinition.class));
	}

	@Test
	public void updateTrainingDefinitionWithNull() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.update(null);
	}

	@Test
	public void cloneTrainingDefinition() {
		TrainingDefinition clonedDefinition = new TrainingDefinition();
		clonedDefinition.setId(3L);
		clonedDefinition.setState(TDState.UNRELEASED);
		clonedDefinition.setTitle("Clone of " + trainingDefinition1.getTitle());

		given(trainingDefinitionService.clone(trainingDefinition1.getId())).willReturn(clonedDefinition);

		TrainingDefinitionDTO newClone = trainingDefinitionFacade.clone(trainingDefinition1.getId());
		assertEquals("Clone of " + trainingDefinition1.getTitle(), newClone.getTitle());
		assertNotEquals(trainingDefinition1.getState(), newClone.getState());
		assertNotEquals(trainingDefinition1.getId(), newClone.getId());

		then(trainingDefinitionService).should().clone(trainingDefinition1.getId());
	}



	@Test
	public void swapLeft() {
		trainingDefinitionFacade.swapLeft(trainingDefinition2.getId(), level1.getId());
		then(trainingDefinitionService).should().swapLeft(trainingDefinition2.getId(), level1.getId());
	}


	@Test
	public void swapRight() {
		trainingDefinitionFacade.swapRight(trainingDefinition2.getId(), level1.getId());
		then(trainingDefinitionService).should().swapRight(trainingDefinition2.getId(), level1.getId());
	}


	@Test
	public void deleteTrainingDefinition() {
		trainingDefinitionFacade.delete(trainingDefinition1.getId());
		then(trainingDefinitionService).should().delete(trainingDefinition1.getId());
	}

	@Test
	public void deleteTrainingDefinitionWithNull() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.delete(null);
	}

	@Test
	public void deleteOneLevel() {
		trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), level1.getId());
		then(trainingDefinitionService).should().deleteOneLevel(trainingDefinition1.getId(), level1.getId());
	}

	@Test
	public void deleteOneLevelWithNullDefinition() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.deleteOneLevel(null, level1.getId());
	}

	@Test
	public void deleteOneLevelWithNullLevel() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), null);
	}

	@Test
	public void updateAssessmentLevel() {
		trainingDefinitionFacade.updateAssessmentLevel(trainingDefinition1.getId(), alUpdate);
		then(trainingDefinitionService).should().updateAssessmentLevel(trainingDefinition1.getId(),
				beanMapping.mapTo(alUpdate, AssessmentLevel.class));
	}

	@Test
	public void updateAssessmentLevelWithNullDefinition() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.updateAssessmentLevel(null, alUpdate);
	}

	@Test
	public void updateAssessmentLevelWithNullLevel() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.updateAssessmentLevel(trainingDefinition1.getId(), null);
	}

	@Test
	public void updateGameLevel() {
		trainingDefinitionFacade.updateGameLevel(trainingDefinition2.getId(), gameLevelUpdate);
		then(trainingDefinitionService).should().updateGameLevel(trainingDefinition2.getId(), beanMapping.mapTo(gameLevelUpdate, GameLevel.class));
	}

	@Test
	public void updateGameLevelWithNullDefinition() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.updateGameLevel(null, gameLevelUpdate);
	}

	@Test
	public void updateGameLevelWithNullLevel() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.updateGameLevel(trainingDefinition2.getId(), null);
	}

	@Test
	public void updateInfoLevel() {
		trainingDefinitionFacade.updateInfoLevel(trainingDefinition2.getId(), infoLevelUpdate);
		then(trainingDefinitionService).should().updateInfoLevel(trainingDefinition2.getId(),
				beanMapping.mapTo(infoLevelUpdate, InfoLevel.class));
	}

	@Test
	public void updateInfoLevelWithNullDefinition() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.updateInfoLevel(null, infoLevelUpdate);
	}

	@Test
	public void updateInfoLevelWithNullLevel() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.updateInfoLevel(trainingDefinition2.getId(), null);
	}
/*
	@Test
	public void createInfoLevel() {
		InfoLevel newInfoLevel = new InfoLevel();
		newInfoLevel.setId(5L);
		newInfoLevel.setTitle("test");
		given(trainingDefinitionService.createInfoLevel(trainingDefinition1.getId(), beanMapping.mapTo(infoLevelCreate, InfoLevel.class))).willReturn(newInfoLevel);

		InfoLevelCreateDTO createdLevel = trainingDefinitionFacade.createInfoLevel(trainingDefinition1.getId(), infoLevelCreate);

		assertEquals(newInfoLevel.getTitle(), createdLevel.getTitle());
		then(trainingDefinitionService).should().createInfoLevel(trainingDefinition1.getId(), beanMapping.mapTo(infoLevelCreate, InfoLevel.class));
	}

	@Test
	public void createInfoLevelWithNullDefinitionId() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.createInfoLevel(null, infoLevelCreate);
	}

	@Test
	public void createInfoLevelWithNullLevel() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.createInfoLevel(trainingDefinition1.getId(), null);
	}

	@Test
	public void createGameLevel() {
		GameLevel newGameLevel = new GameLevel();
		newGameLevel.setId(5L);
		newGameLevel.setTitle("test");
		given(trainingDefinitionService.createGameLevel(trainingDefinition1.getId(), beanMapping.mapTo(gameLevelCreate, GameLevel.class)))
				.willReturn(newGameLevel);

		GameLevelCreateDTO createdLevel = trainingDefinitionFacade.createGameLevel(trainingDefinition1.getId(), gameLevelCreate);

		assertEquals(newGameLevel.getTitle(), createdLevel.getTitle());
		then(trainingDefinitionService).should().createGameLevel(trainingDefinition1.getId(),
				beanMapping.mapTo(gameLevelCreate, GameLevel.class));
	}

	@Test
	public void createGameLevelWithNullDefinitionId() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.createGameLevel(null, gameLevelCreate);
	}

	@Test
	public void createGameLevelWithNullLevel() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.createGameLevel(trainingDefinition1.getId(), null);
	}

	@Test
	public void createAssessmentLevel() {
		AssessmentLevel newAssessmentLevel = new AssessmentLevel();
		newAssessmentLevel.setId(5L);
		newAssessmentLevel.setTitle("test");
		given(trainingDefinitionService.createAssessmentLevel(trainingDefinition1.getId(), beanMapping.mapTo(alCreate, AssessmentLevel.class)))
				.willReturn(newAssessmentLevel);

		AssessmentLevelCreateDTO createdLevel = trainingDefinitionFacade.createAssessmentLevel(trainingDefinition1.getId(), alCreate);

		assertEquals(newAssessmentLevel.getTitle(), createdLevel.getTitle());
		then(trainingDefinitionService).should().createAssessmentLevel(trainingDefinition1.getId(), beanMapping.mapTo(alCreate, AssessmentLevel.class));
	}

	@Test
	public void createAssessmentLevelWithNullDefinitionId() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.createAssessmentLevel(null, alCreate);
	}

	@Test
	public void createAssessmentLevelWithNullLevel() {
		thrown.expect(NullPointerException.class);
		trainingDefinitionFacade.createAssessmentLevel(trainingDefinition1.getId(), null);
	}
*/

}
