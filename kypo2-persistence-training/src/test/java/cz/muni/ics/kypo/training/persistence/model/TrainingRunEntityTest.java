package cz.muni.ics.kypo.training.persistence.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.ParticipantRef;
import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Seda
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
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
        trainingInstance.setPassword("b5f3dc27a09865be37cef07816c4f08cf5585b116a4e74b9387c3e43e3a25ec8");
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
