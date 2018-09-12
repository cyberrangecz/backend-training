package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.config.FacadeConfigTest;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(FacadeConfigTest.class)
public class TrainingInstanceFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private TrainingInstanceFacade trainingInstanceFacade;

    @MockBean
    private TrainingInstanceService trainingInstanceService;

    private TrainingInstance trainingInstance1, trainingInstance2;

    @SpringBootApplication
    static class TestConfiguration {
    }

    @Before
    public void init() {
        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTitle("test");

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTitle("test");
    }

    @Test
    public void findTrainingInstanceById() {
        given(trainingInstanceService.findById(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));

        TrainingInstanceDTO trainingInstanceDTO = trainingInstanceFacade.findById(trainingInstance1.getId());
        deepEquals(trainingInstance1, trainingInstanceDTO);

        then(trainingInstanceService).should().findById(trainingInstance1.getId());
    }

    @Test
    public void findNonexistentTrainingInstanceById() {
        Long id = 6L;
        given(trainingInstanceService.findById(id)).willReturn(Optional.empty());
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.findById(id);
    }

    @Test
    public void findAllTrainingInstances() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page<TrainingInstance> p = new PageImpl<TrainingInstance>(expected);

        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceService.findAll(any(Predicate.class), any (Pageable.class))).willReturn(p);

        PageResultResource<TrainingInstanceDTO> trainingInstanceDTO = trainingInstanceFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEquals(trainingInstance1, trainingInstanceDTO.getContent().get(0));
        deepEquals(trainingInstance2, trainingInstanceDTO.getContent().get(1));

        then(trainingInstanceService).should().findAll(predicate, PageRequest.of(0,2));
    }

    @Test
    public void createTrainingInstance() {
        given(trainingInstanceService.create(trainingInstance1)).willReturn(Optional.of(trainingInstance1));
        TrainingInstanceDTO trainingInstanceDTO = trainingInstanceFacade.create(trainingInstance1);
        deepEquals(trainingInstance1, trainingInstanceDTO);
        then(trainingInstanceService).should().create(trainingInstance1);
    }

    @Test
    public void createTrainingInstanceWithNull() {
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.create(null);
    }

    @Test
    public void deleteTrainingInstanceWithNull() {
        thrown.expect(NullPointerException.class);
        trainingInstanceFacade.delete(null);
    }

    private void deepEquals(TrainingInstance expected, TrainingInstanceDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }


}
