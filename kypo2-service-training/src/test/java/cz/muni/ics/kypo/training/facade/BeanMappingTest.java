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
@SpringBootTest(classes = {TrainingDefinitionMapperImpl.class, UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class})
public class BeanMappingTest {

    @Autowired
    private TestDataFactory testDataFactory;
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

        assertEquals(tDDTO.getId(), tD.getId());
        assertEquals(tDDTO.getTitle(), tD.getTitle());
        assertEquals(tDDTO.getDescription(), tD.getDescription());
        assertEquals(tDDTO.getPrerequisities()[0], tD.getPrerequisities()[0]);
        assertEquals(tDDTO.getPrerequisities()[1], tD.getPrerequisities()[1]);
        assertEquals(tDDTO.getOutcomes()[0], tD.getOutcomes()[0]);
        assertEquals(tDDTO.getOutcomes()[1], tD.getOutcomes()[1]);
        assertEquals(tDDTO.isShowStepperBar(), tD.isShowStepperBar());
    }

}
