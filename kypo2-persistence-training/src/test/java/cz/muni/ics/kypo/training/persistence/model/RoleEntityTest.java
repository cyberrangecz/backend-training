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

import javax.persistence.PersistenceException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class RoleEntityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TestEntityManager entityManager;

    private String roleType1 = "ADMINISTRATOR";
    private String roleType2 = "USER";

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void createWhenRoleTypeIsNullShouldThrowException() {
        thrown.expect(PersistenceException.class);
        Role role = new Role();
        this.entityManager.persistFlushFind(role);
    }

    @Test
    public void saveShouldPersistData() {
        Role role2 = new Role();
        role2.setRoleType(roleType2);
        this.entityManager.persistAndFlush(role2);

        Role role1 = new Role();
        role1.setRoleType(roleType1);
        Role r = this.entityManager.persistFlushFind(role1);
        assertEquals(roleType1, r.getRoleType());
    }


}
