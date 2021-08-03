package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.TrainingLevel;
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
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo.training.persistence.util")
public class TrainingLevelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TrainingLevelRepository trainingLevelRepository;

    private TrainingLevel trainingLevel1, trainingLevel2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setUp() {
        trainingLevel1 = testDataFactory.getPenalizedLevel();
        trainingLevel2 = testDataFactory.getNonPenalizedLevel();
    }

    @Test
    public void findById() {
        Long id = (Long) entityManager.persistAndGetId(trainingLevel1);
        Optional<TrainingLevel> optionalTrainingLevel = trainingLevelRepository.findById(id);
        assertTrue(optionalTrainingLevel.isPresent());
        assertEquals(trainingLevel1, optionalTrainingLevel.get());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void findById_nullableArgument() throws Exception {
        entityManager.persist(trainingLevel2);
        Optional<TrainingLevel> optionalTrainingLevel = trainingLevelRepository.findById(null);
    }

    @Test
    public void findAll() {
        List<TrainingLevel> expectedTrainingLevel = Arrays.asList(trainingLevel1, trainingLevel2);
        expectedTrainingLevel.stream().forEach(g -> entityManager.persist(g));
        List<TrainingLevel> resultTrainingLevel = trainingLevelRepository.findAll();
        assertNotNull(resultTrainingLevel);
        assertEquals(expectedTrainingLevel, resultTrainingLevel);
        assertEquals(2, resultTrainingLevel.size());
    }

    @Test
    public void findAll_emptyDatabase() {
        List<TrainingLevel> expectedTrainingLevel = new ArrayList<>();
        List<TrainingLevel> resultTrainingLevel = trainingLevelRepository.findAll();
        assertNotNull(resultTrainingLevel);
        assertEquals(expectedTrainingLevel, resultTrainingLevel);
        assertEquals(0, resultTrainingLevel.size());
    }
}
