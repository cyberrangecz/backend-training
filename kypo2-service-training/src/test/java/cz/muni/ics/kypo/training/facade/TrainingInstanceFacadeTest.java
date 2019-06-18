package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TrainingInstanceMapperImpl.class, TrainingRunMapperImpl.class, SandboxInstanceRefMapperImpl.class,
        TrainingDefinitionMapper.class, UserRefMapper.class, TrainingDefinitionMapperImpl.class,
        UserRefMapperImpl.class, BetaTestingGroupMapperImpl.class})
public class TrainingInstanceFacadeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingInstanceFacade trainingInstanceFacade;

    @Autowired
    TrainingRunMapperImpl trainingRunMapper;

    @Autowired
    TrainingInstanceMapper trainingInstanceMapper;

    @Mock
    private TrainingInstanceService trainingInstanceService;

    @Mock
    private TrainingDefinitionService trainingDefinitionService;

    private TrainingInstance trainingInstance1, trainingInstance2;
    private SandboxInstanceRef sandboxInstanceRef1, sandboxInstanceRef2;
    private TrainingInstanceCreateDTO trainingInstanceCreate;
    private TrainingInstanceUpdateDTO trainingInstanceUpdate;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstanceFacade = new TrainingInstanceFacadeImpl(trainingInstanceService, trainingDefinitionService, trainingInstanceMapper, trainingRunMapper);

        sandboxInstanceRef1 = new SandboxInstanceRef();
        sandboxInstanceRef1.setSandboxInstanceRef(5L);
        sandboxInstanceRef1.setId(1L);

        sandboxInstanceRef2 = new SandboxInstanceRef();
        sandboxInstanceRef2.setSandboxInstanceRef(6L);
        sandboxInstanceRef2.setId(2L);

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTitle("test");
        trainingInstance1.addSandboxInstanceRef(sandboxInstanceRef1);
        trainingInstance1.addSandboxInstanceRef(sandboxInstanceRef2);
        trainingInstance1.setPoolId(1L);
        trainingInstance1.setPoolSize(2);

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTitle("test");

        trainingInstanceCreate = new TrainingInstanceCreateDTO();
        trainingInstanceCreate.setTitle("test");
        trainingInstanceCreate.setOrganizersLogin(new HashSet<>());
        trainingInstanceCreate.setTrainingDefinitionId(1L);

        trainingInstanceUpdate = new TrainingInstanceUpdateDTO();
        trainingInstanceUpdate.setId(1L);
        trainingInstanceUpdate.setTitle("title");
        trainingInstanceUpdate.setAccessToken("hello");
        trainingInstanceUpdate.setPoolSize(20);
        trainingInstanceUpdate.setEndTime(LocalDateTime.now());
        trainingInstanceUpdate.setStartTime(LocalDateTime.now());
        trainingInstanceUpdate.setTrainingDefinitionId(1L);
        trainingInstanceUpdate.setOrganizersLogin(new HashSet<>());
    }
    
    @Test
    public void findTrainingInstanceById() {
        given(trainingInstanceService.findById(any(Long.class))).willReturn(trainingInstance1);
        trainingInstanceFacade.findById(trainingInstance1.getId());
        then(trainingInstanceService).should().findByIdIncludingDefinition(trainingInstance1.getId());
    }

    @Test
    public void findNonexistentTrainingInstanceById() {
        willThrow(ServiceLayerException.class).given(trainingInstanceService).findByIdIncludingDefinition(1L);
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.findById(1L);
    }

    @Test
    public void findAllTrainingInstances() {
        List<TrainingInstance> expected = new ArrayList<>();
        expected.add(trainingInstance1);
        expected.add(trainingInstance2);

        Page p = new PageImpl<TrainingInstance>(expected);

        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceService.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        PageResultResource<TrainingInstanceDTO> trainingInstanceDTO = trainingInstanceFacade.findAll(predicate, PageRequest.of(0, 2));
        deepEquals(trainingInstance1, trainingInstanceDTO.getContent().get(0));
        deepEquals(trainingInstance2, trainingInstanceDTO.getContent().get(1));

        then(trainingInstanceService).should().findAll(predicate, PageRequest.of(0, 2));
    }


    @Test
    public void createTrainingInstance() {
        given(trainingInstanceService.create(any(TrainingInstance.class))).willReturn(trainingInstance1);
        given(trainingDefinitionService.findById(any(Long.class))).willReturn(new TrainingDefinition());
        given(trainingInstanceService.findUserRefsByLogins(any(Set.class))).willReturn(new HashSet());
        trainingInstanceFacade.create(trainingInstanceCreate);
        then(trainingInstanceService).should().create(any(TrainingInstance.class));
    }

    @Test
    public void createTrainingInstanceWithNull() {
        thrown.expect(NullPointerException.class);
        trainingInstanceFacade.create(null);
    }

    @Test
    public void updateTrainingInstance() {
        trainingInstanceFacade.update(trainingInstanceUpdate);
        then(trainingInstanceService).should().update(any(TrainingInstance.class));
    }

    @Test
    public void updateTrainingInstanceWithNull() {
        thrown.expect(NullPointerException.class);
        trainingInstanceFacade.update(null);
    }

    @Test
    public void updateTrainingInstanceWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingInstanceService).update(any(TrainingInstance.class));
        trainingInstanceFacade.update(trainingInstanceUpdate);
    }

    @Test
    public void deleteTrainingInstance() {
        trainingInstanceFacade.delete(1L);
        then(trainingInstanceService).should().delete(1L);
    }

    @Test
    public void deleteTrainingInstanceWithNull() {
        thrown.expect(NullPointerException.class);
        trainingInstanceFacade.delete(null);
    }

    @Test
    public void deleteTrainingInstanceWithFacadeLayerException() {
        thrown.expect(FacadeLayerException.class);
        willThrow(ServiceLayerException.class).given(trainingInstanceService).delete(1L);
        trainingInstanceFacade.delete(1L);
    }

    @Test
    public void createPool() {
        given(trainingInstanceService.createPoolForSandboxes(trainingInstance1.getId())).willReturn(5L);
        long poolId = trainingInstanceFacade.createPoolForSandboxes(trainingInstance1.getId());

        assertEquals(5L, poolId);
    }

    @Test
    public void createPoolWithServiceException() {
        willThrow(ServiceLayerException.class).given(trainingInstanceService).createPoolForSandboxes(trainingInstance1.getId());
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.createPoolForSandboxes(trainingInstance1.getId());
    }

    @Test
    public void allocateSandboxes() {
        trainingInstance1.setSandboxInstanceRefs(Set.of(sandboxInstanceRef1));
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        trainingInstanceFacade.allocateSandboxes(trainingInstance1.getId(), null);
        then(trainingInstanceService).should().allocateSandboxes(trainingInstance1, null);
    }

    @Test
    public void allocateSandboxesWithFullPool() {
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("cz.muni.ics.kypo.training.exceptions.ServiceLayerException: Pool of sandboxes of training instance with " +
                "id: " + trainingInstance1.getId() + " is full.");
        trainingInstanceFacade.allocateSandboxes(trainingInstance1.getId(), null);
        then(trainingInstanceService).should().allocateSandboxes(trainingInstance1, null);
    }

    @Test
    public void allocateSandboxesWithServiceException() {
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        willThrow(ServiceLayerException.class).given(trainingInstanceService).allocateSandboxes(trainingInstance1, null);
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.allocateSandboxes(trainingInstance1.getId(), null);
    }

    @Test
    public void deleteSandboxes() {
        Set<Long> ids = new HashSet<>();
        ids.add(sandboxInstanceRef1.getSandboxInstanceRef());
        ids.add(sandboxInstanceRef2.getSandboxInstanceRef());
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        trainingInstanceFacade.deleteSandboxes(trainingInstance1.getId(), ids);
        then(trainingInstanceService).should().deleteSandbox(trainingInstance1, sandboxInstanceRef1);
        then(trainingInstanceService).should().deleteSandbox(trainingInstance1, sandboxInstanceRef2);
    }

    @Test
    public void reallocateSandboxWhenPoolIsFull() {
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        thrown.expect(FacadeLayerException.class);
        thrown.expectMessage("cz.muni.ics.kypo.training.exceptions.ServiceLayerException: Sandbox cannot be reallocated because pool of training instance with id: " + trainingInstance1.getId() + " is full. Given sandbox with " +
                "id: " + sandboxInstanceRef1.getSandboxInstanceRef() + " is probably in the process of removing right now. " +
                "Please wait and try allocate new sandbox later or contact administrator.");
        trainingInstanceFacade.reallocateSandbox(trainingInstance1.getId(), sandboxInstanceRef1.getSandboxInstanceRef());
        then(trainingInstanceService).should().deleteSandbox(trainingInstance1, sandboxInstanceRef1);
        then(trainingInstanceService).should().allocateSandboxes(trainingInstance1, 1);
    }

    @Test
    public void reallocateSandboxWhenPoolIsNotFull() {
        trainingInstance1.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1)));
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        trainingInstanceFacade.reallocateSandbox(trainingInstance1.getId(), sandboxInstanceRef1.getSandboxInstanceRef());
        then(trainingInstanceService).should().deleteSandbox(trainingInstance1, sandboxInstanceRef1);
        then(trainingInstanceService).should().allocateSandboxes(trainingInstance1, 1);
    }

    @Test
    public void reallocateSandboxWithTrainingInstanceNotFound() {
        willThrow(ServiceLayerException.class).given(trainingInstanceService).findById(trainingInstance1.getId());
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.reallocateSandbox(trainingInstance1.getId(), sandboxInstanceRef1.getSandboxInstanceRef());
    }

    @Test
    public void deleteSandboxesWithServiceException() {
        Set<Long> ids = new HashSet<>();
        ids.add(sandboxInstanceRef1.getSandboxInstanceRef());
        given(trainingInstanceService.findById(anyLong())).willReturn(trainingInstance1);
        willThrow(ServiceLayerException.class).given(trainingInstanceService).deleteSandbox(trainingInstance1, sandboxInstanceRef1);
        thrown.expect(FacadeLayerException.class);
        trainingInstanceFacade.deleteSandboxes(trainingInstance1.getId(), ids);
    }

    private void deepEquals(TrainingInstance expected, TrainingInstanceDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

}
