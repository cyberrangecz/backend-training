package cz.muni.ics.kypo.training.service;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.impl.SecurityService;
import cz.muni.ics.kypo.training.service.impl.TrainingDefinitionServiceImpl;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileReader;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;

/**
 * @author Boris Jadus(445343)
 */

@RunWith(SpringRunner.class)
public class TrainingDefinitionServiceTest {

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
    private RestTemplate restTemplate;
    @Mock
    private SecurityService securityService;

    private TrainingDefinition trainingDefinition1, trainingDefinition2, unreleasedDefinition, releasedDefinition, definitionWithoutLevels;
    private AssessmentLevel level1, level2, level3, newAssessmentLevel;
    private GameLevel gameLevel, newGameLevel;
    private InfoLevel infoLevel, newInfoLevel;

    @Mock
    private BetaTestingGroup viewGroup;


    private JSONParser parser = new JSONParser();
    private String questions;
    private Pageable pageable;
    private Predicate predicate;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingDefinitionService = new TrainingDefinitionServiceImpl(trainingDefinitionRepository, abstractLevelRepository,
                infoLevelRepository, gameLevelRepository, assessmentLevelRepository, trainingInstanceRepository, userRefRepository,
                restTemplate, securityService);

        parser = new JSONParser();
        try {
            questions = parser.parse(new FileReader(ResourceUtils.getFile("classpath:questions.json"))).toString();
        } catch (IOException | ParseException ex) {
        }

        ReflectionTestUtils.setField(trainingDefinitionService, "userAndGroupUrl", "https://localhost:8083/kypo2/api/v1/");

        level3 = new AssessmentLevel();
        level3.setId(3L);
        level3.setOrder(3);

        level2 = new AssessmentLevel();
        level2.setId(2L);
        level2.setOrder(2);
        level2.setQuestions(questions);

        level1 = new AssessmentLevel();
        level1.setId(1L);
        level1.setOrder(1);

        gameLevel = new GameLevel();
        gameLevel.setId(4L);
        gameLevel.setOrder(4);

        infoLevel = new InfoLevel();
        infoLevel.setId(5L);
        infoLevel.setOrder(5);

        newGameLevel = new GameLevel();
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

        newInfoLevel = new InfoLevel();
        newInfoLevel.setId(11L);
        newInfoLevel.setTitle("Title of info Level");
        newInfoLevel.setMaxScore(20);
        newInfoLevel.setContent("Content of info level should be here.");
        newInfoLevel.setOrder(11);

        newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setId(12L);
        newAssessmentLevel.setTitle("Title of assessment level");
        newAssessmentLevel.setAssessmentType(AssessmentType.QUESTIONNAIRE);
        newAssessmentLevel.setMaxScore(0);
        newAssessmentLevel.setInstructions("Instructions should be here");
        newAssessmentLevel.setQuestions("[]");
        newAssessmentLevel.setOrder(12);

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setDescription("test1");
        trainingDefinition1.setTitle("test1");
        trainingDefinition1.setState(TDState.RELEASED);
        trainingDefinition1.setSandboxDefinitionRefId(1L);
        trainingDefinition1.setBetaTestingGroup(viewGroup);
        trainingDefinition1.setLastEdited(LocalDateTime.now().minusHours(24));

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setDescription("test2");
        trainingDefinition2.setTitle("test2");
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setSandboxDefinitionRefId(1L);

        unreleasedDefinition = new TrainingDefinition();
        unreleasedDefinition.setId(4L);
        unreleasedDefinition.setState(TDState.UNRELEASED);
        unreleasedDefinition.setBetaTestingGroup(viewGroup);

        releasedDefinition = new TrainingDefinition();
        releasedDefinition.setState(TDState.RELEASED);
        releasedDefinition.setId(5L);

        definitionWithoutLevels = new TrainingDefinition();
        definitionWithoutLevels.setId(8L);
        definitionWithoutLevels.setState(TDState.UNRELEASED);
        definitionWithoutLevels.setBetaTestingGroup(viewGroup);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void getTrainingDefinitionById() {
        given(trainingDefinitionRepository.findById(trainingDefinition1.getId())).willReturn(Optional.of(trainingDefinition1));

        TrainingDefinition tD = trainingDefinitionService.findById(trainingDefinition1.getId());
        deepEquals(tD, trainingDefinition1);

        then(trainingDefinitionRepository).should().findById(trainingDefinition1.getId());
    }

    @Test
    public void getNonexistentTrainingDefinitionById() {
        Long id = 6L;
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Training definition with id: " + id + " not found.");
        trainingDefinitionService.findById(id);
    }

    @Test
    public void findAll() {
        given(securityService.isAdmin()).willReturn(true);
        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(trainingDefinition1);
        expected.add(trainingDefinition2);

        Page p = new PageImpl<TrainingDefinition>(expected);
        PathBuilder<TrainingDefinition> tD = new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingRun");
        Predicate predicate = tD.isNotNull();

        given(trainingDefinitionRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingDefinitionService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findAllBySandboxDefinitionId() {
        List<TrainingDefinition> expected = new ArrayList<>();
        expected.add(trainingDefinition1);
        expected.add(trainingDefinition2);

        Page p = new PageImpl<>(expected);

        given(trainingDefinitionRepository.findAllBySandBoxDefinitionRefId(any(Long.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingDefinitionRepository.findAllBySandBoxDefinitionRefId(1L, PageRequest.of(0, 2));
        assertNotNull(pr);
        assertEquals(expected.size(), pr.getTotalElements());
    }

    @Test
    public void cloneTrainingDefinition() {
        mockSpringSecurityContextForGet();
        TrainingDefinition tDcloned = new TrainingDefinition();
        tDcloned.setTitle("Clone of tD");
        tDcloned.setId(null);
        tDcloned.setState(TDState.UNRELEASED);
        tDcloned.setDescription(trainingDefinition1.getDescription());
        tDcloned.setBetaTestingGroup(null);
        tDcloned.setLastEdited(LocalDateTime.now());

        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(new UserRef());
        given(trainingDefinitionRepository.findById(trainingDefinition1.getId())).willReturn(Optional.of(trainingDefinition1));
        trainingDefinitionService.clone(trainingDefinition1.getId(), "Clone of tD");
        then(trainingDefinitionRepository).should().findById(trainingDefinition1.getId());
        then(trainingDefinitionRepository).should().save(tDcloned);
    }

    @Test
    public void cloneTrainingDefinitionWithNull() {
        thrown.expect(ServiceLayerException.class);
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

    @Test
    public void updateTrainingDefinitionWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition");
        trainingDefinitionService.update(releasedDefinition);
    }

    @Test
    public void updateTrainingDefinitionWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot update training definition with already created training instance. " +
                "Remove training instance/s before updating training definition.");
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
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot delete released training definition");

        trainingDefinitionService.delete(releasedDefinition.getId());
    }

    @Test
    public void deleteWithNull() {
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.delete(null);
    }

    @Test
    public void deleteWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot delete training definition with already created training instance. " +
                "Remove training instance/s before deleting training definition.");
        trainingDefinitionService.delete(unreleasedDefinition.getId());
    }

    @Test
    public void deleteOneLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));

        trainingDefinitionService.deleteOneLevel(unreleasedDefinition.getId(), level1.getId());
        assertEquals(unreleasedDefinition.getLastEdited().getSecond(), LocalDateTime.now(Clock.systemUTC()).getSecond());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should().findById(any(Long.class));
        then(assessmentLevelRepository).should().delete(level1);
    }

    @Test
    public void deleteOneLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition");

        trainingDefinitionService.deleteOneLevel(releasedDefinition.getId(), any(Long.class));
    }


    @Test
    public void deleteOneLevelWithNullDefinition() {
        thrown.expect(ServiceLayerException.class);
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
        given(abstractLevelRepository.findLevelInDefinition(anyLong(), anyLong())).willReturn(Optional.of(level2));
        given(assessmentLevelRepository.findById(any(Long.class))).willReturn(Optional.of(level2));

        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), level2);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(assessmentLevelRepository).should().findById(any(Long.class));
        then(assessmentLevelRepository).should().save(level2);
    }

    @Test
    public void updateAssessmentLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition");

        trainingDefinitionService.updateAssessmentLevel(releasedDefinition.getId(), any(AssessmentLevel.class));
    }

    @Test
    public void updateAssessmentLevelWithLevelNotInDefinition() {
        AssessmentLevel level = new AssessmentLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));
        given(abstractLevelRepository.findById(level3.getId())).willReturn(Optional.of(level3));
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Level was not found in definition");

        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), level);
    }

    @Test
    public void updateAssessmentLevelWithNullDefinition() {
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.updateAssessmentLevel(null, level2);
    }

    @Test
    public void updateAssessmentLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), null);
    }

    @Test
    public void updateAssessmentLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot update training definition with already created training instance. " +
                "Remove training instance/s before updating training definition.");
        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), level2);
    }

    @Test
    public void updateGameLevel() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(trainingDefinition2));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(false);
        given(gameLevelRepository.findById(anyLong())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findLevelInDefinition(anyLong(), anyLong())).willReturn(Optional.of(gameLevel));
        trainingDefinitionService.updateGameLevel(trainingDefinition2.getId(), gameLevel);

        then(trainingDefinitionRepository).should().findById(anyLong());
        then(gameLevelRepository).should().findById(anyLong());
        then(gameLevelRepository).should().save(gameLevel);
    }

    @Test
    public void updateGameLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition");

        trainingDefinitionService.updateGameLevel(releasedDefinition.getId(), any(GameLevel.class));
    }

    @Test
    public void updateGameLevelWithLevelNotInDefinition() {
        GameLevel level = new GameLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Level was not found in definition");

        trainingDefinitionService.updateGameLevel(trainingDefinition2.getId(), level);
    }

    @Test
    public void updateGameLevelWithNullDefinition() {
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.updateGameLevel(null, gameLevel);
    }

    @Test
    public void updateGameLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateGameLevel(trainingDefinition2.getId(), null);
    }

    @Test
    public void updateGameLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot update training definition with already created training instance. " +
                "Remove training instance/s before updating training definition.");
        trainingDefinitionService.updateGameLevel(unreleasedDefinition.getId(), gameLevel);
    }

    @Test
    public void updateInfoLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(infoLevelRepository.findById(any(Long.class))).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findLevelInDefinition(anyLong(), anyLong())).willReturn(Optional.of(infoLevel));

        trainingDefinitionService.updateInfoLevel(trainingDefinition2.getId(), infoLevel);

        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
        then(infoLevelRepository).should().save(infoLevel);
    }

    @Test
    public void updateInfoLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition");

        trainingDefinitionService.updateInfoLevel(releasedDefinition.getId(), any(InfoLevel.class));
    }

    @Test
    public void updateInfoLevelWithLevelNotInDefinition() {
        InfoLevel level = new InfoLevel();
        level.setId(8L);
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Level was not found in definition");

        trainingDefinitionService.updateInfoLevel(trainingDefinition2.getId(), level);
    }

    @Test
    public void updateInfoLevelWithNullDefinition() {
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.updateInfoLevel(null, infoLevel);
    }

    @Test
    public void updateInfoLevelWithNullLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        thrown.expect(NullPointerException.class);
        trainingDefinitionService.updateInfoLevel(trainingDefinition2.getId(), null);
    }

    @Test
    public void updateInfoLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot update training definition with already created training instance. " +
                "Remove training instance/s before updating training definition.");
        trainingDefinitionService.updateInfoLevel(unreleasedDefinition.getId(), infoLevel);
    }

    @Test
    public void createGameLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(gameLevelRepository.save(any(GameLevel.class))).willReturn(newGameLevel);
        GameLevel createdLevel = trainingDefinitionService.createGameLevel(trainingDefinition2.getId());

        assertEquals(newGameLevel, createdLevel);

        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createGameLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot create level in released or archived training definition");

        trainingDefinitionService.createGameLevel(releasedDefinition.getId());
    }

    @Test
    public void createGameLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createGameLevel(null);

    }

    @Test
    public void createGameLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot update training definition with already created training instance. " +
                "Remove training instance/s before updating training definition.");
        trainingDefinitionService.createGameLevel(unreleasedDefinition.getId());
    }

    @Test
    public void createInfoLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(infoLevelRepository.save(any(InfoLevel.class))).willReturn(newInfoLevel);

        InfoLevel createdLevel = trainingDefinitionService.createInfoLevel(trainingDefinition2.getId());

        assertNotNull(createdLevel);
        assertEquals(newInfoLevel, createdLevel);

        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createInfoLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition.");

        trainingDefinitionService.createInfoLevel(releasedDefinition.getId());
    }

    @Test
    public void createInfoLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createInfoLevel(null);

    }

    @Test
    public void createInfoLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot update training definition with already created training instance. " +
                "Remove training instance/s before updating training definition.");
        trainingDefinitionService.createInfoLevel(unreleasedDefinition.getId());
    }

    @Test
    public void createAssessmentLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(assessmentLevelRepository.save(any(AssessmentLevel.class))).willReturn(newAssessmentLevel);

        AssessmentLevel createdLevel = trainingDefinitionService.createAssessmentLevel(trainingDefinition2.getId());

        assertNotNull(createdLevel);
        assertEquals(newAssessmentLevel, createdLevel);

        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createAssessmentLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot create level in released or archived training definition");

        trainingDefinitionService.createAssessmentLevel(releasedDefinition.getId());
    }

    @Test
    public void createAssessmentLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createAssessmentLevel(null);

    }

    @Test
    public void createAssessmentLevelWithCreatedInstances() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot update training definition with already created training instance. " +
                "Remove training instance/s before updating training definition.");
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
        given(trainingDefinitionRepository.save(trainingDefinition1)).willReturn(trainingDefinition1);
        given(userRefRepository.findUserByUserRefId(anyLong())).willReturn(Optional.of(user));
        TrainingDefinition tD = trainingDefinitionService.create(trainingDefinition1);
        deepEquals(trainingDefinition1, tD);
        then(trainingDefinitionRepository).should(times(1)).save(trainingDefinition1);
    }

    @Test
    public void createTrainingInstanceWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training definition must not be null");
        trainingDefinitionService.create(null);
    }

    @Test
    public void findLevelById() {
        given(abstractLevelRepository.findByIdIncludinDefinition(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        AbstractLevel abstractLevel = trainingDefinitionService.findLevelById(infoLevel.getId());
        assertTrue(abstractLevel instanceof InfoLevel);
        assertEquals(infoLevel.getId(), abstractLevel.getId());
        then(abstractLevelRepository).should().findByIdIncludinDefinition(infoLevel.getId());
    }

    @Test
    public void findLevelById_notExisting() {
        thrown.expect(ServiceLayerException.class);
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

    @Test
    public void switchState_RELEASEDtoUNRELEASED_withCreatedInstances() {
        given(trainingDefinitionRepository.findById(anyLong())).willReturn(Optional.of(releasedDefinition));
        given(trainingInstanceRepository.existsAnyForTrainingDefinition(anyLong())).willReturn(true);
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot update training definition with already created training instance(s). " +
                "Remove training instance(s) before changing the state from released to unreleased training definition.");
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
        assertEquals(expected.getSandboxDefinitionRefId(), actual.getSandboxDefinitionRefId());
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
