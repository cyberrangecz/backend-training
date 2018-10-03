package cz.muni.ics.kypo.training.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.model.*;
import cz.muni.ics.kypo.training.model.enums.TDState;
import cz.muni.ics.kypo.training.model.enums.TRState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"}, basePackageClasses = Jsr310JpaConverters.class)
public class TrainingRunRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private TrainingRunRepository trainingRunRepository;

	private TrainingRun trainingRun1, trainingRun2;
	private TrainingInstance trainingInstance;
	private SandboxInstanceRef sandboxInstanceRef1, sandboxInstanceRef2;
	private TrainingDefinition trainingDefinition;
	private InfoLevel infoLevel;
	private ParticipantRef participantRef;
	private Pageable pageable;
	private Predicate predicate;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		trainingInstance = new TrainingInstance();
		sandboxInstanceRef1 = new SandboxInstanceRef();
		sandboxInstanceRef1.setSandboxInstanceRef(1L);
		sandboxInstanceRef2 = new SandboxInstanceRef();
		sandboxInstanceRef2.setSandboxInstanceRef(2L);
		trainingDefinition = new TrainingDefinition();
		trainingDefinition.setState(TDState.ARCHIVED);
		trainingDefinition.setTitle("training definition title");
		infoLevel = new InfoLevel();
		infoLevel.setTitle("infoLevel");
		infoLevel.setContent("content for info level");
		participantRef = new ParticipantRef();
		participantRef.setParticipantRefLogin("user");
		trainingInstance.setPasswordHash("b5f3dc27a09865be37cef07816c4f08cf5585b116a4e74b9387c3e43e3a25ec8".toCharArray());
		trainingInstance.setStartTime(LocalDateTime.now());
		trainingInstance.setEndTime(LocalDateTime.now());
		trainingInstance.setTitle("title");
		trainingInstance.setTrainingDefinition(entityManager.persist(trainingDefinition));
		sandboxInstanceRef1.setTrainingInstance(trainingInstance);
		sandboxInstanceRef2.setTrainingInstance(trainingInstance);

		trainingRun1 = new TrainingRun();
		trainingRun1.setStartTime(LocalDateTime.now());
		trainingRun1.setEndTime(LocalDateTime.now());
		trainingRun1.setState(TRState.NEW);
		trainingRun1.setCurrentLevel(entityManager.persist(infoLevel));
		trainingRun1.setParticipantRef(entityManager.persist(participantRef));
		trainingRun1.setTrainingInstance(entityManager.persist(trainingInstance));
		trainingRun1.setSandboxInstanceRef(entityManager.persist(sandboxInstanceRef1));

		trainingRun2 = new TrainingRun();
		trainingRun2.setStartTime(LocalDateTime.now());
		trainingRun2.setEndTime(LocalDateTime.now());
		trainingRun2.setState(TRState.READY);
		trainingRun2.setCurrentLevel(infoLevel);
		trainingRun2.setParticipantRef(participantRef);
		trainingRun2.setTrainingInstance(trainingInstance);
		trainingRun2.setSandboxInstanceRef(entityManager.persist(sandboxInstanceRef2));

		pageable = PageRequest.of(0, 10);
	}

	@Test
	public void findById() throws Exception {
		long expectedId = entityManager.persist(trainingRun1).getId();
		Optional<TrainingRun> optionalTR = trainingRunRepository.findById(expectedId);
		TrainingRun tr = optionalTR.orElseThrow(() -> new Exception("Training run should be found"));
		assertEquals(trainingRun1, tr);
	}

	@Test
	public void findAllByParticipantRefId() throws Exception {
		entityManager.persist(trainingRun1);
		entityManager.persist(trainingRun2);
		List<TrainingRun> trainingRuns = trainingRunRepository.findAllByParticipantRefLogin("user", pageable).getContent();
		assertTrue(trainingRuns.contains(trainingRun1));
		assertTrue(trainingRuns.contains(trainingRun2));
		assertEquals(2, trainingRuns.size());
	}

	@Test
	public void findAllByTrainingDefinitionIdAndParticipantRefId() throws Exception {
		entityManager.persistAndFlush(trainingRun1);
		List<TrainingRun> trainingRuns = trainingRunRepository
				.findAllByTrainingDefinitionIdAndParticipantRefLogin(trainingDefinition.getId(), participantRef.getParticipantRefLogin(), pageable)
				.getContent();
		assertEquals(1, trainingRuns.size());

	}

	@Test
	public void findAllByTrainingInstanceId() throws Exception {
		entityManager.persist(trainingRun1);
		entityManager.persist(trainingRun2);
		List<TrainingRun> trainingRuns = trainingRunRepository.findAllByTrainingInstanceId(trainingInstance.getId(), pageable).getContent();
		assertEquals(2, trainingRuns.size());
		assertTrue(trainingRuns.contains(trainingRun1));
		assertTrue(trainingRuns.contains(trainingRun2));
	}

	@Test
	public void findAllByTrainingDefinitionId() throws Exception {
		entityManager.persist(trainingRun1);
		entityManager.persist(trainingRun2);
		List<TrainingRun> trainingRuns = trainingRunRepository.findAllByTrainingDefinitionId(trainingDefinition.getId(), pageable).getContent();
		assertEquals(2, trainingRuns.size());
		assertTrue(trainingRuns.contains(trainingRun1));
		assertTrue(trainingRuns.contains(trainingRun2));
	}

	@Test
	public void getAllocatedSandboxInstanceRefsOfTrainingInstance() throws Exception {
		entityManager.persist(trainingRun1);
		entityManager.persist(trainingRun2);
		Set<SandboxInstanceRef> sandboxInstanceRefs = trainingRunRepository.findSandboxInstanceRefsOfTrainingInstance(trainingInstance.getId());
		assertEquals(2, sandboxInstanceRefs.size());
	}
}
