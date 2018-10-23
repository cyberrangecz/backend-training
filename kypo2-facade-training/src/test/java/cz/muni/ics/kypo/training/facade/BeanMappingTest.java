package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.AuthorRefDTO;
import cz.muni.ics.kypo.training.api.dto.SandboxDefinitionRefDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.config.FacadeConfigTest;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.persistence.model.AuthorRef;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.SandboxDefinitionRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	@Test
	public void testMapListOfEntitiesToListOfDTO() {
		List<InfoLevel> infoLevelsList = new ArrayList<>();

		InfoLevel infoLevel1 = new InfoLevel();
		infoLevel1.setId(1L);
		infoLevel1.setContent("content1");
		infoLevel1.setMaxScore(10);
		infoLevel1.setTitle("title1");

		InfoLevel infoLevel2 = new InfoLevel();
		infoLevel2.setId(2L);
		infoLevel2.setContent("content2");
		infoLevel2.setMaxScore(9);
		infoLevel2.setTitle("title2");

		infoLevelsList.add(infoLevel1);
		infoLevelsList.add(infoLevel2);
		List<InfoLevelDTO> dtos = beanMapping.mapTo(infoLevelsList, InfoLevelDTO.class);

		assertEquals(infoLevel1.getId(), dtos.get(0).getId());
		assertEquals(infoLevel1.getContent(), dtos.get(0).getContent());
		assertEquals(infoLevel1.getMaxScore(), dtos.get(0).getMaxScore());
		assertEquals(infoLevel1.getTitle(), dtos.get(0).getTitle());

		assertEquals(infoLevel2.getId(), dtos.get(1).getId());
		assertEquals(infoLevel2.getContent(), dtos.get(1).getContent());
		assertEquals(infoLevel2.getMaxScore(), dtos.get(1).getMaxScore());
		assertEquals(infoLevel2.getTitle(), dtos.get(1).getTitle());
	}

	@Test
	public void testMapListOfDTOToListOfEntities() {
		List<InfoLevelDTO> listOfDTO = new ArrayList<>();

		InfoLevelDTO infoLevelDTO1 = new InfoLevelDTO();
		infoLevelDTO1.setId(1L);
		infoLevelDTO1.setContent("content1");
		infoLevelDTO1.setMaxScore(10);
		infoLevelDTO1.setTitle("title1");

		InfoLevelDTO infoLevelDTO2 = new InfoLevelDTO();
		infoLevelDTO2.setId(2L);
		infoLevelDTO2.setContent("content2");
		infoLevelDTO2.setMaxScore(9);
		infoLevelDTO2.setTitle("title2");

		listOfDTO.add(infoLevelDTO1);
		listOfDTO.add(infoLevelDTO2);
		List<InfoLevel> infoLevelList = beanMapping.mapTo(listOfDTO, InfoLevel.class);

		assertEquals(infoLevelDTO1.getId(), infoLevelList.get(0).getId());
		assertEquals(infoLevelDTO1.getContent(), infoLevelList.get(0).getContent());
		assertEquals(infoLevelDTO1.getMaxScore(), infoLevelList.get(0).getMaxScore());
		assertEquals(infoLevelDTO1.getTitle(), infoLevelList.get(0).getTitle());

		assertEquals(infoLevelDTO2.getId(), infoLevelList.get(1).getId());
		assertEquals(infoLevelDTO2.getContent(), infoLevelList.get(1).getContent());
		assertEquals(infoLevelDTO2.getMaxScore(), infoLevelList.get(1).getMaxScore());
		assertEquals(infoLevelDTO2.getTitle(), infoLevelList.get(1).getTitle());
	}

	@Test
	public void testMappingPageToDTO(){
		InfoLevel infoLevel1 = new InfoLevel();
		infoLevel1.setId(1L);
		infoLevel1.setContent("content1");
		infoLevel1.setMaxScore(10);
		infoLevel1.setTitle("title1");

		InfoLevel infoLevel2 = new InfoLevel();
		infoLevel2.setId(2L);
		infoLevel2.setContent("content2");
		infoLevel2.setMaxScore(9);
		infoLevel2.setTitle("title2");

		List<InfoLevel> levels = new ArrayList<>();
		levels.add(infoLevel1);
		levels.add(infoLevel2);
		Page p = new PageImpl<InfoLevel>(levels);
		Page pDTO = beanMapping.mapTo(p, InfoLevelDTO.class);
		InfoLevelDTO iLDTO1 = (InfoLevelDTO) pDTO.getContent().get(0);
		InfoLevelDTO iLDTO2 = (InfoLevelDTO) pDTO.getContent().get(1);

		assertEquals(pDTO.getTotalElements(), p.getTotalElements());
		assertEquals(iLDTO1.getTitle(), infoLevel1.getTitle());
		assertEquals(iLDTO1.getId(), infoLevel1.getId());
		assertEquals(iLDTO1.getContent(), infoLevel1.getContent());
		assertEquals(iLDTO1.getMaxScore(), infoLevel1.getMaxScore());
		assertEquals(iLDTO2.getTitle(), infoLevel2.getTitle());
		assertEquals(iLDTO2.getId(), infoLevel2.getId());
		assertEquals(iLDTO2.getContent(), infoLevel2.getContent());
		assertEquals(iLDTO2.getMaxScore(), infoLevel2.getMaxScore());
	}

	@Test
	public void testMappingDTOPageToEntity(){
		InfoLevelDTO infoLevel1 = new InfoLevelDTO();
		infoLevel1.setId(1L);
		infoLevel1.setContent("content1");
		infoLevel1.setMaxScore(10);
		infoLevel1.setTitle("title1");

		InfoLevelDTO infoLevel2 = new InfoLevelDTO();
		infoLevel2.setId(2L);
		infoLevel2.setContent("content2");
		infoLevel2.setMaxScore(9);
		infoLevel2.setTitle("title2");

		List<InfoLevelDTO> levels = new ArrayList<>();
		levels.add(infoLevel1);
		levels.add(infoLevel2);
		Page pDTO = new PageImpl<InfoLevelDTO>(levels);
		Page p = beanMapping.mapTo(pDTO, InfoLevel.class);
		InfoLevel iL1 = (InfoLevel) p.getContent().get(0);
		InfoLevel iL2 = (InfoLevel) p.getContent().get(1);

		assertEquals(p.getTotalElements(), pDTO.getTotalElements());
		assertEquals(iL1.getTitle(), infoLevel1.getTitle());
		assertEquals(iL1.getId(), infoLevel1.getId());
		assertEquals(iL1.getContent(), infoLevel1.getContent());
		assertEquals(iL1.getMaxScore(), infoLevel1.getMaxScore());
		assertEquals(iL2.getTitle(), infoLevel2.getTitle());
		assertEquals(iL2.getId(), infoLevel2.getId());
		assertEquals(iL2.getContent(), infoLevel2.getContent());
		assertEquals(iL2.getMaxScore(), infoLevel2.getMaxScore());
	}

	@Test
	public void testMapEntityToOptional(){
		InfoLevel iL = new InfoLevel();
		iL.setId(1L);
		iL.setContent("content1");
		iL.setMaxScore(10);
		iL.setTitle("title1");
		Optional<InfoLevelDTO> dto = beanMapping.mapToOptional(iL, InfoLevelDTO.class);

		assertTrue(dto.isPresent());
		assertEquals(iL.getId(), dto.get().getId());
		assertEquals(iL.getTitle(), dto.get().getTitle());
		assertEquals(iL.getContent(), dto.get().getContent());
		assertEquals(iL.getMaxScore(), dto.get().getMaxScore());
	}

}
