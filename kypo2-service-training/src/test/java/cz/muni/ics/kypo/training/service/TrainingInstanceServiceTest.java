package cz.muni.ics.kypo.training.service;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.muni.csirt.kypo.elasticsearch.service.TrainingEventsService;
import cz.muni.ics.kypo.commons.security.enums.AuthenticatedUserOIDCItems;
import cz.muni.ics.kypo.training.api.responses.PageResultResourcePython;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.api.responses.SandboxPoolInfo;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.AccessTokenRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.UserRefRepository;
import cz.muni.ics.kypo.training.service.impl.SecurityService;
import cz.muni.ics.kypo.training.service.impl.TrainingInstanceServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;

/**
 * @author Boris Jadus(445343)
 */

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
    private SecurityService securityService;
    @Mock
    private TrainingEventsService trainingEventsService;
    @Mock
    private TrainingDefinition trainingDefinition;
    @Mock
    private SandboxPoolInfo sandboxPoolInfo;
    private SandboxInfo sandboxInfo1 ,sandboxInfo2;
    private TrainingInstance trainingInstance1, trainingInstance2, trainingInstanceInvalid, trainingInstanceInvalidTime, currentInstance,
            instanceWithSB;
    private TrainingRun trainingRun1, trainingRun2;
    private UserRef user;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingInstanceService = new TrainingInstanceServiceImpl(trainingInstanceRepository, accessTokenRepository,
                trainingRunRepository, organizerRefRepository, restTemplate, securityService, trainingEventsService);

        trainingInstance1 = new TrainingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTitle("test1");
        trainingInstance1.setAccessToken("pass-9876");
        trainingInstance1.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(5L));
        trainingInstance1.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        trainingInstance1.setPoolSize(2);
        trainingInstance1.setPoolId(1L);

        instanceWithSB = new TrainingInstance();
        instanceWithSB.setId(5L);
        instanceWithSB.setTitle("test5");
        instanceWithSB.setAccessToken("pass-9999");
        instanceWithSB.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(5L));
        instanceWithSB.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        instanceWithSB.setTrainingDefinition(trainingDefinition);
        instanceWithSB.setPoolSize(2);
        instanceWithSB.setPoolId(1L);

        user = new UserRef();

        trainingInstance2 = new TrainingInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTitle("test2");
        trainingInstance2.setStartTime(LocalDateTime.now(Clock.systemUTC()).plusHours(1L));
        trainingInstance2.setEndTime(LocalDateTime.now(Clock.systemUTC()).plusHours(5L));
        trainingInstance2.setAccessToken("pass-1253");
        trainingInstance2.setTrainingDefinition(trainingDefinition);
        trainingInstance2.setPoolId(null);

        trainingInstanceInvalid = new TrainingInstance();
        trainingInstanceInvalid.setId(3L);
        trainingInstanceInvalid.setTitle("test3Invalid");
        trainingInstanceInvalid.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstanceInvalid.setEndTime(LocalDateTime.now(Clock.systemUTC()).plusHours(1L));

        trainingInstanceInvalidTime = new TrainingInstance();
        trainingInstanceInvalidTime.setId(4L);
        trainingInstanceInvalidTime.setTitle("test4InvalidTime");
        trainingInstanceInvalidTime.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1L));
        trainingInstanceInvalidTime.setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(10L));

        currentInstance = new TrainingInstance();
        currentInstance.setId(5L);
        currentInstance.setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(5L));
        currentInstance.setEndTime(LocalDateTime.now(Clock.systemUTC()).plusHours(5L));

        trainingRun1 = new TrainingRun();
        trainingRun1.setId(1L);
        trainingRun1.setTrainingInstance(trainingInstance1);

        trainingRun2 = new TrainingRun();
        trainingRun2.setId(2L);
        trainingRun2.setTrainingInstance(trainingInstance1);

        sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId(2L);
        sandboxInfo1.setStatus("CREATE_COMPLETE");
        sandboxInfo1.setPool(5L);

        sandboxInfo2 = new SandboxInfo();
        sandboxInfo2.setId(3L);
        sandboxInfo2.setStatus("CREATE_COMPLETE");
        sandboxInfo2.setPool(5L);
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
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(1L);

        Page p = new PageImpl<TrainingInstance>(expected);
        PathBuilder<TrainingInstance> tI = new PathBuilder<TrainingInstance>(TrainingInstance.class, "trainingInstance");
        Predicate predicate = tI.isNotNull();

        given(trainingInstanceRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(p);

        Page pr = trainingInstanceService.findAll(predicate, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void createTrainingInstance() {
        mockSpringSecurityContextForGet();
        given(trainingInstanceRepository.save(trainingInstance2)).willReturn(trainingInstance2);
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(SandboxPoolInfo.class))).
                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.OK));
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
    public void createTrainingInstanceWithInvalidTimes() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("End time must be later than start time.");
        trainingInstanceService.create(trainingInstanceInvalidTime);
    }

    @Test
    public void updateTrainingInstance() {
        mockSpringSecurityContextForGet();
        given(trainingInstanceRepository.findById(any(Long.class))).willReturn(Optional.of(trainingInstance2));
        given(organizerRefRepository.save(any(UserRef.class))).willReturn(user);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(user);

        String token = trainingInstanceService.update(trainingInstance2);

        then(trainingInstanceRepository).should().findById(trainingInstance2.getId());
        then(trainingInstanceRepository).should().save(trainingInstance2);
        assertEquals(trainingInstance2.getAccessToken(), token);
    }

    @Test
    public void updateTrainingInstanceWithInvalidTimes() {
        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstanceInvalidTime));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("End time must be later than start time.");
        trainingInstanceService.update(trainingInstanceInvalidTime);
    }

    @Test
    public void deleteTrainingInstance() {
        trainingInstanceService.delete(trainingInstance2);
        then(trainingInstanceRepository).should().delete(trainingInstance2);
    }

    @Test
    public void deleteTrainingInstanceWithNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Input training instance" +
                " must not be null");
        trainingInstanceService.delete(null);
    }

    @Test
    public void deleteTrainingInstanceWithAssignedTrainingRuns() {
        List<TrainingRun> runs = new ArrayList<>();
        runs.add(trainingRun1);
        runs.add(trainingRun2);
        Page p = new PageImpl<>(runs);

        given(trainingRunRepository.existsAnyForTrainingInstance(trainingInstance1.getId())).willReturn(true);

        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Training instance with already assigned training runs cannot be deleted. Please delete training runs assigned to training instance and try again or contact administrator.");
        trainingInstanceService.delete(trainingInstance1);
    }

    @Test
    public void deleteInstanceWithAssignedSandboxes() {
        PageResultResourcePython<SandboxInfo> pythonPage = new PageResultResourcePython<SandboxInfo>();
        pythonPage.setResults(List.of(sandboxInfo1, sandboxInfo2));

        given(trainingRunRepository.existsAnyForTrainingInstance(instanceWithSB.getId())).willReturn(false);
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(pythonPage, HttpStatus.OK));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Cannot delete training instance because it contains some sandboxes. Please delete sandboxes and try again or wait until all sandboxes are deleted from OpenStack.");
        trainingInstanceService.delete(instanceWithSB);
    }

    @Test
    public void findTrainingRunsByTrainingInstance() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page p = new PageImpl<>(expected);

        given(trainingInstanceRepository.findById(anyLong())).willReturn(Optional.of(trainingInstance1));
        given(trainingRunRepository.findAllByTrainingInstanceId(any(Long.class), any(Pageable.class))).willReturn(p);
        Page pr = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstance1.getId(), null, PageRequest.of(0, 2));
        assertEquals(2, pr.getTotalElements());
    }

    @Test
    public void findTrainingRunsByTrainingInstance_notContainedId() {
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Training instance with id: 10 not found.");
        trainingInstanceService.findTrainingRunsByTrainingInstance(10L, null, PageRequest.of(0, 2));
    }

    @Test
    public void allocateSandboxesWithNotCreatedPool() {
        given(trainingInstanceRepository.findById(trainingInstance2.getId())).willReturn(Optional.ofNullable(trainingInstance2));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Pool for sandboxes is not created yet. Please create pool before allocating sandboxes.");
        trainingInstanceService.allocateSandboxes(trainingInstance2, null);
    }


    @Test
    public void allocateSandboxesWithFullPool() {
        PageResultResourcePython<SandboxInfo> pythonPage = new PageResultResourcePython<SandboxInfo>();
        pythonPage.setResults(List.of(sandboxInfo1, sandboxInfo2));

        given(trainingInstanceRepository.findById(instanceWithSB.getId())).willReturn(Optional.ofNullable(instanceWithSB));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity<PageResultResourcePython<SandboxInfo>>(pythonPage, HttpStatus.OK));
        thrown.expect(ServiceLayerException.class);
        thrown.expectMessage("Pool of sandboxes of training instance with id: " + instanceWithSB.getId() + " is full.");
        trainingInstanceService.allocateSandboxes(instanceWithSB, 1);
    }

    @After
    public void after() {
        reset(trainingInstanceRepository);
    }

    private void deepEquals(TrainingInstance expected, TrainingInstance actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTitle(), actual.getTitle());
    }

    private void mockSpringSecurityContextForGet() {
        JsonObject sub = new JsonObject();
        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), "participant");
        sub.addProperty(AuthenticatedUserOIDCItems.NAME.getName(), "Pavel");
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2Authentication auth = Mockito.mock(OAuth2Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(auth);
        given(auth.getUserAuthentication()).willReturn(auth);
        given(auth.getCredentials()).willReturn(sub);
        given(auth.getAuthorities()).willReturn(Arrays.asList(new SimpleGrantedAuthority("ADMINISTRATOR")));
        given(authentication.getDetails()).willReturn(auth);
    }

}
