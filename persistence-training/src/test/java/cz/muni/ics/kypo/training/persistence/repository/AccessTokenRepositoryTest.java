package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.AccessToken;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
public class AccessTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    private AccessToken password1, password2;

    @BeforeEach
    public void setUp() {
        password1 = testDataFactory.getAccessToken1();
        password2 = testDataFactory.getAccessToken2();
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
        Optional<AccessToken> optionalPassword = accessTokenRepository.findOneByAccessToken(password1.getAccessToken());
        assertTrue(optionalPassword.isPresent());
        assertEquals(password1, optionalPassword.get());
    }

    @Test
    public void findOneByPasswordHashUnpresentPasswordHash() {
        Optional<AccessToken> optionalPassword = accessTokenRepository.findOneByAccessToken("word-1111");
        assertFalse(optionalPassword.isPresent());
    }
}
