package cz.muni.ics.kypo.training.persistence.repository;

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

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo.training.persistence.util")
public class InfoLevelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private InfoLevelRepository infoLevelRepository;

    private InfoLevel infoLevel, infoLevel2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel2 = testDataFactory.getInfoLevel2();
    }

    @Test
    public void findById() throws Exception {
        long expectedId = entityManager.persist(infoLevel).getId();
        Optional<InfoLevel> infoLevelOptional = infoLevelRepository.findById(expectedId);
        InfoLevel iL = infoLevelOptional.orElseThrow(() -> new Exception("Training run should be found"));
        assertNotNull(iL.getId());
        assertEquals(infoLevel.getContent(), iL.getContent());
    }

    @Test
    public void findById_IdNotInTheDatabase() {
        Optional<InfoLevel> infoLevelOptional = infoLevelRepository.findById(2L);
        assertFalse(infoLevelOptional.isPresent());
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

    @Test
    public void findAll_emptyDatabase() {
        List<InfoLevel> expectedInfoLevels = new ArrayList<>();
        List<InfoLevel> resultInfoLevels = infoLevelRepository.findAll();
        assertNotNull(resultInfoLevels);
        assertEquals(expectedInfoLevels.size(), resultInfoLevels.size());
        assertEquals(expectedInfoLevels, resultInfoLevels);
    }

}
