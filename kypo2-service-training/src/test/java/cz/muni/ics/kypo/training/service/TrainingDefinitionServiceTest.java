package cz.muni.ics.kypo.training.service;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.commons.facade.api.PageResultResource;
import cz.muni.ics.kypo.commons.persistence.model.IDMGroupRef;
import cz.muni.ics.kypo.commons.persistence.repository.IDMGroupRefRepository;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.impl.TrainingDefinitionServiceImpl;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;

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
    private TDViewGroupRepository viewGroupRepository;
    @Mock
    private IDMGroupRefRepository idmGroupRefRepository;
    @Mock
    private UserRefRepository userRefRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpServletRequest servletRequest;

    private TrainingDefinition trainingDefinition1, trainingDefinition2, unreleasedDefinition, releasedDefinition, definitionWithoutLevels;
    private AssessmentLevel level1, level2, level3, newAssessmentLevel;
    private GameLevel gameLevel, newGameLevel;
    private InfoLevel infoLevel, newInfoLevel;
    @Mock
    private IDMGroupRef groupRef1, groupRef2;
    @Mock
    private UserInfoDTO userInfoDTO1, userInfoDTO2;
    @Mock
    private TDViewGroup viewGroup;


    private JSONParser parser = new JSONParser();
    private String questions;
    private Pageable pageable;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingDefinitionService = new TrainingDefinitionServiceImpl(trainingDefinitionRepository, abstractLevelRepository,
                infoLevelRepository, gameLevelRepository, assessmentLevelRepository, trainingInstanceRepository, userRefRepository,
                viewGroupRepository, idmGroupRefRepository, restTemplate, servletRequest);

        parser = new JSONParser();
        try {
            questions = parser.parse(new FileReader(ResourceUtils.getFile("classpath:questions.json"))).toString();
        } catch (IOException | ParseException ex) {
        }

        level3 = new AssessmentLevel();
        level3.setId(3L);
        level3.setNextLevel(null);

        level2 = new AssessmentLevel();
        level2.setId(2L);
        level2.setNextLevel(level3.getId());
        level2.setQuestions(questions);

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
        newGameLevel.setMaxScore(100);
        newGameLevel.setTitle("Title of game level");
        newGameLevel.setIncorrectFlagLimit(5);
        newGameLevel.setFlag("Secret flag");
        newGameLevel.setSolution("Solution of the game should be here");
        newGameLevel.setSolutionPenalized(true);
        newGameLevel.setEstimatedDuration(1);
        newGameLevel.setContent("The test entry should be here");

        newInfoLevel = new InfoLevel();
        newInfoLevel.setId(11L);
        newInfoLevel.setTitle("Title of info Level");
        newInfoLevel.setMaxScore(20);
        newInfoLevel.setContent("Content of info level should be here.");

        newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setId(12L);
        newAssessmentLevel.setTitle("Title of assessment level");
        newAssessmentLevel.setAssessmentType(AssessmentType.QUESTIONNAIRE);
        newAssessmentLevel.setMaxScore(0);
        newAssessmentLevel.setInstructions("Instructions should be here");
        newAssessmentLevel.setQuestions("[]");

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setDescription("test1");
        trainingDefinition1.setTitle("test1");
        trainingDefinition1.setState(TDState.RELEASED);
        trainingDefinition1.setSandboxDefinitionRefId(1L);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setDescription("test2");
        trainingDefinition2.setTitle("test2");
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setStartingLevel(infoLevel.getId());
        trainingDefinition2.setSandboxDefinitionRefId(1L);

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
        mockSpringSecurityContextForGet();
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
        TrainingDefinition tDcloned = new TrainingDefinition();
        tDcloned.setTitle("Clone of " + trainingDefinition1.getTitle());
        tDcloned.setId(3L);
        tDcloned.setState(TDState.UNRELEASED);
        tDcloned.setDescription(trainingDefinition1.getDescription());

        given(trainingDefinitionRepository.findById(trainingDefinition1.getId())).willReturn(Optional.of(trainingDefinition1));
        given(trainingDefinitionRepository.save(any(TrainingDefinition.class))).willReturn(tDcloned);

        TrainingDefinition optionalNewClone = trainingDefinitionService.clone(trainingDefinition1.getId());
        assertNotNull(optionalNewClone);
        assertEquals("Clone of " + trainingDefinition1.getTitle(), optionalNewClone.getTitle());
        assertNotEquals(trainingDefinition1.getId(), optionalNewClone.getId());
        assertNotEquals(trainingDefinition1.getState(), optionalNewClone.getState());
        assertEquals(trainingDefinition1.getDescription(), optionalNewClone.getDescription());

        then(trainingDefinitionRepository).should().findById(trainingDefinition1.getId());
        then(trainingDefinitionRepository).should().save(any(TrainingDefinition.class));
    }

    @Test
    public void cloneTrainingDefinitionWithCannotBeClonedException() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot copy unreleased training definition");
        trainingDefinitionService.clone(unreleasedDefinition.getId());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    }

    @Test
    public void cloneTrainingDefinitionWithNull() {
        thrown.expect(ServiceLayerException.class);
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
    public void updateTrainingDefinitionWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition");
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
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition");

        trainingDefinitionService.swapLeft(releasedDefinition.getId(), any(Long.class));
    }

    @Test
    public void swapLeftOnFirstLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot swap left first level");

        trainingDefinitionService.swapLeft(unreleasedDefinition.getId(), level1.getId());
    }

    @Test
    public void swapLeftWithNullDefinition() {
        thrown.expect(ServiceLayerException.class);
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
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot edit released or archived training definition");

        trainingDefinitionService.swapRight(releasedDefinition.getId(), any(Long.class));
    }

    @Test
    public void swapRightOnLastLevel() {
        given(trainingDefinitionRepository.findById(unreleasedDefinition.getId())).willReturn(Optional.of(unreleasedDefinition));
        given(abstractLevelRepository.findById(level3.getId())).willReturn(Optional.of(level3));
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot swap right last level");

        trainingDefinitionService.swapRight(unreleasedDefinition.getId(), level3.getId());
    }

    @Test
    public void swapRightWithNullDefinition() {
        thrown.expect(ServiceLayerException.class);
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

        trainingDefinitionService.deleteOneLevel(unreleasedDefinition.getId(), level2.getId());
        assertEquals(level1.getNextLevel(), level3.getId());

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should(times(3)).findById(any(Long.class));
        then(assessmentLevelRepository).should().delete(level2);
        then(assessmentLevelRepository).should().save(level1);
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
        given(abstractLevelRepository.findById(level1.getId())).willReturn(Optional.of(level1));
        given(abstractLevelRepository.findById(level2.getId())).willReturn(Optional.of(level2));
        given(assessmentLevelRepository.findById(any(Long.class))).willReturn(Optional.of(level2));

        trainingDefinitionService.updateAssessmentLevel(unreleasedDefinition.getId(), level2);

        then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
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
        level.setNextLevel(null);
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
    public void updateGameLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(any(Long.class))).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(any(Long.class))).willReturn(Optional.of(gameLevel));
        given(gameLevelRepository.findById(any(Long.class))).willReturn(Optional.of(gameLevel));
        trainingDefinitionService.updateGameLevel(trainingDefinition2.getId(), gameLevel);

        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
        then(abstractLevelRepository).should().findById(any(Long.class));
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
        level.setNextLevel(null);
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
    public void updateInfoLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(infoLevelRepository.findById(any(Long.class))).willReturn(Optional.of(infoLevel));
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
        level.setNextLevel(null);
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
    public void createGameLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(gameLevelRepository.save(any(GameLevel.class))).willReturn(newGameLevel);
        GameLevel createdLevel = trainingDefinitionService.createGameLevel(trainingDefinition2.getId());

        assertEquals(newGameLevel, createdLevel);
        assertEquals(gameLevel.getNextLevel(), createdLevel.getId());

        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createGameLevelAsFirstLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        given(gameLevelRepository.save(any(GameLevel.class))).willReturn(newGameLevel);

        GameLevel createdLevel = trainingDefinitionService.createGameLevel(definitionWithoutLevels.getId());

        assertNotNull(createdLevel);
        assertEquals(newGameLevel, createdLevel);
        assertEquals(definitionWithoutLevels.getStartingLevel(), createdLevel.getId());

        then(trainingDefinitionRepository).should(times(2)).findById(definitionWithoutLevels.getId());
        then(trainingDefinitionRepository).should().save(definitionWithoutLevels);
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
    public void createInfoLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(infoLevelRepository.save(any(InfoLevel.class))).willReturn(newInfoLevel);

        InfoLevel createdLevel = trainingDefinitionService.createInfoLevel(trainingDefinition2.getId());

        assertNotNull(createdLevel);
        assertEquals(newInfoLevel, createdLevel);
        assertEquals(gameLevel.getNextLevel(), createdLevel.getId());

        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createInfoLevelAsFirstLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        given(infoLevelRepository.save(any(InfoLevel.class))).willReturn(newInfoLevel);

        InfoLevel createdLevel = trainingDefinitionService.createInfoLevel(definitionWithoutLevels.getId());

        assertNotNull(createdLevel);
        assertEquals(newInfoLevel, createdLevel);
        assertEquals(definitionWithoutLevels.getStartingLevel(), createdLevel.getId());

        then(trainingDefinitionRepository).should(times(2)).findById(definitionWithoutLevels.getId());
        then(trainingDefinitionRepository).should().save(definitionWithoutLevels);
    }

    @Test
    public void createInfoLevelWithCannotBeUpdatedException() {
        given(trainingDefinitionRepository.findById(releasedDefinition.getId())).willReturn(Optional.of(releasedDefinition));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot create level in released or archived training definition");

        trainingDefinitionService.createInfoLevel(releasedDefinition.getId());
    }

    @Test
    public void createInfoLevelWithNullDefinitionId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Definition id must not be null");
        trainingDefinitionService.createInfoLevel(null);

    }


    @Test
    public void createAssessmentLevel() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(assessmentLevelRepository.save(any(AssessmentLevel.class))).willReturn(newAssessmentLevel);

        AssessmentLevel createdLevel = trainingDefinitionService.createAssessmentLevel(trainingDefinition2.getId());

        assertNotNull(createdLevel);
        assertEquals(newAssessmentLevel, createdLevel);
        assertEquals(gameLevel.getNextLevel(), createdLevel.getId());

        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
    }

    @Test
    public void createAssessmentLevelAsFirstLevel() {
        given(trainingDefinitionRepository.findById(definitionWithoutLevels.getId())).willReturn(Optional.of(definitionWithoutLevels));
        given(assessmentLevelRepository.save(any(AssessmentLevel.class))).willReturn(newAssessmentLevel);

        AssessmentLevel createdLevel = trainingDefinitionService.createAssessmentLevel(definitionWithoutLevels.getId());

        assertNotNull(createdLevel);
        assertEquals(newAssessmentLevel, createdLevel);
        assertEquals(definitionWithoutLevels.getStartingLevel(), createdLevel.getId());

        then(trainingDefinitionRepository).should(times(2)).findById(definitionWithoutLevels.getId());
        then(trainingDefinitionRepository).should().save(definitionWithoutLevels);
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
    public void findAllLevelsFromDefinition() {
        given(trainingDefinitionRepository.findById(trainingDefinition2.getId())).willReturn(Optional.of(trainingDefinition2));
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
        ArrayList<AbstractLevel> expected = new ArrayList<>();
        expected.add(infoLevel);
        expected.add(gameLevel);

        List<AbstractLevel> actual = trainingDefinitionService.findAllLevelsFromDefinition(trainingDefinition2.getId());

        assertEquals(expected, actual);
        then(abstractLevelRepository).should(times(2)).findById(any(Long.class));
        then(trainingDefinitionRepository).should().findById(trainingDefinition2.getId());
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
        user.setUserRefLogin("userSub");
        given(trainingDefinitionRepository.save(trainingDefinition1)).willReturn(trainingDefinition1);
        given(userRefRepository.findUserByUserRefLogin(anyString())).willReturn(Optional.of(user));
        TrainingDefinition tD = trainingDefinitionService.create(trainingDefinition1);
        deepEquals(trainingDefinition1, tD);
        then(trainingDefinitionRepository).should(times(2)).save(trainingDefinition1);
    }

    @Test
    public void createTrainingInstanceWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training definition must not be null");
        trainingDefinitionService.create(null);
    }

    @Test
    public void findLevelById() {
        given(abstractLevelRepository.findById(infoLevel.getId())).willReturn(Optional.of(infoLevel));
        AbstractLevel abstractLevel = trainingDefinitionService.findLevelById(infoLevel.getId());
        assertTrue(abstractLevel instanceof InfoLevel);
        assertEquals(infoLevel.getId(), abstractLevel.getId());
        then(abstractLevelRepository).should().findById(infoLevel.getId());
    }

    @Test
    public void findLevelById_notExisting() {
        thrown.expect(ServiceLayerException.class);
        trainingDefinitionService.findLevelById(555L);
    }

    @Test
    public void getUsersWithGivenRole() {
        when(groupRef1.getIdmGroupId()).thenReturn(1L);
        when(groupRef2.getIdmGroupId()).thenReturn(2L);
        when(userInfoDTO1.getLogin()).thenReturn("Peter");
        when(userInfoDTO2.getLogin()).thenReturn("Dave");
        given(idmGroupRefRepository.findAllByRoleType(anyString())).willReturn(Arrays.asList(groupRef1, groupRef2));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserInfoDTO>>(new PageResultResource<>(Arrays.asList(userInfoDTO1, userInfoDTO2)), HttpStatus.OK));

        List<String> usersLogins = trainingDefinitionService.getUsersWithGivenRole(RoleType.DESIGNER, pageable);

        assertEquals(2, usersLogins.size());
        assertTrue(usersLogins.contains("Peter"));
        assertTrue(usersLogins.contains("Dave"));
    }

    @Test
    public void getUsersWithGivenRoleWithUserAndGroupError() {
        when(groupRef1.getIdmGroupId()).thenReturn(1L);
        when(groupRef2.getIdmGroupId()).thenReturn(2L);
        given(idmGroupRefRepository.findAllByRoleType(anyString())).willReturn(Arrays.asList(groupRef1, groupRef2));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResource<UserInfoDTO>>(new PageResultResource<>(Arrays.asList()), HttpStatus.NOT_FOUND));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Error while obtaining info about users in designers groups.");
        List<String> usersLogins = trainingDefinitionService.getUsersWithGivenRole(RoleType.DESIGNER, pageable);

    }

    @Test
    public void getUsersWithGivenRoleWithEmptyListOfIdmGroupRefs() {
        given(idmGroupRefRepository.findAllByRoleType(anyString())).willReturn(Collections.emptyList());

        List<String> usersLogins = trainingDefinitionService.getUsersWithGivenRole(RoleType.DESIGNER, pageable);
        assertEquals(0, usersLogins.size());
    }

    @Test
    public void findTDViewGroupByTitle() {
        given(viewGroupRepository.findByTitle(anyString())).willReturn(Optional.ofNullable(viewGroup));
        TDViewGroup group = trainingDefinitionService.findTDViewGroupByTitle("title");

        assertEquals(group, viewGroup);
    }

    @Test
    public void findTDViewGroupByTitleNotFound() {
        given(viewGroupRepository.findByTitle(anyString())).willReturn(Optional.empty());

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("View group with title: view not found.");
        TDViewGroup group = trainingDefinitionService.findTDViewGroupByTitle("view");
    }

    @Test
    public void findUserRefByLogin() {
        UserRef userRef = new UserRef();
        userRef.setUserRefLogin("Dave");
        given(userRefRepository.findUserByUserRefLogin(userRef.getUserRefLogin())).willReturn(Optional.of(userRef));

        UserRef u = trainingDefinitionService.findUserRefByLogin(userRef.getUserRefLogin());

        assertEquals(userRef.getUserRefLogin(), u.getUserRefLogin());
    }

    @Test
    public void findUserRefByLoginNotFound() {
        given(userRefRepository.findUserByUserRefLogin("Herkules")).willReturn(Optional.empty());

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("UserRef with login Herkules not found.");
        UserRef u = trainingDefinitionService.findUserRefByLogin("Herkules");
    }

    @Test
    public void findUserRefById() {
        UserRef userRef = new UserRef();
        userRef.setId(1L);
        given(userRefRepository.findById(userRef.getId())).willReturn(Optional.of(userRef));

        UserRef u = trainingDefinitionService.findUserRefById(userRef.getId());
        assertEquals(userRef.getId(), u.getId());
    }

    @Test
    public void findUserRefByIdNotFound() {
        given(userRefRepository.findById(1L)).willReturn(Optional.empty());

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("UserRef with id 1 not found.");
        UserRef u = trainingDefinitionService.findUserRefById(1L);
    }


    @After
    public void after() {
        reset(trainingDefinitionRepository);
    }

    private void deepEquals(TrainingDefinition expected, TrainingDefinition actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getState(), actual.getState());
    }


    private void mockSpringSecurityContextForGet() {
        JsonObject sub = new JsonObject();
        sub.addProperty("sub", "participant");
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
