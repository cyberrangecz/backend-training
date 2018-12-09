package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class IDMGroupRefEntityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TestEntityManager entityManager;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void saveShouldPersistData() {
        IDMGroupRef groupRef = new IDMGroupRef();
        groupRef.setIdmGroupId(2L);
        IDMGroupRef g = this.entityManager.persistFlushFind(groupRef);
        assertEquals(2L, g.getIdmGroupId());
    }


}
