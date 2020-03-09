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

import java.time.LocalDateTime;
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

    private TrainingDefinition trainingDefinitionWithBG1, trainingDefinitionWithBG2, trainingDefinitionWithBG3, trainingDefinitionWithBG4;
    private TrainingDefinition trainingDefinition1, trainingDefinition2;
    private UserRef author1, author2, organizer1;
    private BetaTestingGroup viewGroup1, viewGroup2, viewGroup3, viewGroup4;
    private Pageable pageable;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        author1 = new UserRef();
        author1.setUserRefId(3L);
        author2 = new UserRef();
        author2.setUserRefId(8L);
        organizer1 = new UserRef();
        organizer1.setUserRefId(5L);

        trainingDefinition1 = new TrainingDefinition();
        trainingDefinition1.setTitle("test1");
        trainingDefinition1.setState(TDState.RELEASED);
        trainingDefinition1.setLastEdited(LocalDateTime.now());

        trainingDefinition2 = new TrainingDefinition();
        trainingDefinition2.setTitle("test2");
        trainingDefinition2.setState(TDState.UNRELEASED);
        trainingDefinition2.setLastEdited(LocalDateTime.now());

        trainingDefinitionWithBG1 = new TrainingDefinition();
        trainingDefinitionWithBG1.setTitle("test3");
        trainingDefinitionWithBG1.setState(TDState.RELEASED);
        trainingDefinitionWithBG1.setLastEdited(LocalDateTime.now());

        trainingDefinitionWithBG2 = new TrainingDefinition();
        trainingDefinitionWithBG2.setTitle("test4");
        trainingDefinitionWithBG2.setState(TDState.UNRELEASED);
        trainingDefinitionWithBG2.setLastEdited(LocalDateTime.now());

        trainingDefinitionWithBG3 = new TrainingDefinition();
        trainingDefinitionWithBG3.setTitle("test5");
        trainingDefinitionWithBG3.setState(TDState.RELEASED);
        trainingDefinitionWithBG3.setLastEdited(LocalDateTime.now());

        trainingDefinitionWithBG4 = new TrainingDefinition();
        trainingDefinitionWithBG4.setTitle("test6");
        trainingDefinitionWithBG4.setState(TDState.UNRELEASED);
        trainingDefinitionWithBG4.setLastEdited(LocalDateTime.now());

        viewGroup1 = new BetaTestingGroup();
        viewGroup2 = new BetaTestingGroup();
        viewGroup3 = new BetaTestingGroup();
        viewGroup4 = new BetaTestingGroup();

        trainingDefinitionWithBG1.setBetaTestingGroup(viewGroup1);
        trainingDefinitionWithBG2.setBetaTestingGroup(viewGroup2);
        trainingDefinitionWithBG3.setBetaTestingGroup(viewGroup3);
        trainingDefinitionWithBG4.setBetaTestingGroup(viewGroup4);

        entityManager.persist(trainingDefinition1);
        entityManager.persist(trainingDefinition2);
        entityManager.persist(trainingDefinitionWithBG1);
        entityManager.persist(trainingDefinitionWithBG2);
        entityManager.persist(trainingDefinitionWithBG3);
        entityManager.persist(trainingDefinitionWithBG4);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void findAllAsDesigner() {
        entityManager.persist(author1);
        entityManager.persist(author2);
        entityManager.persist(organizer1);

        trainingDefinition2.addAuthor(author1);
        trainingDefinition2.addAuthor(author2);
        entityManager.merge(trainingDefinition2);
        trainingDefinitionWithBG1.addAuthor(author1);
        viewGroup1.addOrganizer(organizer1);
        entityManager.merge(trainingDefinitionWithBG2);
        trainingDefinitionWithBG2.addAuthor(author1);
        trainingDefinitionWithBG2.addAuthor(author2);
        viewGroup2.addOrganizer(author1);
        viewGroup2.addOrganizer(organizer1);
        entityManager.merge(trainingDefinitionWithBG2);

        trainingDefinitionWithBG3.addAuthor(author2);
        viewGroup3.addOrganizer(author2);
        entityManager.merge(trainingDefinitionWithBG3);

        trainingDefinitionWithBG4.addAuthor(author2);
        viewGroup4.addOrganizer(author1);
        viewGroup4.addOrganizer(organizer1);
        entityManager.merge(trainingDefinitionWithBG4);

        List<TrainingDefinition> trainingDefinitions = trainingDefinitionRepository.findAll(null, PageRequest.of(0,10), author1.getUserRefId()).getContent();
        assertTrue(trainingDefinitions.containsAll(List.of(trainingDefinition2, trainingDefinitionWithBG1, trainingDefinitionWithBG2, trainingDefinitionWithBG4)));
        assertEquals(4, trainingDefinitions.size());
    }

}
