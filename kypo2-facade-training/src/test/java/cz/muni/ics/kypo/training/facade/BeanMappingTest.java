package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoLevelMapperImpl.class, SnapshotHookMapperImpl.class, TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class})
public class BeanMappingTest {

    @Autowired
    private InfoLevelMapperImpl infoLevelMapper;
    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;

    private TrainingDefinition tD;
    private TrainingDefinitionDTO tDDTO;
    private UserRef aR;
    private UserRefDTO aRDTO;
    private InfoLevel iL1, iL2;
    private InfoLevelDTO iLDTO1, iLDTO2;
    private List<InfoLevel> levels;
    private List<InfoLevelDTO> levelsDTO;

    @Before
    public void init() {

        aR = new UserRef();
        aR.setId(1L);
        aR.setUserRefLogin("login");
        aR.setTrainingDefinitions(new HashSet<>(Arrays.asList(tD)));

        aRDTO = new UserRefDTO();
        aRDTO.setId(1L);
        aRDTO.setUserRefLogin("login");

        tD = new TrainingDefinition();
        tD.setId(1L);
        tD.setTitle("TrainingDefinition");
        tD.setDescription("description");
        tD.setPrerequisities(new String[]{"p1", "p2"});
        tD.setOutcomes(new String[]{"o1", "o2"});
        tD.setState(TDState.RELEASED);
        tD.setAuthors(new HashSet<>(Arrays.asList(aR)));
        tD.setSandboxDefinitionRefId(1L);
        tD.setStartingLevel(1L);
        tD.setShowStepperBar(true);

        tDDTO = new TrainingDefinitionDTO();
        tDDTO.setId(1L);
        tDDTO.setTitle("TrainingDefinition");
        tDDTO.setDescription("description");
        tDDTO.setPrerequisities(new String[]{"p1", "p2"});
        tDDTO.setOutcomes(new String[]{"o1", "o2"});
        tDDTO.setState(TDState.RELEASED);
        tDDTO.setAuthors(new HashSet<>(Arrays.asList(aRDTO)));
        tDDTO.setSandboxDefinitionRefId(1L);
        tDDTO.setStartingLevel(1L);
        tDDTO.setShowStepperBar(true);

        iL1 = new InfoLevel();
        iL1.setId(1L);
        iL1.setContent("content1");
        iL1.setMaxScore(10);
        iL1.setTitle("title1");

        iL2 = new InfoLevel();
        iL2.setId(2L);
        iL2.setContent("content2");
        iL2.setMaxScore(9);
        iL2.setTitle("title2");

        iLDTO1 = new InfoLevelDTO();
        iLDTO1.setId(1L);
        iLDTO1.setContent("content1");
        iLDTO1.setMaxScore(10);
        iLDTO1.setTitle("title1");

        iLDTO2 = new InfoLevelDTO();
        iLDTO2.setId(2L);
        iLDTO2.setContent("content2");
        iLDTO2.setMaxScore(9);
        iLDTO2.setTitle("title2");

        levels = new ArrayList<>();
        levels.add(iL1);
        levels.add(iL2);

        levelsDTO = new ArrayList<>();
        levelsDTO.add(iLDTO1);
        levelsDTO.add(iLDTO2);
    }

    @Test
    public void testMapEntityToDTO() {
        TrainingDefinitionDTO dto = trainingDefinitionMapper.mapToDTO(tD);

        assertEquals(tD.getId(), dto.getId());
        assertEquals(tD.getTitle(), dto.getTitle());
        assertEquals(tD.getDescription(), dto.getDescription());
        assertEquals(tD.getPrerequisities()[0], dto.getPrerequisities()[0]);
        assertEquals(tD.getPrerequisities()[1], dto.getPrerequisities()[1]);
        assertEquals(tD.getOutcomes()[0], dto.getOutcomes()[0]);
        assertEquals(tD.getOutcomes()[1], dto.getOutcomes()[1]);
        assertEquals(tD.getState(), dto.getState());
        assertEquals(tD.getAuthors().size(), dto.getAuthors().size());
        assertEquals(tD.getAuthors().size(), dto.getAuthors().size());
        assertEquals(tD.getSandboxDefinitionRefId(), dto.getSandboxDefinitionRefId());
        assertEquals(tD.getStartingLevel(), dto.getStartingLevel());
        assertEquals(tD.isShowStepperBar(), dto.isShowStepperBar());
    }

    @Test
    public void testMapDTOToEntity() {
        TrainingDefinition tD = trainingDefinitionMapper.mapToEntity(tDDTO);
        System.out.println(tD.toString());

        assertEquals(tDDTO.getId(), tD.getId());
        assertEquals(tDDTO.getTitle(), tD.getTitle());
        assertEquals(tDDTO.getDescription(), tD.getDescription());
        assertEquals(tDDTO.getPrerequisities()[0], tD.getPrerequisities()[0]);
        assertEquals(tDDTO.getPrerequisities()[1], tD.getPrerequisities()[1]);
        assertEquals(tDDTO.getOutcomes()[0], tD.getOutcomes()[0]);
        assertEquals(tDDTO.getOutcomes()[1], tD.getOutcomes()[1]);
        assertEquals(tDDTO.getState(), tD.getState());
        assertEquals(tDDTO.getAuthors().size(), tD.getAuthors().size());
        assertEquals(tDDTO.getAuthors().size(), tD.getAuthors().size());
        assertEquals(tDDTO.getSandboxDefinitionRefId(), tD.getSandboxDefinitionRefId());
        assertEquals(tDDTO.getStartingLevel(), tD.getStartingLevel());
        assertEquals(tDDTO.isShowStepperBar(), tD.isShowStepperBar());
    }

    @Test
    public void testMapListOfEntitiesToListOfDTO() {
        List<InfoLevelDTO> dtos = infoLevelMapper.mapToListDTO(levels);

        assertEquals(iL1.getId(), dtos.get(0).getId());
        assertEquals(iL1.getContent(), dtos.get(0).getContent());
        assertEquals(iL1.getMaxScore(), dtos.get(0).getMaxScore());
        assertEquals(iL1.getTitle(), dtos.get(0).getTitle());
        assertEquals(iL2.getId(), dtos.get(1).getId());
        assertEquals(iL2.getContent(), dtos.get(1).getContent());
        assertEquals(iL2.getMaxScore(), dtos.get(1).getMaxScore());
        assertEquals(iL2.getTitle(), dtos.get(1).getTitle());
    }

    @Test
    public void testMapListOfDTOToListOfEntities() {
        List<InfoLevel> infoLevelList = infoLevelMapper.mapToList(levelsDTO);

        assertEquals(iLDTO1.getId(), infoLevelList.get(0).getId());
        assertEquals(iLDTO1.getContent(), infoLevelList.get(0).getContent());
        assertEquals(iLDTO1.getMaxScore(), infoLevelList.get(0).getMaxScore());
        assertEquals(iLDTO1.getTitle(), infoLevelList.get(0).getTitle());
        assertEquals(iLDTO2.getId(), infoLevelList.get(1).getId());
        assertEquals(iLDTO2.getContent(), infoLevelList.get(1).getContent());
        assertEquals(iLDTO2.getMaxScore(), infoLevelList.get(1).getMaxScore());
        assertEquals(iLDTO2.getTitle(), infoLevelList.get(1).getTitle());
    }

    @Test
    public void testMapPageToDTO() {
        Page p = new PageImpl<>(levels);
        Page pDTO = infoLevelMapper.mapToPageDTO(p);
        InfoLevelDTO iLDTO1 = (InfoLevelDTO) pDTO.getContent().get(0);
        InfoLevelDTO iLDTO2 = (InfoLevelDTO) pDTO.getContent().get(1);

        assertEquals(pDTO.getTotalElements(), p.getTotalElements());
        assertEquals(iLDTO1.getTitle(), iL1.getTitle());
        assertEquals(iLDTO1.getId(), iL1.getId());
        assertEquals(iLDTO1.getContent(), iL1.getContent());
        assertEquals(iLDTO1.getMaxScore(), iL1.getMaxScore());
        assertEquals(iLDTO2.getTitle(), iL2.getTitle());
        assertEquals(iLDTO2.getId(), iL2.getId());
        assertEquals(iLDTO2.getContent(), iL2.getContent());
        assertEquals(iLDTO2.getMaxScore(), iL2.getMaxScore());
    }

    @Test
    public void testMapDTOPageToEntity() {
        Page pDTO = new PageImpl<>(levelsDTO);
        Page p = infoLevelMapper.mapToPage(pDTO);
        InfoLevel iL1 = (InfoLevel) p.getContent().get(0);
        InfoLevel iL2 = (InfoLevel) p.getContent().get(1);

        assertEquals(p.getTotalElements(), pDTO.getTotalElements());
        assertEquals(iL1.getTitle(), iLDTO1.getTitle());
        assertEquals(iL1.getId(), iLDTO1.getId());
        assertEquals(iL1.getContent(), iLDTO1.getContent());
        assertEquals(iL1.getMaxScore(), iLDTO1.getMaxScore());
        assertEquals(iL2.getTitle(), iLDTO2.getTitle());
        assertEquals(iL2.getId(), iLDTO2.getId());
        assertEquals(iL2.getContent(), iLDTO2.getContent());
        assertEquals(iL2.getMaxScore(), iLDTO2.getMaxScore());
    }

    @Test
    public void testMapEntityToOptional() {
        Optional<InfoLevelDTO> dto = infoLevelMapper.mapToOptional(iL1);

        assertTrue(dto.isPresent());
        assertEquals(iL1.getId(), dto.get().getId());
        assertEquals(iL1.getTitle(), dto.get().getTitle());
        assertEquals(iL1.getContent(), dto.get().getContent());
        assertEquals(iL1.getMaxScore(), dto.get().getMaxScore());
    }

    @Test
    public void testMapDTOToOptional() {
        Optional<InfoLevel> iL = infoLevelMapper.mapToOptional(iLDTO1);

        assertTrue(iL.isPresent());
        assertEquals(iLDTO1.getId(), iL.get().getId());
        assertEquals(iLDTO1.getTitle(), iL.get().getTitle());
        assertEquals(iLDTO1.getContent(), iL.get().getContent());
        assertEquals(iLDTO1.getMaxScore(), iL.get().getMaxScore());
    }

    @Test
    public void testMapToPageResultDTO() {
        Page p = new PageImpl<InfoLevel>(levels);
        PageResultResource<InfoLevelDTO> pRR = infoLevelMapper.mapToPageResultResource(p);
        InfoLevelDTO dto1 = pRR.getContent().get(0);
        InfoLevelDTO dto2 = pRR.getContent().get(1);

        assertEquals(pRR.getPagination().getNumberOfElements(), p.getNumberOfElements());
        assertEquals(dto1.getId(), iL1.getId());
        assertEquals(dto1.getTitle(), iL1.getTitle());
        assertEquals(dto1.getContent(), iL1.getContent());
        assertEquals(dto1.getMaxScore(), iL1.getMaxScore());
        assertEquals(dto2.getId(), iL2.getId());
        assertEquals(dto2.getTitle(), iL2.getTitle());
        assertEquals(dto2.getContent(), iL2.getContent());
        assertEquals(dto2.getMaxScore(), iL2.getMaxScore());
    }

    @Test
    public void testMapEntityToDTOSet() {
        Set<InfoLevelDTO> dtos = infoLevelMapper.mapToSetDTO(levels);
        InfoLevelDTO dto1 = (InfoLevelDTO) dtos.toArray()[0];
        InfoLevelDTO dto2 = (InfoLevelDTO) dtos.toArray()[1];

        assertEquals(dtos.size(), levels.size());
        assertEquals(dto1.getId(), iL1.getId());
        assertEquals(dto1.getTitle(), iL1.getTitle());
        assertEquals(dto1.getContent(), iL1.getContent());
        assertEquals(dto1.getMaxScore(), iL1.getMaxScore());
        assertEquals(dto2.getId(), iL2.getId());
        assertEquals(dto2.getTitle(), iL2.getTitle());
        assertEquals(dto2.getContent(), iL2.getContent());
        assertEquals(dto2.getMaxScore(), iL2.getMaxScore());
    }

    @Test
    public void testMapDTOToEntitySet() {
        Set<InfoLevel> levels = infoLevelMapper.mapToSet(levelsDTO);
        InfoLevel level1 = (InfoLevel) levels.toArray()[1];
        InfoLevel level2 = (InfoLevel) levels.toArray()[0];

        assertEquals(levels.size(), levelsDTO.size());
        assertEquals(level1.getId(), iLDTO1.getId());
        assertEquals(level1.getTitle(), iLDTO1.getTitle());
        assertEquals(level1.getContent(), iLDTO1.getContent());
        assertEquals(level1.getMaxScore(), iLDTO1.getMaxScore());
        assertEquals(level2.getId(), iLDTO2.getId());
        assertEquals(level2.getTitle(), iLDTO2.getTitle());
        assertEquals(level2.getContent(), iLDTO2.getContent());
        assertEquals(level2.getMaxScore(), iLDTO2.getMaxScore());
    }
}
