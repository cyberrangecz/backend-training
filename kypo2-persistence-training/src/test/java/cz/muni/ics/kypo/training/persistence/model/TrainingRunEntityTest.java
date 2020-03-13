package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo.training.persistence.util")
public class TrainingRunEntityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TestDataFactory testDataFactory;
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
        trainingInstance = testDataFactory.getOngoingInstance();
        trainingDefinition = testDataFactory.getReleasedDefinition();
        infoLevel = testDataFactory.getInfoLevel1();

        participantRef = new UserRef();
        participantRef.setUserRefId(3L);
    }

    @Test
    public void saveShouldPersistData() {
        trainingRun1 = testDataFactory.getRunningRun();
        trainingRun1.setCurrentLevel(entityManager.persist(infoLevel));
        trainingRun1.setParticipantRef(entityManager.persist(participantRef));
        trainingRun1.setTrainingInstance(entityManager.persist(trainingInstance));

        TrainingRun tr = this.entityManager.persistFlushFind(trainingRun1);
        assertEquals(TRState.RUNNING, tr.getState());
    }
}
