package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.AbstractLevel;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
public class AssessmentLevelRepositoryTest {

		@Autowired
		private TestEntityManager entityManager;

		@Autowired
		private AssessmentLevelRepository assessmentLevelRepository;

		private AssessmentLevel assessmentLevel1, assessmentLevel2;

		@SpringBootApplication
		static class TestConfiguration { }

		@Before
		public void setUp() {
			assessmentLevel1 = new AssessmentLevel();
			assessmentLevel1.setQuestions("question1");
			assessmentLevel1.setInstructions("instruction1");
			assessmentLevel1.setAssessmentType(AssessmentType.TEST);
			assessmentLevel1.setTitle("title1");

		  assessmentLevel2 = new AssessmentLevel();
			assessmentLevel2.setQuestions("question2");
			assessmentLevel2.setInstructions("instruction2");
			assessmentLevel2.setAssessmentType(AssessmentType.QUESTIONNAIRE);
			assessmentLevel2.setTitle("title2");
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
			entityManager.persist(assessmentLevel1);
			entityManager.persist(assessmentLevel2);
			List<AssessmentLevel> extectedParticipantsRef = Arrays.asList(assessmentLevel1, assessmentLevel2);
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
