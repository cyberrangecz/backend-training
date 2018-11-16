package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.ParticipantRef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class ParticipantRefRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ParticipantRefRepository participantRefRepository;

    private ParticipantRef participantRef1, participantRef2;
    private String participantRef1Login, participantRef2Login;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        participantRef1 = new ParticipantRef("ParticipantRef1Login123456");
        participantRef2 = new ParticipantRef("ParticipantRef2Login654321");
        participantRef1Login = "ParticipantRef1Login123456";
        participantRef2Login = "ParticipantRef2Login654321";
    }

    @Test
    public void findById() throws Exception {
        Long id = (Long) entityManager.persistAndGetId(participantRef1);
        Optional<ParticipantRef> optionalParticipantRef = participantRefRepository.findById(id);
        assertTrue(optionalParticipantRef.isPresent());
        assertEquals(id, optionalParticipantRef.get().getId());
        assertEquals(participantRef1Login, optionalParticipantRef.get().getParticipantRefLogin());
    }

    @Test
    public void findAll() {
        List<ParticipantRef> extectedParticipantsRef = Arrays.asList(participantRef1, participantRef2);
        extectedParticipantsRef.stream().forEach(e -> entityManager.persist(e));
        List<ParticipantRef> resultParticipantRef = participantRefRepository.findAll();
        assertEquals(extectedParticipantsRef, resultParticipantRef);
        assertEquals(2, resultParticipantRef.size());
    }

    @Test
    public void findByParticipantRefLogin() {
        entityManager.persist(participantRef1);
        entityManager.persist(participantRef2);
        Optional<ParticipantRef> optionalParticipantRef = participantRefRepository.findByParticipantRefLogin(participantRef2Login);
        assertTrue(optionalParticipantRef.isPresent());
        assertEquals(participantRef2, optionalParticipantRef.get());
    }

    @Test
    public void findByParticipantRefLogin_unpresent_login() {
        Optional<ParticipantRef> optionalParticipantRef = participantRefRepository.findByParticipantRefLogin("ParticipantInvalidLogin987123");
        assertFalse(optionalParticipantRef.isPresent());
    }
}
