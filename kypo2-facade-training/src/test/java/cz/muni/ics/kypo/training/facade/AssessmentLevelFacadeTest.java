package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.config.FacadeTestConfiguration;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.service.AssessmentLevelService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.training.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.facade", "cz.muni.ics.kypo.training.service", "cz.muni.ics.kypo.training.mapping"})
@Import(FacadeTestConfiguration.class)
public class AssessmentLevelFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private AssessmentLevelFacade assessmentLevelFacade;

    @MockBean
    private AssessmentLevelService assessmentLevelService;

    private AssessmentLevel al1, al2;


    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        al1 = new AssessmentLevel();
        al1.setId(1L);
        al1.setNextLevel(2L);
        al1.setAssessmentType(AssessmentType.TEST);
        al1.setTitle("Test1");

        al2 = new AssessmentLevel();
        al2.setId(2L);
        al2.setNextLevel(3L);
        al2.setAssessmentType(AssessmentType.TEST);
        al2.setTitle("Test2");

    }

    @Test
    public void findByIdAssessmentLevel() {
        given(assessmentLevelService.findById(al1.getId())).willReturn(Optional.of(al1));

        AssessmentLevelDTO alDTO = assessmentLevelFacade.findById(al1.getId());
        deepEquals(al1,alDTO);

        then(assessmentLevelService).should().findById(al1.getId());
    }

    @Test
    public void findByIdWithNullId() {
        Long id = null;
        thrown.expect(FacadeLayerException.class);
        //thrown.expectMessage("Given AssessmentLevel ID is null.");
        assessmentLevelFacade.findById(id);

    }

    @Test
    public void findByIdNotFoundAssessmentLevel() {
        Long id = 3L;
        given(assessmentLevelService.findById(id)).willReturn(Optional.empty());
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("AssessmentLevel with this id is not found");
        assessmentLevelFacade.findById(id);

    }


    @Test
    public void findAll() {
        List<AssessmentLevel> expected = new ArrayList();
        expected.add(al1);
        expected.add(al2);

        Page p = new PageImpl<AssessmentLevel>(expected);

        PathBuilder<AssessmentLevel> aL = new PathBuilder<AssessmentLevel>(AssessmentLevel.class, "assessmentLevel");
        Predicate predicate = aL.isNotNull();

        given(assessmentLevelService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        PageResultResource<AssessmentLevelDTO> assessmentLevelDTOS = assessmentLevelFacade.findAll(predicate,PageRequest.of(0,2));
        deepEquals(al1, assessmentLevelDTOS.getContent().get(0));
        deepEquals(al2, assessmentLevelDTOS.getContent().get(1));

        then(assessmentLevelService).should().findAll(predicate,PageRequest.of(0,2));
    }

    @Test
    public void findAllWithServiceLayerException() {
        willThrow(ServiceLayerException.class).given(assessmentLevelService).findAll(any(Predicate.class),any(Pageable.class));
        thrown.expect(FacadeLayerException.class);

        PathBuilder<AssessmentLevel> aL = new PathBuilder<AssessmentLevel>(AssessmentLevel.class, "assessmentLevel");
        Predicate predicate = aL.isNotNull();

        PageResultResource<AssessmentLevelDTO> assessmentLevelDTOS = assessmentLevelFacade.findAll(predicate,PageRequest.of(0,2));
    }

    @Test
    public void createAssessmentLevel() {

        given(assessmentLevelService.create(al1)).willReturn(Optional.of(al1));
        AssessmentLevelDTO alDTO = assessmentLevelFacade.create(al1);
        deepEquals(al1, alDTO);
        then(assessmentLevelService).should().create(al1);

    }

    @Test
    public void createAssessmentLevelWithNull() {
        thrown.expect(FacadeLayerException.class);
        assessmentLevelFacade.create(null);
    }

    @Test
    public void createAssessmentLevelWithServiceLayerException() {
        willThrow(ServiceLayerException.class).given(assessmentLevelService).create(al1);
        thrown.expect(FacadeLayerException.class);
        assessmentLevelFacade.create(al1);
    }
    /*
    @Test
    public void updateAssessmentLevel() {
        assessmentLevelFacade.update(al1);
        then(assessmentLevelService).should().update(al1);
    }

    @Test
    public void updateAssessmentLevelWithNull() {
        thrown.expect(FacadeLayerException.class);
        assessmentLevelFacade.update(null);
    }

    @Test
    public void updateAssessmentLevelWithServiceLayerException() {
        willThrow(ServiceLayerException.class).given(assessmentLevelService).update(al1);
        thrown.expect(FacadeLayerException.class);
        assessmentLevelFacade.update(al1);
    }
    */


    private void deepEquals(AssessmentLevel expectedAssessmentLevel, AssessmentLevelDTO actualAssessmentLevel) {
        assertEquals(expectedAssessmentLevel.getId(), actualAssessmentLevel.getId());
        assertEquals(expectedAssessmentLevel.getAssessmentType(), actualAssessmentLevel.getType());
    }

}
