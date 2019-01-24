package cz.muni.ics.kypo.training.persistence.repository;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class TrainingRunRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrainingRunRepository trainingRunRepository;

    private TrainingRun trainingRun1, trainingRun2;
    private TrainingInstance trainingInstance;
    private SandboxInstanceRef sandboxInstanceRef1, sandboxInstanceRef2, sandboxInstanceRef3;
    private TrainingDefinition trainingDefinition;
    private InfoLevel infoLevel;
    private UserRef participantRef;
    private TDViewGroup tdViewGroup;
    private Pageable pageable;
    private Predicate predicate;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        tdViewGroup = new TDViewGroup();
        tdViewGroup.setTitle("title1");

        sandboxInstanceRef1 = new SandboxInstanceRef();
        sandboxInstanceRef1.setSandboxInstanceRef(1L);
        sandboxInstanceRef2 = new SandboxInstanceRef();
        sandboxInstanceRef2.setSandboxInstanceRef(2L);
        sandboxInstanceRef3 = new SandboxInstanceRef();
        sandboxInstanceRef3.setSandboxInstanceRef(3L);

        trainingDefinition = new TrainingDefinition();
        trainingDefinition.setState(TDState.ARCHIVED);
        trainingDefinition.setTitle("training definition title");
        trainingDefinition.setSandboxDefinitionRefId(1L);
        trainingDefinition.setTdViewGroup(tdViewGroup);

        infoLevel = new InfoLevel();
        infoLevel.setTitle("infoLevel");
        infoLevel.setContent("content for info level");

        participantRef = new UserRef();
        participantRef.setUserRefLogin("user");

        trainingInstance = new TrainingInstance();
        trainingInstance.setAccessToken("b5f3dc27a09865be37cef07816c4f08cf5585b116a4e74b9387c3e43e3a25ec8");
        trainingInstance.setStartTime(LocalDateTime.now());
        trainingInstance.setEndTime(LocalDateTime.now());
        trainingInstance.setTitle("title");
        trainingInstance.setTrainingDefinition(entityManager.persist(trainingDefinition));

        trainingRun2 = new TrainingRun();
        trainingRun2.setStartTime(LocalDateTime.now());
        trainingRun2.setEndTime(LocalDateTime.now());
        trainingRun2.setState(TRState.ALLOCATED);
        trainingRun2.setCurrentLevel(entityManager.persist(infoLevel));
        trainingRun2.setParticipantRef(entityManager.persist(participantRef));
        trainingRun2.setTrainingInstance(entityManager.persist(trainingInstance));
        sandboxInstanceRef2.setTrainingInstance(trainingInstance);
        trainingRun2.setSandboxInstanceRef(entityManager.persist(sandboxInstanceRef2));

        trainingRun1 = new TrainingRun();
        trainingRun1.setStartTime(LocalDateTime.now());
        trainingRun1.setEndTime(LocalDateTime.now());
        trainingRun1.setState(TRState.ALLOCATED);
        trainingRun1.setCurrentLevel(entityManager.persist(infoLevel));
        trainingRun1.setParticipantRef(entityManager.persist(participantRef));
        trainingRun1.setTrainingInstance(entityManager.persist(trainingInstance));
        sandboxInstanceRef1.setTrainingInstance(trainingInstance);
        trainingRun1.setSandboxInstanceRef(entityManager.persist(sandboxInstanceRef1));

        sandboxInstanceRef2.setTrainingInstance(trainingInstance);
        entityManager.persist(sandboxInstanceRef2);
        sandboxInstanceRef3.setTrainingInstance(trainingInstance);
        entityManager.persist(sandboxInstanceRef3);

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
    public void findAllByParticipantRefLogin() throws Exception {
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
                .findAllByTrainingDefinitionIdAndParticipantRefLogin(trainingDefinition.getId(), participantRef.getUserRefLogin(), pageable)
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
        Set<SandboxInstanceRef> sandboxInstanceRefs = trainingRunRepository.findFreeSandboxesOfTrainingInstance(trainingInstance.getId());
        assertEquals(2, sandboxInstanceRefs.size());
    }
}
