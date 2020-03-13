package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
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
public class AssessmentLevelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private AssessmentLevelRepository assessmentLevelRepository;

    private AssessmentLevel assessmentLevel1, assessmentLevel2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setUp() {
        assessmentLevel1 = testDataFactory.getTest();
        assessmentLevel2 = testDataFactory.getQuestionnaire();
    }

    @Test
    public void findById() {
        Long id = (Long) entityManager.persistAndGetId(assessmentLevel1);
        Optional<AssessmentLevel> optionalAssessmentLevel = assessmentLevelRepository.findById(id);
        assertTrue(optionalAssessmentLevel.isPresent());
        assertEquals(id, optionalAssessmentLevel.get().getId());
        assertEquals(assessmentLevel1, optionalAssessmentLevel.get());
    }

    @Test
    public void findAll() {
        List<AssessmentLevel> extectedParticipantsRef = Arrays.asList(assessmentLevel1, assessmentLevel2);
        extectedParticipantsRef.stream().forEach(p -> entityManager.persist(p));
        List<AssessmentLevel> resultParticipantRef = assessmentLevelRepository.findAll();
        assertEquals(extectedParticipantsRef, resultParticipantRef);
        assertEquals(2, resultParticipantRef.size());
    }

    @Test
    public void findAll_emptyDatabase() {
        List<AssessmentLevel> expectedAssesmentLevel = new ArrayList<>();
        List<AssessmentLevel> resultAssesmentLevel = assessmentLevelRepository.findAll();
        assertNotNull(resultAssesmentLevel);
        assertEquals(expectedAssesmentLevel, resultAssesmentLevel);
        assertEquals(0, resultAssesmentLevel.size());
    }
}
