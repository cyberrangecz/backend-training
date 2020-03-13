package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
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
public class GameLevelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private GameLevelRepository gameLevelRepository;

    private GameLevel gameLevel1, gameLevel2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setUp() {
        gameLevel1 = testDataFactory.getPenalizedLevel();
        gameLevel2 = testDataFactory.getNonPenalizedLevel();
    }

    @Test
    public void findById() {
        Long id = (Long) entityManager.persistAndGetId(gameLevel1);
        Optional<GameLevel> optionalGameLevel = gameLevelRepository.findById(id);
        assertTrue(optionalGameLevel.isPresent());
        assertEquals(gameLevel1, optionalGameLevel.get());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void findById_nullableArgument() throws Exception {
        entityManager.persist(gameLevel2);
        Optional<GameLevel> optionalGameLevel = gameLevelRepository.findById(null);
    }

    @Test
    public void findAll() {
        List<GameLevel> expectedGameLevel = Arrays.asList(gameLevel1, gameLevel2);
        expectedGameLevel.stream().forEach(g -> entityManager.persist(g));
        List<GameLevel> resultGameLevel = gameLevelRepository.findAll();
        assertNotNull(resultGameLevel);
        assertEquals(expectedGameLevel, resultGameLevel);
        assertEquals(2, resultGameLevel.size());
    }

    @Test
    public void findAll_emptyDatabase() {
        List<GameLevel> expectedGameLevel = new ArrayList<>();
        List<GameLevel> resultGameLevel = gameLevelRepository.findAll();
        assertNotNull(resultGameLevel);
        assertEquals(expectedGameLevel, resultGameLevel);
        assertEquals(0, resultGameLevel.size());
    }
}
