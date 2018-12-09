package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;


    @SpringBootApplication
    static class TestConfiguration {
    }

    @Test
    public void findByRoleType() throws Exception {
        Role role = new Role();
        role.setRoleType("ADMINISTRATOR");
        this.entityManager.persist(role);
        Optional<Role> optionalRole = this.roleRepository.findByRoleType("ADMINISTRATOR");
        Role r = optionalRole.orElseThrow(() -> new Exception("Role shoul be found"));
        assertEquals(role, r);
        assertEquals("ADMINISTRATOR", r.getRoleType());
    }

    @Test
    public void findByRoleTypeNotFound() {
        assertFalse(this.roleRepository.findByRoleType("ADMINISTRATOR").isPresent());
    }

}
