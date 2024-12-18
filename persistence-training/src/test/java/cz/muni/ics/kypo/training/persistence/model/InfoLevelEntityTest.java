package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class InfoLevelEntityTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TestEntityManager entityManager;

    private InfoLevel infoLevel;

    @BeforeEach
    public void init() {
        infoLevel = testDataFactory.getInfoLevel1();
    }

    @Test
    public void saveShouldPersistData() {
        InfoLevel iL = this.entityManager.persistFlushFind(infoLevel);
        assertNotNull(iL.getId());
    }

}
