package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.InfoLevel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
public class InfoLevelRepositoryTest {

		@Autowired
		private TestEntityManager entityManager;

		@Autowired
		private InfoLevelRepository infoLevelRepository;

		private InfoLevel infoLevel;

		@SpringBootApplication
		static class TestConfiguration {
		}

		@Before
		public void init() {
				infoLevel = new InfoLevel();
				infoLevel.setTitle("infoLevel");
				infoLevel.setContent("content for info level");
		}

		@Test
		public void findById() throws Exception {
				long expectedId = entityManager.persist(infoLevel).getId();
				Optional<InfoLevel> infoLevelOptional = infoLevelRepository.findById(expectedId);
				InfoLevel iL = infoLevelOptional.orElseThrow(() -> new Exception("Training run should be found"));
				assertNotNull(iL.getId());
				assertEquals("content for info level", iL.getContent());
		}

}
