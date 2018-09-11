package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.csirt.kypo.elasticsearch.service.audit.AuditService;
import cz.muni.ics.kypo.training.config.ServiceTrainingConfigTest;
import cz.muni.ics.kypo.training.exceptions.NoAvailableSandboxException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.*;
import cz.muni.ics.kypo.training.model.enums.TRState;
import cz.muni.ics.kypo.training.repository.AbstractLevelRepository;
import cz.muni.ics.kypo.training.repository.ParticipantRefRepository;
import cz.muni.ics.kypo.training.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.rules.ExpectedException;
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
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import com.google.gson.JsonObject;

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
		trainingInstance.setPassword("Password".toCharArray());
		trainingInstance.setTrainingDefinition(trainingDefinition);

		participantRef = new ParticipantRef();
		participantRef.setId(1L);
		participantRef.setParticipantRefLogin("participant");

		hint1 = new Hint();
		hint1.setId(1L);
		hint1.setContent("hint1 content");

		hint2 = new Hint();
		hint2.setId(2L);
		hint2.setContent("hint2 content");

		gameLevel = new GameLevel();
		gameLevel.setId(1L);
		gameLevel.setSolution("solution");
		gameLevel.setMaxScore(20);
		gameLevel.setContent("content");
		gameLevel.setFlag("flag");
		gameLevel.setHints(new HashSet<>(Arrays.asList(hint1, hint2)));
		gameLevel.setNextLevel(2L);

		infoLevel = new InfoLevel();
		infoLevel.setId(2L);
		infoLevel.setContent("content");
		infoLevel.setTitle("title");
		infoLevel.setNextLevel(5L);

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
		thrown.expectMessage("Training Run with id " + id + " not found.");
		assertEquals(Optional.empty(), trainingRunService.findById(id));
	}

	@Test
	public void getLevels() {
		given(abstractLevelRepository.findById(1L)).willReturn(Optional.of(gameLevel));
		given(abstractLevelRepository.findById(2L)).willReturn(Optional.of(infoLevel));

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
		thrown.expect(NoAvailableSandboxException.class);
		thrown.expectMessage("There is no available sandbox, wait a minute and try again.");
		mockSpringSecurityContextForGet();
		given(trainingInstanceRepository.findAll()).willReturn(Arrays.asList(trainingInstance));
		given(trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(trainingInstance.getId()))
				.willReturn(new HashSet<>(Arrays.asList(sandboxInstanceRef1, sandboxInstanceRef2)));
		given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
		trainingRunService.accessTrainingRun("Password");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void accessTrainingRun() {
		mockSpringSecurityContextForGet();
		given(trainingInstanceRepository.findAll()).willReturn(Arrays.asList(trainingInstance));
		given(trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(trainingInstance.getId())).willReturn(new HashSet<>());
		given(abstractLevelRepository.findById(gameLevel.getId())).willReturn(Optional.of(gameLevel));
		given(participantRefRepository.findByParticipantRefLogin("participant")).willReturn(Optional.of(participantRef));
		given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class), anyString()))
				.willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(Arrays.asList(sandboxInfo)), HttpStatus.OK));
		given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
		AbstractLevel level = trainingRunService.accessTrainingRun("Password");
		assertEquals(gameLevel, level);
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
		given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
		Boolean isCorrect = trainingRunService.isCorrectFlag(trainingRun1.getId(), "flag");
		assertTrue(isCorrect);
	}

	@Test
	public void isCorrectFlagOfNonGameLevel() {
		thrown.expect(ServiceLayerException.class);
		thrown.expectMessage("Current level is not game level and does not have flag.");
		given(trainingRunRepository.findById(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
		trainingRunService.isCorrectFlag(trainingRun2.getId(), "flag");
	}

	@Test
	public void getSolution() {
		given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
		String solution = trainingRunService.getSolution(trainingRun1.getId());
		assertEquals(solution, gameLevel.getSolution());
	}

	@Test
	public void getSolutionOfNonGameLevel() {
		thrown.expect(ServiceLayerException.class);
		thrown.expectMessage("Current level is not game level and does not have solution.");
		given(trainingRunRepository.findById(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
		trainingRunService.getSolution(trainingRun2.getId());
	}

	@Test
	public void getHint() {
		given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
		Hint resultHint1 = trainingRunService.getHint(trainingRun1.getId(), 1L);
		Hint resultHint2 = trainingRunService.getHint(trainingRun1.getId(), 2L);
		assertEquals(hint1, resultHint1);
		assertEquals(hint2, resultHint2);

	}

	@Test
	public void getHintOfNonGameLevel() {
		thrown.expect(ServiceLayerException.class);
		thrown.expectMessage("Current level is not game level and does not contain hints.");
		given(trainingRunRepository.findById(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
		trainingRunService.getHint(trainingRun2.getId(), hint1.getId());

	}

	@Test
	public void findAll() {
		List<TrainingRun> expected = new ArrayList<>();
		expected.add(trainingRun1);
		expected.add(trainingRun2);

		Page<TrainingRun> p = new PageImpl<TrainingRun>(expected);
		PathBuilder<TrainingRun> t = new PathBuilder<TrainingRun>(TrainingRun.class, "trainingRun");
		Predicate predicate = t.isNotNull();

		given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

		Page<TrainingRun> pr = trainingRunService.findAll(predicate, PageRequest.of(0, 2));
		assertEquals(2, pr.getTotalElements());
	}

	@After
	public void after() {
		reset(trainingRunRepository);
	}

}
