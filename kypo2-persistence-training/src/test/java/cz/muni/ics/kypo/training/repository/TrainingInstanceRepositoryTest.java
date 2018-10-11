package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.TrainingInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
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
		public void findAll() {
			entityManager.persist(trainingInstance1);
			entityManager.persist(trainingInstance2);
			List<TrainingInstance> resultTraininginstance = trainingInstanceRepository.findAll();
			assertNotNull(resultTraininginstance);
			assertEquals(2, resultTraininginstance.size());
			assertTrue(resultTraininginstance.contains(trainingInstance1));
			assertTrue(resultTraininginstance.contains(trainingInstance2));
		}
		
}
