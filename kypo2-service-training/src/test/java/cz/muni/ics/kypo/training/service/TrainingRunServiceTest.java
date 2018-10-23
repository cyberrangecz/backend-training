package cz.muni.ics.kypo.training.service;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.ics.kypo.training.config.ServiceTrainingConfigTest;

import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.utils.SandboxInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
import org.springframework.web.client.RestTemplate;


import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(ServiceTrainingConfigTest.class)
public class TrainingRunServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TrainingRunService trainingRunService;

    @MockBean
    private TrainingRunRepository trainingRunRepository;
    @MockBean
    private AuditService auditService;
    @MockBean
    private AbstractLevelRepository abstractLevelRepository;
    @MockBean
    private TrainingInstanceRepository trainingInstanceRepository;
    @MockBean
    private ParticipantRefRepository participantRefRepository;
    @MockBean
    private HintRepository hintRepository;
    @MockBean
    private RestTemplate restTemplate;

    private TrainingRun trainingRun1, trainingRun2;
    private GameLevel gameLevel;
    private InfoLevel infoLevel;
    private Hint hint1, hint2;
    private SandboxInstanceRef sandboxInstanceRef1, sandboxInstanceRef2;
    private TrainingInstance trainingInstance;
    private ParticipantRef participantRef;
    private SandboxInfo sandboxInfo;
    private TrainingDefinition trainingDefinition;


    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setId(1L);
        trainingDefinition.setTitle("Title");
        trainingDefinition.setStartingLevel(1L);

        sandboxInstanceRef1 = new SandboxInstanceRef();
        sandboxInstanceRef1.setId(1L);
        sandboxInstanceRef1.setSandboxInstanceRef(7L);

        sandboxInstanceRef2 = new SandboxInstanceRef();
        sandboxInstanceRef2.setId(2L);
        sandboxInstanceRef2.setSandboxInstanceRef(5L);

        trainingInstance = new TrainingInstance();
        trainingInstance.setId(1L);
        trainingInstance.setSandboxInstanceRefs(new HashSet<>(Arrays.asList(sandboxInstanceRef1, sandboxInstanceRef2)));
        trainingInstance.setPasswordHash("b5f3dc27a09865be37cef07816c4f08cf5585b116a4e74b9387c3e43e3a25ec8");
        trainingInstance.setTrainingDefinition(trainingDefinition);

        participantRef = new ParticipantRef();
        participantRef.setId(1L);
        participantRef.setParticipantRefLogin("participant");

        hint1 = new Hint();
        hint1.setId(1L);
        hint1.setContent("hint1 content");

        gameLevel = new GameLevel();
        gameLevel.setId(1L);
        gameLevel.setSolution("solution");
        gameLevel.setMaxScore(20);
        gameLevel.setContent("content");
        gameLevel.setFlag("flag");
        gameLevel.setHints(new HashSet<>(Arrays.asList(hint1, hint2)));
        gameLevel.setNextLevel(2L);
        gameLevel.setIncorrectFlagLimit(5);
        hint1.setGameLevel(gameLevel);

        infoLevel = new InfoLevel();
        infoLevel.setId(2L);
        infoLevel.setContent("content");
        infoLevel.setTitle("title");

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.NEW);
        trainingRun1.setCurrentLevel(gameLevel);
        trainingRun1.setSandboxInstanceRef(sandboxInstanceRef1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.ARCHIVED);
        trainingRun2.setCurrentLevel(infoLevel);

        sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(7L);
        sandboxInfo.setState("READY");


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
        given(trainingInstanceRepository.findAll()).willReturn(Arrays.asList(trainingInstance));
        trainingRunService.accessTrainingRun("wrong");

    }

    @Test
    public void accessTrainingRunWithNoAvailableSandbox() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("There is no available sandbox, wait a minute and try again.");
        mockSpringSecurityContextForGet();
        given(trainingInstanceRepository.findAll()).willReturn(Arrays.asList(trainingInstance));
        given(trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(trainingInstance.getId())).willReturn(new HashSet<>(Arrays.asList(sandboxInstanceRef1, sandboxInstanceRef2)));
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
        trainingRunService.accessTrainingRun("b5f3dc27a09865be37cef07816c4f08cf5585b116a4e74b9387c3e43e3a25ec8");
    }

    //TODO Boris Fix that test
//    @Test
//    public void accessTrainingRun() {
//        mockSpringSecurityContextForGet();
//        given(trainingInstanceRepository.findAll()).willReturn(Arrays.asList(trainingInstance));
//        given(trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(trainingInstance.getId())).willReturn(new HashSet<>());
//        given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
//        given(participantRefRepository.findByParticipantRefLogin("participant")).willReturn(Optional.of(participantRef));
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class), anyString())).willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(Arrays.asList(sandboxInfo)), HttpStatus.OK));
//        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
//        AbstractLevel level = trainingRunService.accessTrainingRun("b5f3dc27a09865be37cef07816c4f08cf5585b116a4e74b9387c3e43e3a25ec8");
//        assertEquals(gameLevel, level);
//    }


    private void mockSpringSecurityContextForGet() {
        JsonObject sub = new JsonObject();
        sub.addProperty("sub","participant" );
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
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        Boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag");
        assertTrue(isCorrect);
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
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String solution = trainingRunService.getSolution(trainingRun1.getId());
        assertEquals(solution, gameLevel.getSolution());
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
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        given(hintRepository.findById(1L)).willReturn(Optional.ofNullable(hint1));
        Hint resultHint1 = trainingRunService.getHint(trainingRun1.getId(), 1L);
        assertEquals(hint1,resultHint1);

    }

		@Test
		public void getRemainingAttempts() {
				given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.ofNullable(trainingRun1));
				int attempts = trainingRunService.getRemainingAttempts(trainingRun1.getId());
				assertEquals(5, attempts);
		}


		@Test
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

        Page pr = trainingRunService.findAll(predicate, PageRequest.of(0,2));
        assertEquals(2, pr.getTotalElements());
    }

    @After
    public void after() {
        reset(trainingRunRepository);
    }


}
