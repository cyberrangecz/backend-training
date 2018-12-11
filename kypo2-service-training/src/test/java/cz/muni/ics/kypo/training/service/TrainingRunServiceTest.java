package cz.muni.ics.kypo.training.service;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.utils.SandboxInfo;
import cz.muni.ics.kypo.training.service.impl.TrainingRunServiceImpl;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;
import org.springframework.http.*;

@RunWith(SpringRunner.class)
public class TrainingRunServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingRunService trainingRunService;

    @Mock
    private TrainingRunRepository trainingRunRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private AbstractLevelRepository abstractLevelRepository;
    @Mock
    private TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Mock
    private ParticipantRefRepository participantRefRepository;
    @Mock
    private HintRepository hintRepository;
    @Mock
    private RestTemplate restTemplate;

    private TrainingRun trainingRun1, trainingRun2;
    private GameLevel gameLevel;
    private AssessmentLevel assessmentLevel;
    private InfoLevel infoLevel;
    private Hint hint1, hint2;
    private SandboxInstanceRef sandboxInstanceRef1, sandboxInstanceRef2;
    private SandboxDefinitionRef sandBoxDefinitionRef;
    private TrainingInstance trainingInstance1, trainingInstance2;
    private ParticipantRef participantRef;
    private SandboxInfo sandboxInfo;
    private TrainingDefinition trainingDefinition, trainingDefinition2;
    private JSONParser parser = new JSONParser();
    private String responses, questions;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingRunService = new TrainingRunServiceImpl(trainingRunRepository, abstractLevelRepository, trainingInstanceRepository,
            participantRefRepository, restTemplate, hintRepository, auditService);

        parser = new JSONParser();
        try {
            questions = parser.parse(new FileReader(ResourceUtils.getFile("classpath:questions.json"))).toString();
            responses = parser.parse(new FileReader(ResourceUtils.getFile("classpath:responses.json"))).toString();
        } catch (IOException | ParseException ex) {
        }

        sandBoxDefinitionRef = new SandboxDefinitionRef();
        sandBoxDefinitionRef.setId(1L);

        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setId(1L);
        trainingDefinition.setTitle("Title TrainingDefinition");
        trainingDefinition.setStartingLevel(1L);
        trainingDefinition.setState(TDState.RELEASED);
        trainingDefinition.setAuthorRef(new HashSet<>());
        trainingDefinition.setSandBoxDefinitionRef(sandBoxDefinitionRef);
        trainingDefinition.setStartingLevel(1L);
        trainingDefinition.setShowStepperBar(true);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setId(2L);
        trainingDefinition2.setTitle("Title2");
        trainingDefinition2.setStartingLevel(2L);
        trainingDefinition2.setSandBoxDefinitionRef(sandBoxDefinitionRef);

        sandboxInstanceRef1 = new SandboxInstanceRef();
        sandboxInstanceRef1.setId(1L);
        sandboxInstanceRef1.setSandboxInstanceRef(7L);

        sandboxInstanceRef2 = new SandboxInstanceRef();
        sandboxInstanceRef2.setId(2L);
        sandboxInstanceRef2.setSandboxInstanceRef(5L);

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setSandboxInstanceRefs(new HashSet<>(Arrays.asList(sandboxInstanceRef2)));
        trainingInstance2.setPassword("$5asdMmkskdm@365csadSD4fdF45fsdFSC54dw48WD7v7WD4fe254WEF54wd");
        trainingInstance2.setTrainingDefinition(trainingDefinition2);

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setStartTime(LocalDateTime.now());
        trainingInstance1.setEndTime(LocalDateTime.now().plusHours(1L));
        trainingInstance1.setTitle("TrainingInstance1");
        trainingInstance1.setPoolSize(5);
        trainingInstance1.setPassword("$2a$12$rhWNRfPDgxBX1Zv2/jg8DOWT97MIPVKPpXyjGcaCtEhQ0Z36H8y1y");
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        trainingInstance1.setSandboxInstanceRefs(new HashSet<>(Arrays.asList(sandboxInstanceRef1, sandboxInstanceRef2)));

        participantRef = new ParticipantRef();
        participantRef.setId(1L);
        participantRef.setParticipantRefLogin("participant");


        gameLevel = new GameLevel();
        gameLevel.setId(1L);
        gameLevel.setSolution("solution");
        gameLevel.setMaxScore(20);
        gameLevel.setContent("content");
        gameLevel.setFlag("flag");
        gameLevel.setHints(new HashSet<>(Arrays.asList(hint1, hint2)));
        gameLevel.setNextLevel(2L);
        gameLevel.setIncorrectFlagLimit(5);

        hint1 = new Hint();
        hint1.setId(1L);
        hint1.setContent("hint1 content");
        hint1.setGameLevel(gameLevel);
        hint1.setHintPenalty(5);

        infoLevel = new InfoLevel();
        infoLevel.setId(2L);
        infoLevel.setContent("content");
        infoLevel.setTitle("title");
        infoLevel.setMaxScore(10);

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.NEW);
        trainingRun1.setCurrentLevel(gameLevel);
        trainingRun1.setSandboxInstanceRef(sandboxInstanceRef1);
        trainingRun1.setParticipantRef(participantRef);
        trainingRun1.setTrainingInstance(trainingInstance1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.READY);
        trainingRun2.setCurrentLevel(infoLevel);
        trainingRun2.setParticipantRef(participantRef);
        trainingRun2.setTrainingInstance(trainingInstance2);

        sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(7L);
        sandboxInfo.setState("READY");

        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setId(3L);
        assessmentLevel.setTitle("Assessment level");
        assessmentLevel.setAssessmentType(AssessmentType.TEST);
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

    @Test
    public void getNonExistTrainingRunById() {
        Long id = 6L;
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Training Run with id: " + id + " not found.");
        trainingRunService.findById(id);
    }

    @Test
    public void getLevels() {
        given(abstractLevelRepository.findById(1L)).willReturn(Optional.of(gameLevel));
        given(abstractLevelRepository.findById(2L)).willReturn(Optional.of(infoLevel));

        List<AbstractLevel> levels = trainingRunService.getLevels(1L);
        assertEquals(2, levels.size());
        assertEquals(gameLevel, levels.get(0));
        assertEquals(infoLevel, levels.get(1));

    }

    @Test
    public void getNonExistingLevels() {
        given(abstractLevelRepository.findById(1L)).willReturn(Optional.empty());
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Level with id: " + 1L + " not found.");
        trainingRunService.getLevels(1L);
    }

    @Test
    public void accessTrainingRunWithWrongPassword() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("There is no training instance with password wrong.");
        given(trainingInstanceRepository.findAll()).willReturn(Arrays.asList(trainingInstance1));
        trainingRunService.accessTrainingRun("wrong");

    }

    @Test
    public void accessTrainingRun() {
        mockSpringSecurityContextForGet();
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        given(trainingDefinitionRepository.save(any(TrainingDefinition.class))).willReturn(trainingDefinition);
        given(trainingInstanceRepository.findAll()).willReturn(Arrays.asList(trainingInstance1));
        given(trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(trainingInstance1.getId())).willReturn(new HashSet<>());
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class), anyString())).willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(Arrays.asList(sandboxInfo)), HttpStatus.OK));
        given(abstractLevelRepository.findById(trainingInstance1.getTrainingDefinition().getStartingLevel())).willReturn(Optional.of(gameLevel));
        given(participantRefRepository.findByParticipantRefLogin(participantRef.getParticipantRefLogin())).willReturn(Optional.of(participantRef));
        given(participantRefRepository.save(new ParticipantRef(participantRef.getParticipantRefLogin()))).willReturn(participantRef);
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
        TrainingRun trainingRun = trainingRunService.accessTrainingRun(trainingInstance1.getPassword());
        assertEquals(trainingRun1, trainingRun);
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
        given(authentication.getDetails()).willReturn(auth);
    }

    @Test
    public void isCorrectFlag() {
        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        Boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag");
        assertTrue(isCorrect);
        assertTrue(trainingRun1.isLevelAnswered());
    }

    @Test
    public void isCorrectFlagNotCorrect() {
        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        Boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), "wrong flag");
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
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String solution = trainingRunService.getSolution(trainingRun1.getId());
        assertEquals(solution, gameLevel.getSolution());
        assertEquals(1, trainingRun1.getTotalScore());
        assertEquals(1, trainingRun1.getCurrentScore());
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
        assertEquals(hint1,resultHint1);
        assertEquals(gameLevel.getMaxScore() - hint1.getHintPenalty(), trainingRun1.getCurrentScore());
        assertEquals(gameLevel.getMaxScore() - hint1.getHintPenalty(), trainingRun1.getTotalScore());

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
    public void getLevelOrder() {
        given(abstractLevelRepository.findById(1L)).willReturn(Optional.ofNullable(gameLevel));
        given(abstractLevelRepository.findById(2L)).willReturn(Optional.ofNullable(infoLevel));

        int order = trainingRunService.getLevelOrder(1L, 2L);
        assertEquals(1, order);
    }

    @Test
    public void getLevelOrderWrongFirstLevel() {
        given(abstractLevelRepository.findById(1L)).willReturn(Optional.ofNullable(null));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Level with id " + 1L + " not found.");
        trainingRunService.getLevelOrder(1L, 2L);
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
        assertEquals(0,pr.getTotalElements());
    }

    @Test
    public void findAllByParticipantRefLogin() {
        Page<TrainingRun> expectedPage = new PageImpl<>(Arrays.asList(trainingRun1, trainingRun2));

        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findAllByParticipantRefLogin(any(String.class), any(PageRequest.class))).willReturn(expectedPage);

        Page<TrainingRun> resultPage = trainingRunService.findAllByParticipantRefLogin(PageRequest.of(0, 2));

        assertEquals(expectedPage, resultPage);

        then(trainingRunRepository).should().findAllByParticipantRefLogin(participantRef.getParticipantRefLogin(), PageRequest.of(0 ,2));
    }

    @Test
    public void findAllByTrainingDefinitionAndParticipant() {
        Page<TrainingRun> expectedPage = new PageImpl<>(Arrays.asList(trainingRun2));
        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantRefLogin(any(Long.class), any(String.class), any(Pageable.class))).willReturn(expectedPage);

        Page<TrainingRun> resultPage = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinition2.getId(), PageRequest.of(0, 2));

        assertEquals(expectedPage, resultPage);

        then(trainingRunRepository).should().findAllByTrainingDefinitionIdAndParticipantRefLogin(trainingDefinition2.getId(), participantRef.getParticipantRefLogin(), PageRequest.of(0, 2));
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

//    @Test
//    public void createTrainingRun() {
//        given(trainingRunRepository.save(trainingRun1)).willReturn(trainingRun1);
//        TrainingRun tI = trainingRunService.create(trainingRun1);
//        assertEquals(trainingRun1.getId(), tI.getId());
//        then(trainingRunRepository).should().save(trainingRun1);
//    }

    @Test
    public void getNextLevel() {
        mockSpringSecurityContextForGet();
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(abstractLevelRepository.findById(any(Long.class))).willReturn(Optional.of(infoLevel));
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
        AbstractLevel resultAbstractLevel = trainingRunService.getNextLevel(trainingRun1.getId());

        assertEquals(trainingRun1.getCurrentLevel().getId(), resultAbstractLevel.getId());
        assertEquals(trainingRun1.getCurrentScore(), infoLevel.getMaxScore());
        assertEquals(gameLevel.getMaxScore() + infoLevel.getMaxScore(), trainingRun1.getTotalScore());
        assertTrue(trainingRun1.isLevelAnswered()); // because next level is info and it is always set to true

        then(trainingRunRepository).should().findByIdWithLevel(trainingRun1.getId());
        then(abstractLevelRepository).should().findById(trainingRun1.getCurrentLevel().getId());
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
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("There is no next level.");
        trainingRunService.getNextLevel(trainingRun2.getId());
    }

    @Test
    public void testArchiveTrainingRun() {
        mockSpringSecurityContextForGet();
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun2));
        trainingRunService.archiveTrainingRun(trainingRun2.getId());
        assertEquals(trainingRun2.getState(), TRState.ARCHIVED);
    }

    @Test
    public void testArchiveTrainingRunWithNullTrainingRunId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training run id must not be null.");
        trainingRunService.archiveTrainingRun(null);
    }

    @Test
    public void testArchiveTrainingRunWithNonLastLevel() {
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot archive training run because current level is not last or is not answered.");

        trainingRunService.archiveTrainingRun(trainingRun1.getId());
    }

    @Test
    public void testArchiveTrainingRunWithNotAnsweredLevel() {
        given(trainingRunRepository.findById(any(Long.class))).willReturn(Optional.of(trainingRun1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot archive training run because current level is not last or is not answered.");

        trainingRunService.archiveTrainingRun(trainingRun1.getId());
    }

    @Test
    public void resumeTrainingRun() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        AbstractLevel level = trainingRunService.resumeTrainingRun(trainingRun1.getId());

        assertTrue(level.getId().equals(1L));
        assertTrue(level instanceof GameLevel);
    }

    @Test
    public void resumeArchivedTrainingRun() {
        trainingRun1.setState(TRState.ARCHIVED);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot resumed archived training run.");

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
