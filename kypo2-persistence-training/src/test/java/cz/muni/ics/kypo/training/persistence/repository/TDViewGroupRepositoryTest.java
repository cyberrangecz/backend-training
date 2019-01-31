package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.TDViewGroup;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
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

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class TDViewGroupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TDViewGroupRepository viewGroupRepository;

    private TDViewGroup viewGroup1, viewGroup2;
    private TrainingDefinition trainingDefinition1, trainingDefinition2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        viewGroup1 = new TDViewGroup();
        viewGroup1.setTitle("Python group");

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setSandboxDefinitionRefId(1L);
        trainingDefinition1.setTitle("Best training definition");
        trainingDefinition1.setState(TDState.UNRELEASED);
        trainingDefinition1.setTdViewGroup(viewGroup1);

        viewGroup2 = new TDViewGroup();
        viewGroup2.setTitle("Java group");

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setSandboxDefinitionRefId(2L);
        trainingDefinition2.setTitle("Very good training definition");
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setTdViewGroup(viewGroup2);

        entityManager.persist(trainingDefinition1);
        entityManager.persist(trainingDefinition2);
    }

    @Test
    public void findByTitle() {
        Optional<TDViewGroup> tdViewGroupOptional1 = viewGroupRepository.findByTitle("Java group");
        assertTrue(tdViewGroupOptional1.isPresent());
        assertEquals(viewGroup2, tdViewGroupOptional1.get());

        Optional<TDViewGroup> tdViewGroupOptional2 = viewGroupRepository.findByTitle("Python group");
        assertTrue(tdViewGroupOptional2.isPresent());
        assertEquals(viewGroup1, tdViewGroupOptional2.get());

    }

    @Test
    public void existsTDViewGroupByTitle() {
        boolean existViewGroup1 = viewGroupRepository.existsTDViewGroupByTitle("Python group");
        assertTrue(existViewGroup1);

        boolean existViewGroup2 = viewGroupRepository.existsTDViewGroupByTitle("Java group");
        assertTrue(existViewGroup2);


    }
}
