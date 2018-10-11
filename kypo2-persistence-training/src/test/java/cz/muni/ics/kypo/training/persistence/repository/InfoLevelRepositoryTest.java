package cz.muni.ics.kypo.training.persistence.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class InfoLevelRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private InfoLevelRepository infoLevelRepository;

	private InfoLevel infoLevel, infoLevel2;

	@SpringBootApplication
	static class TestConfiguration { }

	@Before
	public void init() {
		infoLevel = new InfoLevel();
		infoLevel.setTitle("infoLevel");
		infoLevel.setContent("content for info level");

		infoLevel2 = new InfoLevel();
		infoLevel2.setTitle("infolevel2");
		infoLevel2.setContent("content for info level2");
	}

	@Test
	public void findById() throws Exception {
		long expectedId = entityManager.persist(infoLevel).getId();
		Optional<InfoLevel> infoLevelOptional = infoLevelRepository.findById(expectedId);
		InfoLevel iL = infoLevelOptional.orElseThrow(() -> new Exception("Training run should be found"));
		assertNotNull(iL.getId());
		assertEquals("content for info level", iL.getContent());
	}

	@Test
	public void findAll() {
		List<InfoLevel> expectedInfoLevels = Arrays.asList(infoLevel, infoLevel2);
		expectedInfoLevels.stream().forEach(i -> entityManager.persist(i));
		List<InfoLevel> resultInfoLevels = infoLevelRepository.findAll();
		assertNotNull(resultInfoLevels);
		assertEquals(expectedInfoLevels, resultInfoLevels);
		assertEquals(expectedInfoLevels.size(), resultInfoLevels.size());
	}

}
