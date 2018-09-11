package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.config.FacadeConfigTest;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.model.InfoLevel;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.model.enums.TDState;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
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
import java.util.Optional;

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

    private TrainingDefinition trainingDefinition1, trainingDefinition2, unreleasedDefinition, releasedDefinition;

    private AssessmentLevel level1;

    private GameLevel gameLevel;

    private InfoLevel infoLevel;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        level1 = new AssessmentLevel();
        level1.setId(1L);

        gameLevel = new GameLevel();
        gameLevel.setId(2L);
        gameLevel.setNextLevel(null);

        infoLevel = new InfoLevel();
        infoLevel.setId(3L);
        infoLevel.setNextLevel(gameLevel.getId());

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setState(TDState.RELEASED);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setStartingLevel(infoLevel.getId());

        unreleasedDefinition = new TrainingDefinition();
        unreleasedDefinition.setId(4L);
        unreleasedDefinition.setState(TDState.UNRELEASED);
        unreleasedDefinition.setStartingLevel(level1.getId());

        releasedDefinition = new TrainingDefinition();
        releasedDefinition.setState(TDState.RELEASED);
        releasedDefinition.setId(5L);

    }

    @Test
    public void findTrainingDefinitionById() {
        given(trainingDefinitionService.findById(trainingDefinition1.getId())).willReturn(Optional.of(trainingDefinition1));

        TrainingDefinitionDTO trainingDefinitionDTO = trainingDefinitionFacade.findById(trainingDefinition1.getId());
        deepEquals(trainingDefinition1, trainingDefinitionDTO);

        then(trainingDefinitionService).should().findById(trainingDefinition1.getId());
    }

    @Test
    public void findNonexistentTrainingDefinitionById() {
        Long id = 6L;
        given(trainingDefinitionService.findById(id)).willReturn(Optional.empty());
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.findById(id);
    }

    @Test
    public void findAllTrainingDefinitions() {
        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(trainingDefinition1);
        expected.add(trainingDefinition2);

        Page<TrainingDefinition> p = new PageImpl<TrainingDefinition>(expected);
        PathBuilder<TrainingDefinition> tD = new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingDefinition");
        Predicate predicate = tD.isNotNull();

        given(trainingDefinitionService.findAll(any(Predicate.class), any (Pageable.class))).willReturn(p);

        PageResultResource<TrainingDefinitionDTO> trainingDefinitionDTO = trainingDefinitionFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEquals(trainingDefinition1, trainingDefinitionDTO.getContent().get(0));
        deepEquals(trainingDefinition2, trainingDefinitionDTO.getContent().get(1));

        then(trainingDefinitionService).should().findAll(predicate, PageRequest.of(0,2));
    }

    @Test
    public void updateTrainingDefinition() {
        trainingDefinitionFacade.update(unreleasedDefinition);
        then(trainingDefinitionService).should().update(unreleasedDefinition);
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

        given(trainingDefinitionService.clone(trainingDefinition1.getId())).willReturn(Optional.of(clonedDefinition));

        TrainingDefinitionDTO newClone = trainingDefinitionFacade.clone(trainingDefinition1.getId());
        assertEquals("Clone of " + trainingDefinition1.getTitle(), newClone.getTitle());
        assertNotEquals(trainingDefinition1.getState(), newClone.getState());
        assertNotEquals(trainingDefinition1.getId(), newClone.getId());

        then(trainingDefinitionService).should().clone(trainingDefinition1.getId());
    }

    @Test
    public void cloneTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.clone(null);
    }

    @Test
    public void swapLeft() {
        trainingDefinitionFacade.swapLeft(unreleasedDefinition.getId(),level1.getId());
        then(trainingDefinitionService).should().swapLeft(unreleasedDefinition.getId(), level1.getId());
    }

    @Test
    public void swapLeftWithNullDefinition() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.swapLeft(null, level1.getId());
    }

    @Test
    public void swapLeftWithNullLevel() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.swapLeft(releasedDefinition.getId(), null);
    }


    @Test
    public void swapRight() {
        trainingDefinitionFacade.swapRight(unreleasedDefinition.getId(),level1.getId());
        then(trainingDefinitionService).should().swapRight(unreleasedDefinition.getId(), level1.getId());
    }

    @Test
    public void swapRightWithNullDefinition() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.swapRight(null, level1.getId());
    }

    @Test
    public void swapRightWithNullLevel() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.swapRight(releasedDefinition.getId(), null);
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
    public void deleteOneLevel(){
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
        trainingDefinitionFacade.updateAssessmentLevel(unreleasedDefinition.getId(), level1);
        then(trainingDefinitionService).should().updateAssessmentLevel(unreleasedDefinition.getId(), level1);
    }

    @Test
    public void updateAssessmentLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateAssessmentLevel(null, level1);
    }

    @Test
    public void updateAssessmentLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateAssessmentLevel(unreleasedDefinition.getId(), null);
    }

    @Test
    public void updateGameLevel() {
        trainingDefinitionFacade.updateGameLevel(trainingDefinition2.getId(), gameLevel);
        then(trainingDefinitionService).should().updateGameLevel(trainingDefinition2.getId(), gameLevel);
    }

    @Test
    public void updateGameLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateGameLevel(null, gameLevel);
    }

    @Test
    public void updateGameLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateGameLevel(trainingDefinition2.getId(), null);
    }

    @Test
    public void updateInfoLevel() {
        trainingDefinitionFacade.updateInfoLevel(trainingDefinition2.getId(), infoLevel);
        then(trainingDefinitionService).should().updateInfoLevel(trainingDefinition2.getId(), infoLevel);
    }

    @Test
    public void updateInfoLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateInfoLevel(null, infoLevel);
    }

    @Test
    public void updateInfoLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.updateInfoLevel(trainingDefinition2.getId(), null);
    }

    @Test
    public void createInfoLevel() {
        InfoLevel newInfoLevel = new InfoLevel();
        newInfoLevel.setId(5L);
        newInfoLevel.setTitle("test");
        given(trainingDefinitionService.createInfoLevel(unreleasedDefinition.getId(), newInfoLevel)).willReturn(Optional.of(newInfoLevel));

        InfoLevelDTO createdLevel = trainingDefinitionFacade.createInfoLevel(unreleasedDefinition.getId(), newInfoLevel);

        assertEquals(newInfoLevel.getId(), createdLevel.getId());
        assertEquals(newInfoLevel.getTitle(), createdLevel.getTitle());
        then(trainingDefinitionService).should().createInfoLevel(unreleasedDefinition.getId(), newInfoLevel);
    }

    @Test
    public void createInfoLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createInfoLevel(null, infoLevel);
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
        given(trainingDefinitionService.createGameLevel(unreleasedDefinition.getId(), newGameLevel)).willReturn(Optional.of(newGameLevel));

        GameLevelDTO createdLevel = trainingDefinitionFacade.createGameLevel(unreleasedDefinition.getId(), newGameLevel);

        assertEquals(newGameLevel.getId(), createdLevel.getId());
        assertEquals(newGameLevel.getTitle(), createdLevel.getTitle());
        then(trainingDefinitionService).should().createGameLevel(unreleasedDefinition.getId(), newGameLevel);
    }

    @Test
    public void createGameLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createGameLevel(null, gameLevel);
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
        given(trainingDefinitionService.createAssessmentLevel(unreleasedDefinition.getId(), newAssessmentLevel)).willReturn(Optional.of(newAssessmentLevel));

        AssessmentLevelDTO createdLevel = trainingDefinitionFacade.createAssessmentLevel(unreleasedDefinition.getId(), newAssessmentLevel);

        assertEquals(newAssessmentLevel.getId(), createdLevel.getId());
        assertEquals(newAssessmentLevel.getTitle(), createdLevel.getTitle());
        then(trainingDefinitionService).should().createAssessmentLevel(unreleasedDefinition.getId(), newAssessmentLevel);
    }

    @Test
    public void createAssessmentLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createAssessmentLevel(null, level1);
    }

    @Test
    public void createAssessmentLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createAssessmentLevel(trainingDefinition1.getId(), null);
    }

    @Test
    public void createTrainingDefinition() {
        given(trainingDefinitionService.create(trainingDefinition1)).willReturn(Optional.of(trainingDefinition1));
        TrainingDefinitionDTO trainingDefinitionDTO= trainingDefinitionFacade.create(trainingDefinition1);
        deepEquals(trainingDefinition1, trainingDefinitionDTO);
        then(trainingDefinitionService).should().create(trainingDefinition1);
    }

    @Test
    public void createTrainingDefinitionWithNull() {
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.create(null);
    }

    private void deepEquals(TrainingDefinition expected, TrainingDefinitionDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getState(), actual.getState());
    }

}
