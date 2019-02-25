package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.repository.AccessTokenRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;

import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.service.impl.TrainingInstanceServiceImpl;
import cz.muni.ics.kypo.training.utils.SandboxInfo;
import cz.muni.ics.kypo.training.utils.SandboxPoolInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;
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
    @Mock
    private UserRefRepository organizerRefRepository;

    @Mock
    private TrainingDefinition trainingDefinition;
    @Mock
    private SandboxPoolInfo sandboxPoolInfo;
    @Mock
    private SandboxInstanceRef sandboxInstanceRef1, sandboxInstanceRef2;
    private SandboxInfo sandboxInfo;
    private TrainingInstance trainingInstance1, trainingInstance2, trainingInstanceInvalid;
    private TrainingRun trainingRun1, trainingRun2;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstanceService = new TrainingInstanceServiceImpl(trainingInstanceRepository, accessTokenRepository,
                trainingRunRepository, organizerRefRepository, restTemplate);

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTitle("test1");
        trainingInstance1.setAccessToken("pass-9876");
        trainingInstance1.setEndTime(LocalDateTime.now().minusHours(1L));
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        trainingInstance1.setPoolSize(2);
        trainingInstance1.setPoolId(1L);

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTitle("test2");
        trainingInstance2.setStartTime(LocalDateTime.now().plusHours(1L));
        trainingInstance2.setEndTime(LocalDateTime.now().plusHours(5L));
        trainingInstance2.setAccessToken("pass-1253");
        trainingInstance2.setTrainingDefinition(trainingDefinition);

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

        sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(2L);
        sandboxInfo.setStatus("CREATE_COMPLETE");
        sandboxInfo.setPool(5L);
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
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Training instance with id: " + 10L + " not found.");
        trainingInstanceService.findById(10L);
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

        String token = trainingInstanceService.update(trainingInstance2);

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(trainingInstance2);
        assertEquals(trainingInstance2.getAccessToken(), token);
    }

    @Test
    public void deleteTrainingInstance() {
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));

        trainingInstanceService.delete(trainingInstance2.getId());

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().delete(trainingInstance2);
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
    public void findTrainingRunsByTrainingInstance() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_notContainedId() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Training instance with id: 10 not found.");
        //Page p = new PageImpl<>(new ArrayList<>());

        //given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);

        //Page pr = trainingInstanceService.findTrainingRunsByTrainingInstance(10L, PageRequest.of(0, 2));
        //assertEquals(0, pr.getTotalElements());
        trainingInstanceService.findTrainingRunsByTrainingInstance(10L, PageRequest.of(0, 2));
    }

    //TODO deal with Thread.sleep maybe with PowerMock
//    @Test
//    public void allocateSandboxes() {
//        when(trainingDefinition.getSandboxDefinitionRefId()).thenReturn(1L);
//        when(sandboxPoolInfo.getId()).thenReturn(4L);
//
//        given(trainingInstanceRepository.findById(trainingInstance1.getId())).willReturn(Optional.ofNullable(trainingInstance1));
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(SandboxPoolInfo.class))).
//                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.OK));
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(Collections.singletonList(sandboxInfo)), HttpStatus.OK));
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(Collections.singletonList(sandboxInfo)), HttpStatus.OK));
//        trainingInstanceService.allocateSandboxes(trainingInstance1.getId());
//        assertTrue(trainingInstance1.getSandboxInstanceRefs().stream().anyMatch(s -> s.getSandboxInstanceRef().equals(2L)));
//    }

    @Test
    public void allocateSandboxesWithNotCreatedPool() {
        given(trainingInstanceRepository.findById(trainingInstance2.getId())).willReturn(Optional.ofNullable(trainingInstance2));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Pool for sandboxes is not created yet. Please create pool before allocating sandboxes.");
        trainingInstanceService.allocateSandboxes(trainingInstance2.getId());
    }


    @Test
    public void allocateSandboxesWithFullPool() {
        trainingInstance1.addSandboxInstanceRef(sandboxInstanceRef1);
        trainingInstance1.addSandboxInstanceRef(sandboxInstanceRef2);
        given(trainingInstanceRepository.findById(trainingInstance1.getId())).willReturn(Optional.ofNullable(trainingInstance1));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Pool of sandboxes of training instance with id: " + trainingInstance1.getId() + " is full.");
        trainingInstanceService.allocateSandboxes(trainingInstance1.getId());
    }

    @Test
    public void createPoolForSandboxes() {
        when(sandboxPoolInfo.getId()).thenReturn(4L);

        given(trainingInstanceRepository.findById(trainingInstance2.getId())).willReturn(Optional.ofNullable(trainingInstance2));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(SandboxPoolInfo.class))).
                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.OK));
        Long poolId = trainingInstanceService.createPoolForSandboxes(trainingInstance2.getId());

        assertEquals(sandboxPoolInfo.getId(), poolId);
    }

    @Test
    public void createPoolWithErrorFromOpenStack() {
        given(trainingInstanceRepository.findById(trainingInstance2.getId())).willReturn(Optional.ofNullable(trainingInstance2));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(SandboxPoolInfo.class))).
                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.CONFLICT));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Error from openstack while creating pool.");
        trainingInstanceService.createPoolForSandboxes(trainingInstance2.getId());
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
