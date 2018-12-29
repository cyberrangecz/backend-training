package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.repository.AccessTokenRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;

import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.service.impl.TrainingInstanceServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
public class TrainingInstanceServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingInstanceService trainingInstanceService;

    @Mock
    private TrainingInstanceRepository trainingInstanceRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private TrainingRunRepository trainingRunRepository;

    private TrainingInstance trainingInstance1, trainingInstance2, trainingInstanceInvalid;

    private TrainingRun trainingRun1, trainingRun2;

    private String accessToken = "1asd2sdASD12dSv5S5a4sd5sad45FFe54hLOFE4547fe54Fe5f";

    @Mock
    private UserRefRepository organizerRefRepository;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstanceService = new TrainingInstanceServiceImpl(trainingInstanceRepository, accessTokenRepository, restTemplate,
                trainingRunRepository, organizerRefRepository);

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTitle("test1");
        trainingInstance1.setAccessToken(accessToken);
        trainingInstance1.setEndTime(LocalDateTime.now().minusHours(1L));

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTitle("test2");
        trainingInstance2.setStartTime(LocalDateTime.now().plusHours(1L));
        trainingInstance2.setEndTime(LocalDateTime.now().plusHours(5L));
        trainingInstance2.setAccessToken("pass-1253");

        trainingInstanceInvalid = new TrainingInstance();
        trainingInstanceInvalid.setId(3L);
        trainingInstanceInvalid.setTitle("test3Invalid");
        trainingInstanceInvalid.setStartTime(LocalDateTime.now().minusHours(1L));
        trainingInstanceInvalid.setEndTime(LocalDateTime.now().plusHours(1L));

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setTrainingInstance(trainingInstance1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setTrainingInstance(trainingInstance1);
    }

    @Test
    public void getTrainingInstanceById() {
        given(trainingInstanceRepository.findById(trainingInstance1.getId())).willReturn(Optional.of(trainingInstance1));

        TrainingInstance tI = trainingInstanceService.findById(trainingInstance1.getId());
        deepEquals(trainingInstance1, tI);

        then(trainingInstanceRepository).should().findById(trainingInstance1.getId());
    }

    @Test
    public void getNonexistentTrainingInstanceById() {
        Long id = 6L;
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Training instance with id: " + id + " not found.");
        trainingInstanceService.findById(id);
    }

    @Test
    public void findAll() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page p = new PageImpl<TrainingInstance>(expected);
        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingInstanceService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void createTrainingInstance() {
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        TrainingInstance tI = trainingInstanceService.create(trainingInstance2);
        deepEquals(trainingInstance2, tI);
        then(trainingInstanceRepository).should().save(trainingInstance2);
    }

    @Test
    public void createTrainingInstanceWithNull() {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("Input training instance must not be null");
      trainingInstanceService.create(null);
    }

    @Test
    public void updateTrainingInstance() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));

        trainingInstanceService.update(trainingInstance2);

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(trainingInstance2);
    }

    @Test
    public void deleteTrainingInstance() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance1));

        trainingInstanceService.delete(trainingInstance1.getId());

        then(trainingInstanceRepository).should().findById(trainingInstance1.getId());
        then(trainingInstanceRepository).should().delete(trainingInstance1);
    }

    @Test
    public void deleteTrainingInstaceWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training instance id" +
                " must not be null");
        trainingInstanceService.delete(null);
    }

    @Test(expected = ServiceLayerException.class)
    public void deleteTrainingInstance_withEndTimeInTheFuture() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstanceInvalid));

        trainingInstanceService.delete(trainingInstanceInvalid.getId());

        then(trainingInstanceRepository).should().findById(trainingInstanceInvalid.getId());
        then(trainingInstanceRepository).should().delete(trainingInstanceInvalid);
    }

    @Test
    public void findTrainingRunsbyTrainingInstance() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page p = new PageImpl<>(expected);

        given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findTrainingRunsbyTrainingInstance_notContainedId() {
        Page p = new PageImpl<>(new ArrayList<>());

        given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingInstanceService.findTrainingRunsByTrainingInstance(10L, PageRequest.of(0, 2));
        assertEquals(0, pr.getTotalElements());
    }

    @After
    public void after() {
        reset(trainingInstanceRepository);
    }

    private void deepEquals(TrainingInstance expected, TrainingInstance actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

}
