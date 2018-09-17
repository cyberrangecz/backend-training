package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.model.AssessmentLevel;
import cz.muni.ics.kypo.training.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.repository.AssessmentLevelRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.training.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.training.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.training.service"})
public class AssessmentLevelServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private AssessmentLevelService assessmentLevelService;

    @MockBean
    private AssessmentLevelRepository assessmentLevelRepository;

    @MockBean
    private RestTemplate restTemplate;

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

    private void deepEquals(AssessmentLevel expectedAssessmentLevel, AssessmentLevel actualAssessmentLevel) {
        assertEquals(expectedAssessmentLevel.getId(), actualAssessmentLevel.getId());
        assertEquals(expectedAssessmentLevel.getAssessmentType(), actualAssessmentLevel.getAssessmentType());
    }
}
