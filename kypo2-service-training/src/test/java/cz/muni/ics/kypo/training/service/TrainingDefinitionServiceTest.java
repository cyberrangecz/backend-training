package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.exceptions.CannotBeClonedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeDeletedException;
import cz.muni.ics.kypo.training.exceptions.CannotBeUpdatedException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.model.InfoLevel;
import cz.muni.ics.kypo.training.model.TrainingDefinition;
import cz.muni.ics.kypo.training.model.enums.TDState;
import cz.muni.ics.kypo.training.repository.*;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.HibernateException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.BDDMockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.training.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service"})
public class TrainingDefinitionServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TrainingDefinitionService trainingDefinitionService;

    @MockBean
    private TrainingDefinitionRepository trainingDefinitionRepository;

    @MockBean
    private AbstractLevelRepository abstractLevelRepository;

    @MockBean
    private GameLevelRepository gameLevelRepository;

    @MockBean
    private InfoLevelRepository infoLevelRepository;

    @MockBean
    private AssessmentLevelRepository assessmentLevelRepository;

    private TrainingDefinition trainingDefinition1, trainingDefinition2, unreleasedDefinition, releasedDefinition, definitionWithoutLevels;

    private AssessmentLevel level1, level2, level3, newAssessmentLevel;

    private GameLevel gameLevel, newGameLevel;

    private InfoLevel infoLevel, newInfoLevel;

    @SpringBootApplication
    static class TestConfiguration{
    }

    @Before
    public void init(){
        level3 = new AssessmentLevel();
        level3.setId(3L);
        level3.setNextLevel(null);

        level2 = new AssessmentLevel();
        level2.setId(2L);
        level2.setNextLevel(level3.getId());

        level1 = new AssessmentLevel();
        level1.setId(1L);
        level1.setNextLevel(level2.getId());

        gameLevel = new GameLevel();
        gameLevel.setId(4L);
        gameLevel.setNextLevel(null);

        infoLevel = new InfoLevel();
        infoLevel.setId(5L);
        infoLevel.setNextLevel(gameLevel.getId());

        newGameLevel = new GameLevel();
        newGameLevel.setId(10L);
        newGameLevel.setTitle("title");
        newGameLevel.setMaxScore(20);

        newInfoLevel = new InfoLevel();
        newInfoLevel.setId(11L);
        newInfoLevel.setTitle("title");
        newInfoLevel.setMaxScore(20);

        newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setId(12L);
        newAssessmentLevel.setTitle("title");
        newAssessmentLevel.setMaxScore(20);


        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setDescription("test1");
        trainingDefinition1.setTitle("test1");
        trainingDefinition1.setState(TDState.RELEASED);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setDescription("test2");
        trainingDefinition2.setTitle("test2");
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setStartingLevel(infoLevel.getId());

        unreleasedDefinition = new TrainingDefinition();
        unreleasedDefinition.setId(4L);
        unreleasedDefinition.setState(TDState.UNRELEASED);
        unreleasedDefinition.setStartingLevel(level1.getId());

        releasedDefinition = new TrainingDefinition();
        releasedDefinition.setState(TDState.RELEASED);
        releasedDefinition.setId(5L);

        definitionWithoutLevels = new TrainingDefinition();
        definitionWithoutLevels.setId(8L);
        definitionWithoutLevels.setState(TDState.UNRELEASED);
        definitionWithoutLevels.setStartingLevel(null);
    }

    @Test
    public void getTrainingDefinitionById() {
        given(trainingDefinitionRepository.findById(trainingDefinition1.getId())).willReturn(Optional.of(trainingDefinition1));

        TrainingDefinition tD = trainingDefinitionService.findById(trainingDefinition1.getId()).get();
        deepEquals(tD, trainingDefinition1);

        then(trainingDefinitionRepository).should().findById(trainingDefinition1.getId());
    }

    @Test
    public void getTrainingDefinitionByIdWithHibernateException() {
        Long id = 1L;
        willThrow(HibernateException.class).given(trainingDefinitionRepository).findById(id);
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.findById(id);
    }

    @Test
    public void getNonexistentTrainingDefinitionById() {
        Long id = 6L;
        assertEquals(Optional.empty(), trainingDefinitionService.findById(id));
    }

    @Test
    public void findAll() {
        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(trainingDefinition1);
        expected.add(trainingDefinition2);

        Page p = new PageImpl<TrainingDefinition>(expected);
        PathBuilder<TrainingDefinition> tD = new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingRun");
        Predicate predicate = tD.isNotNull();

        given(trainingDefinitionRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingDefinitionService.findAll(predicate, PageRequest.of(0,2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void cloneTrainingDefinition(){
        TrainingDefinition tDcloned = new TrainingDefinition();
        tDcloned.setTitle("Clone of "+ trainingDefinition1.getTitle());
        tDcloned.setId(3L);
        tDcloned.setState(TDState.UNRELEASED);
        tDcloned.setDescription(trainingDefinition1.getDescription());

        given(trainingDefinitionRepository.findById(trainingDefinition1.getId())).willReturn(Optional.of(trainingDefinition1));
        given(trainingDefinitionRepository.save(any(TrainingDefinition.class))).willReturn(tDcloned);

        Optional<TrainingDefinition> optionalNewClone = trainingDefinitionService.clone(trainingDefinition1.getId());
        assertTrue(optionalNewClone.isPresent());
        assertEquals("Clone of "+ trainingDefinition1.getTitle(), optionalNewClone.get().getTitle());
        assertNotEquals(trainingDefinition1.getId(), optionalNewClone.get().getId());
        assertNotEquals(trainingDefinition1.getState(), optionalNewClone.get().getState());
        assertEquals(trainingDefinition1.getDescription(), optionalNewClone.get().getDescription());

        then(trainingDefinitionRepository).should().findById(trainingDefinition1.getId());
        then(trainingDefinitionRepository).should().save(any(TrainingDefinition.class));
    }

    @Test
    public void cloneTrainingDefinitionWithCannotBeClonedException() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));

        thrown.expect(CannotBeClonedException.class);
        thrown.expectMessage("Cant copy unreleased training definition");
        trainingDefinitionService.clone(unreleasedDefinition.getId());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test
    public void cloneTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.clone(null);
    }

    @Test
    public void updateTrainingDefinition() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));

        trainingDefinitionService.update(unreleasedDefinition);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(trainingDefinitionRepository).should().save(unreleasedDefinition);
    }

    @Test
    public void updateTrainingDefinitionWithCannotBeUpdatedException(){
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));

        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant edit released or archived training definition");
        trainingDefinitionService.update(releasedDefinition);
    }

    @Test
    public void updateTrainingDefinitionWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training definition must not be null");
        trainingDefinitionService.update(null);
    }

    @Test
    public void swapLeft() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));

        trainingDefinitionService.swapLeft(unreleasedDefinition.getId(), level2.getId());
        assertEquals(unreleasedDefinition.getStartingLevel(), level2.getId());
        assertEquals(level2.getNextLevel(), level1.getId());
        assertEquals(level1.getNextLevel(), level3.getId());

        then(trainingDefinitionRepository).should(times(2)).findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should(times(3)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().save(unreleasedDefinition);
        then(assessmentLevelRepository).should(times(2)).save(any(AssessmentLevel.class));
    }

    @Test
    public void swapLeftWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant edit released or archived training definition");

        trainingDefinitionService.swapLeft(releasedDefinition.getId(), any(Long.class));
    }

    @Test
    public void swapLeftOnFirstLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cant swap left first level");

        trainingDefinitionService.swapLeft(unreleasedDefinition.getId(), level1.getId());
    }

    @Test
    public void swapLeftWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.swapLeft(null, level2.getId());
    }

    @Test
    public void swapLeftWithNullLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.swapLeft(unreleasedDefinition.getId(), null);
    }

    @Test
    public void swapRight() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));

        trainingDefinitionService.swapRight(unreleasedDefinition.getId(), level1.getId());
        assertEquals(unreleasedDefinition.getStartingLevel(), level2.getId());
        assertEquals(level2.getNextLevel(), level1.getId());
        assertEquals(level1.getNextLevel(), level3.getId());

        then(trainingDefinitionRepository).should(times(2)).findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().save(unreleasedDefinition);
        then(assessmentLevelRepository).should(times(2)).save(any(AssessmentLevel.class));
    }

    @Test
    public void swapRightWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant edit released or archived training definition");

        trainingDefinitionService.swapRight(releasedDefinition.getId(), any(Long.class));
    }

    @Test
    public void swapRightOnLastLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level3.getId())).willReturn(Optional.of(level3));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cant swap right last level");

        trainingDefinitionService.swapRight(unreleasedDefinition.getId(), level3.getId());
    }

    @Test
    public void swapRightWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.swapRight(null, level2.getId());
    }

    @Test
    public void swapRightWithNullLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.swapRight(unreleasedDefinition.getId(), null);
    }

    @Test
    public void delete() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));
        given(abstractLevelRepository.findById(level3.getId())).willReturn(Optional.of(level3));

        trainingDefinitionService.delete(unreleasedDefinition.getId());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should(times(3)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().delete(unreleasedDefinition);
        then(assessmentLevelRepository).should(times(3)).delete(any(AssessmentLevel.class));
    }

    @Test
    public void deleteWithCannotBeDeletedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeDeletedException.class);
        thrown.expectMessage("Cant delete released training definition");

        trainingDefinitionService.delete(releasedDefinition.getId());
    }

    @Test
    public void deleteWithNull() {
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.delete(null);
    }

    @Test
    public void deleteOneLevelOnFirstLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));

        trainingDefinitionService.deleteOneLevel(unreleasedDefinition.getId(), level1.getId());
        assertEquals(unreleasedDefinition.getStartingLevel(), level2.getId());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should().findById(any(Long.class));
        then(trainingDefinitionRepository).should().save(unreleasedDefinition);
        then(assessmentLevelRepository).should().delete(level1);
    }

    @Test
    public void deleteOneLevelOnMiddleLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));

        trainingDefinitionService.deleteOneLevel(unreleasedDefinition.getId(),level2.getId());
        assertEquals(level1.getNextLevel(), level3.getId());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should(times(3)).findById(any(Long.class));
        then(assessmentLevelRepository).should().delete(level2);
        then(assessmentLevelRepository).should().save(level1);
    }

    @Test
    public void deleteOneLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant edit released or archived training definition");

        trainingDefinitionService.deleteOneLevel(releasedDefinition.getId(),any(Long.class));
    }


    @Test
    public void deleteOneLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.deleteOneLevel(null, level2.getId());
    }

    @Test
    public void deleteOneLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.deleteOneLevel(unreleasedDefinition.getId(), null);
    }

    @Test
    public void updateAssessmentLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));

        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), level2);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(assessmentLevelRepository).should().save(level2);
    }

    @Test
    public void updateAssessmentLevelWithCannotBeUpdatedException(){
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant edit released or archived training definition");

        trainingDefinitionService.updateAssessmentLevel(releasedDefinition.getId(), any(AssessmentLevel.class));
    }

    @Test
    public void updateAssessmentLevelWithLevelNotInDefinition(){
        AssessmentLevel level = new AssessmentLevel();
        level.setId(8L);
        level.setNextLevel(null);
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));
        given(abstractLevelRepository.findById(level3.getId())).willReturn(Optional.of(level3));
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Level was not found in definition");

        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), level);
    }

    @Test
    public void updateAssessmentLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateAssessmentLevel(null, level2);
    }

    @Test
    public void updateAssessmentLevelWithNullLevel(){
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), null);
    }

    @Test
    public void updateGameLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));

        trainingDefinitionService.updateGameLevel(trainingDefinition2.getId(), gameLevel);

        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(gameLevelRepository).should().save(gameLevel);
    }

    @Test
    public void updateGameLevelWithCannotBeUpdatedException(){
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant edit released or archived training definition");

        trainingDefinitionService.updateGameLevel(releasedDefinition.getId(), any(GameLevel.class));
    }

    @Test
    public void updateGameLevelWithLevelNotInDefinition(){
        GameLevel level = new GameLevel();
        level.setId(8L);
        level.setNextLevel(null);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Level was not found in definition");

        trainingDefinitionService.updateGameLevel(trainingDefinition2.getId(), level);
    }

    @Test
    public void updateGameLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateGameLevel(null, gameLevel);
    }

    @Test
    public void updateGameLevelWithNullLevel(){
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateGameLevel(trainingDefinition2.getId(), null);
    }

    @Test
    public void updateInfoLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        trainingDefinitionService.updateInfoLevel(trainingDefinition2.getId(), infoLevel);
        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
        then(infoLevelRepository).should().save(infoLevel);
    }

    @Test
    public void updateInfoLevelWithCannotBeUpdatedException(){
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant edit released or archived training definition");

        trainingDefinitionService.updateInfoLevel(releasedDefinition.getId(), any(InfoLevel.class));
    }

    @Test
    public void updateInfoLevelWithLevelNotInDefinition(){
        InfoLevel level = new InfoLevel();
        level.setId(8L);
        level.setNextLevel(null);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Level was not found in definition");

        trainingDefinitionService.updateInfoLevel(trainingDefinition2.getId(), level);
    }

    @Test
    public void updateInfoLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateInfoLevel(null, infoLevel);
    }

    @Test
    public void updateInfoLevelWithNullLevel(){
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateInfoLevel(trainingDefinition2.getId(), null);
    }

    @Test
    public void createGameLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(gameLevelRepository.save(newGameLevel)).willReturn(newGameLevel);

        Optional<GameLevel> createdLevel = trainingDefinitionService.createGameLevel(trainingDefinition2.getId(), newGameLevel);

        assertTrue(createdLevel.isPresent());
        assertEquals(newGameLevel.getTitle(), createdLevel.get().getTitle());
        assertEquals(newGameLevel.getMaxScore(), createdLevel.get().getMaxScore());
        assertEquals(newGameLevel.getId(), createdLevel.get().getId());
        assertEquals(gameLevel.getNextLevel(), createdLevel.get().getId());

        then(gameLevelRepository).should().save(newGameLevel);
        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createGameLevelAsFirstLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        given(gameLevelRepository.save(newGameLevel)).willReturn(newGameLevel);

        Optional<GameLevel> createdLevel = trainingDefinitionService.createGameLevel(definitionWithoutLevels.getId(), newGameLevel);

        assertTrue(createdLevel.isPresent());
        assertEquals(newGameLevel.getTitle(), createdLevel.get().getTitle());
        assertEquals(newGameLevel.getMaxScore(), createdLevel.get().getMaxScore());
        assertEquals(newGameLevel.getId(), createdLevel.get().getId());
        assertEquals(definitionWithoutLevels.getStartingLevel(), createdLevel.get().getId());

        then(gameLevelRepository).should().save(newGameLevel);
        then(trainingDefinitionRepository).should(times(2)).findById(definitionWithoutLevels.getId());
        then(trainingDefinitionRepository).should().save(definitionWithoutLevels);
    }

    @Test
    public void createGameLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant create level in released or archived training definition");

        trainingDefinitionService.createGameLevel(releasedDefinition.getId(), any(GameLevel.class));
    }

    @Test
    public void createGameLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createGameLevel(null, newGameLevel);

    }

    @Test
    public void createGameLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Game level must not be null");
        trainingDefinitionService.createGameLevel(definitionWithoutLevels.getId(), null);
    }

    @Test
    public void createInfoLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(infoLevelRepository.save(newInfoLevel)).willReturn(newInfoLevel);

        Optional<InfoLevel> createdLevel = trainingDefinitionService.createInfoLevel(trainingDefinition2.getId(), newInfoLevel);

        assertTrue(createdLevel.isPresent());
        assertEquals(newInfoLevel.getTitle(), createdLevel.get().getTitle());
        assertEquals(newInfoLevel.getMaxScore(), createdLevel.get().getMaxScore());
        assertEquals(newInfoLevel.getId(), createdLevel.get().getId());
        assertEquals(gameLevel.getNextLevel(), createdLevel.get().getId());

        then(infoLevelRepository).should().save(newInfoLevel);
        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createInfoLevelAsFirstLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        given(infoLevelRepository.save(newInfoLevel)).willReturn(newInfoLevel);

        Optional<InfoLevel> createdLevel = trainingDefinitionService.createInfoLevel(definitionWithoutLevels.getId(), newInfoLevel);

        assertTrue(createdLevel.isPresent());
        assertEquals(newInfoLevel.getTitle(), createdLevel.get().getTitle());
        assertEquals(newInfoLevel.getMaxScore(), createdLevel.get().getMaxScore());
        assertEquals(newInfoLevel.getId(), createdLevel.get().getId());
        assertEquals(definitionWithoutLevels.getStartingLevel(), createdLevel.get().getId());

        then(infoLevelRepository).should().save(newInfoLevel);
        then(trainingDefinitionRepository).should(times(2)).findById(definitionWithoutLevels.getId());
        then(trainingDefinitionRepository).should().save(definitionWithoutLevels);
    }

    @Test
    public void createInfoLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant create level in released or archived training definition");

        trainingDefinitionService.createInfoLevel(releasedDefinition.getId(), any(InfoLevel.class));
    }

    @Test
    public void createInfoLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createInfoLevel(null, newInfoLevel);

    }

    @Test
    public void createInfoLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Info level must not be null");
        trainingDefinitionService.createInfoLevel(definitionWithoutLevels.getId(), null);
    }

    @Test
    public void createAssessmentLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(assessmentLevelRepository.save(newAssessmentLevel)).willReturn(newAssessmentLevel);

        Optional<AssessmentLevel> createdLevel = trainingDefinitionService.createAssessmentLevel(trainingDefinition2.getId(), newAssessmentLevel);

        assertTrue(createdLevel.isPresent());
        assertEquals(newAssessmentLevel.getTitle(), createdLevel.get().getTitle());
        assertEquals(newAssessmentLevel.getMaxScore(), createdLevel.get().getMaxScore());
        assertEquals(newAssessmentLevel.getId(), createdLevel.get().getId());
        assertEquals(gameLevel.getNextLevel(), createdLevel.get().getId());

        then(assessmentLevelRepository).should().save(newAssessmentLevel);
        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createAssessmentLevelAsFirstLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        given(assessmentLevelRepository.save(newAssessmentLevel)).willReturn(newAssessmentLevel);

        Optional<AssessmentLevel> createdLevel = trainingDefinitionService.createAssessmentLevel(definitionWithoutLevels.getId(), newAssessmentLevel);

        assertTrue(createdLevel.isPresent());
        assertEquals(newAssessmentLevel.getTitle(), createdLevel.get().getTitle());
        assertEquals(newAssessmentLevel.getMaxScore(), createdLevel.get().getMaxScore());
        assertEquals(newAssessmentLevel.getId(), createdLevel.get().getId());
        assertEquals(definitionWithoutLevels.getStartingLevel(), createdLevel.get().getId());

        then(assessmentLevelRepository).should().save(newAssessmentLevel);
        then(trainingDefinitionRepository).should(times(2)).findById(definitionWithoutLevels.getId());
        then(trainingDefinitionRepository).should().save(definitionWithoutLevels);
    }

    @Test
    public void createAssessmentLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(CannotBeUpdatedException.class);
        thrown.expectMessage("Cant create level in released or archived training definition");

        trainingDefinitionService.createAssessmentLevel(releasedDefinition.getId(), any(AssessmentLevel.class));
    }

    @Test
    public void createAssessmentLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createAssessmentLevel(null, newAssessmentLevel);

    }

    @Test
    public void createAssessmentLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Assessment level must not be null");
        trainingDefinitionService.createAssessmentLevel(definitionWithoutLevels.getId(), null);
    }

    @After
    public void after(){
        reset(trainingDefinitionRepository);
    }

    private void deepEquals(TrainingDefinition expected, TrainingDefinition actual){
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getState(), actual.getState());
    }

}
