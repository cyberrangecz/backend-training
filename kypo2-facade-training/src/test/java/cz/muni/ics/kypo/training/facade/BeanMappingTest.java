package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.AuthorRefDTO;
import cz.muni.ics.kypo.training.api.dto.SandboxDefinitionRefDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.config.FacadeConfigTest;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.persistence.model.AuthorRef;
import cz.muni.ics.kypo.training.persistence.model.SandboxDefinitionRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(FacadeConfigTest.class)
public class BeanMappingTest {

	@Autowired
	private BeanMapping beanMapping;

	@SpringBootApplication
	static class TestConfiguration {
	}

	@Test
	public void testMapEntityToDTO() {
		TrainingDefinition tD = new TrainingDefinition();
		tD.setId(1L);
		tD.setTitle("TrainingDefinition");
		tD.setDescription("description");
		tD.setPrerequisities(new String[] {"p1", "p2"});
		tD.setOutcomes(new String[] {"o1", "o2"});
		tD.setState(TDState.RELEASED);
		AuthorRef aR = new AuthorRef();
		aR.setId(1L);
		aR.setAuthorRefLogin("login");
		aR.setTrainingDefinition(new HashSet<>(Arrays.asList(tD)));
		tD.setAuthorRef(new HashSet<>(Arrays.asList(aR)));
		SandboxDefinitionRef sDR = new SandboxDefinitionRef();
		sDR.setId(1L);
		sDR.setSandboxDefinitionRef(1L);
		tD.setSandBoxDefinitionRef(sDR);
		tD.setStartingLevel(1L);
		tD.setShowStepperBar(true);

		TrainingDefinitionDTO dto = beanMapping.mapTo(tD, TrainingDefinitionDTO.class);

		assertEquals(tD.getId(), dto.getId());
		assertEquals(tD.getTitle(), dto.getTitle());
		assertEquals(tD.getDescription(), dto.getDescription());
		assertEquals(tD.getPrerequisities()[0], dto.getPrerequisities()[0]);
		assertEquals(tD.getPrerequisities()[1], dto.getPrerequisities()[1]);
		assertEquals(tD.getOutcomes()[0], dto.getOutcomes()[0]);
		assertEquals(tD.getOutcomes()[1], dto.getOutcomes()[1]);
		assertEquals(tD.getState(), dto.getState());
		assertEquals(tD.getAuthorRef().size(), dto.getAuthorRef().size());
		assertEquals(tD.getAuthorRef().size(), dto.getAuthorRef().size());
		assertEquals(tD.getSandBoxDefinitionRef().getId(), dto.getSandBoxDefinitionRef().getId());
		assertEquals(tD.getSandBoxDefinitionRef().getSandboxDefinitionRef(), dto.getSandBoxDefinitionRef().getSandboxDefinitionRef());
		assertEquals(tD.getStartingLevel(), dto.getStartingLevel());
		assertEquals(tD.isShowStepperBar(), dto.isShowStepperBar());
	}

	@Test
	public void testMapDTOToEntity() {
		TrainingDefinitionDTO dto = new TrainingDefinitionDTO();
		dto.setId(1L);
		dto.setTitle("TrainingDefinition");
		dto.setDescription("description");
		dto.setPrerequisities(new String[] {"p1", "p2"});
		dto.setOutcomes(new String[] {"o1", "o2"});
		dto.setState(TDState.RELEASED);
		AuthorRefDTO aR = new AuthorRefDTO();
		aR.setId(1L);
		aR.setAuthorRefLogin("login");
		dto.setAuthorRef(new HashSet<>(Arrays.asList(aR)));
		SandboxDefinitionRefDTO sDR = new SandboxDefinitionRefDTO();
		sDR.setId(1L);
		sDR.setSandboxDefinitionRef(1L);
		dto.setSandBoxDefinitionRef(sDR);
		dto.setStartingLevel(1L);
		dto.setShowStepperBar(true);

		TrainingDefinition tD = beanMapping.mapTo(dto, TrainingDefinition.class);

		assertEquals(dto.getId(), tD.getId());
		assertEquals(dto.getTitle(), tD.getTitle());
		assertEquals(dto.getDescription(), tD.getDescription());
		assertEquals(dto.getPrerequisities()[0], tD.getPrerequisities()[0]);
		assertEquals(dto.getPrerequisities()[1], tD.getPrerequisities()[1]);
		assertEquals(dto.getOutcomes()[0], tD.getOutcomes()[0]);
		assertEquals(dto.getOutcomes()[1], tD.getOutcomes()[1]);
		assertEquals(dto.getState(), tD.getState());
		assertEquals(dto.getAuthorRef().size(), tD.getAuthorRef().size());
		assertEquals(dto.getAuthorRef().size(), tD.getAuthorRef().size());
		assertEquals(dto.getSandBoxDefinitionRef().getId(), tD.getSandBoxDefinitionRef().getId());
		assertEquals(dto.getSandBoxDefinitionRef().getSandboxDefinitionRef(), tD.getSandBoxDefinitionRef().getSandboxDefinitionRef());
		assertEquals(dto.getStartingLevel(), tD.getStartingLevel());
		assertEquals(dto.isShowStepperBar(), tD.isShowStepperBar());
	}

}
