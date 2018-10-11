package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.GameLevel;
import cz.muni.ics.kypo.training.model.Hint;
import cz.muni.ics.kypo.training.persistence.model.GameLevel;
import cz.muni.ics.kypo.training.persistence.repository.GameLevelRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
public class GameLevelRepositoryTest {

		@Autowired
		private TestEntityManager entityManager;

		@Autowired
		private GameLevelRepository gameLevelRepository;

		private GameLevel gameLevel1, gameLevel2;

		@SpringBootApplication
		static class TestConfiguration { }

		@Before
		public void setUp() {
			gameLevel1 = new GameLevel();
			gameLevel1.setFlag("flag1");
			gameLevel1.setContent("content1");
			gameLevel1.setSolution("solution1");
			gameLevel1.setSolutionPenalized(true);
			gameLevel1.setTitle("title1");

			gameLevel2 = new GameLevel();
			gameLevel2.setFlag("flag2");
			gameLevel2.setContent("content2");
			gameLevel1.setSolution("solution2");
			gameLevel2.setSolutionPenalized(true);
			gameLevel2.setTitle("title2");
		}

		@Test
		public void findById() {
			Long id = (Long) entityManager.persistAndGetId(gameLevel2);
			Optional<GameLevel> optionalGameLevel = gameLevelRepository.findById(id);
			assertTrue(optionalGameLevel.isPresent());
			assertEquals(gameLevel2, optionalGameLevel.get());
		}

		@Test(expected = javax.persistence.PersistenceException.class)
		public void findById_invalidId() {
			entityManager.persist(gameLevel2);
			Optional<GameLevel> optionalGameLevel = gameLevelRepository.findById(null);
		}

		@Test
		public void findAll() {
			entityManager.persist(gameLevel1);
			entityManager.persist(gameLevel2);
			List<GameLevel> expectedGameLevel = Arrays.asList(gameLevel1, gameLevel2);
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
