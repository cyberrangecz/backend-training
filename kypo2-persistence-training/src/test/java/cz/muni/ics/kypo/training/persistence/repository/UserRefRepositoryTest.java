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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
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
        userRef1.setUserRefLogin("Dave");

        userRef2 = new UserRef();
        userRef2.setUserRefLogin("Karl");

        userRef3 = new UserRef();
        userRef3.setUserRefLogin("John");

        entityManager.persist(userRef1);
        entityManager.persist(userRef2);
        entityManager.persist(userRef3);

    }

    @Test
    public void findAllByLoggedInUser() {
        Set<UserRef> users = userRefRepository.findUsers(Set.of(userRef1.getUserRefLogin(), userRef3.getUserRefLogin()));
        assertEquals(2, users.size());
        assertTrue(users.contains(userRef1));
        assertTrue(users.contains(userRef3));
    }

    @Test
    public void findUserByUserRefLogin() {
        Optional<UserRef> userRef = userRefRepository.findUserByUserRefLogin(userRef1.getUserRefLogin());
        assertTrue(userRef.isPresent());
        assertEquals(userRef1, userRef.get());
    }
}
