package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class TrainingInstanceRepositoryTest {

		@Autowired
		private TestEntityManager entityManager;

		@Autowired
		private TrainingInstanceRepository trainingInstanceRepository;

		private TrainingInstance trainingInstance1, trainingInstance2;

		@SpringBootApplication
		static class TestConfiguration { }


		@Before
		public void setUp() {
			trainingInstance1 = new TrainingInstance();
			trainingInstance2 = new TrainingInstance();

			trainingInstance1.setStartTime(LocalDateTime.now());
			trainingInstance1.setEndTime(LocalDateTime.now());
			trainingInstance1.setTitle("Training instance 1");
			trainingInstance1.setPoolSize(10);
			trainingInstance1.setPasswordHash("1Eh9A5l7Op5As8s0h9");

			trainingInstance2.setStartTime(LocalDateTime.now());
			trainingInstance2.setEndTime(LocalDateTime.now());
			trainingInstance2.setTitle("Training instance 2");
			trainingInstance2.setPoolSize(15);
			trainingInstance2.setPasswordHash("R8a9C7B4a2c8A2cN1E");
		}

		@Test
		public void findById() {
			Long id = (Long) entityManager.persistAndGetId(trainingInstance1);
			Optional<TrainingInstance> optionalTrainingInstance = trainingInstanceRepository.findById(id);
			assertTrue(optionalTrainingInstance.isPresent());
			assertEquals(trainingInstance1, optionalTrainingInstance.get());
		}

		@Test
		public void findById_IdNotInTheDatabase() {
				Optional<TrainingInstance> optionalTrainingInstance = trainingInstanceRepository.findById(5L);
				assertFalse(optionalTrainingInstance.isPresent());
		}

		@Test
		public void findAll() {
			entityManager.persist(trainingInstance1);
			entityManager.persist(trainingInstance2);
			List<TrainingInstance> resultTrainingInstance = trainingInstanceRepository.findAll();
			assertNotNull(resultTrainingInstance);
			assertEquals(2, resultTrainingInstance.size());
			assertTrue(resultTrainingInstance.contains(trainingInstance1));
			assertTrue(resultTrainingInstance.contains(trainingInstance2));
		}

		@Test
		public void findAll_emptyDatabase() {
			List<TrainingInstance> expectedTrainingInstances = new ArrayList<>();
			List<TrainingInstance> resultTrainingInstances = trainingInstanceRepository.findAll();
			assertNotNull(resultTrainingInstances);
			assertEquals(expectedTrainingInstances.size(), resultTrainingInstances.size());
			assertEquals(expectedTrainingInstances, resultTrainingInstances);
		}

}
