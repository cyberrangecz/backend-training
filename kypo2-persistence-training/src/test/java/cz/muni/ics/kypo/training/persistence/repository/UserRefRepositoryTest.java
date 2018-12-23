package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        userRef1.setUserRefLogin("User1");
        entityManager.persist(userRef1);

        userRef2 = new UserRef();
        userRef2.setUserRefLogin("User2");
        entityManager.persist(userRef2);

        userRef3 = new UserRef();
        userRef3.setUserRefLogin("User3");
        entityManager.persist(userRef3);
    }

    @Test
    public void findUsers() {
        HashSet<Long> usersIds = new HashSet<>(Arrays.asList(1L,2L));
        Set<UserRef> userRefs = userRefRepository.findUsers(usersIds);
        assertTrue(userRefs.size() == 2);
        assertTrue(userRefs.contains(userRef1));
        assertTrue(userRefs.contains(userRef2));
    }

}
