package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo.training.persistence.util")
public class TrainingRunRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TrainingRunRepository trainingRunRepository;

    private TrainingRun trainingRun1, trainingRun2;
    private TrainingInstance trainingInstance;
    private TrainingDefinition trainingDefinition;
    private InfoLevel infoLevel;
    private UserRef participantRef;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        trainingDefinition = testDataFactory.getArchivedDefinition();

        infoLevel = testDataFactory.getInfoLevel1();

        participantRef = new UserRef();
        participantRef.setUserRefId(1L);

        trainingInstance = testDataFactory.getOngoingInstance();
        trainingInstance.setTrainingDefinition(entityManager.persist(trainingDefinition));

        trainingRun2 = testDataFactory.getRunningRun();
        trainingRun2.setCurrentLevel(entityManager.persist(infoLevel));
        trainingRun2.setParticipantRef(entityManager.persist(participantRef));
        trainingRun2.setTrainingInstance(entityManager.persist(trainingInstance));
        trainingRun2.setSandboxInstanceRefId(1L);

        trainingRun1 = testDataFactory.getFinishedRun();
        trainingRun1.setCurrentLevel(entityManager.persist(infoLevel));
        trainingRun1.setParticipantRef(entityManager.persist(participantRef));
        trainingRun1.setTrainingInstance(entityManager.persist(trainingInstance));
        trainingRun1.setSandboxInstanceRefId(2L);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void findByIdWithLevel() {
        Long trainingRunId = (Long) entityManager.persistAndGetId(trainingRun2);
        Optional<TrainingRun> optionalTrainingRun = trainingRunRepository.findByIdWithLevel(trainingRunId);
        assertTrue(optionalTrainingRun.isPresent());
        assertTrue(optionalTrainingRun.get().getCurrentLevel() instanceof InfoLevel);
    }

    @Test
    public void findById() throws Exception {
        long expectedId = entityManager.persist(trainingRun1).getId();
        Optional<TrainingRun> optionalTR = trainingRunRepository.findById(expectedId);
        TrainingRun tr = optionalTR.orElseThrow(() -> new Exception("Training run should be found"));
        assertEquals(trainingRun1, tr);
    }

    @Test
    public void findAll() {
        List<TrainingRun> expectedTrainingRuns = Arrays.asList(trainingRun1, trainingRun2);
        expectedTrainingRuns.forEach(t -> entityManager.persist(t));
        List<TrainingRun> resultTrainingRuns = trainingRunRepository.findAll();
        assertNotNull(resultTrainingRuns);
        assertEquals(expectedTrainingRuns, resultTrainingRuns);
        assertEquals(expectedTrainingRuns.size(), resultTrainingRuns.size());
    }

    @Test
    public void findAllByParticipantRefLogin() {
        entityManager.persist(trainingRun1);
        entityManager.persist(trainingRun2);
        List<TrainingRun> trainingRuns = trainingRunRepository.findAllByParticipantRefId(1L, pageable).getContent();
        assertTrue(trainingRuns.contains(trainingRun1));
        assertTrue(trainingRuns.contains(trainingRun2));
        assertEquals(2, trainingRuns.size());
    }

    @Test
    public void findAllByTrainingDefinitionIdAndParticipantRefId() {
        entityManager.persistAndFlush(trainingRun1);
        List<TrainingRun> trainingRuns = trainingRunRepository
                .findAllByTrainingDefinitionIdAndParticipantUserRefId(trainingDefinition.getId(), participantRef.getUserRefId(), pageable)
                .getContent();
        assertEquals(1, trainingRuns.size());

    }

    @Test
    public void findAllByTrainingInstanceId() {
        entityManager.persist(trainingRun1);
        entityManager.persist(trainingRun2);
        List<TrainingRun> trainingRuns = trainingRunRepository.findAllByTrainingInstanceId(trainingInstance.getId(), pageable).getContent();
        assertEquals(2, trainingRuns.size());
        assertTrue(trainingRuns.contains(trainingRun1));
        assertTrue(trainingRuns.contains(trainingRun2));
    }

    @Test
    public void findAllByTrainingDefinitionId() {
        entityManager.persist(trainingRun1);
        entityManager.persist(trainingRun2);
        List<TrainingRun> trainingRuns = trainingRunRepository.findAllByTrainingDefinitionId(trainingDefinition.getId(), pageable).getContent();
        assertEquals(2, trainingRuns.size());
        assertTrue(trainingRuns.contains(trainingRun1));
        assertTrue(trainingRuns.contains(trainingRun2));
    }

    @Test
    public void deleteTrainingRunsByTrainingInstance() {
        entityManager.persist(trainingRun1);
        entityManager.persist(trainingRun2);
        trainingRunRepository.deleteTrainingRunsByTrainingInstance(trainingInstance.getId());
        Page<TrainingRun> trainingRunsAfterDelete = trainingRunRepository.findAllByTrainingInstanceId(trainingInstance.getId(), pageable);
        assertEquals(0, trainingRunsAfterDelete.getContent().size());
        assertFalse(trainingRunsAfterDelete.getContent().contains(trainingRun1));
        assertFalse(trainingRunsAfterDelete.getContent().contains(trainingRun2));
    }

}
