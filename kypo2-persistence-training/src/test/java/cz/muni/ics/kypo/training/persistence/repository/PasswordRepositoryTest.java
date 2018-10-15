package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.Password;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class PasswordRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private PasswordRepository passwordRepository;

	private Password password1, password2;
	private String passwordHash1, passwordHash2;

	@SpringBootApplication
	static class TestConfiguration { }

	@Before
	public void setUp() {
		passwordHash1 = "1Eh9A5l7Op5As8s0h9";
		passwordHash2 = "R8a9C7B4a2c8A2cN1E";
		password1 = new Password();
		password1.setPasswordHash(passwordHash1);
		password2 = new Password();
		password2.setPasswordHash(passwordHash2);
	}

	@Test
	public void findById() {
		Long id = entityManager.persist(password2).getId();
		Optional<Password> optionalPassword = passwordRepository.findById(id);
		assertTrue(optionalPassword.isPresent());
		assertEquals(password2, optionalPassword.get());
	}

	@Test
	public void findAll() {
		entityManager.persist(password2);
		entityManager.persist(password1);
		List<Password> resultPasswords = passwordRepository.findAll();
		assertNotNull(resultPasswords);
		assertEquals(2, resultPasswords.size());
		assertTrue(resultPasswords.contains(password1));
		assertTrue(resultPasswords.contains(password2));
	}

	@Test
	public void findOneByPasswordHash() {
		entityManager.persist(password1);
		entityManager.persist(password2);
		Optional<Password> optionalPassword = passwordRepository.findOneByPasswordHash(passwordHash1);
		assertTrue(optionalPassword.isPresent());
		assertEquals(password1, optionalPassword.get());
	}

	@Test
	public void findOneByPasswordHash_unpresent_passwordHash() {
		Optional<Password> optionalPassword = passwordRepository.findOneByPasswordHash("8W93invalid987As52s");
		assertFalse(optionalPassword.isPresent());
	}
}
