package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.TrainingLevel;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TrainingLevelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TrainingLevelRepository trainingLevelRepository;

    private TrainingLevel trainingLevel1, trainingLevel2;

    @BeforeEach
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

    @Test
    public void findByIdNullableArgument() throws Exception {
        entityManager.persist(trainingLevel2);
        assertThrows(InvalidDataAccessApiUsageException.class, () -> trainingLevelRepository.findById(null));
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
    public void findAllEmptyDatabase() {
        List<TrainingLevel> expectedTrainingLevel = new ArrayList<>();
        List<TrainingLevel> resultTrainingLevel = trainingLevelRepository.findAll();
        assertNotNull(resultTrainingLevel);
        assertEquals(expectedTrainingLevel, resultTrainingLevel);
        assertEquals(0, resultTrainingLevel.size());
    }
}
