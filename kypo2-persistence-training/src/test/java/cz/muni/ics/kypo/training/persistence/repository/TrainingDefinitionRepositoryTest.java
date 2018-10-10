package cz.muni.ics.kypo.training.persistence.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import cz.muni.ics.kypo.training.persistence.config.PersistenceConfigTest;
import cz.muni.ics.kypo.training.persistence.model.SandboxDefinitionRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.repository.TrainingDefinitionRepository;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(PersistenceConfigTest.class)
public class TrainingDefinitionRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private TrainingDefinitionRepository trainingDefinitionRepository;

	private TrainingDefinition trainingDefinition1, trainingDefinition2, trainingDefinition3;
	private SandboxDefinitionRef sandboxDefinitionRef1, sandboxDefinitionRef2;
	private Pageable pageable;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Before
	public void init() {
		sandboxDefinitionRef1 = new SandboxDefinitionRef();
		// sandboxDefinitionRef1.setId(1L);
		sandboxDefinitionRef1.setSandboxDefinitionRef(1L);
		sandboxDefinitionRef2 = new SandboxDefinitionRef();
		// sandboxDefinitionRef2.setId(2L);
		sandboxDefinitionRef2.setSandboxDefinitionRef(2L);

		trainingDefinition1 = new TrainingDefinition();
		// trainingDefinition1.setId(1L);
		trainingDefinition1.setSandBoxDefinitionRef(entityManager.persist(sandboxDefinitionRef1));
		trainingDefinition1.setTitle("test");
		trainingDefinition1.setState(TDState.UNRELEASED);

		trainingDefinition2 = new TrainingDefinition();
		// trainingDefinition2.setId(2L);
		trainingDefinition2.setSandBoxDefinitionRef(entityManager.persist(sandboxDefinitionRef1));
		trainingDefinition2.setTitle("test");
		trainingDefinition2.setState(TDState.UNRELEASED);

		trainingDefinition3 = new TrainingDefinition();
		// trainingDefinition3.setId(3L);
		trainingDefinition3.setSandBoxDefinitionRef(entityManager.persist(sandboxDefinitionRef2));
		trainingDefinition3.setTitle("test");
		trainingDefinition3.setState(TDState.UNRELEASED);

		pageable = PageRequest.of(0, 10);
	}

	@Test
	public void FindAllBySandboxDefinitionRefId() {
		entityManager.persist(trainingDefinition1);
		entityManager.persist(trainingDefinition2);
		entityManager.persist(trainingDefinition3);

		List<TrainingDefinition> trainingDefinitions = trainingDefinitionRepository
				.findAllBySandBoxDefinitionRefId(sandboxDefinitionRef1.getSandboxDefinitionRef(), pageable).getContent();
		System.out.println(trainingDefinitions);
		assertTrue(trainingDefinitions.contains(trainingDefinition1));
		assertTrue(trainingDefinitions.contains(trainingDefinition2));
		assertEquals(2, trainingDefinitions.size());
	}

}
