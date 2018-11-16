package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AuthorRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.impl.TrainingDefinitionFacadeImpl;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.mapping.BeanMappingImpl;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
public class TrainingDefinitionFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingDefinitionFacade trainingDefinitionFacade;

    @Mock
    private TrainingDefinitionService trainingDefinitionService;

    private BeanMapping beanMapping;

    private TrainingDefinition trainingDefinition1, trainingDefinition2;
    private TrainingDefinitionUpdateDTO trainingDefinitionUpdate;
    private TrainingDefinitionCreateDTO trainingDefinitionCreate;

    private AssessmentLevel assessmentLevel;
    private AssessmentLevelUpdateDTO alUpdate;

    private GameLevel gameLevel;
    private GameLevelUpdateDTO gameLevelUpdate;

    private InfoLevel infoLevel;
    private InfoLevelUpdateDTO infoLevelUpdate;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingDefinitionFacade = new TrainingDefinitionFacadeImpl(trainingDefinitionService, new BeanMappingImpl(new ModelMapper()));
        beanMapping = new BeanMappingImpl(new ModelMapper());
        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(1L);

        alUpdate = new AssessmentLevelUpdateDTO();
        alUpdate.setId(2L);

        gameLevel = new GameLevel();
        gameLevel.setId(2L);
        gameLevel.setNextLevel(null);
        gameLevel.setSolution("solution");

        gameLevelUpdate = new GameLevelUpdateDTO();
        gameLevelUpdate.setId(2L);
        gameLevelUpdate.setTitle("title");
        gameLevelUpdate.setAttachments(new String[3]);
        gameLevelUpdate.setContent("Content");
        gameLevelUpdate.setEstimatedDuration(1000);
        gameLevelUpdate.setFlag("flag1");
        gameLevelUpdate.setIncorrectFlagLimit(4);
        gameLevelUpdate.setSolutionPenalized(true);
        gameLevelUpdate.setSolution("solution");

        infoLevel = new InfoLevel();
        infoLevel.setId(3L);
        infoLevel.setNextLevel(gameLevel.getId());

        infoLevelUpdate = new InfoLevelUpdateDTO();
        infoLevelUpdate.setId(3L);
        infoLevelUpdate.setTitle("some title");
        infoLevelUpdate.setContent("some content");

        alUpdate = new AssessmentLevelUpdateDTO();
        alUpdate.setInstructions("instructions");
        alUpdate.setMaxScore(50);
        alUpdate.setQuestions("test");
        alUpdate.setTitle("Some title");
        alUpdate.setType(AssessmentType.QUESTIONNAIRE);

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setId(1L);
        trainingDefinition1.setState(TDState.RELEASED);
        trainingDefinition1.setStartingLevel(infoLevel.getId());

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setStartingLevel(infoLevel.getId());

        trainingDefinitionUpdate = new TrainingDefinitionUpdateDTO();
        trainingDefinitionUpdate.setId(4L);
        trainingDefinitionUpdate.setState(TDState.UNRELEASED);

        Set<Long> authorRefSet = new HashSet<>();
        authorRefSet.add(1L);
        trainingDefinitionCreate = new TrainingDefinitionCreateDTO();
        trainingDefinitionCreate.setDescription("TD desc");
        trainingDefinitionCreate.setOutcomes(new String[0]);
        trainingDefinitionCreate.setPrerequisities(new String[0]);
        trainingDefinitionCreate.setState(TDState.ARCHIVED);
        trainingDefinitionCreate.setTitle("TD some title");
        trainingDefinitionCreate.setAutIds(authorRefSet);
    }

    @Test
    public void findTrainingDefinitionById() {
        given(trainingDefinitionService.findById(1L)).willReturn(trainingDefinition1);
        trainingDefinitionFacade.findById(1L);
        then(trainingDefinitionService).should().findById(1L);
    }

    @Test
    public void findTrainingDefinitionByIdWithFacadeLayerException() {
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).findById(any(long.class));
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.findById(any(Long.class));
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
    public void updateTrainingDefinitionWithFacadeLayerException() {
        willThrow(ServiceLayerException.class).given(trainingDefinitionService)
                .update(any(TrainingDefinition.class));
        thrown.expect(FacadeLayerException.class);
        trainingDefinitionFacade.update(trainingDefinitionUpdate);
    }

    @Test
    public void createTrainingDefinition() {
        given(trainingDefinitionService.create(beanMapping.mapTo(trainingDefinitionCreate, TrainingDefinition.class)))
                .willReturn(beanMapping.mapTo(trainingDefinitionCreate, TrainingDefinition.class));
        trainingDefinitionFacade.create(trainingDefinitionCreate);
        then(trainingDefinitionService).should().create(beanMapping.mapTo(trainingDefinitionCreate, TrainingDefinition.class));
    }

    @Test
    public void createTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.create(null);
    }

    @Test
    public void cloneTrainingDefinition() {
        given(trainingDefinitionService.clone(trainingDefinition1.getId())).willReturn(trainingDefinition1);
        trainingDefinitionFacade.clone(trainingDefinition1.getId());
        then(trainingDefinitionService).should().clone(trainingDefinition1.getId());
    }

    @Test
    public void cloneTrainingDefinitionWithNull() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.clone(null);
    }

    @Test
    public void cloneTrainingDefinitionWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).clone(any(Long.class));
        trainingDefinitionFacade.clone(any(Long.class));
    }

    @Test
    public void swapLeft() {
        trainingDefinitionFacade.swapLeft(trainingDefinition1.getId(), assessmentLevel.getId());
        then(trainingDefinitionService).should().swapLeft(trainingDefinition1.getId(), assessmentLevel.getId());
    }

    @Test
    public void swapLeftWithNullTrainingDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.swapLeft(null, 1L);
    }

    @Test
    public void swapLeftWithNullLevelId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.swapLeft(1L, null);
    }

    @Test
    public void swapLeftWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).swapLeft(any(Long.class), any(Long.class));
        trainingDefinitionFacade.swapLeft(any(Long.class), any(Long.class));
    }

    @Test
    public void swapRight() {
        trainingDefinitionFacade.swapRight(trainingDefinition1.getId(), assessmentLevel.getId());
        then(trainingDefinitionService).should().swapRight(trainingDefinition1.getId(), assessmentLevel.getId());
    }

    @Test
    public void swapRightWithNullTrainingDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.swapRight(null, 1L);
    }

    @Test
    public void swapRightWithNullLevelId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.swapRight(1L, null);
    }

    @Test
    public void swapRightWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).swapRight(any(Long.class), any(Long.class));
        trainingDefinitionFacade.swapRight(any(Long.class), any(Long.class));
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
    public void deleteTrainingDefinitionWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).delete(any(Long.class));
        trainingDefinitionFacade.delete(1L);
    }

    @Test
    public void deleteOneLevel() {
        trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), assessmentLevel.getId());
        then(trainingDefinitionService).should().deleteOneLevel(trainingDefinition1.getId(), assessmentLevel.getId());
    }

    @Test
    public void deleteOneLevelWithNullDefinition() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.deleteOneLevel(null, assessmentLevel.getId());
    }

    @Test
    public void deleteOneLevelWithNullLevel() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.deleteOneLevel(trainingDefinition1.getId(), null);
    }

    @Test
    public void deleteOneLevelWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).deleteOneLevel(any(Long.class), any(Long.class));
        trainingDefinitionFacade.deleteOneLevel(1L, 1L);
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
    public void updateAssessmentLevelWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).updateAssessmentLevel(any(Long.class), any(AssessmentLevel.class));
        trainingDefinitionFacade.updateAssessmentLevel(1L, alUpdate);
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
    public void updateGameLevelWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).updateGameLevel(any(Long.class), any(GameLevel.class));
        trainingDefinitionFacade.updateGameLevel(1L, gameLevelUpdate);
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


    @Test
    public void updateInfoLevelWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingDefinitionService).updateInfoLevel(any(Long.class), any(InfoLevel.class));
        trainingDefinitionFacade.updateInfoLevel(1L, infoLevelUpdate);
    }

    @Test
    public void createInfoLevel() {
        given(trainingDefinitionService.createInfoLevel(trainingDefinition1.getId())).willReturn(infoLevel);
        trainingDefinitionFacade.createInfoLevel(trainingDefinition1.getId());
        then(trainingDefinitionService).should().createInfoLevel(trainingDefinition1.getId());
    }

    @Test
    public void createInfoLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createInfoLevel(null);
    }

    @Test
    public void createInfoLevelWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        given(trainingDefinitionService.createInfoLevel(any(Long.class))).willThrow(ServiceLayerException.class);
        trainingDefinitionFacade.createInfoLevel(any(Long.class));
    }

    @Test
    public void createGameLevel() {
        given(trainingDefinitionService.createGameLevel(trainingDefinition1.getId())).willReturn(gameLevel);
        trainingDefinitionFacade.createGameLevel(trainingDefinition1.getId());
        then(trainingDefinitionService).should().createGameLevel(trainingDefinition1.getId());
    }

    @Test
    public void createGameLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createGameLevel(null);
    }

    @Test
    public void createGameLevelWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        given(trainingDefinitionService.createGameLevel(any(Long.class))).willThrow(ServiceLayerException.class);
        trainingDefinitionFacade.createGameLevel(any(Long.class));
    }

    @Test
    public void createAssessmentLevel() {
        given(trainingDefinitionService.createAssessmentLevel(trainingDefinition1.getId())).willReturn(assessmentLevel);
        trainingDefinitionFacade.createAssessmentLevel(trainingDefinition1.getId());
        then(trainingDefinitionService).should().createAssessmentLevel(trainingDefinition1.getId());
    }

    @Test
    public void createAssessmentLevelWithNullDefinitionId() {
        thrown.expect(NullPointerException.class);
        trainingDefinitionFacade.createAssessmentLevel(null);
    }

    @Test
    public void createAssessmentLevelWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        given(trainingDefinitionService.createAssessmentLevel(any(Long.class))).willThrow(ServiceLayerException.class);
        trainingDefinitionFacade.createAssessmentLevel(any(Long.class));
    }

    private void deepEquals(TrainingDefinition expected, TrainingDefinitionDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getState(), actual.getState());
    }

}
