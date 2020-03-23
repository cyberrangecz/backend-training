package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.responses.PageResultResourcePython;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.exceptions.BadRequestException;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class TrainingRunServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

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
        trainingRunService = new TrainingRunService(trainingRunRepository, abstractLevelRepository, trainingInstanceRepository,
                participantRefRepository, hintRepository, auditEventService, securityService, pythonRestTemplate, trAcquisitionLockRepository);
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

    @Test(expected = EntityNotFoundException.class)
    public void getNonExistTrainingRunById() {
        Long id = 6L;
        trainingRunService.findById(id);
    }

    /*
    @Test
    public void accessTrainingRun() throws Exception{
        mockSpringSecurityContextForGet();
        UserRef participant = new UserRef();
        participant.setUserRefId(2L);
        sandboxInfo.setLocked(false);
        PageResultResourcePython<SandboxInfo> pythonPage = new PageResultResourcePython<SandboxInfo>();
        pythonPage.setResults(List.of(sandboxInfo));

        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participantRef.getUserRefId());
        given(trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(any(LocalDateTime.class), eq(trainingInstance1.getAccessToken()))).willReturn(Optional.ofNullable(trainingInstance1));
        given(pythonRestTemplate.getForEntity(anyString(), any())).
                willReturn(new ResponseEntity<>(sandboxInfo, HttpStatus.OK));
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingInstance1.getTrainingDefinition().getId())).willReturn(new ArrayList<>(List.of(gameLevel, infoLevel)));
        given(participantRefRepository.save(participant)).willReturn(participantRef);
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);

        trainingInstance1.setTrainingDefinition(trainingDefinition);
        TrainingRun trainingRun = trainingRunService.accessTrainingRun(trainingInstance1.getAccessToken());
        then(trAcquisitionLockRepository).should().save(any(TRAcquisitionLock.class));
        assertEquals(trainingRun1, trainingRun);
    }

    @Test(expected = EntityConflictException.class)
    public void accessTrainingRunWithoutAllocatedSandboxes() {
        trainingInstance2.setPoolId(null);
        given(trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(any(LocalDateTime.class), any(String.class))).willReturn(Optional.of(trainingInstance2));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython>(new PageResultResourcePython<SandboxInfo>(), HttpStatus.OK));
        trainingRunService.accessTrainingRun("pass");
    }

    @Test(expected = EntityNotFoundException.class)
    public void accessTrainingRunWithoutStartingLevel() {
        sandboxInfo.setLocked(false);
        PageResultResourcePython<SandboxInfo> pythonPage = new PageResultResourcePython<SandboxInfo>();
        pythonPage.setResults(List.of(sandboxInfo));
        given(trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(any(LocalDateTime.class), any(String.class))).willReturn(Optional.of(trainingInstance1));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
                willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(pythonPage, HttpStatus.OK));
        given(abstractLevelRepository.findById(anyLong())).willReturn(Optional.empty());
        trainingRunService.accessTrainingRun("pass");
        then(trAcquisitionLockRepository).should().save(new TRAcquisitionLock(1L, trainingInstance1.getId(), any(LocalDateTime.class)));
    }
    */

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
        boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), gameLevel.getFlag());
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

    @Test(expected = BadRequestException.class)
    public void isCorrectFlagOfNonGameLevel() {
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

    @Test(expected = BadRequestException.class)
    public void getSolutionOfNonGameLevel() {
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
        assertEquals(hint1.getHintPenalty(), (Integer) trainingRun1.getCurrentPenalty());
    }

    @Test
    public void getRemainingAttempts() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.ofNullable(trainingRun1));
        int attempts = trainingRunService.getRemainingAttempts(trainingRun1.getId());
        assertEquals(gameLevel.getIncorrectFlagLimit() - trainingRun1.getIncorrectFlagCount(), attempts);
    }

    @Test(expected = BadRequestException.class)
    public void getHintOfNonGameLevel() {
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
        levels.add(gameLevel);
        levels.add(infoLevel);

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

    @Test(expected = EntityConflictException.class)
    public void getNextLevelNotAnswered() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.getNextLevel(trainingRun1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNextLevel_noNextLevel() {
        trainingRun2.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun2));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
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

    @Test(expected = EntityConflictException.class)
    public void testFinishTrainingRunWithNonLastLevel() {
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
        trainingRunService.finishTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void testFinishTrainingRunWithNotAnsweredLevel() {
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun1));
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

    @Test(expected = EntityConflictException.class)
    public void resumeTrainingRunWithDeletedSandbox() {
        trainingRun1.setSandboxInstanceRefId(null);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void resumeFinishedTrainingRun() {
        trainingRun1.setState(TRState.FINISHED);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
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

    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    @After
    public void after() {
        reset(trainingRunRepository);
    }


}
