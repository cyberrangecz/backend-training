package cz.muni.ics.kypo.training.model;

import cz.muni.ics.kypo.training.model.enums.TRState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class TrainingRunEntityTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private TestEntityManager entityManager;

	private TrainingRun trainingRun1, trainingRun2;
	private TrainingInstance trainingInstance;
	private SandboxInstanceRef sandboxInstanceRef;
	private TrainingDefinition trainingDefinition;
	private InfoLevel infoLevel;
	private ParticipantRef participantRef;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		trainingInstance = new TrainingInstance();
		sandboxInstanceRef = new SandboxInstanceRef();
		trainingDefinition = new TrainingDefinition();
		infoLevel = new InfoLevel();
		infoLevel.setTitle("infoLevel");
		infoLevel.setContent("content for info level");
		participantRef = new ParticipantRef();
		participantRef.setParticipantRefLogin("user");
		trainingInstance.setPassword("keyword".toCharArray());
		trainingInstance.setStartTime(LocalDateTime.now());
		trainingInstance.setEndTime(LocalDateTime.now());
		trainingInstance.setTitle("title");
		sandboxInstanceRef.setTrainingInstance(trainingInstance);

	}

	@Test
	public void saveShouldPersistData() {
		trainingRun1 = new TrainingRun();
		trainingRun1.setStartTime(LocalDateTime.now());
		trainingRun1.setEndTime(LocalDateTime.now());
		trainingRun1.setState(TRState.NEW);
		trainingRun1.setCurrentLevel(entityManager.persist(infoLevel));
		trainingRun1.setParticipantRef(entityManager.persist(participantRef));
		trainingRun1.setTrainingInstance(entityManager.persist(trainingInstance));

		sandboxInstanceRef.setTrainingInstance(trainingInstance);
		trainingRun1.setSandboxInstanceRef(entityManager.persist(sandboxInstanceRef));
		TrainingRun tr = this.entityManager.persistFlushFind(trainingRun1);
		assertEquals(TRState.NEW, tr.getState());
	}
}
