package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.BetaTestingGroup;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
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
import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class TrainingDefinitionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;

    private TrainingDefinition trainingDefinition1, trainingDefinition2, trainingDefinition3;
    private UserRef author1, author2, organizer1;
    private BetaTestingGroup viewGroup1, viewGroup2;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setSandboxDefinitionRefId(1L);
        trainingDefinition1.setTitle("test");
        trainingDefinition1.setState(TDState.UNRELEASED);

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setSandboxDefinitionRefId(1L);
        trainingDefinition2.setTitle("test");
        trainingDefinition2.setState(TDState.UNRELEASED);

        trainingDefinition3 = new TrainingDefinition();
        trainingDefinition3.setSandboxDefinitionRefId(2L);
        trainingDefinition3.setTitle("test");
        trainingDefinition3.setState(TDState.UNRELEASED);

        viewGroup1 = new BetaTestingGroup();
        viewGroup2 = new BetaTestingGroup();

        organizer1 = new UserRef();
        organizer1.setUserRefLogin("Organizer");

        trainingDefinition1.setBetaTestingGroup(viewGroup1);
        trainingDefinition2.setBetaTestingGroup(viewGroup2);

        entityManager.persist(trainingDefinition1);
        entityManager.persist(trainingDefinition2);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void findAllByLoggedInUser() {

        author1 = new UserRef();
        author1.setUserRefLogin("author1");
        entityManager.persist(author1);

        author2 = new UserRef();
        author2.setUserRefLogin("author2");
        entityManager.persist(author2);

        trainingDefinition1.addAuthor(author1);
        trainingDefinition1.addAuthor(author2);
        entityManager.merge(trainingDefinition1);
        trainingDefinition2.addAuthor(author1);
        entityManager.merge(trainingDefinition2);

        List<TrainingDefinition> trainingDefinitions = trainingDefinitionRepository
                .findAllByLoggedInUser("author1", pageable).getContent();
        assertTrue(trainingDefinitions.contains(trainingDefinition1));
        assertTrue(trainingDefinitions.contains(trainingDefinition2));
        assertEquals(2, trainingDefinitions.size());
    }

    @Test
    public void findAllBySandboxDefinitionRefId() {
        List<TrainingDefinition> trainingDefinitions = trainingDefinitionRepository
                .findAllBySandBoxDefinitionRefId(1L, pageable).getContent();
        assertTrue(trainingDefinitions.contains(trainingDefinition1));
        assertTrue(trainingDefinitions.contains(trainingDefinition2));
        assertEquals(2, trainingDefinitions.size());
    }

//    @Test
//    public void findAllByViewGroup() {
//        entityManager.persist(trainingDefinition1);
//        entityManager.persist(trainingDefinition2);
//        entityManager.persist(organizer1);
//        viewGroup1.addOrganizer(organizer1);
//        viewGroup2.addOrganizer(organizer1);
//
//        List<TrainingDefinition> trainingDefinitions = trainingDefinitionRepository
//                .findAllByBetaTesters("Organizer", pageable).getContent();
//        assertTrue(trainingDefinitions.contains(trainingDefinition1));
//        assertEquals(2, trainingDefinitions.size());
//    }

}
