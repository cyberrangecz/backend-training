package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.Hint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class HintRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HintRepository hintRepository;

    private Hint hint1, hint2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        hint1 = new Hint();
        hint1.setTitle("Basic hint");
        hint1.setContent("content is not null");
        hint1.setHintPenalty(5);
        hint2 = new Hint();
        hint2.setTitle("Basic hint 2");
        hint2.setContent("content is not null");
        hint2.setHintPenalty(2);
    }

    @Test
    public void findById() throws Exception {
        long id = entityManager.persist(hint1).getId();
        Optional<Hint> hintOptional = hintRepository.findById(id);
        assertThat(hintOptional.isPresent());
        assertEquals(hint1, hintOptional.get());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void findById_nullableArgument() throws Exception {
        entityManager.persist(hint1);
        Optional<Hint> hintOptional = hintRepository.findById(null);
    }

    @Test
    public void findAll() {
        List<Hint> expectedHints = Arrays.asList(hint1, hint2);
        expectedHints.stream().forEach(h -> entityManager.persist(h));
        List<Hint> resultHints = hintRepository.findAll();
        assertNotNull(resultHints);
        assertEquals(expectedHints, resultHints);
        assertEquals(2, resultHints.size());
    }

    @Test
    public void findAll_emptyDatabase() {
        List<Hint> expectedHints = new ArrayList<>();
        List<Hint> resultHints = hintRepository.findAll();
        assertNotNull(resultHints);
        assertEquals(expectedHints, resultHints);
        assertEquals(0, resultHints.size());
    }
}
