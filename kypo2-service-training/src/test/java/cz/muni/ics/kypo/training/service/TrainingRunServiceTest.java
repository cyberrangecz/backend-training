package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileReader;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class TrainingRunServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingRunService trainingRunService;

    @Autowired
    private TestDataFactory testDataFactory;

    @Mock
    private TRAcquisitionLockRepository trAcquisitionLockRepository;
    @Mock
    private TrainingRunRepository trainingRunRepository;
    @Mock
    private AuditEventsService auditEventService;
    @Mock
    private AbstractLevelRepository abstractLevelRepository;
    @Mock
    private TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    private UserRefRepository participantRefRepository;
    @Mock
    private HintRepository hintRepository;
    @Mock
    private ExchangeFunction exchangeFunction;
    @Mock
    private WebClient sandboxServiceWebClient;
    @Mock
    private SecurityService securityService;

    private TrainingRun trainingRun1, trainingRun2;
    private GameLevel gameLevel, gameLevel2;
    private AssessmentLevel assessmentLevel;
    private InfoLevel infoLevel, infoLevel2;
    private Hint hint1, hint2;
    private TrainingInstance trainingInstance1, trainingInstance2;
    private UserRef participantRef;
    private SandboxInfo sandboxInfo;
    private TrainingDefinition trainingDefinition, trainingDefinition2;
    private JSONParser parser = new JSONParser();
    private String responses, questions;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        sandboxServiceWebClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();
        trainingRunService = new TrainingRunService(trainingRunRepository, abstractLevelRepository, trainingInstanceRepository,
                participantRefRepository, hintRepository, auditEventService, securityService, sandboxServiceWebClient, trAcquisitionLockRepository);
        parser = new JSONParser();
        try {
            questions = parser.parse(new FileReader(ResourceUtils.getFile("classpath:questions.json"))).toString();
            responses = parser.parse(new FileReader(ResourceUtils.getFile("classpath:responses.json"))).toString();
        } catch (IOException | ParseException ex) {
        }

        trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);
        trainingDefinition.setAuthors(new HashSet<>());

        trainingDefinition2 = testDataFactory.getUnreleasedDefinition();
        trainingDefinition2.setId(2L);

        trainingInstance2 = testDataFactory.getConcludedInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTrainingDefinition(trainingDefinition2);

        trainingInstance1 = testDataFactory.getOngoingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTrainingDefinition(trainingDefinition);

        participantRef = new UserRef();
        participantRef.setId(1L);
        participantRef.setUserRefId(3L);

        hint1 = testDataFactory.getHint1();
        hint1.setId(1L);

        gameLevel = testDataFactory.getPenalizedLevel();
        gameLevel.setId(1L);
        gameLevel.setHints(new HashSet<>(Arrays.asList(hint1, hint2)));
        gameLevel.setOrder(0);
        gameLevel.setTrainingDefinition(trainingDefinition);
        hint1.setGameLevel(gameLevel);

        gameLevel2 = testDataFactory.getNonPenalizedLevel();
        gameLevel2.setId(1L);
        gameLevel2.setOrder(0);
        gameLevel2.setTrainingDefinition(trainingDefinition2);

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(2L);
        infoLevel.setOrder(1);
        infoLevel.setTrainingDefinition(trainingDefinition);

        infoLevel2 = testDataFactory.getInfoLevel2();
        infoLevel2.setId(2L);
        infoLevel2.setOrder(1);
        infoLevel2.setTrainingDefinition(trainingDefinition2);

        sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(7L);

        trainingRun1 = testDataFactory.getRunningRun();
        trainingRun1.setId(1L);
        trainingRun1.setCurrentLevel(gameLevel);
        trainingRun1.setParticipantRef(participantRef);
        trainingRun1.setTrainingInstance(trainingInstance1);
        trainingRun1.setTrainingInstance(trainingInstance1);

        trainingRun2 = testDataFactory.getRunningRun();
        trainingRun2.setId(2L);
        trainingRun2.setCurrentLevel(infoLevel);
        trainingRun2.setParticipantRef(participantRef);
        trainingRun2.setTrainingInstance(trainingInstance2);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(3L);
        assessmentLevel.setQuestions(questions);
    }

    @Test
    public void getTrainingRunById() {
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));

        TrainingRun t = trainingRunService.findById(trainingRun1.getId());
        assertEquals(t.getId(), trainingRun1.getId());
        assertEquals(t.getState(), trainingRun1.getState());

        then(trainingRunRepository).should().findById(trainingRun1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getTrainingRunById_NotFound() {
        Long id = 6L;
        trainingRunService.findById(id);
    }

    @Test
    public void findByIdWithLevel() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));

        Optional<TrainingRun> optionalTrainingRun = trainingRunRepository.findByIdWithLevel(trainingRun1.getId());
        assertTrue(optionalTrainingRun.isPresent());
        assertTrue(optionalTrainingRun.get().getCurrentLevel() instanceof GameLevel);
    }

    @Test(expected = EntityNotFoundException.class)
    public void findById_LevelNotFound() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.empty());
        trainingRunService.findByIdWithLevel(100L);
    }

    @Test
    public void findAll() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> p = new PageImpl<>(expected);
        PathBuilder<TrainingRun> t = new PathBuilder<>(TrainingRun.class, "trainingRun");
        Predicate predicate = t.isNotNull();
        given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page<TrainingRun> pr = trainingRunService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findAll_empty() {
        Page<TrainingRun> p = new PageImpl<>(new ArrayList<>());
        PathBuilder<TrainingRun> t = new PathBuilder<>(TrainingRun.class, "trainingRun");
        Predicate predicate = t.isNotNull();
        given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page<TrainingRun> pr = trainingRunRepository.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(0, pr.getTotalElements());
    }

    @Test
    public void deleteFinishedTrainingRun() {
        trainingRun1.setState(TRState.FINISHED);
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.deleteTrainingRun(trainingRun1.getId(), false);

        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun1.getParticipantRef().getUserRefId(),
                                                                                                 trainingRun1.getTrainingInstance().getId());
        then(trainingRunRepository).should().delete(trainingRun1);
    }

    @Test(expected = EntityConflictException.class)
    public void deleteRunningTrainingRun() {
        trainingRun1.setState(TRState.RUNNING);
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.deleteTrainingRun(trainingRun1.getId(), false);

        then(trAcquisitionLockRepository).should(never()).deleteByParticipantRefIdAndTrainingInstanceId(trainingRun1.getParticipantRef().getUserRefId(),
                trainingRun1.getTrainingInstance().getId());
        then(trainingRunRepository).should(never()).delete(trainingRun1);
    }

    @Test
    public void deleteRunningTrainingRun_Force() {
        trainingRun1.setState(TRState.RUNNING);
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.deleteTrainingRun(trainingRun1.getId(), true);

        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun1.getParticipantRef().getUserRefId(),
                trainingRun1.getTrainingInstance().getId());
        then(trainingRunRepository).should().delete(trainingRun1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteTrainingRun_NotFound() {
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.empty());
        trainingRunService.deleteTrainingRun(trainingRun1.getId(), true);

        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun1.getParticipantRef().getUserRefId(),
                trainingRun1.getTrainingInstance().getId());
        then(trainingRunRepository).should().delete(trainingRun1);
    }

    @Test
    public void existsAnyForTrainingInstance() {
        given(trainingRunRepository.existsAnyForTrainingInstance(trainingRun1.getTrainingInstance().getId())).willReturn(true);
        boolean result = trainingRunService.existsAnyForTrainingInstance(trainingRun1.getTrainingInstance().getId());
        assertTrue(result);
    }

    @Test
    public void findAllByParticipantUserRefId() {
        Page<TrainingRun> expectedPage = new PageImpl<>(Arrays.asList(trainingRun1, trainingRun2));

        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participantRef.getUserRefId());
        given(trainingRunRepository.findAllByParticipantRefId(eq(participantRef.getUserRefId()), any(PageRequest.class))).willReturn(expectedPage);
        Page<TrainingRun> resultPage = trainingRunService.findAllByParticipantRefUserRefId(PageRequest.of(0, 2));

        assertEquals(expectedPage, resultPage);
        then(trainingRunRepository).should().findAllByParticipantRefId(participantRef.getUserRefId(), PageRequest.of(0, 2));
    }



    @Test
    public void findAllByTrainingInstanceId() {
        given(trainingRunRepository.findAllByTrainingInstanceId(trainingInstance1.getId())).willReturn(Set.of(trainingRun1, trainingRun2));
        Set<TrainingRun> resultSet = trainingRunService.findAllByTrainingInstanceId(trainingRun1.getTrainingInstance().getId());
        assertEquals(Set.of(trainingRun1, trainingRun2), resultSet);
    }

    @Test
    public void getNextLevel() {
        List<AbstractLevel> levels = new ArrayList<>();
        levels.add(gameLevel);
        levels.add(infoLevel);
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(any(Long.class))).willReturn(levels);
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);

        AbstractLevel resultAbstractLevel = trainingRunService.getNextLevel(trainingRun1.getId());

        assertEquals(trainingRun1.getCurrentLevel().getId(), resultAbstractLevel.getId());
        assertEquals(trainingRun1.getMaxLevelScore(), infoLevel.getMaxScore());
        assertTrue(trainingRun1.isLevelAnswered()); // because next level is info and it is always set to true
        then(trainingRunRepository).should().findByIdWithLevel(trainingRun1.getId());
        then(trainingRunRepository).should().save(trainingRun1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNextLevel_TrainingRunNotFound() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.empty());
        trainingRunService.getNextLevel(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void getNextLevel_NotAnswered() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.getNextLevel(trainingRun1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNextLevel_NoNextLevel() {
        trainingRun2.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun2));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
        trainingRunService.getNextLevel(trainingRun2.getId());
    }

    @Test
    public void findAllByTrainingDefinitionAndParticipant() {
        Page<TrainingRun> expectedPage = new PageImpl<>(Collections.singletonList(trainingRun2));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participantRef.getUserRefId());
        given(trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantUserRefId(any(Long.class), eq(participantRef.getUserRefId()), any(Pageable.class))).willReturn(expectedPage);

        Page<TrainingRun> resultPage = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinition2.getId(), PageRequest.of(0, 2));

        assertEquals(expectedPage, resultPage);
        then(trainingRunRepository).should().findAllByTrainingDefinitionIdAndParticipantUserRefId(trainingDefinition2.getId(), participantRef.getUserRefId(), PageRequest.of(0, 2));
    }

    @Test
    public void findAllByTrainingDefinition() {
        Page<TrainingRun> expectedPage = new PageImpl<>(Arrays.asList(trainingRun1, trainingRun2));
        given(trainingRunRepository.findAllByTrainingDefinitionId(any(Long.class), any(PageRequest.class))).willReturn(expectedPage);
        Page<TrainingRun> resultPage = trainingRunService.findAllByTrainingDefinition(trainingDefinition.getId(), PageRequest.of(0, 2));
        assertEquals(expectedPage, resultPage);
        then(trainingRunRepository).should().findAllByTrainingDefinitionId(trainingDefinition.getId(), PageRequest.of(0, 2));
    }

    @Test
    public void getLevels() {
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinition.getId()))
                .willReturn(List.of(gameLevel, gameLevel2, infoLevel));
        List<AbstractLevel> result = trainingRunService.getLevels(trainingDefinition.getId());
        assertEquals(List.of(gameLevel, gameLevel2, infoLevel), result);
    }

    @Test
    public void createTrainingRun() throws Exception{
        given(abstractLevelRepository.findFirstLevelByTrainingDefinitionId(eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
                .willReturn(List.of(gameLevel));
        given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
                .willReturn(Optional.of(participantRef));
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);

        TrainingRun trainingRun = trainingRunService.createTrainingRun(trainingInstance1, participantRef.getId());
        then(trainingRunRepository).should().save(any(TrainingRun.class));
        assertEquals(trainingRun1, trainingRun);
    }

    @Test
    public void createTrainingRun_NewParticipant() throws Exception{
        UserRef newParticipant = new UserRef();
        newParticipant.setUserRefId(participantRef.getUserRefId());
        sandboxInfo.setLockId(1);
        given(abstractLevelRepository.findFirstLevelByTrainingDefinitionId(eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
                .willReturn(List.of(gameLevel));
        given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
                .willReturn(Optional.empty());
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(newParticipant);
        given(participantRefRepository.save(newParticipant)).willReturn(participantRef);

        TrainingRun trainingRun = trainingRunService.createTrainingRun(trainingInstance1, participantRef.getId());
        then(trainingRunRepository).should().save(any(TrainingRun.class));
        then(participantRefRepository).should().save(any(UserRef.class));
        assertEquals(trainingRun1, trainingRun);
    }

    @Test(expected = EntityNotFoundException.class)
    public void createTrainingRun_NoStartingLevel() {
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        given(abstractLevelRepository.findFirstLevelByTrainingDefinitionId(eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
                .willReturn(List.of());
        trainingRunService.createTrainingRun(trainingInstance1, participantRef.getId());
    }

    @Test(expected = MicroserviceApiException.class)
    public void createTrainingRun_UserManagementError() {
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        given(abstractLevelRepository.findFirstLevelByTrainingDefinitionId(eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
                .willReturn(List.of(gameLevel));
        given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
                .willReturn(Optional.empty());
        willThrow(new MicroserviceApiException("Error when calling user managements service")).given(securityService).createUserRefEntityByInfoFromUserAndGroup();
        trainingRunService.createTrainingRun(trainingInstance1, participantRef.getId());
        then(trainingRunRepository).should(never()).save(any(TrainingRun.class));
    }


    @Test
    public void findRunningTrainingRunOfUser() {
        given(trainingRunRepository.findRunningTrainingRunOfUser(trainingInstance1.getAccessToken(), participantRef.getUserRefId()))
                .willReturn(Optional.of(trainingRun1));
        Optional<TrainingRun> result = trainingRunService.findRunningTrainingRunOfUser(trainingInstance1.getAccessToken(), participantRef.getUserRefId());
        assertTrue(result.isPresent());
    }

    @Test
    public void trAcquisitionLockToPreventManyRequestsFromSameUser() {
        TRAcquisitionLock trAcquisitionLock = new TRAcquisitionLock(participantRef.getUserRefId(), trainingInstance1.getId(), LocalDateTime.now());
        trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRef.getUserRefId(), trainingInstance1.getId(), trainingInstance1.getAccessToken());
        then(trAcquisitionLockRepository).should().saveAndFlush(trAcquisitionLock);
    }

    @Test(expected = TooManyRequestsException.class)
    public void trAcquisitionLockToPreventManyRequestsFromSameUser_AlreadyExists() {
        TRAcquisitionLock trAcquisitionLock = new TRAcquisitionLock(participantRef.getUserRefId(), trainingInstance1.getId(), LocalDateTime.now());
        willThrow(DataIntegrityViolationException.class).given(trAcquisitionLockRepository).saveAndFlush(trAcquisitionLock);
        trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRef.getUserRefId(), trainingInstance1.getId(), trainingInstance1.getAccessToken());
    }

    @Test
    public void deleteTrAcquisitionLockToPreventManyRequestsFromSameUser() {
        trainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(participantRef.getId(), trainingInstance1.getId());
        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(participantRef.getId(), trainingInstance1.getId());
    }

    @Test
    public void assignSandbox() throws Exception {
        trainingRun1.setSandboxInstanceRefId(null);
        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(sandboxInfo));
        trainingRunService.assignSandbox(trainingRun1, trainingRun1.getTrainingInstance().getPoolId());
        then(trainingRunRepository).should().save(trainingRun1);
        then(auditEventService).should().auditTrainingRunStartedAction(trainingRun1);
        then(auditEventService).should().auditLevelStartedAction(trainingRun1);
        assertEquals(sandboxInfo.getId(), trainingRun1.getSandboxInstanceRefId());
    }

    @Test(expected = ForbiddenException.class)
    public void assignSandbox_NoAvailable() throws Exception {
        trainingRun1.setSandboxInstanceRefId(null);
        willThrow(new CustomWebClientException("No sandbox", HttpStatus.CONFLICT)).given(exchangeFunction).exchange(any(ClientRequest.class));
        trainingRunService.assignSandbox(trainingRun1, trainingRun1.getTrainingInstance().getPoolId());
        then(trainingRunRepository).should(never()).save(trainingRun1);
    }

    @Test(expected = MicroserviceApiException.class)
    public void assignSandbox_MicroserviceException() throws Exception {
        trainingRun1.setSandboxInstanceRefId(null);
        willThrow(new CustomWebClientException("Some error", HttpStatus.NOT_FOUND)).given(exchangeFunction).exchange(any(ClientRequest.class));
        trainingRunService.assignSandbox(trainingRun1, trainingRun1.getTrainingInstance().getPoolId());
        then(trainingRunRepository).should(never()).save(trainingRun1);
    }

    @Test
    public void resumeTrainingRun() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        TrainingRun trainingRun = trainingRunService.resumeTrainingRun(trainingRun1.getId());

        assertEquals(trainingRun.getId(), trainingRun1.getId());
        assertTrue(trainingRun.getCurrentLevel() instanceof GameLevel);
    }

    @Test(expected = EntityNotFoundException.class)
    public void resumeTrainingRunWith_NotFound() {
        trainingRun1.setSandboxInstanceRefId(null);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.empty());
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void resumeTrainingRun_Finished() {
        trainingRun1.setState(TRState.FINISHED);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void resumeTrainingRun_TrainingInstanceFinished() {
        trainingRun1.getTrainingInstance().setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(2));
        trainingRun1.getTrainingInstance().setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1));
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void resumeTrainingRun_DeletedSandbox() {
        trainingRun1.setSandboxInstanceRefId(null);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test
    public void evaluateAndStoreResponses() {
        trainingRun1.setCurrentLevel(assessmentLevel);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), responses);

        Assert.assertTrue(trainingRun1.getAssessmentResponses().contains("\"receivedPoints\":13"));
    }

    @Test
    public void isCorrectFlag() {
        int scoreBefore = trainingRun1.getTotalScore();
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), gameLevel.getFlag());
        assertTrue(isCorrect);
        assertEquals(scoreBefore + (trainingRun1.getMaxLevelScore() - trainingRun1.getCurrentPenalty()), trainingRun1.getTotalScore());
        assertTrue(trainingRun1.isLevelAnswered());
    }

    @Test
    public void isCorrectFlag_NotCorrect() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), "wrong flag");
        assertFalse(isCorrect);
        assertFalse(trainingRun1.isLevelAnswered());
    }

    @Test(expected = BadRequestException.class)
    public void isCorrectFlagOfNonGameLevel() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.isCorrectFlag(trainingRun2.getId(), "flag");
    }

    @Test
    public void getRemainingAttempts() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.ofNullable(trainingRun1));
        int attempts = trainingRunService.getRemainingAttempts(trainingRun1.getId());
        assertEquals(gameLevel.getIncorrectFlagLimit() - trainingRun1.getIncorrectFlagCount(), attempts);
    }

    @Test
    public void getSolution() {
        trainingRun1.setTotalScore(40);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String solution = trainingRunService.getSolution(trainingRun1.getId());
        assertEquals(solution, gameLevel.getSolution());
        assertFalse(trainingRun1.isLevelAnswered());
    }

    @Test
    public void getSolution_AlreadyTaken() {
        trainingRun1.setSolutionTaken(true);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String solution = trainingRunService.getSolution(trainingRun1.getId());
        assertEquals(solution, gameLevel.getSolution());
        assertFalse(trainingRun1.isLevelAnswered());
    }

    @Test(expected = BadRequestException.class)
    public void getSolution_NonGameLevel() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.getSolution(trainingRun2.getId());
    }

    @Test
    public void getHint() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(hintRepository.findById(any(Long.class))).willReturn(Optional.of(hint1));
        Hint resultHint1 = trainingRunService.getHint(trainingRun1.getId(), hint1.getId());
        assertEquals(hint1, resultHint1);
        assertEquals(hint1.getHintPenalty(), (Integer) trainingRun1.getCurrentPenalty());
    }

    @Test(expected = BadRequestException.class)
    public void getHint_NonGameLevel() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.getHint(trainingRun2.getId(), hint1.getId());
    }

    @Test
    public void testFinishTrainingRun() {
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun2));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
        trainingRunService.finishTrainingRun(trainingRun2.getId());
        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun2.getParticipantRef().getUserRefId(), trainingRun2.getTrainingInstance().getId());
        assertEquals(trainingRun2.getState(), TRState.FINISHED);
    }

    @Test(expected = EntityConflictException.class)
    public void testFinishTrainingRun_NonLastLevel() {
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
        trainingRunService.finishTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void testFinishTrainingRun_NotAnsweredLevel() {
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.finishTrainingRun(trainingRun1.getId());
    }

    private Mono<ClientResponse> buildMockResponse(Object body) throws IOException {
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .body(convertObjectToJsonBytes(body))
                .header(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return Mono.just(clientResponse);
    }

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    @After
    public void after() {
        reset(trainingRunRepository);
    }
}
