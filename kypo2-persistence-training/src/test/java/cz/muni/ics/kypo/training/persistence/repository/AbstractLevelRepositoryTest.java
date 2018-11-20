package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class AbstractLevelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AbstractLevelRepository abstractLevelRepository;

    private GameLevel gameLevel, gameLevel2;
    private AssessmentLevel assessmentLevel;
    private InfoLevel infoLevel, infoLevel2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setUp() {
        gameLevel = new GameLevel();
        gameLevel.setFlag("flag1");
        gameLevel.setContent("content1");
        gameLevel.setSolution("solution1");
        gameLevel.setSolutionPenalized(true);
        gameLevel.setTitle("title1");
        gameLevel2 = new GameLevel();
        gameLevel2.setFlag("flag2");
        gameLevel2.setContent("content2");
        gameLevel2.setSolution("solution2");
        gameLevel2.setSolutionPenalized(false);
        gameLevel2.setTitle("title2");
        assessmentLevel = new AssessmentLevel();
        assessmentLevel.setQuestions("question1");
        assessmentLevel.setInstructions("instruction1");
        assessmentLevel.setAssessmentType(AssessmentType.TEST);
        assessmentLevel.setTitle("title1");
        infoLevel = new InfoLevel();
        infoLevel.setTitle("infoLevel");
        infoLevel.setContent("content for info level");
        infoLevel2 = new InfoLevel();
        infoLevel2.setTitle("infoLevel2");
        infoLevel2.setContent("content for info level2");
    }

    @Test
    public void findById_gameLevel() {
        Long id = (Long) entityManager.persistAndGetId(gameLevel);
        entityManager.persist(assessmentLevel);
        entityManager.persist(infoLevel);
        Optional<AbstractLevel> optionalGameLevel = abstractLevelRepository.findById(id);
        assertTrue(optionalGameLevel.isPresent());
        assertTrue(optionalGameLevel.get() instanceof GameLevel);
        assertEquals(gameLevel, optionalGameLevel.get());
    }

    @Test
    public void findById_infoLevel() {
        Long id = (Long) entityManager.persistAndGetId(infoLevel);
        entityManager.persist(gameLevel);
        entityManager.persist(assessmentLevel);
        Optional<AbstractLevel> optionalInfoLevel = abstractLevelRepository.findById(id);
        assertTrue(optionalInfoLevel.isPresent());
        assertTrue(optionalInfoLevel.get() instanceof InfoLevel);
        assertEquals(infoLevel, optionalInfoLevel.get());
    }

    @Test
    public void findById_assesmentLevel() {
        Long id = (Long) entityManager.persistAndGetId(assessmentLevel);
        entityManager.persist(gameLevel2);
        entityManager.persist(gameLevel);
        entityManager.persist(infoLevel);
        Optional<AbstractLevel> optionalAssesmentLevel = abstractLevelRepository.findById(id);
        assertTrue(optionalAssesmentLevel.isPresent());
        assertTrue(optionalAssesmentLevel.get() instanceof AssessmentLevel);
        assertEquals(assessmentLevel, optionalAssesmentLevel.get());
    }

    @Test
    public void findById_gameLevel_multipleOccurrences() {
        entityManager.persist(assessmentLevel);
        entityManager.persist(gameLevel);
        entityManager.persist(infoLevel);
        Long id = (Long) entityManager.persistAndGetId(gameLevel2);
        entityManager.persist(infoLevel2);
        Optional<AbstractLevel> optionalGameLevel = abstractLevelRepository.findById(id);
        assertTrue(optionalGameLevel.isPresent());
        assertTrue(optionalGameLevel.get() instanceof GameLevel);
        assertEquals(gameLevel2, optionalGameLevel.get());
    }

    @Test
    public void findAll() {
        List<AbstractLevel> expectedAbstractLevels = Arrays.asList(gameLevel, infoLevel, assessmentLevel, infoLevel2, gameLevel2);
        expectedAbstractLevels.stream().forEach(a -> entityManager.persist(a));
        List<AbstractLevel> resultAbstractLevels = abstractLevelRepository.findAll();
        assertNotNull(resultAbstractLevels);
        assertEquals(expectedAbstractLevels.size(), resultAbstractLevels.size());
        assertEquals(expectedAbstractLevels, resultAbstractLevels);
    }

}
