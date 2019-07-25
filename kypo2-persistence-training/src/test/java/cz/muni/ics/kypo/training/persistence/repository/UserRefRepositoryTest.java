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
import org.springframework.data.domain.Page;
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
        userRef1.setUserRefId(1L);
        userRef1.setUserRefLogin("Dave");
        userRef1.setUserRefFullName("Mgr. Ing. Pavel Seda");
        userRef1.setUserRefFamilyName("Seda");
        userRef1.setUserRefGivenName("Pavel");
        userRef1.setIss("https://oidc.muni.cz");

        userRef2 = new UserRef();
        userRef2.setUserRefId(2L);
        userRef2.setUserRefLogin("Karl");
        userRef2.setUserRefFullName("Mgr. Ing. Pavel Seda");
        userRef2.setUserRefFamilyName("Seda");
        userRef2.setUserRefGivenName("Pavel");
        userRef2.setIss("https://oidc.muni.cz");

        userRef3 = new UserRef();
        userRef3.setUserRefId(3L);
        userRef3.setUserRefLogin("John");
        userRef3.setUserRefFullName("Mgr. Ing. Pavel Seda");
        userRef3.setUserRefFamilyName("Seda");
        userRef3.setUserRefGivenName("Pavel");
        userRef3.setIss("https://oidc.muni.cz");


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
