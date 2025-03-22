package cz.cyberrange.platform.training.persistence.repository;


import cz.cyberrange.platform.training.persistence.model.JeopardyLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class JeopardyLevelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private JeopardyLevelRepository jeopardyLevelRepository;

    private JeopardyLevel jeopardyLevel1, jeopardyLevel2, jeopardyLevelEmpty;
    private TrainingDefinition trainingDefinition1, trainingDefinition2;

    @BeforeEach
    public void setUp() {
        trainingDefinition1 = testDataFactory.getArchivedDefinition();
        trainingDefinition2 = testDataFactory.getReleasedDefinition();
        entityManager.persist(trainingDefinition1);
        entityManager.persist(trainingDefinition2);

        jeopardyLevel1 = testDataFactory.getJeopardyLevel();
        jeopardyLevel2 = testDataFactory.getJeopardyLevel();
        jeopardyLevelEmpty = testDataFactory.getJeopardyLevelEmpty();

        jeopardyLevel1.setTrainingDefinition(trainingDefinition1);
        jeopardyLevel2.setTrainingDefinition(trainingDefinition2);
        jeopardyLevelEmpty.setTrainingDefinition(trainingDefinition1);
    }

    @Test
    public void findById() {
        Long id = (Long) entityManager.persistAndGetId(jeopardyLevel1);
        Optional<JeopardyLevel> optionalJeopardyLevel = jeopardyLevelRepository.findById(id);
        assertTrue(optionalJeopardyLevel.isPresent());
        assertEquals(jeopardyLevel1, optionalJeopardyLevel.get());
    }

    @Test
    public void findByIdNullableArgument() throws Exception {
        entityManager.persist(jeopardyLevel1);
        assertThrows(InvalidDataAccessApiUsageException.class, () -> jeopardyLevelRepository.findById(null));
    }

    @Test
    public void findAll() {
        List<JeopardyLevel> expectedJeopardyLevel = Arrays.asList(jeopardyLevel1, jeopardyLevelEmpty);
        expectedJeopardyLevel.stream().forEach(g -> entityManager.persist(g));
        List<JeopardyLevel> resultJeopardyLevel = jeopardyLevelRepository.findAll();
        assertNotNull(resultJeopardyLevel);
        assertEquals(expectedJeopardyLevel, resultJeopardyLevel);
        assertEquals(2, resultJeopardyLevel.size());
    }

    @Test
    public void findAllEmptyDatabase() {
        List<JeopardyLevel> expectedJeopardyLevel = new ArrayList<>();
        List<JeopardyLevel> resultJeopardyLevel = jeopardyLevelRepository.findAll();
        assertNotNull(resultJeopardyLevel);
        assertEquals(expectedJeopardyLevel, resultJeopardyLevel);
        assertEquals(0, resultJeopardyLevel.size());
    }

    @Test
    public void findAllByTrainingDefinition() {
        entityManager.persist(jeopardyLevel1);
        entityManager.persist(jeopardyLevel2);
        entityManager.persist(jeopardyLevelEmpty);
        List<JeopardyLevel> resultJeopardyLevels = jeopardyLevelRepository.findAllByTrainingDefinitionId(trainingDefinition1.getId());
        assertTrue(resultJeopardyLevels.stream().anyMatch(l -> l.getId().equals(jeopardyLevel1.getId())));
        assertTrue(resultJeopardyLevels.stream().noneMatch(l -> l.getId().equals(jeopardyLevel2.getId())));
        assertEquals(2, resultJeopardyLevels.size());
    }
}
