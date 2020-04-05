package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.service.SecurityService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
@SpringBootTest(classes = {LevelMapperImpl.class, TrainingDefinitionMapperImpl.class, UserRefMapperImpl.class,
        LevelMapperImpl.class, HintMapperImpl.class, TrainingRunMapperImpl.class, BetaTestingGroupMapperImpl.class,
        AttachmentMapperImpl.class})
public class TrainingRunFacadeTest {

    @Autowired
    TrainingRunMapperImpl trainingRunMapper;
    @Autowired
    LevelMapperImpl levelMapper;
    @Autowired
    HintMapperImpl hintMapper;
    @Autowired
    TestDataFactory testDataFactory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingRunFacade trainingRunFacade;

    @Mock
    private TrainingRunService trainingRunService;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserService userService;

    private TrainingRun trainingRun1, trainingRun2;
    private Hint hint;
    private GameLevel gameLevel;
    private InfoLevel infoLevel;
    private AssessmentLevel assessmentLevel;
    private UserRefDTO participantRefDTO;
    private UserRef participant;
    private TrainingInstance trainingInstance;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingRunFacade = new TrainingRunFacade(trainingRunService, securityService, userService, trainingRunMapper,
                levelMapper, hintMapper);

        participant = new UserRef();
        participant.setUserRefId(5L);
        participant.setId(1L);

        trainingRun1 = testDataFactory.getRunningRun();
        trainingRun1.setId(1L);
        trainingRun1.setParticipantRef(participant);

        hint = testDataFactory.getHint1();
        hint.setId(1L);

        participantRefDTO = new UserRefDTO();
        participantRefDTO.setUserRefSub("4457352@muni.cz");
        participantRefDTO.setUserRefId(2L);


        trainingRun2 = testDataFactory.getFinishedRun();
        trainingRun2.setId(2L);

        TrainingDefinition trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);

        gameLevel = testDataFactory.getPenalizedLevel();
        gameLevel.setId(1L);
        gameLevel.setTrainingDefinition(trainingDefinition);
        gameLevel.setOrder(0);
        trainingRun1.setCurrentLevel(gameLevel);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(2L);
        assessmentLevel.setOrder(1);
        assessmentLevel.setTrainingDefinition(trainingDefinition);

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(3L);
        infoLevel.setOrder(2);
        infoLevel.setTrainingDefinition(trainingDefinition);

        trainingInstance = testDataFactory.getConcludedInstance();
        trainingInstance.setId(1L);
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
        given(trainingRunService.getTrainingInstanceForParticularAccessToken(anyString())).willReturn(trainingInstance);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
        given(trainingRunService.findRunningTrainingRunOfUser(anyString(), anyLong())).willReturn(Optional.empty());
        given(trainingRunService.createTrainingRun(trainingInstance, 1L)).willReturn(trainingRun1);
        trainingRunFacade.accessTrainingRun("password");
        then(trainingRunService).should().trAcquisitionLockToPreventManyRequestsFromSameUser(1l, trainingInstance.getId(), "password");
        then(trainingRunService).should().createTrainingRun(trainingInstance, 1L);
    }

    @Test
    public void accessRunningTrainingRun() {
        given(trainingRunService.getTrainingInstanceForParticularAccessToken(anyString())).willReturn(trainingInstance);
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
        given(trainingRunService.findRunningTrainingRunOfUser(anyString(), anyLong())).willReturn(Optional.of(trainingRun1));
        given(trainingRunService.resumeTrainingRun(anyLong())).willReturn(trainingRun1);
        trainingRunFacade.accessTrainingRun("password");
        then(trainingRunService).should().resumeTrainingRun(anyLong());
    }

    @Test
    public void deleteTrainingRun(){
        trainingRunFacade.deleteTrainingRun(1L, true);
        then(trainingRunService).should().deleteTrainingRun(1L, true);
    }

    @Test
    public void getNextLevel() {
        given(trainingRunService.getNextLevel(3L)).willReturn(infoLevel);
        trainingRunFacade.getNextLevel(3L);
        then(trainingRunService).should().getNextLevel(3L);
    }

    @Test
    public void getHint() {
        given(trainingRunService.getHint(anyLong(), anyLong())).willReturn(hint);
        trainingRunFacade.getHint(1L, 1L);
        then(trainingRunService).should().getHint(1L, 1L);
    }

    @Test
    public void getSolution() {
        given(trainingRunService.getSolution(1L)).willReturn("game solution");
        trainingRunFacade.getSolution(trainingRun1.getId());
        then(trainingRunService).should().getSolution(1L);
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
    public void getParticipant() {
        given(trainingRunService.findById(trainingRun1.getId())).willReturn(trainingRun1);
        given(userService.getUserRefDTOByUserRefId(participant.getUserRefId())).willReturn(participantRefDTO);
        UserRefDTO foundParticipantRefDTO = trainingRunFacade.getParticipant(trainingRun1.getId());
        Assert.assertEquals(participantRefDTO, foundParticipantRefDTO);
    }
}
