package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
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

import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
@ComponentScan(basePackages = "cz.muni.ics.kypo.training.persistence.util")
public class UserRefRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRefRepository userRefRepository;

    private UserRef userRef1, userRef2, userRef3;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setUp() {
        userRef1 = new UserRef();
        userRef1.setUserRefId(1L);

        userRef2 = new UserRef();
        userRef2.setUserRefId(2L);

        userRef3 = new UserRef();
        userRef3.setUserRefId(3L);

        entityManager.persist(userRef1);
        entityManager.persist(userRef2);
        entityManager.persist(userRef3);

    }

    @Test
    public void findAllByLoggedInUser() {
        Set<UserRef> users = userRefRepository.findUsers(Set.of(userRef1.getUserRefId(), userRef3.getUserRefId()));
        assertEquals(2, users.size());
        assertTrue(users.contains(userRef1));
        assertTrue(users.contains(userRef3));
    }
}
