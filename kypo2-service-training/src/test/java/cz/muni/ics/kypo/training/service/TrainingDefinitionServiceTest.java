package cz.muni.ics.kypo.training.service;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class TrainingDefinitionServiceTest {

    @Autowired
    public TestDataFactory testDataFactory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingDefinitionService trainingDefinitionService;

    @Mock
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Mock
    private AbstractLevelRepository abstractLevelRepository;
    @Mock
    private GameLevelRepository gameLevelRepository;
    @Mock
    private InfoLevelRepository infoLevelRepository;
    @Mock
    private AssessmentLevelRepository assessmentLevelRepository;
    @Mock
    private TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    private UserRefRepository userRefRepository;
    @Mock
    private SecurityService securityService;

    private TrainingDefinition unreleasedDefinition, releasedDefinition;
    private AssessmentLevel assessmentLevel;
    private GameLevel gameLevel;
    private InfoLevel infoLevel;

    private Pageable pageable;
    private Predicate predicate;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingDefinitionService = new TrainingDefinitionService(trainingDefinitionRepository, abstractLevelRepository,
                infoLevelRepository, gameLevelRepository, assessmentLevelRepository, trainingInstanceRepository, userRefRepository,
                securityService);

        ReflectionTestUtils.setField(trainingDefinitionService, "userAndGroupUrl", "https://localhost:8083/kypo2/api/v1/");

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(1L);
        infoLevel.setOrder(0);

        gameLevel = testDataFactory.getPenalizedLevel();
        gameLevel.setId(2L);
        gameLevel.setOrder(1);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(3L);
        assessmentLevel.setOrder(2);

        unreleasedDefinition = testDataFactory.getUnreleasedDefinition();
        unreleasedDefinition.setId(1L);

        releasedDefinition = testDataFactory.getReleasedDefinition();
        releasedDefinition.setId(2L);


        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getTrainingDefinitionById() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));

        TrainingDefinition tD = trainingDefinitionService.findById(unreleasedDefinition.getId());
        deepEquals(tD, unreleasedDefinition);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNonexistentTrainingDefinitionById() {
        Long id = 6L;
        trainingDefinitionService.findById(id);
    }

    @Test
    public void findAll() {
        given(securityService.isAdmin()).willReturn(true);
        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(unreleasedDefinition);
        expected.add(releasedDefinition);

        Page p = new PageImpl<TrainingDefinition>(expected);
        PathBuilder<TrainingDefinition> tD = new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingRun");
        Predicate predicate = tD.isNotNull();

        given(trainingDefinitionRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingDefinitionService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    public void cloneTrainingDefinition() {
        mockSpringSecurityContextForGet();
        TrainingDefinition tDcloned = new TrainingDefinition();
        tDcloned.setTitle("Clone of tD");
        tDcloned.setId(null);
        tDcloned.setState(TDState.UNRELEASED);
        tDcloned.setDescription(releasedDefinition.getDescription());
        tDcloned.setBetaTestingGroup(null);
        tDcloned.setLastEdited(LocalDateTime.now());
        tDcloned.setOutcomes(releasedDefinition.getOutcomes());
        tDcloned.setPrerequisities(releasedDefinition.getPrerequisities());

        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(new UserRef());
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.clone(releasedDefinition.getId(), "Clone of tD");
        then(trainingDefinitionRepository).should().findById(releasedDefinition.getId());
        then(trainingDefinitionRepository).should().save(tDcloned);
    }

    @Test(expected = EntityNotFoundException.class)
    public void cloneTrainingDefinitionWithNull() {
        trainingDefinitionService.clone(null, "Clone of tD");
    }

    @Test
    public void updateTrainingDefinition() {
        mockSpringSecurityContextForGet();
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(new UserRef());

        TrainingDefinition updatedDefinition = new TrainingDefinition();
        BeanUtils.copyProperties(unreleasedDefinition, updatedDefinition);
        updatedDefinition.setLastEdited(LocalDateTime.now());
        trainingDefinitionService.update(unreleasedDefinition);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(trainingDefinitionRepository).should().save(updatedDefinition);
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingDefinitionWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.update(releasedDefinition);
    }

    @Test(expected = EntityConflictException.class)
    public void updateTrainingDefinitionWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.update(unreleasedDefinition);
    }

    @Test
    public void updateTrainingDefinitionWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training definition must not be null");
        trainingDefinitionService.update(null);
    }

    public void delete() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(assessmentLevel.getId())).willReturn(Optional.of(assessmentLevel));

        trainingDefinitionService.delete(unreleasedDefinition.getId());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should(times(3)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().delete(unreleasedDefinition);
        then(assessmentLevelRepository).should(times(3)).delete(any(AssessmentLevel.class));
    }

    @Test(expected = EntityConflictException.class)
    public void deleteWithCannotBeDeletedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.delete(releasedDefinition.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteWithNull() {
        trainingDefinitionService.delete(null);
    }

    @Test(expected = EntityConflictException.class)
    public void deleteWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.delete(unreleasedDefinition.getId());
    }

    @Test
    public void deleteOneLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));

        trainingDefinitionService.deleteOneLevel(unreleasedDefinition.getId(), infoLevel.getId());
        assertEquals(unreleasedDefinition.getLastEdited().getSecond(), LocalDateTime.now(Clock.systemUTC()).getSecond());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should().findById(any(Long.class));
        then(infoLevelRepository).should().delete(infoLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void deleteOneLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.deleteOneLevel(releasedDefinition.getId(), any(Long.class));
    }


    @Test(expected = EntityNotFoundException.class)
    public void deleteOneLevelWithNullDefinition() {
        trainingDefinitionService.deleteOneLevel(null, infoLevel.getId());
    }

    @Test(expected = NullPointerException.class)
    public void deleteOneLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.deleteOneLevel(unreleasedDefinition.getId(), null);
    }

    @Test
    public void updateAssessmentLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findLevelInDefinition(anyLong(), anyLong())).willReturn(Optional.of(assessmentLevel));
        given(assessmentLevelRepository.findById(any(Long.class))).willReturn(Optional.of(assessmentLevel));

        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), assessmentLevel);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(assessmentLevelRepository).should().findById(any(Long.class));
        then(assessmentLevelRepository).should().save(assessmentLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void updateAssessmentLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.updateAssessmentLevel(releasedDefinition.getId(), any(AssessmentLevel.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateAssessmentLevelWithLevelNotInDefinition() {
        AssessmentLevel level = new AssessmentLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(assessmentLevel.getId())).willReturn(Optional.of(assessmentLevel));
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), level);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateAssessmentLevelWithNullDefinition() {
        trainingDefinitionService.updateAssessmentLevel(null, assessmentLevel);
    }

    @Test
    public void updateAssessmentLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), null);
    }

    @Test(expected = EntityConflictException.class)
    public void updateAssessmentLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), assessmentLevel);
    }

    @Test
    public void updateGameLevel() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(false);
        given(gameLevelRepository.findById(anyLong())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findLevelInDefinition(anyLong(), anyLong())).willReturn(Optional.of(gameLevel));
        trainingDefinitionService.updateGameLevel(unreleasedDefinition.getId(), gameLevel);

        then(trainingDefinitionRepository).should().findById(anyLong());
        then(gameLevelRepository).should().findById(anyLong());
        then(gameLevelRepository).should().save(gameLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void updateGameLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.updateGameLevel(releasedDefinition.getId(), any(GameLevel.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateGameLevelWithLevelNotInDefinition() {
        GameLevel level = new GameLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.updateGameLevel(unreleasedDefinition.getId(), level);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateGameLevelWithNullDefinition() {
        trainingDefinitionService.updateGameLevel(null, gameLevel);
    }

    @Test
    public void updateGameLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateGameLevel(unreleasedDefinition.getId(), null);
    }

    @Test(expected = EntityConflictException.class)
    public void updateGameLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.updateGameLevel(unreleasedDefinition.getId(), gameLevel);
    }

    @Test
    public void updateInfoLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(infoLevelRepository.findById(any(Long.class))).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findLevelInDefinition(anyLong(), anyLong())).willReturn(Optional.of(infoLevel));

        trainingDefinitionService.updateInfoLevel(unreleasedDefinition.getId(), infoLevel);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(infoLevelRepository).should().save(infoLevel);
    }

    @Test(expected = EntityConflictException.class)
    public void updateInfoLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.updateInfoLevel(releasedDefinition.getId(), any(InfoLevel.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateInfoLevelWithLevelNotInDefinition() {
        InfoLevel level = new InfoLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.updateInfoLevel(unreleasedDefinition.getId(), level);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateInfoLevelWithNullDefinition() {
        trainingDefinitionService.updateInfoLevel(null, infoLevel);
    }

    @Test
    public void updateInfoLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateInfoLevel(unreleasedDefinition.getId(), null);
    }

    @Test(expected = EntityConflictException.class)
    public void updateInfoLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.updateInfoLevel(unreleasedDefinition.getId(), infoLevel);
    }

    @Test
    public void createGameLevel() {
        GameLevel newGameLevel = new GameLevel();
        newGameLevel.setId(10L);
        newGameLevel.setMaxScore(100);
        newGameLevel.setTitle("Title of game level");
        newGameLevel.setIncorrectFlagLimit(100);
        newGameLevel.setFlag("Secret flag");
        newGameLevel.setSolution("Solution of the game should be here");
        newGameLevel.setSolutionPenalized(true);
        newGameLevel.setEstimatedDuration(1);
        newGameLevel.setOrder(10);
        newGameLevel.setContent("The test entry should be here");

        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(gameLevelRepository.save(any(GameLevel.class))).willReturn(newGameLevel);
        GameLevel createdLevel = trainingDefinitionService.createGameLevel(unreleasedDefinition.getId());

        assertEquals(newGameLevel, createdLevel);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createGameLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.createGameLevel(releasedDefinition.getId());
    }

    @Test
    public void createGameLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createGameLevel(null);

    }

    @Test(expected = EntityConflictException.class)
    public void createGameLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.createGameLevel(unreleasedDefinition.getId());
    }

    @Test
    public void createInfoLevel() {
        InfoLevel newInfoLevel = new InfoLevel();
        newInfoLevel.setId(11L);
        newInfoLevel.setTitle("Title of info Level");
        newInfoLevel.setMaxScore(20);
        newInfoLevel.setContent("Content of info level should be here.");
        newInfoLevel.setOrder(11);

        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(infoLevelRepository.save(any(InfoLevel.class))).willReturn(newInfoLevel);
        InfoLevel createdLevel = trainingDefinitionService.createInfoLevel(unreleasedDefinition.getId());

        assertNotNull(createdLevel);
        assertEquals(newInfoLevel, createdLevel);
        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createInfoLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.createInfoLevel(releasedDefinition.getId());
    }

    @Test
    public void createInfoLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createInfoLevel(null);

    }

    @Test(expected = EntityConflictException.class)
    public void createInfoLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.createInfoLevel(unreleasedDefinition.getId());
    }

    @Test
    public void createAssessmentLevel() {
        AssessmentLevel newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setId(12L);
        newAssessmentLevel.setTitle("Title of assessment level");
        newAssessmentLevel.setAssessmentType(AssessmentType.QUESTIONNAIRE);
        newAssessmentLevel.setMaxScore(0);
        newAssessmentLevel.setInstructions("Instructions should be here");
        newAssessmentLevel.setQuestions("[]");
        newAssessmentLevel.setOrder(12);
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(assessmentLevelRepository.save(any(AssessmentLevel.class))).willReturn(newAssessmentLevel);
        AssessmentLevel createdLevel = trainingDefinitionService.createAssessmentLevel(unreleasedDefinition.getId());

        assertNotNull(createdLevel);
        assertEquals(newAssessmentLevel, createdLevel);
        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void createAssessmentLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.createAssessmentLevel(releasedDefinition.getId());
    }

    @Test
    public void createAssessmentLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createAssessmentLevel(null);

    }

    @Test(expected = EntityConflictException.class)
    public void createAssessmentLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        trainingDefinitionService.createAssessmentLevel(unreleasedDefinition.getId());
    }

    @Test
    public void findAllLevelsFromDefinitionWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.findAllLevelsFromDefinition(null);
    }

    @Test
    public void createTrainingDefinition() {
        mockSpringSecurityContextForGet();
        UserRef user = new UserRef();
        given(trainingDefinitionRepository.save(unreleasedDefinition)).willReturn(unreleasedDefinition);
        given(userRefRepository.findUserByUserRefId(anyLong())).willReturn(Optional.of(user));
        TrainingDefinition tD = trainingDefinitionService.create(unreleasedDefinition);
        deepEquals(unreleasedDefinition, tD);
        then(trainingDefinitionRepository).should(times(1)).save(unreleasedDefinition);
    }

    @Test
    public void createTrainingInstanceWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training definition must not be null");
        trainingDefinitionService.create(null);
    }

    @Test
    public void findLevelById() {
        given(abstractLevelRepository.findByIdIncludingDefinition(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        AbstractLevel abstractLevel = trainingDefinitionService.findLevelById(infoLevel.getId());
        assertTrue(abstractLevel instanceof InfoLevel);
        assertEquals(infoLevel.getId(), abstractLevel.getId());
        then(abstractLevelRepository).should().findByIdIncludingDefinition(infoLevel.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void findLevelByIdNotExisting() {
        trainingDefinitionService.findLevelById(555L);
    }

    @Test
    public void switchState_UNRELEASEDtoRELEASED() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(unreleasedDefinition));
        trainingDefinitionService.switchState(unreleasedDefinition.getId(), cz.muni.ics.kypo.training.api.enums.TDState.RELEASED);
        assertEquals(TDState.RELEASED, unreleasedDefinition.getState());
    }

    @Test
    public void switchState_RELEASEDtoARCHIVED() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(releasedDefinition));
        trainingDefinitionService.switchState(releasedDefinition.getId(), cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED);
        assertEquals(TDState.ARCHIVED, releasedDefinition.getState());
    }

    @Test
    public void switchState_RELEASEDtoUNRELEASED() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(releasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(false);
        trainingDefinitionService.switchState(releasedDefinition.getId(), cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        assertEquals(TDState.UNRELEASED, releasedDefinition.getState());
    }

    @Test(expected = EntityConflictException.class)
    public void switchState_RELEASEDtoUNRELEASED_withCreatedInstances() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(releasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(true);
        trainingDefinitionService.switchState(releasedDefinition.getId(), cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
    }

    @After
    public void after() {
        reset(trainingDefinitionRepository);
    }

    private void deepEquals(TrainingDefinition expected, TrainingDefinition actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertArrayEquals(expected.getOutcomes(), actual.getOutcomes());
        assertArrayEquals(expected.getPrerequisities(), actual.getPrerequisities());
        assertEquals(expected.getState(), actual.getState());
        assertEquals(expected.getBetaTestingGroup(), actual.getBetaTestingGroup());
        assertEquals(expected.isShowStepperBar(), actual.isShowStepperBar());
    }


    private void mockSpringSecurityContextForGet() {
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), "participant");
        sub.addProperty(AuthenticatedUserOIDCItems.NAME.getName(), "Pavel");
        sub.addProperty(AuthenticatedUserOIDCItems.ISS.getName(), "https://oidc.muni.cz");
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2Authentication auth = Mockito.mock(OAuth2Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(auth);
        given(auth.getUserAuthentication()).willReturn(auth);
        given(auth.getCredentials()).willReturn(sub);
        given(auth.getAuthorities()).willReturn(List.of(new SimpleGrantedAuthority(RoleType.ROLE_TRAINING_ADMINISTRATOR.name())));
        given(authentication.getDetails()).willReturn(auth);
    }
}
