package cz.muni.ics.kypo.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.AssessmentLevel;
import cz.muni.ics.kypo.model.enums.AssessmentType;
import cz.muni.ics.kypo.repository.AssessmentLevelRepository;
import cz.muni.ics.kypo.service.AssessmentLevelService;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo"})
public class AssessmentLevelServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private AssessmentLevelService assessmentLevelService;

    @MockBean
    private AssessmentLevelRepository assessmentLevelRepository;

    private AssessmentLevel assessmentLevel1, assessmentLevel2;


    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        assessmentLevel1 = new AssessmentLevel();
        assessmentLevel1.setId(1L);
        assessmentLevel1.setNextLevel(2L);
        assessmentLevel1.setAssessmentType(AssessmentType.TEST);
        assessmentLevel1.setTitle("Test");

        assessmentLevel2 = new AssessmentLevel();
        assessmentLevel2.setId(2L);
        assessmentLevel2.setNextLevel(3L);
        assessmentLevel2.setAssessmentType(AssessmentType.QUESTIONNAIRE);
        assessmentLevel2.setTitle("Questionnaire");

    }

    @Test
    public void findByIdAssessmentLevel() {
        given(assessmentLevelRepository.findById(assessmentLevel1.getId())).willReturn(Optional.of(assessmentLevel1));

        AssessmentLevel al = assessmentLevelService.findById(assessmentLevel1.getId()).get();
        deepEquals(assessmentLevel1,al);

        then(assessmentLevelRepository).should().findById(assessmentLevel1.getId());
    }

    @Test
    public void findByIdHibernateException() {
        Long id = 3L;
        willThrow(HibernateException.class).given(assessmentLevelRepository).findById(id);
        //thrown.expectMessage("Error while loading assessment level with id: " + id);
        thrown.expect(ServiceLayerException.class);
        assessmentLevelService.findById(id);

    }
    @Test
    public void findByIdNotFoundAssessmentLevel() {
        Long id = 3L;
        assertEquals(Optional.empty(), assessmentLevelService.findById(id));


    }


    @Test
    public void findAll() {
        List<AssessmentLevel> expected = new ArrayList();
        expected.add(assessmentLevel1);
        expected.add(assessmentLevel2);

        Page p = new PageImpl<AssessmentLevel>(expected);
        PathBuilder<AssessmentLevel> aL = new PathBuilder<AssessmentLevel>(AssessmentLevel.class, "assessmentLevel");
        Predicate predicate = aL.isNotNull();


        given(assessmentLevelRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = assessmentLevelService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void createAssessmentLevel() {

        given(assessmentLevelRepository.save(assessmentLevel1)).willReturn(assessmentLevel1);
        AssessmentLevel al = assessmentLevelService.create(assessmentLevel1).get();
        deepEquals(assessmentLevel1, al);
        then(assessmentLevelRepository).should().save(assessmentLevel1);

    }

    @Test
    public void createAssessmentLevelWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input assessment level must not be null");
        assessmentLevelService.create(null);
    }

    @Test
    public void updateAssessmentLevel() {
        given(assessmentLevelRepository.saveAndFlush(assessmentLevel1)).willReturn(assessmentLevel1);
        AssessmentLevel al = assessmentLevelService.update(assessmentLevel1).get();
        deepEquals(assessmentLevel1, al);
        then(assessmentLevelRepository).should().saveAndFlush(assessmentLevel1);
    }

    @Test
    public void updateAssessmentLevelWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input assessment level must not be null");
        assessmentLevelService.update(null);
    }

    @Test
    public void deleteAssessmentLevelWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input assessment level must not be null");
        assessmentLevelService.delete(null);
    }

    private void deepEquals(AssessmentLevel expectedAssessmentLevel, AssessmentLevel actualAssessmentLevel) {
        assertEquals(expectedAssessmentLevel.getId(), actualAssessmentLevel.getId());
        assertEquals(expectedAssessmentLevel.getAssessmentType(), actualAssessmentLevel.getAssessmentType());
    }



}
