package cz.muni.ics.kypo.training.repository;

import cz.muni.ics.kypo.training.model.ParticipantRef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.text.html.Option;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
public class ParticipantRefRepositoryTest {

		@Autowired
		private TestEntityManager entityManager;

		@Autowired
		private ParticipantRefRepository participantRefRepository;

		private ParticipantRef participantRef1, participantRef2;

		private String participantRef1Login, participantRef2Login;

		@SpringBootApplication
		static class TestConfiguration { }

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
			entityManager.persist(participantRef1);
			entityManager.persist(participantRef2);
			List<ParticipantRef> extectedParticipantsRef = Arrays.asList(participantRef1, participantRef2);
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
