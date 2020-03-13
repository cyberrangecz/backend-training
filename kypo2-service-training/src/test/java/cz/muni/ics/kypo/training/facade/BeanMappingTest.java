package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
@SpringBootTest(classes = {InfoLevelMapperImpl.class, TrainingDefinitionMapperImpl.class, UserRefMapperImpl.class,
        BetaTestingGroupMapperImpl.class, AttachmentMapperImpl.class})
public class BeanMappingTest {

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private InfoLevelMapperImpl infoLevelMapper;
    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;

    private TrainingDefinition tD;
    private TrainingDefinitionByIdDTO tDDTO;
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
        aR.setTrainingDefinitions(new HashSet<>(Arrays.asList(tD)));

        aRDTO = new UserRefDTO();
        aRDTO.setUserRefId(1L);
        aRDTO.setUserRefLogin("login");

        tD = testDataFactory.getUnreleasedDefinition();
        tD.setId(1L);

        tDDTO = new TrainingDefinitionByIdDTO();
        tDDTO.setId(tD.getId());
        tDDTO.setTitle(tD.getTitle());
        tDDTO.setDescription(tD.getDescription());
        tDDTO.setPrerequisities(tD.getPrerequisities());
        tDDTO.setOutcomes(tD.getOutcomes());
        tDDTO.setState(cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED);
        tDDTO.setShowStepperBar(tD.isShowStepperBar());

        iL1 = testDataFactory.getInfoLevel1();
        iL1.setId(1L);

        iL2 = testDataFactory.getInfoLevel2();
        iL2.setId(2L);

        iLDTO1 = new InfoLevelDTO();
        iLDTO1.setId(iL1.getId());
        iLDTO1.setContent(iL1.getContent());
        iLDTO1.setMaxScore(iL1.getMaxScore());
        iLDTO1.setTitle(iL1.getTitle());

        iLDTO2 = new InfoLevelDTO();
        iLDTO2.setId(iL2.getId());
        iLDTO2.setContent(iL2.getContent());
        iLDTO2.setMaxScore(iL2.getMaxScore());
        iLDTO2.setTitle(iL2.getTitle());

        levels = new ArrayList<>();
        levels.add(iL1);
        levels.add(iL2);

        levelsDTO = new ArrayList<>();
        levelsDTO.add(iLDTO1);
        levelsDTO.add(iLDTO2);
    }

    @Test
    public void testMapEntityToDTO() {
        TrainingDefinitionByIdDTO dto = trainingDefinitionMapper.mapToDTOById(tD);

        assertEquals(tD.getId(), dto.getId());
        assertEquals(tD.getTitle(), dto.getTitle());
        assertEquals(tD.getDescription(), dto.getDescription());
        assertEquals(tD.getPrerequisities()[0], dto.getPrerequisities()[0]);
        assertEquals(tD.getPrerequisities()[1], dto.getPrerequisities()[1]);
        assertEquals(tD.getOutcomes()[0], dto.getOutcomes()[0]);
        assertEquals(tD.getOutcomes()[1], dto.getOutcomes()[1]);
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
