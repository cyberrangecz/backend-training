package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.BetaTestingGroup;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class BetaTestingGroupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BetaTestingGroupRepository viewGroupRepository;

    private BetaTestingGroup viewGroup1, viewGroup2;
    private TrainingDefinition trainingDefinition1, trainingDefinition2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        viewGroup1 = new BetaTestingGroup();

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setSandboxDefinitionRefId(1L);
        trainingDefinition1.setTitle("Best training definition");
        trainingDefinition1.setState(TDState.UNRELEASED);
        trainingDefinition1.setBetaTestingGroup(viewGroup1);
        trainingDefinition1.setLastEdited(LocalDateTime.now());

        viewGroup2 = new BetaTestingGroup();

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setSandboxDefinitionRefId(2L);
        trainingDefinition2.setTitle("Very good training definition");
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setBetaTestingGroup(viewGroup2);
        trainingDefinition2.setLastEdited(LocalDateTime.now());

        entityManager.persist(trainingDefinition1);
        entityManager.persist(trainingDefinition2);
    }

    @Test
    public void findById() {
        Optional<BetaTestingGroup> betaTestingGroupOptional = viewGroupRepository.findById(viewGroup2.getId());
        assertTrue(betaTestingGroupOptional.isPresent());
        assertEquals(viewGroup2, betaTestingGroupOptional.get());

        Optional<BetaTestingGroup> tdViewGroupOptional2 = viewGroupRepository.findById(viewGroup1.getId());
        assertTrue(tdViewGroupOptional2.isPresent());
        assertEquals(viewGroup1, tdViewGroupOptional2.get());

    }
}
