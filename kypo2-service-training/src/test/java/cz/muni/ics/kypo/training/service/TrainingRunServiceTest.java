package cz.muni.ics.kypo.training.service;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.responses.PageResultResourcePython;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.enums.SandboxStates;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.impl.AuditEventsService;
import cz.muni.ics.kypo.training.service.impl.SecurityService;
import cz.muni.ics.kypo.training.service.impl.TrainingRunServiceImpl;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.*;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

/**
 * @author Boris Jadus(445343)
 */

@RunWith(SpringRunner.class)
public class TrainingRunServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingRunService trainingRunService;

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
    private RestTemplate restTemplate, pythonRestTemplate;
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
    private PageResultResourcePython sandboxInfoPageResult;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingRunService = new TrainingRunServiceImpl(trainingRunRepository, abstractLevelRepository, trainingInstanceRepository,
                participantRefRepository, hintRepository, auditEventService, securityService, pythonRestTemplate, trAcquisitionLockRepository);
        parser = new JSONParser();
        try {
            questions = parser.parse(new FileReader(ResourceUtils.getFile("classpath:questions.json"))).toString();
            responses = parser.parse(new FileReader(ResourceUtils.getFile("classpath:responses.json"))).toString();
        } catch (IOException | ParseException ex) {
        }


        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setId(1L);
        trainingDefinition.setTitle("Title TrainingDefinition");
        trainingDefinition.setState(TDState.RELEASED);
        trainingDefinition.setAuthors(new HashSet<>());
        trainingDefinition.setSandboxDefinitionRefId(1L);
        trainingDefinition.setShowStepperBar(true);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setTitle("Title2");
        trainingDefinition2.setSandboxDefinitionRefId(1L);

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setAccessToken("keyword-1234");
        trainingInstance2.setTrainingDefinition(trainingDefinition2);

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setStartTime(LocalDateTime.now());
        trainingInstance1.setEndTime(LocalDateTime.now().plusHours(1L));
        trainingInstance1.setTitle("TrainingInstance1");
        trainingInstance1.setPoolSize(5);
        trainingInstance1.setPoolId(1L);
        trainingInstance1.setAccessToken("keyword-5678");
        trainingInstance1.setTrainingDefinition(trainingDefinition);

        participantRef = new UserRef();
        participantRef.setId(1L);
        participantRef.setUserRefId(3L);

        hint1 = new Hint();
        hint1.setId(1L);
        hint1.setContent("hint1 content");
        hint1.setHintPenalty(5);

        gameLevel = new GameLevel();
        gameLevel.setId(1L);
        gameLevel.setSolution("solution");
        gameLevel.setMaxScore(20);
        gameLevel.setContent("content");
        gameLevel.setFlag("flag");
        gameLevel.setHints(new HashSet<>(Arrays.asList(hint1, hint2)));
        gameLevel.setOrder(0);
        gameLevel.setIncorrectFlagLimit(5);
        gameLevel.setTrainingDefinition(trainingDefinition);
        hint1.setGameLevel(gameLevel);

        gameLevel2 = new GameLevel();
        gameLevel2.setId(1L);
        gameLevel2.setSolution("solution");
        gameLevel2.setMaxScore(20);
        gameLevel2.setContent("content");
        gameLevel2.setFlag("flag");
        gameLevel2.setOrder(0);
        gameLevel2.setIncorrectFlagLimit(5);
        gameLevel2.setTrainingDefinition(trainingDefinition2);

        infoLevel = new InfoLevel();
        infoLevel.setId(2L);
        infoLevel.setContent("content");
        infoLevel.setTitle("title");
        infoLevel.setMaxScore(10);
        infoLevel.setOrder(1);
        infoLevel.setTrainingDefinition(trainingDefinition);

        infoLevel2 = new InfoLevel();
        infoLevel2.setId(2L);
        infoLevel2.setContent("content");
        infoLevel2.setTitle("title");
        infoLevel2.setMaxScore(10);
        infoLevel2.setOrder(1);
        infoLevel2.setTrainingDefinition(trainingDefinition2);


        sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(7L);

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.RUNNING);
        trainingRun1.setCurrentLevel(gameLevel);
        trainingRun1.setSandboxInstanceRefId(sandboxInfo.getId());
        trainingRun1.setParticipantRef(participantRef);
        trainingRun1.setTrainingInstance(trainingInstance1);
        trainingRun1.setStartTime(LocalDateTime.of(2019, Month.JANUARY, 3, 1, 1, 1));
        trainingRun1.setEndTime(LocalDateTime.of(2019, Month.JANUARY, 3, 2, 1, 1));
        trainingRun1.setTrainingInstance(trainingInstance1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.RUNNING);
        trainingRun2.setCurrentLevel(infoLevel);
        trainingRun2.setParticipantRef(participantRef);
        trainingRun2.setTrainingInstance(trainingInstance2);
        trainingRun2.setStartTime(LocalDateTime.of(2019, Month.JANUARY, 3, 1, 1, 1));
        trainingRun2.setEndTime(LocalDateTime.of(2019, Month.JANUARY, 3, 2, 1, 1));

        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(3L);
        assessmentLevel.setTitle("Assessment level");
        assessmentLevel.setAssessmentType(AssessmentType.TEST);
        assessmentLevel.setQuestions(questions);

        sandboxInfoPageResult = new PageResultResourcePython();
        sandboxInfoPageResult.setResults(new ArrayList<>(List.of(sandboxInfo)));

    }

    @Test
    public void getTrainingRunById() {
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));

        TrainingRun t = trainingRunService.findById(trainingRun1.getId());
        assertEquals(t.getId(), trainingRun1.getId());
        assertEquals(t.getState(), trainingRun1.getState());

        then(trainingRunRepository).should().findById(trainingRun1.getId());
    }

    @Test
    public void getNonExistTrainingRunById() {
        Long id = 6L;
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Training Run with runId: " + id + " not found.");
        trainingRunService.findById(id);
    }

    @Test
    public void accessTrainingRun() {
        mockSpringSecurityContextForGet();
        UserRef participant = new UserRef();
        participant.setUserRefId(2L);
        sandboxInfo.setLocked(false);
        PageResultResourcePython<SandboxInfo> pythonPage = new PageResultResourcePython<SandboxInfo>();
        pythonPage.setResults(List.of(sandboxInfo));

        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participantRef.getUserRefId());
        given(trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(any(LocalDateTime.class), eq(trainingInstance1.getAccessToken()))).willReturn(Optional.ofNullable(trainingInstance1));
        given(pythonRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(pythonPage, HttpStatus.OK));
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance1.getTrainingDefinition().getId())).willReturn(new ArrayList<>(List.of(gameLevel, infoLevel)));
        given(participantRefRepository.save(participant)).willReturn(participantRef);
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);

        trainingInstance1.setTrainingDefinition(trainingDefinition);
        TrainingRun trainingRun = trainingRunService.accessTrainingRun(trainingInstance1.getAccessToken());
        then(trAcquisitionLockRepository).should().save(any(TRAcquisitionLock.class));
        assertEquals(trainingRun1, trainingRun);
    }

    @Test
    public void accessTrainingRunWithoutAllocatedSandboxes() {
        given(trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(any(LocalDateTime.class), any(String.class))).willReturn(Optional.of(trainingInstance2));

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython>(new PageResultResourcePython<SandboxInfo>(), HttpStatus.OK));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("At first organizer must allocate sandboxes for training instance.");
        trainingRunService.accessTrainingRun("pass");
    }

    @Test
    public void accessTrainingRunWithoutStartingLevel() {
        sandboxInfo.setLocked(false);
        PageResultResourcePython<SandboxInfo> pythonPage = new PageResultResourcePython<SandboxInfo>();
        pythonPage.setResults(List.of(sandboxInfo));
        given(trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(any(LocalDateTime.class), any(String.class))).willReturn(Optional.of(trainingInstance1));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(pythonPage, HttpStatus.OK));
        given(abstractLevelRepository.findById(anyLong())).willReturn(Optional.empty());
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("No starting level available for this training definition");
        trainingRunService.accessTrainingRun("pass");
        then(trAcquisitionLockRepository).should().save(new TRAcquisitionLock(1L, trainingInstance1.getId(), any(LocalDateTime.class)));
    }

    private void mockSpringSecurityContextForGet() {
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), "participant");
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2Authentication auth = Mockito.mock(OAuth2Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(auth);
        given(auth.getUserAuthentication()).willReturn(auth);
        given(auth.getCredentials()).willReturn(sub);
        given(authentication.getDetails()).willReturn(auth);
    }

    @Test
    public void isCorrectFlag() {
        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag");
        assertTrue(isCorrect);
        assertTrue(trainingRun1.isLevelAnswered());
    }

    @Test
    public void isCorrectFlagNotCorrect() {
        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), "wrong flag");
        assertFalse(isCorrect);
        assertFalse(trainingRun1.isLevelAnswered());
    }

    @Test
    public void isCorrectFlagOfNonGameLevel() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Current level is not game level and does not have flag.");
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.isCorrectFlag(trainingRun2.getId(), "flag");
    }

    @Test
    public void getSolution() {
        mockSpringSecurityContextForGet();
        trainingRun1.setTotalScore(40);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String solution = trainingRunService.getSolution(trainingRun1.getId());
        assertEquals(solution, gameLevel.getSolution());
        assertFalse(trainingRun1.isLevelAnswered());
    }

    @Test
    public void getAlreadyTakenSolution() {
        mockSpringSecurityContextForGet();
        trainingRun1.setSolutionTaken(true);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String solution = trainingRunService.getSolution(trainingRun1.getId());
        assertEquals(solution, gameLevel.getSolution());
        assertFalse(trainingRun1.isLevelAnswered());
    }

    @Test
    public void getSolutionOfNonGameLevel() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Current level is not game level and does not have solution.");
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.getSolution(trainingRun2.getId());
    }

    @Test
    public void getHint() {
        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(hintRepository.findById(any(Long.class))).willReturn(Optional.of(hint1));
        Hint resultHint1 = trainingRunService.getHint(trainingRun1.getId(), hint1.getId());
        assertEquals(hint1, resultHint1);
        assertEquals(hint1.getHintPenalty(),(Integer) trainingRun1.getCurrentPenalty());
    }

    @Test
    public void getRemainingAttempts() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.ofNullable(trainingRun1));
        int attempts = trainingRunService.getRemainingAttempts(trainingRun1.getId());
        assertEquals(5, attempts);
    }

    public void getHintOfNonGameLevel() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Current level is not game level and does not have hints.");
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.getHint(trainingRun2.getId(), hint1.getId());
    }

    @Test
    public void findAll() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page p = new PageImpl<>(expected);
        PathBuilder<TrainingRun> t = new PathBuilder<>(TrainingRun.class, "trainingRun");
        Predicate predicate = t.isNotNull();

        given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingRunService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findAll_empty() {
        Page p = new PageImpl<>(new ArrayList<>());
        PathBuilder<TrainingRun> t = new PathBuilder<>(TrainingRun.class, "trainingRun");
        Predicate predicate = t.isNotNull();

        given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingRunRepository.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(0, pr.getTotalElements());
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
    public void findByIdWithLevel() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));

        Optional<TrainingRun> optionalTrainingRun = trainingRunRepository.findByIdWithLevel(trainingRun1.getId());

        assertTrue(optionalTrainingRun.isPresent());
        assertTrue(optionalTrainingRun.get().getCurrentLevel() instanceof GameLevel);
    }

    @Test
    public void getNextLevel() {
        mockSpringSecurityContextForGet();
        List<AbstractLevel> levels = new ArrayList<>();
        levels.add(infoLevel);
        levels.add(gameLevel);

        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(any(Long.class))).willReturn(levels);
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
        AbstractLevel resultAbstractLevel = trainingRunService.getNextLevel(trainingRun1.getId());

        assertEquals(trainingRun1.getCurrentLevel().getId(), resultAbstractLevel.getId());
        assertEquals(trainingRun1.getMaxLevelScore(), infoLevel.getMaxScore());
        assertTrue(trainingRun1.isLevelAnswered()); // because next level is info and it is always set to true

        then(trainingRunRepository).should().findByIdWithLevel(trainingRun1.getId());
        then(trainingRunRepository).should().save(trainingRun1);
    }

    @Test
    public void getNextLevelNotAnswered() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("At first you need to answer the level.");
        trainingRunService.getNextLevel(trainingRun1.getId());
    }

    @Test
    public void getNextLevel_noNextLevel() {
        trainingRun2.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun2));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("There is no next level.");
        trainingRunService.getNextLevel(trainingRun2.getId());
    }

    @Test
    public void testFinishTrainingRun() {
        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun2));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
        trainingRunService.finishTrainingRun(trainingRun2.getId());
        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun2.getParticipantRef().getUserRefId(), trainingRun2.getTrainingInstance().getId());
        assertEquals(trainingRun2.getState(), TRState.FINISHED);
    }

    @Test
    public void testFinishTrainingRunWithNullTrainingRunId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training run id must not be null.");
        trainingRunService.finishTrainingRun(null);
    }

    @Test
    public void testFinishTrainingRunWithNonLastLevel() {
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
        thrown.expect(ServiceLayerException.class);

        trainingRunService.finishTrainingRun(trainingRun1.getId());
    }

    @Test
    public void testFinishTrainingRunWithNotAnsweredLevel() {
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun1));
        thrown.expect(ServiceLayerException.class);

        trainingRunService.finishTrainingRun(trainingRun1.getId());
    }

    @Test
    public void resumeTrainingRun() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<SandboxInfo>((sandboxInfo), HttpStatus.OK));
        TrainingRun trainingRun = trainingRunService.resumeTrainingRun(trainingRun1.getId());

        assertEquals(trainingRun.getId(), trainingRun1.getId());
        assertTrue(trainingRun.getCurrentLevel() instanceof GameLevel);
    }

    @Test
    public void resumeTrainingRunWithDeletedSandbox() {
        trainingRun1.setSandboxInstanceRefId(null);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Sandbox of this training run was already deleted, you have to start new game.");

        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test
    public void resumeFinishedTrainingRun() {
        trainingRun1.setState(TRState.FINISHED);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot resume finished training run.");

        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test
    public void evaluateAndStoreResponses() {
        mockSpringSecurityContextForGet();
        trainingRun1.setCurrentLevel(assessmentLevel);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), responses);

        Assert.assertTrue(trainingRun1.getAssessmentResponses().contains("\"receivedPoints\":13"));

    }


    @After
    public void after() {
        reset(trainingRunRepository);
    }


}
