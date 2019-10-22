package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class, GameLevelMapperImpl.class,
        InfoLevelMapperImpl.class, AssessmentLevelMapperImpl.class, HintMapperImpl.class,
        BasicLevelInfoMapperImpl.class, TrainingRunMapperImpl.class, BetaTestingGroupMapperImpl.class})
public class TrainingRunFacadeTest {

    @Autowired
    TrainingRunMapperImpl trainingRunMapper;
    @Autowired
    GameLevelMapperImpl gameLevelMapper;
    @Autowired
    AssessmentLevelMapperImpl assessmentLevelMapper;
    @Autowired
    InfoLevelMapperImpl infoLevelMapper;
    @Autowired
    HintMapperImpl hintMapper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingRunFacade trainingRunFacade;

    @Mock
    private TrainingRunService trainingRunService;

    @Mock
    private TrainingInstanceService trainingInstanceService;

    @Mock
    private UserService userService;

    private TrainingRun trainingRun1, trainingRun2;
    private TrainingDefinition trainingDefinition;
    private TrainingInstance trainingInstance;
    private Hint hint;
    private GameLevel gameLevel;
    private InfoLevel infoLevel;
    private AssessmentLevel assessmentLevel;
    private UserRefDTO participantRefDTO;
    private UserRef participant;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingRunFacade = new TrainingRunFacadeImpl(trainingRunService, trainingRunMapper, gameLevelMapper,
                assessmentLevelMapper, infoLevelMapper, hintMapper, userService);

        participant = new UserRef();
        participant.setUserRefId(5L);
        participant.setId(1L);

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.RUNNING);
        trainingRun1.setSolutionTaken(false);
        trainingRun1.setParticipantRef(participant);

        hint = new Hint();
        hint.setId(1L);
        hint.setContent("Hint");
        hint.setTitle("Hint Title");

        participantRefDTO = new UserRefDTO();
        participantRefDTO.setUserRefLogin("4457352@muni.cz");
        participantRefDTO.setUserRefId(2L);


        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.FINISHED);

        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setId(1L);
        trainingDefinition.setState(TDState.RELEASED);
        trainingDefinition.setShowStepperBar(true);

        gameLevel = new GameLevel();
        gameLevel.setId(1L);
        gameLevel.setFlag("game flag");
        gameLevel.setContent("game content");
        gameLevel.setTrainingDefinition(trainingDefinition);
        gameLevel.setOrder(0);
        trainingRun1.setCurrentLevel(gameLevel);

        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(2L);
        assessmentLevel.setInstructions("Instructions");
        assessmentLevel.setAssessmentType(AssessmentType.TEST);
        assessmentLevel.setOrder(1);
        assessmentLevel.setTrainingDefinition(trainingDefinition);

        infoLevel = new InfoLevel();
        infoLevel.setId(3L);
        infoLevel.setContent("content");
        infoLevel.setOrder(2);
        infoLevel.setTrainingDefinition(trainingDefinition);

        trainingInstance = new TrainingInstance();
        trainingInstance.setId(1L);
        trainingInstance.setTitle("test");
        trainingInstance.setTrainingDefinition(trainingDefinition);
        trainingRun1.setTrainingInstance(trainingInstance);


    }

    @Test
    public void findTrainingRunById() {
        given(trainingRunService.findById(any(Long.class))).willReturn(trainingRun1);
        given(userService.getUserRefDTOByUserRefId(anyLong())).willReturn(participantRefDTO);
        trainingRunFacade.findById(trainingRun1.getId());
        then(trainingRunService).should().findById(trainingRun1.getId());
    }

    @Test
    public void findTrainingRunByIdWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingRunService).findById(1L);
        trainingRunFacade.findById(1L);
    }

    @Test
    public void findAllTrainingRuns() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<TrainingRun>(expected);

        PathBuilder<TrainingRun> tR = new PathBuilder<TrainingRun>(TrainingRun.class, "trainingRun");
        Predicate predicate = tR.isNotNull();

        given(trainingRunService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);
    }

    @Test
    public void isCorrectFlagBeforeSolutionTaken() {
        given(trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag")).willReturn(true);
        given(trainingRunService.getRemainingAttempts(trainingRun1.getId())).willReturn(1);
        IsCorrectFlagDTO correctFlagDTO = trainingRunFacade.isCorrectFlag(trainingRun1.getId(), "flag");
        assertTrue(correctFlagDTO.isCorrect());
        assertEquals(1, correctFlagDTO.getRemainingAttempts());
    }

    @Test
    public void isCorrectFlagAfterSolutionTaken() {
        given(trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag")).willReturn(false);
        IsCorrectFlagDTO correctFlagDTO = trainingRunFacade.isCorrectFlag(trainingRun1.getId(), "flag");
        assertFalse(correctFlagDTO.isCorrect());
        assertEquals(0, correctFlagDTO.getRemainingAttempts());
    }

    @Test
    public void accessTrainingRun() {
        given(trainingRunService.accessTrainingRun(anyString())).willReturn(trainingRun1);
        given(trainingRunService.assignSandbox(any(TrainingRun.class))).willReturn(trainingRun1);
        Object result = trainingRunFacade.accessTrainingRun("password");
        assertEquals(AccessTrainingRunDTO.class, result.getClass());
        then(trainingRunService).should().accessTrainingRun("password");
    }

    @Test
    public void accessTrainingRunWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        given(trainingInstanceService.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(anyString())).willReturn(trainingInstance);
        willThrow(ServiceLayerException.class).given(trainingRunService).accessTrainingRun("pass");
        trainingRunFacade.accessTrainingRun("pass");
    }

    @Test
    public void getNextLevel() {
        given(trainingRunService.getNextLevel(3L)).willReturn(infoLevel);
        trainingRunFacade.getNextLevel(3L);
        then(trainingRunService).should().getNextLevel(3L);
    }

    @Test
    public void getNextLevelWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingRunService).getNextLevel(1L);
        trainingRunFacade.getNextLevel(1L);
    }

    @Test
    public void getHint() {
        given(trainingRunService.getHint(anyLong(), anyLong())).willReturn(hint);
        trainingRunFacade.getHint(1L, 1L);
        then(trainingRunService).should().getHint(1L, 1L);
    }

    @Test
    public void getHintWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingRunService).getHint(1L, 1L);
        trainingRunFacade.getHint(1L, 1L);
    }

    @Test
    public void getSolution() {
        given(trainingRunService.getSolution(1L)).willReturn("game solution");
        trainingRunFacade.getSolution(trainingRun1.getId());
        then(trainingRunService).should().getSolution(1L);
    }

    @Test
    public void getSolutionWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingRunService).getSolution(1L);
        trainingRunFacade.getSolution(1L);
    }

    @Test
    public void evaluateResponsesToAssessment() {
        trainingRunFacade.evaluateResponsesToAssessment(trainingRun1.getId(), "response");
        then(trainingRunService).should().evaluateResponsesToAssessment(trainingRun1.getId(), "response");
    }

    @Test
    public void finishTrainingRun() {
        trainingRunFacade.finishTrainingRun(trainingRun1.getId());
        then(trainingRunService).should().finishTrainingRun(trainingRun1.getId());
    }

    @Test
    public void finishTrainingRunWithServiceException() {
        willThrow(ServiceLayerException.class).given(trainingRunService).finishTrainingRun(trainingRun1.getId());
        thrown.expect(FacadeLayerException.class);
        trainingRunFacade.finishTrainingRun(trainingRun1.getId());
    }

    @Test
    public void getParticipant() {
        given(trainingRunService.findById(trainingRun1.getId())).willReturn(trainingRun1);
        given(userService.getUserRefDTOByUserRefId(participant.getUserRefId())).willReturn(participantRefDTO);
        UserRefDTO foundParticipantRefDTO = trainingRunFacade.getParticipant(trainingRun1.getId());
        Assert.assertEquals(participantRefDTO, foundParticipantRefDTO);
    }

    @Test
    public void getParticipantTrainingRunNotFound() {
        willThrow(new ServiceLayerException("Training run not found.", ErrorCode.RESOURCE_NOT_FOUND)).given(trainingRunService).findById(15L);
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("Training run not found.");
        trainingRunFacade.getParticipant(15L);
    }

    @Test
    public void getParticipantCallingUserAndGroupError() {
        willThrow(new ServiceLayerException("Error when calling user and group side.", ErrorCode.UNEXPECTED_ERROR)).given(trainingRunService).findById(15L);
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("Error when calling user and group side.");
        trainingRunFacade.getParticipant(15L);
    }

    private void deepEquals(TrainingRun expected, TrainingRunDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getState(), actual.getState());
    }

}

