package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.OrganizerRef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class OrganizerRefRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizerRefRepository organizerRefRepository;

    private OrganizerRef organizerRef1, organizerRef2, organizerRef3;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setUp() {
        organizerRef1 = new OrganizerRef();
        organizerRef1.setOrganizersRefLogin("User1");
        entityManager.persist(organizerRef1);

        organizerRef2 = new OrganizerRef();
        organizerRef2.setOrganizersRefLogin("User2");
        entityManager.persist(organizerRef2);

        organizerRef3 = new OrganizerRef();
        organizerRef3.setOrganizersRefLogin("User3");
        entityManager.persist(organizerRef3);
    }

    @Test
    public void findUsers() {
        HashSet<Long> usersIds = new HashSet<>(Arrays.asList(1L,2L));
        Set<OrganizerRef> organizerRefs = organizerRefRepository.findUsers(usersIds);
        assertTrue(organizerRefs.size() == 2);
        assertTrue(organizerRefs.contains(organizerRef1));
        assertTrue(organizerRefs.contains(organizerRef2));
    }

}
