package cz.muni.ics.kypo.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.api.PageResultResource;
import cz.muni.ics.kypo.api.dto.TrainingRunDTO;
import cz.muni.ics.kypo.config.FacadeTestConfiguration;
import cz.muni.ics.kypo.exception.FacadeLayerException;
import cz.muni.ics.kypo.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.model.TrainingRun;
import cz.muni.ics.kypo.model.enums.TRState;
import cz.muni.ics.kypo.service.TrainingRunService;
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
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@EntityScan(basePackages = {"cz.muni.ics.kypo.model"})
@EnableJpaRepositories(basePackages = {"cz.muni.ics.kypo.repository"})
@ComponentScan(basePackages = {"cz.muni.ics.kypo.facade", "cz.muni.ics.kypo.service", "cz.muni.ics.kypo.mapping"})
@Import(FacadeTestConfiguration.class)
public class TrainingRunFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TrainingRunFacade trainingRunFacade;

    @MockBean
    private TrainingRunService trainingRunService;

    private TrainingRun trainingRun1, trainingRun2;

    @SpringBootApplication
    static class TestConfiguration {
    }


    @Before
    public void init() {
        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setState(TRState.READY);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setState(TRState.ARCHIVED);
    }

    @Test
    public void findTrainingRunById() {
        given(trainingRunService.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));

        TrainingRunDTO trainingRunDTO = trainingRunFacade.findById(trainingRun1.getId());
        deepEquals(trainingRun1, trainingRunDTO);

        then(trainingRunService).should().findById(trainingRun1.getId());
    }

    @Test
    public void findNonexistentTrainingRunById() {
        Long id = 6L;
        given(trainingRunService.findById(id)).willReturn(Optional.empty());
        thrown.expect(FacadeLayerException.class);
        trainingRunFacade.findById(id);
    }

    @Test
    public void findAllTrainingRuns() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page p = new PageImpl<TrainingRun>(expected);

        PathBuilder<TrainingRun> tR = new PathBuilder<TrainingRun>(TrainingRun.class, "trainingRun");
        Predicate predicate = tR.isNotNull();

        given(trainingRunService.findAll(any(Predicate.class), any (Pageable.class))).willReturn(p);

        PageResultResource<TrainingRunDTO> trainingRunDTO = trainingRunFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEquals(trainingRun1, trainingRunDTO.getContent().get(0));
        deepEquals(trainingRun2, trainingRunDTO.getContent().get(1));

        then(trainingRunService).should().findAll(predicate, PageRequest.of(0,2));
    }

    private void deepEquals(TrainingRun expected, TrainingRunDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getState(), actual.getState());

    }

}

