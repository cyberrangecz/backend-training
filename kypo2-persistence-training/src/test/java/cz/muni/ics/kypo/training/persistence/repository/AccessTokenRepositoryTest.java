package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.AccessToken;
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
public class AccessTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    private AccessToken password1, password2;
    private String accessToken1, accessToken2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void setUp() {
        accessToken1 = "pass-1234";
        accessToken2 = "pass-5678";
        password1 = new AccessToken();
        password1.setAccessToken(accessToken1);
        password2 = new AccessToken();
        password2.setAccessToken(accessToken2);
    }

    @Test
    public void findById() {
        Long id = entityManager.persist(password2).getId();
        Optional<AccessToken> optionalPassword = accessTokenRepository.findById(id);
        assertTrue(optionalPassword.isPresent());
        assertEquals(password2, optionalPassword.get());
    }

    @Test
    public void findAll() {
        entityManager.persist(password2);
        entityManager.persist(password1);
        List<AccessToken> resultPasswords = accessTokenRepository.findAll();
        assertNotNull(resultPasswords);
        assertEquals(2, resultPasswords.size());
        assertTrue(resultPasswords.contains(password1));
        assertTrue(resultPasswords.contains(password2));
    }

    @Test
    public void findOneByAccessToken() {
        entityManager.persist(password1);
        entityManager.persist(password2);
        Optional<AccessToken> optionalPassword = accessTokenRepository.findOneByAccessToken(accessToken1);
        assertTrue(optionalPassword.isPresent());
        assertEquals(password1, optionalPassword.get());
    }

    @Test
    public void findOneByPasswordHash_unpresent_passwordHash() {
        Optional<AccessToken> optionalPassword = accessTokenRepository.findOneByAccessToken("word-1111");
        assertFalse(optionalPassword.isPresent());
    }
}
