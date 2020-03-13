package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo.training.persistence.util")
public class InfoLevelEntityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TestEntityManager entityManager;

    private InfoLevel infoLevel;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        infoLevel = testDataFactory.getInfoLevel1();
    }

    @Test
    public void saveShouldPersistData() {
        InfoLevel iL = this.entityManager.persistFlushFind(infoLevel);
        assertNotNull(iL.getId());
    }

}
