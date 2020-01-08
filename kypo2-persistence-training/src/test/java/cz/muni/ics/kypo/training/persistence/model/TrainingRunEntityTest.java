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
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

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
    private TrainingDefinition trainingDefinition;
    private InfoLevel infoLevel;
    private UserRef participantRef;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        trainingInstance = new TrainingInstance();
        trainingDefinition = new TrainingDefinition();
        infoLevel = new InfoLevel();
        infoLevel.setTitle("infoLevel");
        infoLevel.setContent("content for info level");

        participantRef = new UserRef();
        participantRef.setUserRefId(3L);

        trainingInstance.setAccessToken("b5f3dc27a09865be37cef07816c4f08cf5585b116a4e74b9387c3e43e3a25ec8");
        trainingInstance.setStartTime(LocalDateTime.now());
        trainingInstance.setEndTime(LocalDateTime.now());
        trainingInstance.setTitle("title");

    }

    @Test
    public void saveShouldPersistData() {
        trainingRun1 = new TrainingRun();
        trainingRun1.setStartTime(LocalDateTime.now());
        trainingRun1.setEndTime(LocalDateTime.now());
        trainingRun1.setState(TRState.RUNNING);
        trainingRun1.setCurrentLevel(entityManager.persist(infoLevel));
        trainingRun1.setParticipantRef(entityManager.persist(participantRef));
        trainingRun1.setTrainingInstance(entityManager.persist(trainingInstance));

        TrainingRun tr = this.entityManager.persistFlushFind(trainingRun1);
        assertEquals(TRState.RUNNING, tr.getState());
    }
}
